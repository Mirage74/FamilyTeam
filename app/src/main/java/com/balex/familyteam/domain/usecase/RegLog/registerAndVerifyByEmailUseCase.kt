package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class RegisterAndVerifyByEmailUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(email: String, nickName: String, displayName: String, password: String) =
        repository.registerAndVerifyByEmail(email, nickName, displayName, password)
}