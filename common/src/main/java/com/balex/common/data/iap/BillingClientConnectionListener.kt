package com.balex.common.data.iap

@Suppress("unused")
interface BillingClientConnectionListener {
    fun onConnected(status: Boolean, billingResponseCode: Int)
}