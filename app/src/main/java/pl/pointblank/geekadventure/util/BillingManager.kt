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

class BillingManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
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
        Log.d(TAG, "Zakupy zaktualizowane: ${billingResult.responseCode}")
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
        Log.d(TAG, "Łączenie z Google Play Billing...")
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Połączono pomyślnie!")
                    _isBillingReady.value = true
                    queryProducts()
                } else {
                    Log.e(TAG, "Błąd połączenia: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Rozłączono z usługą Billing. Próba ponownego połączenia...")
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
                Log.d(TAG, "Pobrano produkty: ${productDetailsList.size}")
                productDetailsList.forEach { 
                    Log.d(TAG, "Produkt: ${it.productId} - ${it.name}")
                }
                _products.value = productDetailsList
            } else {
                Log.e(TAG, "Błąd pobierania produktów: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val productDetails = _products.value.find { it.productId == productId }
        if (productDetails == null) {
            Log.e(TAG, "Nie znaleziono szczegółów produktu dla: $productId. Czy jest dodany w Play Console?")
            return
        }
        
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

        Log.d(TAG, "Uruchamianie natywnego okna płatności dla: $productId")
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        Log.d(TAG, "Obsługa zakupu: ${purchase.orderId}")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    Log.d(TAG, "Zatwierdzenie zakupu: ${billingResult.responseCode}")
                }
            }
            
            if (purchase.products.contains(ENERGY_PACK) || purchase.products.contains(CRYSTAL_PACK)) {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.consumeAsync(consumeParams) { result, _ ->
                    Log.d(TAG, "Konsumpcja produktu: ${result.responseCode}")
                }
            }
        }
    }
}
