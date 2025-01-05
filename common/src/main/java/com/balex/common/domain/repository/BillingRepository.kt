package com.balex.common.domain.repository

import android.app.Activity

interface BillingRepository {

    fun initIapConnector(activity: Activity)

    fun purchaseCoins(activity: Activity)
}