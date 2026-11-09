// ============================================================================
// APPLE WALLET — IN-APP VERIFICATION
// ============================================================================
// A Wallet abre o app pelo `appLaunchURL` do pass (o app recebe a URL pelo
// `Linking`; a lib não trata URL). Depois do ID&V, o app pede ao emissor o
// `activationData` e chama `activatePass` com o DPANID do pass.

import Foundation
import PassKit

extension RealAppleWalletProvider {

    func activatePass(
        deviceAccountIdentifier: String,
        activationData: String,
        completion: @escaping (Bool, NSError?) -> Void
    ) {
        guard PKPassLibrary.isPassLibraryAvailable() else {
            completion(
                false,
                AWError.make(.passLibraryUnavailable, "PKPassLibrary indisponível neste device.")
            )
            return
        }

        guard let data = Data(base64Encoded: activationData) else {
            completion(
                false,
                AWError.make(.invalidSecurePayload, "activationData não é Base64 válido.")
            )
            return
        }

        guard
            let pass = Self.allSecureElementPasses().first(where: {
                $0.deviceAccountIdentifier == deviceAccountIdentifier
            })
        else {
            let message = "Nenhum pass com esse deviceAccountIdentifier na Wallet."
            log("ERROR", message)
            completion(false, AWError.make(.passNotFound, message))
            return
        }

        log("INFO", "activatePass() — chamando o PassKit.")
        PKPassLibrary().activate(pass, activationData: data) { [weak self] success, error in
            DispatchQueue.main.async {
                if let error {
                    self?.log("ERROR", "Ativação falhou: \(error.localizedDescription)")
                    completion(
                        false,
                        AWError.make(.activationFailed, error.localizedDescription)
                    )
                    return
                }
                self?.log("INFO", "Ativação concluída: \(success).")
                completion(
                    success,
                    success ? nil
                        : AWError.make(.activationFailed, "O PassKit não ativou o pass.")
                )
            }
        }
    }
}
