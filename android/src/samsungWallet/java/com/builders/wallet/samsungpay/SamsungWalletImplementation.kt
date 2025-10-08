package com.builders.wallet.samsungpay

import android.os.Bundle
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.builders.wallet.samsungpay.util.PartnerInfoHolder
import com.builders.wallet.samsungpay.util.ErrorCode
import com.builders.wallet.samsungpay.SerializableCard.toSerializable
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo
import com.samsung.android.sdk.samsungpay.v2.card.AddCardListener
import com.samsung.android.sdk.samsungpay.v2.card.GetCardListener

/**
 * Implementação LIMPA do Samsung Wallet - USA DIRETAMENTE O SDK (SEM REFLEXÃO)
 * 
 * Esta versão só é compilada quando SAMSUNG_WALLET_ENABLED = true
 * Requer a dependência: com.samsung.android.sdk.samsungpay:samsungpay
 */
class SamsungWalletImplementation(private val reactContext: ReactApplicationContext) : SamsungWalletContract {

  private var samsungPay: SamsungPay? = null
  private var cardManager: CardManager? = null

  // https://developer.samsung.com/pay/native/common-api.html
  override fun init(serviceId: String, promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> Init started")

    try {
      val partnerInfo = PartnerInfoHolder.getInstance(serviceId).partnerInfo as PartnerInfo
      
      // Código LIMPO - usa diretamente o SDK!
      samsungPay = SamsungPay(reactContext, partnerInfo)
      cardManager = CardManager(reactContext, partnerInfo)
      
      Log.i(TAG, "- $TAG initialized")
      promise.resolve(true)
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro na inicialização: ${e.message}", e)
      promise.reject("INIT_ERROR", e.message, e)
    }
  }

  // https://developer.samsung.com/pay/native/common-api.html
  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#getSamsungPayStatus(com.samsung.android.sdk.samsungpay.v2.StatusListener)
  override fun getSamsungPayStatus(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getSamsungPayStatus started")
    
    if (samsungPay == null) {
      promise.reject("NOT_INITIALIZED", "Samsung Pay não foi inicializado. Chame init() primeiro.")
      return
    }

    // Código LIMPO!
    val listener = object : StatusListener {
      override fun onSuccess(status: Int, bundle: Bundle) {
        Log.d(TAG, "onSuccess callback is called, status=$status, bundle:$bundle")
        
        val extraErrorReason = bundle.getInt(SpaySdk.EXTRA_ERROR_REASON, SpaySdk.ERROR_NONE)
        val statusName = when (status) {
          SpaySdk.SPAY_READY -> "SPAY_READY"
          SpaySdk.SPAY_NOT_READY -> "SPAY_NOT_READY"
          SpaySdk.SPAY_NOT_SUPPORTED -> "SPAY_NOT_SUPPORTED"
          SpaySdk.SPAY_NOT_ALLOWED_TEMPORALLY -> "SPAY_NOT_ALLOWED_TEMPORALLY"
          SpaySdk.SPAY_HAS_TRANSIT_CARD -> "SPAY_HAS_TRANSIT_CARD"
          SpaySdk.SPAY_HAS_NO_TRANSIT_CARD -> "SPAY_HAS_NO_TRANSIT_CARD"
          else -> "UNKNOWN_STATUS_$status"
        }
        
        Log.i(TAG, "- Samsung Pay Status: $statusName ($status)")
        
        if (status == SpaySdk.SPAY_READY) {
          Log.i(TAG, "- Samsung Pay está pronto para uso")
          promise.resolve(status)
        } else {
          val error = if (extraErrorReason != SpaySdk.ERROR_NONE) {
            ErrorCode.getErrorCodeName(extraErrorReason)
          } else {
            "Samsung Pay não está pronto (Status: $statusName)"
          }
          Log.w(TAG, "Samsung Pay não está disponível: $error")
          promise.reject(status.toString(), error)
        }
      }

      override fun onFail(errorCode: Int, bundle: Bundle) {
        Log.d(TAG, "onFail callback is called, errorCode=$errorCode, bundle:$bundle")
        
        val extraErrorReason = bundle.getInt(SpaySdk.EXTRA_ERROR_REASON, SpaySdk.ERROR_NONE)
        val error = if (extraErrorReason != SpaySdk.ERROR_NONE) {
          ErrorCode.getErrorCodeName(extraErrorReason)
        } else {
          ErrorCode.getErrorCodeName(errorCode)
        }
        
        Log.e(TAG, "Erro ao verificar status do Samsung Pay: $error")
        promise.reject(errorCode.toString(), error)
      }
    }

    samsungPay!!.getSamsungPayStatus(listener)
  }

  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#goToUpdatePage()
  override fun goToUpdatePage() {
    Log.i(TAG, "--")
    Log.i(TAG, "> goToUpdatePage started")
    samsungPay?.goToUpdatePage()
  }

  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#activateSamsungPay()
  override fun activateSamsungPay() {
    Log.i(TAG, "--")
    Log.i(TAG, "> activateSamsungPay started")
    samsungPay?.activateSamsungPay()
  }

