package com.balex.common.domain.usecases.user

import com.balex.common.data.repository.BillingRepositoryImpl
import com.balex.common.domain.repository.UserRepository
import javax.inject.Inject

@Suppress("unused")
class SetPremiumStatusUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(premiumStatusUseCase: BillingRepositoryImpl.Companion.PremiumStatus) =
        repository.setPremiumStatus(premiumStatusUseCase)
}