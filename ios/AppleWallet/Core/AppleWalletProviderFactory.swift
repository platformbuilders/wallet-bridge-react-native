// ============================================================================
// APPLE WALLET — FACTORY DO PROVIDER
// ============================================================================
// Único ponto de criação do provider. A ponte não instancia classe concreta:
// é aqui que uma implementação alternativa (ex.: mock em DEBUG) é registrada.

import Foundation

@objc public final class AppleWalletProviderFactory: NSObject {

    /// Provider padrão. Hoje sempre o real (PassKit).
    ///
    /// Devolve o protocolo, não a classe: o header Swift gerado do pod só
    /// expõe declarações `public`, e o provider concreto é interno.
    @objc public static func defaultProvider() -> AppleWalletProviding {
        RealAppleWalletProvider()
    }

    private override init() { super.init() }
}
