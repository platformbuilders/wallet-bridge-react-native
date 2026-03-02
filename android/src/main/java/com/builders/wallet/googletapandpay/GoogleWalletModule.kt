package com.builders.wallet.googletapandpay

import com.builders.wallet.WalletLogger
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
  init {
    // Inicializar WalletLogger com o contexto
    WalletLogger.initialize(reactContext)
  }

  private val useMock: Boolean by lazy {
    try {
      val mockValue = BuildConfig.GOOGLE_WALLET_USE_MOCK
      WalletLogger.d(TAG, "🔧 [MODULE] GOOGLE_WALLET_USE_MOCK = $mockValue")
      mockValue
    } catch (e: Exception) {
      WalletLogger.w(TAG, "🔧 [MODULE] GOOGLE_WALLET_USE_MOCK não definido, usando padrão: false")
      false
    }
  }

  private val googleWalletImplementation: GoogleWalletContract by lazy {
    if (useMock) {
      WalletLogger.d(TAG, "🔧 [MODULE] Usando implementação MOCK")
      GoogleWalletMock(reactContext)
    } else {
      // A implementação correta (Real ou Stub) será selecionada pelo source set do Gradle
      WalletLogger.d(TAG, "🔧 [MODULE] Usando implementação ${if (BuildConfig.GOOGLE_WALLET_ENABLED) "REAL" else "STUB"}")
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

  @ReactMethod
  fun openWallet(promise: Promise) {
    googleWalletImplementation.openWallet(promise)
  }

  @ReactMethod
  fun setLogListener(promise: Promise) {
    googleWalletImplementation.setLogListener(promise)
  }

  @ReactMethod
  fun removeLogListener(promise: Promise) {
    googleWalletImplementation.removeLogListener(promise)
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

    // Flag para indicar que nenhuma intent foi recebida (app aberto sem wallet)
    @Volatile
    private var hasNoIntentReceivedFlag: Boolean = false

    // Flag para indicar que uma wallet válida chamou o app, mas sem payload
    @Volatile
    private var hasValidCallerNoIntentFlag: Boolean = false

    @JvmStatic
    fun processIntent(activity: android.app.Activity, intent: android.content.Intent) {
      try {
        // Determinar se deve usar mock baseado na configuração
        val useMock = try {
          val mockValue = BuildConfig.GOOGLE_WALLET_USE_MOCK
          WalletLogger.d(TAG, "🔧 [STATIC] GOOGLE_WALLET_USE_MOCK = $mockValue")
          mockValue
        } catch (e: Exception) {
          WalletLogger.w(TAG, "🔧 [STATIC] GOOGLE_WALLET_USE_MOCK não definido, usando padrão: false")
          false
        }

        WalletLogger.d(TAG, "🔍 [STATIC] processIntent chamado - Action: ${intent.action}")

        if (useMock) {
          WalletLogger.d(TAG, "🔧 [STATIC] Processando intent com MOCK")
          GoogleWalletMock.processIntent(activity, intent)
        } else {
          // Usa Real ou Stub dependendo da configuração (selecionado pelo source set do Gradle)
          WalletLogger.d(TAG, "🔧 [STATIC] Processando intent com ${if (BuildConfig.GOOGLE_WALLET_ENABLED) "REAL" else "STUB"}")
          GoogleWalletImplementation.processIntent(activity, intent)
        }
      } catch (e: Exception) {
        WalletLogger.e(TAG, "❌ [STATIC] Erro ao processar intent: ${e.message}", e)
      }
    }

    @JvmStatic
    fun setNoIntentReceivedFlag() {
      hasNoIntentReceivedFlag = true
      WalletLogger.d(TAG, "🔍 [STATIC] Flag de nenhuma intent definido (app aberto sem wallet)")
    }

    @JvmStatic
    fun setValidCallerNoIntentFlag() {
      hasValidCallerNoIntentFlag = true
      WalletLogger.d(TAG, "⚠️ [STATIC] Flag de caller válido sem intent definido (wallet chamou sem payload)")
    }

    @JvmStatic
    fun processValidCallerNoIntentEvent(reactContext: ReactApplicationContext) {
      if (hasValidCallerNoIntentFlag) {
        WalletLogger.d(TAG, "⚠️ [STATIC] Processando evento de caller válido sem intent pendente")
        try {
          val module = reactContext.getNativeModule(GoogleWalletModule::class.java)
          if (module != null) {
            module.googleWalletImplementation.sendValidCallerNoIntentEvent()
            WalletLogger.d(TAG, "✅ [STATIC] Evento de caller válido sem intent enviado com sucesso")
          } else {
            WalletLogger.e(TAG, "❌ [STATIC] Instância do GoogleWalletModule não encontrada.")
          }
        } catch (e: Exception) {
          WalletLogger.e(TAG, "❌ [STATIC] Erro ao enviar evento de caller válido sem intent: ${e.message}", e)
        } finally {
          hasValidCallerNoIntentFlag = false
          WalletLogger.d(TAG, "🧹 [STATIC] Flag de caller válido sem intent limpo")
        }
      }
    }

    @JvmStatic
    fun processNoIntentReceivedEvent(reactContext: ReactApplicationContext) {
      if (hasNoIntentReceivedFlag) {
        WalletLogger.d(TAG, "🔍 [STATIC] Processando evento de nenhuma intent pendente")
        try {
          val module = reactContext.getNativeModule(GoogleWalletModule::class.java)
          if (module != null) {
            module.googleWalletImplementation.sendNoIntentReceivedEvent()
            WalletLogger.d(TAG, "✅ [STATIC] Evento de nenhuma intent enviado com sucesso")
          } else {
            WalletLogger.e(TAG, "❌ [STATIC] Instância do GoogleWalletModule não encontrada.")
          }
        } catch (e: Exception) {
          WalletLogger.e(TAG, "❌ [STATIC] Erro ao enviar evento de nenhuma intent: ${e.message}", e)
        } finally {
          // Limpar flag após processamento
          hasNoIntentReceivedFlag = false
          WalletLogger.d(TAG, "🧹 [STATIC] Flag de nenhuma intent limpo")
        }
      }
    }
  }
}
