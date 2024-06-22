package com.balex.familyteam.domain.usecase.admin

import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.AdminRepository
import javax.inject.Inject

class AddUserUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    operator fun invoke(user: User) = repository.addUser(user)
}