package com.balex.common.domain.usecases.user

import com.balex.common.data.repository.TaskMode
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

@Suppress("unused")
class AddExternalTaskToFirebaseUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(externalTask: ExternalTask, taskMode: TaskMode) = repository.addOrModifyExternalTaskToFirebase(externalTask, taskMode)
}