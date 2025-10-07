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
                // Criar Bundle com configurações do Samsung Pay
                val bundle = android.os.Bundle()
                
                // Obter constantes do SamsungPay via reflexão
                val samsungPayClass = Class.forName("com.samsung.android.sdk.samsungpay.v2.SamsungPay")
                val serviceTypeClass = Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk\$ServiceType")
                
                // Obter PARTNER_SERVICE_TYPE
                val partnerServiceTypeField = samsungPayClass.getField("PARTNER_SERVICE_TYPE")
                val partnerServiceType = partnerServiceTypeField.get(null) as String
                
                // Obter EXTRA_ISSUER_NAME
                val extraIssuerNameField = samsungPayClass.getField("EXTRA_ISSUER_NAME")
                val extraIssuerName = extraIssuerNameField.get(null) as String
                
                // Obter INAPP_PAYMENT do enum ServiceType (padrão para operações gerais)
                val inappPaymentField = serviceTypeClass.getField("INAPP_PAYMENT")
                val inappPayment = inappPaymentField.get(null)
                
                // Configurar o Bundle
                bundle.putString(partnerServiceType, inappPayment.toString())
                bundle.putString(extraIssuerName, "Builders Wallet") // Nome do emissor
                
                Log.d("PartnerInfoHolder", "Bundle configurado com PARTNER_SERVICE_TYPE: $partnerServiceType = ${inappPayment}, EXTRA_ISSUER_NAME: $extraIssuerName = Builders Wallet")
                
                // Criar PartnerInfo com serviceId e Bundle
                val partnerInfoClass = Class.forName("com.samsung.android.sdk.samsungpay.v2.PartnerInfo")
                val ctor = partnerInfoClass.getConstructor(String::class.java, android.os.Bundle::class.java)
                ctor.newInstance(serviceId, bundle)
            } catch (t: Throwable) {
                Log.e("PartnerInfoHolder", "Falha ao instanciar PartnerInfo por reflexão: ${t.message}", t)
                // Fallback: tentar criar PartnerInfo apenas com serviceId
                try {
                    val clazz = Class.forName("com.samsung.android.sdk.samsungpay.v2.PartnerInfo")
                    val ctor = clazz.getConstructor(String::class.java)
                    ctor.newInstance(serviceId)
                } catch (fallbackError: Throwable) {
                    Log.e("PartnerInfoHolder", "Falha no fallback também: ${fallbackError.message}", fallbackError)
                    null
                }
            }
        }
    }
}