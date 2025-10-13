package com.builders.wallet.googletapandpay

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tapandpay.TapAndPay
import com.google.android.gms.tapandpay.TapAndPayClient
import com.google.android.gms.tapandpay.issuer.*
import com.google.android.gms.tapandpay.TapAndPayStatusCodes
import com.google.android.gms.tapandpay.issuer.IsTokenizedRequest
import com.google.android.gms.tapandpay.issuer.TokenInfo
import com.google.android.gms.tapandpay.issuer.TokenStatus
import com.google.android.gms.tapandpay.issuer.ViewTokenRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.builders.wallet.googletapandpay.util.ErrorCode

/**
 * Implementação LIMPA do Google Wallet - USA DIRETAMENTE O SDK (SEM REFLEXÃO)
 *
 * Esta versão só é compilada quando GOOGLE_WALLET_ENABLED = true
 * Requer a dependência: com.google.android.gms:play-services-tapandpay
 */
class GoogleWalletImplementation(
    private val reactContext: com.facebook.react.bridge.ReactApplicationContext
) : GoogleWalletContract {

    private var tapAndPayClient: TapAndPayClient? = null
    private var activity: Activity? = null
    private var mPickerPromise: Promise? = null
    private var intentListenerActive: Boolean = false

    init {
        try {
            // Inicializar TapAndPayClient diretamente (sem reflexão!)
            tapAndPayClient = TapAndPay.getClient(reactContext)

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
                                            Log.w(TAG, "PUSH_TOKENIZE_ERROR: Falha ao tokenizar por push - Token ID é null ou vazio - result_code:null")
                                            reject("PUSH_TOKENIZE_ERROR", "Falha ao tokenizar por push - Token ID é null ou vazio - result_code:null")
                                        } else {
                                            Log.i(TAG, "Push tokenize OK - Token ID: $tokenId")
                                            resolve(tokenId)
                                        }
                                    }
                                    Activity.RESULT_CANCELED -> {
                                        val errorCodeName = ErrorCode.getErrorCodeName(TapAndPayStatusCodes.TAP_AND_PAY_USER_CANCELED_FLOW)
                                        val errorMessage = ErrorCode.getErrorMessage(TapAndPayStatusCodes.TAP_AND_PAY_USER_CANCELED_FLOW)
                                        Log.w(TAG, "PUSH_TOKENIZE_ERROR: $errorMessage ($errorCodeName) - result_code:$resultCode")
                                        reject("PUSH_TOKENIZE_ERROR", "$errorMessage ($errorCodeName) - result_code:$resultCode")
                                    }
                                    else -> {
                                        // Usar resultCode diretamente para obter código de erro
                                        val errorCodeName = ErrorCode.getErrorCodeName(resultCode)
                                        val message = ErrorCode.getErrorMessage(resultCode)
                                        val errorMessage = "$message ($errorCodeName) - result_code:$resultCode"
                                        
                                        Log.w(TAG, "PUSH_TOKENIZE_ERROR: $errorMessage")
                                        reject("PUSH_TOKENIZE_ERROR", errorMessage)
                                    }
                                }
                            }
                            CREATE_WALLET_REQUEST -> {
                                if (resultCode == Activity.RESULT_OK) {
                                    Log.i(TAG, "Carteira criada com sucesso")
                                    resolve(true)
                                } else if (resultCode == Activity.RESULT_CANCELED) {
                                    val errorCodeName = ErrorCode.getErrorCodeName(TapAndPayStatusCodes.TAP_AND_PAY_USER_CANCELED_FLOW)
                                    val errorMessage = ErrorCode.getErrorMessage(TapAndPayStatusCodes.TAP_AND_PAY_USER_CANCELED_FLOW)
                                    Log.w(TAG, "CREATE_WALLET_ERROR: $errorMessage ($errorCodeName) - result_code:$resultCode")
                                    reject("CREATE_WALLET_ERROR", "$errorMessage ($errorCodeName) - result_code:$resultCode")
                                } else {
                                    // Usar resultCode diretamente para obter código de erro
                                    val errorCodeName = ErrorCode.getErrorCodeName(resultCode)
                                    val message = ErrorCode.getErrorMessage(resultCode)
                                    val errorMessage = "$message ($errorCodeName) - result_code:$resultCode"
                                    
                                    Log.w(TAG, "CREATE_WALLET_ERROR: $errorMessage")
                                    reject("CREATE_WALLET_ERROR", errorMessage)
                                }
                            }
                            else -> {}
                        }
                        mPickerPromise = null
                    }
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao inicializar Google Pay SDK: ${e.message}")
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android
    override fun checkWalletAvailability(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] checkWalletAvailability chamado")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Log.d(TAG, "✅ [GOOGLE] Android ${Build.VERSION.SDK_INT} suportado e SDK disponível")
                promise.resolve(true)
            } else {
                Log.w(TAG, "❌ [GOOGLE] Android ${Build.VERSION.SDK_INT} não suportado")
                promise.resolve(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "CHECK_WALLET_AVAILABILITY_ERROR: ${e.message}", e)
            promise.reject("CHECK_WALLET_AVAILABILITY_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?pli=1&authuser=1&hl=pt-br#getactivewalletid
    override fun getSecureWalletInfo(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] getSecureWalletInfo chamado")
        try {
            if (tapAndPayClient == null) {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
                return
            }

            // Código LIMPO - usa diretamente o SDK com segurança nula!
            tapAndPayClient?.let { client ->
                client.activeWalletId.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val walletId = task.result
                        if (walletId != null && walletId.isNotEmpty()) {
                            Log.d(TAG, "✅ [GOOGLE] Wallet ID obtido: $walletId")
                            val result = Arguments.createMap()
                            result.putString("deviceID", "google_device_${walletId.hashCode()}")
                            result.putString("walletAccountID", walletId)
                            promise.resolve(result)
                        } else {
                            Log.w(TAG, "GET_WALLET_INFO_ERROR: Wallet ID é null ou vazio - result_code:null")
                            promise.reject("GET_WALLET_INFO_ERROR", "Wallet ID é null ou vazio - result_code:null")
                        }
                    } else {
                        val errorMessage = getTaskErrorMessage(task)
                        Log.w(TAG, "GET_WALLET_INFO_ERROR: $errorMessage")
                        promise.reject("GET_WALLET_INFO_ERROR", errorMessage)
                    }
                }
            } ?: run {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "GET_SECURE_WALLET_INFO_ERROR: ${e.message}", e)
            promise.reject("GET_SECURE_WALLET_INFO_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?pli=1&authuser=1&hl=pt-br#gettokenstatus
    override fun getTokenStatus(tokenServiceProvider: Int, tokenReferenceId: String, promise: Promise) {
        Log.i(TAG, "--")
        Log.i(TAG, "> getTokenStatus started")
        try {
            if (tapAndPayClient == null) {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
                return
            }

            // Código LIMPO com segurança nula!
            tapAndPayClient?.let { client ->
                client.getTokenStatus(tokenServiceProvider, tokenReferenceId).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val tokenStatus = task.result
                        if (tokenStatus != null) {
                            Log.d(TAG, "✅ [GOOGLE] TokenStatus obtido com sucesso")
                            val result = Arguments.createMap()
                            result.putInt("tokenState", tokenStatus.tokenState)
                            result.putBoolean("isSelected", tokenStatus.isSelected)
                            Log.i(TAG, "- getTokenStatus = ${tokenStatus.tokenState}")
                            promise.resolve(result)
                        } else {
                            Log.w(TAG, "GET_TOKEN_STATUS_ERROR: TokenStatus é null - result_code:null")
                            promise.reject("GET_TOKEN_STATUS_ERROR", "TokenStatus é null - result_code:null")
                        }
                    } else {
                        val errorMessage = getTaskErrorMessage(task)
                        Log.w(TAG, "GET_TOKEN_STATUS_ERROR: $errorMessage")
                        promise.reject("GET_TOKEN_STATUS_ERROR", errorMessage)
                    }
                }
            } ?: run {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "GET_TOKEN_STATUS_ERROR: ${e.message}", e)
            promise.reject("GET_TOKEN_STATUS_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?pli=1&authuser=1&hl=pt-br#getenvironment
    override fun getEnvironment(promise: Promise) {
        Log.i(TAG, "--")
        Log.i(TAG, "> getEnvironment started")
        try {
            if (tapAndPayClient == null) {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
                return
            }

            // Código LIMPO com segurança nula!
            tapAndPayClient?.let { client ->
                client.environment.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val environment = task.result
                        if (environment != null) {
                            Log.i(TAG, "- getEnvironment = $environment")
                            promise.resolve(environment)
                        } else {
                            Log.w(TAG, "GET_ENVIRONMENT_ERROR: Environment é null - result_code:null")
                            promise.reject("GET_ENVIRONMENT_ERROR", "Environment é null - result_code:null")
                        }
                    } else {
                        val errorMessage = getTaskErrorMessage(task)
                        Log.w(TAG, "GET_ENVIRONMENT_ERROR: $errorMessage")
                        promise.reject("GET_ENVIRONMENT_ERROR", errorMessage)
                    }
                }
            } ?: run {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "GET_ENVIRONMENT_ERROR: ${e.message}", e)
            promise.reject("GET_ENVIRONMENT_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?pli=1&authuser=1&hl=pt-br#istokenized
    override fun isTokenized(
        fpanLastFour: String,
        cardNetwork: Int,
        tokenServiceProvider: Int,
        promise: Promise
    ) {
        Log.i(TAG, "--")
        Log.i(TAG, "> isTokenized started")
        try {
            if (tapAndPayClient == null) {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
                return
            }

            // Código LIMPO!
            val request = IsTokenizedRequest.Builder()
                .setIdentifier(fpanLastFour)
                .setNetwork(cardNetwork)
                .setTokenServiceProvider(tokenServiceProvider)
                .build()

            tapAndPayClient!!.isTokenized(request).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isTokenized = task.result ?: false
                    if (isTokenized) {
                        Log.d(TAG, "Found a token with last four digits $fpanLastFour.")
                    }
                    Log.i(TAG, "- isTokenized = $isTokenized")
                    promise.resolve(isTokenized)
                } else {
                    val errorMessage = getTaskErrorMessage(task)
                    Log.w(TAG, "IS_TOKENIZED_ERROR: $errorMessage")
                    promise.reject("IS_TOKENIZED_ERROR", errorMessage)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "IS_TOKENIZED_ERROR: ${e.message}", e)
            promise.reject("IS_TOKENIZED_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?pli=1&authuser=1&hl=pt-br#viewtoken
    override fun viewToken(
        tokenServiceProvider: Int,
        issuerTokenId: String,
        promise: Promise
    ) {
        Log.i(TAG, "--")
        Log.i(TAG, "> viewToken started - Provider: $tokenServiceProvider, TokenId: $issuerTokenId")
        try {
            if (tapAndPayClient == null) {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
                return
            }

            activity = reactContext.currentActivity
            if (activity == null) {
                Log.w(TAG, "NO_ACTIVITY: Nenhuma atividade disponível")
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }

            // Código LIMPO!
            tapAndPayClient!!.listTokens().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val tokenList = task.result
                    if (tokenList != null) {
                        val targetToken = tokenList.find {
                            it.issuerTokenId == issuerTokenId && it.tokenServiceProvider == tokenServiceProvider
                        }

                        if (targetToken != null) {
                            Log.d(TAG, "✅ [GOOGLE] Token encontrado: $issuerTokenId")

                            val tokenData = Arguments.createMap()
                            tokenData.putString("issuerTokenId", targetToken.issuerTokenId)
                            tokenData.putString("issuerName", targetToken.issuerName)
                            tokenData.putString("fpanLastFour", targetToken.fpanLastFour)
                            tokenData.putString("dpanLastFour", targetToken.dpanLastFour)
                            tokenData.putInt("tokenServiceProvider", targetToken.tokenServiceProvider)
                            tokenData.putInt("network", targetToken.network)
                            tokenData.putInt("tokenState", targetToken.tokenState)
                            tokenData.putBoolean("isDefaultToken", targetToken.isDefaultToken)
                            tokenData.putString("portfolioName", targetToken.portfolioName)

                            val viewRequest = ViewTokenRequest.Builder()
                                .setTokenServiceProvider(tokenServiceProvider)
                                .setIssuerTokenId(issuerTokenId)
                                .build()

                            tapAndPayClient!!.viewToken(viewRequest).addOnCompleteListener { viewTask ->
                                if (viewTask.isSuccessful) {
                                    val pendingIntent = viewTask.result
                                    try {
                                        pendingIntent?.send()
                                        Log.d(TAG, "✅ [GOOGLE] PendingIntent enviado com sucesso")
                                        promise.resolve(tokenData)
                                    } catch (e: Exception) {
                                        Log.w(TAG, "VIEW_TOKEN_ERROR: Erro ao enviar PendingIntent: ${e.message} - result_code:unknown")
                                        promise.reject("VIEW_TOKEN_ERROR", "Erro ao enviar PendingIntent: ${e.message} - result_code:unknown")
                                    }
                                } else {
                                    val errorMessage = getTaskErrorMessage(viewTask)
                                    Log.w(TAG, "VIEW_TOKEN_ERROR: $errorMessage")
                                    promise.reject("VIEW_TOKEN_ERROR", errorMessage)
                                }
                            }
                        } else {
                            promise.resolve(null)
                        }
                    } else {
                        Log.w(TAG, "VIEW_TOKEN_ERROR: Lista de tokens é null - result_code:null")
                        promise.reject("VIEW_TOKEN_ERROR", "Lista de tokens é null - result_code:null")
                    }
                } else {
                    val errorMessage = getTaskErrorMessage(task)
                    Log.w(TAG, "VIEW_TOKEN_ERROR: $errorMessage")
                    promise.reject("VIEW_TOKEN_ERROR", errorMessage)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "VIEW_TOKEN_ERROR: ${e.message}", e)
            promise.reject("VIEW_TOKEN_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations?authuser=1&hl=pt-br#push_provisioning_operations
    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] addCardToWallet chamado")
        try {
            if (tapAndPayClient == null) {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
                return
            }

            activity = reactContext.currentActivity
            if (activity == null) {
                Log.w(TAG, "NO_ACTIVITY: Nenhuma atividade disponível")
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }

            mPickerPromise = promise

            val validationError = validateCardData(cardData)
            if (validationError != null) {
                Log.w(TAG, "INVALID_CARD_DATA: $validationError")
                promise.reject("INVALID_CARD_DATA", validationError)
                return
            }

            val address = cardData.getMap("address")!!
            val card = cardData.getMap("card")!!

            // Código LIMPO!
            val userAddress = UserAddress.Builder()
                .setAddress1(address.getString("address1") ?: "")
                .setAddress2(address.getString("address2") ?: "")
                .setCountryCode(address.getString("countryCode") ?: "")
                .setLocality(address.getString("locality") ?: "")
                .setAdministrativeArea(address.getString("administrativeArea") ?: "")
                .setName(address.getString("name") ?: "")
                .setPhoneNumber(address.getString("phoneNumber") ?: "")
                .setPostalCode(address.getString("postalCode") ?: "")
                .build()

            val pushTokenizeRequest = PushTokenizeRequest.Builder()
                .setOpaquePaymentCard(card.getString("opaquePaymentCard")!!.toByteArray())
                .setNetwork(card.getInt("network"))
                .setTokenServiceProvider(card.getInt("tokenServiceProvider"))
                .setDisplayName(card.getString("displayName")!!)
                .setLastDigits(card.getString("lastDigits")!!)
                .setUserAddress(userAddress)
                .build()

            tapAndPayClient!!.pushTokenize(activity!!, pushTokenizeRequest, PUSH_TOKENIZE_REQUEST)
            Log.d(TAG, "✅ [GOOGLE] pushTokenize chamado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "ADD_CARD_TO_WALLET_ERROR: ${e.message}", e)
            promise.reject("ADD_CARD_TO_WALLET_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations?authuser=1&hl=pt-br#create_wallet
    override fun createWalletIfNeeded(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] createWalletIfNeeded chamado")
        try {
            if (tapAndPayClient == null) {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
                return
            }

            activity = reactContext.currentActivity
            if (activity == null) {
                Log.w(TAG, "NO_ACTIVITY: Nenhuma atividade disponível")
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }

            mPickerPromise = promise
            tapAndPayClient!!.createWallet(activity!!, CREATE_WALLET_REQUEST)
            Log.d(TAG, "✅ [GOOGLE] createWalletIfNeeded executado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "CREATE_WALLET_IF_NEEDED_ERROR: ${e.message}", e)
            promise.reject("CREATE_WALLET_IF_NEEDED_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?hl=pt-br&authuser=1#listtokens
    override fun listTokens(promise: Promise) {
        Log.i(TAG, "--")
        Log.i(TAG, "> listTokens started")
        try {
            if (tapAndPayClient == null) {
                Log.w(TAG, "TAP_AND_PAY_CLIENT_NOT_AVAILABLE: Cliente TapAndPay não foi inicializado")
                promise.reject("TAP_AND_PAY_CLIENT_NOT_AVAILABLE", "Cliente TapAndPay não foi inicializado")
                return
            }

            // Código LIMPO!
            tapAndPayClient!!.listTokens().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val tokenList = task.result
                    if (tokenList != null) {
                        Log.d(TAG, "✅ [GOOGLE] Lista de tokens obtida: ${tokenList.size} tokens")

                        val writableArray = Arguments.createArray()
                        tokenList.forEach { tokenInfo ->
                            val tokenMap = Arguments.createMap()
                            tokenMap.putString("issuerTokenId", tokenInfo.issuerTokenId)
                            tokenMap.putString("lastDigits", tokenInfo.fpanLastFour)
                            tokenMap.putString("displayName", tokenInfo.issuerName)
                            tokenMap.putInt("tokenState", tokenInfo.tokenState)
                            tokenMap.putInt("network", tokenInfo.network)
                            writableArray.pushMap(tokenMap)
                        }

                        Log.i(TAG, "- listTokens = ${tokenList.size}")
                        promise.resolve(writableArray)
                    } else {
                        Log.w(TAG, "LIST_TOKENS_ERROR: Lista de tokens é null - result_code:null")
                        promise.reject("LIST_TOKENS_ERROR", "Lista de tokens é null - result_code:null")
                    }
                } else {
                    val errorMessage = getTaskErrorMessage(task)
                    Log.w(TAG, "LIST_TOKENS_ERROR: $errorMessage")
                    promise.reject("LIST_TOKENS_ERROR", errorMessage)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "LIST_TOKENS_ERROR: ${e.message}", e)
            promise.reject("LIST_TOKENS_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?hl=pt-br&authuser=1#add_a_listener_for_wallet_updates
    override fun setIntentListener(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] setIntentListener chamado")
        try {
            intentListenerActive = true
            checkPendingDataFromMainActivity()
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "SET_INTENT_LISTENER_ERROR: ${e.message}", e)
            promise.reject("SET_INTENT_LISTENER_ERROR", e.message, e)
        }
    }

    override fun removeIntentListener(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] removeIntentListener chamado")
        try {
            intentListenerActive = false
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "REMOVE_INTENT_LISTENER_ERROR: ${e.message}", e)
            promise.reject("REMOVE_INTENT_LISTENER_ERROR", e.message, e)
        }
    }

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations?authuser=1&hl=pt-br#handling_result_callbacks
    override fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] setActivationResult chamado - Status: $status")
        try {
            activity = reactContext.currentActivity
            if (activity == null) {
                Log.w(TAG, "NO_ACTIVITY: Nenhuma atividade disponível")
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }

            val validStatuses = listOf("approved", "declined", "failure")
            if (!validStatuses.contains(status)) {
                Log.w(TAG, "INVALID_STATUS: Status deve ser: approved, declined ou failure")
                promise.reject("INVALID_STATUS", "Status deve ser: approved, declined ou failure")
                return
            }

            val resultIntent = Intent()
            resultIntent.putExtra("BANKING_APP_ACTIVATION_RESPONSE", status)

            if (activationCode != null && activationCode.isNotEmpty() && status == "approved") {
                resultIntent.putExtra("BANKING_APP_ACTIVATION_CODE", activationCode)
            }

            activity?.setResult(Activity.RESULT_OK, resultIntent)
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "SET_ACTIVATION_RESULT_ERROR: ${e.message}", e)
            promise.reject("SET_ACTIVATION_RESULT_ERROR", e.message, e)
        }
    }

    override fun finishActivity(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] finishActivity chamado")
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

    // https://developers.google.com/pay/issuers/apis/push-provisioning/android/enumerated-values?authuser=1&hl=pt-br#tapandpay_status_codes
    override fun getConstants(): MutableMap<String, Any> {
        return hashMapOf<String, Any>().apply {
            put("SDK_NAME", "GoogleWallet")

            // Google Token Provider - Usar diretamente as constantes do SDK!
            put("TOKEN_PROVIDER_AMEX", TapAndPay.TOKEN_PROVIDER_AMEX)
            put("TOKEN_PROVIDER_DISCOVER", TapAndPay.TOKEN_PROVIDER_DISCOVER)
            put("TOKEN_PROVIDER_JCB", TapAndPay.TOKEN_PROVIDER_JCB)
            put("TOKEN_PROVIDER_MASTERCARD", TapAndPay.TOKEN_PROVIDER_MASTERCARD)
            put("TOKEN_PROVIDER_VISA", TapAndPay.TOKEN_PROVIDER_VISA)
            put("TOKEN_PROVIDER_ELO", TapAndPay.TOKEN_PROVIDER_ELO)

            // Google Card Network
            put("CARD_NETWORK_AMEX", TapAndPay.CARD_NETWORK_AMEX)
            put("CARD_NETWORK_DISCOVER", TapAndPay.CARD_NETWORK_DISCOVER)
            put("CARD_NETWORK_MASTERCARD", TapAndPay.CARD_NETWORK_MASTERCARD)
            put("CARD_NETWORK_QUICPAY", TapAndPay.CARD_NETWORK_QUICPAY)
            put("CARD_NETWORK_PRIVATE_LABEL", TapAndPay.CARD_NETWORK_PRIVATE_LABEL)
            put("CARD_NETWORK_VISA", TapAndPay.CARD_NETWORK_VISA)
            put("CARD_NETWORK_ELO", TapAndPay.CARD_NETWORK_ELO)

            // Google Token State
            put("TOKEN_STATE_ACTIVE", TapAndPay.TOKEN_STATE_ACTIVE)
            put("TOKEN_STATE_PENDING", TapAndPay.TOKEN_STATE_PENDING)
            put("TOKEN_STATE_SUSPENDED", TapAndPay.TOKEN_STATE_SUSPENDED)
            put("TOKEN_STATE_UNTOKENIZED", TapAndPay.TOKEN_STATE_UNTOKENIZED)
            put("TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION", TapAndPay.TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION)
            put("TOKEN_STATE_FELICA_PENDING_PROVISIONING", TapAndPay.TOKEN_STATE_FELICA_PENDING_PROVISIONING)

            // TapAndPay Status Codes - valores reais do SDK
            put("TAP_AND_PAY_NO_ACTIVE_WALLET", TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET)
            put("TAP_AND_PAY_TOKEN_NOT_FOUND", TapAndPayStatusCodes.TAP_AND_PAY_TOKEN_NOT_FOUND)
            put("TAP_AND_PAY_INVALID_TOKEN_STATE", TapAndPayStatusCodes.TAP_AND_PAY_INVALID_TOKEN_STATE)
            put("TAP_AND_PAY_ATTESTATION_ERROR", TapAndPayStatusCodes.TAP_AND_PAY_ATTESTATION_ERROR)
            put("TAP_AND_PAY_UNAVAILABLE", TapAndPayStatusCodes.TAP_AND_PAY_UNAVAILABLE)
            put("TAP_AND_PAY_SAVE_CARD_ERROR", TapAndPayStatusCodes.TAP_AND_PAY_SAVE_CARD_ERROR)
            put("TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION", TapAndPayStatusCodes.TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION)
            put("TAP_AND_PAY_TOKENIZATION_DECLINED", TapAndPayStatusCodes.TAP_AND_PAY_TOKENIZATION_DECLINED)
            put("TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR", TapAndPayStatusCodes.TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR)
            put("TAP_AND_PAY_TOKENIZE_ERROR", TapAndPayStatusCodes.TAP_AND_PAY_TOKENIZE_ERROR)
            put("TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED", TapAndPayStatusCodes.TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED)
            put("TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT", TapAndPayStatusCodes.TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT)
            put("TAP_AND_PAY_USER_CANCELED_FLOW", TapAndPayStatusCodes.TAP_AND_PAY_USER_CANCELED_FLOW)
            put("TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED", TapAndPayStatusCodes.TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED)

            // Google Common Status Codes - usando constantes do SDK!
            put("SUCCESS", CommonStatusCodes.SUCCESS)
            put("SUCCESS_CACHE", CommonStatusCodes.SUCCESS_CACHE)
            put("SERVICE_VERSION_UPDATE_REQUIRED", CommonStatusCodes.SERVICE_VERSION_UPDATE_REQUIRED)
            put("SERVICE_DISABLED", CommonStatusCodes.SERVICE_DISABLED)
            put("SIGN_IN_REQUIRED", CommonStatusCodes.SIGN_IN_REQUIRED)
            put("INVALID_ACCOUNT", CommonStatusCodes.INVALID_ACCOUNT)
            put("RESOLUTION_REQUIRED", CommonStatusCodes.RESOLUTION_REQUIRED)
            put("NETWORK_ERROR", CommonStatusCodes.NETWORK_ERROR)
            put("INTERNAL_ERROR", CommonStatusCodes.INTERNAL_ERROR)
            put("DEVELOPER_ERROR", CommonStatusCodes.DEVELOPER_ERROR)
            put("ERROR", CommonStatusCodes.ERROR)
            put("INTERRUPTED", CommonStatusCodes.INTERRUPTED)
            put("TIMEOUT", CommonStatusCodes.TIMEOUT)
            put("CANCELED", CommonStatusCodes.CANCELED)
            put("API_NOT_CONNECTED", CommonStatusCodes.API_NOT_CONNECTED)
            put("REMOTE_EXCEPTION", CommonStatusCodes.REMOTE_EXCEPTION)
            put("CONNECTION_SUSPENDED_DURING_CALL", CommonStatusCodes.CONNECTION_SUSPENDED_DURING_CALL)
            put("RECONNECTION_TIMED_OUT_DURING_UPDATE", CommonStatusCodes.RECONNECTION_TIMED_OUT_DURING_UPDATE)
            put("RECONNECTION_TIMED_OUT", CommonStatusCodes.RECONNECTION_TIMED_OUT)
        }
    }

    private fun checkPendingDataFromMainActivity() {
        Log.d(TAG, "🔍 [GOOGLE] Verificando dados pendentes...")
        try {
            // Verificar se há dados pendentes
            val hasData = hasPendingData()
            
            if (hasData) {
                Log.d(TAG, "✅ [GOOGLE] Dados pendentes encontrados")
                
                // Obter os dados pendentes sem limpar
                val data = getPendingIntentDataWithoutClearing()
                val action = getPendingIntentAction()
                val callingPackage = getPendingCallingPackage()
                
                if (data != null && data.isNotEmpty()) {
                    Log.d(TAG, "📋 [GOOGLE] Processando dados pendentes: ${data.length} caracteres")
                    Log.d(TAG, "📋 [GOOGLE] Action: $action, CallingPackage: $callingPackage")
                    
                    // Verificar se action e callingPackage estão disponíveis
                    if (action == null) {
                        Log.e(TAG, "❌ [GOOGLE] Action é null - não é possível processar intent")
                        return
                    }
                    
                    if (callingPackage == null) {
                        Log.e(TAG, "❌ [GOOGLE] CallingPackage é null - não é possível processar intent")
                        return
                    }
                    
                    // Processar os dados como um intent usando os valores reais
                    processWalletIntentData(data, action, callingPackage)
                    
                    // Limpar dados após processamento bem-sucedido
                    clearPendingData()
                } else {
                    Log.w(TAG, "⚠️ [GOOGLE] Dados pendentes são null ou vazios")
                }
            } else {
                Log.d(TAG, "🔍 [GOOGLE] Nenhum dado pendente")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao verificar dados pendentes: ${e.message}", e)
        }
    }

    /**
     * Processa dados de intent e envia evento para React Native
     */
    private fun processWalletIntentData(data: String, action: String, callingPackage: String) {
        Log.d(TAG, "🔍 [GOOGLE] processWalletIntentData chamado")
        try {
            Log.d(TAG, "✅ [GOOGLE] Intent processado: $action")
            
            // Determinar o tipo de intent baseado na action
            val intentType = if (action.endsWith(".action.ACTIVATE_TOKEN")) {
                "ACTIVATE_TOKEN"
            } else {
                "WALLET_INTENT"
            }
            
            // Decodificar dados de base64 para string normal
            var decodedData = data
            var dataFormat = "raw"
            
            try {
                // Tentar decodificar como base64
                val decodedBytes = android.util.Base64.decode(data, android.util.Base64.DEFAULT)
                decodedData = String(decodedBytes, Charsets.UTF_8)
                dataFormat = "base64_decoded"
                Log.d(TAG, "🔍 [GOOGLE] Dados decodificados com sucesso: ${decodedData.length} caracteres")
            } catch (e: Exception) {
                // Se falhar ao decodificar, usar dados originais
                Log.w(TAG, "⚠️ [GOOGLE] Não foi possível decodificar como base64, usando dados originais: ${e.message}")
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
            
            Log.d(TAG, "🔍 [GOOGLE] Evento preparado - Action: $action, Type: $intentType, Format: $dataFormat")
            
            // Enviar evento para React Native
            sendEventToReactNative("GoogleWalletIntentReceived", eventData)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao processar dados da intent: ${e.message}", e)
        }
    }

    private fun sendEventToReactNative(eventName: String, eventData: WritableMap) {
        try {
            Log.d(TAG, "🔍 [GOOGLE] Enviando evento para React Native: $eventName")
            reactContext
                .getJSModule(com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, eventData)
            Log.d(TAG, "✅ [GOOGLE] Evento enviado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao enviar evento para React Native: ${e.message}", e)
        }
    }

    /**
     * Processa erro de Task do Google e retorna mensagem formatada
     */
    private fun getTaskErrorMessage(task: Task<*>): String {
        val exception = task.exception
        return if (exception is com.google.android.gms.common.api.ApiException) {
            val statusCode = exception.statusCode
            val errorCodeName = ErrorCode.getErrorCodeName(statusCode)
            val errorMessage = ErrorCode.getErrorMessage(statusCode)
            "$errorMessage ($errorCodeName) - result_code:$statusCode"
        } else {
            val message = exception?.message ?: "Erro desconhecido"
            "$message - result_code:unknown"
        }
    }

    private fun validateCardData(cardData: ReadableMap): String? {
        val address = cardData.getMap("address")
        val card = cardData.getMap("card")

        if (address == null) return "Campo 'address' é obrigatório"
        if (card == null) return "Campo 'card' é obrigatório"

        val opaquePaymentCard = card.getString("opaquePaymentCard")
        if (opaquePaymentCard.isNullOrEmpty()) return "Campo 'opaquePaymentCard' é obrigatório"

        return null
    }

    companion object {
        private const val TAG = "GoogleWallet"
        private const val PUSH_TOKENIZE_REQUEST = 2
        private const val CREATE_WALLET_REQUEST = 6
        
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
        fun processIntent(activity: Activity, intent: Intent) {
            Log.d(TAG, "🔍 [GOOGLE] processIntent chamado")
            
            Log.d(TAG, "🔍 [GOOGLE] Intent encontrada: ${intent.action}")
            
            // Verificar se é um intent do Google Pay/Wallet
            if (isGooglePayIntent(intent)) {
                Log.d(TAG, "✅ [GOOGLE] Intent do Google Pay detectada")
                
                // Extrair dados da intent
                val extraText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!extraText.isNullOrEmpty()) {
                    Log.d(TAG, "🔍 [GOOGLE] Dados EXTRA_TEXT encontrados: ${extraText.length} caracteres")
                    
                    // Armazenar dados para processamento posterior
                    pendingIntentData = extraText
                    pendingIntentAction = intent.action
                    pendingCallingPackage = activity.callingPackage
                    hasPendingIntentData = true
                    
                    Log.d(TAG, "✅ [GOOGLE] Dados armazenados para processamento - Action: ${intent.action}, CallingPackage: ${activity.callingPackage}")
                    
                    // Limpar intent para evitar reprocessamento
                    activity.intent = Intent()
                } else {
                    Log.w(TAG, "⚠️ [GOOGLE] Nenhum dado EXTRA_TEXT encontrado")
                }
            } else {
                Log.d(TAG, "🔍 [GOOGLE] Intent não relacionada ao Google Pay")
            }
        }
        
        /**
         * Verifica se uma intent é relacionada ao Google Pay/Wallet
         */
        private fun isGooglePayIntent(intent: Intent): Boolean {
            val action = intent.action

            Log.d(TAG, "🔍 [GOOGLE] Verificando intent - Action: $action")
            
            // Verificar action
            val isValidAction = action != null && (
                action.endsWith(".action.ACTIVATE_TOKEN")
            )
            
            return isValidAction
        }

        /**
         * Verifica se o chamador é válido (Google Play Services)
         */
        @JvmStatic
        fun isValidCallingPackage(activity: Activity): Boolean {
            val callingPackage = activity.callingPackage
            Log.d(TAG, "🔍 [GOOGLE] Chamador: $callingPackage")
            
            return callingPackage != null && (
                callingPackage == "com.google.android.gms" ||
                callingPackage == "com.google.android.gms_mock"
            )
        }
    }
}

