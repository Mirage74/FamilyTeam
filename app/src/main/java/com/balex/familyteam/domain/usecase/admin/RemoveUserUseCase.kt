package com.balex.familyteam.domain.usecase.admin

import com.balex.familyteam.domain.repository.AdminRepository
import javax.inject.Inject

class RemoveUserUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(login: String) = repository.removeUser(login)
}