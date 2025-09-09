package com.builders.wallet.googletapandpay

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.nfc.NfcManager
import android.nfc.cardemulation.CardEmulation
import android.util.Log
import com.builders.wallet.googletapandpay.SerializableTokenInfo.Companion.toSerializable
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tapandpay.TapAndPay
import com.google.android.gms.tapandpay.TapAndPayClient
import com.google.android.gms.tapandpay.TapAndPayStatusCodes
import com.google.android.gms.tapandpay.issuer.IsTokenizedRequest
import com.google.android.gms.tapandpay.issuer.PushTokenizeRequest
import com.google.android.gms.tapandpay.issuer.TokenInfo
import com.google.android.gms.tapandpay.issuer.TokenStatus
import com.google.android.gms.tapandpay.issuer.UserAddress
import com.google.android.gms.tapandpay.issuer.ViewTokenRequest
import com.google.android.gms.tasks.Task

class GoogleTapAndPayModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val tapAndPayClient: TapAndPayClient by lazy {
    TapAndPay.getClient(reactContext)
  }
  private val activity: Activity get() = requireNotNull(reactContext.currentActivity)
  private var mPickerPromise: Promise? = null

  init {
    reactContext.addActivityEventListener(object : BaseActivityEventListener() {
      override fun onActivityResult(
          activity: Activity,
          requestCode: Int,
          resultCode: Int,
          data: Intent?
      ) {
        mPickerPromise?.run {
          when (requestCode) {
            SET_GOOGLE_PAY_AS_DEFAULT_NFC_PAYMENT_REQUEST -> {
              if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "- setGooglePayAsDefaultNFCPayment OK")
                resolve("Agora Google Pay é o app de pagamentos por NFC padrão")
              } else {
                Log.i(
                  TAG,
                  "Couldn't setGooglePayAsDefaultNFCPayment - $resultCode"
                )
                reject(
                  E_GOOGLE_PAY_AS_DEFAULT_NFC_PAYMENT,
                  "Falha ao configurar Google Pay como app de pagamentos por NFC padrão"
                )
              }
            }

            PUSH_TOKENIZE_REQUEST -> {
              when (resultCode) {
                Activity.RESULT_OK -> {
                  val tokenId =
                    data?.getStringExtra(TapAndPay.EXTRA_ISSUER_TOKEN_ID)
                  if (tokenId.isNullOrEmpty()) {
                    Log.i(
                      TAG,
                      "Couldn't pushTokenize - tokenId is null or empty"
                    )
                    reject(E_PUSH_TOKENIZE, "Falha ao tokenizar por push - tokenId nulo ou vazio")
                  } else {
                    Log.i(TAG, "- pushTokenize OK")
                    resolve(tokenId)
                  }
                }

                TapAndPayStatusCodes.TAP_AND_PAY_ATTESTATION_ERROR -> {
                  Log.i(TAG, "Couldn't pushTokenize - $resultCode")
                  reject(
                    E_PUSH_TOKENIZE,
                    "Falha ao tokenizar por push por prova - código: $resultCode"
                  )
                }

                else -> {
                  Log.i(TAG, "Couldn't pushTokenize - $resultCode")
                  reject(E_PUSH_TOKENIZE, "Falha ao tokenizar por push - código: $resultCode")
                }
              }
            }

            TOKENIZE_REQUEST -> {
              if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "- tokenize OK")
                resolve("Tokenizado")
              } else {
                Log.i(TAG, "Couldn't tokenize - $resultCode")
                reject(E_TOKENIZE, "Falha ao tokenizar - código: $resultCode")
              }
            }

            REQUEST_SELECT_TOKEN_REQUEST -> {
              if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "- requestSelectToken OK")
                resolve("Token padrão selecionado no Google Pay")
              } else {
                Log.i(TAG, "Couldn't requestSelectToken - $resultCode")
                reject(
                  E_REQUEST_SELECT_TOKEN,
                  "Falha ao selecionar token padrão no Google Pay - código: $resultCode"
                )
              }
            }

            REQUEST_DELETE_TOKEN_REQUEST -> {
              if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "- requestDeleteToken OK")
                resolve("Token deletado")
              } else {
                Log.i(TAG, "Couldn't requestDeleteToken - $resultCode")
                reject(E_REQUEST_DELETE_TOKEN, "Falha ao deletar token - código: $resultCode")
              }
            }

            CREATE_WALLET_REQUEST -> {
              if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "- createWallet OK")
                resolve(true) // true = wallet was created
              } else {
                Log.i(TAG, "Couldn't createWallet - $resultCode")
                reject(E_CREATE_WALLET, "Falha ao criar carteira - código: $resultCode")
              }
            }

            else -> {}
          }
          mPickerPromise = null
        }
      }
    })
  }

  @get:ReactMethod
  val isGooglePayDefaultNFCPayment: Boolean
    // Check if Google Pay is the default NFC payment app
    get() {
      Log.i(TAG, "--")
      Log.i(TAG, "> isGooglePayDefaultNFCPayment started")
      val nfcManager =
        reactContext.getSystemService(Context.NFC_SERVICE) as NfcManager
      val adapter = nfcManager.defaultAdapter
      val emulation = CardEmulation.getInstance(adapter)
      val result = emulation.isDefaultServiceForCategory(
        ComponentName(
          GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
          "com.google.android.gms.tapandpay.hce.service.TpHceService"
        ),
        CardEmulation.CATEGORY_PAYMENT
      )
      Log.i(TAG, "- isGooglePayDefaultNFCPayment = $result")
      return result
    }

  // Set Google Pay as the default NFC payment app
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/set-nfc-wallet#set_google_pay_as_the_default_nfc_payment_app
  @ReactMethod
  fun setGooglePayAsDefaultNFCPayment(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> setGooglePayAsDefaultNFCPayment started")
    mPickerPromise = promise
    val intent = Intent(CardEmulation.ACTION_CHANGE_DEFAULT)
    intent.putExtra(
      CardEmulation.EXTRA_CATEGORY,
      CardEmulation.CATEGORY_PAYMENT
    )
    intent.putExtra(
      CardEmulation.EXTRA_SERVICE_COMPONENT,
      ComponentName(
        "com.google.android.gms",
        "com.google.android.gms.tapandpay.hce.service.TpHceService"
      )
    )
    activity.startActivityForResult(
      intent,
      SET_GOOGLE_PAY_AS_DEFAULT_NFC_PAYMENT_REQUEST
    )
  }

  // Get the active wallet ID
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet#getactivewalletid
  @ReactMethod
  fun getActiveWalletId(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getActiveWalletId started")
    tapAndPayClient.activeWalletId
      .addOnCompleteListener { task: Task<String> ->
        if (task.isSuccessful) {
          Log.i(TAG, "- getActiveWalletId ${task.result}")
          promise.resolve(task.result)
        } else {
          val apiException = task.exception as ApiException?
          val statusCode = apiException?.statusCode ?: -1
          if (statusCode == TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET) {
            // There is no wallet. A wallet will be created when tokenize()
            // or pushTokenize() is called.
            // If necessary, you can call createWallet() to create a wallet
            // eagerly before constructing an OPC (Opaque Payment Card)
            // to pass into pushTokenize()
            Log.i(
              TAG,
              "Couldn't getActiveWalletId - ${task.exception?.message}"
            )
            promise.reject(E_GET_ACTIVE_WALLET_ID, "Sem carteira ativa")
          } else {
            Log.i(
              TAG,
              "Couldn't getActiveWalletId - ${task.exception?.message}"
            )
            promise.reject(E_GET_ACTIVE_WALLET_ID, task.exception)
          }
        }
      }
  }

  // Get secure wallet information (device ID + wallet account ID)
  @ReactMethod
  fun getSecureWalletInfo(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getSecureWalletInfo started")
    
    tapAndPayClient.activeWalletId
      .addOnCompleteListener { task: Task<String> ->
        if (task.isSuccessful) {
          val walletAccountID = task.result
          val deviceID = android.provider.Settings.Secure.getString(
            reactContext.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
          ) ?: ""
          
          val result: WritableMap = Arguments.createMap()
          result.putString("deviceID", deviceID)
          result.putString("walletAccountID", walletAccountID)
          
          Log.i(TAG, "- getSecureWalletInfo deviceID: $deviceID")
          Log.i(TAG, "- getSecureWalletInfo walletAccountID: $walletAccountID")
          promise.resolve(result)
        } else {
          val apiException = task.exception as ApiException?
          val statusCode = apiException?.statusCode ?: -1
          if (statusCode == TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET) {
            Log.i(TAG, "Couldn't getSecureWalletInfo - Sem carteira ativa")
            promise.reject(E_GET_ACTIVE_WALLET_ID, "Sem carteira ativa")
          } else {
            Log.i(TAG, "Couldn't getSecureWalletInfo - ${task.exception?.message}")
            promise.reject(E_GET_ACTIVE_WALLET_ID, task.exception)
          }
        }
      }
  }

  // Get the status of a token
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet#gettokenstatus
  @ReactMethod
  fun getTokenStatus(
    tokenServiceProvider: Int,
    tokenReferenceId: String,
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getTokenStatus started")
    tapAndPayClient.getTokenStatus(tokenServiceProvider, tokenReferenceId)
      .addOnCompleteListener { task: Task<TokenStatus> ->
        if (task.isSuccessful) {
          @TapAndPay.TokenState val tokenStateInt = task.result.tokenState
          val isSelected = task.result.isSelected
          val result: WritableMap = Arguments.createMap()
          result.putInt("tokenState", tokenStateInt)
          result.putBoolean("isSelected", isSelected)
          Log.i(TAG, "- getTokenStatus = $tokenStateInt")
          promise.resolve(result)
        } else {
          val apiException = task.exception as ApiException?
          val statusCode = apiException?.statusCode ?: -1
          Log.i(TAG, "Couldn't getTokenStatus - ${task.exception?.message}")
          if (statusCode == TapAndPayStatusCodes.TAP_AND_PAY_TOKEN_NOT_FOUND) {
            promise.reject(
              E_GET_TOKEN_STATUS,
              "Não foi possível encontrar o token"
            )
          } else {
            promise.reject(E_GET_TOKEN_STATUS, task.exception)
          }
        }
      }
  }

  // Get the current environment
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet#getenvironment
  @ReactMethod
  fun getEnvironment(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getEnvironment started")
    tapAndPayClient.environment
      .addOnCompleteListener { task: Task<String> ->
        if (task.isSuccessful) {
          Log.i(TAG, "- getEnvironment = ${task.result}")
          promise.resolve(task.result)
        } else {
          Log.i(TAG, "Couldn't getEnvironment - ${task.exception?.message}")
          promise.reject(E_GET_ENVIRONMENT, task.exception)
        }
      }
  }

  // Get the stable hardware ID
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet#getstablehardwareid
  @ReactMethod
  fun getStableHardwareId(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> getStableHardwareId started")
    tapAndPayClient.stableHardwareId
      .addOnCompleteListener { task: Task<String> ->
        if (task.isSuccessful) {
          Log.i(TAG, "- getStableHardwareId = ${task.result}")
          promise.resolve(task.result)
        } else {
          Log.i(
            TAG,
            "Couldn't getStableHardwareId - ${task.exception?.message}"
          )
          promise.reject(E_GET_STABLE_HARDWARE_ID, task.exception)
        }
      }
  }

  // List tokens in the active wallet
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet#listtokens
  @ReactMethod
  fun listTokens(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> listTokens started")
    tapAndPayClient.listTokens()
      .addOnCompleteListener { task: Task<List<TokenInfo>> ->
        if (task.isSuccessful) {
          for (token in task.result) {
            Log.d(TAG, "Found token with ID: " + token.issuerTokenId)
          }
          val result = task.result.map { tokenInfo ->
            tokenInfo.toSerializable()
          }
          Log.i(TAG, "- listTokens = ${result.size}")
          promise.resolve(result)
        } else {
          Log.i(TAG, "Couldn't listTokens - ${task.exception?.message}")
          promise.reject(E_LIST_TOKENS, task.exception)
        }
      }
  }

  // isTokenized
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet#istokenized
  @ReactMethod
  fun isTokenized(
    fpanLastFour: String,
    cardNetwork: Int,
    tokenServiceProvider: Int,
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> isTokenized started")
    val request = IsTokenizedRequest.Builder()
      .setIdentifier(fpanLastFour)
      .setNetwork(cardNetwork)
      .setTokenServiceProvider(tokenServiceProvider)
      .build()

    tapAndPayClient.isTokenized(request)
      .addOnCompleteListener { task: Task<Boolean> ->
        if (task.isSuccessful) {
          if (task.result) {
            Log.d(TAG, "Found a token with last four digits $fpanLastFour.")
          }
          Log.i(TAG, "- isTokenized = ${task.result}")
          promise.resolve(task.result)
        } else {
          Log.i(TAG, "Couldn't isTokenized - ${task.exception?.message}")
          promise.reject(E_IS_TOKENIZED, task.exception)
        }
      }
  }

  // Launch Google Pay to a specific token
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet#viewtoken
  @ReactMethod
  fun viewToken(
    tokenServiceProvider: Int,
    issuerTokenId: String,
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> viewToken started")
    val request = ViewTokenRequest.Builder()
      .setTokenServiceProvider(tokenServiceProvider)
      .setIssuerTokenId(issuerTokenId)
      .build()

    tapAndPayClient.viewToken(request)
      .addOnCompleteListener { task: Task<PendingIntent> ->
        if (task.isSuccessful) {
          try {
            Log.i(TAG, "- viewToken will send intent")
            task.result.send()
          } catch (e: PendingIntent.CanceledException) {
            Log.i(TAG, "Couldn't viewToken - ${e.message}")
            promise.reject(E_VIEW_TOKEN, e)
          }
        } else {
          Log.i(TAG, "Couldn't viewToken - ${task.exception?.message}")
          promise.reject(E_VIEW_TOKEN, task.exception)
        }
      }
  }

  // Add a listener for wallet updates
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet#add_a_listener_for_wallet_updates
  @ReactMethod
  fun registerDataChangedListener() {
    tapAndPayClient.registerDataChangedListener {}
  }

  // Push Provisioning operations
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations#push_provisioning_operations
  @ReactMethod
  fun pushTokenize(requestMap: ReadableMap, promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> pushTokenize started")
    mPickerPromise = promise

    val address = requestMap.getMap("userAddress")
    val userAddress = UserAddress.newBuilder()
      .setAddress1(address?.getString("addressOne").orEmpty())
      .setAddress2(address?.getString("addressTwo").orEmpty())
      .setCountryCode(address?.getString("countryCode").orEmpty())
      .setLocality(address?.getString("city").orEmpty())
      .setAdministrativeArea(address?.getString("administrativeArea").orEmpty())
      .setName(address?.getString("name").orEmpty())
      .setPhoneNumber(address?.getString("phoneNumber").orEmpty())
      .setPostalCode(address?.getString("postalCode").orEmpty())
      .build()

    // Converter network de String para Int
    val networkString = requestMap.getString("network")
    val network = networkString?.toIntOrNull() ?: -1

    val pushTokenizeRequest = PushTokenizeRequest.Builder()
      .setOpaquePaymentCard(
        requestMap.getString("opaquePaymentCard").orEmpty().encodeToByteArray()
      )
      .setNetwork(network)
      .setTokenServiceProvider(requestMap.getInt("tokenServiceProvider"))
      .setDisplayName(requestMap.getString("cardHolderName").orEmpty())
      .setLastDigits(requestMap.getString("lastDigits").orEmpty())
      .setUserAddress(userAddress)
      .build()

    Log.i(TAG, "🔄 Chamando tapAndPayClient.pushTokenize...")
    tapAndPayClient.pushTokenize(
      activity,
      pushTokenizeRequest,
      PUSH_TOKENIZE_REQUEST
    )
  }

  // Manual Provisioning
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations#manual-provisioning
  @ReactMethod
  fun tokenize(
    tokenReferenceId: String?,
    tokenServiceProvider: Int,
    displayName: String,
    cardNetwork: Int,
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> tokenize started")
    mPickerPromise = promise
    tapAndPayClient.tokenize(
      activity,
      tokenReferenceId,
      tokenServiceProvider,
      displayName,
      cardNetwork,
      TOKENIZE_REQUEST
    )
  }

  // Setting the default token
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations#setting_the_default_token
  @ReactMethod
  fun requestSelectToken(
    tokenReferenceId: String,
    tokenServiceProvider: Int,
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> requestSelectToken started")
    mPickerPromise = promise
    tapAndPayClient.requestSelectToken(
      activity,
      tokenReferenceId,
      tokenServiceProvider,
      REQUEST_SELECT_TOKEN_REQUEST
    )
  }

  // Token Deletion
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations#token_deletion
  @ReactMethod
  fun requestDeleteToken(
    tokenReferenceId: String,
    tokenServiceProvider: Int,
    promise: Promise
  ) {
    Log.i(TAG, "--")
    Log.i(TAG, "> requestDeleteToken started")
    mPickerPromise = promise
    tapAndPayClient.requestDeleteToken(
      activity,
      tokenReferenceId,
      tokenServiceProvider,
      REQUEST_DELETE_TOKEN_REQUEST
    )
  }

  // Create wallet
  // https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations#create_wallet
  @ReactMethod
  fun createWallet(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> createWallet started")
    mPickerPromise = promise
    tapAndPayClient.createWallet(
      activity,
      CREATE_WALLET_REQUEST
    )
  }

  // Create wallet if it doesn't exist
  @ReactMethod
  fun createWalletIfNeeded(promise: Promise) {
    Log.i(TAG, "--")
    Log.i(TAG, "> createWalletIfNeeded started")
    
    // First check if wallet already exists
    tapAndPayClient.activeWalletId
      .addOnCompleteListener { task: Task<String> ->
        if (task.isSuccessful) {
          // Wallet already exists
          Log.i(TAG, "- Wallet já existe: ${task.result}")
          promise.resolve(false) // false = wallet already existed
        } else {
          val apiException = task.exception as ApiException?
          val statusCode = apiException?.statusCode ?: -1
          if (statusCode == TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET) {
            // No wallet exists, create one
            Log.i(TAG, "- Nenhuma carteira encontrada, criando nova...")
            mPickerPromise = promise
            tapAndPayClient.createWallet(
              activity,
              CREATE_WALLET_REQUEST
            )
          } else {
            Log.i(TAG, "Couldn't check wallet status - ${task.exception?.message}")
            promise.reject(E_CREATE_WALLET, task.exception)
          }
        }
      }
  }

  override fun getConstants(): MutableMap<String, Any> {
    Log.i(TAG, "--")
    Log.i(TAG, "> getConstants started")
    return hashMapOf(
      "TOKEN_PROVIDER_ELO" to TapAndPay.TOKEN_PROVIDER_ELO,
      "CARD_NETWORK_ELO" to TapAndPay.CARD_NETWORK_ELO,
      "TOKEN_STATE_UNTOKENIZED" to TapAndPay.TOKEN_STATE_UNTOKENIZED,
      "TOKEN_STATE_PENDING" to TapAndPay.TOKEN_STATE_PENDING,
      "TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION" to TapAndPay.TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION,
      "TOKEN_STATE_SUSPENDED" to TapAndPay.TOKEN_STATE_SUSPENDED,
      "TOKEN_STATE_ACTIVE" to TapAndPay.TOKEN_STATE_ACTIVE,
      "TOKEN_STATE_FELICA_PENDING_PROVISIONING" to TapAndPay.TOKEN_STATE_FELICA_PENDING_PROVISIONING
    )
  }

  override fun getName(): String {
    return TAG
  }

  companion object {
    private const val SET_GOOGLE_PAY_AS_DEFAULT_NFC_PAYMENT_REQUEST = 1
    private const val PUSH_TOKENIZE_REQUEST = 2
    private const val TOKENIZE_REQUEST = 3
    private const val REQUEST_SELECT_TOKEN_REQUEST = 4
    private const val REQUEST_DELETE_TOKEN_REQUEST = 5
    private const val CREATE_WALLET_REQUEST = 6

    private const val TAG = "GoogleTapAndPay"
    private const val E_GOOGLE_PAY_AS_DEFAULT_NFC_PAYMENT =
      "E_GOOGLE_PAY_AS_DEFAULT_NFC_PAYMENT"
    private const val E_GET_ACTIVE_WALLET_ID = "E_GET_ACTIVE_WALLET_ID"
    private const val E_GET_TOKEN_STATUS = "E_GET_TOKEN_STATUS"
    private const val E_GET_ENVIRONMENT = "E_GET_ENVIRONMENT"
    private const val E_GET_STABLE_HARDWARE_ID = "E_GET_STABLE_HARDWARE_ID"
    private const val E_LIST_TOKENS = "E_LIST_TOKENS"
    private const val E_IS_TOKENIZED = "E_IS_TOKENIZED"
    private const val E_VIEW_TOKEN = "E_VIEW_TOKEN"
    private const val E_PUSH_TOKENIZE = "E_PUSH_TOKENIZE"
    private const val E_TOKENIZE = "E_TOKENIZE"
    private const val E_REQUEST_SELECT_TOKEN = "E_REQUEST_SELECT_TOKEN"
    private const val E_REQUEST_DELETE_TOKEN = "E_REQUEST_DELETE_TOKEN"
    private const val E_CREATE_WALLET = "E_CREATE_WALLET"
  }
}
