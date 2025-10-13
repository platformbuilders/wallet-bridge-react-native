package com.builders.wallet

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.builders.wallet.googletapandpay.GoogleWalletModule
import com.builders.wallet.googletapandpay.GoogleWalletImplementation
import com.builders.wallet.samsungpay.SamsungWalletModule
import com.builders.wallet.samsungpay.SamsungWalletImplementation

/**
 * Processador centralizado de intents para wallets
 * Identifica se a intent é da Samsung ou Google e encaminha para o módulo correto
 */
object WalletIntentProcessor {
    
    private const val TAG = "WalletIntentProcessor"
    
    /**
     * Processa intent com identificação centralizada de package
     * Identifica se é Samsung ou Google e encaminha para o módulo correto
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
            Log.d(TAG, "🔍 [CENTRAL] Nenhum dado EXTRA_TEXT encontrado - ignorando intent")
            return
        }
        
        // Identificar tipo de package e encaminhar para módulo correto
        // Usar as funções isValidCallingPackage das implementações
        when {
            SamsungWalletImplementation.isValidCallingPackage(activity) -> {
                Log.d(TAG, "✅ [CENTRAL] Package identificado como Samsung - encaminhando para SamsungWalletModule")
                SamsungWalletModule.processIntent(activity, intent)
            }
            GoogleWalletImplementation.isValidCallingPackage(activity) -> {
                Log.d(TAG, "✅ [CENTRAL] Package identificado como Google - encaminhando para GoogleWalletModule")
                GoogleWalletModule.processIntent(activity, intent)
            }
            else -> {
                Log.d(TAG, "🔍 [CENTRAL] Package não identificado como Samsung ou Google - ignorando intent")
                // Não fazer nada - apenas ignorar a intent
            }
        }
    }
}
