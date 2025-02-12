package com.balex.common.data.iap

import android.util.Log

interface BillingServiceListener {

    fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
        Log.d("BillingServiceListener", "onPricesUpdated, iapKeyPrices: $iapKeyPrices")
    }


    fun onPurchaseFailed(purchaseInfo: DataWrappers.PurchaseInfo?, billingResponseCode: Int?) {
        Log.d("BillingServiceListener", "onPurchaseFailed, purchaseInfo: $purchaseInfo billingResponseCode: $billingResponseCode")
    }
}