package com.balex.familyteam.domain.repository

import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    fun getRepoAdmin(): Admin

    fun setAdminAndUser(emailOrPhone: String,
                        nickName: String,
                        displayName: String,
                        password: String)

    fun getCurrentLanguage(): String

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


    suspend fun addAdmin(admin: Admin): Result<Unit>

    suspend fun addUser(user: User): Result<Unit>

    suspend fun registerAndVerifyByEmail(email: String, nickName: String, displayName: String, password: String)

    suspend fun emitUserNeedRefresh()

}