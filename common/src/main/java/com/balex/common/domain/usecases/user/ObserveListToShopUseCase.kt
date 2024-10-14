package com.balex.common.domain.usecases.user

import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

class ObserveListToShopUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke() = repository.observeListToShop()
}