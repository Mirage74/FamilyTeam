package com.balex.common.data.iap

import com.balex.common.extensions.logTextToFirebase

interface BillingServiceListener {

    fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
        logTextToFirebase("BillingServiceListener, onPricesUpdated, iapKeyPrices: $iapKeyPrices")
    }


    fun onPurchaseFailed(purchaseInfo: DataWrappers.PurchaseInfo?, billingResponseCode: Int?) {
        logTextToFirebase("BillingServiceListener, onPurchaseFailed, purchaseInfo:" +
                " $purchaseInfo billingResponseCode: $billingResponseCode")
    }
}