package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

@Suppress("unused")
class RefreshFCMLastTimeUpdatedUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke() = repository.refreshFCMLastTimeUpdated()
}