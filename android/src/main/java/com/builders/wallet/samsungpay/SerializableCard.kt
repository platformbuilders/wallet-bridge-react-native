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

    // Campos básicos do Card
    val cardId = (invokeGetter("getId") ?: invokeGetter("getCardId") ?: invokeGetter("getIdRef"))
      ?.toString()?.takeIf { it.isNotBlank() }
    val cardStatus = (invokeGetter("getStatus") ?: invokeGetter("getCardStatus")) as? String
    val cardBrand = invokeGetter("getCardBrand") // SpaySdk.Brand
    val cardInfo = invokeGetter("getCardInfo") // Bundle

    // Adicionar campos básicos
    if (!cardId.isNullOrBlank()) map.putString("cardId", cardId)
    if (!cardStatus.isNullOrBlank()) map.putString("cardStatus", cardStatus)
    
    // Adicionar cardBrand (enum)
    when (cardBrand) {
      is Enum<*> -> map.putString("cardBrand", cardBrand.name)
      is String -> map.putString("cardBrand", cardBrand)
      else -> map.putString("cardBrand", "UNKNOWN_CARD")
    }

    // Processar cardInfo (Bundle) se disponível
    if (cardInfo != null) {
      try {
        val bundleClass = cardInfo.javaClass
        val getStringMethod = bundleClass.getMethod("getString", String::class.java)
        
        // Campos específicos do Samsung Pay Card
        val last4FPan = getStringMethod.invoke(cardInfo, "last4Fpan") as? String
        val last4DPan = getStringMethod.invoke(cardInfo, "last4Dpan") as? String
        val app2AppPayload = getStringMethod.invoke(cardInfo, "app2appPayload") as? String
        val cardType = getStringMethod.invoke(cardInfo, "cardType") as? String
        val issuerName = getStringMethod.invoke(cardInfo, "issuerName") as? String
        val last4Dpan = getStringMethod.invoke(cardInfo, "last4Dpan") as? String
        val last4Fpan = getStringMethod.invoke(cardInfo, "last4Fpan") as? String
        val cardBrand = getStringMethod.invoke(cardInfo, "cardBrand") as? String
        val isDefaultCard = getStringMethod.invoke(cardInfo, "defaultCard") as? String
        val deviceType = getStringMethod.invoke(cardInfo, "deviceType") as? String
        val memberID = getStringMethod.invoke(cardInfo, "memberID") as? String
        val countryCode = getStringMethod.invoke(cardInfo, "countryCode") as? String
        val cryptogramType = getStringMethod.invoke(cardInfo, "cryptogramType") as? String
        val requireCpf = getStringMethod.invoke(cardInfo, "requireCpf") as? String
        val cpfHolderName = getStringMethod.invoke(cardInfo, "cpfHolderName") as? String
        val cpfNumber = getStringMethod.invoke(cardInfo, "cpfNumber") as? String
        val merchantRefId = getStringMethod.invoke(cardInfo, "merchantRefId") as? String
        val transactionType = getStringMethod.invoke(cardInfo, "transactionType") as? String

        // Adicionar campos do cardInfo
        if (!last4FPan.isNullOrBlank()) map.putString("last4FPan", last4FPan)
        if (!last4DPan.isNullOrBlank()) map.putString("last4DPan", last4DPan)
        if (!app2AppPayload.isNullOrBlank()) map.putString("app2AppPayload", app2AppPayload)
        if (!cardType.isNullOrBlank()) map.putString("cardType", cardType)
        if (!issuerName.isNullOrBlank()) map.putString("issuerName", issuerName)
        if (!last4Dpan.isNullOrBlank()) map.putString("last4Dpan", last4Dpan)
        if (!last4Fpan.isNullOrBlank()) map.putString("last4Fpan", last4Fpan)
        if (!cardBrand.isNullOrBlank()) map.putString("cardBrand", cardBrand)
        if (!isDefaultCard.isNullOrBlank()) map.putString("isDefaultCard", isDefaultCard)
        if (!deviceType.isNullOrBlank()) map.putString("deviceType", deviceType)
        if (!memberID.isNullOrBlank()) map.putString("memberID", memberID)
        if (!countryCode.isNullOrBlank()) map.putString("countryCode", countryCode)
        if (!cryptogramType.isNullOrBlank()) map.putString("cryptogramType", cryptogramType)
        if (!requireCpf.isNullOrBlank()) map.putString("requireCpf", requireCpf)
        if (!cpfHolderName.isNullOrBlank()) map.putString("cpfHolderName", cpfHolderName)
        if (!cpfNumber.isNullOrBlank()) map.putString("cpfNumber", cpfNumber)
        if (!merchantRefId.isNullOrBlank()) map.putString("merchantRefId", merchantRefId)
        if (!transactionType.isNullOrBlank()) map.putString("transactionType", transactionType)

      } catch (e: Exception) {
        // Se não conseguir acessar o Bundle, continuar sem os campos extras
        println("Erro ao processar cardInfo: ${e.message}")
      }
    }

    // Campos de compatibilidade (mantidos para compatibilidade com outros SDKs)
    val last4 = (invokeGetter("getLast4") ?: invokeGetter("getLastDigits") ?: invokeGetter("getCardLast4") ?: invokeGetter("getPanSuffix")) as? String
    val tokenizationProvider = (invokeGetter("getTokenizationProvider") ?: invokeGetter("getTsp"))
    val network = (invokeGetter("getNetwork") ?: invokeGetter("getCardNetwork"))
    val displayName = (invokeGetter("getDisplayName") ?: invokeGetter("getCardName")) as? String

    if (!last4.isNullOrBlank()) map.putString("last4", last4)
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