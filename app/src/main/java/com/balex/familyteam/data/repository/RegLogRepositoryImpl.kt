package com.balex.familyteam.data.repository

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.balex.familyteam.data.datastore.Storage
import com.balex.familyteam.data.mappers.mapperFirebaseAdminToEntity
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.Language
import com.balex.familyteam.domain.entity.LanguagesList
import com.balex.familyteam.domain.entity.RegistrationOption
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.RegLogRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RegLogRepositoryImpl @Inject constructor(
    private val context: Context
) : RegLogRepository {

    private var _admin = Admin()
    private val admin: Admin
        get() = _admin.copy()

    private var _user = User()
    private val user: User
        get() = _user.copy()

    private var _language = Language.DEFAULT_LANGUAGE.symbol
    private val language: String
        get() = _language

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

    private var storedSmsVerificationId = SMS_VERIFICATION_ID_INITIAL
    private var resendTokenForSmsVerification: PhoneAuthProvider.ForceResendingToken? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun observeUser(): StateFlow<User> = flow {
        val userFakeEmailFromStorageInfo = Storage.getUser(context)
        Log.d("userFakeEmailFromStorageInfo", userFakeEmailFromStorageInfo)
        val userFakeEmailFromStorage = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
        val phoneLanguageFromStorage = Storage.getLanguage(context)
        val phoneLang =
            if (phoneLanguageFromStorage != Storage.NO_LANGUAGE_SAVED_IN_SHARED_PREFERENCES) {
                _language = phoneLanguageFromStorage
                isCurrentLanguageNeedRefreshFlow.emit(Unit)
                phoneLanguageFromStorage
            } else {
                _language = getCurrentLanguage(context)
                language
            }

        if (userFakeEmailFromStorage == Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES) {
            val emptyUserNotSaved =
                User(nickName = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES, language = phoneLang)
            _user = emptyUserNotSaved
        } else {
            val userFromStorage = extractUserInfoFromFakeEmail(userFakeEmailFromStorage)

            _user = userFromStorage

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
                _language = phoneLanguageFromStorage
                phoneLanguageFromStorage
            } else {
                getCurrentLanguage(context)
            }
        _language = phoneLang
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


    override fun saveLanguage(language: String) {
        _language = language
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
            usersCollection.add(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun registerAndVerifyByEmail(
        email: String,
        nickName: String,
        displayName: String,
        password: String
    ) {

        var user = FirebaseAuth.getInstance().currentUser
        FirebaseAuth.getInstance().signOut()
        user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userInfo =
                user.uid + " " + user.displayName + " " + user.email + " " + user.phoneNumber + " " +
                        user.photoUrl + " " + user.isEmailVerified + " " + user.providerData + " " + user.providerId +
                        " " + user.metadata + " " + user.multiFactor + " " + user.tenantId
            Log.d("userInfo", userInfo)
            if (user.isEmailVerified) {
                // Email is verified
                Log.d("EmailVerification", "Email is verified")
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
                                            authUser.reload().addOnCompleteListener { reloadTask ->
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
                        task.exception?.message?.let {
                            Log.e("Registration Error", it)
                        }
                    }
                }
        }
    }

    private fun regUserWithFakeEmail(
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
//                            coroutineScope.launch {
//                                val user = FirebaseAuth.getInstance().currentUser
                                addAdminAndUserToFirebase(
                                    registrationOption,
                                    emailOrPhone,
                                    nickName,
                                    displayName,
                                    password
                                )
//                                isCurrentUserNeedRefreshFlow.emit(
//                                    Unit
//                                )
                            //}
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
            adminEmailOrPhone = emailOrPhoneNumber,
            displayName = displayName,
            language = language
        )

        coroutineScope.launch {
            val resultFirebaseRegAdmin = addAdmin(newAdmin)
            if (resultFirebaseRegAdmin.isSuccess) {
                val resultFirebaseRegUser = addUser(newUser)
                if (resultFirebaseRegUser.isSuccess) {
                    _admin = newAdmin
                    _user = newUser
                    Storage.saveUser(context, createFakeUserEmail(nickName, emailOrPhoneNumber))
                    Storage.saveUsersPassword(context, password)
                    Storage.saveLanguage(context, language)
                    coroutineScope.launch {
                        isCurrentUserNeedRefreshFlow.emit(Unit)
                    }

                } else {
                    Log.d("Error", "Error user")
                }
            } else {
                Log.d("Error", "Error admin")
            }
        }


    }

    private fun addAUserToFirebase(
        emailOrPhoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val newUser = User(
            nickName = nickName,
            isAdmin = true,
            adminEmailOrPhone = emailOrPhoneNumber,
            displayName = displayName,
            password = password,
            language = language
        )

        coroutineScope.launch {
            val resultFirebaseRegUser = addUser(newUser)
            if (resultFirebaseRegUser.isSuccess) {
                _user = newUser
                Storage.saveUser(context, createFakeUserEmail(nickName, emailOrPhoneNumber))
                Storage.saveUsersPassword(context, password)
                Storage.saveLanguage(context, language)

                coroutineScope.launch {
                    isCurrentUserNeedRefreshFlow.emit(
                        Unit
                    )
                }
            } else {
                Log.d("Error", "Error user")
            }

        }

        coroutineScope.launch {
            isCurrentUserNeedRefreshFlow.emit(Unit)
        }
    }

    override suspend fun sendSmsVerifyCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: Activity
    ) {
        val auth = Firebase.auth
        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(TIMEOUT_VERIFICATION, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential, phoneNumber, nickName, displayName, password)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _isSmsVerificationError = e.message ?: "Error"
                    coroutineScope.launch {
                        isSmsVerificationErrorNeedRefreshFlow.emit(Unit)
                    }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedSmsVerificationId = verificationId
                    resendTokenForSmsVerification = token
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun verifySmsCode(
        verificationCode: String,
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val credential = PhoneAuthProvider.getCredential(storedSmsVerificationId, verificationCode)
        signInWithCredential(credential, phoneNumber, nickName, displayName, password)
    }

    private fun signInWithCredential(
        credential: PhoneAuthCredential,
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val auth = Firebase.auth
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    coroutineScope.launch {
                        while (!isUserMailOrPhoneVerified) {
                            user?.let {
                                it.reload().addOnCompleteListener { reloadTask ->
                                    if (reloadTask.isSuccessful) {
                                        if (user.phoneNumber == phoneNumber) {

                                            regUserWithFakeEmail(phoneNumber, nickName, displayName, password)
                                            isUserMailOrPhoneVerified = true
                                        }
                                    }
                                }
                            }
                            delay(1000)
                        }
                    }
                } else {
                    task.exception?.message?.let {
                        Log.e("Registration Error", it)
                    }
                }
            }
    }

    override fun resendVerificationCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: Activity
    ) {
        val token: PhoneAuthProvider.ForceResendingToken
        if (resendTokenForSmsVerification != null) {
            token = resendTokenForSmsVerification as PhoneAuthProvider.ForceResendingToken
        } else {
            throw RuntimeException("resendTokenForSmsVerification is null")
        }

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(TIMEOUT_VERIFICATION, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential, phoneNumber, nickName, displayName, password)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _isSmsVerificationError = e.message ?: "Error"
                    coroutineScope.launch {
                        isSmsVerificationErrorNeedRefreshFlow.emit(Unit)
                    }
                }

            })
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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

    private fun findAdminByEmailOrPhoneNumber(
        emailOrPhoneNumber: String,
        callback: (DocumentSnapshot?) -> Unit
    ) {
        // Ссылка на коллекцию "admins"


        // Поиск документа по значению поля "emailOrPhoneNumber"
        adminsCollection.whereEqualTo("emailOrPhoneNumber", emailOrPhoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    // Если документ найден, возвращаем первый документ
                    callback(documents.documents[0])
                } else {
                    // Если документ не найден, возвращаем null
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                // Обработка ошибок
                exception.printStackTrace()
                callback(null)
            }
    }

    fun getA() {


// Пример использования функции
        findAdminByEmailOrPhoneNumber("eue@mail.com") { document ->
            if (document != null) {
                // Документ найден
                val adm = mapperFirebaseAdminToEntity(document.data)
                println("Document found: ${document.data}")
            } else {
                // Документ не найден
                println("No document found with the specified emailOrPhoneNumber")
            }
        }
    }

    private fun createFakeUserEmail(nick: String, data: String): String {
        return if (data.contains("@", true)) {
            "$nick-$data"
        } else {
            nick + "-" + data.substring(1) + "@" + FAKE_EMAIL_DOMAIN
        }
    }

    private fun extractUserInfoFromFakeEmail(fakeEmail: String): User {
        if (fakeEmail.endsWith(FAKE_EMAIL_DOMAIN, false)) {
            val parts = fakeEmail.split("@")
            val nick = parts[0].split("-").first()
            val phone = parts[1].split("-").first()
            return User(nickName = nick, adminEmailOrPhone = phone)
        } else {
            val parts = fakeEmail.split("@")
            val nick = parts[0].split("-").first()
            return User(nickName = nick, adminEmailOrPhone = parts[1])

        }

    }

    companion object {
        const val TIMEOUT_VERIFICATION = 60L
        const val FIREBASE_ADMINS_COLLECTION = "admins"
        const val FIREBASE_USERS_COLLECTION = "users"
        const val SMS_VERIFICATION_ID_INITIAL = "SMS_VERIFICATION_ID_INITIAL"
        const val SMS_VERIFICATION_ERROR_INITIAL = "SMS_VERIFICATION_ERROR_INITIAL"
        const val FAKE_EMAIL_DOMAIN = "balexvic.com"
    }
}