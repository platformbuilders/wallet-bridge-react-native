// ============================================================================
// APPLE WALLET — PASSES (PKPassLibrary)
// ============================================================================
// Leitura dos passes de pagamento do iPhone e dos devices pareados. Funciona
// sem o entitlement de provisioning: o PKPassLibrary devolve os passes do
// próprio emissor (associated applications do pass).

import Foundation
import PassKit

extension RealAppleWalletProvider {

    func listPasses() -> [AWPaymentPass] {
        guard PKPassLibrary.isPassLibraryAvailable() else {
            log("WARN", "PKPassLibrary indisponível neste device.")
            return []
        }

        let passes = Self.allSecureElementPasses().map(Self.makePass)
        let pending = passes.filter {
            $0.activationState == .requiresActivation || $0.activationState == .activating
        }.count
        log("INFO", "listPasses() → \(passes.count) pass(es), \(pending) pendente(s).")
        return passes
    }

    func isCardTokenized(primaryAccountIdentifier: String) -> Bool {
        Self.findSecureElementPass(primaryAccountIdentifier: primaryAccountIdentifier) != nil
    }

    var isWatchPaired: Bool { watchSession.isPaired }

    // MARK: - Helpers

    static func makePass(_ sePass: PKSecureElementPass) -> AWPaymentPass {
        let pass = AWPaymentPass()
        pass.primaryAccountIdentifier = sePass.primaryAccountIdentifier
        pass.primaryAccountNumberSuffix = sePass.primaryAccountNumberSuffix
        pass.deviceAccountIdentifier = sePass.deviceAccountIdentifier
        pass.deviceAccountNumberSuffix = sePass.deviceAccountNumberSuffix
        pass.serialNumber = sePass.serialNumber
        pass.passTypeIdentifier = sePass.passTypeIdentifier
        pass.activationState = activationState(sePass.passActivationState)
        pass.deviceType = isAppleWatchPass(sePass) ? .watch : .phone
        pass.isRemote = sePass.isRemotePass
        pass.deviceName = sePass.deviceName
        pass.localizedDescription = sePass.localizedDescription
        return pass
    }

    /// `isRemotePass` sozinho não basta: o pass remoto pode ser de outro
    /// device pareado. O sample da Apple casa com o nome do device.
    static func isAppleWatchPass(_ pass: PKPass) -> Bool {
        pass.isRemotePass
            && pass.deviceName.localizedCaseInsensitiveContains("Apple Watch")
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

    /// Pass com este FPANID, aqui ou num device pareado. Local primeiro: se
    /// o cartão estiver nos dois, o device atual decide se dá para adicionar.
    ///
    /// Não usa `canAddPaymentPassWithPrimaryAccountIdentifier:`: sem o
    /// entitlement ele devolve `true` sempre.
    static func findSecureElementPass(
        primaryAccountIdentifier: String
    ) -> PKSecureElementPass? {
        guard !primaryAccountIdentifier.isEmpty else { return nil }
        return allSecureElementPasses()
            .sorted { !$0.isRemotePass && $1.isRemotePass }
            .first { $0.primaryAccountIdentifier == primaryAccountIdentifier }
    }

    /// Passes locais + remotos, sem repetição.
    ///
    /// A documentação orienta somar `passes(of:)` e `remoteSecureElementPasses`,
    /// mas o sample da Apple sugere que `passes(of:)` já traz os remotos.
    /// Somar e deduplicar por `passTypeIdentifier + serialNumber` funciona nos
    /// dois casos. A chave identifica o pass, não o cartão: o mesmo cartão no
    /// iPhone e no Watch são dois passes.
    static func allSecureElementPasses() -> [PKSecureElementPass] {
        guard PKPassLibrary.isPassLibraryAvailable() else { return [] }

        let library = PKPassLibrary()
        let candidates = library.passes(of: .secureElement).compactMap(\.secureElementPass)
            + library.remoteSecureElementPasses

        var seen = Set<String>()
        return candidates.filter { pass in
            seen.insert("\(pass.passTypeIdentifier)|\(pass.serialNumber)").inserted
        }
    }
}
