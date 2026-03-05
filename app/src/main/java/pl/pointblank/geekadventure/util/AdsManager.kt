package pl.pointblank.geekadventure.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdsManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded = _isAdLoaded.asStateFlow()

    // TEST ID dla Reklamy z nagrodą (Rewarded Ad)
    private val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    init {
        MobileAds.initialize(context)
        loadRewardedAd()
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                _isAdLoaded.value = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                _isAdLoaded.value = true
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: (Int) -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                // Użytkownik obejrzał reklamę do końca
                onRewardEarned(rewardItem.amount)
                loadRewardedAd() // Załaduj kolejną w tle
            }
        } ?: run {
            // Reklama niegotowa, spróbuj załadować
            loadRewardedAd()
        }
    }
}
