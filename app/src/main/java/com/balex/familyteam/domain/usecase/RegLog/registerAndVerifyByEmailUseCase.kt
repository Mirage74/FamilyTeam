package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class RegisterAndVerifyByEmailUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(email: String, password: String) = repository.registerAndVerifyByEmail(email, password)
}