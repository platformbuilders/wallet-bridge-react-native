// ============================================================================
// APPLE WALLET — CONSTANTES E MODELOS COMPARTILHADOS
// ============================================================================
// Contrato nativo ↔ JS: nomes de evento, códigos de erro e formato dos
// dicionários. Espelha src/types/apple-wallet.types.ts e
// src/enums/apple-wallet.enum.ts. Os nomes seguem a nomenclatura do PassKit;
// nada aqui conhece emissor, bandeira ou backend.
//
// Tudo que a ponte (AppleWallet.mm) consome é `@objc`: o TurboModule é
// Objective-C++ e só enxerga o header Swift gerado.

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
    @objc public static let implementationMock = "mock"

    /// Postada quando a implementação ativa muda (só acontece em Debug, com
    /// o mock). A ponte reassina o delegate do provider novo.
    @objc public static let providerDidChangeNotification =
        Notification.Name("AppleWalletProviderDidChange")

    private override init() { super.init() }
}

// ============================================================================
// ENUMS
// ============================================================================

/// Estágios do provisioning. Espelha AppleProvisioningStatus no TS.
public enum AWStatus: String {
    case idle = "IDLE"
    case presenting = "PRESENTING"
    case awaitingHost = "AWAITING_HOST"
    case submitting = "SUBMITTING"
    case added = "ADDED"
    case canceled = "CANCELED"
    case failed = "FAILED"

    var isTerminal: Bool {
        self == .added || self == .canceled || self == .failed
    }
}

/// Motivo da resposta de canAddCard. Espelha AppleCanAddCardReason.
public enum AWReason: String {
    case available = "AVAILABLE"
    case entitlementMissing = "ENTITLEMENT_MISSING"
    case noSecureElement = "NO_SECURE_ELEMENT"
    case alreadyAdded = "ALREADY_ADDED"
    /// Cartão já está na Wallet, mas pendente de ativação. Separado de
    /// `alreadyAdded` porque exige ação do app (CTA de ativação).
    case requiresActivation = "REQUIRES_ACTIVATION"
    case unknown = "UNKNOWN"

    /// Código de erro do reject quando o provisioning é bloqueado.
    var errorCode: AWErrorCode {
        switch self {
        case .entitlementMissing: return .entitlementMissing
        case .noSecureElement: return .noSecureElement
        case .alreadyAdded, .requiresActivation: return .alreadyAdded
        case .available, .unknown: return .unknown
        }
    }
}

/// Códigos legíveis entregues ao JS. Espelha AppleWalletErrorCode.
public enum AWErrorCode: String {
    case unknown = "UNKNOWN"
    case userCanceled = "USER_CANCELED"
    case systemCanceled = "SYSTEM_CANCELED"
    case entitlementMissing = "ENTITLEMENT_MISSING"
    case noSecureElement = "NO_SECURE_ELEMENT"
    case alreadyAdded = "ALREADY_ADDED"
    case invalidConfiguration = "INVALID_CONFIGURATION"
    case invalidSecurePayload = "INVALID_SECURE_PAYLOAD"
    case hostTimeout = "HOST_TIMEOUT"
    case notInProgress = "NOT_IN_PROGRESS"
    case alreadyInProgress = "ALREADY_IN_PROGRESS"
    case passNotFound = "PASS_NOT_FOUND"
    case activationFailed = "ACTIVATION_FAILED"
    case passLibraryUnavailable = "PASS_LIBRARY_UNAVAILABLE"
    case notSupportedOnPlatform = "NOT_SUPPORTED_ON_PLATFORM"
}

/// PKSecureElementPass.PassActivationState. Espelha ApplePassActivationState.
public enum AWActivationState: String {
    case activated = "ACTIVATED"
    case requiresActivation = "REQUIRES_ACTIVATION"
    case activating = "ACTIVATING"
    case suspended = "SUSPENDED"
    case deactivated = "DEACTIVATED"
}

/// Onde o pass está. Espelha AppleDeviceType.
public enum AWDeviceType: String {
    case phone = "PHONE"
    case watch = "WATCH"
}

// ============================================================================
// ERROS
// ============================================================================

@objc public final class AWError: NSObject {
    @objc public static let domain = "com.builders.wallet.apple"
    /// Chave do userInfo que a ponte usa como `code` do reject.
    @objc public static let codeKey = "appleWalletErrorCode"

