package com.balex.familyteam.domain.repository

import android.app.Activity
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    fun getRepoAdmin(): Admin

    fun getCurrentLanguage(): String

    fun observeUser(): StateFlow<User>

    fun observeLanguage(): StateFlow<String>

    fun observeSmsVerificationError(): StateFlow<String>

    fun saveLanguage(language: String)

    fun verifySmsCode(verificationCode: String,
                      phoneNumber: String,
                      nickName: String,
                      displayName: String,
                      password: String)

    fun resendVerificationCode(phoneNumber: String,  nickName: String, displayName: String, password: String, activity: Activity)

    suspend fun addAdmin(admin: Admin): Result<Unit>

    suspend fun addUser(user: User): Result<Unit>

    suspend fun registerAndVerifyByEmail(email: String, nickName: String, displayName: String, password: String)

    suspend fun sendSmsVerifyCode(phoneNumber: String,  nickName: String, displayName: String, password: String, activity: Activity)

}