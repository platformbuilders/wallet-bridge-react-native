package com.builders.wallet

import android.util.Log
import com.builders.wallet.adapters.GoogleTapAndPayAdapter
import com.builders.wallet.adapters.SamsungPayAdapter
import com.facebook.react.bridge.ReactApplicationContext

object WalletModuleFactory {
    private const val TAG = "WalletModuleFactory"
    
    fun createWalletModule(context: ReactApplicationContext, moduleType: String = "auto"): WalletModuleInterface {
        return try {
            when (moduleType) {
                "google" -> {
                    if (isGooglePaySDKAvailable()) {
                        Log.i(TAG, "Criando GoogleTapAndPayAdapter")
                        GoogleTapAndPayAdapter(context)
                    } else {
                        Log.w(TAG, "Google Pay SDK não disponível, usando StubWalletModule")
                        StubWalletModule()
                    }
                }
                "samsung" -> {
                    if (isSamsungPaySDKAvailable()) {
                        Log.i(TAG, "Criando SamsungPayAdapter")
                        SamsungPayAdapter(context)
                    } else {
                        Log.w(TAG, "Samsung Pay SDK não disponível, usando StubWalletModule")
                        StubWalletModule()
                    }
                }
                "auto" -> {
                    // Tenta Google Pay primeiro, depois Samsung Pay
                    when {
                        isGooglePaySDKAvailable() -> {
                            Log.i(TAG, "Google Pay SDK disponível, criando GoogleTapAndPayAdapter")
                            GoogleTapAndPayAdapter(context)
                        }
                        isSamsungPaySDKAvailable() -> {
                            Log.i(TAG, "Samsung Pay SDK disponível, criando SamsungPayAdapter")
                            SamsungPayAdapter(context)
                        }
                        else -> {
                            Log.i(TAG, "Nenhum SDK disponível, usando StubWalletModule")
                            StubWalletModule()
                        }
                    }
                }
                else -> {
                    Log.w(TAG, "Tipo de módulo desconhecido: $moduleType, usando StubWalletModule")
                    StubWalletModule()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao criar módulo de wallet: ${e.message}")
            StubWalletModule()
        }
    }
    
    private fun isGooglePaySDKAvailable(): Boolean {
        return try {
            Class.forName("com.google.android.gms.tapandpay.TapAndPay")
            Class.forName("com.google.android.gms.tapandpay.TapAndPayClient")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    
    private fun isSamsungPaySDKAvailable(): Boolean {
        return try {
            Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk")
            Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk\$Companion")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    
    fun getAvailableModules(): List<String> {
        val modules = mutableListOf<String>()
        
        if (isGooglePaySDKAvailable()) {
            modules.add("GooglePay")
        }
        
        if (isSamsungPaySDKAvailable()) {
            modules.add("SamsungPay")
        }
        
        return modules
    }
    
    fun getAvailableModuleNames(): List<String> {
        return getAvailableModules().map { module ->
            when (module) {
                "GooglePay" -> "GoogleTapAndPay"
                "SamsungPay" -> "SamsungPay"
                else -> module
            }
        }
    }
}
