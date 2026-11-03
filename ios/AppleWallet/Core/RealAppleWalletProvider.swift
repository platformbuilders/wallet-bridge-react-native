// ============================================================================
// APPLE WALLET — IMPLEMENTAÇÃO REAL (PassKit)
// ============================================================================
// Classe base. As features entram como extensions nas pastas Provisioning/,
// Verification/ e Passes/; o estado que elas compartilham fica aqui, porque
// extension não guarda propriedade armazenada.
//
// REGRA: nunca fingir sucesso. Ou faz de verdade, ou falha com o erro real.

import Foundation
import UIKit

final class RealAppleWalletProvider: NSObject, AppleWalletProviding {

    static let tag = "AppleWallet-Real"

    weak var delegate: AppleWalletProviderDelegate?

    var implementationName: String { AWConst.implementationReal }

    var environment: String {
        #if targetEnvironment(simulator)
        return "ios-simulator"
        #else
        return "ios-device"
        #endif
    }

    // MARK: - Log

    func log(_ level: String, _ message: String) {
        NSLog("[%@][%@] %@", Self.tag, level, message)
        delegate?.provider(
            self,
            didLog: AWLogEvent(level: level, tag: Self.tag, message: message)
        )
    }

    // MARK: - Utilitários

    func openWallet() -> Bool {
        guard let url = URL(string: AWConst.walletURLScheme),
              UIApplication.shared.canOpenURL(url)
        else {
            log("WARN", "Não foi possível abrir o app Wallet neste device.")
            return false
        }
        DispatchQueue.main.async {
            UIApplication.shared.open(url)
        }
        return true
    }
}
