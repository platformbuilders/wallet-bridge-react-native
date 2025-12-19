package com.builders.wallet

import android.app.Activity
import android.content.Intent
import com.builders.wallet.WalletLogger
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
     * Se a intent tem extras e foi chamada via startActivityForResult, finaliza e recria a Activity
     * 
     * @param activity Activity atual
     * @param intent Intent recebida
     */
    @JvmStatic
    fun processIntent(activity: Activity, intent: Intent) {
        val intentAction = intent.action
        val packageName = intent.`package`
        val callingPackage = activity.callingPackage
        
        WalletLogger.d(TAG, "🔍 [CENTRAL] Processando intent - Action: $intentAction, Package: $packageName, CallingPackage: $callingPackage")
        
        // Verificar se há extras na intent (usando safe call operator, mais idiomático em Kotlin)
        val hasExtras = intent.extras?.isEmpty() == false
        val wasCalledForResult = activity.getCallingActivity() != null || callingPackage != null
        
        // Se tem extras e foi chamada via startActivityForResult, finalizar e recriar
        if (hasExtras && wasCalledForResult) {
            WalletLogger.d(TAG, "🔄 [CENTRAL] Intent com extras via startActivityForResult - processando e recriando Activity")
            
            // Processar intent antes de finalizar
            processIntentInternal(activity, intent)
            
            // Finalizar a instância atual
            activity.finish()
            
            // Abrir uma nova instância do app com os dados da intent
            activity.packageManager.getLaunchIntentForPackage(activity.packageName)?.apply {
                intent.extras?.let { putExtras(it) }
                action = intent.action
                data = intent.data
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }?.let { activity.startActivity(it) }
            
            return
        }
        
        // Processar intent normalmente (sem recriar)
        processIntentInternal(activity, intent)
    }
    
    /**
     * Processa a intent internamente, identificando o tipo de wallet e encaminhando para o módulo correto
     */
    private fun processIntentInternal(activity: Activity, intent: Intent) {
        // Verificar se há dados EXTRA_TEXT (necessário para processamento)
        val extraText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (extraText.isNullOrEmpty()) {
            WalletLogger.d(TAG, "🔍 [CENTRAL] Nenhum dado EXTRA_TEXT encontrado - armazenando para processamento posterior")
            // Armazenar para processamento posterior quando React Native estiver pronto
            SamsungWalletModule.setNoIntentReceivedFlag()
            GoogleWalletModule.setNoIntentReceivedFlag()
            return
        }
        
        // Verificar configurações de mock
        val useGoogleMock = try {
            BuildConfig.GOOGLE_WALLET_USE_MOCK
        } catch (e: Exception) {
            WalletLogger.w(TAG, "🔧 [CENTRAL] GOOGLE_WALLET_USE_MOCK não definido, usando padrão: false")
            false
        }
        
        val useSamsungMock = try {
            BuildConfig.SAMSUNG_WALLET_USE_MOCK
        } catch (e: Exception) {
            WalletLogger.w(TAG, "🔧 [CENTRAL] SAMSUNG_WALLET_USE_MOCK não definido, usando padrão: false")
            false
        }
        
        WalletLogger.d(TAG, "🔧 [CENTRAL] Configurações de mock - Google: $useGoogleMock, Samsung: $useSamsungMock")
        
        // Identificar tipo de package e encaminhar para módulo correto
        // Usar as funções isValidCallingPackage das implementações (mock ou real)
        when {
            // Verificar Samsung (mock ou real)
            (if (useSamsungMock) SamsungWalletMock.isValidCallingPackage(activity) else SamsungWalletImplementation.isValidCallingPackage(activity)) -> {
                WalletLogger.d(TAG, "✅ [CENTRAL] Package identificado como Samsung - encaminhando para SamsungWalletModule (${if (useSamsungMock) "MOCK" else "REAL"})")
                SamsungWalletModule.processIntent(activity, intent)
            }
            // Verificar Google (mock ou real)
            (if (useGoogleMock) GoogleWalletMock.isValidCallingPackage(activity) else GoogleWalletImplementation.isValidCallingPackage(activity)) -> {
                WalletLogger.d(TAG, "✅ [CENTRAL] Package identificado como Google - encaminhando para GoogleWalletModule (${if (useGoogleMock) "MOCK" else "REAL"})")
                GoogleWalletModule.processIntent(activity, intent)
            }
            else -> {
                WalletLogger.d(TAG, "🔍 [CENTRAL] Package não identificado como Samsung ou Google - armazenando para processamento posterior")
                // Armazenar para processamento posterior quando React Native estiver pronto
                SamsungWalletModule.setNoIntentReceivedFlag()
                GoogleWalletModule.setNoIntentReceivedFlag()
            }
        }
    }
}
