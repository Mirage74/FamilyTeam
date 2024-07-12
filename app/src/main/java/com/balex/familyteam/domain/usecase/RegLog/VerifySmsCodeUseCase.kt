package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class VerifySmsCodeUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(verificationCode: String, phoneNumber: String) =
        repository.verifySmsCode(verificationCode, phoneNumber)
}