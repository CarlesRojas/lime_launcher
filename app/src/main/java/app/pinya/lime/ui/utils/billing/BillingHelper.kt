package app.pinya.lime.ui.utils.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.QueryProductDetailsParams.Product
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BillingHelper private constructor(
    application: Application,
    private val defaultScope: CoroutineScope,
) : PurchasesUpdatedListener, BillingClientStateListener {

    enum class ProPurchaseState {
        NOT_PURCHASED, PENDING, PURCHASED, PURCHASED_AND_ACKNOWLEDGED
    }

    private val billingClient: BillingClient

    private val product: Product = Product.newBuilder()
        .setProductId("lime_launcher_pro")
        .setProductType(ProductType.INAPP)
        .build()

    val purchaseState: MutableLiveData<ProPurchaseState> =
        MutableLiveData(ProPurchaseState.NOT_PURCHASED)

    private var productDetails: MutableLiveData<ProductDetails?> = MutableLiveData(null)

    private var billingServiceConnected = false

    init {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient = BillingClient.newBuilder(application)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        connectToBillingService()
    }

    fun isPro(): Boolean {
        return purchaseState.value == ProPurchaseState.PURCHASED_AND_ACKNOWLEDGED
    }

    private fun connectToBillingService() {
        billingClient.startConnection(this)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {

        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) handlePurchase(purchases[0])
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                billingServiceConnected = true

                defaultScope.launch {
                    getPurchaseProductDetails()
                    getPurchaseState()
                }
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        billingServiceConnected = false
    }

    private suspend fun getPurchaseProductDetails() {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder().setProductList(listOf(product))

        val (billingResult, detailsList) = suspendCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(queryProductDetailsParams.build()) { result, details ->
                continuation.resume(result to details.productDetailsList)
            }
        }

        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                if (detailsList.isNotEmpty()) productDetails.postValue(detailsList[0])
            }
        }
    }

    private suspend fun getPurchaseState() {
        val queryPurchasesParams =
            QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP)

        val (billingResult, purchasesList) = suspendCoroutine { continuation ->
            billingClient.queryPurchasesAsync(queryPurchasesParams.build()) { result, purchases ->
                continuation.resume(result to purchases)
            }
        }

        when (billingResult.responseCode) {
            BillingResponseCode.OK, BillingResponseCode.ITEM_ALREADY_OWNED -> {
                if (purchasesList.isNotEmpty()) handlePurchase(purchasesList[0])
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            PurchaseState.PENDING -> purchaseState.postValue(ProPurchaseState.PENDING)
            PurchaseState.UNSPECIFIED_STATE -> purchaseState.postValue(ProPurchaseState.NOT_PURCHASED)
            PurchaseState.PURCHASED -> purchaseState.postValue(if (purchase.isAcknowledged) ProPurchaseState.PURCHASED_AND_ACKNOWLEDGED else ProPurchaseState.PURCHASED)
        }

        when (purchase.purchaseState) {
            PurchaseState.PURCHASED -> {
                if (!BillingSecurity.verifyPurchase(purchase.originalJson, purchase.signature))
                    return

                if (!purchase.isAcknowledged) {
                    defaultScope.launch {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)

                        val acknowledgePurchaseResult = suspendCoroutine { continuation ->
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) { result ->
                                continuation.resume(result)
                            }
                        }

                        when (acknowledgePurchaseResult.responseCode) {
                            BillingResponseCode.OK -> purchaseState.postValue(ProPurchaseState.PURCHASED_AND_ACKNOWLEDGED)
                        }
                    }
                }
            }
        }
    }

    fun startBillingFlow(activity: Activity): Boolean {
        if (!billingServiceConnected) {
            connectToBillingService()
            return false
        }

        val currentProductDetails = productDetails.value ?: return false

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(currentProductDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setIsOfferPersonalized(true)
            .build()


        billingClient.launchBillingFlow(activity, billingFlowParams)
        return true
    }

    companion object {
        @Volatile
        private var instance: BillingHelper? = null

        @JvmStatic
        fun getInstance(application: Application, defaultScope: CoroutineScope) =
            instance ?: synchronized(this) {
                instance ?: BillingHelper(application, defaultScope).also { instance = it }
            }
    }
}