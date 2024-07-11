package com.balex.familyteam.domain.usecase.regLog

import android.app.Activity
import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class RegisterAndVerifyByPhoneUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(phoneNumber: String, verificationCode: String, activity: Activity) =
        repository.registerAndVerifyByPhone(phoneNumber, verificationCode, activity)
}