// ============================================================================
// APPLE WALLET — CONSTANTES E MODELOS COMPARTILHADOS
// ============================================================================
// Contrato nativo ↔ JS: nomes de evento, códigos de erro e formato dos
// dicionários. Os nomes seguem a nomenclatura do PassKit; nada aqui conhece
// emissor, bandeira ou backend.
//
// Tudo é `@objc` porque a ponte RN (AppleWallet.mm) precisa consumir:
// RCT_EXPORT_MODULE é um macro Objective-C, sem equivalente em Swift puro.

import Foundation

// ============================================================================
// CONSTANTES
// ============================================================================

@objc public final class AWConst: NSObject {

    // --- eventos ---
    /// Nome próprio da Apple para não colidir com o log de Google/Samsung.
    @objc public static let eventLog = "AppleWalletLog"

    // --- Apple ---
    /// Entitlement restrito exigido para instanciar PKAddPaymentPassViewController.
    @objc public static let provisioningEntitlement =
        "com.apple.developer.payment-pass-provisioning"
    /// OID que precisa estar presente e critical no leaf cert.
    @objc public static let appleLeafCertOID = "1.2.840.113635.100.6.39"
    @objc public static let walletURLScheme = "shoebox://"

    // --- implementação ativa ---
    @objc public static let implementationReal = "real"

    private override init() { super.init() }
}

// ============================================================================
// ERROS
// ============================================================================

/// Códigos legíveis entregues ao JS no reject da promise.
public enum AWErrorCode: String {
    case unknown
    case userCanceled
    case systemCanceled
    case unsupported
    case entitlementMissing
    case invalidConfiguration
    case invalidSecurePayload
    case hostTimeout
    case notInProgress
    case alreadyInProgress
    case notSupportedOnPlatform
}

@objc public final class AWError: NSObject {
    @objc public static let domain = "com.builders.wallet.apple"
    /// Chave do userInfo que a ponte usa como `code` do reject.
    @objc public static let codeKey = "appleWalletErrorCode"

    /// Monta um NSError cujo código legível vai para o JS. A mensagem é
    /// prefixada com `[code]` para o JS conseguir casar por string.
    static func make(_ code: AWErrorCode, _ message: String) -> NSError {
        NSError(
            domain: domain,
            code: -1,
            userInfo: [
                NSLocalizedDescriptionKey: "[\(code.rawValue)] \(message)",
                codeKey: code.rawValue,
            ]
        )
    }

    private override init() { super.init() }
}

// ============================================================================
// MODELOS
// ============================================================================

@objc public final class AWLogEvent: NSObject {
    let level: String
    let tag: String
    let message: String

    init(level: String, tag: String, message: String) {
        self.level = level
        self.tag = tag
        self.message = message
        super.init()
    }

    @objc public func toDictionary() -> [String: Any] {
        [
            "level": level,
            "tag": tag,
            "message": message,
        ]
    }
}
