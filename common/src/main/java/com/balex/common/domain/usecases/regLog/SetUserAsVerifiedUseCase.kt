package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

class SetUserAsVerifiedUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke() = repository.setUserAsVerified()
}