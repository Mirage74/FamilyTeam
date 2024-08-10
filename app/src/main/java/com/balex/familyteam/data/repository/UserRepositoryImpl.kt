package com.balex.familyteam.data.repository

import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.UserRepository
import com.balex.familyteam.domain.usecase.regLog.ObserveUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val observeUserUseCase: ObserveUserUseCase
): UserRepository {

    init {
        CoroutineScope(Dispatchers.Default).launch {
            observeUserUseCase().collect {
                user = it
            }
        }
    }
    private var user = User()

    override fun getUser(): User {
        return user
    }
}