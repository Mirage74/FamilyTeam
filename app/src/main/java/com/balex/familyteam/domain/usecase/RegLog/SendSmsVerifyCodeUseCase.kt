package com.balex.familyteam.domain.usecase.regLog

import android.app.Activity
import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class SendSmsVerifyCodeUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(phoneNumber: String, activity: Activity) =
        repository.sendSmsVerifyCode(phoneNumber, activity)
}