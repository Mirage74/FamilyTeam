package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

class SetNewTokenUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(newToken: String) = repository.setNewToken(newToken)
}