  // https://developer.samsung.com/pay/native/apptoapp.html
  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/card/CardManager.html#getAllCards(android.os.Bundle,com.samsung.android.sdk.samsungpay.v2.card.GetCardListener)
  override fun getAllCards(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getAllCards started")
    
    if (cardManager == null) {
      promise.reject("NOT_INITIALIZED", "Samsung Pay não foi inicializado. Chame init() primeiro.")
      return
    }

    // Código LIMPO!
    val listener = object : GetCardListener {
      override fun onSuccess(cardList: List<Card>) {
        Log.d(TAG, "onSuccess callback is called, list.size= ${cardList.size}")
        val result = cardList.map { it.toSerializable() }
        Log.i(TAG, "- cards - $result")
        promise.resolve(result)
      }

      override fun onFail(errorCode: Int, errorData: Bundle) {
        Log.d(TAG, "onFail callback is called, errorCode:$errorCode")
        val error = ErrorCode.getErrorCodeName(errorCode)
        Log.e(TAG, "Error when getting all cards: $error")
        promise.reject(errorCode.toString(), error)
      }
    }

    cardManager!!.getAllCards(null, listener)
  }

  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#getWalletInfo(java.util.List,com.samsung.android.sdk.samsungpay.v2.StatusListener)
  override fun getWalletInfo(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getWalletInfo started")
    
    if (samsungPay == null) {
      promise.reject("NOT_INITIALIZED", "Samsung Pay não foi inicializado. Chame init() primeiro.")
      return
    }

    // Código LIMPO!
    val keys = arrayListOf(
      SpaySdk.WALLET_USER_ID,
      SpaySdk.DEVICE_ID,
      SpaySdk.WALLET_DM_ID
    )

    Log.d(TAG, "Chaves solicitadas para getWalletInfo: $keys")

    val listener = object : StatusListener {
      override fun onSuccess(status: Int, walletData: Bundle) {
        Log.d(TAG, "onSuccess callback is called, status=$status, walletData=$walletData")
        
        val deviceId = walletData.getString(SpaySdk.DEVICE_ID)
        val walletUserId = walletData.getString(SpaySdk.WALLET_USER_ID)
        val walletDmId = walletData.getString(SpaySdk.WALLET_DM_ID)
        
        Log.d(TAG, "Device ID: $deviceId")
        Log.d(TAG, "Wallet User ID: $walletUserId")
        Log.d(TAG, "Wallet DM ID: $walletDmId")
        
        val result: WritableMap = Arguments.createMap().apply {
          putString("walletDMId", walletDmId)
          putString("deviceId", deviceId)
          putString("walletUserId", walletUserId)
        }
        Log.i(TAG, "- Wallet Info obtido com sucesso: $result")
        promise.resolve(result)
      }

      override fun onFail(errorCode: Int, errorData: Bundle) {
        Log.d(TAG, "onFail callback is called, errorCode=$errorCode, errorData=$errorData")
        
        val error = ErrorCode.getErrorCodeName(errorCode)
        Log.e(TAG, "Error when getting wallet info: $error")
        promise.reject(errorCode.toString(), error)
      }
    }

    samsungPay!!.getWalletInfo(keys, listener)
  }

