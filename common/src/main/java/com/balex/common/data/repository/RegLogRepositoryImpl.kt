package com.balex.common.data.repository

import android.content.Context
import android.content.res.Configuration
import com.balex.common.R
import com.balex.common.data.datastore.Storage
import com.balex.common.data.repository.UserRepositoryImpl.Companion.FIREBASE_SCHEDULERS_COLLECTION
import com.balex.common.data.repository.UserRepositoryImpl.Companion.FIREBASE_SCHEDULERS_DELETE_COLLECTION
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
import com.balex.common.extensions.isNotEmptyNickName
import com.balex.common.extensions.logExceptionToFirebase
import com.balex.common.extensions.logTextToFirebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
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
import kotlin.coroutines.cancellation.CancellationException


class RegLogRepositoryImpl @Inject constructor(
    private val context: Context

) : RegLogRepository {


    private val appContext: Context = context.applicationContext

    private val listenerRegistrations = mutableListOf<ListenerRegistration>()

    private var admin = Admin()

    private var token = NO_NEW_TOKEN

    private var globalRepoUser = User()
        set(value) {
            val newValue = if (token != NO_NEW_TOKEN) {
                value.copy(token = token)
            } else if (field.token.isBlank()) {
                value
            } else {
                value.copy(token = field.token)
            }
            val valuePhoneWithPlus =
                newValue.copy(adminEmailOrPhone = newValue.adminEmailOrPhone.formatStringPhoneDelLeadNullAndAddPlus())
            if (field != valuePhoneWithPlus) {
                field = valuePhoneWithPlus
                if (value.token.isBlank() && field.token.isNotBlank()) {
                    logTextToFirebase("Firestore token, globalRepoUser new value token is blank")
                }
                coroutineScope.launch {
                    isCurrentUserNeedRefreshFlow.emit(Unit)
                    if (!isUserListenerRegistered && value.adminEmailOrPhone != User.DEFAULT_FAKE_EMAIL &&
                        value.nickName != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES &&
                        value.nickName != User.DEFAULT_NICK_NAME
                    ) {
                        addUserListenerInFirebase()
                        isUserListenerRegistered = true
                    }
                }
            }

        }

    @Suppress("unused")
    private var isWrongPassword = User()
        set(value) {
            field = value
            coroutineScope.launch {
                isWrongPasswordNeedRefreshFlow.emit(Unit)
            }
        }

    private var language = Language.DEFAULT_LANGUAGE.symbol

    @Suppress("unused")
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
    private val remindersCollection = db.collection(FIREBASE_REMINDERS_COLLECTION)

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)


    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val scheduleCollection = db.collection(FIREBASE_SCHEDULERS_COLLECTION)
    private val scheduleDeleteCollection = db.collection(FIREBASE_SCHEDULERS_DELETE_COLLECTION)


    override fun observeUser(): StateFlow<User> {
        return flow {
            try {
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
                    val emptyUserNotSaved = User(
                        nickName = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES,
                        language = phoneLang,
                        pressedLogoutButton = globalRepoUser.pressedLogoutButton
                    )
                    globalRepoUser = emptyUserNotSaved
                    emit(globalRepoUser)
                } else {
                    if (!globalRepoUser.pressedLogoutButton) {
                        signToFirebaseWithEmailAndPasswordFromPreferences(
                            userFakeEmailFromStorage,
                            Storage.getUsersPassword(context),
                            phoneLang
                        )
                    }
                }

                addUserListenerInFirebase()

                isCurrentUserNeedRefreshFlow.collect {
                    emit(globalRepoUser)
                }
            } catch (e: Exception) {
                logExceptionToFirebase("observeUser", e.message.toString())
            }
        }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                initialValue = globalRepoUser
            )
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

    private fun addUserListenerInFirebase() {
        if (globalRepoUser.adminEmailOrPhone != User.DEFAULT_FAKE_EMAIL && globalRepoUser.nickName != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES) {
            val userCollection = usersCollection.document(globalRepoUser.adminEmailOrPhone)
                .collection(globalRepoUser.nickName.lowercase())
                .document(globalRepoUser.nickName.lowercase())

            val registration = userCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val userListener = it.toObject(User::class.java)
                    if (userListener != null && userListener.nickName == globalRepoUser.nickName && globalRepoUser != userListener) {

                        globalRepoUser = userListener
                    }
                }
            }
            listenerRegistrations.add(registration)
        }
    }


    override fun observeLanguage(): StateFlow<String> {
        return flow {
            try {
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
            } catch (e: Exception) {
                logExceptionToFirebase("observeLanguage", e.message.toString())
            }
        }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                initialValue = language
            )
    }

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
        listenerRegistrations.forEach { it.remove() }
        listenerRegistrations.clear()
        globalRepoUser =
            User(pressedLogoutButton = true, nickName = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES)
        storageClearPreferences()
        admin = Admin()
        isWrongPassword = User(nickName = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES)
        isUserMailOrPhoneVerified = false
    }

    override suspend fun resetWrongPasswordUserToDefault() {
        isWrongPassword = User()
    }

    override fun getRepoUser(): User {
        return globalRepoUser
    }

    override fun setNewToken(newToken: String) {
        token = newToken
        val newUser = globalRepoUser.copy(token = newToken)
        globalRepoUser = newUser
    }

    override fun getToken(): String {
        return token
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
        if (getRepoUser().isNotEmptyNickName()) {
            if (collectionName == FIREBASE_ADMINS_COLLECTION || collectionName == FIREBASE_ADMINS_AND_USERS_COLLECTION) {
                try {
                    withContext(Dispatchers.IO) {
                        val document = adminsCollection.document(emailOrPhoneNumber.trim())
                        val documentSnapshot = document.get().await()

                        if (documentSnapshot.exists()) {
                            document.delete().await()
                        }
                    }
                } catch (e: Exception) {
                    logExceptionToFirebase(
                        "removeRecordFromCollection, adminsCollection",
                        e.message.toString()
                    )
                }
            }

            if (collectionName == FIREBASE_USERS_COLLECTION || collectionName == FIREBASE_ADMINS_AND_USERS_COLLECTION) {
                try {
                    val document = usersCollection.document(emailOrPhoneNumber.trim())
                    withContext(Dispatchers.IO) {
                        document.delete().await()
                    }
                } catch (e: Exception) {
                    logExceptionToFirebase(
                        "removeRecordFromCollection, usersCollection",
                        e.message.toString()
                    )
                }
            }
        }
    }

    private suspend fun addAdminToCollection(admin: Admin): Result<Unit> {
        val adminWithList = admin.copy(
            emailOrPhoneNumber = admin.emailOrPhoneNumber.lowercase(),
            nickName = admin.nickName.formatStringFirstLetterUppercase(),
            usersNickNamesList = listOf(admin.nickName.formatStringFirstLetterUppercase())
        )
        return try {
            val adminDocument = adminsCollection.document(adminWithList.emailOrPhoneNumber)
            withContext(Dispatchers.IO) {
                adminDocument.set(adminWithList).await()
            }
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

    private suspend fun addUserToCollection(userToAdd: User): Result<Unit> {
        return try {
            val newUser = formatUser(userToAdd)
            val userCollection =
                usersCollection.document(newUser.adminEmailOrPhone)
                    .collection(newUser.nickName.lowercase())
                    .document(newUser.nickName.lowercase())
            withContext(Dispatchers.IO) {
                userCollection.set(newUser).await()
            }
            Storage.saveUser(
                context,
                createFakeUserEmail(newUser.nickName, newUser.adminEmailOrPhone)
            )
            Storage.saveUsersPassword(context, newUser.password)
            Storage.saveLanguage(context, language)
            globalRepoUser = newUser
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
        if (globalRepoUser.nickName != User.DEFAULT_FAKE_EMAIL &&
            globalRepoUser.nickName != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
        ) {
            val currentTimestamp = System.currentTimeMillis()
            var isPremiumAccount = globalRepoUser.hasPremiumAccount

            val userCollection =
                usersCollection.document(globalRepoUser.adminEmailOrPhone)
                    .collection(globalRepoUser.nickName.lowercase())
                    .document(globalRepoUser.nickName.lowercase())
            if (isPremiumAccount) {
                if (globalRepoUser.premiumAccountExpirationDate < currentTimestamp) {
                    isPremiumAccount = false
                }
            }
            if (currentTimestamp - globalRepoUser.lastTimeAvailableFCMWasUpdated > MILLIS_IN_DAY) {

                var maxTaskPerDay = if (globalRepoUser.hasPremiumAccount) {
                    context.resources.getInteger(R.integer.max_available_tasks_per_day_premium)
                } else {
                    if (globalRepoUser.id > 0 && globalRepoUser.id <= context.resources.getInteger(R.integer.max_users_with_renew_tasks)) {
                        context.resources.getInteger(R.integer.max_available_tasks_per_day_default)
                    } else {
                        globalRepoUser.availableTasksToAdd
                    }

                }

                var maxFCMPerDay = if (globalRepoUser.hasPremiumAccount) {
                    context.resources.getInteger(R.integer.max_available_FCM_per_day_premium)
                } else {
                    if (globalRepoUser.id > 0 && globalRepoUser.id <= context.resources.getInteger(R.integer.max_users_with_renew_tasks)) {
                        context.resources.getInteger(R.integer.max_available_FCM_per_day_default)
                    } else {
                        globalRepoUser.availableFCM
                    }

                }

                if (maxTaskPerDay < globalRepoUser.availableTasksToAdd) {
                    maxTaskPerDay = globalRepoUser.availableTasksToAdd
                }

                if (maxFCMPerDay < globalRepoUser.availableFCM) {
                    maxFCMPerDay = globalRepoUser.availableFCM
                }

                val newPremiumExpirationDate = if (isPremiumAccount) {
                    globalRepoUser.premiumAccountExpirationDate
                } else {
                    0
                }
                val userForUpdate = globalRepoUser.copy(
                    hasPremiumAccount = isPremiumAccount,
                    availableTasksToAdd = maxTaskPerDay,
                    availableFCM = maxFCMPerDay,
                    lastTimeAvailableFCMWasUpdated = currentTimestamp,
                    premiumAccountExpirationDate = newPremiumExpirationDate
                )

                try {
                    withContext(Dispatchers.IO) {
                        userCollection.set(userForUpdate).await()
                    }

                } catch (e: Exception) {
                    logExceptionToFirebase("refreshFCMLastTimeUpdated", e.message.toString())
                }
            }
        }
    }


    private suspend fun signToFirebaseWithEmailAndPasswordFromPreferences(
        fakeEmail: String,
        password: String,
        phoneLang: String = Language.DEFAULT_LANGUAGE.symbol

    ) {
        val extractedUser = extractUserInfoFromFakeEmail(fakeEmail)
        if (extractedUser.isNotEmptyNickName()) {

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
                            withContext(Dispatchers.IO) {
                                val authRes =
                                    auth.signInWithEmailAndPassword(fakeEmail, password).await()

                                val firebaseAuthUser = authRes.user
                                if (!globalRepoUser.pressedLogoutButton) {
                                    if (firebaseAuthUser != null) {
                                        globalRepoUser = userFromCollection
                                    } else {
                                        setUserWithError("signToFirebaseWithEmailAndPasswordFromPreferences: ERROR AUTH USER: $fakeEmail")
                                    }
                                }
                            }
                        } else {
                            globalRepoUser = userFromCollection.copy(password = User.WRONG_PASSWORD)
                            isWrongPassword =
                                userFromCollection.copy(password = User.WRONG_PASSWORD)
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
                } else {
                    Storage.clearPreferences(context)
                    setUserWithError(ERROR_LOADING_USER_DATA_FROM_FIREBASE)
                }

            } catch (e: Exception) {
                logExceptionToFirebase(
                    "signToFirebaseWithEmailAndPasswordFromPreferences",
                    e.message.toString()
                )
                setUserWithError(ERROR_LOADING_USER_DATA_FROM_FIREBASE)
            }
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
            val firebaseAuthUser: FirebaseUser?
            withContext(Dispatchers.IO) {
                val authRes = auth.signInWithEmailAndPassword(adminEmail, adminPassword).await()
                firebaseAuthUser = authRes.user
            }

            if (firebaseAuthUser != null) {
                val adminFromCollection = findAdminInCollectionByDocumentName(adminEmail)

                if (adminFromCollection != null) {
                    if (adminFromCollection.nickName != Admin.DEFAULT_NICK_NAME) {
                        if (firebaseAuthUser.isEmailVerified) {
                            admin = adminFromCollection
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
                }
            } else {
                val errCode = e.errorCode.trim()
                if (errCode == "ERROR_INVALID_CREDENTIAL" || errCode == "ERROR_USER_NOT_FOUND") {
                    return StatusEmailSignIn.ADMIN_NOT_FOUND
                } else {
                    Storage.clearPreferences(context)
                    logExceptionToFirebase(
                        "signToFirebaseInWithEmailAndPassword",
                        e.message ?: "Unknown error"
                    )
                    return StatusEmailSignIn.OTHER_SIGN_IN_ERROR
                }
            }
        } catch (e: Exception) {
            logExceptionToFirebase(
                "signToFirebaseInWithEmailAndPassword",
                e.message ?: "Unknown error"
            )
            //setUserWithError(ERROR_LOADING_USER_DATA_FROM_FIREBASE)
        }

        return StatusEmailSignIn.OTHER_SIGN_IN_ERROR
    }

    override suspend fun signToFirebaseWithFakeEmail(
        userToSignIn: User,
        userNameTrySignIn: String
    ): StatusFakeEmailSignIn {
        if (userToSignIn.fakeEmail != User.DEFAULT_FAKE_EMAIL) {
            try {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    FirebaseAuth.getInstance().signOut()
                }


                val newUser = User(
                    id = userToSignIn.id,
                    fakeEmail = userToSignIn.fakeEmail,
                    displayName = userToSignIn.displayName,
                    password = userToSignIn.password,
                    adminEmailOrPhone = extractUserInfoFromFakeEmail(userToSignIn.fakeEmail).adminEmailOrPhone
                )

                val firebaseUser: FirebaseUser?
                withContext(Dispatchers.IO) {
                    val authRes =
                        auth.signInWithEmailAndPassword(
                            userToSignIn.fakeEmail,
                            userToSignIn.password
                        ).await()
                    firebaseUser = authRes.user
                }
                if (firebaseUser != null && !globalRepoUser.pressedLogoutButton) {
                    val userFromCollection = findUserInCollection(userToSignIn)
                    if (userFromCollection != null) {
                        if (userFromCollection.nickName.lowercase()
                                .trim() == userNameTrySignIn.lowercase().trim()
                        ) {
                            globalRepoUser =
                                if (userFromCollection.nickName != User.DEFAULT_NICK_NAME) {
                                    userFromCollection
                                } else {
                                    val result = addUserToCollection(newUser)
                                    if (result.isSuccess) {
                                        newUser
                                    } else {
                                        setUserWithError(ERROR_LOADING_USER_DATA_FROM_FIREBASE)
                                        return StatusFakeEmailSignIn.OTHER_FAKE_EMAIL_SIGN_IN_ERROR
                                    }
                                }
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
                    logExceptionToFirebase(
                        "signToFirebaseInWithEmailAndPassword",
                        e.message ?: "Unknown error"
                    )
                    return StatusFakeEmailSignIn.OTHER_FAKE_EMAIL_SIGN_IN_ERROR
                }
            }
        }
        return StatusFakeEmailSignIn.OTHER_FAKE_EMAIL_SIGN_IN_ERROR
    }

    override fun storageClearPreferences() {
        val isLogoutWasClicked = globalRepoUser.pressedLogoutButton
        globalRepoUser = User(
            nickName = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES,
            pressedLogoutButton = isLogoutWasClicked
        )
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
                val authResult: AuthResult
                withContext(Dispatchers.IO) {
                    authResult = withContext(Dispatchers.IO) {
                        auth.createUserWithEmailAndPassword(email, password).await()
                    }
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
            logTextToFirebase("registerAndVerifyNewTeamByEmail: Team '$email' already registered")
        }
    }


    private suspend fun sendVerificationEmailAndWaitForResult(
        authUser: FirebaseUser,
        email: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        withContext(Dispatchers.IO) {
            authUser.sendEmailVerification().await()
        }

        withTimeoutOrNull(TIMEOUT_VERIFICATION_MAIL) {
            while (!authUser.isEmailVerified) {
                delay(TIMEOUT_VERIFICATION_CHECK)
                withContext(Dispatchers.IO) {
                    authUser.reload().await()
                }
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
            withContext(Dispatchers.IO) {
                auth.createUserWithEmailAndPassword(fakeEmail, password).await()
            }

            addAdminAndUserToFirebase(
                registrationOption,
                emailOrPhone,
                fakeEmail,
                nickName,
                displayName,
                password
            )

        } catch (e: Exception) {
            logExceptionToFirebase("regUserWithFakeEmail", e.message.toString())
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
            id = 1,
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
        if (userToFind.isNotEmptyNickName()) {
            return try {
                val userData: User?
                withContext(Dispatchers.IO) {
                    val document =
                        usersCollection.document(userToFind.adminEmailOrPhone)
                            .collection(userToFind.nickName.lowercase())
                            .document(userToFind.nickName.lowercase())
                            .get()
                            .await()

                    userData = document?.toObject(User::class.java)
                }

                return userData

            } catch (e: Exception) {
                e.printStackTrace()
                setUserWithError(e.message ?: "findUserInCollection, Error: ${e.message}")
                null
            }
        } else {
            return null
        }
    }


    override suspend fun findAdminInCollectionByDocumentName(documentName: String): Admin? {
        val updatedDocumentName = documentName.formatStringPhoneDelLeadNullAndAddPlus()
        return try {
            val adminData: Admin?
            withContext(Dispatchers.IO) {
                val document = adminsCollection.document(updatedDocumentName)
                    .get()
                    .await()

                adminData = document?.toObject(Admin::class.java)
            }
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
                    ),
                    nickName
                )
                return false
            } else {
                if (sigInResultAdmin == StatusEmailSignIn.ADMIN_NOT_FOUND) {
                    removeRecordFromCollection(
                        FIREBASE_ADMINS_AND_USERS_COLLECTION,
                        email
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
        return false
    }

    override suspend fun setWrongPasswordUser(user: User) {
        isWrongPassword = user
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
                adminEmailOrPhone = phone.formatStringPhoneDelLeadNullAndAddPlus(),
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
        if (globalRepoUser.pressedLogoutButton) {
            globalRepoUser = globalRepoUser.copy(pressedLogoutButton = false)
        }
        val adminEmailOrPhoneWithPlus =
            adminEmailOrPhone.formatStringPhoneDelLeadNullAndAddPlus()
        val adminData = findAdminInCollectionByDocumentName(adminEmailOrPhoneWithPlus)
        if (adminData == null || adminData.emailOrPhoneNumber.trim() != adminEmailOrPhoneWithPlus.trim()) {
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
                            nickName = adminData.nickName,
                            fakeEmail = createFakeUserEmail(
                                nickName,
                                adminEmailOrPhoneWithPlus
                            ),
                            password = password
                        ),
                        nickName
                    )
                    if (trySignIn == StatusFakeEmailSignIn.USER_SIGNED_IN) {
                        admin = adminData
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

    private suspend fun deleteOldRemindersFromSchedule(currentTimestamp: Long) {
        try {
            val querySnapshot: QuerySnapshot
            withContext(Dispatchers.IO) {
                querySnapshot = scheduleCollection
                    .whereLessThan("alarmTime", currentTimestamp)
                    .get()
                    .await()
            }

            for (document in querySnapshot.documents) {
                currentCoroutineContext().ensureActive()
                withContext(Dispatchers.IO) {
                    scheduleCollection.document(document.id).delete().await()
                }
            }
        } catch (e: CancellationException) {
            println("Coroutine was cancelled: ${e.message}")
        } catch (e: Exception) {
            println("Error deleting old reminders from schedule: ${e.message}")
        }
    }

    private suspend fun deleteOldRemindersFromScheduleToDelete(currentTimestamp: Long) {
        try {
            val querySnapshot: QuerySnapshot
            withContext(Dispatchers.IO) {
                querySnapshot = scheduleDeleteCollection
                    .whereLessThan("id", currentTimestamp)
                    .get()
                    .await()
            }

            for (document in querySnapshot.documents) {
                currentCoroutineContext().ensureActive()
                withContext(Dispatchers.IO) {
                    scheduleCollection.document(document.id).delete().await()
                }
            }
        } catch (e: CancellationException) {
            println("Coroutine was cancelled: ${e.message}")
        } catch (e: Exception) {
            println("Error deleting schedulers from schedule: ${e.message}")
        }
    }

    private suspend fun deleteOldDocumentsFromRemindersQueueCollection(currentTimestamp: Long) {
        try {
            val querySnapshot: QuerySnapshot
            withContext(Dispatchers.IO) {
                querySnapshot = remindersCollection
                    .whereLessThan("alarmTime", currentTimestamp)
                    .get()
                    .await()
            }

            for (document in querySnapshot.documents) {
                currentCoroutineContext().ensureActive()
                withContext(Dispatchers.IO) {
                    scheduleCollection.document(document.id).delete().await()
                }
            }
        } catch (e: CancellationException) {
            println("Coroutine was cancelled: ${e.message}")
        } catch (e: Exception) {
            println("Error deleting old reminders from queue: ${e.message}")
        }
    }


    override suspend fun deleteOldTasks() {
        val userForModify = globalRepoUser.copy()
        if (userForModify.nickName != User.DEFAULT_NICK_NAME &&
            userForModify.nickName != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
        ) {
            val currentTimestamp = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                deleteOldRemindersFromSchedule(currentTimestamp)
                deleteOldRemindersFromScheduleToDelete(currentTimestamp)
                deleteOldDocumentsFromRemindersQueueCollection(currentTimestamp)
            }


            val taskMaxExpireTimeInMillis =
                context.resources.getInteger(R.integer.max_expired_task_save_in_days) * MILLIS_IN_DAY
            val privateTasks =
                userForModify.listToDo.thingsToDoPrivate.privateTasks.filter { task ->
                    task.cutoffTime - currentTimestamp > taskMaxExpireTimeInMillis
                }
            val sharedTasks =
                userForModify.listToDo.thingsToDoShared.externalTasks.filter { externalTask ->
                    externalTask.task.cutoffTime - currentTimestamp > taskMaxExpireTimeInMillis
                }
            val tasksForOtherUsers =
                userForModify.listToDo.thingsToDoForOtherUsers.externalTasks.filter { externalTask ->
                    externalTask.task.cutoffTime - currentTimestamp > taskMaxExpireTimeInMillis
                }

            val toDoOld = userForModify.listToDo
            val updatedTodoList = toDoOld.copy(
                thingsToDoPrivate = PrivateTasks(privateTasks = privateTasks),
                thingsToDoShared = ExternalTasks(externalTasks = sharedTasks),
                thingsToDoForOtherUsers = ExternalTasks(externalTasks = tasksForOtherUsers)
            )

            val userCollection =
                usersCollection.document(userForModify.adminEmailOrPhone)
                    .collection(userForModify.nickName.lowercase())
                    .document(userForModify.nickName.lowercase())

            val freshToken = if (token != NO_NEW_TOKEN) {
                token
            } else {
                globalRepoUser.token
            }
            val userForUpdate = userForModify.copy(
                listToDo = updatedTodoList,
                token = freshToken
            )

            try {
                withContext(Dispatchers.IO) {
                    userCollection.set(userForUpdate).await()
                }
            } catch (e: Exception) {
                logExceptionToFirebase("deleteOldTasks", e.message.toString())
            }
        }

    }

    companion object {
        const val NO_NOTIFICATION_PERMISSION_GRANTED = "NO_NOTIFICATION_PERMISSION_GRANTED"

        const val TIMEOUT_VERIFICATION_MAIL = 60000L * 60L * 24L
        const val TIMEOUT_VERIFICATION_CHECK = 15000L

        const val FIREBASE_ADMINS_COLLECTION = "admins"
        const val FIREBASE_USERS_COLLECTION = "users"
        const val FIREBASE_REMINDERS_COLLECTION = "reminders-in-queue"

        const val NO_NEW_TOKEN = "No new token"

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

        private const val MILLIS_IN_MINUTE = 60 * 1_000L
        private const val MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60
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