    /// NSError cujo código legível vai para o JS como `code` do reject.
    static func make(_ code: AWErrorCode, _ message: String) -> NSError {
        NSError(
            domain: domain,
            code: -1,
            userInfo: [
                NSLocalizedDescriptionKey: message,
                codeKey: code.rawValue,
            ]
        )
    }

    /// Código legível de um NSError (o nosso ou um do sistema).
    @objc public static func code(of error: NSError) -> String {
        (error.userInfo[codeKey] as? String) ?? AWErrorCode.unknown.rawValue
    }

    private override init() { super.init() }
}

// ============================================================================
// MODELOS
// ============================================================================

/// PKAddPaymentPassRequestConfiguration. Espelha AppleProvisioningConfig.
///
/// Sem `encryptionScheme`: o provisioning usa sempre ECC_V2 (ver
/// RealAppleWalletProvider+Provisioning).
@objc public final class AWProvisioningRequestConfig: NSObject {
    let cardholderName: String
    let primaryAccountSuffix: String
    let localizedDescription: String
    let paymentNetwork: String
    let primaryAccountIdentifier: String?
    /// Vai em `cardDetails` como PKLabeledValue(label: "expiration"), "MM/YY".
    let expiration: String?

    init(
        cardholderName: String,
        primaryAccountSuffix: String,
        localizedDescription: String,
        paymentNetwork: String,
        primaryAccountIdentifier: String? = nil,
        expiration: String? = nil
    ) {
        self.cardholderName = cardholderName
        self.primaryAccountSuffix = primaryAccountSuffix
        self.localizedDescription = localizedDescription
        self.paymentNetwork = paymentNetwork
        self.primaryAccountIdentifier = primaryAccountIdentifier
        self.expiration = expiration
        super.init()
    }

    /// Nunca prossegue com config incompleta: o PassKit falharia sem motivo.
    @objc public static func from(dictionary: [String: Any]?) throws
        -> AWProvisioningRequestConfig
    {
        guard let dictionary else {
            throw AWError.make(
                .invalidConfiguration,
                "Configuração de provisioning ausente."
            )
        }

        func require(_ key: String) throws -> String {
            guard let value = dictionary[key] as? String, !value.isEmpty else {
                throw AWError.make(
                    .invalidConfiguration,
                    "Campo obrigatório ausente: \(key)"
                )
            }
            return value
        }

        func optional(_ key: String) -> String? {
            guard let value = dictionary[key] as? String, !value.isEmpty else {
                return nil
            }
            return value
        }

        return AWProvisioningRequestConfig(
            cardholderName: try require("cardholderName"),
            primaryAccountSuffix: try require("primaryAccountSuffix"),
            localizedDescription: try require("localizedDescription"),
            paymentNetwork: try require("paymentNetwork"),
            primaryAccountIdentifier: optional("primaryAccountIdentifier"),
            expiration: optional("expiration")
        )
    }
}

/// Passo 2 do handshake: vem do emissor, nunca é calculado no device.
/// Base64 padrão (RFC 4648 §4). Espelha AppleSecurePayload.
@objc public final class AWSecurePayload: NSObject {
    let activationData: String
    let encryptedPassData: String
    let ephemeralPublicKey: String

    init(activationData: String, encryptedPassData: String, ephemeralPublicKey: String) {
        self.activationData = activationData
        self.encryptedPassData = encryptedPassData
        self.ephemeralPublicKey = ephemeralPublicKey
        super.init()
    }

    @objc public static func from(dictionary: [String: Any]?) throws -> AWSecurePayload {
        guard let dictionary else {
            throw AWError.make(.invalidSecurePayload, "Payload do passo 2 ausente.")
        }

        func require(_ key: String) throws -> String {
            guard let value = dictionary[key] as? String, !value.isEmpty else {
                throw AWError.make(.invalidSecurePayload, "\(key) ausente.")
            }
            return value
        }

        return AWSecurePayload(
            activationData: try require("activationData"),
            encryptedPassData: try require("encryptedPassData"),
            ephemeralPublicKey: try require("ephemeralPublicKey")
        )
    }
}

