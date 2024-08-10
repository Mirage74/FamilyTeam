package com.balex.familyteam.domain.repository

import com.balex.familyteam.domain.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    fun getUser(): User
}
