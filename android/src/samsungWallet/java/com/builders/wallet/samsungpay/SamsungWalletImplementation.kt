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
import com.builders.wallet.WalletOpener
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
  private var activity: android.app.Activity? = null
  private var intentListenerActive: Boolean = false
  private var walletOpener: WalletOpener? = null

  init {
    // Inicializar WalletOpener
    walletOpener = WalletOpener(reactContext)
  }

  // https://developer.samsung.com/pay/native/common-api.html
  override fun init(serviceId: String, promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> Init started")

    try {
      val bundle = Bundle()
      bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.APP2APP.toString())
      val partnerInfo = PartnerInfo(serviceId, bundle)

      // Código LIMPO - usa diretamente o SDK!
      samsungPay = SamsungPay(reactContext, partnerInfo)
      cardManager = CardManager(reactContext, partnerInfo)

      Log.i(TAG, "- $TAG initialized")
      promise.resolve(true)
    } catch (e: Exception) {
      Log.e(TAG, "INIT_ERROR: ${e.message}", e)
      promise.reject("INIT_ERROR", e.message, e)
    }
  }

  // https://developer.samsung.com/pay/native/common-api.html
  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#getSamsungPayStatus(com.samsung.android.sdk.samsungpay.v2.StatusListener)
  override fun getSamsungPayStatus(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getSamsungPayStatus started")

    if (samsungPay == null) {
      Log.w(TAG, "NOT_INITIALIZED: Samsung Pay não foi inicializado. Chame init() primeiro.")
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
          // Priorizar EXTRA_ERROR_REASON se disponível (mais específico)
          val errorCode = if (extraErrorReason != SpaySdk.ERROR_NONE) {
            extraErrorReason
          } else {
            status
          }
          val errorCodeName = ErrorCode.getErrorCodeName(errorCode)
          val errorMessage = if (extraErrorReason != SpaySdk.ERROR_NONE) {
            ErrorCode.getErrorMessage(extraErrorReason)
          } else {
            "Samsung Pay não está pronto (Status: $statusName)"
          }
          val formattedErrorMessage = "$errorMessage ($errorCodeName) - result_code:$errorCode"

          Log.w(TAG, "SAMSUNG_PAY_NOT_READY: $formattedErrorMessage")
          promise.reject("SAMSUNG_PAY_NOT_READY", formattedErrorMessage)
        }
      }

      override fun onFail(errorCode: Int, bundle: Bundle) {
        Log.d(TAG, "onFail callback is called, errorCode=$errorCode, bundle:$bundle")

        val extraErrorReason = bundle.getInt(SpaySdk.EXTRA_ERROR_REASON, SpaySdk.ERROR_NONE)
        val actualErrorCode = if (extraErrorReason != SpaySdk.ERROR_NONE) extraErrorReason else errorCode
        val errorCodeName = ErrorCode.getErrorCodeName(actualErrorCode)
        val errorMessage = ErrorCode.getErrorMessage(actualErrorCode)
        val formattedErrorMessage = "$errorMessage ($errorCodeName) - result_code:$actualErrorCode"

        Log.e(TAG, "SAMSUNG_PAY_STATUS_ERROR: $formattedErrorMessage")
        promise.reject("SAMSUNG_PAY_STATUS_ERROR", formattedErrorMessage)
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
      Log.w(TAG, "NOT_INITIALIZED: Samsung Pay não foi inicializado. Chame init() primeiro.")
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
        val extraErrorReason = errorData.getInt(SpaySdk.EXTRA_ERROR_REASON, SpaySdk.ERROR_NONE)
        val actualErrorCode = if (extraErrorReason != SpaySdk.ERROR_NONE) extraErrorReason else errorCode
        val errorCodeName = ErrorCode.getErrorCodeName(actualErrorCode)
        val errorMessage = ErrorCode.getErrorMessage(actualErrorCode)
        val formattedErrorMessage = "$errorMessage ($errorCodeName) - result_code:$actualErrorCode"

        Log.e(TAG, "GET_ALL_CARDS_ERROR: $formattedErrorMessage")
        promise.reject("GET_ALL_CARDS_ERROR", formattedErrorMessage)
      }
    }

    cardManager!!.getAllCards(null, listener)
  }

  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#getWalletInfo(java.util.List,com.samsung.android.sdk.samsungpay.v2.StatusListener)
  override fun getWalletInfo(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getWalletInfo started")

    if (samsungPay == null) {
      Log.w(TAG, "NOT_INITIALIZED: Samsung Pay não foi inicializado. Chame init() primeiro.")
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
        val extraErrorReason = errorData.getInt(SpaySdk.EXTRA_ERROR_REASON, SpaySdk.ERROR_NONE)
        val actualErrorCode = if (extraErrorReason != SpaySdk.ERROR_NONE) extraErrorReason else errorCode
        val errorCodeName = ErrorCode.getErrorCodeName(actualErrorCode)
        val errorMessage = ErrorCode.getErrorMessage(actualErrorCode)
        val formattedErrorMessage = "$errorMessage ($errorCodeName) - result_code:$actualErrorCode"

        Log.e(TAG, "GET_WALLET_INFO_ERROR: $formattedErrorMessage")
        promise.reject("GET_WALLET_INFO_ERROR", formattedErrorMessage)
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
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> addCard started")
    Log.d(
      TAG,
      "addCard payload : $payload, issuerId : $issuerId, tokenizationProvider : $tokenizationProvider, cardType : $cardType"
    )

    if (cardManager == null) {
      Log.w(TAG, "NOT_INITIALIZED: Samsung Pay não foi inicializado. Chame init() primeiro.")
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

        // Priorizar EXTRA_ERROR_REASON (código mais específico)
        val extraErrorReason = errorData.getInt(SpaySdk.EXTRA_ERROR_REASON, SpaySdk.ERROR_NONE)
        val actualErrorCode = if (extraErrorReason != SpaySdk.ERROR_NONE) extraErrorReason else errorCode
        val errorCodeName = ErrorCode.getErrorCodeName(actualErrorCode)

        // Priorizar EXTRA_ERROR_REASON_MESSAGE (mensagem mais específica)
        val errorMessage = if (errorData.containsKey(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)) {
          val error = errorData.getString(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)
          Log.e(TAG, "doAddCard onFail extra reason message: $error")
          error ?: ErrorCode.getErrorMessage(actualErrorCode)
        } else {
          ErrorCode.getErrorMessage(actualErrorCode)
        }
        val formattedErrorMessage = "$errorMessage ($errorCodeName) - result_code:$actualErrorCode"

        Log.e(TAG, "ADD_CARD_ERROR: $formattedErrorMessage")
        promise.reject("ADD_CARD_ERROR", formattedErrorMessage)
      }

      override fun onProgress(currentCount: Int, totalCount: Int, errorData: Bundle) {
        Log.d(TAG, "doAddCard onProgress : $currentCount / $totalCount")
        // Progress callback removido - não é compatível com Promise no React Native Bridge
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
      Log.w(TAG, "NOT_INITIALIZED: Samsung Pay não foi inicializado. Chame init() primeiro.")
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
        val extraErrorReason = bundle.getInt(SpaySdk.EXTRA_ERROR_REASON, SpaySdk.ERROR_NONE)
        val actualErrorCode = if (extraErrorReason != SpaySdk.ERROR_NONE) extraErrorReason else errorCode
        val errorCodeName = ErrorCode.getErrorCodeName(actualErrorCode)
        val errorMessage = ErrorCode.getErrorMessage(actualErrorCode)
        val formattedErrorMessage = "$errorMessage ($errorCodeName) - result_code:$actualErrorCode"

        Log.e(TAG, "SAMSUNG_PAY_NOT_READY: $formattedErrorMessage")
        // Nota: Este método resolve com false em vez de rejeitar para manter compatibilidade
        promise.resolve(false)
      }
    }

    samsungPay!!.getSamsungPayStatus(listener)
  }

  // https://developer.samsung.com/pay/ID&V/implementing-app2app-id&v.html
  override fun setIntentListener(promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] setIntentListener chamado")
    try {
      intentListenerActive = true
      checkPendingDataFromMainActivity()
      
      // Processar eventos de nenhuma intent pendentes
      SamsungWalletModule.processNoIntentReceivedEvent(reactContext)
      
      promise.resolve(true)
    } catch (e: Exception) {
      Log.e(TAG, "SET_INTENT_LISTENER_ERROR: ${e.message}", e)
      promise.reject("SET_INTENT_LISTENER_ERROR", e.message, e)
    }
  }

  override fun removeIntentListener(promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] removeIntentListener chamado")
    try {
      intentListenerActive = false
      promise.resolve(true)
    } catch (e: Exception) {
      Log.e(TAG, "REMOVE_INTENT_LISTENER_ERROR: ${e.message}", e)
      promise.reject("REMOVE_INTENT_LISTENER_ERROR", e.message, e)
    }
  }


  // https://developer.samsung.com/pay/ID&V/implementing-app2app-id&v.html
  override fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] setActivationResult chamado - Status: $status")
    try {
      activity = reactContext.currentActivity
      if (activity == null) {
        Log.w(TAG, "NO_ACTIVITY: Nenhuma atividade disponível")
        promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
        return
      }

      val validStatuses = listOf("accepted", "declined", "failure", "appNotReady")
      if (!validStatuses.contains(status)) {
        Log.w(TAG, "INVALID_STATUS: Status deve ser: accepted, declined, failure ou appNotReady")
        promise.reject("INVALID_STATUS", "Status deve ser: accepted, declined, failure ou appNotReady")
        return
      }

      val resultIntent = android.content.Intent()
      resultIntent.putExtra("STEP_UP_RESPONSE", status)

      if (activationCode != null && activationCode.isNotEmpty() && status == "accepted") {
        resultIntent.putExtra("ACTIVATION_CODE", activationCode)
      }

      activity?.setResult(android.app.Activity.RESULT_OK, resultIntent)
      promise.resolve(true)
    } catch (e: Exception) {
      Log.e(TAG, "SET_ACTIVATION_RESULT_ERROR: ${e.message}", e)
      promise.reject("SET_ACTIVATION_RESULT_ERROR", e.message, e)
    }
  }

  override fun finishActivity(promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] finishActivity chamado")
    try {
      activity = reactContext.currentActivity
      if (activity == null) {
        Log.w(TAG, "NO_ACTIVITY: Nenhuma atividade disponível")
        promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
        return
      }
      activity?.finish()
      promise.resolve(true)
    } catch (e: Exception) {
      Log.e(TAG, "FINISH_ACTIVITY_ERROR: ${e.message}", e)
      promise.reject("FINISH_ACTIVITY_ERROR", e.message, e)
    }
  }

  private fun checkPendingDataFromMainActivity() {
    Log.d(TAG, "🔍 [SAMSUNG] Verificando dados pendentes...")
    try {
      // Verificar se há dados pendentes
      val hasData = hasPendingData()
      
      if (hasData) {
        Log.d(TAG, "✅ [SAMSUNG] Dados pendentes encontrados")
        
        // Obter os dados pendentes sem limpar
        val data = getPendingIntentDataWithoutClearing()
        val action = getPendingIntentAction()
        val callingPackage = getPendingCallingPackage()
        
        if (data != null && data.isNotEmpty()) {
          Log.d(TAG, "📋 [SAMSUNG] Processando dados pendentes: ${data.length} caracteres")
          Log.d(TAG, "📋 [SAMSUNG] Action: $action, CallingPackage: $callingPackage")
          
          // Verificar se action e callingPackage estão disponíveis
          if (action == null) {
            Log.e(TAG, "❌ [SAMSUNG] Action é null - não é possível processar intent")
            return
          }
          
          if (callingPackage == null) {
            Log.e(TAG, "❌ [SAMSUNG] CallingPackage é null - não é possível processar intent")
            return
          }
          
          // Processar os dados como um intent usando os valores reais
          processSamsungWalletIntentData(data, action, callingPackage)
          
          // Limpar dados após processamento bem-sucedido
          clearPendingData()
        } else {
          Log.w(TAG, "⚠️ [SAMSUNG] Dados pendentes são null ou vazios")
        }
      } else {
        Log.d(TAG, "🔍 [SAMSUNG] Nenhum dado pendente")
      }
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro ao verificar dados pendentes: ${e.message}", e)
    }
  }

  override fun getConstants(): MutableMap<String, Any> {
    val constants = hashMapOf<String, Any>()

    // SDK Info
    constants["SDK_NAME"] = "SamsungWallet"
    constants["SAMSUNG_PAY_PACKAGE"] = SAMSUNG_PAY_PACKAGE
    constants["SAMSUNG_PAY_PLAY_STORE_URL"] = SAMSUNG_PAY_PLAY_STORE_URL

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

  /**
   * Processa dados específicos da Samsung Wallet
   */
  private fun processSamsungWalletIntentData(data: String, action: String, callingPackage: String) {
    Log.d(TAG, "🔍 [SAMSUNG] processSamsungWalletIntentData chamado")
    try {
      Log.d(TAG, "✅ [SAMSUNG] Processando dados Samsung Wallet: ${data.length} caracteres")

      // Determinar o tipo de intent baseado na action
      val intentType = if (action.endsWith(".action.LAUNCH_A2A_IDV")) {
        "LAUNCH_A2A_IDV"
      } else {
        "WALLET_INTENT"
      }

      // Processar dados específicos (Mastercard/Visa)
      val processedData = processSamsungWalletData(data)

      Log.d(TAG, "🔍 [SAMSUNG] Dados processados - CardType: ${processedData["cardType"]}, Format: ${processedData["dataFormat"]}")

      // Decodificar dados de base64 para string normal
      var decodedData = data
      var dataFormat = "raw"

      try {
        // Tentar decodificar como base64
        val decodedBytes = android.util.Base64.decode(data, android.util.Base64.DEFAULT)
        decodedData = String(decodedBytes, Charsets.UTF_8)
        dataFormat = "base64_decoded"
        Log.d(TAG, "🔍 [SAMSUNG] Dados decodificados com sucesso: ${decodedData.length} caracteres")
      } catch (e: Exception) {
        // Se falhar ao decodificar, usar dados originais
        Log.w(TAG, "⚠️ [SAMSUNG] Não foi possível decodificar como base64, usando dados originais: ${e.message}")
        dataFormat = "raw"
      }

      val eventData = Arguments.createMap()
      eventData.putString("action", action)
      eventData.putString("type", intentType)
      eventData.putString("data", decodedData)
      eventData.putString("dataFormat", dataFormat)
      eventData.putString("callingPackage", callingPackage)

      // Adicionar dados originais em base64 para referência
      eventData.putString("originalData", data)

      Log.d(TAG, "🔍 [SAMSUNG] Evento preparado - Action: $action, Type: $intentType, Format: $dataFormat")

      // Enviar evento para React Native
      sendEventToReactNative("SamsungWalletIntentReceived", eventData)

      Log.d(TAG, "✅ [SAMSUNG] Dados Samsung Wallet processados com sucesso")

    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro ao processar dados Samsung Wallet: ${e.message}", e)
    }
  }

  /**
   * Envia evento para React Native
   */
  private fun sendEventToReactNative(eventName: String, eventData: WritableMap?) {
    try {
      Log.d(TAG, "🔍 [SAMSUNG] Enviando evento para React Native: $eventName")
      reactContext
        .getJSModule(com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(eventName, eventData)
      Log.d(TAG, "✅ [SAMSUNG] Evento enviado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro ao enviar evento para React Native: ${e.message}", e)
    }
  }


    companion object {
      private const val TAG = "SamsungWallet"

      private const val SAMSUNG_PAY_PACKAGE = "com.samsung.android.spay"
      private val SAMSUNG_PAY_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=$SAMSUNG_PAY_PACKAGE&hl=pt_BR"
    
    // Variáveis estáticas para armazenar dados da intent
    @Volatile
    private var pendingIntentData: String? = null
    @Volatile
    private var pendingIntentAction: String? = null
    @Volatile
    private var pendingCallingPackage: String? = null
    
    // Flag para indicar se há dados pendentes
    @Volatile
    private var hasPendingIntentData: Boolean = false
    
    @JvmStatic
    fun getPendingIntentData(): String? {
      val data = pendingIntentData
      if (data != null) {
        // Limpar dados após leitura
        pendingIntentData = null
        pendingIntentAction = null
        pendingCallingPackage = null
        hasPendingIntentData = false
      }
      return data
    }
    
    @JvmStatic
    fun getPendingIntentAction(): String? = pendingIntentAction
    
    @JvmStatic
    fun getPendingCallingPackage(): String? = pendingCallingPackage
    
    @JvmStatic
    fun getPendingIntentDataWithoutClearing(): String? = pendingIntentData
    
    @JvmStatic
    fun clearPendingData() {
      pendingIntentData = null
      pendingIntentAction = null
      pendingCallingPackage = null
      hasPendingIntentData = false
    }
    
    @JvmStatic
    fun hasPendingData(): Boolean = hasPendingIntentData

    @JvmStatic
    fun processIntent(activity: android.app.Activity, intent: android.content.Intent) {
      Log.d(TAG, "🔍 [SAMSUNG] processIntent chamado")
      
      Log.d(TAG, "🔍 [SAMSUNG] Intent encontrada: ${intent.action}")
      
      // Verificar se é um intent do Samsung Pay/Wallet
      if (isSamsungPayIntent(intent)) {
        Log.d(TAG, "✅ [SAMSUNG] Intent do Samsung Pay detectada")
        
        // Extrair dados da intent
        val extraText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
        if (!extraText.isNullOrEmpty()) {
          Log.d(TAG, "🔍 [SAMSUNG] Dados EXTRA_TEXT encontrados: ${extraText.length} caracteres")
          
          // Armazenar dados para processamento posterior
          pendingIntentData = extraText
          pendingIntentAction = intent.action
          pendingCallingPackage = activity.callingPackage
          hasPendingIntentData = true
          
          Log.d(TAG, "✅ [SAMSUNG] Dados armazenados para processamento - Action: ${intent.action}, CallingPackage: ${activity.callingPackage}")
          
          // Limpar intent para evitar reprocessamento
          activity.intent = android.content.Intent()
        } else {
          Log.w(TAG, "⚠️ [SAMSUNG] Nenhum dado EXTRA_TEXT encontrado")
        }
      } else {
        Log.d(TAG, "🔍 [SAMSUNG] Intent não relacionada ao Samsung Pay")
      }
    }




    /**
     * Verifica se uma intent é relacionada ao Samsung Pay/Wallet
     */
    private fun isSamsungPayIntent(intent: android.content.Intent): Boolean {
      val action = intent.action
      Log.d(TAG, "🔍 [SAMSUNG] Verificando intent - Action: $action")

      // Verificar action
      val isValidAction = action != null && (
        action.endsWith(".action.LAUNCH_A2A_IDV")
      )
      return isValidAction
    }

    /**
     * Verifica se o chamador é válido (Samsung Pay)
     */
    @JvmStatic
    fun isValidCallingPackage(activity: android.app.Activity): Boolean {
      val callingPackage = activity.callingPackage
      Log.d(TAG, "🔍 [SAMSUNG] Chamador: $callingPackage")
      return callingPackage != null && callingPackage == SAMSUNG_PAY_PACKAGE
    }
    /**
     * Processa dados específicos da Samsung Wallet (Mastercard/Visa)
     */
    private fun processSamsungWalletData(data: String): Map<String, Any> {
      val result = mutableMapOf<String, Any>()

      try {
        // Tentar decodificar como base64 primeiro (Mastercard)
        var decodedData = data
        var dataFormat = "raw"
        var cardType = "UNKNOWN"

        try {
          val decodedBytes = android.util.Base64.decode(data, android.util.Base64.DEFAULT)
          decodedData = String(decodedBytes, Charsets.UTF_8)
          dataFormat = "base64_decoded"
          Log.d(TAG, "🔍 [SAMSUNG] Dados decodificados como base64: ${decodedData.length} caracteres")
        } catch (e: Exception) {
          Log.d(TAG, "🔍 [SAMSUNG] Dados não são base64, usando formato original")
          dataFormat = "raw"
        }

        result["dataFormat"] = dataFormat
        result["decodedData"] = decodedData

        // Tentar identificar o tipo de cartão baseado nos dados
        try {
          val jsonData = org.json.JSONObject(decodedData)

          // Verificar se é Mastercard (campos específicos)
          if (jsonData.has("paymentAppProviderId") ||
              jsonData.has("paymentAppInstanceId") ||
              jsonData.has("tokenUniqueReference")) {
            cardType = "MASTERCARD"
            Log.d(TAG, "✅ [SAMSUNG] Detectado Mastercard")

            // Extrair campos específicos do Mastercard
            if (jsonData.has("paymentAppProviderId")) {
              result["paymentAppProviderId"] = jsonData.getString("paymentAppProviderId")
            }
            if (jsonData.has("paymentAppInstanceId")) {
              result["paymentAppInstanceId"] = jsonData.getString("paymentAppInstanceId")
            }
            if (jsonData.has("tokenUniqueReference")) {
              result["tokenUniqueReference"] = jsonData.getString("tokenUniqueReference")
            }
            if (jsonData.has("accountPanSuffix")) {
              result["accountPanSuffix"] = jsonData.getString("accountPanSuffix")
            }
            if (jsonData.has("accountExpiry")) {
              result["accountExpiry"] = jsonData.getString("accountExpiry")
            }
          }
          // Verificar se é Visa (campos específicos)
          else if (jsonData.has("panId") ||
                  jsonData.has("trId") ||
                  jsonData.has("tokenReferenceId")) {
            cardType = "VISA"
            Log.d(TAG, "✅ [SAMSUNG] Detectado Visa")

            // Extrair campos específicos do Visa
            if (jsonData.has("panId")) {
              result["panId"] = jsonData.getString("panId")
            }
            if (jsonData.has("trId")) {
              result["trId"] = jsonData.getString("trId")
            }
            if (jsonData.has("tokenReferenceId")) {
              result["tokenReferenceId"] = jsonData.getString("tokenReferenceId")
            }
            if (jsonData.has("last4Digits")) {
              result["last4Digits"] = jsonData.getString("last4Digits")
            }
            if (jsonData.has("deviceId")) {
              result["deviceId"] = jsonData.getString("deviceId")
            }
            if (jsonData.has("walletAccountId")) {
              result["walletAccountId"] = jsonData.getString("walletAccountId")
            }
          }
          // Se não conseguir identificar, tentar campos genéricos
          else {
            Log.d(TAG, "🔍 [SAMSUNG] Tipo de cartão não identificado, usando campos genéricos")

            // Adicionar todos os campos disponíveis
            val keys = jsonData.keys()
            while (keys.hasNext()) {
              val key = keys.next()
              val value = jsonData.get(key)
              result[key] = value.toString()
            }
          }

          Log.d(TAG, "✅ [SAMSUNG] Dados JSON processados com sucesso")

        } catch (e: Exception) {
          Log.w(TAG, "⚠️ [SAMSUNG] Dados não são JSON válido: ${e.message}")
          cardType = "ENCRYPTED_OR_BINARY"
        }

        result["cardType"] = cardType

      } catch (e: Exception) {
        Log.e(TAG, "❌ [SAMSUNG] Erro ao processar dados Samsung Wallet: ${e.message}", e)
        result["error"] = e.message ?: "Erro desconhecido"
        result["cardType"] = "ERROR"
      }

      return result
    }
  }

  override fun openWallet(promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] openWallet chamado")
    try {
      if (walletOpener == null) {
        Log.w(TAG, "WALLET_OPENER_NOT_AVAILABLE: WalletOpener não foi inicializado")
        promise.reject("WALLET_OPENER_NOT_AVAILABLE", "WalletOpener não foi inicializado")
        return
      }

      val packageName = SAMSUNG_PAY_PACKAGE
      val appName = "Samsung Pay"
      val playStoreUrl = "market://details?id=$packageName"
      val webUrl = SAMSUNG_PAY_PLAY_STORE_URL

      val success = walletOpener!!.openWallet(packageName, appName, playStoreUrl, webUrl)
      
      if (success) {
        Log.d(TAG, "✅ [SAMSUNG] Wallet aberto com sucesso")
        promise.resolve(true)
      } else {
        Log.w(TAG, "❌ [SAMSUNG] Falha ao abrir wallet")
        promise.reject("OPEN_WALLET_ERROR", "Falha ao abrir Samsung Pay")
      }
    } catch (e: Exception) {
      Log.e(TAG, "OPEN_WALLET_ERROR: ${e.message}")
      promise.reject("OPEN_WALLET_ERROR", e.message, e)
    }
  }

  override fun sendNoIntentReceivedEvent() {
    Log.d(TAG, "🔍 [SAMSUNG] sendNoIntentReceivedEvent chamado")
    try {
      sendEventToReactNative("SamsungWalletNoIntentReceived", null)
      Log.d(TAG, "✅ [SAMSUNG] Evento de nenhuma intent enviado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [SAMSUNG] Erro ao enviar evento de nenhuma intent: ${e.message}", e)
    }
  }
}

