package com.builders.wallet.samsungpay

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import com.builders.wallet.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*
import org.json.JSONObject

class SamsungWalletMock(private val reactContext: com.facebook.react.bridge.ReactApplicationContext) : SamsungWalletContract {

    companion object {
        private const val TAG = "SamsungWalletMock"
        private const val DEFAULT_API_BASE_URL = "http://localhost:3000"
        private const val REQUEST_TIMEOUT = 5000 // 5 segundos
        
        // Obter URL da API do BuildConfig
        private val API_BASE_URL: String by lazy {
            try {
                val buildConfigUrl = BuildConfig.SAMSUNG_WALLET_MOCK_API_URL
                if (buildConfigUrl.isNotEmpty()) {
                    Log.d(TAG, "🌐 [MOCK] Usando API URL do BuildConfig: $buildConfigUrl")
                    return@lazy buildConfigUrl
                }
                
                // Se não configurado, usar DEFAULT_API_BASE_URL
                Log.d(TAG, "🌐 [MOCK] API URL não configurada, usando DEFAULT: $DEFAULT_API_BASE_URL")
                DEFAULT_API_BASE_URL
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ [MOCK] Erro ao obter URL da API: ${e.message}")
                DEFAULT_API_BASE_URL
            }
        }
    }

    /**
     * Função para buscar dados de uma API local com fallback para valores padrão
     */
    private fun fetchFromLocalAPI(
        endpoint: String,
        defaultResponse: () -> Any,
        onSuccess: (JSONObject) -> Unit,
        onError: (Exception) -> Unit,
        method: String = "GET",
        body: String? = null
    ) {
        val apiUrl = API_BASE_URL
        
        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                val urlString = "$apiUrl$endpoint"
                Log.d(TAG, "🌐 [API][REQUEST] ➜ $method $urlString")
                
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
                Log.d(TAG, "🌐 [API][RESPONSE] ⇦ code=$responseCode (${tookMs}ms)")
                
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
                    val jsonResponse = JSONObject(raw)
                    Log.d(TAG, "✅ [API] Dados obtidos com sucesso da API local")
                    
                    withContext(Dispatchers.Main) {
                        onSuccess(jsonResponse)
                    }
                } else {
                    throw Exception("API retornou código de erro: $responseCode")
                }
                
            } catch (e: Exception) {
                Log.w(TAG, "❌ [API] Erro ao buscar dados da API local: ${e::class.java.simpleName}: ${e.message}")
                Log.d(TAG, "🔄 [API] Usando valor padrão como fallback")
                
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * Função auxiliar para buscar dados da API com Promise
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
                    Log.e(TAG, "❌ [API] Erro ao processar resposta da API: ${e.message}")
                    promise.resolve(defaultResponse())
                }
            },
            onError = { _ ->
                promise.resolve(defaultResponse())
            }
        )
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
                ReadableType.Array -> {
                    // Para arrays, converter para JSONArray
                    val array = map.getArray(key)!!
                    val jsonArray = org.json.JSONArray()
                    for (i in 0 until array.size()) {
                        when (array.getType(i)) {
                            ReadableType.Null -> jsonArray.put(org.json.JSONObject.NULL)
                            ReadableType.Boolean -> jsonArray.put(array.getBoolean(i))
                            ReadableType.Number -> jsonArray.put(array.getDouble(i))
                            ReadableType.String -> jsonArray.put(array.getString(i))
                            ReadableType.Map -> jsonArray.put(readableMapToJson(array.getMap(i)!!))
                            ReadableType.Array -> {
                                // Para arrays aninhados, converter recursivamente
                                val nestedArray = array.getArray(i)!!
                                val nestedJsonArray = org.json.JSONArray()
                                for (j in 0 until nestedArray.size()) {
                                    when (nestedArray.getType(j)) {
                                        ReadableType.Null -> nestedJsonArray.put(org.json.JSONObject.NULL)
                                        ReadableType.Boolean -> nestedJsonArray.put(nestedArray.getBoolean(j))
                                        ReadableType.Number -> nestedJsonArray.put(nestedArray.getDouble(j))
                                        ReadableType.String -> nestedJsonArray.put(nestedArray.getString(j))
                                        ReadableType.Map -> nestedJsonArray.put(readableMapToJson(nestedArray.getMap(j)!!))
                                        else -> nestedJsonArray.put(nestedArray.getString(j))
                                    }
                                }
                                jsonArray.put(nestedJsonArray)
                            }
                            else -> jsonArray.put(array.getString(i))
                        }
                    }
                    json.put(key, jsonArray)
                }
            }
        }
        return json
    }

    override fun init(serviceId: String, promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] init chamado com serviceId: $serviceId")
        fetchFromLocalAPI(
            endpoint = "/samsung/init",
            defaultResponse = { true },
            onSuccess = { json ->
                try {
                    promise.resolve(json.optBoolean("success", true))
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ [MOCK] Erro ao processar init, usando fallback: ${e.message}")
                    promise.resolve(true)
                }
            },
            onError = {
                promise.resolve(true)
            },
            method = "POST",
            body = """{"serviceId": "$serviceId"}"""
        )
    }

    override fun getSamsungPayStatus(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getSamsungPayStatus chamado")
        fetchFromLocalAPI(
            endpoint = "/samsung/status",
            defaultResponse = { 1 }, // SPAY_READY
            onSuccess = { json ->
                try {
                    promise.resolve(json.optInt("status", 1))
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ [MOCK] Erro ao processar status, usando fallback: ${e.message}")
                    promise.resolve(1)
                }
            },
            onError = {
                promise.resolve(1)
            }
        )
    }

    override fun goToUpdatePage() {
        Log.d(TAG, "🔍 [MOCK] goToUpdatePage chamado")
        // Simular abertura da página de atualização
        Log.d(TAG, "✅ [MOCK] Página de atualização simulada")
    }

    override fun activateSamsungPay() {
        Log.d(TAG, "🔍 [MOCK] activateSamsungPay chamado")
        // Simular ativação do Samsung Pay
        Log.d(TAG, "✅ [MOCK] Samsung Pay ativado (simulado)")
    }

    override fun getAllCards(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getAllCards chamado")
        
        val defaultArray = {
            val writableArray = Arguments.createArray()
            
            // Adicionar alguns cartões simulados
            val card1 = Arguments.createMap()
            card1.putString("cardId", "mock_card_001")
            card1.putString("cardName", "Cartão Mock Visa")
            card1.putString("lastFourDigits", "1234")
            card1.putString("cardType", "CREDIT")
            card1.putString("issuerName", "Banco Mock")
            card1.putString("tokenizationProvider", "VISA")
            card1.putInt("cardState", 1) // ACTIVE
            writableArray.pushMap(card1)
            
            val card2 = Arguments.createMap()
            card2.putString("cardId", "mock_card_002")
            card2.putString("cardName", "Cartão Mock Mastercard")
            card2.putString("lastFourDigits", "5678")
            card2.putString("cardType", "DEBIT")
            card2.putString("issuerName", "Banco Mock")
            card2.putString("tokenizationProvider", "MASTERCARD")
            card2.putInt("cardState", 1) // ACTIVE
            writableArray.pushMap(card2)
            
            Log.d(TAG, "✅ [MOCK] Lista de cartões obtida (valor padrão) - ${writableArray.size()} cartões")
            writableArray
        }

        fetchFromLocalAPI(
            endpoint = "/samsung/cards",
            defaultResponse = defaultArray,
            onSuccess = { jsonResponse ->
                try {
                    val writableArray = Arguments.createArray()
                    
                    if (jsonResponse.has("cards")) {
                        val cardsArray = jsonResponse.getJSONArray("cards")
                        for (i in 0 until cardsArray.length()) {
                            val cardJson = cardsArray.getJSONObject(i)
                            val card = Arguments.createMap()
                            
                            if (cardJson.has("cardId")) card.putString("cardId", cardJson.getString("cardId"))
                            if (cardJson.has("cardName")) card.putString("cardName", cardJson.getString("cardName"))
                            if (cardJson.has("lastFourDigits")) card.putString("lastFourDigits", cardJson.getString("lastFourDigits"))
                            if (cardJson.has("cardType")) card.putString("cardType", cardJson.getString("cardType"))
                            if (cardJson.has("issuerName")) card.putString("issuerName", cardJson.getString("issuerName"))
                            if (cardJson.has("tokenizationProvider")) card.putString("tokenizationProvider", cardJson.getString("tokenizationProvider"))
                            if (cardJson.has("cardState")) card.putInt("cardState", cardJson.getInt("cardState"))
                            
                            writableArray.pushMap(card)
                        }
                    }
                    
                    Log.d(TAG, "✅ [API] Lista de cartões obtida da API - ${writableArray.size()} cartões")
                    promise.resolve(writableArray)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ [API] Erro ao processar resposta da API: ${e.message}")
                    promise.resolve(defaultArray())
                }
            },
            onError = { _ ->
                promise.resolve(defaultArray())
            }
        )
    }

    override fun getWalletInfo(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getWalletInfo chamado")
        fetchFromAPIWithPromise(
            endpoint = "/samsung/wallet-info",
            defaultResponse = {
                val result = Arguments.createMap()
                result.putString("WALLET_DM_ID", "mock_wallet_dm_12345")
                result.putString("DEVICE_ID", "mock_device_67890")
                result.putString("WALLET_USER_ID", "mock_user_54321")
                Log.d(TAG, "✅ [MOCK] Informações da carteira obtidas (valor padrão)")
                result
            },
            promise = promise
        )
    }

    override fun addCard(
        payload: String,
        issuerId: String,
        tokenizationProvider: String,
        progress: Callback,
        promise: Promise
    ) {
        Log.d(TAG, "🔍 [MOCK] addCard chamado - Provider: $tokenizationProvider, IssuerId: $issuerId")
        
        val bodyJson = JSONObject().apply {
            put("payload", payload)
            put("issuerId", issuerId)
            put("tokenizationProvider", tokenizationProvider)
        }.toString()
        
        fetchFromLocalAPI(
            endpoint = "/samsung/add-card",
            defaultResponse = { 
                // Simular cartão adicionado com sucesso
                val card = Arguments.createMap()
                card.putString("cardId", "mock_card_${System.currentTimeMillis()}")
                card.putString("cardName", "Cartão Adicionado")
                card.putString("lastFourDigits", "9999")
                card.putString("cardType", "CREDIT")
                card.putString("issuerName", "Banco Mock")
                card.putString("tokenizationProvider", tokenizationProvider)
                card.putInt("cardState", 1) // ACTIVE
                Log.d(TAG, "✅ [MOCK] Cartão adicionado com sucesso (simulado)")
                card
            },
            onSuccess = { json ->
                try {
                    if (json.has("error")) {
                        val errorCode = json.getString("errorCode") ?: "ADD_CARD_ERROR"
                        val errorMessage = json.getString("error") ?: "Erro ao adicionar cartão"
                        Log.w(TAG, "❌ [MOCK] Erro da API: $errorMessage")
                        promise.reject(errorCode, errorMessage)
                    } else {
                        val card = Arguments.createMap()
                        card.putString("cardId", json.optString("cardId", "mock_card_${System.currentTimeMillis()}"))
                        card.putString("cardName", json.optString("cardName", "Cartão Adicionado"))
                        card.putString("lastFourDigits", json.optString("lastFourDigits", "9999"))
                        card.putString("cardType", json.optString("cardType", "CREDIT"))
                        card.putString("issuerName", json.optString("issuerName", "Banco API"))
                        card.putString("tokenizationProvider", tokenizationProvider)
                        card.putInt("cardState", json.optInt("cardState", 1))
                        
                        Log.d(TAG, "✅ [MOCK] Cartão adicionado com sucesso da API")
                        promise.resolve(card)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "❌ [MOCK] Erro ao processar resposta da API: ${e.message}")
                    val card = Arguments.createMap()
                    card.putString("cardId", "mock_card_${System.currentTimeMillis()}")
                    card.putString("cardName", "Cartão Adicionado")
                    card.putString("lastFourDigits", "9999")
                    card.putString("cardType", "CREDIT")
                    card.putString("issuerName", "Banco Mock")
                    card.putString("tokenizationProvider", tokenizationProvider)
                    card.putInt("cardState", 1)
                    promise.resolve(card)
                }
            },
            onError = { error ->
                // Sempre retorna sucesso no fallback de erro
                val card = Arguments.createMap()
                card.putString("cardId", "mock_card_${System.currentTimeMillis()}")
                card.putString("cardName", "Cartão Adicionado")
                card.putString("lastFourDigits", "9999")
                card.putString("cardType", "CREDIT")
                card.putString("issuerName", "Banco Mock")
                card.putString("tokenizationProvider", tokenizationProvider)
                card.putInt("cardState", 1)
                Log.d(TAG, "✅ [MOCK] Cartão adicionado com sucesso (fallback)")
                promise.resolve(card)
            },
            method = "POST",
            body = bodyJson
        )
    }

    override fun checkWalletAvailability(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] checkWalletAvailability chamado")
        getSamsungPayStatus(promise)
    }

    override fun getSecureWalletInfo(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getSecureWalletInfo chamado")
        getWalletInfo(promise)
    }

    override fun addCardToWallet(cardData: ReadableMap, promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] addCardToWallet chamado")
        try {
            val payload = cardData.getString("payload") ?: ""
            val issuerId = cardData.getString("issuerId") ?: ""
            val tokenizationProvider = cardData.getString("tokenizationProvider") ?: "VISA"
            
            addCard(payload, issuerId, tokenizationProvider, object : Callback {
                override fun invoke(vararg args: Any?) {
                    // Progress callback vazio para compatibilidade
                }
            }, promise)
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MOCK] Erro em addCardToWallet: ${e.message}", e)
            promise.reject("ADD_CARD_TO_WALLET_ERROR", e.message, e)
        }
    }

    override fun getCardStatusBySuffix(lastDigits: String, promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getCardStatusBySuffix chamado com lastDigits: $lastDigits")
        fetchFromLocalAPI(
            endpoint = "/samsung/card-status/suffix?lastDigits=$lastDigits",
            defaultResponse = { "not found" },
            onSuccess = { json ->
                try {
                    promise.resolve(json.optString("status", "not found"))
                } catch (e: Exception) {
                    promise.resolve("not found")
                }
            },
            onError = { _ ->
                promise.resolve("not found")
            }
        )
    }

    override fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] getCardStatusByIdentifier chamado com identifier: $identifier, tsp: $tsp")
        fetchFromLocalAPI(
            endpoint = "/samsung/card-status/identifier?identifier=$identifier&tsp=$tsp",
            defaultResponse = { "not found" },
            onSuccess = { json ->
                try {
                    promise.resolve(json.optString("status", "not found"))
                } catch (e: Exception) {
                    promise.resolve("not found")
                }
            },
            onError = { _ ->
                promise.resolve("not found")
            }
        )
    }

    override fun createWalletIfNeeded(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] createWalletIfNeeded chamado")
        fetchFromLocalAPI(
            endpoint = "/samsung/create-wallet",
            defaultResponse = { false },
            onSuccess = { json ->
                try {
                    promise.resolve(json.optBoolean("created", false))
                } catch (e: Exception) {
                    promise.resolve(false)
                }
            },
            onError = { _ ->
                promise.resolve(false)
            },
            method = "POST"
        )
    }

    override fun getConstants(): MutableMap<String, Any> {
        Log.d(TAG, "🔍 [MOCK] getConstants chamado")
        
        val constants = hashMapOf<String, Any>()
        
        constants["SDK_NAME"] = "SamsungWallet"
        
        // Samsung Pay Status Codes - valores simulados
        constants["SPAY_READY"] = 1
        constants["SPAY_NOT_READY"] = 0
        constants["SPAY_NEED_UPDATE"] = 2
        constants["SPAY_NEED_ACTIVATION"] = 3
        
        // Samsung Card Types - valores simulados
        constants["CARD_TYPE_CREDIT"] = 1
        constants["CARD_TYPE_DEBIT"] = 2
        constants["CARD_TYPE_CREDIT_DEBIT"] = 3
        
        // Samsung Card States - valores simulados
        constants["CARD_STATE_ACTIVE"] = 1
        constants["CARD_STATE_INACTIVE"] = 0
        constants["CARD_STATE_PENDING"] = 2
        constants["CARD_STATE_SUSPENDED"] = 3
        
        // Samsung Tokenization Providers - valores simulados
        constants["PROVIDER_VISA"] = "VISA"
        constants["PROVIDER_MASTERCARD"] = "MASTERCARD"
        constants["PROVIDER_AMEX"] = "AMEX"
        constants["PROVIDER_ELO"] = "ELO"
        
        // Samsung Error Codes - valores simulados
        constants["ERROR_NONE"] = 0
        constants["ERROR_SDK_NOT_AVAILABLE"] = 1001
        constants["ERROR_INIT_FAILED"] = 1002
        constants["ERROR_CARD_ADD_FAILED"] = 1003
        constants["ERROR_WALLET_NOT_AVAILABLE"] = 1004
        
        Log.d(TAG, "✅ [MOCK] Constantes obtidas (simuladas)")
        return constants
    }
}