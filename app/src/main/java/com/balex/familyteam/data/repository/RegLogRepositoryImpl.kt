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
import com.google.firebase.auth.FirebaseUser
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

    private var isWrongPassword = User(nickName = User.DEFAULT_NICK_NAME)

    private var isUserMailOrPhoneVerified = false

    private val isCurrentUserNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
    private val isWrongPasswordNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
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

    override fun observeIsWrongPassword(): StateFlow<User> = flow {
        isWrongPasswordNeedRefreshFlow.emit(Unit)
        isWrongPasswordNeedRefreshFlow.collect {
            emit(isWrongPassword)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = isWrongPassword
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

    override suspend fun removeRecordFromCollection(
        collectionName: String,
        emailOrPhoneNumber: String
    ) {

        if (collectionName == FIREBASE_ADMINS_COLLECTION || collectionName == FIREBASE_ADMINS_AND_USERS_COLLECTION) {
            try {
                val document = adminsCollection.document(emailOrPhoneNumber.trim())
                val documentSnapshot = document.get().await()

                if (documentSnapshot.exists()) {
                    document.delete().await()
                }
            } catch (e: Exception) {
                Log.d("removeRecordFromCollection, adminsCollection ", "Error: ${e.message}")
            }
        }

        if (collectionName == FIREBASE_USERS_COLLECTION || collectionName == FIREBASE_ADMINS_AND_USERS_COLLECTION) {
            try {
                val document = usersCollection.document(emailOrPhoneNumber.trim())
                val documentSnapshot = document.get().await()

                if (documentSnapshot.exists()) {
                    document.delete().await()
                }
            } catch (e: Exception) {
                Log.d("removeRecordFromCollection, usersCollection ", "Error: ${e.message}")
            }
        }
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
                userToAdd.copy(
                    fakeEmail = createFakeUserEmail(
                        userToAdd.nickName,
                        userToAdd.adminEmailOrPhone
                    )
                )
            } else {
                userToAdd
            }
            val userCollection =
                usersCollection.document(newUser.adminEmailOrPhone).collection(newUser.nickName)
                    .document(newUser.nickName)
            userCollection.set(newUser).await()
            Storage.saveUser(
                context,
                createFakeUserEmail(newUser.nickName, newUser.adminEmailOrPhone)
            )
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

    private suspend fun addAdminToCollectionWhenAdminIsAuthenticated(
        firebaseAuthUser: FirebaseUser,
        adminEmail: String,
        adminNickName: String,
        displayName: String,
        adminPassword: String
    ) {

        val newAdmin = Admin(
            nickName = adminNickName,
            emailOrPhoneNumber = adminEmail,
            isEmailOrPhoneNumberVerified = true
        )
        if (firebaseAuthUser.isEmailVerified) {
            val result = addAdminToCollection(newAdmin)
            if (result.isSuccess) {
                admin = newAdmin
                //isCurrentAdminNeedRefreshFlow.emit(Unit)
            }
        } else {
            sendVerificationEmailAndWaitForResult(
                firebaseAuthUser,
                adminEmail,
                adminNickName,
                displayName,
                adminPassword
            )
            //if not verified, then exception in sendVerificationEmailAndWaitForResult
            //sendVerificationEmailAndWaitForResult if verified, add Admin and User in DB

        }
    }

    private suspend fun signToFirebaseWithEmailAndPassword(
        adminEmail: String,
        adminNickName: String,
        displayName: String,
        adminPassword: String


    ): StatusEmailSignIn {
        try {
            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().signOut()
            }

            val authRes = auth.signInWithEmailAndPassword(adminEmail, adminPassword).await()
            val firebaseAuthUser = authRes.user

            if (firebaseAuthUser != null) {
                val adminFromCollection = findAdminInCollectionByEmail(adminEmail)

                if (adminFromCollection != null) {
                    if (adminFromCollection.nickName != Admin.DEFAULT_NICK_NAME) {
                        if (firebaseAuthUser.isEmailVerified) {
                            admin = adminFromCollection
                            //isCurrentAdminNeedRefreshFlow.emit(Unit)
                        } else {
                            sendVerificationEmailAndWaitForResult(
                                firebaseAuthUser,
                                adminEmail,
                                adminNickName,
                                displayName,
                                adminPassword
                            )
                            //if not verified, then exception in sendVerificationEmailAndWaitForResult
                            //sendVerificationEmailAndWaitForResult if verified, add Admin and User in DB
                        }

                    } else {
                        addAdminToCollectionWhenAdminIsAuthenticated(
                            firebaseAuthUser,
                            adminEmail,
                            adminNickName,
                            displayName,
                            adminPassword
                        )
                    }
                    return StatusEmailSignIn.ADMIN_SIGNED_IN_AND_VERIFIED
                } else {
                    if (firebaseAuthUser.isEmailVerified) {
                        addAdminToCollectionWhenAdminIsAuthenticated(
                            firebaseAuthUser,
                            adminEmail,
                            adminNickName,
                            displayName,
                            adminPassword
                        )
                    } else {
                        sendVerificationEmailAndWaitForResult(
                            firebaseAuthUser,
                            adminEmail,
                            adminNickName,
                            displayName,
                            adminPassword
                        )
                        //if not verified, then exception in sendVerificationEmailAndWaitForResult
                        //sendVerificationEmailAndWaitForResult if verified, add Admin and User in DB
                    }
                }
            }

        } catch (e: FirebaseAuthException) {

            val admin = findAdminInCollectionByEmail(adminEmail)

            if (admin != null && admin.nickName != Admin.DEFAULT_NICK_NAME) {
                val user = findUserInCollection(User(adminEmailOrPhone = adminEmail, nickName = admin?.nickName ?: Admin.DEFAULT_NICK_NAME))
                if (user != null && user.password != adminPassword) {
                    isWrongPassword = user
                    isWrongPasswordNeedRefreshFlow.emit(Unit)
                }
            } else {
                val errCode = e.errorCode.trim()
                if (errCode == "ERROR_INVALID_CREDENTIAL" || errCode == "ERROR_USER_NOT_FOUND") {
                    return StatusEmailSignIn.ADMIN_NOT_FOUND
                } else {
                    Storage.clearPreferences(context)
                    Log.e("signToFirebaseInWithEmailAndPassword, Error", e.message ?: "Unknown error")
                    return StatusEmailSignIn.OTHER_SIGN_IN_ERROR
                }
            }
        }

        return StatusEmailSignIn.OTHER_SIGN_IN_ERROR
    }

    override suspend fun signToFirebaseWithFakeEmail(userToSignIn: User): StatusFakeEmailSignIn {
        if (userToSignIn.fakeEmail != User.DEFAULT_FAKE_EMAIL) {
            try {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    FirebaseAuth.getInstance().signOut()
                }


                val newUser = User(
                    fakeEmail = userToSignIn.fakeEmail,
                    displayName = userToSignIn.displayName,
                    password = userToSignIn.password,
                    adminEmailOrPhone = extractUserInfoFromFakeEmail(userToSignIn.fakeEmail).adminEmailOrPhone
                )

                val authRes =
                    auth.signInWithEmailAndPassword(userToSignIn.fakeEmail, userToSignIn.password)
                        .await()
                val firebaseUser = authRes.user
                if (firebaseUser != null) {
                    val userFromCollection = findUserInCollection(userToSignIn)

                    if (userFromCollection != null && userFromCollection.nickName != User.DEFAULT_NICK_NAME) {
                        user = userFromCollection
                        isCurrentUserNeedRefreshFlow.emit(Unit)

                    } else {
                        val result = addUserToCollection(newUser)
                        if (result.isSuccess) {
                            user = newUser
                            isCurrentUserNeedRefreshFlow.emit(Unit)
                        } else {
                            user = User(nickName = User.ERROR_LOADING_USER_DATA_FROM_FIREBASE)
                            isCurrentUserNeedRefreshFlow.emit(Unit)
                            return StatusFakeEmailSignIn.OTHER_FAKE_EMAIL_SIGN_IN_ERROR
                        }
                    }
                    return StatusFakeEmailSignIn.USER_SIGNED_IN

                }

            } catch (e: FirebaseAuthException) {
                val errCode = e.errorCode.trim()
                if (errCode == "ERROR_INVALID_CREDENTIAL" || errCode == "ERROR_USER_NOT_FOUND") {
                    return StatusFakeEmailSignIn.USER_NOT_FOUND
                } else {
                    Storage.clearPreferences(context)
                    Log.e(
                        "signToFirebaseInWithEmailAndPassword, Error",
                        e.message ?: "Unknown error"
                    )
                    return StatusFakeEmailSignIn.OTHER_FAKE_EMAIL_SIGN_IN_ERROR
                }
            }
        }
        return StatusFakeEmailSignIn.OTHER_FAKE_EMAIL_SIGN_IN_ERROR
    }

    override fun storageClearPreferences() {
        Storage.clearPreferences(context)
    }


    override suspend fun registerAndVerifyNewTeamByEmail(
        email: String,
        nickName: String,
        displayName: String,
        password: String
    ) {

        val isEnteredTeamIsNotRegisteredInFirebase =
            isNewTeamCheckByEmail(email, nickName, displayName, password)

        if (isEnteredTeamIsNotRegisteredInFirebase) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().signOut()
            }

            try {
                val authResult = withContext(Dispatchers.IO) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                }

                val authUser = authResult.user
                    ?: throw RuntimeException("registerAndVerifyNewTeamByEmail: $AUTH_USER_NOT_FOUND")

                sendVerificationEmailAndWaitForResult(
                    authUser,
                    email,
                    nickName,
                    displayName,
                    password
                )

            } catch (e: FirebaseAuthUserCollisionException) {
                throw RuntimeException("registerAndVerifyNewTeamByEmail: $USER_ALREADY_EXIST_IN_AUTH")
            } catch (e: Exception) {
                throw RuntimeException("registerAndVerifyNewTeamByEmail: $REGISTRATION_ERROR: ${e.message}")
            }
        } else {
            Storage.saveAllPreferences(
                context,
                createFakeUserEmail(nickName, email),
                password,
                language
            )
        }
    }

    private suspend fun sendVerificationEmailAndWaitForResult(
        authUser: FirebaseUser,
        email: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
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

//            auth.signInWithEmailAndPassword("balexvicx@gmail.com", "111111").await()
//            FirebaseAuth.getInstance().currentUser?.email?.let { Log.d("currentUser", it) }


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
            admin = true,
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
            val document =
                usersCollection.document(user.adminEmailOrPhone).collection(user.nickName)
                    .document(user.nickName)
                    .get()
                    .await()

            val userData = document?.toObject(User::class.java) ?: User()

            return userData

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("findUserInCollection", "Error: ${e.message}")
            null
        }
    }

    private suspend fun findAdminInCollectionByEmail(email: String): Admin? {
        return try {
            val document = adminsCollection.document(email)
                .get()
                .await()

            val adminData = document?.toObject(Admin::class.java) ?: Admin()
            return adminData

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("findAdminInCollection", "Error: ${e.message}")
            null
        }
    }


    private suspend fun isNewTeamCheckByEmail(
        email: String,
        nickName: String,
        displayName: String,
        password: String
    ): Boolean {

        try {
            val sigInResultAdmin =
                signToFirebaseWithEmailAndPassword(email, nickName, displayName, password)

            if (sigInResultAdmin == StatusEmailSignIn.ADMIN_SIGNED_IN_AND_VERIFIED) {
                signToFirebaseWithFakeEmail(
                    User(
                        adminEmailOrPhone = email,
                        nickName = admin.nickName,
                        fakeEmail = createFakeUserEmail(admin.nickName, email),
                        password = password
                    )
                )
                return false
            } else {
                if (sigInResultAdmin == StatusEmailSignIn.ADMIN_NOT_FOUND) {
                    removeRecordFromCollection(FIREBASE_ADMINS_AND_USERS_COLLECTION, email)
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }


    override fun createFakeUserEmail(nick: String, data: String): String {
        val nameLowCase = nick.lowercase()
        val dataLowCase = data.lowercase()
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
        const val TIMEOUT_VERIFICATION_MAIL = 60000L * 60L * 24L
        const val TIMEOUT_VERIFICATION_CHECK = 15000L

        const val FIREBASE_ADMINS_COLLECTION = "admins"
        const val FIREBASE_USERS_COLLECTION = "users"
        const val FIREBASE_ADMINS_AND_USERS_COLLECTION = "adminsAndUsers"

        const val SMS_VERIFICATION_ERROR_INITIAL = "SMS_VERIFICATION_ERROR_INITIAL"
        const val FAKE_EMAIL_DOMAIN = "balexvic.com"

        const val ERROR_ADD_USER_TO_FIREBASE = "ERROR_ADD_USER_TO_FIREBASE"
        const val ERROR_ADD_ADMIN_TO_FIREBASE = "ERROR_ADD_ADMIN_TO_FIREBASE"
        const val USER_ALREADY_EXIST_IN_AUTH =
            "user already exist in AUTH"
        const val REGISTRATION_ERROR = "REGISTRATION ERROR"

        const val AUTH_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"

        enum class StatusFakeEmailSignIn {
            USER_SIGNED_IN,
            USER_NOT_FOUND,
            OTHER_FAKE_EMAIL_SIGN_IN_ERROR
        }

        enum class StatusEmailSignIn {
            ADMIN_SIGNED_IN_AND_VERIFIED,
            ADMIN_NOT_FOUND,
            OTHER_SIGN_IN_ERROR
        }
    }
}