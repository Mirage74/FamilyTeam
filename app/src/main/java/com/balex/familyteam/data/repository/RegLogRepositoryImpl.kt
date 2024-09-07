package com.balex.familyteam.data.repository

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.balex.familyteam.data.datastore.Storage
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.Language
import com.balex.familyteam.domain.entity.LanguagesList
import com.balex.familyteam.domain.entity.RegistrationOption
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.RegLogRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject


class RegLogRepositoryImpl @Inject constructor(

    private val context: Context
) : RegLogRepository {

    private var admin = Admin()

    private var user = User()

    private var language = Language.DEFAULT_LANGUAGE.symbol

    private var isUserMailOrPhoneVerified = false

    private val isCurrentUserNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
    private val isCurrentLanguageNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
    private val isSmsVerificationErrorNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)


    private var _isSmsVerificationError = SMS_VERIFICATION_ERROR_INITIAL
    private val isSmsVerificationError: String
        get() = _isSmsVerificationError

    private val db = Firebase.firestore
    private val adminsCollection = db.collection(FIREBASE_ADMINS_COLLECTION)
    private val usersCollection = db.collection(FIREBASE_USERS_COLLECTION)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)


    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun observeUser(): StateFlow<User> = flow {
        Storage.clearPreferences(context)

        val userTest = User(
            nickName = "qw",
            adminEmailOrPhone = "balexvicx@gmail.com",
            password = "111111",
            displayName = "rr",
            fakeEmail = "qw-balexvicx@gmail.com"
        )
        signToFirebaseWithEmailAndPassword(userTest)


        val userFakeEmailFromStorage = Storage.getUser(context)
        //findUser(userFakeEmailFromStorage)
        val phoneLanguageFromStorage = Storage.getLanguage(context)
        val phoneLang =
            if (phoneLanguageFromStorage != Storage.NO_LANGUAGE_SAVED_IN_SHARED_PREFERENCES) {
                language = phoneLanguageFromStorage
                isCurrentLanguageNeedRefreshFlow.emit(Unit)
                phoneLanguageFromStorage
            } else {
                language = getCurrentLanguage(context)
                language
            }

        if (userFakeEmailFromStorage == Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES) {
            val emptyUserNotSaved =
                User(nickName = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES, language = phoneLang)
            user = emptyUserNotSaved
        } else {
            val userFromStorage = extractUserInfoFromFakeEmail(userFakeEmailFromStorage)

            user = userFromStorage

        }
        isCurrentUserNeedRefreshFlow.emit(Unit)

        isCurrentUserNeedRefreshFlow.collect {
            emit(user)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = user
        )

    override fun observeLanguage(): StateFlow<String> = flow {
        val phoneLanguageFromStorage = Storage.getLanguage(context)
        val phoneLang =
            if (phoneLanguageFromStorage != Storage.NO_LANGUAGE_SAVED_IN_SHARED_PREFERENCES) {
                language = phoneLanguageFromStorage
                phoneLanguageFromStorage
            } else {
                getCurrentLanguage(context)
            }
        language = phoneLang
        isCurrentLanguageNeedRefreshFlow.emit(Unit)

        isCurrentLanguageNeedRefreshFlow.collect {
            emit(language)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = language
        )


    override fun observeSmsVerificationError(): StateFlow<String> = flow {

        isSmsVerificationErrorNeedRefreshFlow.emit(Unit)

        isSmsVerificationErrorNeedRefreshFlow.collect {
            emit(_isSmsVerificationError)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = isSmsVerificationError
        )

    override fun getRepoUser(): User {
        return user
    }

    override fun setUserAsVerified() {
        isUserMailOrPhoneVerified = true
    }

    override fun saveLanguage(language: String) {
        this.language = language
        Storage.saveLanguage(context, language)
    }

    override fun getCurrentLanguage(): String {
        return language
    }

    override suspend fun addAdminToCollection(admin: Admin): Result<Unit> {
        return try {
            val adminDocument = adminsCollection.document(admin.emailOrPhoneNumber)
            adminDocument.set(admin).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addUserToCollection(userToAdd: User): Result<Unit> {
        return try {
            val newUser = if (userToAdd.fakeEmail == User.DEFAULT_FAKE_EMAIL) {
                userToAdd.copy(fakeEmail = createFakeUserEmail(userToAdd.nickName, userToAdd.adminEmailOrPhone))
            } else {
                userToAdd
            }
            val userCollection =
                usersCollection.document(newUser.adminEmailOrPhone).collection(newUser.nickName)
                    .document(newUser.nickName)
            userCollection.set(newUser).await()
            Storage.saveUser(context, createFakeUserEmail(newUser.nickName, newUser.adminEmailOrPhone))
            Storage.saveUsersPassword(context, newUser.password)
            Storage.saveLanguage(context, language)
            user = newUser
            isCurrentUserNeedRefreshFlow.emit(Unit)
            Result.success(Unit)
        } catch (e: Exception) {
            //Result.failure(e)
            throw RuntimeException("addUserToCollection: $ERROR_ADD_USER_TO_FIREBASE")
        }
    }


//    private fun isAdminExistInFirebase(emailOrPhone: String): Boolean {
//
//    }


    override suspend fun signToFirebaseWithEmailAndPassword(userToSignIn: User): StatusSignIn  {
        if (userToSignIn.fakeEmail != User.DEFAULT_FAKE_EMAIL) {
            try {
                FirebaseAuth.getInstance().signOut()

                val authRes = auth.signInWithEmailAndPassword(userToSignIn.fakeEmail, userToSignIn.password).await()
                val firebaseUser = authRes.user
                if (firebaseUser != null) {
                    val userFromCollection = findUserInCollection(userToSignIn)
                    if (userFromCollection != null) {
                        if (userFromCollection.nickName != User.DEFAULT_NICK_NAME) {
                            user = userFromCollection
                            isCurrentUserNeedRefreshFlow.emit(Unit)

                        } else {
                            val newUser = User(
                                fakeEmail = userToSignIn.fakeEmail,
                                displayName = userToSignIn.displayName,
                                password = userToSignIn.password,
                                adminEmailOrPhone = extractUserInfoFromFakeEmail(userToSignIn.fakeEmail).adminEmailOrPhone
                            )
                            val result = addUserToCollection(newUser)
                            if (result.isSuccess) {
                                user = newUser
                                isCurrentUserNeedRefreshFlow.emit(Unit)
                            }
                        }
                        return StatusSignIn.USER_SIGNED_IN
                    } else {
                        Storage.clearPreferences(context)
                        Log.d("signToFirebaseInWithEmailAndPassword", "No user with email ${userToSignIn.fakeEmail}, nickName ${userToSignIn.nickName} found in Firebase")
                        return StatusSignIn.ERROR_GET_USER_FROM_COLLECTION
                    }
                }

            } catch (e: FirebaseAuthException) {
                val errCode = e.errorCode.trim()
                if (errCode == "ERROR_INVALID_CREDENTIAL" || errCode == "ERROR_USER_NOT_FOUND") {
                    return StatusSignIn.USER_NOT_FOUND
                } else {
                    Storage.clearPreferences(context)
                    Log.e("signToFirebaseInWithEmailAndPassword, Error", e.message ?: "Unknown error")
                    return StatusSignIn.OTHER_ERROR
                }
            }
        }
        return StatusSignIn.OTHER_ERROR
    }


    override suspend fun registerAndVerifyNewTeamByEmail(
        email: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        FirebaseAuth.getInstance().signOut()


        val document = isNewTeam(email, nickName, password)


        //if (document != null) {
        if (false) {
//            val newUser = document.toObject(User::class.java) ?: User()
//
//            val newAdmin = Admin(
//                registrationOption = RegistrationOption.EMAIL,
//                emailOrPhoneNumber = newUser.adminEmailOrPhone,
//                isEmailOrPhoneNumberVerified = true
//            )
//
//            admin = newAdmin
//            user = newUser
//            isCurrentUserNeedRefreshFlow.emit(Unit)

        } else {
            try {
                val authResult = withContext(Dispatchers.IO) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                }

                val authUser = authResult.user
                    ?: throw RuntimeException("registerAndVerifyNewTeamByEmail: $AUTH_USER_NOT_FOUND")

                authUser.sendEmailVerification().await()

                withTimeoutOrNull(TIMEOUT_VERIFICATION_MAIL) {
                    while (!authUser.isEmailVerified) {
                        delay(TIMEOUT_VERIFICATION_CHECK)
                        authUser.reload().await()
                        if (authUser.isEmailVerified) {
                            regUserWithFakeEmailToAuthAndToUsersCollection(
                                email,
                                nickName,
                                displayName,
                                password
                            )
                            isUserMailOrPhoneVerified = true
                            return@withTimeoutOrNull
                        }
                    }
                } ?: throw RuntimeException("registerAndVerifyByEmail: Verification timed out")

            } catch (e: FirebaseAuthUserCollisionException) {
                throw RuntimeException("registerAndVerifyByEmail: $USER_ALREADY_EXIST_IN_AUTH_BUT_NOT_IN_USERS")
            } catch (e: Exception) {
                throw RuntimeException("registerAndVerifyByEmail: $REGISTRATION_ERROR: ${e.message}")
            }
        }
    }

    override suspend fun regUserWithFakeEmailToAuthAndToUsersCollection(
        emailOrPhone: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val auth: FirebaseAuth = Firebase.auth
        val fakeEmail = createFakeUserEmail(nickName, emailOrPhone)
        val registrationOption = if (emailOrPhone.contains("@", true)) {
            RegistrationOption.EMAIL
        } else {
            RegistrationOption.PHONE
        }

        try {
            auth.createUserWithEmailAndPassword(fakeEmail, password).await()

            auth.signInWithEmailAndPassword(fakeEmail, password).await()

            addAdminAndUserToFirebase(
                registrationOption,
                emailOrPhone,
                fakeEmail,
                nickName,
                displayName,
                password
            )

        } catch (e: Exception) {
            Log.e("regUserWithFakeEmail", "Error: ${e.message}")
        }
    }


    private fun addAdminAndUserToFirebase(
        registrationOption: RegistrationOption,
        emailOrPhoneNumber: String,
        fakeEmail: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val newAdmin = Admin(
            nickName = nickName,
            registrationOption = registrationOption,
            emailOrPhoneNumber = emailOrPhoneNumber,
            isEmailOrPhoneNumberVerified = true
        )
        val newUser = User(
            nickName = nickName,
            isAdmin = true,
            fakeEmail = fakeEmail,
            adminEmailOrPhone = emailOrPhoneNumber,
            displayName = displayName,
            language = language,
            password = password

        )

        coroutineScope.launch {
            val resultFirebaseRegAdmin = addAdminToCollection(newAdmin)
            if (resultFirebaseRegAdmin.isSuccess) {
                admin = newAdmin
                addUserToCollection(newUser)
            } else {
                throw RuntimeException("addAdminAndUserToFirebase: $ERROR_ADD_ADMIN_TO_FIREBASE")
            }
        }
    }

    override suspend fun emitUserNeedRefresh() {
        coroutineScope.launch {
            isCurrentUserNeedRefreshFlow.emit(Unit)
        }
    }


    private fun getCurrentLanguage(context: Context): String {
        val configuration: Configuration = context.resources.configuration
        val locale =
            configuration.locales[0]
        val supportedLanguages = LanguagesList().languages.map { it.symbol }
        val lang = if (supportedLanguages.contains(locale.language)) {
            locale.language
        } else {
            "en"
        }
        return lang
    }

    private suspend fun findUserInCollection(user: User): User? {

        return try {
            val querySnapshot = usersCollection.document(user.adminEmailOrPhone).collection(user.nickName)
                .whereEqualTo("fakeEmail", user.fakeEmail)
                .whereEqualTo("nickName", user.nickName)
                .get()
                .await()

            var userData = User()
            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0].toObject(User::class.java)?.let {
                    userData = it
                }

            }
            return userData

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("findUserInCollection", "Error: ${e.message}")
            null
        }
    }

    private suspend fun isNewTeam(
        email: String,
        nickName: String,
        password: String
    ): Boolean {


        return try {

            signToFirebaseWithEmailAndPassword(
                User(
                    nickName = "qw",
                    adminEmailOrPhone = "balexvicx@gmail.com",
                    password = "111111",
                    displayName = "rr"                )
            )

            val querySnapshot = usersCollection
                .whereEqualTo("fakeEmail", email)
                .whereEqualTo("nickName", nickName)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0]
                true
            } else {
                null
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
            false
        }

    }


    private fun createFakeUserEmail(nick: String, data: String): String {
        val nameLowCase = nick.lowercase()
        val dataLowCase = data.lowercase()
        val u = if (dataLowCase.contains("@", true)) {
            "$nameLowCase-$dataLowCase".trim()
        } else {
            nameLowCase + "-" + dataLowCase.substring(1) + "@" + FAKE_EMAIL_DOMAIN.trim()
        }
        return if (dataLowCase.contains("@", true)) {
            "$nameLowCase-$dataLowCase".trim()
        } else {
            nameLowCase + "-" + dataLowCase.substring(1) + "@" + FAKE_EMAIL_DOMAIN.trim()
        }
    }

    private fun extractUserInfoFromFakeEmail(fakeEmail: String): User {
        if (fakeEmail.endsWith(FAKE_EMAIL_DOMAIN, false)) {
            val parts = fakeEmail.split("@")
            val nick = parts[0].split("-").first()
            val phone = parts[1].split("-").first()
            return User(
                nickName = nick,
                fakeEmail = fakeEmail,
                adminEmailOrPhone = phone,
                password = Storage.getUsersPassword(context)
            )
        } else {
            val parts = fakeEmail.split("@")
            val nick = parts[0].split("-").first()
            val indexSplitter = parts[0].indexOf("-")
            val mailBeforeAt = parts[0].substring(indexSplitter + 1)
            return User(
                nickName = nick,
                fakeEmail = fakeEmail,
                adminEmailOrPhone = mailBeforeAt + "@" + parts[1],
                password = Storage.getUsersPassword(context)
            )

        }

    }

    companion object {
        const val TIMEOUT_VERIFICATION_PHONE = 60L
        const val TIMEOUT_VERIFICATION_MAIL = 60000L * 60L * 24L
        const val TIMEOUT_VERIFICATION_CHECK = 15000L

        const val FIREBASE_ADMINS_COLLECTION = "admins"
        const val FIREBASE_USERS_COLLECTION = "users"

        const val SMS_VERIFICATION_ERROR_INITIAL = "SMS_VERIFICATION_ERROR_INITIAL"
        const val FAKE_EMAIL_DOMAIN = "balexvic.com"

        const val ERROR_ADD_USER_TO_FIREBASE = "ERROR_ADD_USER_TO_FIREBASE"
        const val ERROR_ADD_ADMIN_TO_FIREBASE = "ERROR_ADD_ADMIN_TO_FIREBASE"
        const val USER_ALREADY_EXIST_IN_AUTH_BUT_NOT_IN_USERS =
            "user already exist in AUTH, but not in USERS"
        const val REGISTRATION_ERROR = "REGISTRATION ERROR"

        const val AUTH_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
        const val ERROR_INVALID_CREDENTIAL = "ERROR_INVALID_CREDENTIAL"

        enum class StatusSignIn {
            USER_SIGNED_IN,
            ERROR_GET_USER_FROM_COLLECTION,
            USER_NOT_FOUND,
            OTHER_ERROR
        }
    }
}