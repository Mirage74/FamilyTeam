package com.balex.common.domain.usecases.user

import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

class RemoveUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(nickName: String) = repository.removeUser(nickName)
}