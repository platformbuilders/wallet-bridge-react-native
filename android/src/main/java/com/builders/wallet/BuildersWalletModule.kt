package com.builders.wallet

import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = BuildersWalletModule.NAME)
class BuildersWalletModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val walletModule: WalletModuleInterface by lazy {
    WalletModuleFactory.createWalletModule(reactApplicationContext)
  }

  @ReactMethod
  fun getAvailableWallets(promise: Promise) {
    try {
      val availableModules = WalletModuleFactory.getAvailableModules()
      val moduleNames = WalletModuleFactory.getAvailableModuleNames()
      
      val result = mapOf(
        "modules" to availableModules,
        "moduleNames" to moduleNames,
        "currentModule" to walletModule.getName()
      )
      
      promise.resolve(result)
    } catch (e: Exception) {
      Log.e(TAG, "Erro ao obter wallets disponíveis: ${e.message}")
      promise.reject("GET_AVAILABLE_WALLETS_ERROR", e.message)
    }
  }

  @ReactMethod
  fun switchWallet(walletType: String, promise: Promise) {
    try {
      val newModule = WalletModuleFactory.createWalletModule(reactApplicationContext, walletType)
      if (newModule.isAvailable()) {
        // Aqui você poderia implementar uma lógica para trocar o módulo ativo
        // Por enquanto, apenas retorna sucesso
        promise.resolve("Wallet trocado para: ${newModule.getName()}")
      } else {
        promise.reject("WALLET_NOT_AVAILABLE", "Wallet $walletType não está disponível")
      }
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
      walletModule.checkWalletAvailability(promise)
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
      walletModule.getSecureWalletInfo(promise)
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
      walletModule.getCardStatusBySuffix(lastDigits, promise)
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
      walletModule.getCardStatusByIdentifier(identifier, tsp, promise)
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
      walletModule.addCardToWallet(cardData, promise)
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
      walletModule.createWalletIfNeeded(promise)
      Log.d(TAG, "✅ [NATIVE] createWalletIfNeeded executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [NATIVE] Erro em createWalletIfNeeded: ${e.message}", e)
      promise.reject("CREATE_WALLET_IF_NEEDED_ERROR", e.message, e)
    }
  }

  override fun getConstants(): MutableMap<String, Any> {
    val constants = mutableMapOf<String, Any>()
    
    // Adiciona constantes do módulo ativo
    constants.putAll(walletModule.getConstants())
    
    // Adiciona informações sobre o módulo principal
    constants["MAIN_MODULE_NAME"] = NAME
    constants["CURRENT_WALLET_MODULE"] = walletModule.getName()
    constants["WALLET_AVAILABLE"] = walletModule.isAvailable()
    
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