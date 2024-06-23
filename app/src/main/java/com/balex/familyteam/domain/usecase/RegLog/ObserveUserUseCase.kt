package com.balex.familyteam.domain.usecase.RegLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class ObserveUserUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke() = repository.observeUser()
}