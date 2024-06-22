package com.balex.familyteam.domain.repository

import com.balex.familyteam.domain.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AdminRepository {

    val users: StateFlow<List<User>>

    fun addUser(user: User)

    fun removeUser(login: String)

}