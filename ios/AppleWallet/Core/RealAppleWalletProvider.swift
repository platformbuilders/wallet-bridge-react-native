// ============================================================================
// APPLE WALLET — IMPLEMENTAÇÃO REAL (PassKit)
// ============================================================================
// Classe base. As features entram como extensions nas pastas Provisioning/,
// Verification/ e Passes/; o estado que elas compartilham fica aqui, porque
// extension não guarda propriedade armazenada. O estado é `internal` (não
// `private`) para as extensions em outros arquivos o alcançarem; o header
// Swift gerado só expõe o que é `public`.
//
// REGRA: nunca fingir sucesso. Ou faz de verdade, ou falha com o erro real.
// REGRA: log sem dado de cartão (PAN, DPAN, FPANID, sufixos, payloads).

import Foundation
import PassKit
import UIKit

final class RealAppleWalletProvider: NSObject, AppleWalletProviding {

    static let tag = "AppleWallet-Real"

    weak var delegate: AppleWalletProviderDelegate?

    var implementationName: String { AWConst.implementationReal }

    var environment: String { AWRuntime.environment }

    // MARK: - Estado do provisioning em andamento

    /// Handler entregue pelo PassKit no passo 1; guardado até o passo 2
    /// chegar do emissor. É o que amarra as duas metades do handshake.
    var pendingRequestHandler: ((PKAddPaymentPassRequest) -> Void)?
    var pendingRequestId: String?
    var pendingController: PKAddPaymentPassViewController?

    func clearPending() {
        pendingRequestId = nil
        pendingRequestHandler = nil
        pendingController = nil
    }

    // MARK: - Apple Watch

    /// Criado no primeiro uso: ativar a WCSession à toa interferiria em um
    /// app que tenha a própria integração com o Watch.
    lazy var watchSession = WatchSession()

    // MARK: - Log e eventos

    /// Log de diagnóstico: só em build Debug, como o `WalletLogger` do Android.
    func log(_ level: AWLogLevel, _ message: String) {
        #if DEBUG
        NSLog("[%@][%@] %@", Self.tag, level.rawValue, message)
        delegate?.provider(
            self,
            didLog: AWLogEvent(level: level, tag: Self.tag, message: message)
        )
        #endif
    }

    func emit(_ event: AWProvisioningResultEvent) {
        delegate?.provider(self, didUpdateProvisioning: event)
    }

    // MARK: - Utilitários

    func openWallet() -> Bool {
        guard let url = URL(string: AWConst.walletURLScheme),
              UIApplication.shared.canOpenURL(url)
        else {
            log(.warn, "Não foi possível abrir o app Wallet neste device.")
            return false
        }
        log(.info, "Abrindo o app Wallet.")
        DispatchQueue.main.async {
            UIApplication.shared.open(url)
        }
        return true
    }
}
