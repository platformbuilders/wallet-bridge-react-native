package com.builders.wallet.samsungpay

import android.os.Bundle
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.builders.wallet.samsungpay.util.PartnerInfoHolder
import com.builders.wallet.samsungpay.util.ErrorCode
import com.builders.wallet.samsungpay.SerializableCard.toSerializable
// NENHUM import do SDK da Samsung aqui. Tudo é chamado por reflexão.

class SamsungWalletImplementation(private val reactContext: ReactApplicationContext) : SamsungWalletContract {

  private var samsungPay: Any? = null
  private var cardManager: Any? = null

  private val isSDKAvailable: Boolean by lazy {
    try {
      Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk")
      true
    } catch (_: ClassNotFoundException) {
      false
    }
  }

  private fun requireSdkAvailable(promise: Promise): Boolean {
    if (!isSDKAvailable) {
      Log.e(TAG, "Samsung Pay SDK não está disponível")
      promise.reject("SDK_NOT_AVAILABLE", "Samsung Pay SDK não está disponível")
      return false
    }
    return true
  }

  private fun requireInitialized(promise: Promise): Boolean {
    if (samsungPay == null || cardManager == null) {
      Log.e(TAG, "Samsung Pay não foi inicializado")
      promise.reject("NOT_INITIALIZED", "Samsung Pay não foi inicializado. Chame init() primeiro.")
      return false
    }
    return true
  }

  private fun getStaticInt(clazz: String, field: String, defaultValue: Int = 0): Int = try {
    Class.forName(clazz).getField(field).get(null) as? Int ?: defaultValue
  } catch (_: Throwable) { defaultValue }

  private fun getStaticString(clazz: String, field: String, defaultValue: String = ""): String = try {
    Class.forName(clazz).getField(field).get(null) as? String ?: defaultValue
  } catch (_: Throwable) { defaultValue }

  private fun call(instance: Any?, method: String, vararg args: Any?): Any? {
    if (instance == null) return null
    val argTypes = args.map { it?.javaClass?.interfaces?.firstOrNull() ?: it?.javaClass }.toTypedArray()
    val m = instance.javaClass.methods.firstOrNull { it.name == method && it.parameterTypes.size == args.size }
    return m?.invoke(instance, *args)
  }

  private fun newInstance(className: String, vararg args: Any?): Any? {
    return try {
      val clazz = Class.forName(className)
      val ctor = clazz.constructors.firstOrNull { it.parameterTypes.size == args.size }
      ctor?.newInstance(*args)
    } catch (t: Throwable) {
      Log.e(TAG, "Erro ao instanciar $className: ${t.message}", t)
      null
    }
  }

  private fun <T> proxy(interfaceName: String, handlers: Map<String, (Array<out Any?>) -> T>): Any {
    val clazz = Class.forName(interfaceName)
    val loader = clazz.classLoader
    return java.lang.reflect.Proxy.newProxyInstance(loader, arrayOf(clazz)) { _, method, args ->
      val fn = handlers[method.name]
      fn?.invoke(args ?: emptyArray())
    }
  }

