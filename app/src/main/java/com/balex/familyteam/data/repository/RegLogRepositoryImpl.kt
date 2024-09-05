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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
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
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


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
        val userFakeEmailFromStorage = Storage.getUser(context)
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
            val adminDocument = adminsCollection.document("Balex")
            //adminsCollection.add(admin).await()
            adminDocument.set(admin).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addUserToCollection(user: User): Result<Unit> {
        return try {
            val newUser = if (user.fakeEmail == User.DEFAULT_FAKE_EMAIL) {
                user.copy(fakeEmail = createFakeUserEmail(user.nickName, user.adminEmailOrPhone))
            } else {
                user
            }
            //registerAuthUser(newUser)
            val userSubCollection = usersCollection.document("admin@mail.com").collection(user.nickName)
            //usersCollection.add(newUser).await()
            userSubCollection.add(newUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    private fun isAdminExistInFirebase(emailOrPhone: String): Boolean {
//
//    }


    override suspend fun signRepoCurrentUserToFirebaseWithEmailAndPassword() {
        if (user.fakeEmail != User.DEFAULT_FAKE_EMAIL) {
            try {
                FirebaseAuth.getInstance().signOut()

                auth.signInWithEmailAndPassword(user.fakeEmail, user.password).await()

                val document = findUserByEmailInFirebase(user.fakeEmail, user.nickName)

                if (document != null) {
                    document.toObject(User::class.java)?.let {
                        user = it
                    }

                    isCurrentUserNeedRefreshFlow.emit(Unit)
                } else {
                    Storage.clearPreferences(context)
                    Log.e(
                        "signToFirebaseInWithEmailAndPassword",
                        "No user with email ${user.fakeEmail}, nickName ${user.nickName} found in Firebase"
                    )
                }
            } catch (e: Exception) {
                Storage.clearPreferences(context)
                Log.e("signToFirebaseInWithEmailAndPassword, Error", e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun registerAuthUser(user: User) {
        try {
            val document = findUserByEmailInFirebase(user.fakeEmail, user.nickName)

            if (document == null) {
                try {
                    auth.createUserWithEmailAndPassword(user.fakeEmail, user.password).await()
                } catch (e: FirebaseAuthUserCollisionException) {
                    throw RuntimeException("registerAuthUser: $USER_ALREADY_EXIST_IN_AUTH_BUT_NOT_IN_USERS: ${e.message}")
                } catch (e: Exception) {
                    throw RuntimeException("registerAuthUser: $REGISTRATION_ERROR: ${e.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("registerAuthUser: ${e.message}")
        }
    }

    override suspend fun registerAndVerifyByEmail(
        email: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        FirebaseAuth.getInstance().signOut()

        val document = findUserByEmailInFirebase(email, nickName)

        if (document != null) {
            val newUser = document.toObject(User::class.java) ?: User()

            val newAdmin = Admin(
                registrationOption = RegistrationOption.EMAIL,
                emailOrPhoneNumber = newUser.adminEmailOrPhone,
                isEmailOrPhoneNumberVerified = true
            )

            admin = newAdmin
            user = newUser
            isCurrentUserNeedRefreshFlow.emit(Unit)

        } else {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val authUser = authResult.user
                    ?: throw RuntimeException("registerAndVerifyByEmail: $AUTH_USER_NOT_FOUND")
                authUser.sendEmailVerification().await()

                while (!authUser.isEmailVerified) {
                    authUser.reload().await()
                    if (authUser.isEmailVerified) {
                        regUserWithFakeEmail(email, nickName, displayName, password)
                        isUserMailOrPhoneVerified = true
                    }
                    delay(1000)
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                throw RuntimeException("registerAndVerifyByEmail: $USER_ALREADY_EXIST_IN_AUTH_BUT_NOT_IN_USERS")


            } catch (e: CancellationException) {
                val m = e.message
            }

            catch (e: Exception) {
                throw RuntimeException("registerAndVerifyByEmail: $REGISTRATION_ERROR: ${e.message}")
            }
        }
    }

    override suspend fun regUserWithFakeEmail(
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
                nickName,
                displayName,
                password
            )

        } catch (e: Exception) {
            Log.e("regUserWithFakeEmail", "Error: ${e.message}")
        }
    }

    override suspend fun setAdminAndUser(
        emailOrPhone: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        regUserWithFakeEmail(
            emailOrPhone,
            nickName,
            displayName,
            password
        )

    }

    private fun addAdminAndUserToFirebase(
        registrationOption: RegistrationOption,
        emailOrPhoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val newAdmin = Admin(
            registrationOption = registrationOption,
            emailOrPhoneNumber = emailOrPhoneNumber,
            isEmailOrPhoneNumberVerified = true
        )
        val newUser = User(
            nickName = nickName,
            isAdmin = true,
            fakeEmail = createFakeUserEmail(nickName, emailOrPhoneNumber),
            adminEmailOrPhone = emailOrPhoneNumber,
            displayName = displayName,
            language = language,
            password = password

        )

        coroutineScope.launch {
            val resultFirebaseRegAdmin = addAdminToCollection(newAdmin)
            if (resultFirebaseRegAdmin.isSuccess) {
                val resultFirebaseRegUser = addUserToCollection(newUser)
                if (resultFirebaseRegUser.isSuccess) {
                    admin = newAdmin
                    user = newUser
                    Storage.saveUser(context, createFakeUserEmail(nickName, emailOrPhoneNumber))
                    Storage.saveUsersPassword(context, password)
                    Storage.saveLanguage(context, language)
                    isCurrentUserNeedRefreshFlow.emit(Unit)
                } else {
                    throw RuntimeException("addAdminAndUserToFirebase: $ERROR_ADD_USER_TO_FIREBASE")
                }
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

    private suspend fun findUserByEmailInFirebase(
        email: String,
        nickName: String
    ): DocumentSnapshot? {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("fakeEmail", email)
                .whereEqualTo("nickName", nickName)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0]
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
        const val TIMEOUT_VERIFICATION = 60L
        const val FIREBASE_ADMINS_COLLECTION = "admins"
        const val FIREBASE_USERS_COLLECTION = "users"
        const val FIREBASE_TEAM_COLLECTION = "team"

        const val SMS_VERIFICATION_ERROR_INITIAL = "SMS_VERIFICATION_ERROR_INITIAL"
        const val FAKE_EMAIL_DOMAIN = "balexvic.com"

        const val ERROR_ADD_USER_TO_FIREBASE = "ERROR_ADD_USER_TO_FIREBASE"
        const val ERROR_ADD_ADMIN_TO_FIREBASE = "ERROR_ADD_ADMIN_TO_FIREBASE"
        const val USER_ALREADY_EXIST_IN_AUTH_BUT_NOT_IN_USERS =
            "user already exist in AUTH, but not in USERS"
        const val REGISTRATION_ERROR = "REGISTRATION ERROR"
        const val AUTH_USER_NOT_FOUND = "AUTH USER NOT FOUND"

    }
}