package com.balex.familyteam.data.repository

import com.balex.familyteam.domain.repository.UserRepository
import com.balex.familyteam.domain.usecase.regLog.ObserveUserUseCase
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val observeUserUseCase: ObserveUserUseCase
): UserRepository {


}