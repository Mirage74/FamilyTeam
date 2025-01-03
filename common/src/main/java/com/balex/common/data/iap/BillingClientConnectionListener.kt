package com.balex.common.data.iap

interface BillingClientConnectionListener {
    fun onConnected(status: Boolean, billingResponseCode: Int)
}