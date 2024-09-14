package com.balex.familyteam.domain.repository

import com.balex.familyteam.data.repository.RegLogRepositoryImpl.Companion.StatusFakeEmailSignIn
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    fun getRepoUser(): User

    fun setUserAsVerified()

    fun observeUser(): StateFlow<User>

    fun observeIsWrongPassword(): StateFlow<User>

    suspend fun emitUserNeedRefresh()

    fun getCurrentLanguage(): String

    fun saveLanguage(language: String)

    fun observeLanguage(): StateFlow<String>

    fun observeSmsVerificationError(): StateFlow<String>

    fun storageClearPreferences()

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

    fun createFakeUserEmail(nick: String, data: String): String

    suspend fun addUserToCollection(userToAdd: User): Result<Unit>

    suspend fun addAdminToCollection(admin: Admin): Result<Unit>

    suspend fun removeRecordFromCollection(collectionName: String, emailOrPhoneNumber: String)

    suspend fun signToFirebaseWithFakeEmail(userToSignIn: User): StatusFakeEmailSignIn


}