package com.balex.familyteam.domain.repository

import com.balex.familyteam.presentation.MainActivity

interface PhoneFirebaseRepository {

    fun sendSmsVerifyCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: MainActivity
    )

    fun resendVerificationCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: MainActivity
    )

    fun verifySmsCode(
        verificationCode: String,
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    )
}