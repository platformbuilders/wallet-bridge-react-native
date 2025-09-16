package com.builders.wallet.googletapandpay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class GoogleWalletImplementation(
    private val reactContext: com.facebook.react.bridge.ReactApplicationContext
) : GoogleWalletContract {

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
    private var intentListenerActive: Boolean = false

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
                        data: android.content.Intent?
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
                                            reject("PUSH_TOKENIZE_ERROR", "Falha ao tokenizar por push - result_code:$resultCode")
                                        }
                                    }
                                }
                                CREATE_WALLET_REQUEST -> {
                                    if (resultCode == Activity.RESULT_OK) {
                                        Log.i(TAG, "Carteira criada com sucesso")
                                        resolve(true)
                                    } else {
                                        Log.w(TAG, "Falha ao criar carteira - código: $resultCode")
                                        reject("CREATE_WALLET_ERROR", "Falha ao criar carteira - result_code:$resultCode")
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
    }

    override fun checkWalletAvailability(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] checkWalletAvailability chamado")
        try {
            // Verificar se o SDK está disponível
            if (!isSDKAvailable) {
                Log.w(TAG, "❌ [GOOGLE] Google Pay SDK não está disponível")
                throw Exception("Google Pay SDK não está disponível")
            }
            
            // Verificar se é Android e acima do ICE_CREAM_SANDWICH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Log.d(TAG, "✅ [GOOGLE] Android ${Build.VERSION.SDK_INT} suportado e SDK disponível")
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

    override fun getSecureWalletInfo(promise: Promise) {
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

    override fun getTokenStatus(tokenServiceProvider: Int, tokenReferenceId: String, promise: Promise) {
        Log.i(TAG, "--")
        Log.i(TAG, "> getTokenStatus started")
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
            
            try {
                // Chamar getTokenStatus usando reflexão
                val getTokenStatusMethod = tapAndPayClient?.javaClass?.getMethod("getTokenStatus", 
                    Int::class.java, String::class.java)
                val task = getTokenStatusMethod?.invoke(tapAndPayClient, tokenServiceProvider, tokenReferenceId) as? Any
                
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
                                        val tokenStatus = getResultMethod.invoke(completedTask)
                                        
                                        if (tokenStatus != null) {
                                            Log.d(TAG, "✅ [GOOGLE] TokenStatus obtido com sucesso")
                                            
                                            // Obter tokenState usando reflexão
                                            val tokenStateMethod = tokenStatus.javaClass.getMethod("getTokenState")
                                            val tokenState = tokenStateMethod.invoke(tokenStatus) as? Int
                                            
                                            // Obter isSelected usando reflexão
                                            val isSelectedMethod = tokenStatus.javaClass.getMethod("isSelected")
                                            val isSelected = isSelectedMethod.invoke(tokenStatus) as? Boolean
                                            
                                            val result = Arguments.createMap()
                                            result.putInt("tokenState", tokenState ?: -1)
                                            result.putBoolean("isSelected", isSelected ?: false)
                                            
                                            Log.i(TAG, "- getTokenStatus = ${tokenState}")
                                            promise.resolve(result)
                                        } else {
                                            Log.w(TAG, "❌ [GOOGLE] TokenStatus é null")
                                            promise.reject("GET_TOKEN_STATUS_ERROR", "TokenStatus é null")
                                        }
                                    } else {
                                        // Tentar obter o código de erro da task
                                        var errorMessage = "Falha ao obter status do token - task não foi bem-sucedida"
                                        var statusCode = -1
                                        
                                        try {
                                            val getExceptionMethod = completedTask.javaClass.getMethod("getException")
                                            val exception = getExceptionMethod.invoke(completedTask) as? Exception
                                            if (exception != null) {
                                                errorMessage = "Falha ao obter status do token - Erro: ${exception.message}"
                                                Log.w(TAG, "❌ [GOOGLE] Exception da task: ${exception.message}")
                                                
                                                // Tentar obter statusCode da ApiException
                                                try {
                                                    val statusCodeField = exception.javaClass.getField("statusCode")
                                                    statusCode = statusCodeField.getInt(exception)
                                                } catch (e: Exception) {
                                                    Log.w(TAG, "❌ [GOOGLE] Não foi possível obter statusCode: ${e.message}")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.w(TAG, "❌ [GOOGLE] Não foi possível obter exception da task: ${e.message}")
                                        }
                                        
                                        // Verificar se é TAP_AND_PAY_TOKEN_NOT_FOUND
                                        if (statusCode == 15003) { // TapAndPayStatusCodes.TAP_AND_PAY_TOKEN_NOT_FOUND
                                            Log.w(TAG, "❌ [GOOGLE] Token não encontrado")
                                            promise.reject("GET_TOKEN_STATUS_ERROR", "Não foi possível encontrar o token")
                                        } else {
                                            Log.w(TAG, "❌ [GOOGLE] $errorMessage")
                                            promise.reject("GET_TOKEN_STATUS_ERROR", errorMessage)
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "❌ [GOOGLE] CompletedTask é null")
                                    promise.reject("GET_TOKEN_STATUS_ERROR", "CompletedTask é null")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ [GOOGLE] Erro ao processar resultado do status do token: ${e.message}", e)
                                promise.reject("GET_TOKEN_STATUS_ERROR", "Erro ao processar resultado do status do token: ${e.message}")
                            }
                        }
                        null
                    }
                    
                    val addOnCompleteListenerMethod = task.javaClass.getMethod("addOnCompleteListener", onCompleteListenerClass)
                    addOnCompleteListenerMethod.invoke(task, onCompleteListener)
                    
                    Log.d(TAG, "✅ [GOOGLE] Listener configurado com sucesso")
                } else {
                    Log.w(TAG, "❌ [GOOGLE] Não foi possível obter task do getTokenStatus")
                    promise.reject("GET_TOKEN_STATUS_ERROR", "Não foi possível obter task do getTokenStatus")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao obter status do token: ${e.message}", e)
                promise.reject("GET_TOKEN_STATUS_ERROR", "Erro ao obter status do token: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro em getTokenStatus: ${e.message}", e)
            promise.reject("GET_TOKEN_STATUS_ERROR", e.message, e)
        }
    }

    override fun getEnvironment(promise: Promise) {
        Log.i(TAG, "--")
        Log.i(TAG, "> getEnvironment started")
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
            
            try {
                // Obter environment usando reflexão - environment é um método, não um campo
                val environmentMethod = tapAndPayClient?.javaClass?.getMethod("getEnvironment")
                val environmentTask = environmentMethod?.invoke(tapAndPayClient) as? Any
                
                if (environmentTask != null) {
                    Log.d(TAG, "🔍 [GOOGLE] Environment task obtida, configurando listener...")
                    
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
                                        val environment = getResultMethod.invoke(completedTask) as? String
                                        
                                        if (environment != null) {
                                            Log.i(TAG, "- getEnvironment = $environment")
                                            promise.resolve(environment)
                                        } else {
                                            Log.w(TAG, "❌ [GOOGLE] Environment é null")
                                            promise.reject("GET_ENVIRONMENT_ERROR", "Environment é null")
                                        }
                                    } else {
                                        // Tentar obter o código de erro da task
                                        var errorMessage = "Falha ao obter environment - task não foi bem-sucedida"
                                        try {
                                            val getExceptionMethod = completedTask.javaClass.getMethod("getException")
                                            val exception = getExceptionMethod.invoke(completedTask) as? Exception
                                            if (exception != null) {
                                                errorMessage = "Falha ao obter environment - Erro: ${exception.message}"
                                                Log.w(TAG, "❌ [GOOGLE] Exception da task: ${exception.message}")
                                            }
                                        } catch (e: Exception) {
                                            Log.w(TAG, "❌ [GOOGLE] Não foi possível obter exception da task: ${e.message}")
                                        }
                                        
                                        Log.w(TAG, "❌ [GOOGLE] $errorMessage")
                                        promise.reject("GET_ENVIRONMENT_ERROR", errorMessage)
                                    }
                                } else {
                                    Log.w(TAG, "❌ [GOOGLE] CompletedTask é null")
                                    promise.reject("GET_ENVIRONMENT_ERROR", "CompletedTask é null")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ [GOOGLE] Erro ao processar resultado do environment: ${e.message}", e)
                                promise.reject("GET_ENVIRONMENT_ERROR", "Erro ao processar resultado do environment: ${e.message}")
                            }
                        }
                        null
                    }
                    
                    val addOnCompleteListenerMethod = environmentTask.javaClass.getMethod("addOnCompleteListener", onCompleteListenerClass)
                    addOnCompleteListenerMethod.invoke(environmentTask, onCompleteListener)
                    
                    Log.d(TAG, "✅ [GOOGLE] Listener configurado com sucesso")
                } else {
                    Log.w(TAG, "❌ [GOOGLE] Não foi possível obter environment task")
                    promise.reject("GET_ENVIRONMENT_ERROR", "Não foi possível obter environment task")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao obter environment: ${e.message}", e)
                promise.reject("GET_ENVIRONMENT_ERROR", "Erro ao obter environment: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro em getEnvironment: ${e.message}", e)
            promise.reject("GET_ENVIRONMENT_ERROR", e.message, e)
        }
    }

    override fun isTokenized(
        fpanLastFour: String,
        cardNetwork: Int,
        tokenServiceProvider: Int,
        promise: Promise
    ) {
        Log.i(TAG, "--")
        Log.i(TAG, "> isTokenized started")
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
            
            try {
                // Criar IsTokenizedRequest usando reflexão
                val isTokenizedRequestClass = Class.forName("com.google.android.gms.tapandpay.issuer.IsTokenizedRequest")
                val builderClass = Class.forName("com.google.android.gms.tapandpay.issuer.IsTokenizedRequest\$Builder")
                val builder = builderClass.newInstance()
                
                // Configurar parâmetros do builder
                builderClass.getMethod("setIdentifier", String::class.java)
                    .invoke(builder, fpanLastFour)
                builderClass.getMethod("setNetwork", Int::class.java)
                    .invoke(builder, cardNetwork)
                builderClass.getMethod("setTokenServiceProvider", Int::class.java)
                    .invoke(builder, tokenServiceProvider)
                
                val request = builderClass.getMethod("build").invoke(builder)
                
                // Chamar isTokenized usando reflexão
                val isTokenizedMethod = tapAndPayClient?.javaClass?.getMethod("isTokenized", isTokenizedRequestClass)
                val task = isTokenizedMethod?.invoke(tapAndPayClient, request) as? Any
                
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
                                        val isTokenized = getResultMethod.invoke(completedTask) as? Boolean
                                        
                                        if (isTokenized != null) {
                                            if (isTokenized) {
                                                Log.d(TAG, "Found a token with last four digits $fpanLastFour.")
                                            }
                                            Log.i(TAG, "- isTokenized = $isTokenized")
                                            promise.resolve(isTokenized)
                                        } else {
                                            Log.w(TAG, "❌ [GOOGLE] Resultado isTokenized é null")
                                            promise.reject("IS_TOKENIZED_ERROR", "Resultado isTokenized é null")
                                        }
                                    } else {
                                        // Tentar obter o código de erro da task
                                        var errorMessage = "Falha ao verificar se está tokenizado - task não foi bem-sucedida"
                                        try {
                                            val getExceptionMethod = completedTask.javaClass.getMethod("getException")
                                            val exception = getExceptionMethod.invoke(completedTask) as? Exception
                                            if (exception != null) {
                                                errorMessage = "Falha ao verificar se está tokenizado - Erro: ${exception.message}"
                                                Log.w(TAG, "❌ [GOOGLE] Exception da task: ${exception.message}")
                                            }
                                        } catch (e: Exception) {
                                            Log.w(TAG, "❌ [GOOGLE] Não foi possível obter exception da task: ${e.message}")
                                        }
                                        
                                        Log.w(TAG, "❌ [GOOGLE] $errorMessage")
                                        promise.reject("IS_TOKENIZED_ERROR", errorMessage)
                                    }
                                } else {
                                    Log.w(TAG, "❌ [GOOGLE] CompletedTask é null")
                                    promise.reject("IS_TOKENIZED_ERROR", "CompletedTask é null")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ [GOOGLE] Erro ao processar resultado do isTokenized: ${e.message}", e)
                                promise.reject("IS_TOKENIZED_ERROR", "Erro ao processar resultado do isTokenized: ${e.message}")
                            }
                        }
                        null
                    }
                    
                    val addOnCompleteListenerMethod = task.javaClass.getMethod("addOnCompleteListener", onCompleteListenerClass)
                    addOnCompleteListenerMethod.invoke(task, onCompleteListener)
                    
                    Log.d(TAG, "✅ [GOOGLE] Listener configurado com sucesso")
                } else {
                    Log.w(TAG, "❌ [GOOGLE] Não foi possível obter task do isTokenized")
                    promise.reject("IS_TOKENIZED_ERROR", "Não foi possível obter task do isTokenized")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao verificar se está tokenizado: ${e.message}", e)
                promise.reject("IS_TOKENIZED_ERROR", "Erro ao verificar se está tokenizado: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro em isTokenized: ${e.message}", e)
            promise.reject("IS_TOKENIZED_ERROR", e.message, e)
        }
    }

    override fun viewToken(
        tokenServiceProvider: Int,
        issuerTokenId: String,
        promise: Promise
    ) {
        Log.i(TAG, "--")
        Log.i(TAG, "> viewToken started")
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
            
            // Obter atividade atual
            activity = reactContext.currentActivity
            if (activity == null) {
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }
            
            try {
                // Criar ViewTokenRequest usando reflexão
                val viewTokenRequestClass = Class.forName("com.google.android.gms.tapandpay.issuer.ViewTokenRequest")
                val builderClass = Class.forName("com.google.android.gms.tapandpay.issuer.ViewTokenRequest\$Builder")
                val builder = builderClass.newInstance()
                
                // Configurar parâmetros do builder
                builderClass.getMethod("setTokenServiceProvider", Int::class.java)
                    .invoke(builder, tokenServiceProvider)
                builderClass.getMethod("setIssuerTokenId", String::class.java)
                    .invoke(builder, issuerTokenId)
                
                val request = builderClass.getMethod("build").invoke(builder)
                
                // Chamar viewToken usando reflexão
                val viewTokenMethod = tapAndPayClient?.javaClass?.getMethod("viewToken", viewTokenRequestClass)
                val task = viewTokenMethod?.invoke(tapAndPayClient, request) as? Any
                
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
                                        val pendingIntent = getResultMethod.invoke(completedTask)
                                        
                                        if (pendingIntent != null) {
                                            Log.i(TAG, "- viewToken will send intent")
                                            
                                            try {
                                                // Enviar o PendingIntent
                                                val sendMethod = pendingIntent.javaClass.getMethod("send")
                                                sendMethod.invoke(pendingIntent)
                                                
                                                Log.d(TAG, "✅ [GOOGLE] PendingIntent enviado com sucesso")
                                                promise.resolve(true)
                                            } catch (e: Exception) {
                                                Log.w(TAG, "❌ [GOOGLE] Erro ao enviar PendingIntent: ${e.message}")
                                                promise.reject("VIEW_TOKEN_ERROR", "Erro ao enviar PendingIntent: ${e.message}")
                                            }
                                        } else {
                                            Log.w(TAG, "❌ [GOOGLE] PendingIntent é null")
                                            promise.reject("VIEW_TOKEN_ERROR", "PendingIntent é null")
                                        }
                                    } else {
                                        // Tentar obter o código de erro da task
                                        var errorMessage = "Falha ao visualizar token - task não foi bem-sucedida"
                                        try {
                                            val getExceptionMethod = completedTask.javaClass.getMethod("getException")
                                            val exception = getExceptionMethod.invoke(completedTask) as? Exception
                                            if (exception != null) {
                                                errorMessage = "Falha ao visualizar token - Erro: ${exception.message}"
                                                Log.w(TAG, "❌ [GOOGLE] Exception da task: ${exception.message}")
                                            }
                                        } catch (e: Exception) {
                                            Log.w(TAG, "❌ [GOOGLE] Não foi possível obter exception da task: ${e.message}")
                                        }
                                        
                                        Log.w(TAG, "❌ [GOOGLE] $errorMessage")
                                        promise.reject("VIEW_TOKEN_ERROR", errorMessage)
                                    }
                                } else {
                                    Log.w(TAG, "❌ [GOOGLE] CompletedTask é null")
                                    promise.reject("VIEW_TOKEN_ERROR", "CompletedTask é null")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ [GOOGLE] Erro ao processar resultado do viewToken: ${e.message}", e)
                                promise.reject("VIEW_TOKEN_ERROR", "Erro ao processar resultado do viewToken: ${e.message}")
                            }
                        }
                        null
                    }
                    
                    val addOnCompleteListenerMethod = task.javaClass.getMethod("addOnCompleteListener", onCompleteListenerClass)
                    addOnCompleteListenerMethod.invoke(task, onCompleteListener)
                    
                    Log.d(TAG, "✅ [GOOGLE] Listener configurado com sucesso")
                } else {
                    Log.w(TAG, "❌ [GOOGLE] Não foi possível obter task do viewToken")
                    promise.reject("VIEW_TOKEN_ERROR", "Não foi possível obter task do viewToken")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao visualizar token: ${e.message}", e)
                promise.reject("VIEW_TOKEN_ERROR", "Erro ao visualizar token: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro em viewToken: ${e.message}", e)
            promise.reject("VIEW_TOKEN_ERROR", e.message, e)
        }
    }

    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] addCardToWallet chamado")
        try {
            if (!isSDKAvailable) {
                Log.w(TAG, "Google Pay SDK não está disponível")
                promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
                return
            }
            
            Log.d(TAG, "🔍 [GOOGLE] Dados do cartão recebidos: $cardData")
            
            // Obter atividade atual
            activity = reactContext.currentActivity
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

    override fun createWalletIfNeeded(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] createWalletIfNeeded chamado")
        try {
            if (!isSDKAvailable) {
                Log.w(TAG, "Google Pay SDK não está disponível")
                promise.reject("SDK_NOT_AVAILABLE", "Google Pay SDK não está disponível")
                return
            }
            
            // Obter atividade atual
            activity = reactContext.currentActivity
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

    override fun listTokens(promise: Promise) {
        Log.i(TAG, "--")
        Log.i(TAG, "> listTokens started")
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
            
            try {
                // Chamar listTokens usando reflexão
                val listTokensMethod = tapAndPayClient?.javaClass?.getMethod("listTokens")
                val task = listTokensMethod?.invoke(tapAndPayClient) as? Any
                
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
                                        val tokenList = getResultMethod.invoke(completedTask) as? List<*>
                                        
                                        if (tokenList != null) {
                                            Log.d(TAG, "✅ [GOOGLE] Lista de tokens obtida: ${tokenList.size} tokens")
                                            
                                            val result = tokenList.map { tokenInfo ->
                                                try {
                                                    // Converter TokenInfo para mapa serializável usando reflexão
                                                    val tokenMap = Arguments.createMap()
                                                    
                                                    // Obter issuerTokenId
                                                    val issuerTokenIdMethod = tokenInfo?.javaClass?.getMethod("getIssuerTokenId")
                                                    val issuerTokenId = issuerTokenIdMethod?.invoke(tokenInfo) as? String
                                                    tokenMap.putString("issuerTokenId", issuerTokenId)
                                                    
                                                    // Obter lastDigits
                                                    val lastDigitsMethod = tokenInfo?.javaClass?.getMethod("getLastDigits")
                                                    val lastDigits = lastDigitsMethod?.invoke(tokenInfo) as? String
                                                    tokenMap.putString("lastDigits", lastDigits)
                                                    
                                                    // Obter displayName
                                                    val displayNameMethod = tokenInfo?.javaClass?.getMethod("getDisplayName")
                                                    val displayName = displayNameMethod?.invoke(tokenInfo) as? String
                                                    tokenMap.putString("displayName", displayName)
                                                    
                                                    // Obter tokenState
                                                    val tokenStateMethod = tokenInfo?.javaClass?.getMethod("getTokenState")
                                                    val tokenState = tokenStateMethod?.invoke(tokenInfo) as? Int
                                                    tokenMap.putInt("tokenState", tokenState ?: -1)
                                                    
                                                    // Obter network
                                                    val networkMethod = tokenInfo?.javaClass?.getMethod("getNetwork")
                                                    val network = networkMethod?.invoke(tokenInfo) as? Int
                                                    tokenMap.putInt("network", network ?: -1)
                                                    
                                                    Log.d(TAG, "🔍 [GOOGLE] Token processado - ID: $issuerTokenId, LastDigits: $lastDigits")
                                                    tokenMap
                                                } catch (e: Exception) {
                                                    Log.w(TAG, "❌ [GOOGLE] Erro ao processar token: ${e.message}")
                                                    val errorMap = Arguments.createMap()
                                                    errorMap.putString("error", "Erro ao processar token: ${e.message}")
                                                    errorMap
                                                }
                                            }
                                            
                                            // Converter List<WritableMap> para WritableArray
                                            val writableArray = Arguments.createArray()
                                            for (tokenMap in result) {
                                                writableArray.pushMap(tokenMap)
                                            }
                                            
                                            Log.i(TAG, "- listTokens = ${result.size}")
                                            promise.resolve(writableArray)
                                        } else {
                                            Log.w(TAG, "❌ [GOOGLE] Lista de tokens é null")
                                            promise.reject("LIST_TOKENS_ERROR", "Lista de tokens é null")
                                        }
                                    } else {
                                        // Tentar obter o código de erro da task
                                        var errorMessage = "Falha ao listar tokens - task não foi bem-sucedida"
                                        try {
                                            val getExceptionMethod = completedTask.javaClass.getMethod("getException")
                                            val exception = getExceptionMethod.invoke(completedTask) as? Exception
                                            if (exception != null) {
                                                errorMessage = "Falha ao listar tokens - Erro: ${exception.message}"
                                                Log.w(TAG, "❌ [GOOGLE] Exception da task: ${exception.message}")
                                            }
                                        } catch (e: Exception) {
                                            Log.w(TAG, "❌ [GOOGLE] Não foi possível obter exception da task: ${e.message}")
                                        }
                                        
                                        Log.w(TAG, "❌ [GOOGLE] $errorMessage")
                                        promise.reject("LIST_TOKENS_ERROR", errorMessage)
                                    }
                                } else {
                                    Log.w(TAG, "❌ [GOOGLE] CompletedTask é null")
                                    promise.reject("LIST_TOKENS_ERROR", "CompletedTask é null")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ [GOOGLE] Erro ao processar resultado da lista de tokens: ${e.message}", e)
                                promise.reject("LIST_TOKENS_ERROR", "Erro ao processar resultado da lista de tokens: ${e.message}")
                            }
                        }
                        null
                    }
                    
                    val addOnCompleteListenerMethod = task.javaClass.getMethod("addOnCompleteListener", onCompleteListenerClass)
                    addOnCompleteListenerMethod.invoke(task, onCompleteListener)
                    
                    Log.d(TAG, "✅ [GOOGLE] Listener configurado com sucesso")
                } else {
                    Log.w(TAG, "❌ [GOOGLE] Não foi possível obter task do listTokens")
                    promise.reject("LIST_TOKENS_ERROR", "Não foi possível obter task do listTokens")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao listar tokens: ${e.message}", e)
                promise.reject("LIST_TOKENS_ERROR", "Erro ao listar tokens: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro em listTokens: ${e.message}", e)
            promise.reject("LIST_TOKENS_ERROR", e.message, e)
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

    override fun setIntentListener(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] setIntentListener chamado")
        try {
            intentListenerActive = true
            Log.d(TAG, "✅ [GOOGLE] Listener de intent ativado")
            
            // Verificar dados pendentes da MainActivity automaticamente
            checkPendingDataFromMainActivity()
            
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao ativar listener de intent: ${e.message}", e)
            promise.reject("SET_INTENT_LISTENER_ERROR", e.message, e)
        }
    }

    override fun removeIntentListener(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] removeIntentListener chamado")
        try {
            intentListenerActive = false
            Log.d(TAG, "✅ [GOOGLE] Listener de intent desativado")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao desativar listener de intent: ${e.message}", e)
            promise.reject("REMOVE_INTENT_LISTENER_ERROR", e.message, e)
        }
    }

    override fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] setActivationResult chamado - Status: $status, ActivationCode: $activationCode")
        try {
            // Obter atividade atual
            activity = reactContext.currentActivity
            if (activity == null) {
                Log.w(TAG, "❌ [GOOGLE] Nenhuma atividade disponível para definir resultado")
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }

            // Validar status
            val validStatuses = listOf("approved", "declined", "failure")
            if (!validStatuses.contains(status)) {
                Log.w(TAG, "❌ [GOOGLE] Status inválido: $status. Deve ser: approved, declined ou failure")
                promise.reject("INVALID_STATUS", "Status deve ser: approved, declined ou failure")
                return
            }

            // Criar Intent de resultado
            val resultIntent = Intent()
            resultIntent.putExtra("BANKING_APP_ACTIVATION_RESPONSE", status)
            
            // Adicionar activationCode se fornecido e status for approved
            if (activationCode != null && !activationCode.isEmpty() && status == "approved") {
                Log.d(TAG, "🔍 [GOOGLE] Adicionando activationCode: $activationCode")
                resultIntent.putExtra("BANKING_APP_ACTIVATION_CODE", activationCode)
            }

            // Definir resultado da atividade
            activity?.setResult(Activity.RESULT_OK, resultIntent)
            
            Log.d(TAG, "✅ [GOOGLE] Resultado de ativação definido - Status: $status")
            if (activationCode != null && !activationCode.isEmpty() && status == "approved") {
                Log.d(TAG, "✅ [GOOGLE] ActivationCode incluído: $activationCode")
            }
            
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao definir resultado de ativação: ${e.message}", e)
            promise.reject("SET_ACTIVATION_RESULT_ERROR", e.message, e)
        }
    }

    override fun finishActivity(promise: Promise) {
        Log.d(TAG, "🔍 [GOOGLE] finishActivity chamado")
        try {
            // Obter atividade atual
            activity = reactContext.currentActivity
            if (activity == null) {
                Log.w(TAG, "❌ [GOOGLE] Nenhuma atividade disponível para finalizar")
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }

            // Finalizar a atividade
            activity?.finish()
            
            Log.d(TAG, "✅ [GOOGLE] Atividade finalizada com sucesso")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao finalizar atividade: ${e.message}", e)
            promise.reject("FINISH_ACTIVITY_ERROR", e.message, e)
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
            
            val eventData = Arguments.createMap()
            eventData.putString("action", action)
            eventData.putString("type", intentType)
            eventData.putString("data", data)
            eventData.putString("dataFormat", "base64")
            eventData.putString("dataNote", "Dados obtidos automaticamente no onCreate")
            eventData.putString("callingPackage", callingPackage)
            
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
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, eventData)
            Log.d(TAG, "✅ [GOOGLE] Evento enviado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao enviar evento para React Native: ${e.message}", e)
        }
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
        fun processIntent(activity: android.app.Activity, intent: android.content.Intent) {
            Log.d(TAG, "🔍 [IMPLEMENTATION] processIntent chamado")
            
            if (intent != null) {
                Log.d(TAG, "🔍 [IMPLEMENTATION] Intent encontrada: ${intent.action}")
                
                // Verificar se é um intent do Google Pay/Wallet
                if (isGooglePayIntent(intent)) {
                    Log.d(TAG, "✅ [IMPLEMENTATION] Intent do Google Pay detectada")
                    
                    // Validar chamador
                    if (isValidCallingPackage(activity)) {
                        Log.d(TAG, "✅ [IMPLEMENTATION] Chamador validado: Google Play Services")
                        
                        // Extrair dados da intent
                        val extraText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
                        if (!extraText.isNullOrEmpty()) {
                            Log.d(TAG, "🔍 [IMPLEMENTATION] Dados EXTRA_TEXT encontrados: ${extraText.length} caracteres")
                            
                            // Armazenar dados para processamento posterior
                            pendingIntentData = extraText
                            pendingIntentAction = intent.action
                            pendingCallingPackage = activity.callingPackage
                            hasPendingIntentData = true
                            
                            Log.d(TAG, "✅ [IMPLEMENTATION] Dados armazenados para processamento - Action: ${intent.action}, CallingPackage: ${activity.callingPackage}")
                        } else {
                            Log.w(TAG, "⚠️ [IMPLEMENTATION] Nenhum dado EXTRA_TEXT encontrado")
                        }
                        
                        // Limpar intent para evitar reprocessamento
                        activity.intent = android.content.Intent()
                        
                    } else {
                        Log.w(TAG, "❌ [IMPLEMENTATION] Chamador inválido: ${activity.callingPackage}")
                        
                        // Abortar ativação do token
                        activity.setResult(android.app.Activity.RESULT_CANCELED)
                        activity.finish()
                    }
                } else {
                    Log.d(TAG, "🔍 [IMPLEMENTATION] Intent não relacionada ao Google Pay")
                }
            } else {
                Log.d(TAG, "🔍 [IMPLEMENTATION] Nenhuma intent encontrada")
            }
        }
        
        /**
         * Verifica se uma intent é relacionada ao Google Pay/Wallet
         */
        private fun isGooglePayIntent(intent: android.content.Intent): Boolean {
            val action = intent.action
            val packageName = intent.`package`
            
            Log.d(TAG, "🔍 [IMPLEMENTATION] Verificando intent - Action: $action, Package: $packageName")
            
            // Verificar action
            val isValidAction = action != null && (
                action.endsWith(".action.ACTIVATE_TOKEN") ||
                action.contains("google", ignoreCase = true) ||
                action.contains("wallet", ignoreCase = true)
            )
            
            // Verificar package
            val isValidPackage = packageName != null && (
                packageName == "com.google.android.gms" ||
                packageName == "com.google.android.gms_mock"
            )
            
            return isValidAction || isValidPackage
        }

        /**
         * Verifica se o chamador é válido (Google Play Services)
         */
        private fun isValidCallingPackage(activity: android.app.Activity): Boolean {
            val callingPackage = activity.callingPackage
            Log.d(TAG, "🔍 [IMPLEMENTATION] Chamador: $callingPackage")
            
            return callingPackage != null && (
                callingPackage == "com.google.android.gms" ||
                callingPackage == "com.google.android.gms_mock"
            )
        }
    }
}
