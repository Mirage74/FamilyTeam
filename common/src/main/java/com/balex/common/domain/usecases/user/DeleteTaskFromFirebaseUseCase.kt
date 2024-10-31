package com.balex.common.domain.usecases.user

import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

class DeleteTaskFromFirebaseUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        externalTask: ExternalTask,
        taskType: UserRepositoryImpl.Companion.TaskType,
        token: String
    ) = repository.deleteTaskFromFirebase(externalTask, taskType, token)
}