package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class VerifySmsCodeUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(verificationCode: String,
                        phoneNumber: String,
                        nickName: String,
                        displayName: String,
                        password: String) =
        repository.verifySmsCode(verificationCode, phoneNumber, nickName, displayName, password)
}