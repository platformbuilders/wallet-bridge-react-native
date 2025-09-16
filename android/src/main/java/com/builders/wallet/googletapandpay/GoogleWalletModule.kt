package com.builders.wallet.googletapandpay

import android.util.Log
import com.builders.wallet.BuildConfig
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = GoogleWalletModule.NAME)
class GoogleWalletModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  // Lê configuração de mock do BuildConfig (variável de ambiente do Gradle)
  private val useMock: Boolean by lazy {
    try {
      val mockValue = BuildConfig.GOOGLE_WALLET_USE_MOCK
      Log.d(TAG, "🔧 [MODULE] GOOGLE_WALLET_USE_MOCK = $mockValue")
      mockValue
    } catch (e: Exception) {
      Log.w(TAG, "🔧 [MODULE] GOOGLE_WALLET_USE_MOCK não definido, usando padrão: false")
      false
    }
  }

  private val googleWalletImplementation: GoogleWalletContract by lazy {
    if (useMock) {
      Log.d(TAG, "🔧 [MODULE] Usando implementação MOCK")
      GoogleWalletMock()
    } else {
      Log.d(TAG, "🔧 [MODULE] Usando implementação REAL")
      GoogleWalletImplementation(reactContext)
    }
  }

  @ReactMethod
  fun checkWalletAvailability(promise: Promise) {
    googleWalletImplementation.checkWalletAvailability(promise)
  }

  @ReactMethod
  fun getSecureWalletInfo(promise: Promise) {
    googleWalletImplementation.getSecureWalletInfo(promise)
  }

  @ReactMethod
  fun getTokenStatus(tokenServiceProvider: Int, tokenReferenceId: String, promise: Promise) {
    googleWalletImplementation.getTokenStatus(tokenServiceProvider, tokenReferenceId, promise)
  }

  @ReactMethod
  fun getEnvironment(promise: Promise) {
    googleWalletImplementation.getEnvironment(promise)
  }

  @ReactMethod
  fun isTokenized(
    fpanLastFour: String,
    cardNetwork: Int,
    tokenServiceProvider: Int,
    promise: Promise
  ) {
    googleWalletImplementation.isTokenized(fpanLastFour, cardNetwork, tokenServiceProvider, promise)
  }

  @ReactMethod
  fun viewToken(
    tokenServiceProvider: Int,
    issuerTokenId: String,
    promise: Promise
  ) {
    googleWalletImplementation.viewToken(tokenServiceProvider, issuerTokenId, promise)
  }

  @ReactMethod
  fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
    googleWalletImplementation.addCardToWallet(cardData, promise)
  }

  @ReactMethod
  fun createWalletIfNeeded(promise: Promise) {
    googleWalletImplementation.createWalletIfNeeded(promise)
  }

  @ReactMethod
  fun listTokens(promise: Promise) {
    googleWalletImplementation.listTokens(promise)
  }

  @ReactMethod
  fun setIntentListener(promise: Promise) {
    googleWalletImplementation.setIntentListener(promise)
  }

  @ReactMethod
  fun removeIntentListener(promise: Promise) {
    googleWalletImplementation.removeIntentListener(promise)
  }

  @ReactMethod
  fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
    googleWalletImplementation.setActivationResult(status, activationCode, promise)
  }

  @ReactMethod
  fun finishActivity(promise: Promise) {
    googleWalletImplementation.finishActivity(promise)
  }

  override fun getConstants(): MutableMap<String, Any> {
    val constants = googleWalletImplementation.getConstants().toMutableMap()
    
    // Adicionar informações de configuração
    constants["useMock"] = useMock
    constants["SDK_NAME"] = if (useMock) "GoogleWalletMock" else "GoogleWallet"
    
    return constants
  }


  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "GoogleWallet"
    private const val TAG = "GoogleWallet"
  }
}