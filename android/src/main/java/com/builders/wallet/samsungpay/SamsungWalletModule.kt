package com.builders.wallet.samsungpay

import android.util.Log
import com.builders.wallet.BuildConfig
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Callback
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = SamsungWalletModule.NAME)
class SamsungWalletModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  // Lê configuração de mock do BuildConfig (variável de ambiente do Gradle)
  private val useMock: Boolean by lazy {
    try {
      
      val mockValue = BuildConfig.SAMSUNG_WALLET_USE_MOCK
      Log.d(TAG, "🔧 [MODULE] SAMSUNG_WALLET_USE_MOCK = $mockValue")
      mockValue
    } catch (e: Exception) {
      Log.w(TAG, "🔧 [MODULE] SAMSUNG_WALLET_USE_MOCK não definido, usando padrão: false")
      false
    }
  }

  private val samsungWalletImplementation: SamsungWalletContract by lazy {
    if (useMock) {
      Log.d(TAG, "🔧 [MODULE] Usando implementação MOCK")
      SamsungWalletMock(reactContext)
    } else {
      Log.d(TAG, "🔧 [MODULE] Usando implementação REAL")
      SamsungWalletImplementation(reactContext)
    }
  }

  @ReactMethod
  fun init(serviceId: String, promise: Promise) {
    samsungWalletImplementation.init(serviceId, promise)
  }

  @ReactMethod
  fun getSamsungPayStatus(promise: Promise) {
    samsungWalletImplementation.getSamsungPayStatus(promise)
  }

  @ReactMethod
  fun goToUpdatePage() {
    samsungWalletImplementation.goToUpdatePage()
  }

  @ReactMethod
  fun activateSamsungPay() {
    samsungWalletImplementation.activateSamsungPay()
  }

  @ReactMethod
  fun getAllCards(promise: Promise) {
    samsungWalletImplementation.getAllCards(promise)
  }

  @ReactMethod
  fun getWalletInfo(promise: Promise) {
    samsungWalletImplementation.getWalletInfo(promise)
  }

  @ReactMethod
  fun addCard(
    payload: String,
    issuerId: String,
    tokenizationProvider: String,
    cardType: String,
    progress: Callback,
    promise: Promise
  ) {
    samsungWalletImplementation.addCard(payload, issuerId, tokenizationProvider, cardType, progress, promise)
  }

  @ReactMethod
  fun checkWalletAvailability(promise: Promise) {
    samsungWalletImplementation.checkWalletAvailability(promise)
  }

  @ReactMethod
  fun getSecureWalletInfo(promise: Promise) {
    samsungWalletImplementation.getSecureWalletInfo(promise)
  }

  @ReactMethod
  fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
    samsungWalletImplementation.addCardToWallet(cardData, promise)
  }

  @ReactMethod
  fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
    samsungWalletImplementation.getCardStatusBySuffix(lastDigits, promise)
  }

  @ReactMethod
  fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
    samsungWalletImplementation.getCardStatusByIdentifier(identifier, tsp, promise)
  }

  @ReactMethod
  fun createWalletIfNeeded(promise: Promise) {
    samsungWalletImplementation.createWalletIfNeeded(promise)
  }

  override fun getConstants(): MutableMap<String, Any> {
    val constants = samsungWalletImplementation.getConstants().toMutableMap()
    
    // Adicionar informações de configuração
    constants["useMock"] = useMock
    constants["SDK_NAME"] = if (useMock) "SamsungWalletMock" else "SamsungWallet"
    
    return constants
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "SamsungWallet"
    private const val TAG = "SamsungWallet"
  }
}