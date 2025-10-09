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
      // A implementação correta (Real ou Stub) será selecionada pelo source set do Gradle
      Log.d(TAG, "🔧 [MODULE] Usando implementação ${if (BuildConfig.SAMSUNG_WALLET_ENABLED) "REAL" else "STUB"}")
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
    promise: Promise
  ) {
    samsungWalletImplementation.addCard(payload, issuerId, tokenizationProvider, cardType, promise)
  }

  @ReactMethod
  fun checkWalletAvailability(promise: Promise) {
    samsungWalletImplementation.checkWalletAvailability(promise)
  }

  @ReactMethod
  fun setIntentListener(promise: Promise) {
    samsungWalletImplementation.setIntentListener(promise)
  }

  @ReactMethod
  fun removeIntentListener(promise: Promise) {
    samsungWalletImplementation.removeIntentListener(promise)
  }

  @ReactMethod
  fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
    samsungWalletImplementation.setActivationResult(status, activationCode, promise)
  }

  @ReactMethod
  fun finishActivity(promise: Promise) {
    samsungWalletImplementation.finishActivity(promise)
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

    @JvmStatic
    fun processIntent(activity: android.app.Activity, intent: android.content.Intent) {
      try {
        // Determinar se deve usar mock baseado na configuração
        val useMock = try {
          val mockValue = BuildConfig.SAMSUNG_WALLET_USE_MOCK
          Log.d(TAG, "🔧 [STATIC] SAMSUNG_WALLET_USE_MOCK = $mockValue")
          mockValue
        } catch (e: Exception) {
          Log.w(TAG, "🔧 [STATIC] SAMSUNG_WALLET_USE_MOCK não definido, usando padrão: false")
          false
        }

        Log.d(TAG, "🔍 [STATIC] processIntent chamado - Action: ${intent.action}")
        
        if (useMock) {
          Log.d(TAG, "🔧 [STATIC] Processando intent com MOCK")
          SamsungWalletMock.processIntent(activity, intent)
        } else {
          // Usa Real ou Stub dependendo da configuração (selecionado pelo source set do Gradle)
          Log.d(TAG, "🔧 [STATIC] Processando intent com ${if (BuildConfig.SAMSUNG_WALLET_ENABLED) "REAL" else "STUB"}")
          SamsungWalletImplementation.processIntent(activity, intent)
        }
      } catch (e: Exception) {
        Log.e(TAG, "❌ [STATIC] Erro ao processar intent: ${e.message}", e)
      }
    }
  }
}