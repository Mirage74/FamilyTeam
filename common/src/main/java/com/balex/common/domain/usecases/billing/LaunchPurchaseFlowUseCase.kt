package com.balex.common.domain.usecases.billing

import android.app.Activity
import com.balex.common.domain.repository.BillingRepository
import javax.inject.Inject

class LaunchPurchaseFlowUseCase @Inject constructor(
    private val repository: BillingRepository
) {
    operator fun invoke(activity: Activity) = repository.launchPurchaseFlow(activity)
}