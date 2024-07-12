package com.balex.familyteam.domain.repository

import android.app.Activity
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    fun getRepoAdmin(): Admin

    fun getRepoUser(): User

    fun getCurrentLanguage(): String

    fun observeUser(): StateFlow<User>

    fun observeLanguage(): StateFlow<String>

    fun observeVerifiedStatus(): StateFlow<Boolean>

    fun observeSmsVerificationError(): StateFlow<String>

    fun registerAdmin(email: String = "", phone: String = "", password: String)

    fun loginAdmin(email: String = "", phone: String = "", password: String)

    fun loginUser(email: String, password: String)

    fun saveUser(userLogin: String)

    fun saveLanguage(language: String)

    fun verifySmsCode(verificationCode: String, phoneNumber: String)

    fun resendVerificationCode(phoneNumber: String, activity: Activity)

    suspend fun addAdmin(admin: Admin): Result<Unit>

    suspend fun registerAndVerifyByEmail(email: String, password: String)

    suspend fun sendSmsVerifyCode(phoneNumber: String, activity: Activity)

}