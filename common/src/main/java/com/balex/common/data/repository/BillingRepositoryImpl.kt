package com.balex.common.data.repository

import android.app.Activity
import com.balex.common.BuildConfig
import com.balex.common.data.iap.DataWrappers
import com.balex.common.data.iap.IapConnector
import com.balex.common.data.iap.PurchaseServiceListener
import com.balex.common.domain.repository.BillingRepository
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.user.ExchangeCoinsUseCase
import com.balex.common.extensions.isNotEmptyNickName
import com.balex.common.extensions.logExceptionToFirebase
import com.balex.common.extensions.logTextToFirebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val exchangeCoinsUseCase: ExchangeCoinsUseCase
) : BillingRepository {


    private val licenceKey = BuildConfig.LICENCE_KEY

    private lateinit var iapConnector: IapConnector

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)


    private suspend fun addCreditsToUser(credits: Int) {
        val user = getUserUseCase()
        if (user.isNotEmptyNickName()) {
            exchangeCoinsUseCase(
                user.teamCoins + credits,
                user.availableTasksToAdd,
                user.availableFCM
            )
        }

    }


    override fun purchaseCoins(activity: Activity) {
        iapConnector.purchase(activity, TEAM_COIN)
    }

//    private fun logAppVersion(context: Context) {
//        try {
//            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
//            val versionName = packageInfo.versionName
//            Log.d("AppVersion", "Current version: $versionName")
//        } catch (e: Exception) {
//            Log.i("AppVersion", "Failed to get version name", e)
//        }
//    }

    override fun initIapConnector(activity: Activity) {

        //logAppVersion(context)

        iapConnector = IapConnector(
            context = activity,
            consumableKeys = listOf(TEAM_COIN),
            key = licenceKey,
            enableLogging = true
        )

        iapConnector.addPurchaseListener(object : PurchaseServiceListener {

            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                val jsonObject = JSONObject(purchaseInfo.originalJson)
                val quantity: Int
                try {
                    quantity = jsonObject.getInt("quantity")
                    coroutineScope.launch {
                        addCreditsToUser(quantity)
                    }
                } catch (e: Exception) {
                    logExceptionToFirebase(
                        "launchPurchaseFlow, Wrong quantity in jsonObject:",
                        "$jsonObject."
                    )
                    e.printStackTrace()
                }
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
                logTextToFirebase("launchPurchaseFlow, onPricesUpdated: $iapKeyPrices")
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                logTextToFirebase("launchPurchaseFlow, onProductRestored, purchaseInfo: $purchaseInfo")
            }

            override fun onPurchaseFailed(
                purchaseInfo: DataWrappers.PurchaseInfo?,
                billingResponseCode: Int?
            ) {
                logTextToFirebase("launchPurchaseFlow, onPurchaseFailed, billingResponseCode: $billingResponseCode")
            }
        })
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