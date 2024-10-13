package com.balex.familyteam.domain.repository

import com.balex.common.entity.ExternalTasks
import com.balex.common.entity.PrivateTasks
import com.balex.common.entity.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    fun observeUsersList(): StateFlow<List<User>>

    fun observeExternalTasks(): StateFlow<ExternalTasks>

    fun observePrivateTasks(): StateFlow<PrivateTasks>

    fun observeListToShop(): StateFlow<List<String>>

    fun observeMyTasksForOtherUsers(): StateFlow<ExternalTasks>

    suspend fun removeUser(nickName: String)

}
