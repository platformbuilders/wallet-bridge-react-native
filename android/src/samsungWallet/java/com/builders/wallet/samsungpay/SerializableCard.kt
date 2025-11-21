package com.builders.wallet.samsungpay

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.samsung.android.sdk.samsungpay.v2.card.Card

/**
 * SerializableCard LIMPO - USA DIRETAMENTE O SDK (SEM REFLEXÃO)
 * 
 * Esta versão só é compilada quando SAMSUNG_WALLET_ENABLED = true
 */
object SerializableCard {
  // Conversão direta do objeto Card do Samsung Pay SDK
  fun Card.toSerializable(): WritableMap {
    val map = Arguments.createMap()

    // Usar diretamente os métodos do Card!
    try {
      // Campos básicos do Card usando getters
      val cardId = this.cardId
      val cardStatus = this.cardStatus
      val cardBrand = this.cardBrand
      val cardInfo = this.cardInfo

      if (!cardId.isNullOrBlank()) map.putString("cardId", cardId)
      if (!cardStatus.isNullOrBlank()) map.putString("cardStatus", cardStatus)
      
      // Adicionar cardBrand (pode ser Brand enum ou String)
      val brandValue = cardBrand?.toString() ?: "UNKNOWN_CARD"
      map.putString("cardBrand", brandValue)

      // Processar cardInfo (Bundle) se disponível
      if (cardInfo != null) {
        // Campos específicos do Samsung Pay Card
        val last4FPan = cardInfo.getString("last4Fpan")
        val last4DPan = cardInfo.getString("last4Dpan")
        val app2AppPayload = cardInfo.getString("app2appPayload")
        val cardType = cardInfo.getString("cardType")
        val issuerName = cardInfo.getString("issuerName")
        val last4Dpan = cardInfo.getString("last4Dpan")
        val last4Fpan = cardInfo.getString("last4Fpan")
        val isDefaultCard = cardInfo.getString("defaultCard")
        val deviceType = cardInfo.getString("deviceType")
        val memberID = cardInfo.getString("memberID")
        val countryCode = cardInfo.getString("countryCode")
        val cryptogramType = cardInfo.getString("cryptogramType")
        val requireCpf = cardInfo.getString("requireCpf")
        val cpfHolderName = cardInfo.getString("cpfHolderName")
        val cpfNumber = cardInfo.getString("cpfNumber")
        val merchantRefId = cardInfo.getString("merchantRefId")
        val transactionType = cardInfo.getString("transactionType")

        // Adicionar campos do cardInfo
        if (!last4FPan.isNullOrBlank()) map.putString("last4FPan", last4FPan)
        if (!last4DPan.isNullOrBlank()) map.putString("last4DPan", last4DPan)
        if (!app2AppPayload.isNullOrBlank()) map.putString("app2AppPayload", app2AppPayload)
        if (!cardType.isNullOrBlank()) map.putString("cardType", cardType)
        if (!issuerName.isNullOrBlank()) map.putString("issuerName", issuerName)
        if (!last4Dpan.isNullOrBlank()) map.putString("last4Dpan", last4Dpan)
        if (!last4Fpan.isNullOrBlank()) map.putString("last4Fpan", last4Fpan)
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
      }
    } catch (e: Exception) {
      // Se houver erro, retornar mapa básico
      map.putString("error", "Erro ao serializar card: ${e.message}")
    }

    return map
  }
}

