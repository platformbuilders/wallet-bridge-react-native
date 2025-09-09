package com.builders.wallet.samsungpay.util

object ErrorCode {
    fun getErrorCodeName(errorCode: Int): String {
        return when (errorCode) {
            0 -> "SUCCESS"
            else -> "UNKNOWN_ERROR_$errorCode"
        }
    }
}