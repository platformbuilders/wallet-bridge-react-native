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
        
        // Simular a criação do PartnerInfo com Bundle (como na implementação real)
        try {
            val bundle = android.os.Bundle()
            bundle.putString("PartnerServiceType", "INAPP_PAYMENT")
            bundle.putString("EXTRA_ISSUER_NAME", "Builders Wallet")
            Log.d(TAG, "✅ [MOCK] PartnerInfo simulado com Bundle configurado (INAPP_PAYMENT + EXTRA_ISSUER_NAME)")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ [MOCK] Erro ao simular PartnerInfo: ${e.message}")
        }
        
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
            body = """{"serviceId": "$serviceId", "serviceType": "INAPP_PAYMENT", "issuerName": "Builders Wallet"}"""
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
            
            // Adicionar alguns cartões simulados seguindo a estrutura do SerializableCard
            val card1 = Arguments.createMap()
            // Campos básicos do Card
            card1.putString("cardId", "mock_card_001")
            card1.putString("cardStatus", "ACTIVE")
            card1.putString("cardBrand", "VISA")
            
            // Campos do cardInfo Bundle (Samsung Pay específicos)
            card1.putString("last4FPan", "1234")
            card1.putString("last4DPan", "1234")
            card1.putString("app2AppPayload", "mock_payload_visa_001")
            card1.putString("cardType", "CREDIT")
            card1.putString("issuerName", "Banco Mock")
            card1.putString("isDefaultCard", "true")
            card1.putString("deviceType", "phone")
            card1.putString("memberID", "mock_member_001")
            card1.putString("countryCode", "BR")
            card1.putString("cryptogramType", "UCAF")
            card1.putString("requireCpf", "false")
            card1.putString("cpfHolderName", "João Silva")
            card1.putString("cpfNumber", "12345678901")
            card1.putString("merchantRefId", "merchant_001")
            card1.putString("transactionType", "PURCHASE")
            
            // Campos de compatibilidade
            card1.putString("last4", "1234")
            card1.putString("tokenizationProvider", "VISA")
            card1.putString("network", "VISA")
            card1.putString("displayName", "Cartão Mock Visa")
            
            writableArray.pushMap(card1)
            
            val card2 = Arguments.createMap()
            // Campos básicos do Card
            card2.putString("cardId", "mock_card_002")
            card2.putString("cardStatus", "ACTIVE")
            card2.putString("cardBrand", "MASTERCARD")
            
            // Campos do cardInfo Bundle (Samsung Pay específicos)
            card2.putString("last4FPan", "5678")
            card2.putString("last4DPan", "5678")
            card2.putString("app2AppPayload", "mock_payload_mc_002")
            card2.putString("cardType", "DEBIT")
            card2.putString("issuerName", "Banco Mock")
            card2.putString("isDefaultCard", "false")
            card2.putString("deviceType", "phone")
            card2.putString("memberID", "mock_member_002")
            card2.putString("countryCode", "BR")
            card2.putString("cryptogramType", "ICC")
            card2.putString("requireCpf", "true")
            card2.putString("cpfHolderName", "Maria Santos")
            card2.putString("cpfNumber", "98765432109")
            card2.putString("merchantRefId", "merchant_002")
            card2.putString("transactionType", "PURCHASE")
            
            // Campos de compatibilidade
            card2.putString("last4", "5678")
            card2.putString("tokenizationProvider", "MASTERCARD")
            card2.putString("network", "MASTERCARD")
            card2.putString("displayName", "Cartão Mock Mastercard")
            
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
                            
                            // Campos básicos do Card
                            if (cardJson.has("cardId")) card.putString("cardId", cardJson.getString("cardId"))
                            if (cardJson.has("cardStatus")) card.putString("cardStatus", cardJson.getString("cardStatus"))
                            if (cardJson.has("cardBrand")) card.putString("cardBrand", cardJson.getString("cardBrand"))
                            
                            // Campos do cardInfo Bundle (Samsung Pay específicos)
                            if (cardJson.has("last4FPan")) card.putString("last4FPan", cardJson.getString("last4FPan"))
                            if (cardJson.has("last4DPan")) card.putString("last4DPan", cardJson.getString("last4DPan"))
                            if (cardJson.has("app2AppPayload")) card.putString("app2AppPayload", cardJson.getString("app2AppPayload"))
                            if (cardJson.has("cardType")) card.putString("cardType", cardJson.getString("cardType"))
                            if (cardJson.has("issuerName")) card.putString("issuerName", cardJson.getString("issuerName"))
                            if (cardJson.has("isDefaultCard")) card.putString("isDefaultCard", cardJson.getString("isDefaultCard"))
                            if (cardJson.has("deviceType")) card.putString("deviceType", cardJson.getString("deviceType"))
                            if (cardJson.has("memberID")) card.putString("memberID", cardJson.getString("memberID"))
                            if (cardJson.has("countryCode")) card.putString("countryCode", cardJson.getString("countryCode"))
                            if (cardJson.has("cryptogramType")) card.putString("cryptogramType", cardJson.getString("cryptogramType"))
                            if (cardJson.has("requireCpf")) card.putString("requireCpf", cardJson.getString("requireCpf"))
                            if (cardJson.has("cpfHolderName")) card.putString("cpfHolderName", cardJson.getString("cpfHolderName"))
                            if (cardJson.has("cpfNumber")) card.putString("cpfNumber", cardJson.getString("cpfNumber"))
                            if (cardJson.has("merchantRefId")) card.putString("merchantRefId", cardJson.getString("merchantRefId"))
                            if (cardJson.has("transactionType")) card.putString("transactionType", cardJson.getString("transactionType"))
                            
                            // Campos de compatibilidade
                            if (cardJson.has("last4")) card.putString("last4", cardJson.getString("last4"))
                            if (cardJson.has("tokenizationProvider")) card.putString("tokenizationProvider", cardJson.getString("tokenizationProvider"))
                            if (cardJson.has("network")) card.putString("network", cardJson.getString("network"))
                            if (cardJson.has("displayName")) card.putString("displayName", cardJson.getString("displayName"))
                            
                            // Compatibilidade com campos antigos
                            if (cardJson.has("cardName")) card.putString("displayName", cardJson.getString("cardName"))
                            if (cardJson.has("lastFourDigits")) card.putString("last4", cardJson.getString("lastFourDigits"))
                            if (cardJson.has("cardState")) {
                                val cardState = cardJson.getInt("cardState")
                                val status = when (cardState) {
                                    1 -> "ACTIVE"
                                    0 -> "INACTIVE"
                                    2 -> "PENDING"
                                    3 -> "SUSPENDED"
                                    else -> "UNKNOWN"
                                }
                                card.putString("cardStatus", status)
                            }
                            
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
                result.putString("walletDMId", "mock_wallet_dm_12345")
                result.putString("deviceId", "mock_device_67890")
                result.putString("walletUserId", "mock_user_54321")
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
        cardType: String,
        progress: Callback,
        promise: Promise
    ) {
        Log.d(TAG, "🔍 [MOCK] addCard chamado - Provider: $tokenizationProvider, IssuerId: $issuerId, CardType: $cardType")
        
        val bodyJson = JSONObject().apply {
            put("payload", payload)
            put("issuerId", issuerId)
            put("tokenizationProvider", tokenizationProvider)
            put("cardType", cardType)
        }.toString()
        
        fetchFromLocalAPI(
            endpoint = "/samsung/add-card",
            defaultResponse = { 
                // Simular cartão adicionado com sucesso seguindo a estrutura do SerializableCard
                val card = Arguments.createMap()
                
                // Campos básicos do Card
                card.putString("cardId", "mock_card_added_001")
                card.putString("cardStatus", "ACTIVE")
                card.putString("cardBrand", tokenizationProvider)
                
                // Campos do cardInfo Bundle (Samsung Pay específicos)
                card.putString("last4FPan", "9999")
                card.putString("last4DPan", "9999")
                card.putString("app2AppPayload", "mock_payload_added_001")
                card.putString("cardType", "CREDIT")
                card.putString("issuerName", "Banco Mock")
                card.putString("isDefaultCard", "false")
                card.putString("deviceType", "phone")
                card.putString("memberID", "mock_member_added_001")
                card.putString("countryCode", "BR")
                card.putString("cryptogramType", "UCAF")
                card.putString("requireCpf", "false")
                card.putString("cpfHolderName", "Usuário Mock")
                card.putString("cpfNumber", "00000000000")
                card.putString("merchantRefId", "merchant_added_001")
                card.putString("transactionType", "PURCHASE")
                
                // Campos de compatibilidade
                card.putString("last4", "9999")
                card.putString("tokenizationProvider", tokenizationProvider)
                card.putString("network", tokenizationProvider)
                card.putString("displayName", "Cartão Adicionado")
                
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
                        
                        // Campos básicos do Card
                        card.putString("cardId", json.optString("cardId", "mock_card_added_001"))
                        card.putString("cardStatus", json.optString("cardStatus", "ACTIVE"))
                        card.putString("cardBrand", json.optString("cardBrand", tokenizationProvider))
                        
                        // Campos do cardInfo Bundle (Samsung Pay específicos)
                        card.putString("last4FPan", json.optString("last4FPan", "9999"))
                        card.putString("last4DPan", json.optString("last4DPan", "9999"))
                        card.putString("app2AppPayload", json.optString("app2AppPayload", "mock_payload_added_001"))
                        card.putString("cardType", json.optString("cardType", "CREDIT"))
                        card.putString("issuerName", json.optString("issuerName", "Banco API"))
                        card.putString("isDefaultCard", json.optString("isDefaultCard", "false"))
                        card.putString("deviceType", json.optString("deviceType", "phone"))
                        card.putString("memberID", json.optString("memberID", "mock_member_added_001"))
                        card.putString("countryCode", json.optString("countryCode", "BR"))
                        card.putString("cryptogramType", json.optString("cryptogramType", "UCAF"))
                        card.putString("requireCpf", json.optString("requireCpf", "false"))
                        card.putString("cpfHolderName", json.optString("cpfHolderName", "Usuário API"))
                        card.putString("cpfNumber", json.optString("cpfNumber", "00000000000"))
                        card.putString("merchantRefId", json.optString("merchantRefId", "merchant_added_001"))
                        card.putString("transactionType", json.optString("transactionType", "PURCHASE"))
                        
                        // Campos de compatibilidade
                        card.putString("last4", json.optString("last4", "9999"))
                        card.putString("tokenizationProvider", tokenizationProvider)
                        card.putString("network", json.optString("network", tokenizationProvider))
                        card.putString("displayName", json.optString("displayName", "Cartão Adicionado"))
                        
                        // Compatibilidade com campos antigos
                        if (json.has("cardName")) card.putString("displayName", json.getString("cardName"))
                        if (json.has("lastFourDigits")) card.putString("last4", json.getString("lastFourDigits"))
                        if (json.has("cardState")) {
                            val cardState = json.getInt("cardState")
                            val status = when (cardState) {
                                1 -> "ACTIVE"
                                0 -> "INACTIVE"
                                2 -> "PENDING"
                                3 -> "SUSPENDED"
                                else -> "UNKNOWN"
                            }
                            card.putString("cardStatus", status)
                        }
                        
                        Log.d(TAG, "✅ [MOCK] Cartão adicionado com sucesso da API")
                        promise.resolve(card)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "❌ [MOCK] Erro ao processar resposta da API: ${e.message}")
                    val card = Arguments.createMap()
                    
                    // Campos básicos do Card
                    card.putString("cardId", "mock_card_added_001")
                    card.putString("cardStatus", "ACTIVE")
                    card.putString("cardBrand", tokenizationProvider)
                    
                    // Campos do cardInfo Bundle (Samsung Pay específicos)
                    card.putString("last4FPan", "9999")
                    card.putString("last4DPan", "9999")
                    card.putString("app2AppPayload", "mock_payload_added_001")
                    card.putString("cardType", "CREDIT")
                    card.putString("issuerName", "Banco Mock")
                    card.putString("isDefaultCard", "false")
                    card.putString("deviceType", "phone")
                    card.putString("memberID", "mock_member_added_001")
                    card.putString("countryCode", "BR")
                    card.putString("cryptogramType", "UCAF")
                    card.putString("requireCpf", "false")
                    card.putString("cpfHolderName", "Usuário Mock")
                    card.putString("cpfNumber", "00000000000")
                    card.putString("merchantRefId", "merchant_added_001")
                    card.putString("transactionType", "PURCHASE")
                    
                    // Campos de compatibilidade
                    card.putString("last4", "9999")
                    card.putString("tokenizationProvider", tokenizationProvider)
                    card.putString("network", tokenizationProvider)
                    card.putString("displayName", "Cartão Adicionado")
                    
                    promise.resolve(card)
                }
            },
            onError = { error ->
                // Sempre retorna sucesso no fallback de erro
                val card = Arguments.createMap()
                
                // Campos básicos do Card
                card.putString("cardId", "mock_card_added_001")
                card.putString("cardStatus", "ACTIVE")
                card.putString("cardBrand", tokenizationProvider)
                
                // Campos do cardInfo Bundle (Samsung Pay específicos)
                card.putString("last4FPan", "9999")
                card.putString("last4DPan", "9999")
                card.putString("app2AppPayload", "mock_payload_added_001")
                card.putString("cardType", "CREDIT")
                card.putString("issuerName", "Banco Mock")
                card.putString("isDefaultCard", "false")
                card.putString("deviceType", "phone")
                card.putString("memberID", "mock_member_added_001")
                card.putString("countryCode", "BR")
                card.putString("cryptogramType", "UCAF")
                card.putString("requireCpf", "false")
                card.putString("cpfHolderName", "Usuário Mock")
                card.putString("cpfNumber", "00000000000")
                card.putString("merchantRefId", "merchant_added_001")
                card.putString("transactionType", "PURCHASE")
                
                // Campos de compatibilidade
                card.putString("last4", "9999")
                card.putString("tokenizationProvider", tokenizationProvider)
                card.putString("network", tokenizationProvider)
                card.putString("displayName", "Cartão Adicionado")
                
                Log.d(TAG, "✅ [MOCK] Cartão adicionado com sucesso (fallback)")
                promise.resolve(card)
            },
            method = "POST",
            body = bodyJson
        )
    }

    override fun checkWalletAvailability(promise: Promise) {
        Log.d(TAG, "🔍 [MOCK] checkWalletAvailability chamado")
        fetchFromLocalAPI(
            endpoint = "/samsung/availability",
            defaultResponse = { true }, // Por padrão, Samsung Pay está disponível no mock
            onSuccess = { json ->
                try {
                    val isAvailable = json.optBoolean("available", true)
                    Log.d(TAG, "✅ [MOCK] Disponibilidade: $isAvailable")
                    promise.resolve(isAvailable)
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ [MOCK] Erro ao processar disponibilidade, usando fallback: ${e.message}")
                    promise.resolve(true)
                }
            },
            onError = { _ ->
                promise.resolve(true) // Por padrão, disponível no mock
            }
        )
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
            val cardType = cardData.getString("cardType") ?: "CREDIT" // Default para CREDIT se não especificado
            
            addCard(payload, issuerId, tokenizationProvider, cardType, object : Callback {
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
        
        // SDK Info
        constants["SDK_NAME"] = "SamsungWalletMock"
        
        // Samsung Pay Status Codes (valores reais do SpaySdk)
        constants["SPAY_READY"] = 2
        constants["SPAY_NOT_READY"] = 1
        constants["SPAY_NOT_SUPPORTED"] = 0
        constants["SPAY_NOT_ALLOWED_TEMPORALLY"] = 3
        constants["SPAY_HAS_TRANSIT_CARD"] = 10
        constants["SPAY_HAS_NO_TRANSIT_CARD"] = 11
        
        // Samsung Card Types (da classe Card)
        constants["CARD_TYPE"] = "CARD_TYPE"
        constants["CARD_TYPE_CREDIT_DEBIT"] = "PAYMENT"
        constants["CARD_TYPE_GIFT"] = "GIFT"
        constants["CARD_TYPE_LOYALTY"] = "LOYALTY"
        constants["CARD_TYPE_CREDIT"] = "CREDIT"
        constants["CARD_TYPE_DEBIT"] = "DEBIT"
        constants["CARD_TYPE_TRANSIT"] = "TRANSIT"
        constants["CARD_TYPE_VACCINE_PASS"] = "VACCINE_PASS"
        
        // Samsung Card States (da classe Card)
        constants["ACTIVE"] = "ACTIVE"
        constants["DISPOSED"] = "DISPOSED"
        constants["EXPIRED"] = "EXPIRED"
        constants["PENDING_ENROLLED"] = "ENROLLED"
        constants["PENDING_PROVISION"] = "PENDING_PROVISION"
        constants["SUSPENDED"] = "SUSPENDED"
        constants["PENDING_ACTIVATION"] = "PENDING_ACTIVATION"
        
        // Samsung Tokenization Providers (baseado na classe AddCardInfo)
        constants["PROVIDER_VISA"] = "VI"
        constants["PROVIDER_MASTERCARD"] = "MC"
        constants["PROVIDER_AMEX"] = "AX"
        constants["PROVIDER_DISCOVER"] = "DS"
        constants["PROVIDER_PLCC"] = "PL"
        constants["PROVIDER_GIFT"] = "GI"
        constants["PROVIDER_LOYALTY"] = "LO"
        constants["PROVIDER_PAYPAL"] = "PP"
        constants["PROVIDER_GEMALTO"] = "GT"
        constants["PROVIDER_NAPAS"] = "NP"
        constants["PROVIDER_MIR"] = "MI"
        constants["PROVIDER_PAGOBANCOMAT"] = "PB"
        constants["PROVIDER_VACCINE_PASS"] = "VaccinePass"
        constants["PROVIDER_MADA"] = "MADA"
        constants["PROVIDER_ELO"] = "ELO"
        
        // Samsung Error Codes (todos do ErrorCode.kt)
        constants["ERROR_NONE"] = 0
        constants["ERROR_SPAY_INTERNAL"] = -1
        constants["ERROR_INVALID_INPUT"] = -2
        constants["ERROR_NOT_SUPPORTED"] = -3
        constants["ERROR_NOT_FOUND"] = -4
        constants["ERROR_ALREADY_DONE"] = -5
        constants["ERROR_NOT_ALLOWED"] = -6
        constants["ERROR_USER_CANCELED"] = -7
        constants["ERROR_PARTNER_SDK_API_LEVEL"] = -10
        constants["ERROR_PARTNER_SERVICE_TYPE"] = -11
        constants["ERROR_INVALID_PARAMETER"] = -12
        constants["ERROR_NO_NETWORK"] = -21
        constants["ERROR_SERVER_NO_RESPONSE"] = -22
        constants["ERROR_PARTNER_INFO_INVALID"] = -99
        constants["ERROR_INITIATION_FAIL"] = -103
        constants["ERROR_REGISTRATION_FAIL"] = -104
        constants["ERROR_DUPLICATED_SDK_API_CALLED"] = -105
        constants["ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION"] = -300
        constants["ERROR_SERVICE_ID_INVALID"] = -301
        constants["ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION"] = -302
        constants["ERROR_PARTNER_APP_SIGNATURE_MISMATCH"] = -303
        constants["ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED"] = -304
        constants["ERROR_PARTNER_APP_BLOCKED"] = -305
        constants["ERROR_USER_NOT_REGISTERED_FOR_DEBUG"] = -306
        constants["ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE"] = -307
        constants["ERROR_PARTNER_NOT_APPROVED"] = -308
        constants["ERROR_UNAUTHORIZED_REQUEST_TYPE"] = -309
        constants["ERROR_EXPIRED_OR_INVALID_DEBUG_KEY"] = -310
        constants["ERROR_SERVER_INTERNAL"] = -311
        constants["ERROR_DEVICE_NOT_SAMSUNG"] = -350
        constants["ERROR_SPAY_PKG_NOT_FOUND"] = -351
        constants["ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE"] = -352
        constants["ERROR_DEVICE_INTEGRITY_CHECK_FAIL"] = -353
        constants["ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL"] = -360
        constants["ERROR_ANDROID_PLATFORM_CHECK_FAIL"] = -361
        constants["ERROR_MISSING_INFORMATION"] = -354
        constants["ERROR_SPAY_SETUP_NOT_COMPLETED"] = -356
        constants["ERROR_SPAY_APP_NEED_TO_UPDATE"] = -357
        constants["ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED"] = -358
        constants["ERROR_UNABLE_TO_VERIFY_CALLER"] = -359
        constants["ERROR_SPAY_FMM_LOCK"] = -604
        constants["ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY"] = -605
        
        Log.d(TAG, "✅ [MOCK] Constantes obtidas (baseadas na classe Card)")
        return constants
    }
}