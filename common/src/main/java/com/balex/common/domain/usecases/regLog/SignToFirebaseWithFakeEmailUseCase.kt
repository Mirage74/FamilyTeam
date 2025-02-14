package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

@Suppress("unused")
class SignToFirebaseWithFakeEmailUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(userToSignIn: User, userNameTrySignIn: String) =
        repository.signToFirebaseWithFakeEmail(userToSignIn, userNameTrySignIn)
}