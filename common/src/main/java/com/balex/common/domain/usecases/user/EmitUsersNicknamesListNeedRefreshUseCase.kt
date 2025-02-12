package com.balex.common.domain.usecases.user

import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

@Suppress("unused")
class EmitUsersNicknamesListNeedRefreshUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() = repository.emitUsersNicknamesListNeedRefresh()
}