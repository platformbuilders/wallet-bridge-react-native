package com.builders.wallet.googletapandpay

import android.app.Activity
import android.content.Intent
import com.builders.wallet.WalletLogger
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*
import org.json.JSONObject
import com.builders.wallet.BuildConfig
import com.builders.wallet.WalletOpener

class GoogleWalletMock(private val reactContext: ReactApplicationContext) : GoogleWalletContract {

    private var activity: Activity? = null
    private var intentListenerActive: Boolean = false
    private var walletOpener: WalletOpener? = null

    init {
        // Inicializar WalletOpener
        walletOpener = WalletOpener(reactContext)
    }

    companion object {
        private const val TAG = "GoogleWalletMock"
        private const val DEFAULT_API_BASE_URL = "http://localhost:3000"
        private const val REQUEST_TIMEOUT = 5000 // 5 segundos
        private const val GOOGLE_WALLET_PACKAGE = "com.google.android.gms"
        private const val GOOGLE_WALLET_APP_PACKAGE = "com.google.android.apps.walletnfcrel"
        private val GOOGLE_WALLET_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=$GOOGLE_WALLET_APP_PACKAGE&hl=pt_BR"

        // Versão mínima do Android suportada pelo Google Wallet: Android 9.0 (Pie) - API level 28
        private const val MIN_ANDROID_VERSION = android.os.Build.VERSION_CODES.P

        // Obter URL da API do BuildConfig
        private val API_BASE_URL: String by lazy {
            try {
                val buildConfigUrl = BuildConfig.GOOGLE_WALLET_MOCK_API_URL
                if (buildConfigUrl.isNotEmpty()) {
                    WalletLogger.d(TAG, "🌐 [MOCK] Usando API URL do BuildConfig: $buildConfigUrl")
                    return@lazy buildConfigUrl
                }

                // Se não configurado, usar DEFAULT_API_BASE_URL
                WalletLogger.d(TAG, "🌐 [MOCK] API URL não configurada, usando DEFAULT: $DEFAULT_API_BASE_URL")
                DEFAULT_API_BASE_URL
            } catch (e: Exception) {
                WalletLogger.w(TAG, "⚠️ [MOCK] Erro ao obter URL da API: ${e.message}")
                DEFAULT_API_BASE_URL
            }
        }

        // Enum de códigos de erro para push tokenize
        enum class PushTokenizeErrorCode(val code: String, val description: String) {
            CANCELLED("0", "Push tokenize cancelado pelo usuário"),
            NO_ACTIVE_WALLET("15002", "Nenhuma carteira ativa encontrada"),
            TOKEN_NOT_FOUND("15003", "Token não encontrado"),
            INVALID_TOKEN_STATE("15004", "Estado do token inválido"),
            ATTESTATION_ERROR("15005", "Erro de atestação"),
            UNAVAILABLE("15009", "Serviço indisponível")
        }

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
            WalletLogger.d(TAG, "🔍 [GOOGLE MOCK] processIntent chamado")

            WalletLogger.d(TAG, "🔍 [GOOGLE MOCK] Intent encontrada: ${intent.action}")

            // Verificar se é um intent do Google Pay/Wallet
            if (isGooglePayIntent(intent)) {
                WalletLogger.d(TAG, "✅ [GOOGLE MOCK] Intent do Google Pay detectada")

                // Extrair dados da intent
                val extraText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
                if (!extraText.isNullOrEmpty()) {
                    WalletLogger.d(TAG, "🔍 [GOOGLE MOCK] Dados EXTRA_TEXT encontrados: ${extraText.length} caracteres")

                    // Armazenar dados para processamento posterior
                    pendingIntentData = extraText
                    pendingIntentAction = intent.action
                    pendingCallingPackage = activity.callingPackage
                    hasPendingIntentData = true

                    WalletLogger.d(TAG, "✅ [GOOGLE MOCK] Dados armazenados para processamento - Action: ${intent.action}, CallingPackage: ${activity.callingPackage}")

                    // Limpar intent para evitar reprocessamento
                    activity.intent = android.content.Intent()
                } else {
                    WalletLogger.w(TAG, "⚠️ [GOOGLE MOCK] Nenhum dado EXTRA_TEXT encontrado")
                }
            } else {
                WalletLogger.d(TAG, "🔍 [GOOGLE MOCK] Intent não relacionada ao Google Pay")
            }
        }

        /**
         * Verifica se uma intent é relacionada ao Google Pay/Wallet
         */
        private fun isGooglePayIntent(intent: Intent): Boolean {
            val action = intent.action

            WalletLogger.d(TAG, "🔍 [GOOGLE] Verificando intent - Action: $action")

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
        fun isValidCallingPackage(activity: android.app.Activity): Boolean {
            val callingPackage = activity.callingPackage
            WalletLogger.d(TAG, "🔍 [GOOGLE MOCK] Chamador: $callingPackage")

            return callingPackage != null && (
                callingPackage == GOOGLE_WALLET_PACKAGE ||
                callingPackage == GOOGLE_WALLET_APP_PACKAGE ||
                callingPackage == "com.google.android.gms_mock"
            )
        }
    }

    /**
     * Processa dados de intent e envia evento para React Native
     */
    private fun processWalletIntentData(data: String, action: String, callingPackage: String) {
        WalletLogger.d(TAG, "🔍 [GOOGLE] processWalletIntentData chamado")
        try {
            WalletLogger.d(TAG, "✅ [GOOGLE] Intent processado: $action")

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
                WalletLogger.d(TAG, "🔍 [GOOGLE] Dados decodificados com sucesso: ${decodedData.length} caracteres")
            } catch (e: Exception) {
                // Se falhar ao decodificar, usar dados originais
                WalletLogger.w(TAG, "⚠️ [GOOGLE] Não foi possível decodificar como base64, usando dados originais: ${e.message}")
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

            WalletLogger.d(TAG, "🔍 [GOOGLE] Evento preparado - Action: $action, Type: $intentType, Format: $dataFormat")

            // Enviar evento para React Native
            sendEventToReactNative("GoogleWalletIntentReceived", eventData)

        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [GOOGLE] Erro ao processar dados da intent: ${e.message}", e)
        }
    }

