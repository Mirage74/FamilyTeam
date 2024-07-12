package com.balex.familyteam.domain.usecase.regLog

import android.app.Activity
import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class ResendVerificationCodeUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(phoneNumber: String, activity: Activity) = repository.resendVerificationCode(phoneNumber, activity)
}