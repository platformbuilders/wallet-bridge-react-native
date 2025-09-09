package com.builders.wallet.samsungpay.util

data class PartnerInfo(
    val serviceId: String
)

object PartnerInfoHolder {
    private val partnerInfoMap = mutableMapOf<String, PartnerInfo>()

    fun getInstance(serviceId: String): PartnerInfoHolder {
        if (!partnerInfoMap.containsKey(serviceId)) {
            partnerInfoMap[serviceId] = PartnerInfo(serviceId)
        }
        return PartnerInfoHolder
    }

    val partnerInfo: PartnerInfo
        get() = partnerInfoMap.values.firstOrNull() ?: PartnerInfo("default")
}