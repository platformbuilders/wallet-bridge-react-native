// ============================================================================
// APPLE WALLET — IN-APP PROVISIONING (PKAddPaymentPassViewController)
// ============================================================================
// Fluxo push:
//   1. startInAppProvisioning → apresenta a sheet do PassKit;
//   2. PassKit devolve certificados + nonce (delegate) → evento para o JS;
//   3. o app chama o emissor e devolve os 3 blobs em submitProvisioningData;
//   4. PassKit conclui → evento de resultado com o pass.
//
// A cripto nunca acontece no device: os blobs vêm do emissor.
// Sem o entitlement `com.apple.developer.payment-pass-provisioning`, o
// controller não instancia e o fluxo falha com ENTITLEMENT_MISSING.

import Foundation
import PassKit

extension RealAppleWalletProvider {

    /// `PKAddPaymentPassViewController.canAddPaymentPass()`.
    func canAddPaymentPass() -> Bool {
        PKAddPaymentPassViewController.canAddPaymentPass()
    }

    /// `PKPassLibrary.canAddSecureElementPass(primaryAccountIdentifier:)`.
    ///
    /// Repassado como o PassKit devolve. A Apple avisa que, sem o
    /// entitlement de provisioning, o método responde `true` sempre.
    func canAddSecureElementPass(primaryAccountIdentifier: String) -> Bool {
        PKPassLibrary().canAddSecureElementPass(primaryAccountIdentifier: primaryAccountIdentifier)
    }

    func startInAppProvisioning(config: AWProvisioningRequestConfig) throws -> String {
        if pendingRequestId != nil {
            let message = "Já existe um provisioning em andamento."
            log("ERROR", message)
            throw AWError.make(.alreadyInProgress, message)
        }

        // Sem pré-checagem: decidir se o fluxo deve começar (cartão já na
        // Wallet, pendente de ativação...) é do app. A lib só falha quando o
        // próprio PassKit não consegue seguir.

        // ECC_V2 fixo. A Apple define EV_ECC_V2 como esquema obrigatório; RSA
        // exige liberação da Apple e muda o formato do payload inteiro. Se um
        // dia houver escolha, ela entra aqui e o caso RSA precisa falhar alto.
        guard let pkConfig = PKAddPaymentPassRequestConfiguration(encryptionScheme: .ECC_V2)
        else {
            let message = "PKAddPaymentPassRequestConfiguration não instanciou."
            log("ERROR", message)
            throw AWError.make(.invalidConfiguration, message)
        }

        pkConfig.cardholderName = config.cardholderName
        pkConfig.primaryAccountSuffix = config.primaryAccountSuffix
        pkConfig.localizedDescription = config.localizedDescription
        pkConfig.paymentNetwork = PKPaymentNetwork(rawValue: config.paymentNetwork)
        pkConfig.style = .payment
        if let identifier = config.primaryAccountIdentifier {
            pkConfig.primaryAccountIdentifier = identifier
        }
        // Label "expiration" e formato "MM/YY" vêm do sample da Apple
        // (não há constante no PassKit). Enviado quando o app informa.
        if let expiration = config.expiration {
            pkConfig.cardDetails.append(
                PKLabeledValue(label: "expiration", value: expiration)
            )
        }

        guard
            let controller = PKAddPaymentPassViewController(
                requestConfiguration: pkConfig,
                delegate: self
            )
        else {
            // O PassKit não diz o motivo. No Simulator não há Secure Element;
            // em device, a causa esperada é o entitlement ausente.
            #if targetEnvironment(simulator)
            let message = "PKAddPaymentPassViewController não instanciou: "
                + "Simulator não tem Secure Element."
            log("ERROR", message)
            throw AWError.make(.noSecureElement, message)
            #else
            let message = "PKAddPaymentPassViewController não instanciou. O "
                + "entitlement \(AWConst.provisioningEntitlement) precisa estar "
                + "no provisioning profile do app."
            log("ERROR", message)
            throw AWError.make(.entitlementMissing, message)
            #endif
        }

        let requestId = UUID().uuidString
        pendingRequestId = requestId
        pendingController = controller

        log("INFO", "startInAppProvisioning(\(requestId)) — apresentando a sheet.")
        emit(AWProvisioningResultEvent(requestId: requestId, status: .presenting))

        DispatchQueue.main.async { [weak self] in
            self?.presentWhenReady(controller) { [weak self] in
                guard let self, self.pendingRequestId == requestId else { return }
                let message = "Nenhum view controller disponível para apresentar a sheet."
                self.log("ERROR", message)
                self.clearPending()
                let event = AWProvisioningResultEvent(requestId: requestId, status: .failed)
                event.errorCode = .unknown
                event.errorMessage = message
                self.emit(event)
            }
        }

        return requestId
    }

