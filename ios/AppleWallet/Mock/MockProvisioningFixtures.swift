// ============================================================================
// APPLE WALLET MOCK — CENÁRIOS E FIXTURES (só Debug)
// ============================================================================
// Tudo aqui é falso e inerte: nenhum certificado, chave ou dado de cartão real.
// Os valores decodificados começam com "FAKE_". Sem emissor nem bandeira fixa:
// rede e nome vêm da config que o app manda, e os FPANIDs semeados podem ser
// passados em `configure({ primaryAccountIdentifiers })`.

#if DEBUG

import Foundation

/// Cenários do mock. Espelha AppleWalletMockScenario no TS.
enum AWScenario: String, CaseIterable {
    /// Wallet vazia; push conclui no iPhone.
    case happyPush = "HAPPY_PUSH"
    /// Watch pareado; a sheet oferece adicionar também ao Watch.
    case phoneAndWatch = "PHONE_AND_WATCH"
    /// Mesmo cartão no iPhone (ativo) e no Watch (pendente).
    case mixedDeviceStates = "MIXED_DEVICE_STATES"
    /// Vários passes pendentes de ativação, em devices diferentes.
    case multiplePending = "MULTIPLE_PENDING"
    /// Um pass desativado e um ativo.
    case deactivated = "DEACTIVATED"
    /// Um pass em ativação (aguardando o emissor).
    case activating = "ACTIVATING"
    /// Usuário cancela na sheet.
    case userCanceled = "USER_CANCELED"
    case entitlementMissing = "ENTITLEMENT_MISSING"
    case noSecureElement = "NO_SECURE_ELEMENT"
    /// O cartão pedido já está na Wallet.
    case alreadyAdded = "ALREADY_ADDED"
    /// O PassKit rejeita o passo 2.
    case invalidActivationData = "INVALID_ACTIVATION_DATA"
    /// O emissor não responde o passo 2 a tempo.
    case hostTimeout = "HOST_TIMEOUT"
    /// `activatePass` falha.
    case activationFailed = "ACTIVATION_FAILED"

    /// Cenários em que há um Apple Watch pareado.
    var hasWatch: Bool {
        switch self {
        case .phoneAndWatch, .mixedDeviceStates, .multiplePending: return true
        default: return false
        }
    }
}

enum MockProvisioningFixtures {

    static let passTypeIdentifier = "pass.com.builders.wallet.mock"

    private static func base64(_ plain: String) -> String {
        Data(plain.utf8).base64EncodedString()
    }

    private static func uniqueSuffix() -> String {
        "\(Int(Date().timeIntervalSince1970 * 1000))_\(Int.random(in: 0..<100_000))"
    }

    static func defaultPrimaryAccountIdentifiers() -> [String] {
        ["FAKE_FPANID_1", "FAKE_FPANID_2", "FAKE_FPANID_3"]
    }

    // MARK: - Handshake

    static func certificatesEvent(requestId: String) -> AWProvisioningCertificatesEvent {
        let suffix = uniqueSuffix()
        return AWProvisioningCertificatesEvent(
            requestId: requestId,
            certificates: [
                base64("FAKE_LEAF_CERT|oid=\(AWConst.appleLeafCertOID)|req=\(suffix)"),
                base64("FAKE_SUB_CA_CERT|req=\(suffix)"),
            ],
            nonce: base64("FAKE_NONCE|\(suffix)"),
            nonceSignature: base64("FAKE_NONCE_SIGNATURE|\(suffix)")
        )
    }

    // MARK: - Passes

    static func pass(
        primaryAccountIdentifier: String,
        primaryAccountNumberSuffix: String,
        deviceType: AWDeviceType,
        state: AWActivationState,
        description: String
    ) -> AWPaymentPass {
        let suffix = uniqueSuffix()
        let pass = AWPaymentPass()
        pass.primaryAccountIdentifier = primaryAccountIdentifier
        pass.primaryAccountNumberSuffix = primaryAccountNumberSuffix
        pass.deviceAccountIdentifier = "FAKE_DPANID_\(suffix)_\(deviceType.rawValue)"
        pass.deviceAccountNumberSuffix = String(Int.random(in: 1000...9999))
        pass.serialNumber = "FAKE_SERIAL_\(suffix)_\(deviceType.rawValue)"
        pass.passTypeIdentifier = passTypeIdentifier
        pass.activationState = state
        pass.deviceType = deviceType
        pass.isRemote = deviceType == .watch
        pass.deviceName = deviceType == .watch ? "Apple Watch" : "iPhone"
        pass.localizedDescription = description
        return pass
    }

    /// Pass recém-provisionado a partir da config do app. Nasce pendente:
    /// quem ativa é o emissor (como o token do Google/Samsung).
    static func provisionedPass(
        config: AWProvisioningRequestConfig,
        deviceType: AWDeviceType
    ) -> AWPaymentPass {
        pass(
            primaryAccountIdentifier: config.primaryAccountIdentifier
                ?? "FAKE_FPANID_\(uniqueSuffix())",
            primaryAccountNumberSuffix: config.primaryAccountSuffix,
            deviceType: deviceType,
            state: .requiresActivation,
            description: config.localizedDescription
        )
    }

    /// Passes que já estão na Wallet ao entrar no cenário.
    static func seed(
        for scenario: AWScenario,
        primaryAccountIdentifiers ids: [String]
    ) -> [AWPaymentPass] {
        func id(_ index: Int) -> String {
            index < ids.count ? ids[index] : "FAKE_FPANID_\(index + 1)"
        }

        switch scenario {
        case .mixedDeviceStates:
            return [
                pass(primaryAccountIdentifier: id(0), primaryAccountNumberSuffix: "1111",
                     deviceType: .phone, state: .activated, description: "Cartão 1"),
                pass(primaryAccountIdentifier: id(0), primaryAccountNumberSuffix: "1111",
                     deviceType: .watch, state: .requiresActivation, description: "Cartão 1"),
            ]
        case .multiplePending:
            return [
                pass(primaryAccountIdentifier: id(0), primaryAccountNumberSuffix: "1111",
                     deviceType: .phone, state: .requiresActivation, description: "Cartão 1"),
                pass(primaryAccountIdentifier: id(1), primaryAccountNumberSuffix: "2222",
                     deviceType: .phone, state: .requiresActivation, description: "Cartão 2"),
                pass(primaryAccountIdentifier: id(1), primaryAccountNumberSuffix: "2222",
                     deviceType: .watch, state: .requiresActivation, description: "Cartão 2"),
            ]
        case .deactivated:
            return [
                pass(primaryAccountIdentifier: id(0), primaryAccountNumberSuffix: "1111",
                     deviceType: .phone, state: .deactivated, description: "Cartão 1"),
                pass(primaryAccountIdentifier: id(1), primaryAccountNumberSuffix: "2222",
                     deviceType: .phone, state: .activated, description: "Cartão 2"),
            ]
        case .activating:
            return [
                pass(primaryAccountIdentifier: id(0), primaryAccountNumberSuffix: "1111",
                     deviceType: .phone, state: .activating, description: "Cartão 1"),
            ]
        case .alreadyAdded:
            return [
                pass(primaryAccountIdentifier: id(0), primaryAccountNumberSuffix: "1111",
                     deviceType: .phone, state: .activated, description: "Cartão 1"),
            ]
        default:
            return []
        }
    }
}

#endif