  // https://developer.samsung.com/pay/native/common-api.html
  override fun init(serviceId: String, promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> Init started")

    try {
      if (!requireSdkAvailable(promise)) return
      val partnerInfo = PartnerInfoHolder.getInstance(serviceId).partnerInfo
      samsungPay = newInstance("com.samsung.android.sdk.samsungpay.v2.SamsungPay", reactContext, partnerInfo)
      cardManager = newInstance("com.samsung.android.sdk.samsungpay.v2.card.CardManager", reactContext, partnerInfo)
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
    if (!requireSdkAvailable(promise)) return
    if (!requireInitialized(promise)) return

    val EXTRA_ERROR_REASON = getStaticString("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "EXTRA_ERROR_REASON")
    val ERROR_NONE = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_NONE")
    val SPAY_READY = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_READY")
    val SPAY_NOT_READY = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_NOT_READY")
    val SPAY_NOT_SUPPORTED = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_NOT_SUPPORTED")
    val SPAY_NOT_ALLOWED_TEMPORALLY = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_NOT_ALLOWED_TEMPORALLY")
    val SPAY_HAS_TRANSIT_CARD = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_HAS_TRANSIT_CARD")
    val SPAY_HAS_NO_TRANSIT_CARD = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_HAS_NO_TRANSIT_CARD")

    val listener = proxy("com.samsung.android.sdk.samsungpay.v2.StatusListener", mapOf(
      "onSuccess" to { args: Array<out Any?> ->
        val status = (args[0] as? Number)?.toInt() ?: -1
        val bundle = args[1] as android.os.Bundle
        Log.d(TAG, "onSuccess callback is called, status=$status, bundle:$bundle")
        
        val extraErrorReason = bundle.getInt(EXTRA_ERROR_REASON, ERROR_NONE)
        val statusName = when (status) {
          SPAY_READY -> "SPAY_READY"
          SPAY_NOT_READY -> "SPAY_NOT_READY"
          SPAY_NOT_SUPPORTED -> "SPAY_NOT_SUPPORTED"
          SPAY_NOT_ALLOWED_TEMPORALLY -> "SPAY_NOT_ALLOWED_TEMPORALLY"
          SPAY_HAS_TRANSIT_CARD -> "SPAY_HAS_TRANSIT_CARD"
          SPAY_HAS_NO_TRANSIT_CARD -> "SPAY_HAS_NO_TRANSIT_CARD"
          else -> "UNKNOWN_STATUS_$status"
        }
        
        Log.i(TAG, "- Samsung Pay Status: $statusName ($status)")
        
        if (status == SPAY_READY) {
          Log.i(TAG, "- Samsung Pay está pronto para uso")
          promise.resolve(status)
        } else {
          val error = if (extraErrorReason != ERROR_NONE) {
            ErrorCode.getErrorCodeName(extraErrorReason)
          } else {
            "Samsung Pay não está pronto (Status: $statusName)"
          }
          Log.w(TAG, "Samsung Pay não está disponível: $error")
          // Rejeita a promise com o status como código de erro e a mensagem de erro
          promise.reject(status.toString(), error)
        }
        null
      },
      "onFail" to { args: Array<out Any?> ->
        val errorCode = (args[0] as? Number)?.toInt() ?: -1
        val bundle = args[1] as android.os.Bundle
        Log.d(TAG, "onFail callback is called, errorCode=$errorCode, bundle:$bundle")
        
        val extraErrorReason = bundle.getInt(EXTRA_ERROR_REASON, ERROR_NONE)
        val error = if (extraErrorReason != ERROR_NONE) {
          ErrorCode.getErrorCodeName(extraErrorReason)
        } else {
          ErrorCode.getErrorCodeName(errorCode)
        }
        
        Log.e(TAG, "Erro ao verificar status do Samsung Pay: $error")
        promise.reject(errorCode.toString(), error)
        null
      }
    ))

    call(samsungPay, "getSamsungPayStatus", listener)
  }

  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#goToUpdatePage()
  override fun goToUpdatePage() {
    Log.i(TAG, "--")
    Log.i(TAG, "> goToUpdatePage started")
    if (samsungPay == null) return
    call(samsungPay, "goToUpdatePage")
  }

  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#activateSamsungPay()
  override fun activateSamsungPay() {
    Log.i(TAG, "--")
    Log.i(TAG, "> activateSamsungPay started")
    if (samsungPay == null) return
    call(samsungPay, "activateSamsungPay")
  }

  // https://developer.samsung.com/pay/native/apptoapp.html
  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/card/CardManager.html#getAllCards(android.os.Bundle,com.samsung.android.sdk.samsungpay.v2.card.GetCardListener)
  override fun getAllCards(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getAllCards started")
    if (!requireSdkAvailable(promise)) return
    if (!requireInitialized(promise)) return

    val listener = proxy("com.samsung.android.sdk.samsungpay.v2.card.GetCardListener", mapOf(
      "onSuccess" to { args: Array<out Any?> ->
        val list = args[0] as java.util.List<*>
        Log.d(TAG, "onSuccess callback is called, list.size= ${list.size}")
        val result = list.map { it?.toSerializable() }
        Log.i(TAG, "- cards - $result")
        promise.resolve(result)
        null
      },
      "onFail" to { args: Array<out Any?> ->
        val errorCode = (args[0] as? Number)?.toInt() ?: -1
        Log.d(TAG, "onFail callback is called, errorCode:$errorCode")
        val error = ErrorCode.getErrorCodeName(errorCode)
        Log.e(TAG, "Error when getting all cards: $error")
        promise.reject(errorCode.toString(), error)
        null
      }
    ))

    call(cardManager, "getAllCards", null, listener)
  }

  // https://developer.samsung.com/pay/api-reference/com/samsung/android/sdk/samsungpay/v2/SamsungPay.html#getWalletInfo(java.util.List,com.samsung.android.sdk.samsungpay.v2.StatusListener)
  override fun getWalletInfo(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getWalletInfo started")
    if (!requireSdkAvailable(promise)) return
    if (!requireInitialized(promise)) return

    // Obter as chaves necessárias para getWalletInfo conforme documentação
    val WALLET_USER_ID = getStaticString("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "WALLET_USER_ID")
    val DEVICE_ID = getStaticString("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "DEVICE_ID")
    val WALLET_DM_ID = getStaticString("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "WALLET_DM_ID")

    // Criar ArrayList com as chaves necessárias (conforme exemplo da documentação)
    val keys = java.util.ArrayList<String>().apply {
      add(WALLET_USER_ID)
      add(DEVICE_ID)
      add(WALLET_DM_ID)
    }

    Log.d(TAG, "Chaves solicitadas para getWalletInfo: $keys")

    val listener = proxy("com.samsung.android.sdk.samsungpay.v2.StatusListener", mapOf(
      "onSuccess" to { args: Array<out Any?> ->
        val status = (args[0] as? Number)?.toInt() ?: -1
        val walletData = args[1] as android.os.Bundle
        Log.d(TAG, "onSuccess callback is called, status=$status, walletData=$walletData")
        
        // Extrair dados conforme documentação
        val deviceId = walletData.getString(DEVICE_ID)
        val walletUserId = walletData.getString(WALLET_USER_ID)
        val walletDmId = walletData.getString(WALLET_DM_ID)
        
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
        null
      },
      "onFail" to { args: Array<out Any?> ->
        val errorCode = (args[0] as? Number)?.toInt() ?: -1
        val errorData = args[1] as android.os.Bundle
        Log.d(TAG, "onFail callback is called, errorCode=$errorCode, errorData=$errorData")
        
        val error = ErrorCode.getErrorCodeName(errorCode)
        Log.e(TAG, "Error when getting wallet info: $error")
        promise.reject(errorCode.toString(), error)
        null
      }
    ))

    call(samsungPay, "getWalletInfo", keys, listener)
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

    if (!requireSdkAvailable(promise)) return
    if (!requireInitialized(promise)) return

    val ADD_CARD_INFO = Class.forName("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo")
    val EXTRA_PROVISION_PAYLOAD = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "EXTRA_PROVISION_PAYLOAD")
    val EXTRA_ISSUER_ID = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "EXTRA_ISSUER_ID")
    val PROVIDER_ELO = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_ELO")
    
    val cardDetail = android.os.Bundle().apply {
      putString(EXTRA_PROVISION_PAYLOAD, payload)
    }
    //issuerId is Mandatory for ELO
    if (tokenizationProvider == PROVIDER_ELO) {
      cardDetail.putString(EXTRA_ISSUER_ID, issuerId)
    }
    
    // Usar o cardType recebido como parâmetro (conforme exemplo fornecido)
    val addCardInfo = ADD_CARD_INFO.getConstructor(String::class.java, String::class.java, android.os.Bundle::class.java)
      .newInstance(cardType, tokenizationProvider, cardDetail)

    val EXTRA_ERROR_REASON_MESSAGE = getStaticString("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "EXTRA_ERROR_REASON_MESSAGE")

    val listener = proxy("com.samsung.android.sdk.samsungpay.v2.card.AddCardListener", mapOf(
      "onSuccess" to { args: Array<out Any?> ->
        val card = args[1]
        Log.d(TAG, "doAddCard onSuccess callback is called")
        Log.i(TAG, "card added - $card")
        promise.resolve(card?.toSerializable())
        null
      },
      "onFail" to { args: Array<out Any?> ->
        val errorCode = (args[0] as? Number)?.toInt() ?: -1
        val errorData = args[1] as android.os.Bundle
        Log.d(TAG, "doAddCard onFail callback is called, errorCode:$errorCode")
        val message = if (errorData.containsKey(EXTRA_ERROR_REASON_MESSAGE)) {
          val error = errorData.getString(EXTRA_ERROR_REASON_MESSAGE)
          Log.e(TAG, "doAddCard onFail extra reason message: $error")
          error
        } else {
          ErrorCode.getErrorCodeName(errorCode)
        }
        Log.e(TAG, "Error when adding card: $message")
        promise.reject(errorCode.toString(), message)
        null
      },
      "onProgress" to { args: Array<out Any?> ->
        val currentCount = (args[0] as? Number)?.toInt() ?: 0
        val totalCount = (args[1] as? Number)?.toInt() ?: 0
        Log.d(TAG, "doAddCard onProgress : $currentCount / $totalCount")
        progress.invoke(currentCount, totalCount)
        null
      }
    ))

    call(cardManager, "addCard", addCardInfo, listener)
  }

  // Métodos de compatibilidade com a API anterior
  override fun checkWalletAvailability(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> checkWalletAvailability started")
    if (!requireSdkAvailable(promise)) return
    if (!requireInitialized(promise)) return

    val EXTRA_ERROR_REASON = getStaticString("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "EXTRA_ERROR_REASON")
    val ERROR_NONE = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_NONE")
    val SPAY_READY = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_READY")

    val listener = proxy("com.samsung.android.sdk.samsungpay.v2.StatusListener", mapOf(
      "onSuccess" to { args: Array<out Any?> ->
        val status = (args[0] as? Number)?.toInt() ?: -1
        val bundle = args[1] as android.os.Bundle
        Log.d(TAG, "onSuccess callback is called, status=$status, bundle:$bundle")
        
        val extraErrorReason = bundle.getInt(EXTRA_ERROR_REASON, ERROR_NONE)
        val isAvailable = status == SPAY_READY
        
        Log.i(TAG, "- Samsung Pay disponível: $isAvailable (Status: $status)")
        promise.resolve(isAvailable)
        null
      },
      "onFail" to { args: Array<out Any?> ->
        val errorCode = (args[0] as? Number)?.toInt() ?: -1
        val bundle = args[1] as android.os.Bundle
        Log.d(TAG, "onFail callback is called, errorCode=$errorCode, bundle:$bundle")
        
        val extraErrorReason = bundle.getInt(EXTRA_ERROR_REASON, ERROR_NONE)
        val error = if (extraErrorReason != ERROR_NONE) {
          ErrorCode.getErrorCodeName(extraErrorReason)
        } else {
          ErrorCode.getErrorCodeName(errorCode)
        }
        
        Log.e(TAG, "Erro ao verificar disponibilidade do Samsung Pay: $error")
        promise.resolve(false) // Retorna false em caso de erro
        null
      }
    ))

    call(samsungPay, "getSamsungPayStatus", listener)
  }


  override fun getConstants(): MutableMap<String, Any> {
    val constants = hashMapOf<String, Any>()
    
    // SDK Info
    constants["SDK_NAME"] = "SamsungWallet"
    
    // Samsung Pay Status Codes
    constants["SPAY_READY"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_READY", 2)
    constants["SPAY_NOT_READY"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_NOT_READY", 1)
    constants["SPAY_NOT_SUPPORTED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_NOT_SUPPORTED", 0)
    constants["SPAY_NOT_ALLOWED_TEMPORALLY"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_NOT_ALLOWED_TEMPORALLY", 3)
    constants["SPAY_HAS_TRANSIT_CARD"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_HAS_TRANSIT_CARD", 10)
    constants["SPAY_HAS_NO_TRANSIT_CARD"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "SPAY_HAS_NO_TRANSIT_CARD", 11)
    
    // Samsung Card Types (da classe Card)
    constants["CARD_TYPE"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "CARD_TYPE", "CARD_TYPE")
    constants["CARD_TYPE_CREDIT_DEBIT"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "CARD_TYPE_CREDIT_DEBIT", "PAYMENT")
    constants["CARD_TYPE_GIFT"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "CARD_TYPE_GIFT", "GIFT")
    constants["CARD_TYPE_LOYALTY"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "CARD_TYPE_LOYALTY", "LOYALTY")
    constants["CARD_TYPE_CREDIT"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "CARD_TYPE_CREDIT", "CREDIT")
    constants["CARD_TYPE_DEBIT"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "CARD_TYPE_DEBIT", "DEBIT")
    constants["CARD_TYPE_TRANSIT"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "CARD_TYPE_TRANSIT", "TRANSIT")
    constants["CARD_TYPE_VACCINE_PASS"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "CARD_TYPE_VACCINE_PASS", "VACCINE_PASS")
    
    // Samsung Card States (da classe Card)
    constants["ACTIVE"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "ACTIVE", "ACTIVE")
    constants["DISPOSED"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "DISPOSED", "DISPOSED")
    constants["EXPIRED"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "EXPIRED", "EXPIRED")
    constants["PENDING_ENROLLED"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "PENDING_ENROLLED", "ENROLLED")
    constants["PENDING_PROVISION"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "PENDING_PROVISION", "PENDING_PROVISION")
    constants["SUSPENDED"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "SUSPENDED", "SUSPENDED")
    constants["PENDING_ACTIVATION"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.Card", "PENDING_ACTIVATION", "PENDING_ACTIVATION")
    
    // Samsung Tokenization Providers (baseado na classe AddCardInfo)
    constants["PROVIDER_VISA"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_VISA", "VI")
    constants["PROVIDER_MASTERCARD"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_MASTERCARD", "MC")
    constants["PROVIDER_AMEX"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_AMEX", "AX")
    constants["PROVIDER_DISCOVER"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_DISCOVER", "DS")
    constants["PROVIDER_PLCC"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_PLCC", "PL")
    constants["PROVIDER_GIFT"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_GIFT", "GI")
    constants["PROVIDER_LOYALTY"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_LOYALTY", "LO")
    constants["PROVIDER_PAYPAL"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_PAYPAL", "PP")
    constants["PROVIDER_GEMALTO"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_GEMALTO", "GT")
    constants["PROVIDER_NAPAS"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_NAPAS", "NP")
    constants["PROVIDER_MIR"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_MIR", "MI")
    constants["PROVIDER_PAGOBANCOMAT"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_PAGOBANCOMAT", "PB")
    constants["PROVIDER_VACCINE_PASS"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_VACCINE_PASS", "VaccinePass")
    constants["PROVIDER_MADA"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_MADA", "MADA")
    constants["PROVIDER_ELO"] = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_ELO", "ELO")
    
    // Samsung Error Codes (todos do ErrorCode.kt)
    constants["ERROR_NONE"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_NONE", 0)
    constants["ERROR_SPAY_INTERNAL"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SPAY_INTERNAL", -1)
    constants["ERROR_INVALID_INPUT"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_INVALID_INPUT", -2)
    constants["ERROR_NOT_SUPPORTED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_NOT_SUPPORTED", -3)
    constants["ERROR_NOT_FOUND"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_NOT_FOUND", -4)
    constants["ERROR_ALREADY_DONE"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_ALREADY_DONE", -5)
    constants["ERROR_NOT_ALLOWED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_NOT_ALLOWED", -6)
    constants["ERROR_USER_CANCELED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_USER_CANCELED", -7)
    constants["ERROR_PARTNER_SDK_API_LEVEL"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_PARTNER_SDK_API_LEVEL", -10)
    constants["ERROR_PARTNER_SERVICE_TYPE"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_PARTNER_SERVICE_TYPE", -11)
    constants["ERROR_INVALID_PARAMETER"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_INVALID_PARAMETER", -12)
    constants["ERROR_NO_NETWORK"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_NO_NETWORK", -21)
    constants["ERROR_SERVER_NO_RESPONSE"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SERVER_NO_RESPONSE", -22)
    constants["ERROR_PARTNER_INFO_INVALID"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_PARTNER_INFO_INVALID", -99)
    constants["ERROR_INITIATION_FAIL"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_INITIATION_FAIL", -103)
    constants["ERROR_REGISTRATION_FAIL"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_REGISTRATION_FAIL", -104)
    constants["ERROR_DUPLICATED_SDK_API_CALLED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_DUPLICATED_SDK_API_CALLED", -105)
    constants["ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION", -300)
    constants["ERROR_SERVICE_ID_INVALID"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SERVICE_ID_INVALID", -301)
    constants["ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION", -302)
    constants["ERROR_PARTNER_APP_SIGNATURE_MISMATCH"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_PARTNER_APP_SIGNATURE_MISMATCH", -303)
    constants["ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED", -304)
    constants["ERROR_PARTNER_APP_BLOCKED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_PARTNER_APP_BLOCKED", -305)
    constants["ERROR_USER_NOT_REGISTERED_FOR_DEBUG"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_USER_NOT_REGISTERED_FOR_DEBUG", -306)
    constants["ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE", -307)
    constants["ERROR_PARTNER_NOT_APPROVED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_PARTNER_NOT_APPROVED", -308)
    constants["ERROR_UNAUTHORIZED_REQUEST_TYPE"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_UNAUTHORIZED_REQUEST_TYPE", -309)
    constants["ERROR_EXPIRED_OR_INVALID_DEBUG_KEY"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_EXPIRED_OR_INVALID_DEBUG_KEY", -310)
    constants["ERROR_SERVER_INTERNAL"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SERVER_INTERNAL", -311)
    constants["ERROR_DEVICE_NOT_SAMSUNG"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_DEVICE_NOT_SAMSUNG", -350)
    constants["ERROR_SPAY_PKG_NOT_FOUND"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SPAY_PKG_NOT_FOUND", -351)
    constants["ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE", -352)
    constants["ERROR_DEVICE_INTEGRITY_CHECK_FAIL"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_DEVICE_INTEGRITY_CHECK_FAIL", -353)
    constants["ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL", -360)
    constants["ERROR_ANDROID_PLATFORM_CHECK_FAIL"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_ANDROID_PLATFORM_CHECK_FAIL", -361)
    constants["ERROR_MISSING_INFORMATION"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_MISSING_INFORMATION", -354)
    constants["ERROR_SPAY_SETUP_NOT_COMPLETED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SPAY_SETUP_NOT_COMPLETED", -356)
    constants["ERROR_SPAY_APP_NEED_TO_UPDATE"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SPAY_APP_NEED_TO_UPDATE", -357)
    constants["ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED", -358)
    constants["ERROR_UNABLE_TO_VERIFY_CALLER"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_UNABLE_TO_VERIFY_CALLER", -359)
    constants["ERROR_SPAY_FMM_LOCK"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SPAY_FMM_LOCK", -604)
    constants["ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY"] = getStaticInt("com.samsung.android.sdk.samsungpay.v2.SpaySdk", "ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY", -605)
    
    return constants
  }

  companion object {
    private const val TAG = "SamsungWallet"
  }
}
