package com.builders.wallet

import android.util.Log
import com.builders.wallet.googletapandpay.GoogleTapAndPayModule
import com.builders.wallet.samsungpay.SamsungPayModule
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class BuildersWalletPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    val modules = mutableListOf<NativeModule>()
    
    try {
      // Sempre adiciona o módulo principal
      modules.add(BuildersWalletModule(reactContext))
      
      // Cria módulos específicos baseados nos SDKs disponíveis
      val availableModules = WalletModuleFactory.getAvailableModules()
      Log.i("BuildersWalletPackage", "SDKs disponíveis: $availableModules")
      
      if (availableModules.contains("GooglePay")) {
        modules.add(GoogleTapAndPayModule(reactContext))
      }
      
      if (availableModules.contains("SamsungPay")) {
        modules.add(SamsungPayModule(reactContext))
      }
      
    } catch (e: Exception) {
      Log.e("BuildersWalletPackage", "Erro ao criar módulos: ${e.message}")
      // Em caso de erro, adiciona pelo menos o módulo principal
      if (modules.isEmpty()) {
        modules.add(BuildersWalletModule(reactContext))
      }
    }
    
    return modules
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return emptyList()
  }
}
