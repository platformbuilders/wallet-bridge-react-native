package com.builders.wallet.googletapandpay

import java.io.Serializable

/**
 * STUB do SerializableTokenInfo - usado quando GOOGLE_WALLET_ENABLED = false
 * 
 * Esta versão NÃO requer o SDK do Google Pay
 * Mantém a mesma estrutura de dados mas sem conversão de TokenInfo
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
    // Stub - nunca será chamado pois GoogleWalletStub rejeita antes
    fun fromTokenInfo(tokenInfo: Any): SerializableTokenInfo? {
      // Retorna null pois não há SDK para processar TokenInfo
      return null
    }
  }
}

