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
        //Storage.clearPreferences(context)
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

    override fun getRepoAdmin(): Admin {
        return admin
    }

    override fun getRepoUser(): User {
        return user
    }

    override fun saveLanguage(language: String) {
        this.language = language
        Storage.saveLanguage(context, language)
    }

    override fun getCurrentLanguage(): String {
        return language
    }

    override suspend fun addAdmin(admin: Admin): Result<Unit> {
        return try {
            adminsCollection.add(admin).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addUser(user: User): Result<Unit> {
        return try {
            val newUser = if (user.fakeEmail == User.DEFAULT_FAKE_EMAIL) {
                user.copy(fakeEmail = createFakeUserEmail(user.nickName, user.adminEmailOrPhone))
            } else {
                user
            }
            usersCollection.add(newUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signToFirebaseInWithEmailAndPassword() {
        val fakeEmail = createFakeUserEmail(user.nickName, user.adminEmailOrPhone)

        FirebaseAuth.getInstance().signOut()
        auth.signInWithEmailAndPassword(fakeEmail, user.password)
            .addOnCompleteListener { signTask ->
                if (signTask.isSuccessful) {
                    findUserByEmailInFirebase(fakeEmail, user.nickName) { document ->
                        if (document != null) {
                            document.toObject(User::class.java)?.let {
                                user = it
                            }
                            coroutineScope.launch {
                                isCurrentUserNeedRefreshFlow.emit(Unit)
                            }
                        } else {
                            //Storage.clearPreferences(context)
                            throw RuntimeException("No user with email $fakeEmail, nickName ${user.nickName} found in Firebase")
                        }
                    }
                } else {
                    signTask.exception?.message?.let {
                        //Storage.clearPreferences(context)
                        throw RuntimeException("signInWithEmailAndPassword, signTask Error: $it")
                    }
                }
            }


    }

    override suspend fun registerAndVerifyByEmail(
        email: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        FirebaseAuth.getInstance().signOut()

        findUserByEmailInFirebase(email, nickName) { document ->
            if (document != null) {
                var newUser = User()
                document.toObject(User::class.java)?.let {
                    newUser = it
                }
                val newAdmin = Admin(
                    registrationOption = RegistrationOption.EMAIL,
                    emailOrPhoneNumber = newUser.adminEmailOrPhone,
                    isEmailOrPhoneNumberVerified = true
                )

                admin = newAdmin
                user = newUser
                coroutineScope.launch {
                    isCurrentUserNeedRefreshFlow.emit(Unit)
                }
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val authUser = auth.currentUser
                            authUser?.sendEmailVerification()
                                ?.addOnCompleteListener { emailVerification ->
                                    if (emailVerification.isSuccessful) {
                                        coroutineScope.launch {
                                            while (!isUserMailOrPhoneVerified) {
                                                authUser.reload()
                                                    .addOnCompleteListener { reloadTask ->
                                                        if (reloadTask.isSuccessful) {
                                                            if (authUser.isEmailVerified) {
                                                                regUserWithFakeEmail(
                                                                    email,
                                                                    nickName,
                                                                    displayName,
                                                                    password
                                                                )
                                                                isUserMailOrPhoneVerified = true
                                                            }
                                                        }
                                                    }
                                                delay(1000)
                                            }
                                        }
                                    }
                                }
                        } else {
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                throw RuntimeException("registerAndVerifyByEmail: $USER_ALREADY_EXIST_IN_AUTH_BUT_NOT_IN_USERS")
                            } else {
                                task.exception?.message?.let {
                                    throw RuntimeException("registerAndVerifyByEmail: $REGISTRATION_ERROR: $it")
                                }
                            }
                        }
                    }
            }
        }
    }

    override fun regUserWithFakeEmail(
        emailOrPhone: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val fakeEmail = createFakeUserEmail(nickName, emailOrPhone)
        val registrationOption = if (emailOrPhone.contains("@", true)) {
            RegistrationOption.EMAIL
        } else {
            RegistrationOption.PHONE
        }
        auth.createUserWithEmailAndPassword(fakeEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    auth.signInWithEmailAndPassword(
                        fakeEmail,
                        password
                    ).addOnCompleteListener { signTask ->
                        if (signTask.isSuccessful) {
                            addAdminAndUserToFirebase(
                                registrationOption,
                                emailOrPhone,
                                //createFakeUserEmail(nickName, emailOrPhone),
                                nickName,
                                displayName,
                                password
                            )
                        } else {
                            signTask.exception?.message?.let {
                                Log.e(
                                    "regUserWithFakeEmail, signTask Error",
                                    it
                                )
                            }
                        }
                    }
                } else {
                    task.exception?.message?.let {
                        Log.e("regUserWithFakeEmail, Registration Error", it)
                    }
                }
            }
    }

    override fun setAdminAndUser(
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
            val resultFirebaseRegAdmin = addAdmin(newAdmin)
            if (resultFirebaseRegAdmin.isSuccess) {
                val resultFirebaseRegUser = addUser(newUser)
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

    private fun findUserByEmailInFirebase(
        email: String,
        nickName: String,
        callback: (DocumentSnapshot?) -> Unit
    ) {
        usersCollection
            .whereEqualTo("fakeEmail", email)
            .whereEqualTo("nickName", nickName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    callback(documents.documents[0])
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                callback(null)
            }
    }


    private fun createFakeUserEmail(nick: String, data: String): String {
        return if (data.contains("@", true)) {
            "$nick-$data".lowercase().trim()
        } else {
            nick + "-" + data.substring(1) + "@" + FAKE_EMAIL_DOMAIN.lowercase().trim()
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

        const val SMS_VERIFICATION_ERROR_INITIAL = "SMS_VERIFICATION_ERROR_INITIAL"
        const val FAKE_EMAIL_DOMAIN = "balexvic.com"

        const val ERROR_ADD_USER_TO_FIREBASE = "ERROR_ADD_USER_TO_FIREBASE"
        const val ERROR_ADD_ADMIN_TO_FIREBASE = "ERROR_ADD_ADMIN_TO_FIREBASE"
        const val USER_ALREADY_EXIST_IN_AUTH_BUT_NOT_IN_USERS = "user already exist in AUTH, but not in USERS"
        const val REGISTRATION_ERROR = "Registration Error"
    }
}