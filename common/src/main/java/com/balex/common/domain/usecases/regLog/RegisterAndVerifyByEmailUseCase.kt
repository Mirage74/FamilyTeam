package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

class RegisterAndVerifyByEmailUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(email: String, nickName: String, displayName: String, password: String) =
        repository.registerAndVerifyNewTeamByEmail(email, nickName, displayName, password)
}