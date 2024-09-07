package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class SignInWithEmailAndPasswordUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(user: User) = repository.signToFirebaseWithEmailAndPassword(user)
}