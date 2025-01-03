package com.balex.common.domain.repository

import android.app.Activity

interface BillingRepository {

    fun initIapConnectorInRepository(activity: Activity)

    fun launchPurchaseFlow(activity: Activity)

    fun purchaseCoins(activity: Activity)
}