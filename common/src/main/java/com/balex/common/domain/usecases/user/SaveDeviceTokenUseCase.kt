package com.balex.common.domain.usecases.user

import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

@Suppress("unused")
class SaveDeviceTokenUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(token: String) = repository.saveDeviceToken(token)
}