  // https://developer.samsung.com/pay/api-reference/com/samsung.android.sdk.samsungpay.v2/card/CardManager.html#addCard(com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo,com.samsung.android.sdk.samsungpay.v2.card.AddCardListener)
  override fun addCard(
    payload: String,
    issuerId: String,
    tokenizationProvider: String,
    cardType: String,
    progress: Callback,
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> addCard started")
    Log.d(
      TAG,
      "addCard payload : $payload, issuerId : $issuerId, tokenizationProvider : $tokenizationProvider, cardType : $cardType"
    )

    if (cardManager == null) {
      promise.reject("NOT_INITIALIZED", "Samsung Pay não foi inicializado. Chame init() primeiro.")
      return
    }

    // Código LIMPO!
    val cardDetail = Bundle().apply {
      putString(AddCardInfo.EXTRA_PROVISION_PAYLOAD, payload)
    }
    
    if (tokenizationProvider == AddCardInfo.PROVIDER_ELO) {
      cardDetail.putString(AddCardInfo.EXTRA_ISSUER_ID, issuerId)
    }
    
    val addCardInfo = AddCardInfo(cardType, tokenizationProvider, cardDetail)

    val listener = object : AddCardListener {
      override fun onSuccess(status: Int, card: Card) {
        Log.d(TAG, "doAddCard onSuccess callback is called")
        Log.i(TAG, "card added - $card")
        promise.resolve(card.toSerializable())
      }

      override fun onFail(errorCode: Int, errorData: Bundle) {
        Log.d(TAG, "doAddCard onFail callback is called, errorCode:$errorCode")
        val message = if (errorData.containsKey(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)) {
          val error = errorData.getString(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)
          Log.e(TAG, "doAddCard onFail extra reason message: $error")
          error
        } else {
          ErrorCode.getErrorCodeName(errorCode)
        }
        Log.e(TAG, "Error when adding card: $message")
        promise.reject(errorCode.toString(), message)
      }

      override fun onProgress(currentCount: Int, totalCount: Int, errorData: Bundle) {
        Log.d(TAG, "doAddCard onProgress : $currentCount / $totalCount")
        progress.invoke(currentCount, totalCount)
      }
    }

    cardManager!!.addCard(addCardInfo, listener)
  }

  // Método de compatibilidade com a API anterior
  // https://developer.samsung.com/pay/native/common-api.html
  override fun checkWalletAvailability(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> checkWalletAvailability started")
    
    if (samsungPay == null) {
      promise.reject("NOT_INITIALIZED", "Samsung Pay não foi inicializado. Chame init() primeiro.")
      return
    }

    // Código LIMPO!
    val listener = object : StatusListener {
      override fun onSuccess(status: Int, bundle: Bundle) {
        Log.d(TAG, "onSuccess callback is called, status=$status, bundle:$bundle")
        val isAvailable = status == SpaySdk.SPAY_READY
        Log.i(TAG, "- Samsung Pay disponível: $isAvailable (Status: $status)")
        promise.resolve(isAvailable)
      }

      override fun onFail(errorCode: Int, bundle: Bundle) {
        Log.d(TAG, "onFail callback is called, errorCode=$errorCode, bundle:$bundle")
        val error = ErrorCode.getErrorCodeName(errorCode)
        Log.e(TAG, "Erro ao verificar disponibilidade do Samsung Pay: $error")
        promise.resolve(false)
      }
    }

    samsungPay!!.getSamsungPayStatus(listener)
  }

