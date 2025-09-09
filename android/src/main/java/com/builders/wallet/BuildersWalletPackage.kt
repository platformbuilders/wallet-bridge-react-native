package com.builders.wallet

import com.builders.wallet.googletapandpay.GoogleWalletModule
import com.builders.wallet.googletapandpay.GoogleWalletPackage
import com.builders.wallet.samsungpay.SamsungWalletModule
import com.builders.wallet.samsungpay.SamsungWalletPackage
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class BuildersWalletPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf(
      BuildersWalletModule(reactContext),
      GoogleWalletModule(reactContext),
      SamsungWalletModule(reactContext)
    )
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return emptyList()
  }
}
