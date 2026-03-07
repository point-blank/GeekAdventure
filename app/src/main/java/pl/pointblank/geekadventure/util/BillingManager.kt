package pl.pointblank.geekadventure.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(private val context: Context, private val onPurchaseSuccess: (String) -> Unit) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady = _isBillingReady.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products = _products.asStateFlow()

    private var billingClient: BillingClient? = null

    companion object {
        const val TAG = "BillingManager"
        const val ENERGY_PACK = "energy_pack_20"
        const val CRYSTAL_PACK = "crystals_pack_5"
        const val PREMIUM_SUB = "geek_master_subscription"
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()

        startConnection()
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isBillingReady.value = true
                    queryProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                startConnection()
            }
        })
    }

    fun queryPurchases() {
        val client = billingClient ?: return
        if (!client.isReady) return

        // 1. Sprawdzamy subskrypcje (Geek Master)
        val subParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(subParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }

        // 2. Sprawdzamy produkty jednorazowe (jeśli jakieś nie zostały skonsumowane)
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    private fun queryProducts() {
        scope.launch {
            val allFetchedProducts = mutableListOf<ProductDetails>()

            // 1. Zapytanie o produkty INAPP (Energia, Kryształy)
            val inAppProductList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(ENERGY_PACK)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(CRYSTAL_PACK)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            val inAppParams = QueryProductDetailsParams.newBuilder()
                .setProductList(inAppProductList)
                .build()

            val inAppResult = billingClient?.queryProductDetails(inAppParams)
            if (inAppResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                inAppResult.productDetailsList?.let { allFetchedProducts.addAll(it) }
            }

            // 2. Zapytanie o SUBSKRYPCJE (Geek Master)
            val subProductList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PREMIUM_SUB)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            val subParams = QueryProductDetailsParams.newBuilder()
                .setProductList(subProductList)
                .build()

            val subResult = billingClient?.queryProductDetails(subParams)
            if (subResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                subResult.productDetailsList?.let { allFetchedProducts.addAll(it) }
            }

            Log.d(TAG, "Łącznie pobrano produktów: ${allFetchedProducts.size}")
            _products.value = allFetchedProducts
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val productDetails = _products.value.find { it.productId == productId } ?: return
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .apply {
                    if (productDetails.productType == BillingClient.ProductType.SUBS) {
                        productDetails.subscriptionOfferDetails?.firstOrNull()?.let {
                            setOfferToken(it.offerToken)
                        }
                    }
                }
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        scope.launch {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(acknowledgeParams)
                    purchase.products.forEach { productId ->
                        onPurchaseSuccess(productId)
                    }
                }
                
                if (purchase.products.contains(ENERGY_PACK) || purchase.products.contains(CRYSTAL_PACK)) {
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.consumePurchase(consumeParams)
                    purchase.products.forEach { productId ->
                        onPurchaseSuccess(productId)
                    }
                }
            }
        }
    }
}
