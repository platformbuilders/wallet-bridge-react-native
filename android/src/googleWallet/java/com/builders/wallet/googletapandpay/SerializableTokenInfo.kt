package com.builders.wallet.googletapandpay

import com.google.android.gms.tapandpay.issuer.TokenInfo
import java.io.Serializable

/**
 * SerializableTokenInfo LIMPO - USA DIRETAMENTE O SDK (SEM REFLEXÃO)
 * 
 * Esta versão só é compilada quando GOOGLE_WALLET_ENABLED = true
 * Requer a dependência: com.google.android.gms:play-services-tapandpay
 */
data class SerializableTokenInfo(
  val issuerTokenId: String,
  val issuerName: String,
  val fpanLastFour: String,
  val dpanLastFour: String,
  val tokenServiceProvider: Int,
  val network: Int,
  val tokenState: Int,
  val isDefaultToken: Boolean,
  val portfolioName: String
) : Serializable {

  companion object {
    // Conversão direta do objeto TokenInfo do Google Pay SDK
    fun fromTokenInfo(tokenInfo: TokenInfo): SerializableTokenInfo {
      // Usa diretamente as propriedades do TokenInfo (sem reflexão!)
      return SerializableTokenInfo(
        issuerTokenId = tokenInfo.issuerTokenId,
        issuerName = tokenInfo.issuerName,
        fpanLastFour = tokenInfo.fpanLastFour,
        dpanLastFour = tokenInfo.dpanLastFour,
        tokenServiceProvider = tokenInfo.tokenServiceProvider,
        network = tokenInfo.network,
        tokenState = tokenInfo.tokenState,
        isDefaultToken = tokenInfo.isDefaultToken,
        portfolioName = tokenInfo.portfolioName
      )
    }
  }
}

