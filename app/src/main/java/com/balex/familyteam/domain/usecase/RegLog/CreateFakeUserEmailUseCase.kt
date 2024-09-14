package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class CreateFakeUserEmailUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(nick: String, data: String) = repository.createFakeUserEmail(nick, data)
}