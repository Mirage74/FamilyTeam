package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class SetUserWithErrorUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(message: String) = repository.setUserWithError(message)
}