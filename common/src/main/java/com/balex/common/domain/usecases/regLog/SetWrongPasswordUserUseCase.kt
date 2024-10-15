package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

class SetWrongPasswordUserUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(user: User) = repository.setWrongPasswordUser(user)
}