package com.balex.common.data.iap

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper

@Suppress("unused")
abstract class IBillingService {

    private val purchaseServiceListeners: MutableList<PurchaseServiceListener> = mutableListOf()
    private val subscriptionServiceListeners: MutableList<SubscriptionServiceListener> = mutableListOf()
    private val billingClientConnectedListeners: MutableList<BillingClientConnectionListener> = mutableListOf()

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.add(purchaseServiceListener)
    }


    /**
     * @param purchaseInfo Product specifier
     * @param isRestore Flag indicating whether it's a fresh purchase or restored product
     */
    fun productOwned(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        findUiHandler().post {
            productOwnedInternal(purchaseInfo, isRestore)
        }
    }

    private fun productOwnedInternal(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        for (purchaseServiceListener in purchaseServiceListeners) {
            if (isRestore) {
                purchaseServiceListener.onProductRestored(purchaseInfo)
            } else {
                purchaseServiceListener.onProductPurchased(purchaseInfo)
            }
        }
    }

    /**
     * @param purchaseInfo Subscription specifier
     * @param isRestore Flag indicating whether it's a fresh purchase or restored subscription
     */
    fun subscriptionOwned(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        findUiHandler().post {
            subscriptionOwnedInternal(purchaseInfo, isRestore)
        }
    }

    private fun subscriptionOwnedInternal(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        for (subscriptionServiceListener in subscriptionServiceListeners) {
            if (isRestore) {
                subscriptionServiceListener.onSubscriptionRestored(purchaseInfo)
            } else {
                subscriptionServiceListener.onSubscriptionPurchased(purchaseInfo)
            }
        }
    }

    fun isBillingClientConnected(status: Boolean, responseCode: Int) {
        findUiHandler().post {
            for (billingServiceListener in billingClientConnectedListeners) {
                billingServiceListener.onConnected(status, responseCode)
            }
        }
    }

    fun updatePrices(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
        findUiHandler().post {
            updatePricesInternal(iapKeyPrices)
        }
    }

    private fun updatePricesInternal(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
        for (billingServiceListener in purchaseServiceListeners) {
            billingServiceListener.onPricesUpdated(iapKeyPrices)
        }
        for (billingServiceListener in subscriptionServiceListeners) {
            billingServiceListener.onPricesUpdated(iapKeyPrices)
        }
    }

    fun updateFailedPurchases(purchaseInfo: List<DataWrappers.PurchaseInfo>? = null, billingResponseCode: Int? = null) {
        purchaseInfo?.forEach {
            updateFailedPurchase(it, billingResponseCode)
        } ?: updateFailedPurchase()
    }

    fun updateFailedPurchase(purchaseInfo: DataWrappers.PurchaseInfo? = null, billingResponseCode: Int? = null) {
        findUiHandler().post {
            updateFailedPurchasesInternal(purchaseInfo, billingResponseCode)
        }
    }

    private fun updateFailedPurchasesInternal(purchaseInfo: DataWrappers.PurchaseInfo? = null, billingResponseCode: Int? = null) {
        for (billingServiceListener in purchaseServiceListeners) {
            billingServiceListener.onPurchaseFailed(purchaseInfo, billingResponseCode)
        }
        for (billingServiceListener in subscriptionServiceListeners) {
            billingServiceListener.onPurchaseFailed(purchaseInfo, billingResponseCode)
        }
    }

    abstract fun init(key: String?)
    abstract fun buy(activity: Activity, sku: String, obfuscatedAccountId: String?, obfuscatedProfileId: String?)
    abstract fun subscribe(activity: Activity, sku: String, obfuscatedAccountId: String?, obfuscatedProfileId: String?)
    abstract fun unsubscribe(activity: Activity, sku: String)
    abstract fun enableDebugLogging(enable: Boolean)

    @CallSuper
    open fun close() {
        subscriptionServiceListeners.clear()
        purchaseServiceListeners.clear()
        billingClientConnectedListeners.clear()
    }

//    fun addBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
//        billingClientConnectedListeners.add(billingClientConnectionListener)
//    }
//
//    fun removeBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
//        billingClientConnectedListeners.remove(billingClientConnectionListener)
//    }
//
//    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
//        purchaseServiceListeners.remove(purchaseServiceListener)
//    }
//
//    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
//        subscriptionServiceListeners.add(subscriptionServiceListener)
//    }
//
//    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
//        subscriptionServiceListeners.remove(subscriptionServiceListener)
//    }

}

fun findUiHandler(): Handler {
    return Handler(Looper.getMainLooper())
}