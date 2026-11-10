package com.builders.wallet.googletapandpay

import com.builders.wallet.turboModuleInfo
import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfoProvider

/** Registra só o GoogleWallet como TurboModule. */
class GoogleWalletPackage : BaseReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? =
    if (name == GoogleWalletModule.NAME) GoogleWalletModule(reactContext) else null

  override fun getReactModuleInfoProvider() = ReactModuleInfoProvider {
    mapOf(GoogleWalletModule.NAME to turboModuleInfo(GoogleWalletModule.NAME, GoogleWalletModule::class.java.name))
  }
}
