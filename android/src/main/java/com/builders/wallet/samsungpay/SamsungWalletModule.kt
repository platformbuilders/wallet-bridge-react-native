package com.builders.wallet.samsungpay

import com.builders.wallet.WalletLogger
import com.builders.wallet.BuildConfig
import com.builders.wallet.NativeSamsungWalletSpec
import com.builders.wallet.WalletEvents
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Callback
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = SamsungWalletModule.NAME)
class SamsungWalletModule(reactContext: ReactApplicationContext) :
  NativeSamsungWalletSpec(reactContext) {

  // TurboModule: eventos saem pelos EventEmitter gerados pelo codegen.
  // O WalletEvents guarda só uma referência fraca ao sink; este campo o mantém
  // vivo enquanto o módulo existir (sem ele, o GC descartaria o sink e os
  // eventos seriam perdidos sem aviso).
  private val eventSink = object : WalletEvents.Sink {
    override fun emitIntentReceived(data: WritableMap) = emitOnIntentReceived(data)
    override fun emitNoIntentReceived() = emitOnNoIntentReceived()
    override fun emitValidCallerNoIntentReceived() = emitOnValidCallerNoIntentReceived()
    override fun emitLog(data: WritableMap) = emitOnLog(data)
  }

  // Lê configuração de mock do BuildConfig (variável de ambiente do Gradle)
  init {
    // Inicializar WalletLogger com o contexto
    WalletLogger.initialize(reactContext)

    WalletEvents.registerSamsung(eventSink)
  }

  private val useMock: Boolean by lazy {
    try {
      val mockValue = BuildConfig.SAMSUNG_WALLET_USE_MOCK
      WalletLogger.d(TAG, "🔧 [MODULE] SAMSUNG_WALLET_USE_MOCK = $mockValue")
      mockValue
    } catch (e: Exception) {
      WalletLogger.w(TAG, "🔧 [MODULE] SAMSUNG_WALLET_USE_MOCK não definido, usando padrão: false")
      false
    }
  }

  private val samsungWalletImplementation: SamsungWalletContract by lazy {
    if (useMock) {
      WalletLogger.d(TAG, "🔧 [MODULE] Usando implementação MOCK")
      SamsungWalletMock(reactContext)
    } else {
      // A implementação correta (Real ou Stub) será selecionada pelo source set do Gradle
      WalletLogger.d(TAG, "🔧 [MODULE] Usando implementação ${if (BuildConfig.SAMSUNG_WALLET_ENABLED) "REAL" else "STUB"}")
      SamsungWalletImplementation(reactContext)
    }
  }

  override fun init(serviceId: String, promise: Promise) {
    samsungWalletImplementation.init(serviceId, promise)
  }

  override fun getSamsungPayStatus(promise: Promise) {
    samsungWalletImplementation.getSamsungPayStatus(promise)
  }

  override fun goToUpdatePage() {
    samsungWalletImplementation.goToUpdatePage()
  }

  override fun activateSamsungPay() {
    samsungWalletImplementation.activateSamsungPay()
  }

  override fun getAllCards(promise: Promise) {
    samsungWalletImplementation.getAllCards(promise)
  }

  override fun getWalletInfo(promise: Promise) {
    samsungWalletImplementation.getWalletInfo(promise)
  }

  override fun addCard(
    payload: String,
    issuerId: String,
    tokenizationProvider: String,
    cardType: String,
    promise: Promise
  ) {
    samsungWalletImplementation.addCard(payload, issuerId, tokenizationProvider, cardType, promise)
  }

  override fun checkWalletAvailability(promise: Promise) {
    samsungWalletImplementation.checkWalletAvailability(promise)
  }

  override fun setIntentListener(promise: Promise) {
    samsungWalletImplementation.setIntentListener(promise)
  }

  override fun removeIntentListener(promise: Promise) {
    samsungWalletImplementation.removeIntentListener(promise)
  }

  override fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
    samsungWalletImplementation.setActivationResult(status, activationCode, promise)
  }

  override fun finishActivity(promise: Promise) {
    samsungWalletImplementation.finishActivity(promise)
  }

  override fun openWallet(promise: Promise) {
    samsungWalletImplementation.openWallet(promise)
  }

  override fun setLogListener(promise: Promise) {
    samsungWalletImplementation.setLogListener(promise)
  }

  override fun removeLogListener(promise: Promise) {
    samsungWalletImplementation.removeLogListener(promise)
  }

  override fun getTypedExportedConstants(): Map<String, Any> {
    val constants = samsungWalletImplementation.getConstants().toMutableMap()

    // Adicionar informações de configuração
    constants["useMock"] = useMock
    constants["SDK_NAME"] = if (useMock) "SamsungWalletMock" else "SamsungWallet"

    return constants
  }


  companion object {
    const val NAME = "SamsungWallet"
    private const val TAG = "SamsungWallet"

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
          val mockValue = BuildConfig.SAMSUNG_WALLET_USE_MOCK
          WalletLogger.d(TAG, "🔧 [STATIC] SAMSUNG_WALLET_USE_MOCK = $mockValue")
          mockValue
        } catch (e: Exception) {
          WalletLogger.w(TAG, "🔧 [STATIC] SAMSUNG_WALLET_USE_MOCK não definido, usando padrão: false")
          false
        }

        WalletLogger.d(TAG, "🔍 [STATIC] processIntent chamado - Action: ${intent.action}")

        if (useMock) {
          WalletLogger.d(TAG, "🔧 [STATIC] Processando intent com MOCK")
          SamsungWalletMock.processIntent(activity, intent)
        } else {
          // Usa Real ou Stub dependendo da configuração (selecionado pelo source set do Gradle)
          WalletLogger.d(TAG, "🔧 [STATIC] Processando intent com ${if (BuildConfig.SAMSUNG_WALLET_ENABLED) "REAL" else "STUB"}")
          SamsungWalletImplementation.processIntent(activity, intent)
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
          val module = reactContext.getNativeModule(SamsungWalletModule::class.java)
          if (module != null) {
            module.samsungWalletImplementation.sendValidCallerNoIntentEvent()
            WalletLogger.d(TAG, "✅ [STATIC] Evento de caller válido sem intent enviado com sucesso")
          } else {
            WalletLogger.e(TAG, "❌ [STATIC] Instância do SamsungWalletModule não encontrada.")
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
          val module = reactContext.getNativeModule(SamsungWalletModule::class.java)
          if (module != null) {
            module.samsungWalletImplementation.sendNoIntentReceivedEvent()
            WalletLogger.d(TAG, "✅ [STATIC] Evento de nenhuma intent enviado com sucesso")
          } else {
            WalletLogger.e(TAG, "❌ [STATIC] Instância do SamsungWalletModule não encontrada.")
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
