package com.balex.common.domain.repository

import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.domain.entity.PrivateTasks
import com.balex.common.domain.entity.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    fun observeUsersList(): StateFlow<List<User>>

    fun observeExternalTasks(): StateFlow<ExternalTasks>

    fun observePrivateTasks(): StateFlow<PrivateTasks>

    fun observeListToShop(): StateFlow<List<String>>

    fun observeMyTasksForOtherUsers(): StateFlow<ExternalTasks>

    suspend fun removeUser(nickName: String)

}
