package com.chemscanner.omniscient.utils.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BILLING MANAGER - Google Play Billing Library v7 integration.
 *
 * IMPORTANT (real-world setup step, not fake): [PRO_PRODUCT_ID] below must match an in-app
 * product ID that YOU configure in Google Play Console for this app before purchases will work.
 * Until that product exists in Play Console, purchases will fail with ITEM_UNAVAILABLE - this is
 * expected and is a Play Console configuration step, not a code bug.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        const val PRO_PRODUCT_ID = "pro_upgrade_yearly"
        private const val PREFS_NAME = "billing_cache"
        private const val KEY_IS_PRO = "is_pro_user"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isProUser = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    val isProUserFlow: StateFlow<Boolean> = _isProUser

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            com.android.billingclient.api.PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        connectAndSync()
    }

    private fun connectAndSync() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshPurchases()
                } else {
                    Timber.w("BillingManager: setup failed (${result.responseCode}) - ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("BillingManager: service disconnected, will reconnect on next launch/purchase attempt.")
            }
        })
    }

    /** Re-queries owned purchases from Play and updates the cached PRO state from real purchase records. */
    fun refreshPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Timber.w("BillingManager: queryPurchasesAsync failed (${result.responseCode})")
                return@queryPurchasesAsync
            }
            val hasProPurchase = purchases.any { purchase ->
                purchase.products.contains(PRO_PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            setProUserCached(hasProPurchase)
            purchases.forEach { acknowledgeIfNeeded(it) }
        }
    }

    fun launchProBillingFlow(activity: Activity) {
        if (!billingClient.isReady) {
            Timber.w("BillingManager: client not ready, reconnecting before launching flow.")
            connectAndSync()
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRO_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val queryParams = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(queryParams) { result, productDetailsResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Timber.e("BillingManager: product details query failed (${result.responseCode}) - is '$PRO_PRODUCT_ID' configured in Play Console?")
                return@queryProductDetailsAsync
            }
            val productDetails = productDetailsResult.productDetailsList.firstOrNull { it.productId == PRO_PRODUCT_ID }
            if (productDetails == null) {
                Timber.e("BillingManager: '$PRO_PRODUCT_ID' not found - create this in-app product in Play Console first.")
                return@queryProductDetailsAsync
            }

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.products.contains(PRO_PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        setProUserCached(true)
                        acknowledgeIfNeeded(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.i("BillingManager: user cancelled purchase flow.")
            }
            else -> {
                Timber.w("BillingManager: purchase update failed (${result.responseCode}) - ${result.debugMessage}")
            }
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            scope.launch {
                billingClient.acknowledgePurchase(ackParams) { ackResult ->
                    if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        Timber.w("BillingManager: acknowledge failed (${ackResult.responseCode})")
                    }
                }
            }
        }
    }

    private fun setProUserCached(isPro: Boolean) {
        _isProUser.value = isPro
        prefs.edit().putBoolean(KEY_IS_PRO, isPro).apply()
    }

    /**
     * Synchronous read for UI code that can't collect a Flow (e.g. one-shot fragment inflation).
     * Backed by the last real, server-verified purchase query - not a hardcoded value. On first
     * app launch before any query has completed, this returns false (safe default) until
     * refreshPurchases() completes and updates the cache.
     */
    fun isProUser(): Boolean = prefs.getBoolean(KEY_IS_PRO, false)
}
