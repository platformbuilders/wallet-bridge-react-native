package com.builders.wallet

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.builders.wallet.BuildConfig
import com.builders.wallet.googletapandpay.GoogleWalletModule
import com.builders.wallet.googletapandpay.GoogleWalletImplementation
import com.builders.wallet.googletapandpay.GoogleWalletMock
import com.builders.wallet.samsungpay.SamsungWalletModule
import com.builders.wallet.samsungpay.SamsungWalletImplementation
import com.builders.wallet.samsungpay.SamsungWalletMock

/**
 * Processador centralizado de intents para wallets
 * Identifica se a intent é da Samsung ou Google e encaminha para o módulo correto
 */
object WalletIntentProcessor {
    
    private const val TAG = "WalletIntentProcessor"
    
    /**
     * Processa intent com identificação centralizada de package
     * Identifica se é Samsung ou Google e encaminha para o módulo correto
     * Considera configuração de mock para determinar qual implementação usar
     * 
     * @param activity Activity atual
     * @param intent Intent recebida
     */
    @JvmStatic
    fun processIntent(activity: Activity, intent: Intent) {
        val action = intent.action
        val packageName = intent.`package`
        val callingPackage = activity.callingPackage
        
        Log.d(TAG, "🔍 [CENTRAL] Processando intent - Action: $action, Package: $packageName, CallingPackage: $callingPackage")
        
        // Verificar se há dados EXTRA_TEXT (necessário para processamento)
        val extraText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (extraText.isNullOrEmpty()) {
            Log.d(TAG, "🔍 [CENTRAL] Nenhum dado EXTRA_TEXT encontrado - armazenando para processamento posterior")
            // Armazenar para processamento posterior quando React Native estiver pronto
            SamsungWalletModule.setNoIntentReceivedFlag()
            GoogleWalletModule.setNoIntentReceivedFlag()
            return
        }
        
        // Verificar configurações de mock
        val useGoogleMock = try {
            BuildConfig.GOOGLE_WALLET_USE_MOCK
        } catch (e: Exception) {
            Log.w(TAG, "🔧 [CENTRAL] GOOGLE_WALLET_USE_MOCK não definido, usando padrão: false")
            false
        }
        
        val useSamsungMock = try {
            BuildConfig.SAMSUNG_WALLET_USE_MOCK
        } catch (e: Exception) {
            Log.w(TAG, "🔧 [CENTRAL] SAMSUNG_WALLET_USE_MOCK não definido, usando padrão: false")
            false
        }
        
        Log.d(TAG, "🔧 [CENTRAL] Configurações de mock - Google: $useGoogleMock, Samsung: $useSamsungMock")
        
        // Identificar tipo de package e encaminhar para módulo correto
        // Usar as funções isValidCallingPackage das implementações (mock ou real)
        when {
            // Verificar Samsung (mock ou real)
            (if (useSamsungMock) SamsungWalletMock.isValidCallingPackage(activity) else SamsungWalletImplementation.isValidCallingPackage(activity)) -> {
                Log.d(TAG, "✅ [CENTRAL] Package identificado como Samsung - encaminhando para SamsungWalletModule (${if (useSamsungMock) "MOCK" else "REAL"})")
                SamsungWalletModule.processIntent(activity, intent)
            }
            // Verificar Google (mock ou real)
            (if (useGoogleMock) GoogleWalletMock.isValidCallingPackage(activity) else GoogleWalletImplementation.isValidCallingPackage(activity)) -> {
                Log.d(TAG, "✅ [CENTRAL] Package identificado como Google - encaminhando para GoogleWalletModule (${if (useGoogleMock) "MOCK" else "REAL"})")
                GoogleWalletModule.processIntent(activity, intent)
            }
            else -> {
                Log.d(TAG, "🔍 [CENTRAL] Package não identificado como Samsung ou Google - armazenando para processamento posterior")
                // Armazenar para processamento posterior quando React Native estiver pronto
                SamsungWalletModule.setNoIntentReceivedFlag()
                GoogleWalletModule.setNoIntentReceivedFlag()
            }
        }
    }
}
