package com.balex.common.domain.repository

import com.balex.common.data.repository.TaskMode
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    suspend fun addOrModifyPrivateTaskToFirebase(task: Task, taskMode: TaskMode)

    suspend fun addOrModifyExternalTaskToFirebase(externalTask: ExternalTask, taskMode: TaskMode)

    suspend fun deleteTaskFromFirebase(externalTask: ExternalTask, taskType: UserRepositoryImpl.Companion.TaskType)

    fun observeUsersList(): StateFlow<List<String>>

    suspend fun emitUsersNicknamesListNeedRefresh()

    suspend fun removeUser(nickName: String)

}
