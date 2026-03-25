package com.builders.wallet

import android.content.pm.ApplicationInfo
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.lang.reflect.Method

/**
 * Logger customizado que envia logs para React Native além do Log padrão do Android.
 * Usa reflexão para chamar o Log do Android nativo.
 *
 * Os logs são totalmente suprimidos em versões de produção (FLAG_DEBUGGABLE não definido).
 * A flag de debug é resolvida uma vez na inicialização e armazenada em cache durante toda a vida do processo.
 */
object WalletLogger {

    private var reactContext: ReactApplicationContext? = null
    private var logListenerActive: Boolean = false

    /**
     * Flag de depuração em cache. `null` = ainda não inicializado (padrão seguro → suprimido).
        * Escrita uma vez na primeira chamada bem-sucedida de [initialize].
     */
    @Volatile
    private var enableLogs: Boolean? = null

    // Cache de métodos do Log por reflexão para melhor performance
    private val logMethods: Map<String, Method> by lazy {
        try {
            val logClass = Log::class.java
            val methods = mutableMapOf<String, Method>()

            // Métodos: d, i, w, e, v
            methods["d"] = logClass.getMethod("d", String::class.java, String::class.java)
            methods["i"] = logClass.getMethod("i", String::class.java, String::class.java)
            methods["w"] = logClass.getMethod("w", String::class.java, String::class.java)
            methods["e"] = logClass.getMethod("e", String::class.java, String::class.java)
            methods["v"] = logClass.getMethod("v", String::class.java, String::class.java)

            // Métodos com Throwable: w, e
            methods["w_throwable"] = logClass.getMethod("w", String::class.java, String::class.java, Throwable::class.java)
            methods["e_throwable"] = logClass.getMethod("e", String::class.java, String::class.java, Throwable::class.java)

            methods
        } catch (e: Exception) {
            // Se falhar ao obter métodos, tentar usar Log direto como fallback
            try {
                Log.e("WalletLogger", "Erro ao inicializar métodos de log por reflexão: ${e.message}")
            } catch (_: Exception) {
                // Ignorar se nem isso funcionar
            }
            emptyMap()
        }
    }

    /**
     * Inicializa o logger com o contexto do React Native.
     * A flag de debug é resolvida e armazenada em cache na primeira chamada.
     * Chamadas subsequentes são no-ops em relação a [enableLogs].
     * Um log de diagnóstico é sempre emitido para o logcat.
     */
    fun initialize(context: ReactApplicationContext) {
        reactContext = context
        if (enableLogs == null) {
            enableLogs = resolveDebugFlag(context)
        }
        val mode = when {
            enableLogs == true -> "debug"
            else -> "suppressed"
        }
        Log.i("WalletLogger", "logging active — mode=$mode")
    }

    /**
     * Resolve a flag de depuração do aplicativo host a partir de [ApplicationInfo.FLAG_DEBUGGABLE].
     * Retorna `false` (padrão seguro) em caso de qualquer erro.
     */
    private fun resolveDebugFlag(context: ReactApplicationContext): Boolean {
        return try {
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Retorna `true` quando a saída de log é permitida.
        * A guarda é derivada apenas da flag de depuração do aplicativo host em cache.
     * Retorna `false` quando ainda não inicializado.
     */
    private fun isLoggingAllowed(): Boolean = enableLogs == true

    /**
     * Ativa o listener de logs
     */
    fun setLogListener(active: Boolean) {
        logListenerActive = active
    }

    /**
     * Envia log para React Native
     */
    private fun sendLogToReactNative(level: String, tag: String, message: String, throwable: Throwable? = null) {
        if (!logListenerActive || reactContext == null) {
            return
        }

        try {
            val eventData = Arguments.createMap()
            eventData.putString("level", level)
            eventData.putString("tag", tag)
            eventData.putString("message", message)

            if (throwable != null) {
                eventData.putString("error", throwable.message ?: "Unknown error")
                eventData.putString("stackTrace", throwable.stackTraceToString())
            }

            reactContext!!
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("WalletLog", eventData)
        } catch (e: Exception) {
            // Se falhar ao enviar evento, ignorar silenciosamente
            // (não queremos criar loop de erros)
        }
    }

    /**
     * Chama o método do Log usando reflexão
     */
    private fun callLogMethod(methodName: String, tag: String, message: String, throwable: Throwable? = null) {
        try {
            val methodKey = if (throwable != null && (methodName == "w" || methodName == "e")) {
                "${methodName}_throwable"
            } else {
                methodName
            }

            val method = logMethods[methodKey]
            if (method != null) {
                if (throwable != null) {
                    method.invoke(null, tag, message, throwable)
                } else {
                    method.invoke(null, tag, message)
                }
            } else {
                // Fallback para chamada direta se reflexão falhar
                when (methodName) {
                    "d" -> Log.d(tag, message)
                    "i" -> Log.i(tag, message)
                    "w" -> if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
                    "e" -> if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
                    "v" -> Log.v(tag, message)
                }
            }
        } catch (e: Exception) {
            // Se falhar, tentar usar Log direto
            try {
                when (methodName) {
                    "d" -> Log.d(tag, message)
                    "i" -> Log.i(tag, message)
                    "w" -> if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
                    "e" -> if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
                    "v" -> Log.v(tag, message)
                }
            } catch (_: Exception) {
                // Se tudo falhar, ignorar
            }
        }
    }

    /**
     * Log DEBUG
     */
    fun d(tag: String, message: String) {
        if (!isLoggingAllowed()) return
        callLogMethod("d", tag, message)
        sendLogToReactNative("DEBUG", tag, message)
    }

    /**
     * Log INFO
     */
    fun i(tag: String, message: String) {
        if (!isLoggingAllowed()) return
        callLogMethod("i", tag, message)
        sendLogToReactNative("INFO", tag, message)
    }

    /**
     * Log WARN
     */
    fun w(tag: String, message: String) {
        if (!isLoggingAllowed()) return
        callLogMethod("w", tag, message)
        sendLogToReactNative("WARN", tag, message)
    }

    /**
     * Log WARN com Throwable
     */
    fun w(tag: String, message: String, throwable: Throwable) {
        if (!isLoggingAllowed()) return
        callLogMethod("w", tag, message, throwable)
        sendLogToReactNative("WARN", tag, message, throwable)
    }

    /**
     * Log ERROR
     */
    fun e(tag: String, message: String) {
        if (!isLoggingAllowed()) return
        callLogMethod("e", tag, message)
        sendLogToReactNative("ERROR", tag, message)
    }

    /**
     * Log ERROR com Throwable
     */
    fun e(tag: String, message: String, throwable: Throwable) {
        if (!isLoggingAllowed()) return
        callLogMethod("e", tag, message, throwable)
        sendLogToReactNative("ERROR", tag, message, throwable)
    }

    /**
     * Log VERBOSE
     */
    fun v(tag: String, message: String) {
        if (!isLoggingAllowed()) return
        callLogMethod("v", tag, message)
        sendLogToReactNative("VERBOSE", tag, message)
    }
}

