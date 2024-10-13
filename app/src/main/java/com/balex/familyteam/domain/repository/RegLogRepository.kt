package com.balex.familyteam.domain.repository

import com.balex.familyteam.data.repository.RegLogRepositoryImpl.Companion.StatusFakeEmailSignIn
import com.balex.common.entity.Admin
import com.balex.common.entity.User
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    suspend fun logoutUser()

    suspend fun resetWrongPasswordUserToDefault()

    fun getRepoUser(): User

    fun getWrongPasswordUser(): User

    fun setUserAsVerified()

    suspend fun setUserWithError(message: String)

    fun observeUser(): StateFlow<User>

    suspend fun findAdminInCollectionByDocumentName(documentName: String): Admin?

    suspend fun checkUserInCollectionAndLoginIfExist(
        adminEmailOrPhone: String,
        nickName: String,
        password: String
    ): User

    fun observeIsWrongPassword(): StateFlow<User>

    suspend fun emitUserNeedRefresh()

    fun getCurrentLanguage(): String

    fun saveLanguage(language: String)

    fun observeLanguage(): StateFlow<String>

    fun observeSmsVerificationError(): StateFlow<String>

    fun storageClearPreferences()

    fun storageSavePreferences(email: String, nickName: String, password: String, language: String)

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

    suspend fun addUserToCollection(userToAdd: User): Result<Unit>

    suspend fun addAdminToCollection(admin: Admin): Result<Unit>

    suspend fun removeRecordFromCollection(collectionName: String, emailOrPhoneNumber: String)

    suspend fun signToFirebaseWithFakeEmail(userToSignIn: User): StatusFakeEmailSignIn


}