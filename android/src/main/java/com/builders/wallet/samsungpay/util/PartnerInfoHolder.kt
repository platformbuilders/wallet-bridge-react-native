package com.builders.wallet.samsungpay.util

import android.util.Log

class PartnerInfoHolder private constructor() {
    var partnerInfo: Any? = null

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

        private fun createPartnerInfo(serviceId: String): Any? {
            return try {
                val clazz = Class.forName("com.samsung.android.sdk.samsungpay.v2.PartnerInfo")
                val ctor = clazz.getConstructor(String::class.java)
                ctor.newInstance(serviceId)
            } catch (t: Throwable) {
                Log.e("PartnerInfoHolder", "Falha ao instanciar PartnerInfo por reflexão: ${t.message}", t)
                null
            }
        }
    }
}