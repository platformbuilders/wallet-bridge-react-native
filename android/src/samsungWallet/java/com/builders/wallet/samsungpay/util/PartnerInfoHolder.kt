package com.builders.wallet.samsungpay.util

import android.os.Bundle
import com.builders.wallet.WalletLogger
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk

/**
 * PartnerInfoHolder LIMPO - USA DIRETAMENTE O SDK (SEM REFLEXÃO)
 * 
 * Esta versão só é compilada quando SAMSUNG_WALLET_ENABLED = true
 */
class PartnerInfoHolder private constructor() {
    var partnerInfo: PartnerInfo? = null

    companion object {
        @Volatile
        private var INSTANCE: PartnerInfoHolder? = null

        fun getInstance(serviceId: String): PartnerInfoHolder {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PartnerInfoHolder().also { instance ->
                    instance.partnerInfo = createPartnerInfo(serviceId)
                }
            }
        }

        private fun createPartnerInfo(serviceId: String): PartnerInfo? {
            return try {
                // Código LIMPO - usa diretamente o SDK!
                val bundle = Bundle()
                bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.INAPP_PAYMENT.toString())
                bundle.putString(SamsungPay.EXTRA_ISSUER_NAME, "Builders Wallet")
                
                WalletLogger.d("PartnerInfoHolder", "Bundle configurado com PARTNER_SERVICE_TYPE: INAPP_PAYMENT, EXTRA_ISSUER_NAME: Builders Wallet")
                
                // Criar PartnerInfo diretamente!
                PartnerInfo(serviceId, bundle)
            } catch (t: Throwable) {
                WalletLogger.e("PartnerInfoHolder", "Falha ao criar PartnerInfo: ${t.message}", t)
                // Fallback: tentar criar apenas com serviceId
                try {
                    PartnerInfo(serviceId)
                } catch (fallbackError: Throwable) {
                    WalletLogger.e("PartnerInfoHolder", "Falha no fallback: ${fallbackError.message}", fallbackError)
                    null
                }
            }
        }
    }
}

