package com.builders.wallet.samsungpay

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

object SerializableCard {
  // Conversão reflexiva para qualquer objeto semelhante a Card
  fun Any.toSerializable(): WritableMap {
    val map = Arguments.createMap()

    fun invokeGetter(name: String): Any? = try {
      val method = this.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 0 }
      method?.invoke(this)
    } catch (_: Throwable) { null }

    val cardId = (invokeGetter("getId") ?: invokeGetter("getCardId") ?: invokeGetter("getIdRef"))
      ?.toString()?.takeIf { it.isNotBlank() }
    val last4 = (invokeGetter("getLast4") ?: invokeGetter("getLastDigits") ?: invokeGetter("getCardLast4") ?: invokeGetter("getPanSuffix")) as? String
    val status = (invokeGetter("getStatus") ?: invokeGetter("getCardStatus"))
    val tokenizationProvider = (invokeGetter("getTokenizationProvider") ?: invokeGetter("getTsp"))
    val network = (invokeGetter("getNetwork") ?: invokeGetter("getCardNetwork"))
    val displayName = (invokeGetter("getDisplayName") ?: invokeGetter("getCardName")) as? String

    if (!cardId.isNullOrBlank()) map.putString("id", cardId)
    if (!last4.isNullOrBlank()) map.putString("last4", last4)
    when (status) {
      is Number -> map.putInt("status", status.toInt())
      is String -> map.putString("status", status)
    }
    when (tokenizationProvider) {
      is Number -> map.putInt("tokenizationProvider", tokenizationProvider.toInt())
      is String -> map.putString("tokenizationProvider", tokenizationProvider)
    }
    when (network) {
      is Number -> map.putInt("network", network.toInt())
      is String -> map.putString("network", network)
    }
    if (!displayName.isNullOrBlank()) map.putString("displayName", displayName)

    return map
  }
}