  override fun getConstants(): MutableMap<String, Any> {
    val constants = hashMapOf<String, Any>()
    
    // SDK Info
    constants["SDK_NAME"] = "SamsungWallet"
    
    // Usar diretamente as constantes do SDK!
    constants["SPAY_READY"] = SpaySdk.SPAY_READY
    constants["SPAY_NOT_READY"] = SpaySdk.SPAY_NOT_READY
    constants["SPAY_NOT_SUPPORTED"] = SpaySdk.SPAY_NOT_SUPPORTED
    constants["SPAY_NOT_ALLOWED_TEMPORALLY"] = SpaySdk.SPAY_NOT_ALLOWED_TEMPORALLY
    constants["SPAY_HAS_TRANSIT_CARD"] = SpaySdk.SPAY_HAS_TRANSIT_CARD
    constants["SPAY_HAS_NO_TRANSIT_CARD"] = SpaySdk.SPAY_HAS_NO_TRANSIT_CARD
    
    constants["CARD_TYPE"] = Card.CARD_TYPE
    constants["CARD_TYPE_CREDIT_DEBIT"] = Card.CARD_TYPE_CREDIT_DEBIT
    constants["CARD_TYPE_GIFT"] = Card.CARD_TYPE_GIFT
    constants["CARD_TYPE_LOYALTY"] = Card.CARD_TYPE_LOYALTY
    constants["CARD_TYPE_CREDIT"] = Card.CARD_TYPE_CREDIT
    constants["CARD_TYPE_DEBIT"] = Card.CARD_TYPE_DEBIT
    constants["CARD_TYPE_TRANSIT"] = Card.CARD_TYPE_TRANSIT
    constants["CARD_TYPE_VACCINE_PASS"] = Card.CARD_TYPE_VACCINE_PASS
    
    constants["ACTIVE"] = Card.ACTIVE
    constants["DISPOSED"] = Card.DISPOSED
    constants["EXPIRED"] = Card.EXPIRED
    constants["PENDING_ENROLLED"] = Card.PENDING_ENROLLED
    constants["PENDING_PROVISION"] = Card.PENDING_PROVISION
    constants["SUSPENDED"] = Card.SUSPENDED
    constants["PENDING_ACTIVATION"] = Card.PENDING_ACTIVATION
    
    constants["PROVIDER_VISA"] = AddCardInfo.PROVIDER_VISA
    constants["PROVIDER_MASTERCARD"] = AddCardInfo.PROVIDER_MASTERCARD
    constants["PROVIDER_AMEX"] = AddCardInfo.PROVIDER_AMEX
    constants["PROVIDER_DISCOVER"] = AddCardInfo.PROVIDER_DISCOVER
    constants["PROVIDER_PLCC"] = AddCardInfo.PROVIDER_PLCC
    constants["PROVIDER_GIFT"] = AddCardInfo.PROVIDER_GIFT
    constants["PROVIDER_LOYALTY"] = AddCardInfo.PROVIDER_LOYALTY
    constants["PROVIDER_PAYPAL"] = AddCardInfo.PROVIDER_PAYPAL
    constants["PROVIDER_GEMALTO"] = AddCardInfo.PROVIDER_GEMALTO
    constants["PROVIDER_NAPAS"] = AddCardInfo.PROVIDER_NAPAS
    constants["PROVIDER_MIR"] = AddCardInfo.PROVIDER_MIR
    constants["PROVIDER_PAGOBANCOMAT"] = AddCardInfo.PROVIDER_PAGOBANCOMAT
    constants["PROVIDER_VACCINE_PASS"] = AddCardInfo.PROVIDER_VACCINE_PASS
    constants["PROVIDER_MADA"] = AddCardInfo.PROVIDER_MADA
    constants["PROVIDER_ELO"] = AddCardInfo.PROVIDER_ELO
    
    constants["ERROR_NONE"] = SpaySdk.ERROR_NONE
    constants["ERROR_SPAY_INTERNAL"] = SpaySdk.ERROR_SPAY_INTERNAL
    constants["ERROR_INVALID_INPUT"] = SpaySdk.ERROR_INVALID_INPUT
    constants["ERROR_NOT_SUPPORTED"] = SpaySdk.ERROR_NOT_SUPPORTED
    constants["ERROR_NOT_FOUND"] = SpaySdk.ERROR_NOT_FOUND
    constants["ERROR_ALREADY_DONE"] = SpaySdk.ERROR_ALREADY_DONE
    constants["ERROR_NOT_ALLOWED"] = SpaySdk.ERROR_NOT_ALLOWED
    constants["ERROR_USER_CANCELED"] = SpaySdk.ERROR_USER_CANCELED
    constants["ERROR_PARTNER_SDK_API_LEVEL"] = SpaySdk.ERROR_PARTNER_SDK_API_LEVEL
    constants["ERROR_PARTNER_SERVICE_TYPE"] = SpaySdk.ERROR_PARTNER_SERVICE_TYPE
    constants["ERROR_INVALID_PARAMETER"] = SpaySdk.ERROR_INVALID_PARAMETER
    constants["ERROR_NO_NETWORK"] = SpaySdk.ERROR_NO_NETWORK
    constants["ERROR_SERVER_NO_RESPONSE"] = SpaySdk.ERROR_SERVER_NO_RESPONSE
    constants["ERROR_PARTNER_INFO_INVALID"] = SpaySdk.ERROR_PARTNER_INFO_INVALID
    constants["ERROR_INITIATION_FAIL"] = SpaySdk.ERROR_INITIATION_FAIL
    constants["ERROR_REGISTRATION_FAIL"] = SpaySdk.ERROR_REGISTRATION_FAIL
    constants["ERROR_DUPLICATED_SDK_API_CALLED"] = SpaySdk.ERROR_DUPLICATED_SDK_API_CALLED
    constants["ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION"] = SpaySdk.ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION
    constants["ERROR_SERVICE_ID_INVALID"] = SpaySdk.ERROR_SERVICE_ID_INVALID
    constants["ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION"] = SpaySdk.ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION
    constants["ERROR_PARTNER_APP_SIGNATURE_MISMATCH"] = SpaySdk.ERROR_PARTNER_APP_SIGNATURE_MISMATCH
    constants["ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED"] = SpaySdk.ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED
    constants["ERROR_PARTNER_APP_BLOCKED"] = SpaySdk.ERROR_PARTNER_APP_BLOCKED
    constants["ERROR_USER_NOT_REGISTERED_FOR_DEBUG"] = SpaySdk.ERROR_USER_NOT_REGISTERED_FOR_DEBUG
    constants["ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE"] = SpaySdk.ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE
    constants["ERROR_PARTNER_NOT_APPROVED"] = SpaySdk.ERROR_PARTNER_NOT_APPROVED
    constants["ERROR_UNAUTHORIZED_REQUEST_TYPE"] = SpaySdk.ERROR_UNAUTHORIZED_REQUEST_TYPE
    constants["ERROR_EXPIRED_OR_INVALID_DEBUG_KEY"] = SpaySdk.ERROR_EXPIRED_OR_INVALID_DEBUG_KEY
    constants["ERROR_SERVER_INTERNAL"] = SpaySdk.ERROR_SERVER_INTERNAL
    constants["ERROR_DEVICE_NOT_SAMSUNG"] = SpaySdk.ERROR_DEVICE_NOT_SAMSUNG
    constants["ERROR_SPAY_PKG_NOT_FOUND"] = SpaySdk.ERROR_SPAY_PKG_NOT_FOUND
    constants["ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE"] = SpaySdk.ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE
    constants["ERROR_DEVICE_INTEGRITY_CHECK_FAIL"] = SpaySdk.ERROR_DEVICE_INTEGRITY_CHECK_FAIL
    constants["ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL"] = SpaySdk.ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL
    constants["ERROR_ANDROID_PLATFORM_CHECK_FAIL"] = SpaySdk.ERROR_ANDROID_PLATFORM_CHECK_FAIL
    constants["ERROR_MISSING_INFORMATION"] = SpaySdk.ERROR_MISSING_INFORMATION
    constants["ERROR_SPAY_SETUP_NOT_COMPLETED"] = SpaySdk.ERROR_SPAY_SETUP_NOT_COMPLETED
    constants["ERROR_SPAY_APP_NEED_TO_UPDATE"] = SpaySdk.ERROR_SPAY_APP_NEED_TO_UPDATE
    constants["ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED"] = SpaySdk.ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED
    constants["ERROR_UNABLE_TO_VERIFY_CALLER"] = SpaySdk.ERROR_UNABLE_TO_VERIFY_CALLER
    constants["ERROR_SPAY_FMM_LOCK"] = SpaySdk.ERROR_SPAY_FMM_LOCK
    constants["ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY"] = SpaySdk.ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY
    
    return constants
  }

  companion object {
    private const val TAG = "SamsungWallet"
  }
}

