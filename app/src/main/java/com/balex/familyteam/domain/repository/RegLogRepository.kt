package com.balex.familyteam.domain.repository

import android.app.Activity
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    fun observeAdmin(): StateFlow<Admin>

    fun observeUser(): StateFlow<User>

    fun observeLanguage(): StateFlow<String>

    fun observeVerifiedStatus(): StateFlow<Boolean>

    fun registerAdmin(email: String = "", phone: String = "", password: String)

    fun loginAdmin(email: String = "", phone: String = "", password: String)

    fun loginUser(email: String, password: String)

    fun saveUser(userLogin: String)

    fun saveLanguage(language: String)

    fun getCurrentLanguage(): String

    suspend fun addAdmin(admin: Admin): Result<Unit>

    suspend fun registerAndVerifyByEmail(email: String, password: String)

    suspend fun registerAndVerifyByPhone(phoneNumber: String, verificationCode: String, activity: Activity)



}