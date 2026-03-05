package pl.pointblank.geekadventure.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady = _isBillingReady.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products = _products.asStateFlow()

    private var billingClient: BillingClient? = null

    // Definicje ID produktów (muszą zgadzać się z Google Play Console)
    companion object {
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
                // Rekoneksja w razie potrzeby
                startConnection()
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(ENERGY_PACK)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(CRYSTAL_PACK)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_SUB)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = productDetailsList
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val productDetails = _products.value.find { it.productId == productId } ?: return
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                // Dla subskrypcji należy dodać offerToken jeśli jest dostępny
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
            .setProductList(productDetailsParamsList)
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge lub Consume w zależności od typu
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // Tutaj powinieneś powiadomić ViewModel o sukcesie
                        // W prawdziwej aplikacji warto zweryfikować zakup na backendzie
                    }
                }
            }
            
            // Produkty jednorazowe (Energia, Kryształy) należy skonsumować, aby móc je kupić ponownie
            if (purchase.products.contains(ENERGY_PACK) || purchase.products.contains(CRYSTAL_PACK)) {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.consumeAsync(consumeParams) { _, _ -> }
            }
        }
    }
}
