package com.balex.common.domain.usecases.admin

import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.AdminRepository
import javax.inject.Inject

@Suppress("unused")
class CreateNewUserUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(user: User) = repository.createNewUser(user)
}