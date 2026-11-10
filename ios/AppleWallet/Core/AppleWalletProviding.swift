// ============================================================================
// APPLE WALLET — CONTRATO NATIVO
// ============================================================================
// A ponte RN (AppleWallet.mm) conhece SÓ este protocolo, nunca a classe
// concreta. Cada feature (Provisioning, Verification, Passes) acrescenta aqui
// os métodos que expõe; uma implementação alternativa (ex.: mock) entra pela
// AppleWalletProviderFactory sem mudar a ponte nem o JS.

import Foundation

// ============================================================================
// DELEGATE — provider → módulo → JS
// ============================================================================

@objc public protocol AppleWalletProviderDelegate: AnyObject {
    func provider(_ provider: AppleWalletProviding, didLog event: AWLogEvent)
}

// ============================================================================
// PROVIDER
// ============================================================================

@objc public protocol AppleWalletProviding: AnyObject {

    var delegate: AppleWalletProviderDelegate? { get set }

    /// Implementação ativa (`real`), exposta para log e diagnóstico.
    var implementationName: String { get }

    /// `ios-simulator` ou `ios-device`.
    var environment: String { get }

    // --- utilitários -----------------------------------------------------

    /// Abre o app Wallet. `false` quando o device não consegue abrir.
    func openWallet() -> Bool
}
