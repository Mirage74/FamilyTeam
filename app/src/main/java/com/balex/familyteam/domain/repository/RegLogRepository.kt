package com.balex.familyteam.domain.repository

import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RegLogRepository {

    fun getAdmin(): StateFlow<Admin>

    fun getUser(): StateFlow<User>

    fun registerAdmin(email: String = "", phone: String = "", password: String)

    fun loginAdmin(email: String = "", phone: String = "", password: String)

    fun loginUser(email: String, password: String)

}