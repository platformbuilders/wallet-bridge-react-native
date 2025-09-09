package com.builders.wallet

import android.util.Log
import com.builders.wallet.googletapandpay.GoogleWalletModule
import com.builders.wallet.samsungpay.SamsungWalletModule
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = BuildersWalletModule.NAME)
class BuildersWalletModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val googleWalletModule = GoogleWalletModule(reactContext)
  private val samsungWalletModule = SamsungWalletModule(reactContext)

  @ReactMethod
  fun getAvailableWallets(promise: Promise) {
    try {
      val availableModules = listOf("GoogleWallet", "SamsungWallet")
      val moduleNames = listOf("GoogleWallet", "SamsungWallet")
      
      val result = Arguments.createMap()
      result.putArray("modules", Arguments.fromArray(availableModules.toTypedArray()))
      result.putArray("moduleNames", Arguments.fromArray(moduleNames.toTypedArray()))
      result.putString("currentModule", "GoogleWallet") // Default to Google
      
      promise.resolve(result)
    } catch (e: Exception) {
      Log.e(TAG, "Erro ao obter wallets disponíveis: ${e.message}")
      promise.reject("GET_AVAILABLE_WALLETS_ERROR", e.message)
    }
  }

  @ReactMethod
  fun switchWallet(walletType: String, promise: Promise) {
    try {
      // Simula troca de wallet - na prática, o usuário deve usar os módulos específicos
      promise.resolve("Wallet trocado para: $walletType. Use os módulos específicos para melhor controle.")
    } catch (e: Exception) {
      Log.e(TAG, "Erro ao trocar wallet: ${e.message}")
      promise.reject("SWITCH_WALLET_ERROR", e.message)
    }
  }

  // Check wallet availability and initialize
  @ReactMethod
  fun checkWalletAvailability(promise: Promise) {
    Log.d(TAG, "🔍 [NATIVE] checkWalletAvailability chamado")
    try {
      // Tenta Google Wallet primeiro, depois Samsung
      googleWalletModule.checkWalletAvailability(promise)
      Log.d(TAG, "✅ [NATIVE] checkWalletAvailability executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [NATIVE] Erro em checkWalletAvailability: ${e.message}", e)
      promise.reject("CHECK_WALLET_AVAILABILITY_ERROR", e.message, e)
    }
  }

  // Get wallet information for secure transactions
  @ReactMethod
  fun getSecureWalletInfo(promise: Promise) {
    Log.d(TAG, "🔍 [NATIVE] getSecureWalletInfo chamado")
    try {
      googleWalletModule.getSecureWalletInfo(promise)
      Log.d(TAG, "✅ [NATIVE] getSecureWalletInfo executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [NATIVE] Erro em getSecureWalletInfo: ${e.message}", e)
      promise.reject("GET_SECURE_WALLET_INFO_ERROR", e.message, e)
    }
  }

  // Get card status by last digits
  @ReactMethod
  fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
    Log.d(TAG, "🔍 [NATIVE] getCardStatusBySuffix chamado com lastDigits: $lastDigits")
    try {
      googleWalletModule.getCardStatusBySuffix(lastDigits, promise)
      Log.d(TAG, "✅ [NATIVE] getCardStatusBySuffix executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [NATIVE] Erro em getCardStatusBySuffix: ${e.message}", e)
      promise.reject("GET_CARD_STATUS_BY_SUFFIX_ERROR", e.message, e)
    }
  }

  // Get card status by identifier
  @ReactMethod
  fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
    Log.d(TAG, "🔍 [NATIVE] getCardStatusByIdentifier chamado com identifier: $identifier, tsp: $tsp")
    try {
      googleWalletModule.getCardStatusByIdentifier(identifier, tsp, promise)
      Log.d(TAG, "✅ [NATIVE] getCardStatusByIdentifier executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [NATIVE] Erro em getCardStatusByIdentifier: ${e.message}", e)
      promise.reject("GET_CARD_STATUS_BY_IDENTIFIER_ERROR", e.message, e)
    }
  }

  // Add card to wallet (Push Provisioning)
  @ReactMethod
  fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
    Log.d(TAG, "🔍 [NATIVE] addCardToWallet chamado")
    try {
      Log.d(TAG, "🔍 [NATIVE] Dados do cartão recebidos: $cardData")
      googleWalletModule.addCardToWallet(cardData, promise)
      Log.d(TAG, "✅ [NATIVE] addCardToWallet executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [NATIVE] Erro em addCardToWallet: ${e.message}", e)
      promise.reject("ADD_CARD_TO_WALLET_ERROR", e.message, e)
    }
  }

  // Create wallet if it doesn't exist
  @ReactMethod
  fun createWalletIfNeeded(promise: Promise) {
    Log.d(TAG, "🔍 [NATIVE] createWalletIfNeeded chamado")
    try {
      googleWalletModule.createWalletIfNeeded(promise)
      Log.d(TAG, "✅ [NATIVE] createWalletIfNeeded executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [NATIVE] Erro em createWalletIfNeeded: ${e.message}", e)
      promise.reject("CREATE_WALLET_IF_NEEDED_ERROR", e.message, e)
    }
  }

  override fun getConstants(): MutableMap<String, Any> {
    val constants = mutableMapOf<String, Any>()
    
    // Adiciona constantes do Google Wallet como padrão
    try {
      val googleConstants = googleWalletModule.getConstants()
      if (googleConstants != null) {
        constants.putAll(googleConstants)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Erro ao obter constantes do Google Wallet: ${e.message}")
    }
    
    // Adiciona informações sobre o módulo principal
    constants["MAIN_MODULE_NAME"] = NAME
    constants["CURRENT_WALLET_MODULE"] = "GoogleWallet"
    constants["WALLET_AVAILABLE"] = true
    
    return constants
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "BuildersWallet"
    private const val TAG = "BuildersWallet"
  }
}