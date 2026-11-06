package com.builders.wallet

import com.builders.wallet.googletapandpay.GoogleWalletModule
import com.builders.wallet.samsungpay.SamsungWalletModule
import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider

/** Registra GoogleWallet e SamsungWallet como TurboModules. */
class BuildersWalletPackage : BaseReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? =
    when (name) {
      GoogleWalletModule.NAME -> GoogleWalletModule(reactContext)
      SamsungWalletModule.NAME -> SamsungWalletModule(reactContext)
      else -> null
    }

  override fun getReactModuleInfoProvider() = ReactModuleInfoProvider {
    mapOf(
      GoogleWalletModule.NAME to turboModuleInfo(GoogleWalletModule.NAME, GoogleWalletModule::class.java.name),
      SamsungWalletModule.NAME to turboModuleInfo(SamsungWalletModule.NAME, SamsungWalletModule::class.java.name),
    )
  }
}

internal fun turboModuleInfo(name: String, className: String) = ReactModuleInfo(
  name = name,
  className = className,
  canOverrideExistingModule = false,
  needsEagerInit = false,
  isCxxModule = false,
  isTurboModule = true,
)
