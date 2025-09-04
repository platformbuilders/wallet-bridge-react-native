package com.builders.wallet

import com.google.android.gms.tapandpay.issuer.TokenInfo
import java.io.Serializable

data class SerializableTokenInfo(
  val issuerTokenId: String,
  val issuerName: String,
  val fpanLastFour: String,
  val dpanLastFour: String,
  val tokenServiceProvider: Int,
  val network: Int,
  val tokenState: Int,
  val isDefaultToken: Boolean,
  val portifolioName: String
) : Serializable {

  companion object {
    fun TokenInfo.toSerializable(): SerializableTokenInfo {
      return SerializableTokenInfo(
        issuerTokenId,
        issuerName,
        fpanLastFour,
        dpanLastFour,
        tokenServiceProvider,
        network,
        tokenState,
        isDefaultToken,
        portfolioName
      )
    }
  }
}
