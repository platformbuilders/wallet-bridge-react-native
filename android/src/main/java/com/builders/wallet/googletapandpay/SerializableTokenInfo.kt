package com.builders.wallet.googletapandpay

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
    // Verifica se o SDK do Google Pay está disponível
    private val isSDKAvailable: Boolean by lazy {
      try {
        Class.forName("com.google.android.gms.tapandpay.issuer.TokenInfo")
        true
      } catch (e: ClassNotFoundException) {
        false
      }
    }

    fun fromTokenInfo(tokenInfo: Any): SerializableTokenInfo? {
      if (!isSDKAvailable) {
        return null
      }
      
      try {
        // Usa reflexão para acessar as propriedades do TokenInfo
        val tokenInfoClass = tokenInfo.javaClass
        
        val issuerTokenId = tokenInfoClass.getMethod("getIssuerTokenId").invoke(tokenInfo) as String
        val issuerName = tokenInfoClass.getMethod("getIssuerName").invoke(tokenInfo) as String
        val fpanLastFour = tokenInfoClass.getMethod("getFpanLastFour").invoke(tokenInfo) as String
        val dpanLastFour = tokenInfoClass.getMethod("getDpanLastFour").invoke(tokenInfo) as String
        val tokenServiceProvider = tokenInfoClass.getMethod("getTokenServiceProvider").invoke(tokenInfo) as Int
        val network = tokenInfoClass.getMethod("getNetwork").invoke(tokenInfo) as Int
        val tokenState = tokenInfoClass.getMethod("getTokenState").invoke(tokenInfo) as Int
        val isDefaultToken = tokenInfoClass.getMethod("isDefaultToken").invoke(tokenInfo) as Boolean
        val portfolioName = tokenInfoClass.getMethod("getPortfolioName").invoke(tokenInfo) as String
        
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
      } catch (e: Exception) {
        return null
      }
    }
  }
}
