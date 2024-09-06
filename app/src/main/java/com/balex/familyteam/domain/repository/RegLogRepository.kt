package com.balex.familyteam.domain.repository

import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    fun getRepoUser(): User

    fun setUserAsVerified()

    fun observeUser(): StateFlow<User>

    suspend fun emitUserNeedRefresh()

    fun getCurrentLanguage(): String

    fun saveLanguage(language: String)

    fun observeLanguage(): StateFlow<String>

    fun observeSmsVerificationError(): StateFlow<String>

    suspend fun registerAndVerifyNewTeamByEmail(
        email: String,
        nickName: String,
        displayName: String,
        password: String
    )

    suspend fun regUserWithFakeEmailToAuthAndToUsersCollection(
        emailOrPhone: String,
        nickName: String,
        displayName: String,
        password: String
    )


    suspend fun addUserToCollection(user: User): Result<Unit>

    suspend fun addAdminToCollection(admin: Admin): Result<Unit>

    suspend fun signRepoCurrentUserToFirebaseWithEmailAndPassword()







}