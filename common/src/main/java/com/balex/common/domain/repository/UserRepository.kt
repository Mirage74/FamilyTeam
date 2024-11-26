package com.balex.common.domain.repository

import com.balex.common.data.repository.TaskMode
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    suspend fun saveDeviceToken(token: String)

    suspend fun addOrModifyPrivateTaskToFirebase(task: Task, taskMode: TaskMode, token: String)

    suspend fun addOrModifyExternalTaskToFirebase(externalTask: ExternalTask, taskMode: TaskMode, token: String)

    suspend fun deleteTaskFromFirebase(externalTask: ExternalTask, taskType: UserRepositoryImpl.Companion.TaskType, token: String)

    fun observeUsersList(): StateFlow<List<String>>

    suspend fun emitUsersNicknamesListNeedRefresh()

}
