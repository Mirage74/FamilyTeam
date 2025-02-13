package com.balex.common.data.iap

import android.app.Activity
import android.content.Context

/**
 * Initialize billing service.
 *
 * @param context Application context.
 * @param nonConsumableKeys SKU list for non-consumable one-time products.
 * @param consumableKeys SKU list for consumable one-time products.
 * @param subscriptionKeys SKU list for subscriptions.
 * @param key Key to verify purchase messages. Leave it empty if you want to skip verification.
 * @param enableLogging Log operations/errors to the logcat for debugging purposes.
 */

@Suppress("unused")
class IapConnector @JvmOverloads constructor(
    context: Context,
    nonConsumableKeys: List<String> = emptyList(),
    consumableKeys: List<String> = emptyList(),
    subscriptionKeys: List<String> = emptyList(),
    key: String? = null,
    enableLogging: Boolean = false
) {

    private var mBillingService: IBillingService? = null

    init {
        val contextLocal = context.applicationContext ?: context
        mBillingService = BillingService(contextLocal, nonConsumableKeys, consumableKeys, subscriptionKeys)
        getBillingService().init(key)
        getBillingService().enableDebugLogging(enableLogging)
    }

    private fun getBillingService(): IBillingService {
        return mBillingService ?: let {
            throw RuntimeException("Call IapConnector to initialize billing service")
        }
    }

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().addPurchaseListener(purchaseServiceListener)
    }

    fun purchase(activity: Activity, sku: String, obfuscatedAccountId: String? = null, obfuscatedProfileId: String? = null) {
        getBillingService().buy(activity, sku, obfuscatedAccountId, obfuscatedProfileId)
    }

//    fun addBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
//        getBillingService().addBillingClientConnectionListener(billingClientConnectionListener)
//    }
//
//    fun removeBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
//        getBillingService().removeBillingClientConnectionListener(billingClientConnectionListener)
//    }
//
//    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
//        getBillingService().removePurchaseListener(purchaseServiceListener)
//    }
//
//    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
//        getBillingService().addSubscriptionListener(subscriptionServiceListener)
//    }
//
//    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
//        getBillingService().removeSubscriptionListener(subscriptionServiceListener)
//    }
//
//    fun subscribe(activity: Activity, sku: String, obfuscatedAccountId: String? = null, obfuscatedProfileId: String? = null) {
//        getBillingService().subscribe(activity, sku, obfuscatedAccountId, obfuscatedProfileId)
//    }
//
//    fun unsubscribe(activity: Activity, sku: String) {
//        getBillingService().unsubscribe(activity, sku)
//    }
//
//    fun destroy() {
//        getBillingService().close()
//    }

}
