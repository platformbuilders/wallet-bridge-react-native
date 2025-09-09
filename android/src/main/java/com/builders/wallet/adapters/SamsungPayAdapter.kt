package com.builders.wallet.adapters

import android.util.Log
import com.builders.wallet.samsungpay.SamsungPayModule
import com.builders.wallet.WalletModuleInterface
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap

class SamsungPayAdapter(
    private val reactContext: ReactApplicationContext
) : WalletModuleInterface {

    private val samsungPayModule = SamsungPayModule(reactContext)
    private val isSDKAvailable: Boolean by lazy {
        try {
            Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk")
            Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk\$Companion")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    override fun checkWalletAvailability(promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
            return
        }
        
        // Check Samsung Pay status
        samsungPayModule.getSamsungPayStatus(promise)
    }

    override fun getSecureWalletInfo(promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
            return
        }
        
        // Get wallet information for secure transactions
        samsungPayModule.getWalletInfo(promise)
    }

    override fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
            return
        }
        
        // Get all cards and check by last digits
        samsungPayModule.getAllCards(promise)
    }

    override fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
            return
        }
        
        // Samsung Pay doesn't have direct identifier lookup, use getAllCards
        samsungPayModule.getAllCards(promise)
    }

    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
            return
        }
        
        // Extract data from cardData map
        val payload = cardData.getString("opaquePaymentCard") ?: ""
        val issuerId = cardData.getString("issuerId") ?: ""
        val tokenizationProvider = cardData.getString("tokenizationProvider") ?: ""
        
        // Add card using Samsung Pay
        samsungPayModule.addCard(
            payload,
            issuerId,
            tokenizationProvider,
            { args ->
                val current = args[0] as Int
                val total = args[1] as Int
                Log.d(TAG, "Add card progress: $current/$total")
            },
            promise
        )
    }

    override fun getConstants(): MutableMap<String, Any> {
        return hashMapOf(
            "SDK_AVAILABLE" to isSDKAvailable,
            "SDK_NAME" to "SamsungPay",
            "MODULE_NAME" to getName()
        )
    }

    override fun getName(): String {
        return "SamsungPay"
    }

    override fun isAvailable(): Boolean {
        return isSDKAvailable
    }

    companion object {
        private const val TAG = "SamsungPayAdapter"
    }
}