package com.builders.wallet.samsungpay

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = SamsungWalletModule.NAME)
class SamsungWalletModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val isSDKAvailable: Boolean by lazy {
    try {
      Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk")
      Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk\$Companion")
      true
    } catch (e: ClassNotFoundException) {
      false
    }
  }

  @ReactMethod
  fun checkWalletAvailability(promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] checkWalletAvailability chamado")
    try {
      if (!isSDKAvailable) {
        promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
        return
      }
      
      // Implementação simplificada - retorna true se SDK disponível
      promise.resolve(true)
      Log.d(TAG, "✅ [SAMSUNG] checkWalletAvailability executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro em checkWalletAvailability: ${e.message}", e)
      promise.reject("CHECK_WALLET_AVAILABILITY_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun getSecureWalletInfo(promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] getSecureWalletInfo chamado")
    try {
      if (!isSDKAvailable) {
        promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
        return
      }
      
      // Implementação simplificada - retorna dados mockados
      val result = Arguments.createMap()
      result.putString("deviceID", "samsung_device_123")
      result.putString("walletAccountID", "samsung_account_456")
      promise.resolve(result)
      Log.d(TAG, "✅ [SAMSUNG] getSecureWalletInfo executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro em getSecureWalletInfo: ${e.message}", e)
      promise.reject("GET_SECURE_WALLET_INFO_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] getCardStatusBySuffix chamado com lastDigits: $lastDigits")
    try {
      if (!isSDKAvailable) {
        promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
        return
      }
      
      // Implementação simplificada - retorna status mockado
      promise.resolve("not found")
      Log.d(TAG, "✅ [SAMSUNG] getCardStatusBySuffix executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro em getCardStatusBySuffix: ${e.message}", e)
      promise.reject("GET_CARD_STATUS_BY_SUFFIX_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] getCardStatusByIdentifier chamado com identifier: $identifier, tsp: $tsp")
    try {
      if (!isSDKAvailable) {
        promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
        return
      }
      
      // Implementação simplificada - retorna status mockado
      promise.resolve("not found")
      Log.d(TAG, "✅ [SAMSUNG] getCardStatusByIdentifier executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro em getCardStatusByIdentifier: ${e.message}", e)
      promise.reject("GET_CARD_STATUS_BY_IDENTIFIER_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] addCardToWallet chamado")
    try {
      if (!isSDKAvailable) {
        promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
        return
      }
      
      Log.d(TAG, "🔍 [SAMSUNG] Dados do cartão recebidos: $cardData")
      
      // Implementação simplificada - retorna token mockado
      promise.resolve("samsung_token_123")
      Log.d(TAG, "✅ [SAMSUNG] addCardToWallet executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro em addCardToWallet: ${e.message}", e)
      promise.reject("ADD_CARD_TO_WALLET_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun createWalletIfNeeded(promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] createWalletIfNeeded chamado")
    try {
      if (!isSDKAvailable) {
        promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
        return
      }
      
      // Implementação simplificada - sempre retorna false
      promise.resolve(false)
      Log.d(TAG, "✅ [SAMSUNG] createWalletIfNeeded executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro em createWalletIfNeeded: ${e.message}", e)
      promise.reject("CREATE_WALLET_IF_NEEDED_ERROR", e.message, e)
    }
  }

  @ReactMethod
  override fun getConstants(): MutableMap<String, Any> {
    return hashMapOf<String, Any>(
      "SDK_NAME" to "SamsungWallet",
      "MODULE_NAME" to getName()
    )
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "SamsungWallet"
    private const val TAG = "SamsungWallet"
  }
}
