package com.balex.familyteam.domain.usecase.user

import com.balex.common.domain.entity.User
import com.balex.familyteam.domain.repository.UserRepository
import javax.inject.Inject

class RemoveUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(nickName: String) = repository.removeUser(nickName)
}