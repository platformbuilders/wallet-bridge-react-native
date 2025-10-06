package com.builders.wallet.samsungpay

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Callback

interface SamsungWalletContract {
    fun init(serviceId: String, promise: Promise)
    fun getSamsungPayStatus(promise: Promise)
    fun goToUpdatePage()
    fun activateSamsungPay()
    fun getAllCards(promise: Promise)
    fun getWalletInfo(promise: Promise)
    fun addCard(
        payload: String,
        issuerId: String,
        tokenizationProvider: String,
        progress: Callback,
        promise: Promise
    )
    fun checkWalletAvailability(promise: Promise)
    fun getSecureWalletInfo(promise: Promise)
    fun addCardToWallet(cardData: ReadableMap, promise: Promise)
    fun getCardStatusBySuffix(lastDigits: String, promise: Promise)
    fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise)
    fun createWalletIfNeeded(promise: Promise)
    fun getConstants(): MutableMap<String, Any>
}
