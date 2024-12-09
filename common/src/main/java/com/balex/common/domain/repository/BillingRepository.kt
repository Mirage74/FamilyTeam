package com.balex.common.domain.repository

import android.app.Activity

interface BillingRepository {

    fun launchPurchaseFlow(activity: Activity)
}