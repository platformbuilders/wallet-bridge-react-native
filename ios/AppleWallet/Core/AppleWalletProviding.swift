// ============================================================================
// APPLE WALLET — CONTRATO NATIVO
// ============================================================================
// A ponte RN (AppleWallet.mm) conhece SÓ este protocolo, nunca a classe
// concreta. A implementação real (PassKit) e o mock (só em Debug) entram pela
// AppleWalletProviderFactory sem mudar a ponte nem o JS.
//
// Só PassKit puro: nada de Wallet Extension (status, pass entries,
// generateAddPaymentPassRequest, autorização). Extension roda em outro
// processo e é Swift nativo do app.

import Foundation
import UIKit

// ============================================================================
// DELEGATE — provider → módulo → JS
// ============================================================================

@objc public protocol AppleWalletProviderDelegate: AnyObject {
    /// Passo 1 do handshake: o app repassa ao emissor.
    func provider(
        _ provider: AppleWalletProviding,
        didReceiveCertificates event: AWProvisioningCertificatesEvent
    )

    /// Transição de estágio ou resultado final do provisioning.
    func provider(
        _ provider: AppleWalletProviding,
        didUpdateProvisioning event: AWProvisioningResultEvent
    )

    func provider(_ provider: AppleWalletProviding, didLog event: AWLogEvent)
}

// ============================================================================
// PROVIDER
// ============================================================================

@objc public protocol AppleWalletProviding: AnyObject {

    var delegate: AppleWalletProviderDelegate? { get set }

    /// Implementação ativa (`real` ou `mock`), para log e diagnóstico.
    var implementationName: String { get }

    /// `ios-simulator` ou `ios-device`.
    var environment: String { get }

    // --- Provisioning ----------------------------------------------------

    /// `PKAddPaymentPassViewController.canAddPaymentPass()`.
    func canAddPaymentPass() -> Bool

    /// `PKPassLibrary.canAddSecureElementPass(primaryAccountIdentifier:)`.
    func canAddSecureElementPass(primaryAccountIdentifier: String) -> Bool

    /// Apresenta a sheet do PassKit. Devolve o `requestId` do fluxo.
    func startInAppProvisioning(config: AWProvisioningRequestConfig) throws -> String

    /// Entrega o passo 2 (dados do emissor) ao PassKit. O pass chega pelo
    /// delegate (`didUpdateProvisioning` com status ADDED).
    func submitProvisioningData(payload: AWSecurePayload) throws

    /// Cancela o fluxo em andamento. `false` se não havia nenhum.
    func cancelProvisioning() -> Bool

    // --- Passes ----------------------------------------------------------

    /// `PKPassLibrary.passes(of: .secureElement)`.
    func listPasses() -> [AWPaymentPass]

    /// `PKPassLibrary.remoteSecureElementPasses` (devices pareados).
    func listRemotePasses() -> [AWPaymentPass]


    var isWatchPaired: Bool { get }

    // --- Verification ----------------------------------------------------

    /// Ativa um pass `requiresActivation` (In-App Verification).
    func activatePass(
        deviceAccountIdentifier: String,
        activationData: String,
        completion: @escaping (Bool, NSError?) -> Void
    )

    // --- Utilitários -----------------------------------------------------

    /// Abre o app Wallet. `false` quando o device não consegue abrir.
    func openWallet() -> Bool
}

// ============================================================================
// CONTROLES DO MOCK
// ============================================================================
// Fora de AppleWalletProviding de propósito: o contrato reflete só o que o
// PassKit oferece. Só a implementação mock (compilada em Debug) adota.

@objc public protocol AppleWalletMockControlling: AnyObject {
    /// Ajusta cenário / latência. Devolve o estado depois de aplicar.
    func configure(_ config: [String: Any]) -> [String: Any]

    /// Limpa passes e provisioning em andamento e volta ao cenário padrão.
    func reset()

    func stateSnapshot() -> [String: Any]
}

// ============================================================================
// HELPERS DE UI COMPARTILHADOS PELAS IMPLEMENTAÇÕES
// ============================================================================

extension AppleWalletProviding {

    /// View controller no topo da janela que o usuário está vendo.
    ///
    /// `windowScene.windows` tem ordem arbitrária e, num app React Native,
    /// a LogBox e cada `Modal` têm janela própria. Apresentar numa janela
    /// oculta gera uma sheet invisível que engole os toques. Por isso a key
    /// window primeiro e, sem ela, o maior `windowLevel` visível.
    func topViewController() -> UIViewController? {
        let scenes = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
        let scene = scenes.first { $0.activationState == .foregroundActive }
            ?? scenes.first

        let candidates = (scene?.windows ?? []).filter {
            !$0.isHidden && $0.alpha > 0 && $0.rootViewController != nil
        }
        let window = candidates.first(where: \.isKeyWindow)
            ?? candidates.max(by: { $0.windowLevel < $1.windowLevel })

        var controller = window?.rootViewController
        while let presented = controller?.presentedViewController {
            controller = presented
        }
        return controller
    }

    /// Apresenta quando a hierarquia estiver estável (sem transição em curso).
    /// Desiste depois de ~1,5 s e chama `onFailure`.
    func presentWhenReady(
        _ controller: UIViewController,
        attempt: Int = 0,
        onFailure: @escaping () -> Void
    ) {
        let maxAttempts = 15
        guard attempt < maxAttempts else {
            onFailure()
            return
        }

        guard let presenter = topViewController(),
              !presenter.isBeingDismissed,
              !presenter.isBeingPresented,
              presenter.presentedViewController == nil,
              let presenterWindow = presenter.view.window,
              !presenterWindow.isHidden
        else {
            DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(100)) {
                self.presentWhenReady(
                    controller,
                    attempt: attempt + 1,
                    onFailure: onFailure
                )
            }
            return
        }

        presenter.present(controller, animated: true)
    }
}
