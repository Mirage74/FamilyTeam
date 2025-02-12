package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

@Suppress("unused")
class CreateFakeUserEmailUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(nick: String, data: String) = repository.createFakeUserEmail(nick, data)
}