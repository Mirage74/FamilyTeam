package com.balex.common.domain.repository

import com.balex.common.domain.entity.Admin
import com.balex.common.domain.entity.User

interface AdminRepository {

    suspend fun createNewUser(user: User)

    suspend fun deleteUser(userName: String)

    suspend fun deleteTeam(navigateToNotloggedScreen: () -> Unit)

    suspend fun deleteSelfAccount(userName: String, navigateToNotloggedScreen: () -> Unit)

}