    private fun sendEventToReactNative(eventName: String, eventData: WritableMap?) {
        try {
            WalletLogger.d(TAG, "🔍 [GOOGLE] Enviando evento para React Native: $eventName")
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, eventData)
            WalletLogger.d(TAG, "✅ [GOOGLE] Evento enviado com sucesso")
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [GOOGLE] Erro ao enviar evento para React Native: ${e.message}", e)
        }
    }

    /**
     * Função para buscar dados de uma API local com fallback para valores padrão
     * @param endpoint Endpoint da API (ex: "/wallet/availability")
     * @param defaultResponse Resposta padrão caso a API falhe
     * @param onSuccess Callback chamado em caso de sucesso
     * @param onError Callback chamado em caso de erro
     */
    private fun fetchFromLocalAPI(
        endpoint: String,
        defaultResponse: () -> Any,
        onSuccess: (JSONObject) -> Unit,
        onError: (Exception) -> Unit,
        method: String = "GET",
        body: String? = null
    ) {
        // API_BASE_URL sempre definido (usa DEFAULT quando não configurado)
        val apiUrl = API_BASE_URL

        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                val urlString = "$apiUrl$endpoint"
                WalletLogger.d(TAG, "🌐 [API][REQUEST] ➜ ${'$'}method $urlString")
                WalletLogger.d(TAG, "🌐 [API][REQUEST] Headers: Content-Type=application/json, Accept=application/json")
                WalletLogger.d(TAG, "🌐 [API][REQUEST] Timeouts: connect=${REQUEST_TIMEOUT}ms, read=${REQUEST_TIMEOUT}ms")
                if (body != null) {
                    val bodyPreview = if (body.length > 512) body.substring(0, 512) + "…" else body
                    WalletLogger.d(TAG, "🌐 [API][REQUEST] bodyPreview=${'$'}bodyPreview")
                }

                val startAtMs = System.currentTimeMillis()
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = method
                    connectTimeout = REQUEST_TIMEOUT
                    readTimeout = REQUEST_TIMEOUT
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    if (body != null) doOutput = true
                }

                if (body != null) {
                    val writer = OutputStreamWriter(connection.outputStream)
                    writer.write(body)
                    writer.flush()
                    writer.close()
                }

                val responseCode = connection.responseCode
                val tookMs = System.currentTimeMillis() - startAtMs
                WalletLogger.d(TAG, "🌐 [API][RESPONSE] ⇦ code=$responseCode (${tookMs}ms)")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    inputStream.close()

                    val raw = response.toString()
                    val preview = if (raw.length > 512) raw.substring(0, 512) + "…" else raw
                    WalletLogger.d(TAG, "🌐 [API][RESPONSE] bodyPreview=${'$'}preview")
                    val jsonResponse = JSONObject(raw)
                    WalletLogger.d(TAG, "✅ [API] Dados obtidos com sucesso da API local (len=${raw.length})")

                    withContext(Dispatchers.Main) {
                        onSuccess(jsonResponse)
                    }
                } else {
                    // Tentar ler corpo de erro, se houver
                    val errorBody = try {
                        val es = connection.errorStream
                        if (es != null) {
                            val er = BufferedReader(InputStreamReader(es))
                            val sb = StringBuilder()
                            var lineErr: String?
                            while (er.readLine().also { lineErr = it } != null) {
                                sb.append(lineErr)
                            }
                            er.close()
                            es.close()
                            val rawErr = sb.toString()
                            if (rawErr.isNotEmpty()) rawErr else null
                        } else null
                    } catch (_: Exception) { null }
                    if (errorBody != null) {
                        val previewErr = if (errorBody.length > 512) errorBody.substring(0, 512) + "…" else errorBody
                        WalletLogger.w(TAG, "⚠️ [API][RESPONSE] code=$responseCode errorBodyPreview=${'$'}previewErr")
                    } else {
                        WalletLogger.w(TAG, "⚠️ [API][RESPONSE] code=$responseCode (sem corpo de erro)")
                    }
                    throw Exception("API retornou código de erro: $responseCode")
                }

            } catch (e: Exception) {
                WalletLogger.w(TAG, "❌ [API] Erro ao buscar dados da API local: ${e::class.java.simpleName}: ${e.message}")
                WalletLogger.d(TAG, "🔄 [API] Usando valor padrão como fallback")

                withContext(Dispatchers.Main) {
                    onError(e)
                }
            } finally {
                WalletLogger.d(TAG, "🌐 [API] Encerrando conexão com servidor mock")
                connection?.disconnect()
            }
        }
    }

    /**
     * Função auxiliar para buscar dados da API com Promise
     * @param endpoint Endpoint da API
     * @param defaultResponse Função que retorna o valor padrão
     * @param promise Promise do React Native
     */
    private fun fetchFromAPIWithPromise(
        endpoint: String,
        defaultResponse: () -> Any,
        promise: Promise
    ) {
        fetchFromLocalAPI(
            endpoint = endpoint,
            defaultResponse = defaultResponse,
            onSuccess = { jsonResponse ->
                try {
                    // Converter JSONObject para ReadableMap se necessário
                    val result = Arguments.createMap()
                    val keys = jsonResponse.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = jsonResponse.get(key)
                        when (value) {
                            is String -> result.putString(key, value)
                            is Int -> result.putInt(key, value)
                            is Boolean -> result.putBoolean(key, value)
                            is Double -> result.putDouble(key, value)
                            else -> result.putString(key, value.toString())
                        }
                    }
                    promise.resolve(result)
                } catch (e: Exception) {
                    WalletLogger.e(TAG, "❌ [API] Erro ao processar resposta da API: ${e.message}")
                    promise.resolve(defaultResponse())
                }
            },
            onError = { _ ->
                promise.resolve(defaultResponse())
            }
        )
    }

    private fun readableArrayToJson(array: ReadableArray): org.json.JSONArray {
        val jsonArray = org.json.JSONArray()
        for (i in 0 until array.size()) {
            when (array.getType(i)) {
                ReadableType.Null -> jsonArray.put(org.json.JSONObject.NULL)
                ReadableType.Boolean -> jsonArray.put(array.getBoolean(i))
                ReadableType.Number -> jsonArray.put(array.getDouble(i))
                ReadableType.String -> jsonArray.put(array.getString(i))
                ReadableType.Map -> jsonArray.put(readableMapToJson(array.getMap(i)!!))
                ReadableType.Array -> jsonArray.put(readableArrayToJson(array.getArray(i)!!))
            }
        }
        return jsonArray
    }

    private fun readableMapToJson(map: ReadableMap): JSONObject {
        val json = JSONObject()
        val iterator = map.keySetIterator()
        while (iterator.hasNextKey()) {
            val key = iterator.nextKey()
            when (map.getType(key)) {
                ReadableType.Null -> json.put(key, JSONObject.NULL)
                ReadableType.Boolean -> json.put(key, map.getBoolean(key))
                ReadableType.Number -> json.put(key, map.getDouble(key))
                ReadableType.String -> json.put(key, map.getString(key))
                ReadableType.Map -> json.put(key, readableMapToJson(map.getMap(key)!!))
                ReadableType.Array -> json.put(key, readableArrayToJson(map.getArray(key)!!))
            }
        }
        return json
    }

    /**
     * Verifica internamente se a wallet está disponível
     * @return true se disponível, false caso contrário
     */
    private fun checkWalletAvailabilityInternal(): Boolean {
        // Verificar versão mínima do Android (Android 9.0 - API level 28)
        if (android.os.Build.VERSION.SDK_INT < MIN_ANDROID_VERSION) {
            WalletLogger.w(TAG, "❌ [MOCK] Android ${android.os.Build.VERSION.SDK_INT} não suportado. Versão mínima requerida: Android 9.0 (API ${MIN_ANDROID_VERSION})")
            return false
        }

        WalletLogger.d(TAG, "✅ [MOCK] Android ${android.os.Build.VERSION.SDK_INT} suportado")
        return true
    }

    /**
     * Verifica se a wallet está disponível e rejeita a promise se não estiver
     * @param promise Promise a ser rejeitada se a wallet não estiver disponível
     * @param methodName Nome do método que está sendo chamado (para mensagem de erro)
     * @return true se disponível, false caso contrário (e promise já foi rejeitada)
     */
    private fun ensureWalletAvailable(promise: Promise, methodName: String): Boolean {
        if (!checkWalletAvailabilityInternal()) {
            val errorMessage = "Google Wallet não está disponível. Verifique a versão do Android (mínimo: Android 9.0)."
            WalletLogger.w(TAG, "❌ [MOCK] $methodName falhou: $errorMessage")
            promise.reject("WALLET_NOT_AVAILABLE", errorMessage)
            return false
        }
        return true
    }

    override fun checkWalletAvailability(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] checkWalletAvailability chamado")

        // Verificar versão mínima do Android (Android 9.0 - API level 28)
        if (android.os.Build.VERSION.SDK_INT < MIN_ANDROID_VERSION) {
            WalletLogger.w(TAG, "❌ [MOCK] Android ${android.os.Build.VERSION.SDK_INT} não suportado. Versão mínima requerida: Android 9.0 (API ${MIN_ANDROID_VERSION})")
            promise.resolve(false)
            return
        }

        fetchFromLocalAPI(
            endpoint = "/wallet/availability",
            defaultResponse = { true },
            onSuccess = { json ->
                try {
                    promise.resolve(json.optBoolean("available", true))
                } catch (e: Exception) {
                    WalletLogger.w(TAG, "⚠️ [MOCK] Erro ao processar disponibilidade, usando fallback: ${e.message}")
                    promise.resolve(true)
                }
            },
            onError = {
                // Em caso de erro de rede, assume-se que está disponível (comportamento de fallback)
                promise.resolve(true)
            }
        )
    }

    override fun getSecureWalletInfo(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] getSecureWalletInfo chamado")

        if (!ensureWalletAvailable(promise, "getSecureWalletInfo")) {
            return
        }

        // Tentar buscar da API local primeiro, com fallback para valor padrão
        fetchFromAPIWithPromise(
            endpoint = "/wallet/info",
            defaultResponse = {
                val result = Arguments.createMap()
                result.putString("deviceID", "mock_device_12345")
                result.putString("walletAccountID", "mock_wallet_67890")
                WalletLogger.d(TAG, "✅ [MOCK] Informações da carteira obtidas (valor padrão)")
                result
            },
            promise = promise
        )
    }

    override fun getTokenStatus(tokenServiceProvider: Int, tokenReferenceId: String, promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] getTokenStatus chamado - Provider: $tokenServiceProvider, RefId: $tokenReferenceId")

        if (!ensureWalletAvailable(promise, "getTokenStatus")) {
            return
        }

        // Simular diferentes cenários baseados no tokenReferenceId
        val endpoint = "/wallet/token/status?provider=$tokenServiceProvider&refId=$tokenReferenceId"
        fetchFromLocalAPI(
            endpoint = endpoint,
            defaultResponse = {
                // Simular diferentes cenários baseados no tokenReferenceId
                simulateTokenStatusResponse(tokenReferenceId, tokenServiceProvider)
            },
            onSuccess = { json ->
                try {
                    // Verificar se há erro na resposta
                    if (json.has("error")) {
                        val errorCode = json.getString("errorCode") ?: "TOKEN_STATUS_ERROR"
                        val errorMessage = json.getString("error") ?: "Erro ao obter status do token"
                        WalletLogger.w(TAG, "❌ [MOCK] Erro da API: $errorMessage")
                        promise.reject(errorCode, errorMessage)
                    } else {
                        // Resposta de sucesso
                        val result = Arguments.createMap()
                        result.putInt("tokenState", json.optInt("tokenState", 5))
                        result.putBoolean("isSelected", json.optBoolean("isSelected", true))
                        WalletLogger.d(TAG, "✅ [MOCK] Status do token obtido da API")
                        promise.resolve(result)
                    }
                } catch (e: Exception) {
                    WalletLogger.w(TAG, "❌ [MOCK] Erro ao processar resposta da API: ${e.message}")
                    // Fallback para simulação local
                    val result = simulateTokenStatusResponse(tokenReferenceId, tokenServiceProvider)
                    if (result is Exception) {
                        promise.reject("TOKEN_STATUS_ERROR", result.message, result)
                    } else {
                        promise.resolve(result)
                    }
                }
            },
            onError = { error ->
                // Simular diferentes tipos de erro baseados no erro da API
                simulateTokenStatusError(error.toString(), tokenReferenceId, tokenServiceProvider, promise)
            }
        )
    }

    /**
     * Simula resposta do getTokenStatus baseada no tokenReferenceId
     */
    private fun simulateTokenStatusResponse(tokenReferenceId: String, tokenServiceProvider: Int): Any {
        return when {
            // Simular token não encontrado
            tokenReferenceId.contains("not_found") || tokenReferenceId.contains("404") -> {
                WalletLogger.w(TAG, "❌ [MOCK] Simulando token não encontrado: $tokenReferenceId")
                Exception("Token não encontrado na carteira ativa")
            }
            // Simular calling package não verificado
            tokenReferenceId.contains("unverified") || tokenReferenceId.contains("15009") -> {
                WalletLogger.w(TAG, "❌ [MOCK] Simulando calling package não verificado: $tokenReferenceId")
                Exception("15009: Calling package not verified")
            }
            // Simular token suspenso
            tokenReferenceId.contains("suspended") || tokenReferenceId.contains("suspended") -> {
                WalletLogger.d(TAG, "✅ [MOCK] Simulando token suspenso: $tokenReferenceId")
                val result = Arguments.createMap()
                result.putInt("tokenState", 4) // TOKEN_STATE_SUSPENDED
                result.putBoolean("isSelected", false)
                result
            }
            // Simular token pendente
            tokenReferenceId.contains("pending") || tokenReferenceId.contains("pending") -> {
                WalletLogger.d(TAG, "✅ [MOCK] Simulando token pendente: $tokenReferenceId")
                val result = Arguments.createMap()
                result.putInt("tokenState", 2) // TOKEN_STATE_PENDING
                result.putBoolean("isSelected", false)
                result
            }
            // Simular token ativo (padrão)
            else -> {
                WalletLogger.d(TAG, "✅ [MOCK] Simulando token ativo: $tokenReferenceId")
                val result = Arguments.createMap()
                result.putInt("tokenState", 5) // TOKEN_STATE_ACTIVE
                result.putBoolean("isSelected", true)
                result
            }
        }
    }

    /**
     * Simula erro do getTokenStatus baseado no erro da API
     */
    private fun simulateTokenStatusError(error: String, tokenReferenceId: String, tokenServiceProvider: Int, promise: Promise) {
        try {
            // Simular diferentes tipos de erro baseados no erro
            when {
                error.contains("15009") || error.contains("unverified") -> {
                    WalletLogger.w(TAG, "❌ [MOCK] Simulando erro 15009: Calling package not verified")
                    promise.reject("CALLING_PACKAGE_NOT_VERIFIED", "15009: Calling package not verified")
                }
                error.contains("15003") || error.contains("not_found") -> {
                    WalletLogger.w(TAG, "❌ [MOCK] Simulando erro 15003: Token não encontrado")
                    promise.reject("TOKEN_NOT_FOUND", "15003: Token não encontrado na carteira ativa")
                }
                error.contains("15004") || error.contains("invalid_state") -> {
                    WalletLogger.w(TAG, "❌ [MOCK] Simulando erro 15004: Estado do token inválido")
                    promise.reject("INVALID_TOKEN_STATE", "15004: Token encontrado mas em estado inválido")
                }
                error.contains("15005") || error.contains("attestation") -> {
                    WalletLogger.w(TAG, "❌ [MOCK] Simulando erro 15005: Falha na verificação de compatibilidade")
                    promise.reject("ATTESTATION_ERROR", "15005: Falha na verificação de compatibilidade do dispositivo")
                }
                error.contains("15002") || error.contains("no_wallet") -> {
                    WalletLogger.w(TAG, "❌ [MOCK] Simulando erro 15002: Nenhuma carteira ativa")
                    promise.reject("NO_ACTIVE_WALLET", "15002: Nenhuma carteira ativa encontrada")
                }
                else -> {
                    // Fallback para sucesso padrão
                    WalletLogger.d(TAG, "✅ [MOCK] Fallback para sucesso padrão")
                    val result = Arguments.createMap()
                    result.putInt("tokenState", 5) // TOKEN_STATE_ACTIVE
                    result.putBoolean("isSelected", true)
                    promise.resolve(result)
                }
            }
        } catch (e: Exception) {
            WalletLogger.w(TAG, "❌ [MOCK] Erro ao simular erro: ${e.message}")
            promise.reject("TOKEN_STATUS_ERROR", "Erro ao obter status do token: ${e.message}")
        }
    }

    override fun getEnvironment(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] getEnvironment chamado")
        fetchFromLocalAPI(
            endpoint = "/wallet/environment",
            defaultResponse = { "PROD" },
            onSuccess = { json ->
                try {
                    if (json.has("environment")) {
                        promise.resolve(json.getString("environment"))
                    } else {
                        promise.resolve("PROD")
                    }
                } catch (e: Exception) {
                    promise.resolve("PROD")
                }
            },
            onError = { _ ->
                promise.resolve("PROD")
            }
        )
    }

    override fun isTokenized(
        fpanLastFour: String,
        cardNetwork: Int,
        tokenServiceProvider: Int,
        promise: Promise
    ) {
        WalletLogger.d(TAG, "🔍 [MOCK] isTokenized chamado - LastFour: $fpanLastFour, Network: $cardNetwork, Provider: $tokenServiceProvider")

        if (!ensureWalletAvailable(promise, "isTokenized")) {
            return
        }

        val endpoint = "/wallet/is-tokenized?lastFour=$fpanLastFour&network=$cardNetwork&provider=$tokenServiceProvider"
        fetchFromLocalAPI(
            endpoint = endpoint,
            defaultResponse = { fpanLastFour == "1234" },
            onSuccess = { json ->
                try {
                    if (json.has("isTokenized")) {
                        promise.resolve(json.getBoolean("isTokenized"))
                    } else {
                        promise.resolve(fpanLastFour == "1234")
                    }
                } catch (e: Exception) {
                    promise.resolve(fpanLastFour == "1234")
                }
            },
            onError = { _ ->
                promise.resolve(fpanLastFour == "1234")
            }
        )
    }

    override fun viewToken(
        tokenServiceProvider: Int,
        issuerTokenId: String,
        promise: Promise
    ) {
        WalletLogger.d(TAG, "🔍 [MOCK] viewToken chamado - Provider: $tokenServiceProvider, TokenId: $issuerTokenId")

        if (!ensureWalletAvailable(promise, "viewToken")) {
            return
        }

        val endpoint = "/wallet/view-token?provider=$tokenServiceProvider&tokenId=$issuerTokenId"
        fetchFromLocalAPI(
            endpoint = endpoint,
            defaultResponse = {
                // Simular dados do token encontrado
                val tokenData = Arguments.createMap()
                tokenData.putString("issuerTokenId", issuerTokenId)
                tokenData.putString("issuerName", "Banco Mock")
                tokenData.putString("fpanLastFour", "1234")
                tokenData.putString("dpanLastFour", "4321")
                tokenData.putInt("tokenServiceProvider", tokenServiceProvider)
                tokenData.putInt("network", 12) // CARD_NETWORK_ELO
                tokenData.putInt("tokenState", 5) // TOKEN_STATE_ACTIVE
                tokenData.putBoolean("isDefaultToken", true)
                tokenData.putString("portfolioName", "Carteira Principal")
                WalletLogger.d(TAG, "✅ [MOCK] Dados do token simulados para: $issuerTokenId")
                tokenData
            },
            onSuccess = { json ->
                try {
                    if (json.has("success") && json.getBoolean("success")) {
                        // Se a API retornar sucesso, criar dados do token
                        val tokenData = Arguments.createMap()
                        tokenData.putString("issuerTokenId", issuerTokenId)
                        tokenData.putString("issuerName", json.optString("issuerName", "Banco API"))
                        tokenData.putString("fpanLastFour", json.optString("fpanLastFour", "1234"))
                        tokenData.putString("dpanLastFour", json.optString("dpanLastFour", "4321"))
                        tokenData.putInt("tokenServiceProvider", tokenServiceProvider)
                        tokenData.putInt("network", json.optInt("network", 12))
                        tokenData.putInt("tokenState", json.optInt("tokenState", 5))
                        tokenData.putBoolean("isDefaultToken", json.optBoolean("isDefaultToken", true))
                        tokenData.putString("portfolioName", json.optString("portfolioName", "Carteira Principal"))

                        WalletLogger.d(TAG, "✅ [MOCK] Dados do token obtidos da API para: $issuerTokenId")
                        promise.resolve(tokenData)
                    } else {
                        // Se não encontrou o token, retornar null
                        WalletLogger.w(TAG, "❌ [MOCK] Token não encontrado: $issuerTokenId")
                        promise.resolve(null)
                    }
                } catch (e: Exception) {
                    WalletLogger.w(TAG, "❌ [MOCK] Erro ao processar resposta da API: ${e.message}")
                    promise.resolve(null)
                }
            },
            onError = { _ ->
                // Em caso de erro, retornar dados simulados
                val tokenData = Arguments.createMap()
                tokenData.putString("issuerTokenId", issuerTokenId)
                tokenData.putString("issuerName", "Banco Mock")
                tokenData.putString("fpanLastFour", "1234")
                tokenData.putString("dpanLastFour", "4321")
                tokenData.putInt("tokenServiceProvider", tokenServiceProvider)
                tokenData.putInt("network", 12) // CARD_NETWORK_ELO
                tokenData.putInt("tokenState", 5) // TOKEN_STATE_ACTIVE
                tokenData.putBoolean("isDefaultToken", true)
                tokenData.putString("portfolioName", "Carteira Principal")
                WalletLogger.d(TAG, "✅ [MOCK] Dados do token simulados (fallback) para: $issuerTokenId")
                promise.resolve(tokenData)
            }
        )
    }

    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] addCardToWallet chamado")
        try {
            if (!ensureWalletAvailable(promise, "addCardToWallet")) {
                return
            }

            // Validar dados do cartão (mesmo que na implementação real)
            val validationError = validateCardData(cardData)
            if (validationError != null) {
                WalletLogger.w(TAG, "❌ [MOCK] $validationError")
                promise.reject("INVALID_CARD_DATA", validationError)
                return
            }

            WalletLogger.d(TAG, "🔍 [MOCK] Dados validados com sucesso")
            WalletLogger.d(TAG, "🔍 [MOCK] Dados do cartão recebidos: $cardData")

            val bodyJson = readableMapToJson(cardData).toString()
            fetchFromLocalAPI(
                endpoint = "/wallet/add-card",
                defaultResponse = {
                    // Simular diferentes cenários baseados nos dados do cartão
                    simulateAddCardResponse(cardData)
                },
                onSuccess = { json ->
                    try {
                        // Verificar se há erro na resposta
                        if (json.has("error")) {
                            val errorCode = json.getString("errorCode") ?: "ADD_CARD_ERROR"
                            val errorMessage = json.getString("error") ?: "Erro ao adicionar cartão"
                            WalletLogger.w(TAG, "❌ [MOCK] Erro da API: $errorMessage")
                            promise.reject(errorCode, errorMessage)
                        } else {
                            // Retornar null por enquanto (tokenId não está mais sendo retornado)
                            WalletLogger.d(TAG, "✅ [MOCK] Cartão adicionado com sucesso (sem tokenId)")
                            promise.resolve(null)
                        }
                    } catch (e: Exception) {
                        WalletLogger.w(TAG, "❌ [MOCK] Erro ao processar resposta da API: ${e.message}")
                        // Retornar null mesmo em caso de erro no processamento
                        WalletLogger.d(TAG, "✅ [MOCK] Cartão adicionado com sucesso (sem tokenId - fallback)")
                        promise.resolve(null)
                    }
                },
                onError = { error ->
                    // Simular diferentes tipos de erro baseados no erro da API
                    simulateAddCardError(error.toString(), cardData, promise)
                },
                method = "POST",
                body = bodyJson
            )
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [MOCK] Erro em addCardToWallet: ${e.message}", e)
            promise.reject("ADD_CARD_TO_WALLET_ERROR", e.message, e)
        }
    }

    /**
     * Simula resposta padrão de sucesso do addCardToWallet
     */
    private fun simulateAddCardResponse(cardData: ReadableMap): String {
        try {
            // Sempre retorna sucesso para o fallback
            val tokenId = "mock_token_${System.currentTimeMillis()}"
            return """{"tokenId": "$tokenId", "success": true, "message": "Cartão adicionado com sucesso"}"""
        } catch (e: Exception) {
            WalletLogger.w(TAG, "❌ [MOCK] Erro ao simular resposta: ${e.message}")
            val tokenId = "mock_token_${System.currentTimeMillis()}"
            return """{"tokenId": "$tokenId", "success": true, "message": "Cartão adicionado com sucesso"}"""
        }
    }

    /**
     * Simula fallback de erro do addCardToWallet (sempre sucesso)
     */
    private fun simulateAddCardError(error: String, cardData: ReadableMap, promise: Promise) {
        try {
            // Sempre retorna sucesso no fallback de erro
            val mockTokenId = "mock_token_${System.currentTimeMillis()}"
            WalletLogger.d(TAG, "✅ [MOCK] Token ID gerado (fallback de erro): $mockTokenId")
            promise.resolve(mockTokenId)
        } catch (e: Exception) {
            WalletLogger.w(TAG, "❌ [MOCK] Erro ao simular erro: ${e.message}")
            val mockTokenId = "mock_token_${System.currentTimeMillis()}"
            promise.resolve(mockTokenId)
        }
    }

    /**
     * Valida os dados do cartão para Push Provisioning (mesmo que na implementação real)
     */
    private fun validateCardData(cardData: ReadableMap): String? {
        val address = cardData.getMap("address")
        val card = cardData.getMap("card")

        if (address == null) {
            return "Campo 'address' é obrigatório"
        }

        if (card == null) {
            return "Campo 'card' é obrigatório"
        }

        // Validar campos obrigatórios do cartão
        val opaquePaymentCard = card.getString("opaquePaymentCard")
        val displayName = card.getString("displayName")
        val lastDigits = card.getString("lastDigits")

        if (opaquePaymentCard.isNullOrEmpty()) {
            return "Campo 'opaquePaymentCard' é obrigatório"
        }

        if (displayName.isNullOrEmpty()) {
            return "Campo 'displayName' é obrigatório"
        }

        if (lastDigits.isNullOrEmpty()) {
            return "Campo 'lastDigits' é obrigatório"
        }

        // Validar formato do opaquePaymentCard (deve ser base64)
        try {
            android.util.Base64.decode(opaquePaymentCard, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            return "Campo 'opaquePaymentCard' deve estar em formato base64 válido"
        }

        // Validar lastDigits (deve ter 4 dígitos)
        if (!lastDigits.matches(Regex("\\d{4}"))) {
            return "Campo 'lastDigits' deve conter exatamente 4 dígitos"
        }

        return null // Validação passou
    }

    override fun createWalletIfNeeded(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] createWalletIfNeeded chamado")

        if (!ensureWalletAvailable(promise, "createWalletIfNeeded")) {
            return
        }

        fetchFromLocalAPI(
            endpoint = "/wallet/create",
            defaultResponse = { true },
            onSuccess = { json ->
                try {
                    if (json.has("success")) {
                        promise.resolve(json.getBoolean("success"))
                    } else {
                        promise.resolve(true)
                    }
                } catch (e: Exception) {
                    promise.resolve(true)
                }
            },
            onError = { _ ->
                promise.resolve(true)
            },
            method = "POST"
        )
    }

    override fun listTokens(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] listTokens chamado")

        if (!ensureWalletAvailable(promise, "listTokens")) {
            return
        }

        // Função especial para arrays - precisa de tratamento diferente
        val defaultArray = {
            val writableArray = Arguments.createArray()

            // Adicionar alguns tokens simulados
            val token1 = Arguments.createMap()
            token1.putString("issuerTokenId", "mock_token_001")
            token1.putString("issuerName", "Banco Mock")
            token1.putString("fpanLastFour", "1234")
            token1.putString("dpanLastFour", "4321")
            token1.putInt("tokenServiceProvider", 1)
            token1.putInt("tokenState", 5) // TOKEN_STATE_ACTIVE
            token1.putInt("network", 1) // VISA
            token1.putBoolean("isDefaultToken", true)
            token1.putString("portfolioName", "Carteira Principal")
            writableArray.pushMap(token1)

            val token2 = Arguments.createMap()
            token2.putString("issuerTokenId", "mock_token_002")
            token2.putString("issuerName", "Banco Mock")
            token2.putString("fpanLastFour", "5678")
            token2.putString("dpanLastFour", "8765")
            token2.putInt("tokenServiceProvider", 1)
            token2.putInt("tokenState", 5) // TOKEN_STATE_ACTIVE
            token2.putInt("network", 2) // MASTERCARD
            token2.putBoolean("isDefaultToken", false)
            token2.putString("portfolioName", "Outros Cartões")
            writableArray.pushMap(token2)

            WalletLogger.d(TAG, "✅ [MOCK] Lista de tokens obtida (valor padrão) - ${writableArray.size()} tokens")
            writableArray
        }

        fetchFromLocalAPI(
            endpoint = "/wallet/tokens",
            defaultResponse = defaultArray,
            onSuccess = { jsonResponse ->
                try {
                    val writableArray = Arguments.createArray()

                    // Se a API retornar um array de tokens
                    if (jsonResponse.has("tokens")) {
                        val tokensArray = jsonResponse.getJSONArray("tokens")
                        for (i in 0 until tokensArray.length()) {
                            val tokenJson = tokensArray.getJSONObject(i)
                            val token = Arguments.createMap()

                        if (tokenJson.has("issuerTokenId")) token.putString("issuerTokenId", tokenJson.getString("issuerTokenId"))
                        if (tokenJson.has("issuerName")) token.putString("issuerName", tokenJson.getString("issuerName"))
                        if (tokenJson.has("fpanLastFour")) token.putString("fpanLastFour", tokenJson.getString("fpanLastFour"))
                        if (tokenJson.has("dpanLastFour")) token.putString("dpanLastFour", tokenJson.getString("dpanLastFour"))
                        if (tokenJson.has("tokenServiceProvider")) token.putInt("tokenServiceProvider", tokenJson.getInt("tokenServiceProvider"))
                        if (tokenJson.has("network")) token.putInt("network", tokenJson.getInt("network"))
                        if (tokenJson.has("tokenState")) token.putInt("tokenState", tokenJson.getInt("tokenState"))
                        if (tokenJson.has("isDefaultToken")) token.putBoolean("isDefaultToken", tokenJson.getBoolean("isDefaultToken"))
                        if (tokenJson.has("portfolioName")) token.putString("portfolioName", tokenJson.getString("portfolioName"))
                        // remover campos legados (não usados)

                            writableArray.pushMap(token)
                        }
                    }

                    WalletLogger.d(TAG, "✅ [API] Lista de tokens obtida da API - ${writableArray.size()} tokens")
                    promise.resolve(writableArray)
                } catch (e: Exception) {
                    WalletLogger.e(TAG, "❌ [API] Erro ao processar resposta da API: ${e.message}")
                    promise.resolve(defaultArray())
                }
            },
            onError = { _ ->
                promise.resolve(defaultArray())
            }
        )
    }


    private fun checkPendingDataFromMainActivity() {
        WalletLogger.d(TAG, "🔍 [GOOGLE] Verificando dados pendentes...")
        try {
            // Verificar se há dados pendentes
            val hasData = hasPendingData()

            if (hasData) {
                WalletLogger.d(TAG, "✅ [GOOGLE] Dados pendentes encontrados")

                // Obter os dados pendentes sem limpar
                val data = getPendingIntentDataWithoutClearing()
                val action = getPendingIntentAction()
                val callingPackage = getPendingCallingPackage()

                if (data != null && data.isNotEmpty()) {
                    WalletLogger.d(TAG, "📋 [GOOGLE] Processando dados pendentes: ${data.length} caracteres")
                    WalletLogger.d(TAG, "📋 [GOOGLE] Action: $action, CallingPackage: $callingPackage")

                    // Verificar se action e callingPackage estão disponíveis
                    if (action == null) {
                        WalletLogger.e(TAG, "❌ [GOOGLE] Action é null - não é possível processar intent")
                        return
                    }

                    if (callingPackage == null) {
                        WalletLogger.e(TAG, "❌ [GOOGLE] CallingPackage é null - não é possível processar intent")
                        return
                    }

                    // Processar os dados como um intent usando os valores reais
                    processWalletIntentData(data, action, callingPackage)

                    // Limpar dados após processamento bem-sucedido
                    clearPendingData()
                } else {
                    WalletLogger.w(TAG, "⚠️ [GOOGLE] Dados pendentes são null ou vazios")
                }
            } else {
                WalletLogger.d(TAG, "🔍 [GOOGLE] Nenhum dado pendente")
            }
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [GOOGLE] Erro ao verificar dados pendentes: ${e.message}", e)
        }
    }

    //https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?hl=pt-br&authuser=1#add_a_listener_for_wallet_updates
    override fun setIntentListener(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [GOOGLE] setIntentListener chamado")
        try {
            intentListenerActive = true
            WalletLogger.d(TAG, "✅ [GOOGLE] Listener de intent ativado")

            // Verificar dados pendentes da MainActivity automaticamente
            checkPendingDataFromMainActivity()

            // Processar eventos de nenhuma intent pendentes
            GoogleWalletModule.processNoIntentReceivedEvent(reactContext)

            // Processar eventos de caller válido sem intent pendentes
            GoogleWalletModule.processValidCallerNoIntentEvent(reactContext)

            promise.resolve(true)
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [GOOGLE] Erro ao ativar listener de intent: ${e.message}", e)
            promise.reject("SET_INTENT_LISTENER_ERROR", e.message, e)
        }
    }

    override fun removeIntentListener(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] removeIntentListener chamado")
        try {
            intentListenerActive = false
            WalletLogger.d(TAG, "✅ [MOCK] Listener de intent desativado")
            promise.resolve(true)
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [MOCK] Erro ao desativar listener de intent: ${e.message}", e)
            promise.reject("REMOVE_INTENT_LISTENER_ERROR", e.message, e)
        }
    }

    override fun setActivationResult(status: String, activationCode: String?, promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] setActivationResult chamado - Status: $status, ActivationCode: $activationCode")
        try {
            activity = reactContext.currentActivity
            if (activity == null) {
                WalletLogger.w(TAG, "❌ [MOCK] Nenhuma atividade disponível para definir resultado")
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }

            val validStatuses = listOf("approved", "declined", "failure")
            if (!validStatuses.contains(status)) {
                WalletLogger.w(TAG, "❌ [MOCK] Status inválido: $status. Deve ser: approved, declined ou failure")
                promise.reject("INVALID_STATUS", "Status deve ser: approved, declined ou failure")
                return
            }

            val resultIntent = android.content.Intent()
            resultIntent.putExtra("BANKING_APP_ACTIVATION_RESPONSE", status)

            if (activationCode != null && activationCode.isNotEmpty() && status == "approved") {
                WalletLogger.d(TAG, "🔍 [MOCK] Adicionando activationCode: $activationCode")
                resultIntent.putExtra("BANKING_APP_ACTIVATION_CODE", activationCode)
            }

            activity?.setResult(Activity.RESULT_OK, resultIntent)

            WalletLogger.d(TAG, "✅ [MOCK] Resultado de ativação definido - Status: $status")
            if (activationCode != null && activationCode.isNotEmpty() && status == "approved") {
                WalletLogger.d(TAG, "✅ [MOCK] ActivationCode incluído: $activationCode")
            }

            promise.resolve(true)
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [MOCK] Erro ao definir resultado de ativação: ${e.message}", e)
            promise.reject("SET_ACTIVATION_RESULT_ERROR", e.message, e)
        }
    }

    override fun finishActivity(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] finishActivity chamado")
        try {
            activity = reactContext.currentActivity
            if (activity == null) {
                WalletLogger.w(TAG, "❌ [MOCK] Nenhuma atividade disponível para finalizar")
                promise.reject("NO_ACTIVITY", "Nenhuma atividade disponível")
                return
            }

            activity?.finish()
            WalletLogger.d(TAG, "✅ [MOCK] Atividade finalizada com sucesso")
            promise.resolve(true)
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [MOCK] Erro ao finalizar atividade: ${e.message}", e)
            promise.reject("FINISH_ACTIVITY_ERROR", e.message, e)
        }
    }

    override fun getConstants(): MutableMap<String, Any> {
        WalletLogger.d(TAG, "🔍 [MOCK] getConstants chamado")

        val constants = hashMapOf<String, Any>()

        constants["SDK_NAME"] = "GoogleWallet"
        constants["GOOGLE_WALLET_PACKAGE"] = GOOGLE_WALLET_PACKAGE
        constants["GOOGLE_WALLET_APP_PACKAGE"] = GOOGLE_WALLET_APP_PACKAGE
        constants["GOOGLE_WALLET_PLAY_STORE_URL"] = GOOGLE_WALLET_PLAY_STORE_URL

        // Google Token Provider - valores simulados
        constants["TOKEN_PROVIDER_AMEX"] = 1
        constants["TOKEN_PROVIDER_DISCOVER"] = 2
        constants["TOKEN_PROVIDER_JCB"] = 3
        constants["TOKEN_PROVIDER_MASTERCARD"] = 4
        constants["TOKEN_PROVIDER_VISA"] = 5
        constants["TOKEN_PROVIDER_ELO"] = 14

        // Google Card Network - valores simulados
        constants["CARD_NETWORK_AMEX"] = 1
        constants["CARD_NETWORK_DISCOVER"] = 2
        constants["CARD_NETWORK_MASTERCARD"] = 3
        constants["CARD_NETWORK_QUICPAY"] = 4
        constants["CARD_NETWORK_PRIVATE_LABEL"] = 5
        constants["CARD_NETWORK_VISA"] = 6
        constants["CARD_NETWORK_ELO"] = 12

        // TapAndPay Status Codes - valores reais do SDK
        constants["TAP_AND_PAY_NO_ACTIVE_WALLET"] = 15002
        constants["TAP_AND_PAY_TOKEN_NOT_FOUND"] = 15003
        constants["TAP_AND_PAY_INVALID_TOKEN_STATE"] = 15004
        constants["TAP_AND_PAY_ATTESTATION_ERROR"] = 15005
        constants["TAP_AND_PAY_UNAVAILABLE"] = 15009
        constants["TAP_AND_PAY_SAVE_CARD_ERROR"] = 15019
        constants["TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION"] = 15021
        constants["TAP_AND_PAY_TOKENIZATION_DECLINED"] = 15022
        constants["TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR"] = 15023
        constants["TAP_AND_PAY_TOKENIZE_ERROR"] = 15024
        constants["TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED"] = 15025
        constants["TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT"] = 15026
        constants["TAP_AND_PAY_USER_CANCELED_FLOW"] = 15027
        constants["TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED"] = 15028

        // Google Token State - valores simulados
        constants["TOKEN_STATE_UNTOKENIZED"] = 1
        constants["TOKEN_STATE_PENDING"] = 2
        constants["TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION"] = 3
        constants["TOKEN_STATE_SUSPENDED"] = 4
        constants["TOKEN_STATE_ACTIVE"] = 5
        constants["TOKEN_STATE_FELICA_PENDING_PROVISIONING"] = 6

        // Google Common Status Codes - valores reais do SDK
        constants["SUCCESS"] = 0
        constants["SUCCESS_CACHE"] = -1
        constants["SERVICE_VERSION_UPDATE_REQUIRED"] = 2
        constants["SERVICE_DISABLED"] = 3
        constants["SIGN_IN_REQUIRED"] = 4
        constants["INVALID_ACCOUNT"] = 5
        constants["RESOLUTION_REQUIRED"] = 6
        constants["NETWORK_ERROR"] = 7
        constants["INTERNAL_ERROR"] = 8
        constants["DEVELOPER_ERROR"] = 10
        constants["ERROR"] = 13
        constants["INTERRUPTED"] = 14
        constants["TIMEOUT"] = 15
        constants["CANCELED"] = 16
        constants["API_NOT_CONNECTED"] = 17
        constants["REMOTE_EXCEPTION"] = 19
        constants["CONNECTION_SUSPENDED_DURING_CALL"] = 20
        constants["RECONNECTION_TIMED_OUT_DURING_UPDATE"] = 21
        constants["RECONNECTION_TIMED_OUT"] = 22

        WalletLogger.d(TAG, "✅ [MOCK] Constantes obtidas (simuladas)")
        return constants
    }

    override fun openWallet(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] openWallet chamado")
        try {
            if (walletOpener == null) {
                WalletLogger.w(TAG, "WALLET_OPENER_NOT_AVAILABLE: WalletOpener não foi inicializado")
                promise.reject("WALLET_OPENER_NOT_AVAILABLE", "WalletOpener não foi inicializado")
                return
            }

            val packageName = GOOGLE_WALLET_APP_PACKAGE
            val appName = "Google Wallet"
            val playStoreUrl = "market://details?id=$packageName"
            val webUrl = GOOGLE_WALLET_PLAY_STORE_URL

            val success = walletOpener!!.openWallet(packageName, appName, playStoreUrl, webUrl)

            if (success) {
                WalletLogger.d(TAG, "✅ [MOCK] Wallet aberto com sucesso")
                promise.resolve(true)
            } else {
                WalletLogger.w(TAG, "❌ [MOCK] Falha ao abrir wallet")
                promise.reject("OPEN_WALLET_ERROR", "Falha ao abrir Google Wallet")
            }
        } catch (e: Exception) {
            WalletLogger.e(TAG, "OPEN_WALLET_ERROR: ${e.message}")
            promise.reject("OPEN_WALLET_ERROR", e.message, e)
        }
    }

    override fun sendNoIntentReceivedEvent() {
        WalletLogger.d(TAG, "🔍 [GOOGLE] sendNoIntentReceivedEvent chamado")
        try {
            sendEventToReactNative("GoogleWalletNoIntentReceived", null)
            WalletLogger.d(TAG, "✅ [GOOGLE] Evento de nenhuma intent enviado com sucesso")
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [GOOGLE] Erro ao enviar evento de nenhuma intent: ${e.message}", e)
        }
    }

    override fun sendValidCallerNoIntentEvent() {
        WalletLogger.d(TAG, "⚠️ [GOOGLE] sendValidCallerNoIntentEvent chamado")
        try {
            sendEventToReactNative("GoogleWalletValidCallerNoIntentReceived", null)
            WalletLogger.d(TAG, "✅ [GOOGLE] Evento de caller válido sem intent enviado com sucesso")
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [GOOGLE] Erro ao enviar evento de caller válido sem intent: ${e.message}", e)
        }
    }

    override fun setLogListener(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] setLogListener chamado")
        try {
            WalletLogger.setLogListener(true)
            WalletLogger.d(TAG, "✅ [MOCK] Listener de log ativado")
            promise.resolve(true)
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [MOCK] Erro ao ativar listener de log: ${e.message}", e)
            promise.reject("SET_LOG_LISTENER_ERROR", e.message, e)
        }
    }

    override fun removeLogListener(promise: Promise) {
        WalletLogger.d(TAG, "🔍 [MOCK] removeLogListener chamado")
        try {
            WalletLogger.setLogListener(false)
            WalletLogger.d(TAG, "✅ [MOCK] Listener de log desativado")
            promise.resolve(true)
        } catch (e: Exception) {
            WalletLogger.e(TAG, "❌ [MOCK] Erro ao desativar listener de log: ${e.message}", e)
            promise.reject("REMOVE_LOG_LISTENER_ERROR", e.message, e)
        }
    }

}
