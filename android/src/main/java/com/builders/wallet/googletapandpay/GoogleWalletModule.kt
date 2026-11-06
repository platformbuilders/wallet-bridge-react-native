package com.builders.wallet.googletapandpay

import com.builders.wallet.WalletLogger
import com.builders.wallet.BuildConfig
import com.builders.wallet.NativeGoogleWalletSpec
import com.builders.wallet.WalletEvents
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = GoogleWalletModule.NAME)
class GoogleWalletModule(reactContext: ReactApplicationContext) :
  NativeGoogleWalletSpec(reactContext) {

  // Lê configuração de mock do BuildConfig (variável de ambiente do Gradle)
  init {
    // Inicializar WalletLogger com o contexto
    WalletLogger.initialize(reactContext)

    // TurboModule: eventos saem pelos EventEmitter gerados pelo codegen.
    WalletEvents.registerGoogle(object : WalletEvents.Sink {
      override fun emitIntentReceived(data: WritableMap) = emitOnIntentReceived(data)
      override fun emitNoIntentReceived() = emitOnNoIntentReceived()
      override fun emitValidCallerNoIntentReceived() = emitOnValidCallerNoIntentReceived()
      override fun emitLog(data: WritableMap) = emitOnLog(data)
    })
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

  override fun checkWalletAvailability(promise: Promise) {
    googleWalletImplementation.checkWalletAvailability(promise)
  }

  override fun getSecureWalletInfo(promise: Promise) {
    googleWalletImplementation.getSecureWalletInfo(promise)
  }

  // Inteiros chegam como Double: a spec usa `number` (ver src/specs).
  override fun getTokenStatus(tokenServiceProvider: Double, tokenReferenceId: String, promise: Promise) {
    googleWalletImplementation.getTokenStatus(tokenServiceProvider.toInt(), tokenReferenceId, promise)
  }

  override fun getEnvironment(promise: Promise) {
    googleWalletImplementation.getEnvironment(promise)
  }

  override fun isTokenized(
    fpanLastFour: String,
    cardNetwork: Double,
    tokenServiceProvider: Double,
    promise: Promise
  ) {
    googleWalletImplementation.isTokenized(fpanLastFour, cardNetwork.toInt(), tokenServiceProvider.toInt(), promise)
  }

  override fun viewToken(
    tokenServiceProvider: Double,
    issuerTokenId: String,
    promise: Promise
  ) {
    googleWalletImplementation.viewToken(tokenServiceProvider.toInt(), issuerTokenId, promise)
  }

  override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
    googleWalletImplementation.addCardToWallet(cardData, promise)
  }

  override fun createWalletIfNeeded(promise: Promise) {
    googleWalletImplementation.createWalletIfNeeded(promise)
  }

  override fun listTokens(promise: Promise) {
    googleWalletImplementation.listTokens(promise)
  }

  override fun setIntentListener(promise: Promise) {
    googleWalletImplementation.setIntentListener(promise)
  }

  override fun removeIntentListener(promise: Promise) {
    googleWalletImplementation.removeIntentListener(promise)
  }

  override fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
    googleWalletImplementation.setActivationResult(status, activationCode, promise)
  }

  override fun finishActivity(promise: Promise) {
    googleWalletImplementation.finishActivity(promise)
  }

  override fun openWallet(promise: Promise) {
    googleWalletImplementation.openWallet(promise)
  }

  override fun setLogListener(promise: Promise) {
    googleWalletImplementation.setLogListener(promise)
  }

  override fun removeLogListener(promise: Promise) {
    googleWalletImplementation.removeLogListener(promise)
  }



  override fun getTypedExportedConstants(): Map<String, Any> {
    val constants = googleWalletImplementation.getConstants().toMutableMap()

    // Adicionar informações de configuração
    constants["useMock"] = useMock
    constants["SDK_NAME"] = if (useMock) "GoogleWalletMock" else "GoogleWallet"

    return constants
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
