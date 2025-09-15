package com.builders.wallet.googletapandpay

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap

class GoogleWalletMock : GoogleWalletContract {

    companion object {
        private const val TAG = "GoogleWalletMock"
        
        // Enum de códigos de erro para push tokenize
        enum class PushTokenizeErrorCode(val code: String, val description: String) {
            CANCELLED("0", "Push tokenize cancelado pelo usuário"),
            NO_ACTIVE_WALLET("15002", "Nenhuma carteira ativa encontrada"),
            TOKEN_NOT_FOUND("15003", "Token não encontrado"),
            INVALID_TOKEN_STATE("15004", "Estado do token inválido"),
            ATTESTATION_ERROR("15005", "Erro de atestação"),
            UNAVAILABLE("15009", "Serviço indisponível")
        }
    }

    override fun checkWalletAvailability(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] checkWalletAvailability chamado")
        try {
            // Simular verificação de disponibilidade
            Log.d(TAG, "✅ [MOCK] Wallet disponível (simulado)")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em checkWalletAvailability: ${e.message}", e)
            promise.reject("CHECK_WALLET_AVAILABILITY_ERROR", e.message, e)
        }
    }

    override fun getSecureWalletInfo(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getSecureWalletInfo chamado")
        try {
            // Simular dados da carteira
            val result = Arguments.createMap()
            result.putString("deviceID", "mock_device_12345")
            result.putString("walletAccountID", "mock_wallet_67890")
            
            Log.d(TAG, "✅ [MOCK] Informações da carteira obtidas (simuladas)")
            promise.resolve(result)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em getSecureWalletInfo: ${e.message}", e)
            promise.reject("GET_SECURE_WALLET_INFO_ERROR", e.message, e)
        }
    }

    override fun getTokenStatus(tokenServiceProvider: Int, tokenReferenceId: String, promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getTokenStatus chamado - Provider: $tokenServiceProvider, RefId: $tokenReferenceId")
        try {
            // Simular status do token
            val result = Arguments.createMap()
            result.putInt("tokenState", 4) // TOKEN_STATE_ACTIVE
            result.putBoolean("isSelected", true)
            
            Log.d(TAG, "✅ [MOCK] Status do token obtido (simulado)")
            promise.resolve(result)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em getTokenStatus: ${e.message}", e)
            promise.reject("GET_TOKEN_STATUS_ERROR", e.message, e)
        }
    }

    override fun getEnvironment(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getEnvironment chamado")
        try {
            // Simular environment
            val environment = "PRODUCTION"
            Log.d(TAG, "✅ [MOCK] Environment obtido (simulado): $environment")
            promise.resolve(environment)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em getEnvironment: ${e.message}", e)
            promise.reject("GET_ENVIRONMENT_ERROR", e.message, e)
        }
    }

    override fun isTokenized(
        fpanLastFour: String,
        cardNetwork: Int,
        tokenServiceProvider: Int,
        promise: Promise
    ) {
        Log.d(TAG, "🔍 [MOCK] isTokenized chamado - LastFour: $fpanLastFour, Network: $cardNetwork, Provider: $tokenServiceProvider")
        try {
            // Simular verificação de tokenização
            val isTokenized = fpanLastFour == "1234" // Simular que apenas cartões terminados em 1234 estão tokenizados
            Log.d(TAG, "✅ [MOCK] Verificação de tokenização (simulada): $isTokenized")
            promise.resolve(isTokenized)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em isTokenized: ${e.message}", e)
            promise.reject("IS_TOKENIZED_ERROR", e.message, e)
        }
    }

    override fun viewToken(
        tokenServiceProvider: Int,
        issuerTokenId: String,
        promise: Promise
    ) {
        Log.d(TAG, "🔍 [MOCK] viewToken chamado - Provider: $tokenServiceProvider, TokenId: $issuerTokenId")
        try {
            // Simular visualização do token
            Log.d(TAG, "✅ [MOCK] Token visualizado (simulado)")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em viewToken: ${e.message}", e)
            promise.reject("VIEW_TOKEN_ERROR", e.message, e)
        }
    }

    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] addCardToWallet chamado")
        try {
            Log.d(TAG, "🔍 [MOCK] Dados do cartão recebidos: $cardData")
            
            // // 🔧 TESTE DE ERRO - Usando enum de códigos de erro
            // val errorCode = PushTokenizeErrorCode.CANCELLED
            // Log.d(TAG, "❌ [MOCK] Simulando erro de push tokenize: ${errorCode.description}")
            // promise.reject("ADD_CARD_TO_WALLET_ERROR", "Push tokenize falhou - código: ${errorCode.code}")
            
            // Simular adição do cartão com delay (comentado para teste de erro)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    val tokenId = "mock_token_${System.currentTimeMillis()}"
                    Log.d(TAG, "✅ [MOCK] Cartão adicionado à carteira (simulado) - Token ID: $tokenId")
                    promise.resolve(tokenId)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ [MOCK] Erro ao simular adição do cartão: ${e.message}", e)
                    promise.reject("ADD_CARD_TO_WALLET_ERROR", e.message, e)
                }
            }, 2000) // Simular delay de 2 segundos
            
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em addCardToWallet: ${e.message}", e)
            promise.reject("ADD_CARD_TO_WALLET_ERROR", e.message, e)
        }
    }

    override fun createWalletIfNeeded(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] createWalletIfNeeded chamado")
        try {
            // Simular criação da carteira
            Log.d(TAG, "✅ [MOCK] Carteira criada (simulada)")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em createWalletIfNeeded: ${e.message}", e)
            promise.reject("CREATE_WALLET_IF_NEEDED_ERROR", e.message, e)
        }
    }

    override fun listTokens(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] listTokens chamado")
        try {
            // Simular lista de tokens
            val writableArray = Arguments.createArray()
            
            // Adicionar alguns tokens simulados
            val token1 = Arguments.createMap()
            token1.putString("issuerTokenId", "mock_token_001")
            token1.putString("lastDigits", "1234")
            token1.putString("displayName", "Cartão Mock Visa")
            token1.putInt("tokenState", 4) // TOKEN_STATE_ACTIVE
            token1.putInt("network", 1) // VISA
            writableArray.pushMap(token1)
            
            val token2 = Arguments.createMap()
            token2.putString("issuerTokenId", "mock_token_002")
            token2.putString("lastDigits", "5678")
            token2.putString("displayName", "Cartão Mock Mastercard")
            token2.putInt("tokenState", 4) // TOKEN_STATE_ACTIVE
            token2.putInt("network", 2) // MASTERCARD
            writableArray.pushMap(token2)
            
            Log.d(TAG, "✅ [MOCK] Lista de tokens obtida (simulada) - ${writableArray.size()} tokens")
            promise.resolve(writableArray)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em listTokens: ${e.message}", e)
            promise.reject("LIST_TOKENS_ERROR", e.message, e)
        }
    }

    override fun getConstants(): MutableMap<String, Any> {
        Log.d(TAG, "🔍 [MOCK] getConstants chamado")
        
        val constants = hashMapOf<String, Any>()
        
        // Adicionar constantes simuladas
        constants["SDK_AVAILABLE"] = true
        constants["SDK_NAME"] = "GoogleWalletMock"
        constants["TOKEN_PROVIDER_ELO"] = 1
        constants["CARD_NETWORK_ELO"] = 1
        constants["TOKEN_STATE_UNTOKENIZED"] = 0
        constants["TOKEN_STATE_PENDING"] = 1
        constants["TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION"] = 2
        constants["TOKEN_STATE_SUSPENDED"] = 3
        constants["TOKEN_STATE_ACTIVE"] = 4
        constants["TOKEN_STATE_FELICA_PENDING_PROVISIONING"] = 5
        
        Log.d(TAG, "✅ [MOCK] Constantes obtidas (simuladas)")
        return constants
    }
}
