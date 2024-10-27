package com.balex.common.data.repository

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.balex.common.R
import com.balex.common.data.datastore.Storage
import com.balex.common.domain.entity.Admin
import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.domain.entity.Language
import com.balex.common.domain.entity.LanguagesList
import com.balex.common.domain.entity.PrivateTasks
import com.balex.common.domain.entity.RegistrationOption
import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.RegLogRepository
import com.balex.common.extensions.formatStringFirstLetterUppercase
import com.balex.common.extensions.formatStringPhoneDelLeadNullAndAddPlus
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
import kotlinx.coroutines.flow.distinctUntilChanged
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

    private val appContext: Context = context.applicationContext

    private var admin = Admin()

    private var globalRepoUser = User()
        set(value) {
            field = value
            coroutineScope.launch {
                isCurrentUserNeedRefreshFlow.emit(Unit)
                if (!isUserListenerRegistered && value.adminEmailOrPhone != User.DEFAULT_FAKE_EMAIL && value.nickName != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES)  {
                    addUserListenerInFirebase()
                    isUserListenerRegistered = true
                }
            }
        }

    private var isWrongPassword = User()
        set(value) {
            field = value
            coroutineScope.launch {
                isWrongPasswordNeedRefreshFlow.emit(Unit)
            }
        }

    private var language = Language.DEFAULT_LANGUAGE.symbol



    private var isUserMailOrPhoneVerified = false
    private var isUserListenerRegistered = false


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
            globalRepoUser = emptyUserNotSaved
            //isCurrentUserNeedRefreshFlow.emit(Unit)

        } else {
            signToFirebaseWithEmailAndPasswordFromPreferences(
                userFakeEmailFromStorage,
                Storage.getUsersPassword(context),
                phoneLang
            )
        }
        emit(globalRepoUser)
        addUserListenerInFirebase()
        isCurrentUserNeedRefreshFlow.distinctUntilChanged().collect {
            emit(globalRepoUser)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = globalRepoUser
        )

    private fun addUserListenerInFirebase() {
        if (globalRepoUser.adminEmailOrPhone != User.DEFAULT_FAKE_EMAIL && globalRepoUser.nickName != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES) {
            val userCollection = usersCollection.document(globalRepoUser.adminEmailOrPhone)
                .collection(globalRepoUser.nickName.lowercase())
                .document(globalRepoUser.nickName.lowercase())

            userCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                snapshot?.let {
                    globalRepoUser = it.toObject(User::class.java) ?: globalRepoUser
                }
            }
        }
    }

    override fun observeIsWrongPassword(): StateFlow<User> = flow {
        emit(isWrongPassword)
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

    override suspend fun logoutUser() {
        globalRepoUser = User()
        admin = Admin()
        isWrongPassword = User()
        isUserMailOrPhoneVerified = false
        //isCurrentUserNeedRefreshFlow.emit(Unit)
        //isCurrentLanguageNeedRefreshFlow.emit(Unit)
    }

    override suspend fun resetWrongPasswordUserToDefault() {
        isWrongPassword = User()
        //isWrongPasswordNeedRefreshFlow.emit(Unit)

    }

    override fun getRepoUser(): User {
        return globalRepoUser
    }

    override fun getRepoAdmin(): Admin {
        return admin
    }

    override fun getWrongPasswordUser(): User {
        return isWrongPassword
    }

    override fun setUserAsVerified() {
        isUserMailOrPhoneVerified = true
    }

    override suspend fun setUserWithError(message: String) {
        globalRepoUser = User(existErrorInData = true, errorMessage = message)
        //isCurrentUserNeedRefreshFlow.emit(Unit)
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
        emailOrPhoneNumber: String,
        nickName: String
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

                document.delete().await()
            } catch (e: Exception) {
                Log.d("removeRecordFromCollection, usersCollection", "Error: ${e.message}")
            }
        }
    }

    override suspend fun addAdminToCollection(admin: Admin): Result<Unit> {
        val adminWithList = admin.copy(
            emailOrPhoneNumber = admin.emailOrPhoneNumber.lowercase(),
            nickName = admin.nickName.formatStringFirstLetterUppercase(),
            usersNickNamesList = listOf(admin.nickName.formatStringFirstLetterUppercase())
        )
        return try {
            val adminDocument = adminsCollection.document(adminWithList.emailOrPhoneNumber)
            adminDocument.set(adminWithList).await()
            Result.success(Unit)
        } catch (e: Exception) {
            setUserWithError(ERROR_ADD_ADMIN_TO_FIREBASE)
            Result.failure(e)
        }
    }

    private fun formatUser(user: User): User {
        val newUser = if (user.fakeEmail == User.DEFAULT_FAKE_EMAIL) {
            user.copy(
                adminEmailOrPhone = user.adminEmailOrPhone.lowercase(),
                nickName = user.nickName.formatStringFirstLetterUppercase(),
                displayName = user.displayName.formatStringFirstLetterUppercase(),
                fakeEmail = createFakeUserEmail(
                    user.nickName.formatStringFirstLetterUppercase(),
                    user.adminEmailOrPhone
                )
            )
        } else {
            user.copy(
                adminEmailOrPhone = user.adminEmailOrPhone.lowercase(),
                nickName = user.nickName.formatStringFirstLetterUppercase(),
                displayName = user.displayName.formatStringFirstLetterUppercase()
            )
        }
        val userForFirebase = newUser.copy(
            lastTimeAvailableFCMWasUpdated = newUser.lastTimeAvailableFCMWasUpdated
        )
        return userForFirebase
    }

    override suspend fun addUserToCollection(userToAdd: User): Result<Unit> {
        return try {
            val newUser = formatUser(userToAdd)
            val userCollection =
                usersCollection.document(newUser.adminEmailOrPhone)
                    .collection(newUser.nickName.lowercase())
                    .document(newUser.nickName.lowercase())
            userCollection.set(newUser).await()
            Storage.saveUser(
                context,
                createFakeUserEmail(newUser.nickName, newUser.adminEmailOrPhone)
            )
            Storage.saveUsersPassword(context, newUser.password)
            Storage.saveLanguage(context, language)
            globalRepoUser = newUser
            //isCurrentUserNeedRefreshFlow.emit(Unit)
            Result.success(Unit)
        } catch (e: Exception) {
            setUserWithError(ERROR_ADD_USER_TO_FIREBASE)
            Result.failure(e)
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
            emailOrPhoneNumberVerified = true
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


    override suspend fun refreshFCMLastTimeUpdated() {
        deleteOldTasks()
        var isPremiumAccount = globalRepoUser.hasPremiumAccount
        if (isPremiumAccount) {
            if (globalRepoUser.premiumAccountExpirationDate < System.currentTimeMillis()) {
                isPremiumAccount = false
            }
        }
        if (System.currentTimeMillis() - globalRepoUser.lastTimeAvailableFCMWasUpdated > MILLIS_IN_DAY) {

            var maxTaskPerDay = if (globalRepoUser.hasPremiumAccount) {
                context.resources.getInteger(R.integer.max_available_tasks_per_day_premium)
            } else {
                context.resources.getInteger(R.integer.max_available_tasks_per_day_default)
            }

            var maxFCMPerDay = if (globalRepoUser.hasPremiumAccount) {
                context.resources.getInteger(R.integer.max_available_FCM_per_day_premium)
            } else {
                context.resources.getInteger(R.integer.max_available_FCM_per_day_default)
            }

            if (maxTaskPerDay < globalRepoUser.availableTasksToAdd) {
                maxTaskPerDay = globalRepoUser.availableTasksToAdd
            }

            if (maxFCMPerDay < globalRepoUser.availableFCM) {
                maxFCMPerDay = globalRepoUser.availableFCM
            }


            val userForUpdate = globalRepoUser.copy(
                hasPremiumAccount = isPremiumAccount,
                availableTasksToAdd = maxTaskPerDay,
                availableFCM = maxFCMPerDay,
                lastTimeAvailableFCMWasUpdated = System.currentTimeMillis()
            )

            val userCollection =
                usersCollection.document(globalRepoUser.adminEmailOrPhone)
                    .collection(globalRepoUser.nickName.lowercase())
                    .document(globalRepoUser.nickName.lowercase())

            try {
                userCollection.set(userForUpdate).await()

            } catch (e: Exception) {
                Log.d("refreshFCMLastTimeUpdated error", e.toString())
            }
        }
    }

    private suspend fun signToFirebaseWithEmailAndPasswordFromPreferences(
        fakeEmail: String,
        password: String,
        phoneLang: String = Language.DEFAULT_LANGUAGE.symbol

    ) {
        val extractedUser = extractUserInfoFromFakeEmail(fakeEmail)


        try {

            val adminFromCollection =
                findAdminInCollectionByDocumentName(extractedUser.adminEmailOrPhone)

            if (adminFromCollection != null && adminFromCollection.nickName != Admin.DEFAULT_NICK_NAME) {
                admin = adminFromCollection
                val userFromCollection = findUserInCollection(
                    User(
                        adminEmailOrPhone = extractedUser.adminEmailOrPhone,
                        nickName = extractedUser.nickName
                    )
                )
                if (userFromCollection != null) {
                    if (userFromCollection.password == extractedUser.password) {

                        if (FirebaseAuth.getInstance().currentUser != null) {
                            FirebaseAuth.getInstance().signOut()
                        }

                        val authRes =
                            auth.signInWithEmailAndPassword(fakeEmail, password).await()
                        val firebaseAuthUser = authRes.user
                        if (firebaseAuthUser != null) {
                            globalRepoUser = userFromCollection
                            //isCurrentUserNeedRefreshFlow.emit(Unit)
                        } else {
                            setUserWithError("signToFirebaseWithEmailAndPasswordFromPreferences: ERROR AUTH USER: $fakeEmail")
                        }
                    } else {
                        globalRepoUser = userFromCollection.copy(password = User.WRONG_PASSWORD)
                        isWrongPassword =
                            userFromCollection.copy(password = User.WRONG_PASSWORD)

                        //isWrongPasswordNeedRefreshFlow.emit(Unit)
                    }
                } else {
                    //setUserWithError("signToFirebaseWithEmailAndPasswordFromPreferences: ADMIN_NOT_FOUND: ${extractedUser.adminEmailOrPhone}")
                    Storage.clearPreferences(context)
                    val emptyUserNotSaved =
                        User(
                            nickName = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES,
                            language = phoneLang
                        )
                    globalRepoUser = emptyUserNotSaved
                }

            } else {
                //setUserWithError("signToFirebaseWithEmailAndPasswordFromPreferences: ADMIN_NOT_FOUND: ${extractedUser.adminEmailOrPhone}")
                Storage.clearPreferences(context)
                val emptyUserNotSaved =
                    User(
                        nickName = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES,
                        language = phoneLang
                    )
                globalRepoUser = emptyUserNotSaved
            }

        } catch (e: FirebaseAuthException) {
            val errCode = e.errorCode.trim()
            if (errCode == "ERROR_INVALID_CREDENTIAL" || errCode == "ERROR_USER_NOT_FOUND") {
                isWrongPassword = globalRepoUser
                //isWrongPasswordNeedRefreshFlow.emit(Unit)
            } else {
                Storage.clearPreferences(context)
                setUserWithError(ERROR_LOADING_USER_DATA_FROM_FIREBASE)
            }

        } catch (e: Exception) {
            Log.e("signToFirebaseInWithEmailAndPassword, Error", e.message ?: "Unknown error")
            setUserWithError(ERROR_LOADING_USER_DATA_FROM_FIREBASE)
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
                val adminFromCollection = findAdminInCollectionByDocumentName(adminEmail)

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

            val admin = findAdminInCollectionByDocumentName(adminEmail)

            if (admin != null && admin.nickName != Admin.DEFAULT_NICK_NAME) {
                val user = findUserInCollection(
                    User(
                        adminEmailOrPhone = adminEmail,
                        nickName = admin.nickName
                    )
                )
                if (user != null && user.password != adminPassword) {
                    isWrongPassword = user
                    //isWrongPasswordNeedRefreshFlow.emit(Unit)
                }
            } else {
                val errCode = e.errorCode.trim()
                if (errCode == "ERROR_INVALID_CREDENTIAL" || errCode == "ERROR_USER_NOT_FOUND") {
                    return StatusEmailSignIn.ADMIN_NOT_FOUND
                } else {
                    Storage.clearPreferences(context)
                    Log.e(
                        "signToFirebaseInWithEmailAndPassword, Error",
                        e.message ?: "Unknown error"
                    )
                    return StatusEmailSignIn.OTHER_SIGN_IN_ERROR
                }
            }
        } catch (e: Exception) {
            Log.e("signToFirebaseInWithEmailAndPassword, Error", e.message ?: "Unknown error")
            //setUserWithError(ERROR_LOADING_USER_DATA_FROM_FIREBASE)
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
                    auth.signInWithEmailAndPassword(
                        userToSignIn.fakeEmail,
                        userToSignIn.password
                    )
                        .await()
                val firebaseUser = authRes.user
                if (firebaseUser != null) {
                    val userFromCollection = findUserInCollection(userToSignIn)

                    if (userFromCollection != null && userFromCollection.nickName != User.DEFAULT_NICK_NAME) {
                        globalRepoUser = userFromCollection
                        //isCurrentUserNeedRefreshFlow.emit(Unit)

                    } else {
                        val result = addUserToCollection(newUser)
                        if (result.isSuccess) {
                            globalRepoUser = newUser
                            //isCurrentUserNeedRefreshFlow.emit(Unit)
                        } else {
                            setUserWithError(ERROR_LOADING_USER_DATA_FROM_FIREBASE)
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

    override fun storageSavePreferences(
        email: String,
        nickName: String,
        password: String,
        language: String
    ) {

        val fakeEmail = createFakeUserEmail(nickName, email)
        Storage.saveAllPreferences(
            context,
            fakeEmail,
            password,
            language
        )
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
                setUserWithError("registerAndVerifyNewTeamByEmail: $USER_ALREADY_EXIST_IN_AUTH: ${e.message}")
            } catch (e: Exception) {
                setUserWithError("registerAndVerifyNewTeamByEmail: $REGISTRATION_ERROR: ${e.message}")
            }
        } else {
            //storageSavePreferences(email, nickName, password, language)

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
            emailOrPhoneNumberVerified = true
        )
        val newUser = User(
            nickName = nickName,
            hasAdminRights = true,
            fakeEmail = fakeEmail,
            adminEmailOrPhone = emailOrPhoneNumber,
            displayName = displayName,
            password = password,
            language = language,
            availableTasksToAdd = appContext.resources.getInteger(R.integer.max_available_tasks_per_day_default),
            availableFCM = appContext.resources.getInteger(R.integer.max_available_FCM_per_day_default)
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

    private suspend fun findUserInCollection(userToFind: User): User? {
        return try {
            val document =
                usersCollection.document(userToFind.adminEmailOrPhone)
                    .collection(userToFind.nickName.lowercase())
                    .document(userToFind.nickName.lowercase())
                    .get()
                    .await()

            val userData = document?.toObject(User::class.java)

            return userData

        } catch (e: Exception) {
            e.printStackTrace()
            setUserWithError(e.message ?: "findUserInCollection, Error: ${e.message}")
            null
        }
    }


    override suspend fun findAdminInCollectionByDocumentName(documentName: String): Admin? {
        val updatedDocumentName = documentName.formatStringPhoneDelLeadNullAndAddPlus()
        return try {
            val document = adminsCollection.document(updatedDocumentName)
                .get()
                .await()

            val adminData = document?.toObject(Admin::class.java)
            return adminData

        } catch (e: Exception) {
            e.printStackTrace()
            setUserWithError(e.message ?: "findAdminInCollection, Error: ${e.message}")
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
                    removeRecordFromCollection(
                        FIREBASE_ADMINS_AND_USERS_COLLECTION,
                        email,
                        nickName
                    )
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setUserWithError(e.message ?: "isNewTeamCheckByEmail: Unknown error")
            return false
        }
        isWrongPassword = User(adminEmailOrPhone = email)
        //isWrongPasswordNeedRefreshFlow.emit(Unit)
        return false
    }

    override suspend fun setWrongPasswordUser(user: User) {
        isWrongPassword = user
        //isWrongPasswordNeedRefreshFlow.emit(Unit)

    }

    override fun createFakeUserEmail(nick: String, data: String): String {
        val nameLowCase = nick.lowercase()
        val dataLowCase = data.lowercase()
        return if (dataLowCase.contains("@", true)) {
            "$nameLowCase-$dataLowCase".trim()
        } else {
            val phone = dataLowCase.formatStringPhoneDelLeadNullAndAddPlus()
            nameLowCase + "-" + phone.substring(1) + "@" + FAKE_EMAIL_DOMAIN.trim()
        }
    }

    private fun extractUserInfoFromFakeEmail(fakeEmail: String): User {
        if (fakeEmail.endsWith(FAKE_EMAIL_DOMAIN, false)) {
            val parts = fakeEmail.split("@")
            val nick = parts[0].split("-").first()
            val phone = parts[0].split("-").last()
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

    override suspend fun checkUserInCollectionAndLoginIfExist(
        adminEmailOrPhone: String,
        nickName: String,
        password: String
    ): User {
        val adminEmailOrPhoneWithPlus =
            adminEmailOrPhone.formatStringPhoneDelLeadNullAndAddPlus()
        val admin = findAdminInCollectionByDocumentName(adminEmailOrPhoneWithPlus)
        if (admin == null || admin.emailOrPhoneNumber.trim() != adminEmailOrPhoneWithPlus.trim()) {
            return User(
                existErrorInData = true,
                errorMessage = CheckUserInCollectionAndLoginIfExistErrorMessages.ADMIN_NOT_FOUND.name
            )
        } else {
            val userFromCollection = findUserInCollection(
                User(
                    adminEmailOrPhone = adminEmailOrPhoneWithPlus,
                    nickName = nickName.formatStringFirstLetterUppercase()
                )
            )
            if (userFromCollection != null) {
                if (userFromCollection.password != password) {
                    return User(
                        existErrorInData = true,
                        errorMessage = CheckUserInCollectionAndLoginIfExistErrorMessages.WRONG_PASSWORD.name
                    )

                } else {
                    val trySignIn = signToFirebaseWithFakeEmail(
                        User(
                            adminEmailOrPhone = adminEmailOrPhoneWithPlus,
                            nickName = admin.nickName,
                            fakeEmail = createFakeUserEmail(
                                nickName,
                                adminEmailOrPhoneWithPlus
                            ),
                            password = password
                        )
                    )
                    if (trySignIn == StatusFakeEmailSignIn.USER_SIGNED_IN) {
                        globalRepoUser = userFromCollection
                        //isCurrentUserNeedRefreshFlow.emit(Unit)
                    }
                    return globalRepoUser
                }

            } else {
                return User(
                    existErrorInData = true,
                    errorMessage = CheckUserInCollectionAndLoginIfExistErrorMessages.NICK_NAME_NOT_FOUND.name
                )
            }

        }
    }

    suspend fun deleteOldTasks() {
        val userForModify = globalRepoUser
        val taskMaxExpireTimeInMillis =
            context.resources.getInteger(R.integer.max_expired_task_save_in_days) * MILLIS_IN_DAY
        val privateTasks = userForModify.listToDo.thingsToDoPrivate.privateTasks.filter { task ->
            task.cutoffTime - System.currentTimeMillis() > taskMaxExpireTimeInMillis
        }
        val sharedTasks =
            userForModify.listToDo.thingsToDoShared.externalTasks.filter { externalTask ->
                externalTask.task.cutoffTime - System.currentTimeMillis() > taskMaxExpireTimeInMillis
            }
        val tasksForOtherUsers =
            userForModify.listToDo.thingsToDoForOtherUsers.externalTasks.filter { externalTask ->
                externalTask.task.cutoffTime - System.currentTimeMillis() > taskMaxExpireTimeInMillis
            }

        val toDoOld = userForModify.listToDo
        val updatedTodoList = toDoOld.copy(
            thingsToDoPrivate = PrivateTasks(privateTasks = privateTasks),
            thingsToDoShared = ExternalTasks(externalTasks = sharedTasks),
            thingsToDoForOtherUsers = ExternalTasks(externalTasks = tasksForOtherUsers)
        )

        val userForUpdate = userForModify.copy(
            listToDo = updatedTodoList
        )
        val userCollection =
            usersCollection.document(userForModify.adminEmailOrPhone)
                .collection(userForModify.nickName.lowercase())
                .document(userForModify.nickName.lowercase())

        try {
            userCollection.set(userForUpdate).await()
        } catch (e: Exception) {
            Log.d("deleteOldTasks error", e.toString())
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

        const val ERROR_LOADING_USER_DATA_FROM_FIREBASE =
            "ERROR_LOADING_USER_DATA_FROM_FIREBASE"
        const val ERROR_ADD_USER_TO_FIREBASE = "ERROR_ADD_USER_TO_FIREBASE"
        const val ERROR_ADD_ADMIN_TO_FIREBASE = "ERROR_ADD_ADMIN_TO_FIREBASE"
        const val USER_ALREADY_EXIST_IN_AUTH =
            "user already exist in AUTH"
        const val REGISTRATION_ERROR = "REGISTRATION ERROR"

        const val AUTH_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"

        const val MILLIS_IN_MINUTE = 60 * 1_000L
        const val MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60
        const val MILLIS_IN_DAY = MILLIS_IN_HOUR * 24

        enum class CheckUserInCollectionAndLoginIfExistErrorMessages {
            ADMIN_NOT_FOUND,
            NICK_NAME_NOT_FOUND,
            WRONG_PASSWORD,
        }


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



