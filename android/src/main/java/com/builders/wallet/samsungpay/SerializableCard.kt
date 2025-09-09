package com.builders.wallet.samsungpay

import java.io.Serializable

data class SerializableCard(
  val cardId: String,
  val cardStatus: String,
  val cardBrand: String,
  val cardLast4Fpan: String,
  val cardLast4Dpan: String,
  val cardIssuer: String,
  val cardType: String,
  val isSamsungPayCard: Boolean
) : Serializable {

  companion object {
    fun toSerializable(): SerializableCard {
      return SerializableCard(
        "stub_card_id",
        "stub_status",
        "stub_brand",
        "0000",
        "0000",
        "stub_issuer",
        "stub_type",
        false
      )
    }
  }
}