package com.builders.wallet

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap

class StubWalletModule : WalletModuleInterface {
    
    override fun checkWalletAvailability(promise: Promise) {
        promise.reject("SDK_NOT_AVAILABLE", "Nenhum SDK de wallet disponível")
    }

    override fun getSecureWalletInfo(promise: Promise) {
        promise.reject("SDK_NOT_AVAILABLE", "Nenhum SDK de wallet disponível")
    }

    override fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
        promise.reject("SDK_NOT_AVAILABLE", "Nenhum SDK de wallet disponível")
    }

    override fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
        promise.reject("SDK_NOT_AVAILABLE", "Nenhum SDK de wallet disponível")
    }

    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        promise.reject("SDK_NOT_AVAILABLE", "Nenhum SDK de wallet disponível")
    }

    override fun getConstants(): MutableMap<String, Any> {
        return hashMapOf(
            "SDK_AVAILABLE" to false,
            "SDK_NAME" to "Nenhum SDK disponível"
        )
    }

    override fun getName(): String {
        return "StubWallet"
    }

    override fun isAvailable(): Boolean {
        return false
    }
}