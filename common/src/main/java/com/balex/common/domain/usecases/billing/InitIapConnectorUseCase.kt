package com.balex.common.domain.usecases.billing

import android.app.Activity
import com.balex.common.domain.repository.BillingRepository
import javax.inject.Inject

class InitIapConnectorUseCase @Inject constructor(
    private val repository: BillingRepository
) {
    operator fun invoke(activity: Activity) = repository.initIapConnector(activity)
}