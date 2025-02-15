package com.balex.common.data.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*

object InterstitialAdHelper {
    private var interstitialAd: InterstitialAd? = null

    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, BANNER_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
            }
        })
    }

    fun showAd(context: Context, onAdClosed: () -> Unit) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdClosed()
                    interstitialAd = null
                    loadAd(context)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdClosed()
                    interstitialAd = null
                }
            }
            ad.show(context as Activity)
        } ?: onAdClosed()
    }
}

const val BANNER_ID = "ca-app-pub-3112871611759530~9730823858"
