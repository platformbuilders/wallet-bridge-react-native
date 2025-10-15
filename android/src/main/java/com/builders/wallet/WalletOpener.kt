package com.builders.wallet

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast

/**
 * Classe nativa para gerenciar a abertura de apps de carteira
 * Tenta abrir o app diretamente, se não conseguir redireciona para Play Store ou navegador
 */
class WalletOpener(private val context: Context) {

    companion object {
        private const val TAG = "WalletOpener"
    }

    /**
     * Abre um app de carteira ou redireciona para Play Store/navegador
     * @param packageName Nome do pacote do app (ex: com.samsung.android.spay)
     * @param appName Nome do app para exibição (ex: "Samsung Pay")
     * @param playStoreUrl URL da Play Store (ex: market://details?id=com.samsung.android.spay)
     * @param webUrl URL da web como fallback (ex: https://play.google.com/store/apps/details?id=...)
     * @return true se conseguiu abrir o app ou redirecionar, false se falhou
     */
    fun openWallet(
        packageName: String,
        appName: String,
        playStoreUrl: String,
        webUrl: String
    ): Boolean {
        log("🔍 Abrindo $appName ($packageName)")
        
        try {
            val packageManager = context.packageManager
            val isInstalled = try {
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
            
            return if (isInstalled) {
                log("✅ $appName instalado - abrindo app")
                openAppDirectly(packageName, appName)
            } else {
                log("⚠️ $appName não instalado - redirecionando para Play Store")
                openPlayStore(playStoreUrl, webUrl, appName)
            }
            
        } catch (e: Exception) {
            log("❌ Erro: ${e.message}")
            showToast("Erro: ${e.message}")
            return false
        }
    }

    /**
     * Tenta abrir o app diretamente com múltiplas estratégias
     */
    private fun openAppDirectly(packageName: String, appName: String): Boolean {
        val packageManager = context.packageManager
        
        // Tentativa 1: getLaunchIntentForPackage
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) {
            try {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launch)
                log("✅ $appName aberto (método 1)")
                showToast("$appName aberto")
                return true
            } catch (e: Exception) {
                log("⚠️ Método 1 falhou: ${e.message}")
            }
        }
        
        // Tentativa 2: Intent explícito
        try {
            val explicitIntent = Intent(Intent.ACTION_MAIN)
            explicitIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            explicitIntent.setPackage(packageName)
            explicitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            if (explicitIntent.resolveActivity(packageManager) != null) {
                context.startActivity(explicitIntent)
                log("✅ $appName aberto (método 2)")
                showToast("$appName aberto")
                return true
            }
        } catch (e: Exception) {
            log("⚠️ Método 2 falhou: ${e.message}")
        }
        
        // Tentativa 3: Intent genérico
        try {
            val genericIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (genericIntent != null) {
                genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(genericIntent)
                log("✅ $appName aberto (método 3)")
                showToast("$appName aberto")
                return true
            }
        } catch (e: Exception) {
            log("⚠️ Método 3 falhou: ${e.message}")
        }
        
        log("❌ Falha ao abrir $appName")
        showToast("Erro ao abrir $appName")
        return false
    }

    /**
     * Tenta abrir a Play Store
     */
    private fun openPlayStore(playStoreUrl: String, webUrl: String, appName: String): Boolean {
        try {
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            if (playStoreIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(playStoreIntent)
                log("✅ Play Store aberta para $appName")
                showToast("Abrindo Play Store para download...")
                return true
            } else {
                log("⚠️ Play Store não disponível - tentando navegador")
                return openBrowser(webUrl, appName)
            }
            
        } catch (e: Exception) {
            log("❌ Erro na Play Store: ${e.message}")
            return openBrowser(webUrl, appName)
        }
    }

    /**
     * Abre no navegador como fallback
     */
    private fun openBrowser(webUrl: String, appName: String): Boolean {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            if (browserIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(browserIntent)
                log("✅ Navegador aberto para $appName")
                showToast("Abrindo navegador para download...")
                return true
            } else {
                log("❌ Nenhum navegador disponível")
                showToast("Erro: Nenhum navegador disponível")
                return false
            }
            
        } catch (e: Exception) {
            log("❌ Erro no navegador: ${e.message}")
            showToast("Erro ao abrir $appName")
            return false
        }
    }

    /**
     * Verifica se um app está instalado
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Log helper
     */
    private fun log(message: String) {
        Log.d(TAG, message)
    }

    /**
     * Toast helper
     */
    private fun showToast(message: String) {
        if (context is android.app.Activity) {
            context.runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
