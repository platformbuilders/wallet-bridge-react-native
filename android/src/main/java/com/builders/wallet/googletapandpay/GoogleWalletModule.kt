package com.builders.wallet.googletapandpay

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.nfc.NfcManager
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = GoogleWalletModule.NAME)
class GoogleWalletModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val isSDKAvailable: Boolean by lazy {
    try {
      Class.forName("com.google.android.gms.tapandpay.TapAndPay")
      Class.forName("com.google.android.gms.tapandpay.TapAndPayClient")
      Class.forName("com.google.android.gms.common.GoogleApiAvailability")
      true
    } catch (e: ClassNotFoundException) {
      Log.w(TAG, "Google Pay SDK não está disponível: ${e.message}")
      false
    }
  }

  private var tapAndPayClient: Any? = null
  private var activity: Activity? = null
  private var mPickerPromise: Promise? = null

  init {
    if (isSDKAvailable) {
      try {
        // Inicializar TapAndPayClient usando reflexão
        val tapAndPayClass = Class.forName("com.google.android.gms.tapandpay.TapAndPay")
        val getClientMethod = tapAndPayClass.getMethod("getClient", Context::class.java)
        tapAndPayClient = getClientMethod.invoke(null, reactContext)
        
        reactContext.addActivityEventListener(object : BaseActivityEventListener() {
          override fun onActivityResult(
            activity: Activity,
            requestCode: Int,
            resultCode: Int,
            data: Intent?
          ) {
            mPickerPromise?.run {
              when (requestCode) {
                PUSH_TOKENIZE_REQUEST -> {
                  when (resultCode) {
                    Activity.RESULT_OK -> {
                      val tokenId = data?.getStringExtra("com.google.android.gms.tapandpay.EXTRA_ISSUER_TOKEN_ID")
                      if (tokenId.isNullOrEmpty()) {
                        Log.w(TAG, "Token ID é null ou vazio")
                        reject("PUSH_TOKENIZE_ERROR", "Falha ao tokenizar por push - Token ID é null ou vazio")
                      } else {
                        Log.i(TAG, "Push tokenize OK - Token ID: $tokenId")
                        resolve(tokenId)
                      }
                    }
                    else -> {
                      Log.w(TAG, "Push tokenize falhou - código: $resultCode")
                      reject("PUSH_TOKENIZE_ERROR", "Falha ao tokenizar por push - Result Code: $resultCode")
                    }
                  }
                }
                CREATE_WALLET_REQUEST -> {
                  if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Carteira criada com sucesso")
                    resolve(true)
                  } else {
                    Log.w(TAG, "Falha ao criar carteira - código: $resultCode")
                    reject("CREATE_WALLET_ERROR", "Falha ao criar carteira - Result Code: $resultCode")
                  }
                }
                else -> {}
              }
              mPickerPromise = null
            }
          }
          
          override fun onNewIntent(intent: Intent) {
            // Captura a atividade atual quando uma nova intenção é recebida
            this@GoogleWalletModule.activity = reactApplicationContext.currentActivity
          }
        })
      } catch (e: Exception) {
        Log.w(TAG, "Erro ao inicializar Google Pay SDK: ${e.message}")
      }
    }
  }

  @ReactMethod
  fun checkWalletAvailability(promise: Promise) {
    Log.d(TAG, "🔍 [GOOGLE] checkWalletAvailability chamado")
    try {
      // Verificar se é Android e acima do ICE_CREAM_SANDWICH
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        Log.d(TAG, "✅ [GOOGLE] Android ${Build.VERSION.SDK_INT} suportado")
        promise.resolve(true)
      } else {
        Log.w(TAG, "❌ [GOOGLE] Android ${Build.VERSION.SDK_INT} não suportado")
        promise.resolve(false)
      }
      
      Log.d(TAG, "✅ [GOOGLE] checkWalletAvailability executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [GOOGLE] Erro em checkWalletAvailability: ${e.message}", e)
      promise.reject("CHECK_WALLET_AVAILABILITY_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun getSecureWalletInfo(promise: Promise) {
    Log.d(TAG, "🔍 [GOOGLE] getSecureWalletInfo chamado")
    try {
      if (!isSDKAvailable) {
        Log.w(TAG, "Google Pay SDK não está disponível")
        promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
        return
      }
      
      if (tapAndPayClient == null) {
        Log.w(TAG, "Cliente TapAndPay não foi inicializado")
        promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
        return
      }
      
      // Obter ID da carteira ativa usando reflexão
      try {
        Log.d(TAG, "🔍 [GOOGLE] Tentando obter ID da carteira ativa...")
        val activeWalletIdMethod = tapAndPayClient?.javaClass?.getMethod("getActiveWalletId")
        val task = activeWalletIdMethod?.invoke(tapAndPayClient) as? Any
        
        if (task != null) {
          Log.d(TAG, "🔍 [GOOGLE] Task obtida, configurando listener...")
          
          // Criar OnCompleteListener usando reflexão
          val onCompleteListenerClass = Class.forName("com.google.android.gms.tasks.OnCompleteListener")
          val onCompleteListener = java.lang.reflect.Proxy.newProxyInstance(
            onCompleteListenerClass.classLoader,
            arrayOf(onCompleteListenerClass)
          ) { _, method, args ->
            if (method.name == "onComplete") {
              try {
                val completedTask = args?.get(0) as? Any
                if (completedTask != null) {
                  Log.d(TAG, "🔍 [GOOGLE] Callback executado, processando resultado...")
                  
                  val isSuccessfulMethod = completedTask.javaClass.getMethod("isSuccessful")
                  val isSuccessful = isSuccessfulMethod.invoke(completedTask) as Boolean
                  
                  Log.d(TAG, "🔍 [GOOGLE] Task bem-sucedida: $isSuccessful")
                  
                  if (isSuccessful) {
                    val getResultMethod = completedTask.javaClass.getMethod("getResult")
                    val walletId = getResultMethod.invoke(completedTask) as? String
                    
                    if (walletId != null && walletId.isNotEmpty()) {
                      Log.d(TAG, "✅ [GOOGLE] Wallet ID obtido: $walletId")
                      val result = Arguments.createMap()
                      result.putString("deviceID", "google_device_${walletId.hashCode()}")
                      result.putString("walletAccountID", walletId)
                      promise.resolve(result)
                    } else {
                      Log.w(TAG, "❌ [GOOGLE] Wallet ID é null ou vazio")
                      promise.reject("GET_WALLET_INFO_ERROR", "Wallet ID é null ou vazio")
                    }
                  } else {
                    // Tentar obter o código de erro da task
                    var errorMessage = "Falha ao obter informações da carteira - task não foi bem-sucedida"
                    try {
                      val getExceptionMethod = completedTask.javaClass.getMethod("getException")
                      val exception = getExceptionMethod.invoke(completedTask) as? Exception
                      if (exception != null) {
                        errorMessage = "Falha ao obter informações da carteira - Erro: ${exception.message}"
                        Log.w(TAG, "❌ [GOOGLE] Exception da task: ${exception.message}")
                      }
                    } catch (e: Exception) {
                      Log.w(TAG, "❌ [GOOGLE] Não foi possível obter exception da task: ${e.message}")
                    }
                    
                    Log.w(TAG, "❌ [GOOGLE] $errorMessage")
                    promise.reject("GET_WALLET_INFO_ERROR", errorMessage)
                  }
                } else {
                  Log.w(TAG, "❌ [GOOGLE] CompletedTask é null")
                  promise.reject("GET_WALLET_INFO_ERROR", "CompletedTask é null")
                }
              } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao processar resultado da carteira: ${e.message}", e)
                promise.reject("GET_WALLET_INFO_ERROR", "Erro ao processar resultado da carteira: ${e.message}")
              }
            }
            null
          }
          
          val addOnCompleteListenerMethod = task.javaClass.getMethod("addOnCompleteListener", onCompleteListenerClass)
          addOnCompleteListenerMethod.invoke(task, onCompleteListener)
          
          Log.d(TAG, "✅ [GOOGLE] Listener configurado com sucesso")
        } else {
          Log.w(TAG, "❌ [GOOGLE] Não foi possível obter task do getActiveWalletId")
          promise.reject("GET_WALLET_INFO_ERROR", "Não foi possível obter task do getActiveWalletId")
        }
      } catch (e: Exception) {
        Log.e(TAG, "❌ [GOOGLE] Erro ao obter informações da carteira: ${e.message}", e)
        promise.reject("GET_WALLET_INFO_ERROR", "Erro ao obter informações da carteira: ${e.message}")
      }
      
    } catch (e: Exception) {
      Log.e(TAG, "❌ [GOOGLE] Erro em getSecureWalletInfo: ${e.message}", e)
      promise.reject("GET_SECURE_WALLET_INFO_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
    Log.d(TAG, "🔍 [GOOGLE] getCardStatusBySuffix chamado com lastDigits: $lastDigits")
    try {
      if (!isSDKAvailable) {
        promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
        return
      }
      
      // Implementação simplificada - retorna status mockado
      promise.resolve("not found")
      Log.d(TAG, "✅ [GOOGLE] getCardStatusBySuffix executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [GOOGLE] Erro em getCardStatusBySuffix: ${e.message}", e)
      promise.reject("GET_CARD_STATUS_BY_SUFFIX_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
    Log.d(TAG, "🔍 [GOOGLE] getCardStatusByIdentifier chamado com identifier: $identifier, tsp: $tsp")
    try {
      if (!isSDKAvailable) {
        promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
        return
      }
      
      // Implementação simplificada - retorna status mockado
      promise.resolve("not found")
      Log.d(TAG, "✅ [GOOGLE] getCardStatusByIdentifier executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [GOOGLE] Erro em getCardStatusByIdentifier: ${e.message}", e)
      promise.reject("GET_CARD_STATUS_BY_IDENTIFIER_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
    Log.d(TAG, "🔍 [GOOGLE] addCardToWallet chamado")
    try {
      if (!isSDKAvailable) {
        Log.w(TAG, "Google Pay SDK não está disponível")
        promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
        return
      }
      
      Log.d(TAG, "🔍 [GOOGLE] Dados do cartão recebidos: $cardData")
      
      // Obter atividade atual
      activity = reactApplicationContext.currentActivity
      if (activity == null) {
        promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
        return
      }
      
      mPickerPromise = promise
      
      try {
        // Criar UserAddress usando reflexão
        val userAddressClass = Class.forName("com.google.android.gms.tapandpay.issuer.UserAddress")
        val userAddressBuilderClass = Class.forName("com.google.android.gms.tapandpay.issuer.UserAddress\$Builder")
        val userAddressBuilder = userAddressBuilderClass.newInstance()
        
        val userAddress = cardData.getMap("userAddress")
        if (userAddress != null) {
          userAddressBuilderClass.getMethod("setAddress1", String::class.java)
            .invoke(userAddressBuilder, userAddress.getString("addressOne") ?: "")
          userAddressBuilderClass.getMethod("setAddress2", String::class.java)
            .invoke(userAddressBuilder, userAddress.getString("addressTwo") ?: "")
          userAddressBuilderClass.getMethod("setCountryCode", String::class.java)
            .invoke(userAddressBuilder, userAddress.getString("countryCode") ?: "")
          userAddressBuilderClass.getMethod("setLocality", String::class.java)
            .invoke(userAddressBuilder, userAddress.getString("city") ?: "")
          userAddressBuilderClass.getMethod("setAdministrativeArea", String::class.java)
            .invoke(userAddressBuilder, userAddress.getString("administrativeArea") ?: "")
          userAddressBuilderClass.getMethod("setName", String::class.java)
            .invoke(userAddressBuilder, userAddress.getString("name") ?: "")
          userAddressBuilderClass.getMethod("setPhoneNumber", String::class.java)
            .invoke(userAddressBuilder, userAddress.getString("phoneNumber") ?: "")
          userAddressBuilderClass.getMethod("setPostalCode", String::class.java)
            .invoke(userAddressBuilder, userAddress.getString("postalCode") ?: "")
        }
        
        val userAddressObj = userAddressBuilderClass.getMethod("build").invoke(userAddressBuilder)
        
        // Criar PushTokenizeRequest usando reflexão
        val pushTokenizeRequestClass = Class.forName("com.google.android.gms.tapandpay.issuer.PushTokenizeRequest")
        val pushTokenizeRequestBuilderClass = Class.forName("com.google.android.gms.tapandpay.issuer.PushTokenizeRequest\$Builder")
        val pushTokenizeRequestBuilder = pushTokenizeRequestBuilderClass.newInstance()
        
        pushTokenizeRequestBuilderClass.getMethod("setOpaquePaymentCard", ByteArray::class.java)
          .invoke(pushTokenizeRequestBuilder, (cardData.getString("opaquePaymentCard") ?: "").toByteArray())
        pushTokenizeRequestBuilderClass.getMethod("setNetwork", Int::class.java)
          .invoke(pushTokenizeRequestBuilder, cardData.getInt("tokenServiceProvider"))
        pushTokenizeRequestBuilderClass.getMethod("setTokenServiceProvider", Int::class.java)
          .invoke(pushTokenizeRequestBuilder, cardData.getInt("tokenServiceProvider"))
        pushTokenizeRequestBuilderClass.getMethod("setDisplayName", String::class.java)
          .invoke(pushTokenizeRequestBuilder, cardData.getString("cardHolderName") ?: "")
        pushTokenizeRequestBuilderClass.getMethod("setLastDigits", String::class.java)
          .invoke(pushTokenizeRequestBuilder, cardData.getString("lastDigits") ?: "")
        pushTokenizeRequestBuilderClass.getMethod("setUserAddress", userAddressClass)
          .invoke(pushTokenizeRequestBuilder, userAddressObj)
        
        val pushTokenizeRequest = pushTokenizeRequestBuilderClass.getMethod("build").invoke(pushTokenizeRequestBuilder)
        
        // Chamar pushTokenize usando reflexão
        val pushTokenizeMethod = tapAndPayClient?.javaClass?.getMethod("pushTokenize", 
          Activity::class.java, pushTokenizeRequestClass, Int::class.java)
        pushTokenizeMethod?.invoke(tapAndPayClient, activity, pushTokenizeRequest, PUSH_TOKENIZE_REQUEST)
        
      } catch (e: Exception) {
        Log.w(TAG, "Erro ao processar pushTokenize: ${e.message}")
        promise.reject("PUSH_TOKENIZE_ERROR", "Erro ao processar tokenização: ${e.message}")
      }
      
      Log.d(TAG, "✅ [GOOGLE] addCardToWallet executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [GOOGLE] Erro em addCardToWallet: ${e.message}", e)
      promise.reject("ADD_CARD_TO_WALLET_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun createWalletIfNeeded(promise: Promise) {
    Log.d(TAG, "🔍 [GOOGLE] createWalletIfNeeded chamado")
    try {
      if (!isSDKAvailable) {
        Log.w(TAG, "Google Pay SDK não está disponível")
        promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
        return
      }
      
      // Obter atividade atual
      activity = reactApplicationContext.currentActivity
      if (activity == null) {
        promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
        return
      }
      
      mPickerPromise = promise
      
      try {
        // Chamar createWallet usando reflexão
        val createWalletMethod = tapAndPayClient?.javaClass?.getMethod("createWallet", 
          Activity::class.java, Int::class.java)
        createWalletMethod?.invoke(tapAndPayClient, activity, CREATE_WALLET_REQUEST)
      } catch (e: Exception) {
        Log.w(TAG, "Erro ao criar carteira: ${e.message}")
        promise.reject("CREATE_WALLET_ERROR", "Erro ao criar carteira: ${e.message}")
      }
      
      Log.d(TAG, "✅ [GOOGLE] createWalletIfNeeded executado com sucesso")
    } catch (e: Exception) {
      Log.e(TAG, "❌ [GOOGLE] Erro em createWalletIfNeeded: ${e.message}", e)
      promise.reject("CREATE_WALLET_IF_NEEDED_ERROR", e.message, e)
    }
  }

  override fun getConstants(): MutableMap<String, Any> {
    Log.i(TAG, "--")
    Log.i(TAG, "> getConstants started")
    
    val constants = hashMapOf<String, Any>()
    
    // Adiciona constantes básicas sempre
    constants["SDK_AVAILABLE"] = isSDKAvailable
    constants["SDK_NAME"] = "GoogleWallet"
    
    // Adiciona constantes do SDK se estiver disponível
    if (isSDKAvailable) {
      Log.i(TAG, "> SDK disponível, obtendo constantes do TapAndPay")
      
      // Usa reflection para acessar as constantes do TapAndPay de forma segura
      val tapAndPayClass = Class.forName("com.google.android.gms.tapandpay.TapAndPay")
      
      // Obtém as constantes usando reflection
      constants["TOKEN_PROVIDER_ELO"] = tapAndPayClass.getField("TOKEN_PROVIDER_ELO").getInt(null)
      constants["CARD_NETWORK_ELO"] = tapAndPayClass.getField("CARD_NETWORK_ELO").getInt(null)
      constants["TOKEN_STATE_UNTOKENIZED"] = tapAndPayClass.getField("TOKEN_STATE_UNTOKENIZED").getInt(null)
      constants["TOKEN_STATE_PENDING"] = tapAndPayClass.getField("TOKEN_STATE_PENDING").getInt(null)
      constants["TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION"] = tapAndPayClass.getField("TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION").getInt(null)
      constants["TOKEN_STATE_SUSPENDED"] = tapAndPayClass.getField("TOKEN_STATE_SUSPENDED").getInt(null)
      constants["TOKEN_STATE_ACTIVE"] = tapAndPayClass.getField("TOKEN_STATE_ACTIVE").getInt(null)
      constants["TOKEN_STATE_FELICA_PENDING_PROVISIONING"] = tapAndPayClass.getField("TOKEN_STATE_FELICA_PENDING_PROVISIONING").getInt(null)
      
      Log.i(TAG, "> Constantes do TapAndPay obtidas com sucesso")
    } else {
      Log.w(TAG, "> SDK não disponível, retornando valores padrão para constantes")
      
      // Retorna valores padrão quando SDK não está disponível
      constants["TOKEN_PROVIDER_ELO"] = -1
      constants["CARD_NETWORK_ELO"] = -1
      constants["TOKEN_STATE_UNTOKENIZED"] = -1
      constants["TOKEN_STATE_PENDING"] = -1
      constants["TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION"] = -1
      constants["TOKEN_STATE_SUSPENDED"] = -1
      constants["TOKEN_STATE_ACTIVE"] = -1
      constants["TOKEN_STATE_FELICA_PENDING_PROVISIONING"] = -1
    }
    
    Log.i(TAG, "> getConstants completed")
    return constants
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "GoogleWallet"
    private const val TAG = "GoogleWallet"
    private const val PUSH_TOKENIZE_REQUEST = 2
    private const val CREATE_WALLET_REQUEST = 6
  }
}
