package com.balex.common.domain.repository

import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.domain.entity.PrivateTasks
import com.balex.common.domain.entity.Task
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    suspend fun addPrivateTaskToFirebase(task: Task)

    suspend fun deleteTaskFromFirebase(externalTask: ExternalTask)

    fun observeUsersList(): StateFlow<List<String>>

    suspend fun emitUsersNicknamesListNeedRefresh()

    fun observeExternalTasks(): StateFlow<ExternalTasks>

    fun observePrivateTasks(): StateFlow<PrivateTasks>

    fun observeListToShop(): StateFlow<List<String>>

    fun observeMyTasksForOtherUsers(): StateFlow<ExternalTasks>

    suspend fun removeUser(nickName: String)

}
