// ============================================================================
// APPLE WALLET — PASSES (PKPassLibrary)
// ============================================================================
// Repassa os passes de pagamento como o PassKit devolve, sem interpretar:
//   - listPasses       → PKPassLibrary.passes(of: .secureElement)
//   - listRemotePasses → PKPassLibrary.remoteSecureElementPasses
// Juntar as listas, identificar o device (iPhone, Apple Watch) e decidir o
// que fazer com cada estado é do app. Funciona sem o entitlement de
// provisioning: o PKPassLibrary devolve os passes do próprio emissor.

import Foundation
import PassKit

extension RealAppleWalletProvider {

    func listPasses() -> [AWPaymentPass] {
        Self.localPasses().map(Self.makePass)
    }

    func listRemotePasses() -> [AWPaymentPass] {
        Self.remotePasses().map(Self.makePass)
    }

    func isCardTokenized(primaryAccountIdentifier: String) -> Bool {
        Self.findSecureElementPass(primaryAccountIdentifier: primaryAccountIdentifier) != nil
    }

    var isWatchPaired: Bool { watchSession.isPaired }

    // MARK: - Helpers

    static func localPasses() -> [PKSecureElementPass] {
        guard PKPassLibrary.isPassLibraryAvailable() else { return [] }
        return PKPassLibrary().passes(of: .secureElement).compactMap(\.secureElementPass)
    }

    static func remotePasses() -> [PKSecureElementPass] {
        guard PKPassLibrary.isPassLibraryAvailable() else { return [] }
        return PKPassLibrary().remoteSecureElementPasses
    }

    /// Campos do `PKSecureElementPass`, sem transformação além do enum de estado.
    static func makePass(_ sePass: PKSecureElementPass) -> AWPaymentPass {
        let pass = AWPaymentPass()
        pass.primaryAccountIdentifier = sePass.primaryAccountIdentifier
        pass.primaryAccountNumberSuffix = sePass.primaryAccountNumberSuffix
        pass.deviceAccountIdentifier = sePass.deviceAccountIdentifier
        pass.deviceAccountNumberSuffix = sePass.deviceAccountNumberSuffix
        pass.serialNumber = sePass.serialNumber
        pass.passTypeIdentifier = sePass.passTypeIdentifier
        pass.activationState = activationState(sePass.passActivationState)
        pass.isRemote = sePass.isRemotePass
        pass.deviceName = sePass.deviceName
        pass.localizedDescription = sePass.localizedDescription
        return pass
    }

    static func activationState(
        _ state: PKSecureElementPass.PassActivationState
    ) -> AWActivationState {
        switch state {
        case .activated: return .activated
        case .requiresActivation: return .requiresActivation
        case .activating: return .activating
        case .suspended: return .suspended
        case .deactivated: return .deactivated
        @unknown default: return .activated
        }
    }

    /// Primeiro pass com este FPANID: locais, depois remotos.
    static func findSecureElementPass(
        primaryAccountIdentifier: String
    ) -> PKSecureElementPass? {
        guard !primaryAccountIdentifier.isEmpty else { return nil }
        return (localPasses() + remotePasses())
            .first { $0.primaryAccountIdentifier == primaryAccountIdentifier }
    }

    /// Pass com este DPANID: locais, depois remotos.
    static func findSecureElementPass(
        deviceAccountIdentifier: String
    ) -> PKSecureElementPass? {
        (localPasses() + remotePasses())
            .first { $0.deviceAccountIdentifier == deviceAccountIdentifier }
    }
}
