package com.balex.common.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.balex.common.domain.repository.BillingRepository
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.user.ExchangeCoinsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val exchangeCoinsUseCase: ExchangeCoinsUseCase,
    context: Context
) : BillingRepository {
    init {
        setupBillingClient(context)
    }

    private lateinit var billingClient: BillingClient

    private fun setupBillingClient(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                CoroutineScope(Dispatchers.IO).launch {
                    handlePurchase(billingResult, purchases)
                }

            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("setupBillingClient", "startConnection billingResult.responseCode == BillingClient.BillingResponseCode.OK ")
                } else {
                    Log.e(
                        "setupBillingClient",
                        "Billing setup failed: ${billingResult.debugMessage}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("setupBillingClient", "onBillingServiceDisconnected")
            }
        })
    }


    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("consumePurchase", "Purchase consumed successfully: $purchaseToken")
            } else {
                Log.e("consumePurchase", "Error consuming purchase: ${billingResult.debugMessage}")
            }
        }
    }

    private suspend fun handlePurchase(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d("handlePurchase", "billingResult: $billingResult")
        Log.d("handlePurchase", "billingResult.responseCode: ${billingResult.responseCode}")
        Log.d("handlePurchase", "purchases: $purchases")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                Log.d("handlePurchase", "Purchase: ${purchase.products}, State: ${purchase.purchaseState}")
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    consumePurchase(purchase)
                    addCreditsToUser(purchase.quantity)
                    acknowledgePurchase(purchase)
                } else {
                    Log.e("handlePurchase", "Error: ${billingResult.debugMessage}")
                }
            }
        } else {
            Log.e("handlePurchase", "BillingResult not OK: ${billingResult.debugMessage}")
        }
    }

    private suspend fun addCreditsToUser(credits: Int) {
        val user = getUserUseCase()
        exchangeCoinsUseCase(user.teamCoins + credits, user.availableTasksToAdd, user.availableFCM)
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("acknowledgePurchase", "billingResult.responseCode == OK")
            } else {
                Log.d("acknowledgePurchase", "billingResult.responseCode not OK: ${billingResult.responseCode}")
                Log.e("acknowledgePurchase", "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }


    override fun launchPurchaseFlow(activity: Activity) {
        val productDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("team_coin")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

//        val productDetailsParams = QueryProductDetailsParams.newBuilder()
//            .setProductList(
//                listOf(
//                    QueryProductDetailsParams.Product.newBuilder()
//                        .setProductId("android.test.purchased")
//                        .setProductType(BillingClient.ProductType.INAPP)
//                        .build()
//                )
//            )
//            .build()

        if (!billingClient.isReady) {
            Log.e("launchPurchaseFlow", "BillingClient not ready")
        }

        billingClient.queryProductDetailsAsync(productDetailsParams) { billingResult, productDetailsList ->
            Log.d("launchPurchaseFlow", "billingResult: $billingResult")
            Log.d("launchPurchaseFlow", "billingResult.responseCode: ${billingResult.responseCode}")
            Log.d("launchPurchaseFlow", "productDetailsList: $productDetailsList")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList.firstOrNull()
                if (productDetails != null) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .build()
                            )
                        )
                        .build()

                    val result = billingClient.launchBillingFlow(activity, flowParams)
                    Log.d("launchPurchaseFlow", "Launch result: ${result.responseCode}")
                }
            } else {
                Log.e("launchPurchaseFlow", "Error: ${billingResult.debugMessage}")
            }
        }
    }

    companion object {
        enum class PremiumStatus {
            NO_PREMIUM,
            ONE_MONTH,
            ONE_YEAR,
            UNLIMITED
        }
    }
}