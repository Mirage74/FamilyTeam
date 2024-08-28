package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class SetLoggedUserUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(user: User) =
        repository.setLoggedUser(user)
}