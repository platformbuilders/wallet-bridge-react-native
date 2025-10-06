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
          promise.resolve(status) // Retorna o status mesmo quando não está pronto
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

    val WALLET_DM_ID = getStaticString("com.samsung.android.sdk.samsungpay.v2.SamsungPay", "WALLET_DM_ID")
    val DEVICE_ID = getStaticString("com.samsung.android.sdk.samsungpay.v2.SamsungPay", "DEVICE_ID")
    val WALLET_USER_ID = getStaticString("com.samsung.android.sdk.samsungpay.v2.SamsungPay", "WALLET_USER_ID")

    val keys = listOf(WALLET_DM_ID, DEVICE_ID, WALLET_USER_ID)
    val listener = proxy("com.samsung.android.sdk.samsungpay.v2.StatusListener", mapOf(
      "onSuccess" to { args: Array<out Any?> ->
        val status = (args[0] as? Number)?.toInt() ?: -1
        val walletData = args[1] as android.os.Bundle
        val clientWalletDmId = walletData.getString(WALLET_DM_ID)
        val clientDeviceId = walletData.getString(DEVICE_ID)
        val clientWalletAccountId = walletData.getString(WALLET_USER_ID)
        val result: WritableMap = Arguments.createMap().apply {
          putString("walletDMId", clientWalletDmId)
          putString("deviceId", clientDeviceId)
          putString("walletUserId", clientWalletAccountId)
        }
        Log.i(TAG, "- Wallet Info: $result")
        promise.resolve(result)
        null
      },
      "onFail" to { args: Array<out Any?> ->
        val errorCode = (args[0] as? Number)?.toInt() ?: -1
        Log.d(TAG, "doGetWalletInfo onFail callback is called, errorCode:$errorCode")
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
    progress: Callback,
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> addCard started")
    Log.d(
      TAG,
      "addCard payload : $payload, issuerId : $issuerId, tokenizationProvider : $tokenizationProvider"
    )

    if (!requireSdkAvailable(promise)) return
    if (!requireInitialized(promise)) return

    val ADD_CARD_INFO = Class.forName("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo")
    val EXTRA_PROVISION_PAYLOAD = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "EXTRA_PROVISION_PAYLOAD")
    val EXTRA_ISSUER_ID = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "EXTRA_ISSUER_ID")
    val PROVIDER_ELO = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_ELO")
    val CARD = Class.forName("com.samsung.android.sdk.samsungpay.v2.card.Card")
    val CARD_TYPE_CREDIT_DEBIT = (CARD.getField("CARD_TYPE_CREDIT_DEBIT").get(null) as? Int) ?: 0
    
    val cardDetail = android.os.Bundle().apply {
      putString(EXTRA_PROVISION_PAYLOAD, payload)
    }
    //issuerId is Mandatory for ELO
    if (tokenizationProvider == PROVIDER_ELO) {
      cardDetail.putString(EXTRA_ISSUER_ID, issuerId)
    }
    val addCardInfo = ADD_CARD_INFO.getConstructor(Int::class.java, String::class.java, android.os.Bundle::class.java)
      .newInstance(CARD_TYPE_CREDIT_DEBIT, tokenizationProvider, cardDetail)

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
    getSamsungPayStatus(promise)
  }

  override fun getSecureWalletInfo(promise: Promise) {
    getWalletInfo(promise)
  }

  override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
    // Implementação simplificada para compatibilidade
    val payload = cardData.getString("payload") ?: ""
    val issuerId = cardData.getString("issuerId") ?: ""
    val PROVIDER_VISA = getStaticString("com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo", "PROVIDER_VISA")
    val tokenizationProvider = cardData.getString("tokenizationProvider") ?: PROVIDER_VISA
    
    addCard(payload, issuerId, tokenizationProvider, object : Callback {
      override fun invoke(vararg args: Any?) {
        // Progress callback vazio para compatibilidade
      }
    }, promise)
  }

  override fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] getCardStatusBySuffix chamado com lastDigits: $lastDigits")
    // Implementação simplificada - retorna status mockado
    promise.resolve("not found")
  }

  override fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] getCardStatusByIdentifier chamado com identifier: $identifier, tsp: $tsp")
    // Implementação simplificada - retorna status mockado
    promise.resolve("not found")
  }

  override fun createWalletIfNeeded(promise: Promise) {
    Log.d(TAG, "🔍 [SAMSUNG] createWalletIfNeeded chamado")
    // Implementação simplificada - sempre retorna false
    promise.resolve(false)
  }

  override fun getConstants(): MutableMap<String, Any> {
    return hashMapOf<String, Any>(
      "SDK_NAME" to "SamsungWallet"
    )
  }

  companion object {
    private const val TAG = "SamsungWallet"
  }
}
