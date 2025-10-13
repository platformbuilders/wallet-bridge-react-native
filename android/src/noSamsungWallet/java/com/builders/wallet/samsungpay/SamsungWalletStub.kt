package com.builders.wallet.samsungpay

import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext

/**
 * STUB do Samsung Wallet - usado quando SAMSUNG_WALLET_ENABLED = false
 * 
 * Esta versão NÃO requer o SDK do Samsung Pay
 * Retorna erros informativos indicando que o SDK não está habilitado
 */
class SamsungWalletImplementation(private val reactContext: ReactApplicationContext) : SamsungWalletContract {

  override fun init(serviceId: String, promise: Promise) {
    Log.w(TAG, "Samsung Wallet não está habilitado neste build")
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun getSamsungPayStatus(promise: Promise) {
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun goToUpdatePage() {
    Log.w(TAG, "Samsung Wallet não está habilitado - goToUpdatePage ignorado")
  }

  override fun activateSamsungPay() {
    Log.w(TAG, "Samsung Wallet não está habilitado - activateSamsungPay ignorado")
  }

  override fun getAllCards(promise: Promise) {
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun getWalletInfo(promise: Promise) {
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun addCard(
    payload: String,
    issuerId: String,
    tokenizationProvider: String,
    cardType: String,
    promise: Promise
  ) {
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun checkWalletAvailability(promise: Promise) {
    Log.w(TAG, "Samsung Wallet não está habilitado neste build")
    promise.resolve(false)
  }

  override fun setIntentListener(promise: Promise) {
    Log.w(TAG, "Samsung Wallet não está habilitado - setIntentListener ignorado")
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun removeIntentListener(promise: Promise) {
    Log.w(TAG, "Samsung Wallet não está habilitado - removeIntentListener ignorado")
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
    Log.w(TAG, "Samsung Wallet não está habilitado - setActivationResult ignorado")
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun finishActivity(promise: Promise) {
    Log.w(TAG, "Samsung Wallet não está habilitado - finishActivity ignorado")
    promise.reject(
      "SDK_NOT_ENABLED",
      "Samsung Wallet SDK não está habilitado neste build. Configure SAMSUNG_WALLET_ENABLED=true no Gradle."
    )
  }

  override fun getConstants(): MutableMap<String, Any> {
    return hashMapOf<String, Any>().apply {
      put("SDK_NAME", "SamsungWalletStub")
      put("SDK_ENABLED", false)
      
      // Samsung Pay Status Codes - valores placeholder (SDK não disponível)
      put("SPAY_READY", 2)
      put("SPAY_NOT_READY", 1)
      put("SPAY_NOT_SUPPORTED", 0)
      put("SPAY_NOT_ALLOWED_TEMPORALLY", 3)
      put("SPAY_HAS_TRANSIT_CARD", 10)
      put("SPAY_HAS_NO_TRANSIT_CARD", 11)
      
      // Samsung Card Types - valores placeholder
      put("CARD_TYPE", "CARD_TYPE")
      put("CARD_TYPE_CREDIT_DEBIT", "PAYMENT")
      put("CARD_TYPE_GIFT", "GIFT")
      put("CARD_TYPE_LOYALTY", "LOYALTY")
      put("CARD_TYPE_CREDIT", "CREDIT")
      put("CARD_TYPE_DEBIT", "DEBIT")
      put("CARD_TYPE_TRANSIT", "TRANSIT")
      put("CARD_TYPE_VACCINE_PASS", "VACCINE_PASS")
      
      // Samsung Card States - valores placeholder
      put("ACTIVE", "ACTIVE")
      put("DISPOSED", "DISPOSED")
      put("EXPIRED", "EXPIRED")
      put("PENDING_ENROLLED", "ENROLLED")
      put("PENDING_PROVISION", "PENDING_PROVISION")
      put("SUSPENDED", "SUSPENDED")
      put("PENDING_ACTIVATION", "PENDING_ACTIVATION")
      
      // Samsung Tokenization Providers - valores placeholder
      put("PROVIDER_VISA", "VI")
      put("PROVIDER_MASTERCARD", "MC")
      put("PROVIDER_AMEX", "AX")
      put("PROVIDER_DISCOVER", "DS")
      put("PROVIDER_PLCC", "PL")
      put("PROVIDER_GIFT", "GI")
      put("PROVIDER_LOYALTY", "LO")
      put("PROVIDER_PAYPAL", "PP")
      put("PROVIDER_GEMALTO", "GT")
      put("PROVIDER_NAPAS", "NP")
      put("PROVIDER_MIR", "MI")
      put("PROVIDER_PAGOBANCOMAT", "PB")
      put("PROVIDER_VACCINE_PASS", "VaccinePass")
      put("PROVIDER_MADA", "MADA")
      put("PROVIDER_ELO", "ELO")
      
      // Samsung Error Codes - valores placeholder
      put("ERROR_NONE", 0)
      put("ERROR_SPAY_INTERNAL", -1)
      put("ERROR_INVALID_INPUT", -2)
      put("ERROR_NOT_SUPPORTED", -3)
      put("ERROR_NOT_FOUND", -4)
      put("ERROR_ALREADY_DONE", -5)
      put("ERROR_NOT_ALLOWED", -6)
      put("ERROR_USER_CANCELED", -7)
      put("ERROR_PARTNER_SDK_API_LEVEL", -10)
      put("ERROR_PARTNER_SERVICE_TYPE", -11)
      put("ERROR_INVALID_PARAMETER", -12)
      put("ERROR_NO_NETWORK", -21)
      put("ERROR_SERVER_NO_RESPONSE", -22)
      put("ERROR_PARTNER_INFO_INVALID", -99)
      put("ERROR_INITIATION_FAIL", -103)
      put("ERROR_REGISTRATION_FAIL", -104)
      put("ERROR_DUPLICATED_SDK_API_CALLED", -105)
      put("ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION", -300)
      put("ERROR_SERVICE_ID_INVALID", -301)
      put("ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION", -302)
      put("ERROR_PARTNER_APP_SIGNATURE_MISMATCH", -303)
      put("ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED", -304)
      put("ERROR_PARTNER_APP_BLOCKED", -305)
      put("ERROR_USER_NOT_REGISTERED_FOR_DEBUG", -306)
      put("ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE", -307)
      put("ERROR_PARTNER_NOT_APPROVED", -308)
      put("ERROR_UNAUTHORIZED_REQUEST_TYPE", -309)
      put("ERROR_EXPIRED_OR_INVALID_DEBUG_KEY", -310)
      put("ERROR_SERVER_INTERNAL", -311)
      put("ERROR_DEVICE_NOT_SAMSUNG", -350)
      put("ERROR_SPAY_PKG_NOT_FOUND", -351)
      put("ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE", -352)
      put("ERROR_DEVICE_INTEGRITY_CHECK_FAIL", -353)
      put("ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL", -360)
      put("ERROR_ANDROID_PLATFORM_CHECK_FAIL", -361)
      put("ERROR_MISSING_INFORMATION", -354)
      put("ERROR_SPAY_SETUP_NOT_COMPLETED", -356)
      put("ERROR_SPAY_APP_NEED_TO_UPDATE", -357)
      put("ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED", -358)
      put("ERROR_UNABLE_TO_VERIFY_CALLER", -359)
      put("ERROR_SPAY_FMM_LOCK", -604)
      put("ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY", -605)
    }
  }

  companion object {
    private const val TAG = "SamsungWallet"
  }
}

