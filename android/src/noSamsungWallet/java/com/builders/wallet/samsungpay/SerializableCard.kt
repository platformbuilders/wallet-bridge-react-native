package com.builders.wallet.samsungpay

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

/**
 * STUB do SerializableCard - usado quando SAMSUNG_WALLET_ENABLED = false
 * 
 * Esta versão NÃO requer o SDK do Samsung Pay
 * Retorna mapa vazio pois não há Card para serializar
 */
object SerializableCard {
  // Stub - apenas para compatibilidade (nunca será chamado pois SamsungWalletStub rejeita antes)
  fun Any.toSerializable(): WritableMap {
    val map = Arguments.createMap()
    map.putString("error", "Samsung Pay SDK não está habilitado")
    return map
  }
}

