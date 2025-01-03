package com.balex.common.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.balex.common.BuildConfig
import com.balex.common.data.iap.DataWrappers
import com.balex.common.data.iap.IapConnector
import com.balex.common.data.iap.PurchaseServiceListener
import com.balex.common.domain.repository.BillingRepository
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.user.ExchangeCoinsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val exchangeCoinsUseCase: ExchangeCoinsUseCase,
    private val context: Context
) : BillingRepository {


    private val licenceKey = BuildConfig.LICENCE_KEY

    private lateinit var iapConnector: IapConnector

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)


    private suspend fun addCreditsToUser(credits: Int) {
        val user = getUserUseCase()
        exchangeCoinsUseCase(user.teamCoins + credits, user.availableTasksToAdd, user.availableFCM)
    }

    override fun initIapConnectorInRepository(activity: Activity) {
        Log.d("initIapConnectorInRepository", "initIapConnectorInRepository")
        iapConnector = IapConnector(
            context = activity,
            consumableKeys = listOf(TEAM_COIN),
            key = licenceKey,
            enableLogging = true
        )
    }

    override fun purchaseCoins(activity: Activity) {
        iapConnector.purchase(activity, TEAM_COIN)
    }

    private fun logAppVersion(context: Context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            Log.d("AppVersion", "Current version: $versionName")
        } catch (e: Exception) {
            Log.e("AppVersion", "Failed to get version name", e)
        }
    }

    override fun launchPurchaseFlow(activity: Activity) {

        logAppVersion(context)

        Log.d("launchPurchaseFlow", "Initializing IAP Connector")

        iapConnector.addPurchaseListener(object : PurchaseServiceListener {

            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                Log.d("launchPurchaseFlow", "onProductPurchased, purchaseInfo: $purchaseInfo")
                val jsonObject = JSONObject(purchaseInfo.originalJson)
                val quantity: Int
                try {
                    quantity = jsonObject.getInt("quantity")
                    coroutineScope.launch {
                        addCreditsToUser(quantity)
                    }
                } catch (e: Exception) {
                    Log.d("launchPurchaseFlow", "Wrong quantity in jsonObject: $jsonObject")
                    e.printStackTrace()
                }
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
                Log.d("launchPurchaseFlow", "onProductPurchased, onPricesUpdated: $iapKeyPrices")
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                Log.d("launchPurchaseFlow", "onProductRestored, purchaseInfo: $purchaseInfo")
            }

            override fun onPurchaseFailed(
                purchaseInfo: DataWrappers.PurchaseInfo?,
                billingResponseCode: Int?
            ) {
                Log.d(
                    "launchPurchaseFlow",
                    "onPurchaseFailed, billingResponseCode: $billingResponseCode"
                )
            }
        })
        //iapConnector.purchase(activity, TEAM_COIN)
    }

    companion object {
        enum class PremiumStatus {
            NO_PREMIUM,
            ONE_MONTH,
            ONE_YEAR,
            UNLIMITED
        }

        const val TEAM_COIN = "team_coin"
    }
}