/// PKSecureElementPass. Espelha ApplePaymentPass.
@objc public final class AWPaymentPass: NSObject {
    /// FPANID.
    var primaryAccountIdentifier = ""
    var primaryAccountNumberSuffix = ""
    /// DPANID.
    var deviceAccountIdentifier = ""
    var deviceAccountNumberSuffix = ""
    var serialNumber = ""
    var passTypeIdentifier = ""
    var activationState: AWActivationState = .activated
    var deviceType: AWDeviceType = .phone
    /// `true` quando o pass está em um device pareado (Apple Watch).
    var isRemote = false
    var deviceName = ""
    var localizedDescription = ""

    @objc public func toDictionary() -> [String: Any] {
        [
            "primaryAccountIdentifier": primaryAccountIdentifier,
            "primaryAccountNumberSuffix": primaryAccountNumberSuffix,
            "deviceAccountIdentifier": deviceAccountIdentifier,
            "deviceAccountNumberSuffix": deviceAccountNumberSuffix,
            "serialNumber": serialNumber,
            "passTypeIdentifier": passTypeIdentifier,
            "passActivationState": activationState.rawValue,
            "deviceType": deviceType.rawValue,
            "isRemote": isRemote,
            "deviceName": deviceName,
            "localizedDescription": localizedDescription,
        ]
    }
}

/// Resultado de canAddCard, com o motivo. O PassKit devolve só um booleano;
/// aqui o motivo é preservado para o app distinguir entitlement ausente de
/// device sem Secure Element. Espelha AppleCanAddCardResult.
@objc public final class AWCanAddCardResult: NSObject {
    let canAdd: Bool
    let reason: AWReason
    let message: String
    /// DPANID do pass que já existe (`alreadyAdded` / `requiresActivation`).
    let deviceAccountIdentifier: String?

    init(
        canAdd: Bool,
        reason: AWReason,
        message: String,
        deviceAccountIdentifier: String? = nil
    ) {
        self.canAdd = canAdd
        self.reason = reason
        self.message = message
        self.deviceAccountIdentifier = deviceAccountIdentifier
        super.init()
    }

    @objc public var canAddCard: Bool { canAdd }

    @objc public func toDictionary() -> [String: Any] {
        var dictionary: [String: Any] = [
            "canAdd": canAdd,
            "reason": reason.rawValue,
            "message": message,
        ]
        if let deviceAccountIdentifier {
            dictionary["deviceAccountIdentifier"] = deviceAccountIdentifier
        }
        return dictionary
    }
}

/// Passo 1 do handshake, coletado pelo PassKit no Secure Element.
/// Base64 padrão; `certificates` com o leaf primeiro e a sub-CA depois.
@objc public final class AWProvisioningCertificatesEvent: NSObject {
    let requestId: String
    let certificates: [String]
    let nonce: String
    let nonceSignature: String

    init(requestId: String, certificates: [String], nonce: String, nonceSignature: String) {
        self.requestId = requestId
        self.certificates = certificates
        self.nonce = nonce
        self.nonceSignature = nonceSignature
        super.init()
    }

    @objc public func toDictionary() -> [String: Any] {
        [
            "requestId": requestId,
            "certificates": certificates,
            "nonce": nonce,
            "nonceSignature": nonceSignature,
        ]
    }
}

/// Transição de estágio / resultado final do provisioning.
@objc public final class AWProvisioningResultEvent: NSObject {
    let requestId: String
    let status: AWStatus
    var pass: AWPaymentPass?
    /// Passes adicionados juntos (ex.: o mesmo cartão no Apple Watch).
    var additionalPasses: [AWPaymentPass] = []
    var errorCode: AWErrorCode?
    var errorMessage: String?

    init(requestId: String, status: AWStatus) {
        self.requestId = requestId
        self.status = status
        super.init()
    }

    @objc public func toDictionary() -> [String: Any] {
        var dictionary: [String: Any] = [
            "requestId": requestId,
            "status": status.rawValue,
            "additionalPasses": additionalPasses.map { $0.toDictionary() },
        ]
        if let pass {
            dictionary["pass"] = pass.toDictionary()
        }
        if let errorCode {
            dictionary["errorCode"] = errorCode.rawValue
        }
        if let errorMessage {
            dictionary["errorMessage"] = errorMessage
        }
        return dictionary
    }
}

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
