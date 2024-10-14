package com.balex.familyteam.domain.repository

interface PhoneFirebaseRepository {

    suspend fun sendSmsVerifyCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: com.balex.familyteam.presentation.MainActivity
    )

    suspend fun resendVerificationCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: com.balex.familyteam.presentation.MainActivity
    )

    suspend fun verifySmsCode(
        verificationCode: String,
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    )
}