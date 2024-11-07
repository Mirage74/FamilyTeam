package com.balex.common.domain.usecases.user

import com.balex.common.data.repository.TaskMode
import com.balex.common.domain.entity.Task
import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

class AddPrivateTaskToFirebaseUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(task: Task, taskMode: TaskMode, diffReminders: Int, token: String) =
        repository.addOrModifyPrivateTaskToFirebase(task, taskMode, diffReminders, token)
}