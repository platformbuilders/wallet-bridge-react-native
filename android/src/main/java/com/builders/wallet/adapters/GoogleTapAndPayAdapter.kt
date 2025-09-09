package com.builders.wallet.adapters

import android.os.Build
import android.util.Log
import com.builders.wallet.googletapandpay.GoogleTapAndPayModule
import com.builders.wallet.WalletModuleInterface
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap

class GoogleTapAndPayAdapter(
    private val reactContext: ReactApplicationContext
) : WalletModuleInterface {

    private val googleTapAndPayModule = GoogleTapAndPayModule(reactContext)
    private val isSDKAvailable: Boolean by lazy {
        try {
            Class.forName("com.google.android.gms.tapandpay.TapAndPay")
            Class.forName("com.google.android.gms.tapandpay.TapAndPayClient")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    override fun checkWalletAvailability(promise: Promise) {
        // Verificar se é Android
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            promise.reject("PLATFORM_NOT_SUPPORTED", "Google Pay só é suportado em Android")
            return
        }
        
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
            return
        }
        
        // Para Google Pay, apenas verificar se é Android e SDK disponível
        // A verificação de NFC será feita quando necessário
        promise.resolve(true)
    }

    override fun getSecureWalletInfo(promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
            return
        }
        
        // Get wallet information for secure transactions
        googleTapAndPayModule.getSecureWalletInfo(promise)
    }

    override fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
            return
        }
        
        // Check if card is tokenized by last 4 digits
        googleTapAndPayModule.isTokenized(
            lastDigits, 
            1, // CARD_NETWORK_VISA (example)
            1, // TOKEN_PROVIDER_VISA (example)
            promise
        )
    }

    override fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
            return
        }
        
        // Get token status by identifier
        googleTapAndPayModule.getTokenStatus(
            tsp.toIntOrNull() ?: 1,
            identifier,
            promise
        )
    }

    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
            return
        }
        
        // Add card using push tokenization
        googleTapAndPayModule.pushTokenize(cardData, promise)
    }

    override fun createWalletIfNeeded(promise: Promise) {
        if (!isSDKAvailable) {
            promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
            return
        }
        
        // Create wallet if it doesn't exist
        googleTapAndPayModule.createWalletIfNeeded(promise)
    }

    override fun getConstants(): MutableMap<String, Any> {
        return if (isSDKAvailable) {
            googleTapAndPayModule.getConstants()
        } else {
            hashMapOf(
                "SDK_AVAILABLE" to false,
                "SDK_NAME" to "GoogleTapAndPay"
            )
        }
    }

    override fun getName(): String {
        return "GoogleTapAndPay"
    }

    override fun isAvailable(): Boolean {
        return isSDKAvailable
    }

    companion object {
        private const val TAG = "GoogleTapAndPayAdapter"
    }
}