package com.balex.familyteam.domain.repository

import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    fun getRepoUser(): User

    fun setAdminAndUser(
        emailOrPhone: String,
        nickName: String,
        displayName: String,
        password: String
    )

    fun getCurrentLanguage(): String

    fun setUserAsVerified()

    fun observeUser(): StateFlow<User>

    fun observeLanguage(): StateFlow<String>

    fun observeSmsVerificationError(): StateFlow<String>

    fun saveLanguage(language: String)

    fun regUserWithFakeEmail(
        emailOrPhone: String,
        nickName: String,
        displayName: String,
        password: String
    )


    suspend fun addAdminToCollection(admin: Admin): Result<Unit>

    suspend fun addUserToCollection(user: User): Result<Unit>

    suspend fun signRepoCurrentUserToFirebaseWithEmailAndPassword()

    //fun loginToFirebaseAndLoadUserData(adminEmailOrPhone: String, nickName: String, password: String)


     suspend fun registerAndVerifyByEmail(
        email: String,
        nickName: String,
        displayName: String,
        password: String
    )

    suspend fun emitUserNeedRefresh()

}