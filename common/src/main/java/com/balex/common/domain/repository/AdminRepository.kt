package com.balex.common.domain.repository

import com.balex.common.domain.entity.User

interface AdminRepository {

    suspend fun createNewUser(user: User)

    suspend fun deleteUser(userName: String)

}