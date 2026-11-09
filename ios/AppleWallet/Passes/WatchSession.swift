// ============================================================================
// APPLE WATCH — PAREAMENTO
// ============================================================================
// `WCSession.isPaired` diz se o iPhone tem um Apple Watch pareado.
//
// A WCSession é um singleton do app: se o app já tiver um delegate próprio
// (integração com o Watch), ele é mantido e a sessão não é reativada aqui.

import Foundation
import WatchConnectivity

final class WatchSession: NSObject, WCSessionDelegate {

    override init() {
        super.init()
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        guard session.delegate == nil else { return }
        session.delegate = self
        session.activate()
    }

    /// `true` quando há um Apple Watch pareado. Enquanto a ativação
    /// (assíncrona) não completa, devolve `false`.
    var isPaired: Bool {
        guard WCSession.isSupported() else { return false }
        let session = WCSession.default
        guard session.activationState == .activated else { return false }
        return session.isPaired
    }

    // MARK: - WCSessionDelegate

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {}

    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        session.activate()
    }
}
