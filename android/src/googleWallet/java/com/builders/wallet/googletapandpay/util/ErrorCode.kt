package com.builders.wallet.googletapandpay.util

import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tapandpay.TapAndPayStatusCodes

object ErrorCode {
    fun getErrorCodeName(errorCode: Int): String {
        return when (errorCode) {
            // Common Status Codes
            CommonStatusCodes.SUCCESS -> "SUCCESS"
            CommonStatusCodes.SUCCESS_CACHE -> "SUCCESS_CACHE"
            CommonStatusCodes.SERVICE_VERSION_UPDATE_REQUIRED -> "SERVICE_VERSION_UPDATE_REQUIRED"
            CommonStatusCodes.SERVICE_DISABLED -> "SERVICE_DISABLED"
            CommonStatusCodes.SIGN_IN_REQUIRED -> "SIGN_IN_REQUIRED"
            CommonStatusCodes.INVALID_ACCOUNT -> "INVALID_ACCOUNT"
            CommonStatusCodes.RESOLUTION_REQUIRED -> "RESOLUTION_REQUIRED"
            CommonStatusCodes.NETWORK_ERROR -> "NETWORK_ERROR"
            CommonStatusCodes.INTERNAL_ERROR -> "INTERNAL_ERROR"
            CommonStatusCodes.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
            CommonStatusCodes.ERROR -> "ERROR"
            CommonStatusCodes.INTERRUPTED -> "INTERRUPTED"
            CommonStatusCodes.TIMEOUT -> "TIMEOUT"
            CommonStatusCodes.CANCELED -> "CANCELED"
            CommonStatusCodes.API_NOT_CONNECTED -> "API_NOT_CONNECTED"
            CommonStatusCodes.REMOTE_EXCEPTION -> "REMOTE_EXCEPTION"
            CommonStatusCodes.CONNECTION_SUSPENDED_DURING_CALL -> "CONNECTION_SUSPENDED_DURING_CALL"
            CommonStatusCodes.RECONNECTION_TIMED_OUT_DURING_UPDATE -> "RECONNECTION_TIMED_OUT_DURING_UPDATE"
            CommonStatusCodes.RECONNECTION_TIMED_OUT -> "RECONNECTION_TIMED_OUT"

            // TapAndPay Status Codes
            TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET -> "TAP_AND_PAY_NO_ACTIVE_WALLET"
            TapAndPayStatusCodes.TAP_AND_PAY_TOKEN_NOT_FOUND -> "TAP_AND_PAY_TOKEN_NOT_FOUND"
            TapAndPayStatusCodes.TAP_AND_PAY_INVALID_TOKEN_STATE -> "TAP_AND_PAY_INVALID_TOKEN_STATE"
            TapAndPayStatusCodes.TAP_AND_PAY_ATTESTATION_ERROR -> "TAP_AND_PAY_ATTESTATION_ERROR"
            TapAndPayStatusCodes.TAP_AND_PAY_UNAVAILABLE -> "TAP_AND_PAY_UNAVAILABLE"
            TapAndPayStatusCodes.TAP_AND_PAY_SAVE_CARD_ERROR -> "TAP_AND_PAY_SAVE_CARD_ERROR"
            TapAndPayStatusCodes.TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION -> "TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION"
            TapAndPayStatusCodes.TAP_AND_PAY_TOKENIZATION_DECLINED -> "TAP_AND_PAY_TOKENIZATION_DECLINED"
            TapAndPayStatusCodes.TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR -> "TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR"
            TapAndPayStatusCodes.TAP_AND_PAY_TOKENIZE_ERROR -> "TAP_AND_PAY_TOKENIZE_ERROR"
            TapAndPayStatusCodes.TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED -> "TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED"
            TapAndPayStatusCodes.TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT -> "TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT"
            TapAndPayStatusCodes.TAP_AND_PAY_USER_CANCELED_FLOW -> "TAP_AND_PAY_USER_CANCELED_FLOW"
            TapAndPayStatusCodes.TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED -> "TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED"

            else -> "UNKNOWN_ERROR_$errorCode"
        }
    }

    fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            // Common Status Codes - Mensagens amigáveis
            CommonStatusCodes.SUCCESS -> "Operação bem-sucedida"
            CommonStatusCodes.SUCCESS_CACHE -> "Operação bem-sucedida (cache)"
            CommonStatusCodes.SERVICE_VERSION_UPDATE_REQUIRED -> "Atualização do Google Play Services necessária"
            CommonStatusCodes.SERVICE_DISABLED -> "Serviço desabilitado"
            CommonStatusCodes.SIGN_IN_REQUIRED -> "Login necessário"
            CommonStatusCodes.INVALID_ACCOUNT -> "Conta inválida"
            CommonStatusCodes.RESOLUTION_REQUIRED -> "Resolução necessária"
            CommonStatusCodes.NETWORK_ERROR -> "Erro de rede"
            CommonStatusCodes.INTERNAL_ERROR -> "Erro interno"
            CommonStatusCodes.DEVELOPER_ERROR -> "Erro de desenvolvedor"
            CommonStatusCodes.ERROR -> "Erro"
            CommonStatusCodes.INTERRUPTED -> "Operação interrompida"
            CommonStatusCodes.TIMEOUT -> "Tempo limite excedido"
            CommonStatusCodes.CANCELED -> "Operação cancelada"
            CommonStatusCodes.API_NOT_CONNECTED -> "API não conectada"
            CommonStatusCodes.REMOTE_EXCEPTION -> "Exceção remota"
            CommonStatusCodes.CONNECTION_SUSPENDED_DURING_CALL -> "Conexão suspensa durante chamada"
            CommonStatusCodes.RECONNECTION_TIMED_OUT_DURING_UPDATE -> "Tempo de reconexão esgotado durante atualização"
            CommonStatusCodes.RECONNECTION_TIMED_OUT -> "Tempo de reconexão esgotado"

            // TapAndPay Status Codes - Mensagens amigáveis
            TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET -> "Nenhuma carteira ativa"
            TapAndPayStatusCodes.TAP_AND_PAY_TOKEN_NOT_FOUND -> "Token não encontrado"
            TapAndPayStatusCodes.TAP_AND_PAY_INVALID_TOKEN_STATE -> "Estado do token inválido"
            TapAndPayStatusCodes.TAP_AND_PAY_ATTESTATION_ERROR -> "Erro de atestado"
            TapAndPayStatusCodes.TAP_AND_PAY_UNAVAILABLE -> "Google Pay indisponível"
            TapAndPayStatusCodes.TAP_AND_PAY_SAVE_CARD_ERROR -> "Erro ao salvar cartão"
            TapAndPayStatusCodes.TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION -> "Inelegível para tokenização"
            TapAndPayStatusCodes.TAP_AND_PAY_TOKENIZATION_DECLINED -> "Tokenização recusada"
            TapAndPayStatusCodes.TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR -> "Erro ao verificar elegibilidade"
            TapAndPayStatusCodes.TAP_AND_PAY_TOKENIZE_ERROR -> "Erro ao tokenizar"
            TapAndPayStatusCodes.TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED -> "Ativação do token necessária"
            TapAndPayStatusCodes.TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT -> "Tempo limite de entrega de credenciais"
            TapAndPayStatusCodes.TAP_AND_PAY_USER_CANCELED_FLOW -> "Usuário cancelou a operação"
            TapAndPayStatusCodes.TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED -> "Falha ao registrar cartões virtuais"

            else -> "Erro desconhecido"
        }
    }
}

