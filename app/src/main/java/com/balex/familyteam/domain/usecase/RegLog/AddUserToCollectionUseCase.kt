package com.balex.familyteam.domain.usecase.regLog

import com.balex.common.entity.User
import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class AddUserToCollectionUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(user: User) = repository.addUserToCollection(user)
}