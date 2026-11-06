package com.builders.wallet

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import java.lang.ref.WeakReference

/**
 * Encaminha eventos nativos para o JS pelos `EventEmitter` gerados pelo codegen
 * (TurboModule), no lugar de `RCTDeviceEventEmitter.emit(nome, dados)`.
 *
 * Implementações reais, stubs, mocks e o [WalletLogger] continuam emitindo pelo
 * nome do evento; cada módulo (Google/Samsung) registra aqui como entregar os
 * eventos que são dele. Nomes e payloads não mudam.
 */
object WalletEvents {

  const val GOOGLE_INTENT_RECEIVED = "GoogleWalletIntentReceived"
  const val GOOGLE_NO_INTENT_RECEIVED = "GoogleWalletNoIntentReceived"
  const val GOOGLE_VALID_CALLER_NO_INTENT = "GoogleWalletValidCallerNoIntentReceived"
  const val SAMSUNG_INTENT_RECEIVED = "SamsungWalletIntentReceived"
  const val SAMSUNG_NO_INTENT_RECEIVED = "SamsungWalletNoIntentReceived"
  const val SAMSUNG_VALID_CALLER_NO_INTENT = "SamsungWalletValidCallerNoIntentReceived"
  const val LOG = "WalletLog"

  /** Como um módulo entrega os próprios eventos ao JS. */
  interface Sink {
    fun emitIntentReceived(data: WritableMap)
    fun emitNoIntentReceived()
    fun emitValidCallerNoIntentReceived()
    fun emitLog(data: WritableMap)
  }

  @Volatile private var google: WeakReference<Sink>? = null
  @Volatile private var samsung: WeakReference<Sink>? = null

  fun registerGoogle(sink: Sink) {
    google = WeakReference(sink)
  }

  fun registerSamsung(sink: Sink) {
    samsung = WeakReference(sink)
  }

  /** Retorna false se nenhum módulo estiver registrado para o evento. */
  fun emit(eventName: String, data: WritableMap?): Boolean {
    val googleSink = google?.get()
    val samsungSink = samsung?.get()

    return when (eventName) {
      GOOGLE_INTENT_RECEIVED -> deliver(googleSink) { it.emitIntentReceived(data ?: Arguments.createMap()) }
      GOOGLE_NO_INTENT_RECEIVED -> deliver(googleSink) { it.emitNoIntentReceived() }
      GOOGLE_VALID_CALLER_NO_INTENT -> deliver(googleSink) { it.emitValidCallerNoIntentReceived() }
      SAMSUNG_INTENT_RECEIVED -> deliver(samsungSink) { it.emitIntentReceived(data ?: Arguments.createMap()) }
      SAMSUNG_NO_INTENT_RECEIVED -> deliver(samsungSink) { it.emitNoIntentReceived() }
      SAMSUNG_VALID_CALLER_NO_INTENT -> deliver(samsungSink) { it.emitValidCallerNoIntentReceived() }
      // O log é comum às duas wallets: antes um único evento chegava aos
      // listeners de Google e Samsung; agora cada módulo recebe a sua cópia
      // (um WritableMap só pode ser enviado uma vez).
      LOG -> {
        val payload = data ?: Arguments.createMap()
        val toGoogle = deliver(googleSink) { it.emitLog(copy(payload)) }
        val toSamsung = deliver(samsungSink) { it.emitLog(copy(payload)) }
        toGoogle || toSamsung
      }
      else -> false
    }
  }

  private inline fun deliver(sink: Sink?, block: (Sink) -> Unit): Boolean {
    if (sink == null) return false
    block(sink)
    return true
  }

  private fun copy(source: WritableMap): WritableMap =
    Arguments.createMap().apply { merge(source) }
}
