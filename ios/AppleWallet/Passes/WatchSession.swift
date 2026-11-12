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

    /// Intervalo e número de conferências da ativação: ~2 s no total.
    private static let activationCheckInterval = DispatchTimeInterval.milliseconds(100)
    private static let maxActivationChecks = 20

    /// `true` quando há um Apple Watch pareado. A ativação da sessão é
    /// assíncrona e `isPaired` só vale depois dela, então espera a ativação
    /// antes de responder. Confere o estado em vez de depender do callback do
    /// delegate, que pode ser do app. Sem ativação em ~2 s, responde `false`.
    func isPaired(completion: @escaping (Bool) -> Void) {
        checkPaired(attempt: 0, completion: completion)
    }

    private func checkPaired(attempt: Int, completion: @escaping (Bool) -> Void) {
        guard WCSession.isSupported() else {
            completion(false)
            return
        }
        let session = WCSession.default
        if session.activationState == .activated {
            completion(session.isPaired)
            return
        }
        guard attempt < Self.maxActivationChecks else {
            completion(false)
            return
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + Self.activationCheckInterval) {
            self.checkPaired(attempt: attempt + 1, completion: completion)
        }
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
