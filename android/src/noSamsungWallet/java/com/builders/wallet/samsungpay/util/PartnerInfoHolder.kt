package com.builders.wallet.samsungpay.util

import android.util.Log

/**
 * STUB do PartnerInfoHolder - usado quando SAMSUNG_WALLET_ENABLED = false
 * 
 * Esta versão NÃO requer o SDK do Samsung Pay
 * Retorna null pois não pode criar PartnerInfo sem o SDK
 */
class PartnerInfoHolder private constructor() {
    var partnerInfo: Any? = null

    companion object {
        @Volatile
        private var INSTANCE: PartnerInfoHolder? = null

        fun getInstance(serviceId: String): PartnerInfoHolder {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PartnerInfoHolder().also { instance ->
                    Log.w("PartnerInfoHolder", "Samsung Pay SDK não está habilitado - PartnerInfo será null")
                    instance.partnerInfo = null
                }
            }
        }
    }
}

