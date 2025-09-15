package com.builders.wallet.googletapandpay

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap

interface GoogleWalletContract {
    fun checkWalletAvailability(promise: Promise)
    fun getSecureWalletInfo(promise: Promise)
    fun getTokenStatus(tokenServiceProvider: Int, tokenReferenceId: String, promise: Promise)
    fun getEnvironment(promise: Promise)
    fun isTokenized(
        fpanLastFour: String,
        cardNetwork: Int,
        tokenServiceProvider: Int,
        promise: Promise
    )
    fun viewToken(
        tokenServiceProvider: Int,
        issuerTokenId: String,
        promise: Promise
    )
    fun addCardToWallet(cardData: ReadableMap, promise: Promise)
    fun createWalletIfNeeded(promise: Promise)
    fun listTokens(promise: Promise)
    fun getConstants(): MutableMap<String, Any>
    fun setIntentListener(promise: Promise)
    fun removeIntentListener(promise: Promise)
}
