package com.builders.wallet.samsungpay

import com.builders.wallet.turboModuleInfo
import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfoProvider

/** Registra só o SamsungWallet como TurboModule. */
class SamsungWalletPackage : BaseReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? =
    if (name == SamsungWalletModule.NAME) SamsungWalletModule(reactContext) else null

  override fun getReactModuleInfoProvider() = ReactModuleInfoProvider {
    mapOf(SamsungWalletModule.NAME to turboModuleInfo(SamsungWalletModule.NAME, SamsungWalletModule::class.java.name))
  }
}
