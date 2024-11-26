package com.balex.common.domain.usecases.admin

import com.balex.common.domain.repository.AdminRepository
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(userName: String) = repository.deleteUser(userName)
}