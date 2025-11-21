package com.builders.wallet.samsungpay.util

/**
 * STUB do ErrorCode - usado quando SAMSUNG_WALLET_ENABLED = false
 * 
 * Esta versão NÃO requer o SDK do Samsung Pay
 * Retorna nomes de erro baseados em valores conhecidos
 */
object ErrorCode {
    fun getErrorCodeName(errorCode: Int): String {
        return when (errorCode) {
            0 -> "ERROR_NONE"
            -1 -> "ERROR_SPAY_INTERNAL"
            -2 -> "ERROR_INVALID_INPUT"
            -3 -> "ERROR_NOT_SUPPORTED"
            -4 -> "ERROR_NOT_FOUND"
            -5 -> "ERROR_ALREADY_DONE"
            -6 -> "ERROR_NOT_ALLOWED"
            -7 -> "ERROR_USER_CANCELED"
            -10 -> "ERROR_PARTNER_SDK_API_LEVEL"
            -11 -> "ERROR_PARTNER_SERVICE_TYPE"
            -12 -> "ERROR_INVALID_PARAMETER"
            -21 -> "ERROR_NO_NETWORK"
            -22 -> "ERROR_SERVER_NO_RESPONSE"
            -99 -> "ERROR_PARTNER_INFO_INVALID"
            -103 -> "ERROR_INITIATION_FAIL"
            -104 -> "ERROR_REGISTRATION_FAIL"
            -105 -> "ERROR_DUPLICATED_SDK_API_CALLED"
            -300 -> "ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION"
            -301 -> "ERROR_SERVICE_ID_INVALID"
            -302 -> "ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION"
            -303 -> "ERROR_PARTNER_APP_SIGNATURE_MISMATCH"
            -304 -> "ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED"
            -305 -> "ERROR_PARTNER_APP_BLOCKED"
            -306 -> "ERROR_USER_NOT_REGISTERED_FOR_DEBUG"
            -307 -> "ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE"
            -308 -> "ERROR_PARTNER_NOT_APPROVED"
            -309 -> "ERROR_UNAUTHORIZED_REQUEST_TYPE"
            -310 -> "ERROR_EXPIRED_OR_INVALID_DEBUG_KEY"
            -311 -> "ERROR_SERVER_INTERNAL"
            -350 -> "ERROR_DEVICE_NOT_SAMSUNG"
            -351 -> "ERROR_SPAY_PKG_NOT_FOUND"
            -352 -> "ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE"
            -353 -> "ERROR_DEVICE_INTEGRITY_CHECK_FAIL"
            -360 -> "ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL"
            -361 -> "ERROR_ANDROID_PLATFORM_CHECK_FAIL"
            -354 -> "ERROR_MISSING_INFORMATION"
            -356 -> "ERROR_SPAY_SETUP_NOT_COMPLETED"
            -357 -> "ERROR_SPAY_APP_NEED_TO_UPDATE"
            -358 -> "ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED"
            -359 -> "ERROR_UNABLE_TO_VERIFY_CALLER"
            -604 -> "ERROR_SPAY_FMM_LOCK"
            -605 -> "ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY"
            // Status codes
            0 -> "SPAY_NOT_SUPPORTED"
            1 -> "SPAY_NOT_READY"
            2 -> "SPAY_READY"
            3 -> "SPAY_NOT_ALLOWED_TEMPORALLY"
            10 -> "SPAY_HAS_TRANSIT_CARD"
            11 -> "SPAY_HAS_NO_TRANSIT_CARD"
            else -> "UNKNOWN_ERROR_$errorCode"
        }
    }
}

