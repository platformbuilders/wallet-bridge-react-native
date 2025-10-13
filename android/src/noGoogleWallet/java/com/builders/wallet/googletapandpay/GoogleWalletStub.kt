package com.builders.wallet.googletapandpay

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap

/**
 * STUB do Google Wallet - usado quando GOOGLE_WALLET_ENABLED = false
 * 
 * Esta versão NÃO requer o SDK do Google Pay
 * Retorna erros informativos indicando que o SDK não está habilitado
 */
class GoogleWalletImplementation(
    private val reactContext: com.facebook.react.bridge.ReactApplicationContext
) : GoogleWalletContract {

    override fun checkWalletAvailability(promise: Promise) {
        Log.w(TAG, "Google Wallet não está habilitado neste build")
        promise.resolve(false)
    }

    override fun getSecureWalletInfo(promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun getTokenStatus(tokenServiceProvider: Int, tokenReferenceId: String, promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun getEnvironment(promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun isTokenized(
        fpanLastFour: String,
        cardNetwork: Int,
        tokenServiceProvider: Int,
        promise: Promise
    ) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun viewToken(
        tokenServiceProvider: Int,
        issuerTokenId: String,
        promise: Promise
    ) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun createWalletIfNeeded(promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun listTokens(promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun setIntentListener(promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun removeIntentListener(promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun finishActivity(promise: Promise) {
        promise.reject(
            "SDK_NOT_ENABLED",
            "Google Wallet SDK não está habilitado neste build. Configure GOOGLE_WALLET_ENABLED=true no Gradle."
        )
    }

    override fun getConstants(): MutableMap<String, Any> {
        return hashMapOf<String, Any>().apply {
            put("SDK_NAME", "GoogleWalletStub")
            put("SDK_ENABLED", false)
            
            // Google Token Provider - valores placeholder (SDK não disponível)
            put("TOKEN_PROVIDER_AMEX", -1)
            put("TOKEN_PROVIDER_DISCOVER", -1)
            put("TOKEN_PROVIDER_JCB", -1)
            put("TOKEN_PROVIDER_MASTERCARD", -1)
            put("TOKEN_PROVIDER_VISA", -1)
            put("TOKEN_PROVIDER_ELO", -1)
            
            // Google Card Network - valores placeholder
            put("CARD_NETWORK_AMEX", -1)
            put("CARD_NETWORK_DISCOVER", -1)
            put("CARD_NETWORK_MASTERCARD", -1)
            put("CARD_NETWORK_QUICPAY", -1)
            put("CARD_NETWORK_PRIVATE_LABEL", -1)
            put("CARD_NETWORK_VISA", -1)
            put("CARD_NETWORK_ELO", -1)
            
            // Google Token State - valores placeholder
            put("TOKEN_STATE_ACTIVE", -1)
            put("TOKEN_STATE_PENDING", -1)
            put("TOKEN_STATE_SUSPENDED", -1)
            put("TOKEN_STATE_UNTOKENIZED", -1)
            put("TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION", -1)
            put("TOKEN_STATE_FELICA_PENDING_PROVISIONING", -1)
            
            // TapAndPay Status Codes - valores reais (não dependem do SDK)
            put("TAP_AND_PAY_NO_ACTIVE_WALLET", 15002)
            put("TAP_AND_PAY_TOKEN_NOT_FOUND", 15003)
            put("TAP_AND_PAY_INVALID_TOKEN_STATE", 15004)
            put("TAP_AND_PAY_ATTESTATION_ERROR", 15005)
            put("TAP_AND_PAY_UNAVAILABLE", 15009)
            put("TAP_AND_PAY_SAVE_CARD_ERROR", 15019)
            put("TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION", 15021)
            put("TAP_AND_PAY_TOKENIZATION_DECLINED", 15022)
            put("TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR", 15023)
            put("TAP_AND_PAY_TOKENIZE_ERROR", 15024)
            put("TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED", 15025)
            put("TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT", 15026)
            put("TAP_AND_PAY_USER_CANCELED_FLOW", 15027)
            put("TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED", 15028)
            
            // Google Common Status Codes - valores reais (não dependem do SDK)
            put("SUCCESS", 0)
            put("SUCCESS_CACHE", -1)
            put("SERVICE_VERSION_UPDATE_REQUIRED", 2)
            put("SERVICE_DISABLED", 3)
            put("SIGN_IN_REQUIRED", 4)
            put("INVALID_ACCOUNT", 5)
            put("RESOLUTION_REQUIRED", 6)
            put("NETWORK_ERROR", 7)
            put("INTERNAL_ERROR", 8)
            put("DEVELOPER_ERROR", 10)
            put("ERROR", 13)
            put("INTERRUPTED", 14)
            put("TIMEOUT", 15)
            put("CANCELED", 16)
            put("API_NOT_CONNECTED", 17)
            put("REMOTE_EXCEPTION", 19)
            put("CONNECTION_SUSPENDED_DURING_CALL", 20)
            put("RECONNECTION_TIMED_OUT_DURING_UPDATE", 21)
            put("RECONNECTION_TIMED_OUT", 22)
        }
    }

    companion object {
        private const val TAG = "GoogleWallet"
        
        @JvmStatic
        fun processIntent(activity: Activity, intent: Intent) {
            Log.w(TAG, "Google Wallet não está habilitado - processIntent ignorado")
        }
    }
}

