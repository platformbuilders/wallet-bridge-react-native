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
        cardType: String,
        promise: Promise
    )
    fun checkWalletAvailability(promise: Promise)
    fun getConstants(): MutableMap<String, Any>
    fun setIntentListener(promise: Promise)
    fun removeIntentListener(promise: Promise)
    fun setActivationResult(status: String, activationCode: String?, promise: Promise)
    fun finishActivity(promise: Promise)
    fun openWallet(promise: Promise)
}
