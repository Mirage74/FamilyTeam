package com.balex.familyteam.data.repository

import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class AdminRepositoryImpl(): AdminRepository {
    override val users: StateFlow<List<User>>
        get() = TODO("Not yet implemented")

    override fun addUser(user: User) {
        TODO("Not yet implemented")
    }

    override fun removeUser(login: String) {
        TODO("Not yet implemented")
    }
}