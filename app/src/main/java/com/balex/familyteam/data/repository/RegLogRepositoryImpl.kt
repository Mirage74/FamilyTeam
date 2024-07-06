package com.balex.familyteam.data.repository

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.balex.familyteam.data.datastore.Storage
import com.balex.familyteam.data.mappers.mapperFirebaseAdminToEntity
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.Admins
import com.balex.familyteam.domain.entity.Language
import com.balex.familyteam.domain.entity.LanguagesList
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.RegLogRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class RegLogRepositoryImpl @Inject constructor(
    private val context: Context
) : RegLogRepository {

    private var _user = User()
    private val user: User
        get() = _user.copy()

    private var _language = Language.DEFAULT_LANGUAGE.symbol
    private val language: String
        get() = _language

    private val isCurrentUserNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
    private val isCurrentLanguageNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)

    private val db = Firebase.firestore
    private val adminsCollection = db.collection(FIREBASE_ADMINS_COLLECTION)
    private val usersCollection = db.collection(FIREBASE_USERS_COLLECTION)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun observeUser(): StateFlow<User> = flow {
        val adminFromStorage = Storage.getUser(context)
        val phoneLanguageFromStorage = Storage.getLanguage(context)
        val phoneLang = if (phoneLanguageFromStorage != Storage.NO_LANGUAGE_SAVED_IN_SHARED_PREFERENCES) {
            _language = phoneLanguageFromStorage
            isCurrentLanguageNeedRefreshFlow.emit(Unit)
            phoneLanguageFromStorage
        } else {
            _language = getCurrentLanguage(context)
            language
        }

        if (adminFromStorage == Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES) {
            val emptyUserNotSaved = User(login = Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES, language = phoneLang)
            _user = emptyUserNotSaved
        } else {

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
        val phoneLang = if (phoneLanguageFromStorage != Storage.NO_LANGUAGE_SAVED_IN_SHARED_PREFERENCES) {
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

    override fun observeAdmin(): StateFlow<Admin> {
        TODO("Not yet implemented")
    }

    override fun registerAdmin(email: String, phone: String, password: String) {
        TODO("Not yet implemented")
    }

    override fun loginAdmin(email: String, phone: String, password: String) {
        TODO("Not yet implemented")
    }

    override fun loginUser(email: String, password: String) {
        TODO("Not yet implemented")
    }

    override fun saveLanguage(language: String) {
        _language = language
        Storage.saveLanguage(context, language)
    }

    override fun getCurrentLanguage(): String {
        return language
    }

    override suspend fun addAdmin(admin: Admin): Result<Unit> {
        //val ad = getA()

        return try {
            adminsCollection.add(admin).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    override suspend fun registerAndVerifyByEmail(email: String, password: String) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Регистрация успешна
                val user = auth.currentUser
                user?.sendEmailVerification()?.addOnCompleteListener { emailVerification->
                    if (emailVerification.isSuccessful) {
                        Log.d("EMAIL", "Email sent.")
                    }
                }

                // Вы можете сохранить информацию о пользователе в Firestore или Realtime Database
            } else {
                // Регистрация не удалась, показать сообщение пользователю
                task.exception?.message?.let {
                    Log.e("Registration Error", it)
                }
            }
        }
}

    override fun saveUser(userLogin: String) {
        Storage.saveUser(context, userLogin)
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

    fun findAdminByEmailOrPhoneNumber(emailOrPhoneNumber: String, callback: (DocumentSnapshot?) -> Unit) {
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
    companion object {
        const val FIREBASE_ADMINS_COLLECTION = "admins"
        const val FIREBASE_USERS_COLLECTION = "users"
    }
}