    func submitProvisioningData(payload: AWSecurePayload) throws {
        guard let handler = pendingRequestHandler, let requestId = pendingRequestId else {
            let message = "Nenhum provisioning aguardando o passo 2. "
                + "Chame startInAppProvisioning antes."
            log("ERROR", message)
            throw AWError.make(.notInProgress, message)
        }

        guard
            let encryptedPassData = Data(base64Encoded: payload.encryptedPassData),
            let ephemeralPublicKey = Data(base64Encoded: payload.ephemeralPublicKey),
            let activationData = Data(base64Encoded: payload.activationData)
        else {
            let message = "Payload do passo 2 não é Base64 válido."
            log("ERROR", message)
            throw AWError.make(.invalidSecurePayload, message)
        }

        let request = PKAddPaymentPassRequest()
        request.encryptedPassData = encryptedPassData
        request.ephemeralPublicKey = ephemeralPublicKey
        request.activationData = activationData

        emit(AWProvisioningResultEvent(requestId: requestId, status: .submitting))
        log("INFO", "Passo 2 entregue ao PassKit (\(requestId)).")

        pendingRequestHandler = nil
        handler(request)
    }

    func cancelProvisioning() -> Bool {
        guard let requestId = pendingRequestId else {
            log("WARN", "cancelProvisioning() sem provisioning em andamento.")
            return false
        }

        let controller = pendingController
        clearPending()
        DispatchQueue.main.async {
            controller?.dismiss(animated: true)
        }

        let event = AWProvisioningResultEvent(requestId: requestId, status: .canceled)
        event.errorCode = .systemCanceled
        event.errorMessage = "Provisioning cancelado pelo app."
        emit(event)
        return true
    }
}

// ============================================================================
// PKAddPaymentPassViewControllerDelegate
// ============================================================================

extension RealAppleWalletProvider: PKAddPaymentPassViewControllerDelegate {

    func addPaymentPassViewController(
        _ controller: PKAddPaymentPassViewController,
        generateRequestWithCertificateChain certificates: [Data],
        nonce: Data,
        nonceSignature: Data,
        completionHandler handler: @escaping (PKAddPaymentPassRequest) -> Void
    ) {
        // Passo 1: guarda o handler e entrega o material ao JS, que repassa
        // ao emissor.
        let requestId = pendingRequestId ?? ""
        pendingRequestHandler = handler

        let event = AWProvisioningCertificatesEvent(
            requestId: requestId,
            certificates: certificates.map { $0.base64EncodedString() },
            nonce: nonce.base64EncodedString(),
            nonceSignature: nonceSignature.base64EncodedString()
        )

        log("INFO", "Passo 1 concluído (\(requestId)). Aguardando o emissor.")
        delegate?.provider(self, didReceiveCertificates: event)
        emit(AWProvisioningResultEvent(requestId: requestId, status: .awaitingHost))
    }

    func addPaymentPassViewController(
        _ controller: PKAddPaymentPassViewController,
        didFinishAdding pass: PKPaymentPass?,
        error: Error?
    ) {
        let requestId = pendingRequestId ?? ""
        clearPending()
        DispatchQueue.main.async {
            controller.dismiss(animated: true)
        }

        if let error {
            let nsError = error as NSError
            let canceled = nsError.code == PKAddPaymentPassError.userCancelled.rawValue
            log("ERROR", "PassKit concluiu com erro (\(nsError.code)).")
            let event = AWProvisioningResultEvent(
                requestId: requestId,
                status: canceled ? .canceled : .failed
            )
            event.errorCode = canceled ? .userCanceled : .unknown
            event.errorMessage = nsError.localizedDescription
            emit(event)
            return
        }

        guard let pass else {
            log("WARN", "Usuário cancelou a adição do cartão.")
            let event = AWProvisioningResultEvent(requestId: requestId, status: .canceled)
            event.errorCode = .userCanceled
            event.errorMessage = "Usuário cancelou a adição do cartão."
            emit(event)
            return
        }

        log("INFO", "Cartão adicionado à Wallet (\(requestId)).")
        let event = AWProvisioningResultEvent(requestId: requestId, status: .added)
        event.pass = Self.makePass(pass)
        emit(event)
    }
}
