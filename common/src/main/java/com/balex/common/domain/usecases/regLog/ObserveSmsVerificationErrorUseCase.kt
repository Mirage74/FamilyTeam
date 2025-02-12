package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

@Suppress("unused")
class ObserveSmsVerificationErrorUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke() = repository.observeSmsVerificationError()
}