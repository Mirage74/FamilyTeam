package com.balex.common.domain.usecases.user

import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

class AddExternalTaskToFirebaseUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(externalTask: ExternalTask) = repository.addExternalTaskToFirebase(externalTask)
}