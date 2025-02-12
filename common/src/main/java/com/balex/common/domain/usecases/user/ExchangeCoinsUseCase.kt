package com.balex.common.domain.usecases.user

import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

@Suppress("unused")
class ExchangeCoinsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(coins: Int, tasks: Int, reminders: Int) = repository.exchangeCoins(coins, tasks, reminders)
}