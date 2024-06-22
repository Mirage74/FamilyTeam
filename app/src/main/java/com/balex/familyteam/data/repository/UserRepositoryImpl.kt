package com.balex.familyteam.data.repository

import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class UserRepositoryImpl : UserRepository {
    override val user: StateFlow<User>
        get() = TODO("Not yet implemented")
}