// ============================================================================
// APPLE WALLET MOCK — IMPLEMENTAÇÃO (só Debug)
// ============================================================================
// Implementa AppleWalletProviding inteiro, sem PassKit. Existe porque, sem o
// entitlement de provisioning, a sheet da Apple não abre (e no Simulator não
// há Secure Element): o PassKit em si precisa ser substituível para testar.
//
// Apresenta uma sheet look-alike (com badge MOCK) que fica na tela durante o
// handshake, como a da Apple. Nada aqui é compilado em Release.

#if DEBUG

import Foundation
import UIKit

final class MockAppleWalletProvider: NSObject, AppleWalletProviding,
    AppleWalletMockControlling
{
    private static let tag = "AppleWallet-Mock"

    weak var delegate: AppleWalletProviderDelegate?

    var implementationName: String { AWConst.implementationMock }

    var environment: String { AWRuntime.environment }

    // MARK: - Configuração

    private(set) var scenario: AWScenario = .happyPush
    /// Latência padrão das transições simuladas, em ms.
    private static let defaultLatencyMs = 800
    /// Latência simulada das transições, em ms.
    private(set) var latencyMs = defaultLatencyMs
    private var primaryAccountIdentifiers = MockProvisioningFixtures
        .defaultPrimaryAccountIdentifiers()

    // MARK: - Estado

    private var passes: [AWPaymentPass] = []
    private var pendingRequestId: String?
    /// Estágio do provisioning em andamento; `nil` sem provisioning.
    private var pendingStage: AWStatus?
    private var pendingConfig: AWProvisioningRequestConfig?
    private var pendingIncludesWatch = false
    private weak var pendingSheet: MockAddPassSheetController?

    // MARK: - Log e eventos

    private func log(_ level: AWLogLevel, _ message: String) {
        NSLog("[%@][%@] %@", Self.tag, level.rawValue, message)
        delegate?.provider(
            self,
            didLog: AWLogEvent(level: level, tag: Self.tag, message: message)
        )
    }

    /// Em estado terminal a sheet fecha ANTES de o evento chegar ao JS: duas
    /// transições ao mesmo tempo (dismiss da sheet + `Modal` do RN) deixam uma
    /// janela invisível engolindo os toques.
    private func emit(_ event: AWProvisioningResultEvent) {
        guard event.status.isTerminal, let sheet = pendingSheet else {
            delegate?.provider(self, didUpdateProvisioning: event)
            return
        }
        pendingSheet = nil
        sheet.dismissSheet { [weak self] in
            guard let self else { return }
            self.delegate?.provider(self, didUpdateProvisioning: event)
        }
    }

    private func afterLatency(_ block: @escaping () -> Void) {
        DispatchQueue.main.asyncAfter(
            deadline: .now() + .milliseconds(latencyMs),
            execute: block
        )
    }

    // MARK: - Provisioning

    /// Como o PassKit: `false` só quando o device não consegue adicionar.
    func canAddPaymentPass() -> Bool {
        scenario != .entitlementMissing && scenario != .noSecureElement
    }

    /// Como o PassKit: `false` quando o cartão já está em todos os devices
    /// (no mock, quando há um pass local com o FPANID, ou no ALREADY_ADDED).
    func canAddSecureElementPass(primaryAccountIdentifier: String) -> Bool {
        guard canAddPaymentPass(), scenario != .alreadyAdded else { return false }
        return !passes.contains {
            !$0.isRemote && $0.primaryAccountIdentifier == primaryAccountIdentifier
        }
    }

    func startInAppProvisioning(config: AWProvisioningRequestConfig) throws -> String {
        if pendingRequestId != nil {
            log(.error, AWMessage.alreadyInProgress)
            throw AWError.alreadyInProgress()
        }

        try throwIfControllerUnavailable()

        let requestId = UUID().uuidString
        pendingRequestId = requestId
        pendingConfig = config
        pendingStage = .presenting
        pendingIncludesWatch = false

        log(.info, "startInAppProvisioning(\(requestId)) — sheet mock.")
        emit(AWProvisioningResultEvent(requestId: requestId, status: .presenting))

        if scenario == .userCanceled {
            afterLatency { [weak self] in
                self?.finishCanceled(
                    requestId: requestId,
                    errorCode: .userCanceled,
                    message: AWMessage.userCanceled
                )
            }
            return requestId
        }

        presentSheet(config: config, requestId: requestId)
        return requestId
    }

    /// Como no real: sem pré-checagem (isso é do app). Só falha onde o
    /// PassKit falharia, quando o controller não instancia.
    private func throwIfControllerUnavailable() throws {
        switch scenario {
        case .entitlementMissing:
            let message = "[MOCK] PKAddPaymentPassViewController não instanciou: "
                + "entitlement \(AWConst.provisioningEntitlement) ausente."
            log(.error, message)
            throw AWError.make(.entitlementMissing, message)
        case .noSecureElement:
            let message = "[MOCK] PKAddPaymentPassViewController não instanciou: "
                + "device sem Secure Element."
            log(.error, message)
            throw AWError.make(.noSecureElement, message)
        default:
            break
        }
    }

    /// Sheet mock no lugar da do PassKit; confirmar segue para o passo 1.
    private func presentSheet(config: AWProvisioningRequestConfig, requestId: String) {
        let allowWatch = scenario.hasWatch
        DispatchQueue.main.async { [weak self] in
            guard let self else { return }
            let sheet = MockAddPassSheetController(
                config: config,
                allowDeviceSelection: allowWatch
            ) { [weak self] outcome, includeWatch in
                guard let self else { return }
                switch outcome {
                case .canceled:
                    self.finishCanceled(
                        requestId: requestId,
                        errorCode: .userCanceled,
                        message: "Usuário fechou a sheet."
                    )
                case .confirmed:
                    self.proceedToHandshake(requestId: requestId, includeWatch: includeWatch)
                }
            }
            sheet.modalPresentationStyle = .formSheet
            self.pendingSheet = sheet
            self.presentWhenReady(sheet) { [weak self] in
                guard let self else { return }
                self.pendingSheet = nil
                self.log(.error, "Não foi possível apresentar a sheet; seguindo sem ela.")
                self.proceedToHandshake(requestId: requestId, includeWatch: false)
            }
        }
    }

    private func proceedToHandshake(requestId: String, includeWatch: Bool) {
        guard pendingRequestId == requestId else { return }
        pendingIncludesWatch = includeWatch

        afterLatency { [weak self] in
            guard let self, self.pendingRequestId == requestId else { return }
            let certificates = MockProvisioningFixtures.certificatesEvent(requestId: requestId)
            self.pendingStage = .awaitingHost
            self.log(.info, "Passo 1 concluído (\(requestId)). Aguardando o emissor.")
            self.pendingSheet?.showProgress("Aguardando o emissor…")
            self.delegate?.provider(self, didReceiveCertificates: certificates)
            self.emit(AWProvisioningResultEvent(requestId: requestId, status: .awaitingHost))
        }
    }

    func submitProvisioningData(payload: AWSecurePayload) throws {
        guard let requestId = pendingRequestId, pendingStage == .awaitingHost,
              let config = pendingConfig
        else {
            log(.error, AWMessage.notAwaitingStep2)
            throw AWError.notAwaitingStep2()
        }

        let includeWatch = pendingIncludesWatch
        pendingStage = .submitting
        pendingSheet?.showProgress("Adicionando à Wallet…")
        emit(AWProvisioningResultEvent(requestId: requestId, status: .submitting))

        let rejection = invalidReason(for: payload)
        afterLatency { [weak self] in
            guard let self, self.pendingRequestId == requestId else { return }
            if let rejection {
                self.failProvisioning(requestId: requestId, reason: rejection)
            } else {
                self.completeProvisioning(
                    requestId: requestId,
                    config: config,
                    includeWatch: includeWatch
                )
            }
        }
    }

    /// Motivo para o "PassKit" rejeitar o passo 2, ou `nil` se ele aceita.
    private func invalidReason(for payload: AWSecurePayload) -> String? {
        if scenario == .invalidActivationData {
            return "activationData rejeitado pelo PassKit (cenário)."
        }
        let blobs = [payload.encryptedPassData, payload.ephemeralPublicKey, payload.activationData]
        let decodes = blobs.allSatisfy { Data(base64Encoded: $0) != nil }
        return decodes ? nil : "Payload do passo 2 não é Base64 válido."
    }

    private func failProvisioning(requestId: String, reason: String) {
        log(.error, reason)
        clearPending()
        let event = AWProvisioningResultEvent(requestId: requestId, status: .failed)
        event.errorCode = .invalidSecurePayload
        event.errorMessage = reason
        emit(event)
    }

    /// Adiciona o pass do iPhone (e o do Watch, se escolhido na sheet) e emite `.added`.
    private func completeProvisioning(
        requestId: String,
        config: AWProvisioningRequestConfig,
        includeWatch: Bool
    ) {
        let pass = MockProvisioningFixtures.provisionedPass(config: config, remote: false)
        passes.append(pass)
        // Como no PassKit: o evento traz um pass; o do Watch fica só na lista
        // de `listRemotePasses()`.
        if includeWatch {
            let watchPass = MockProvisioningFixtures.provisionedPass(config: config, remote: true)
            watchPass.primaryAccountIdentifier = pass.primaryAccountIdentifier
            passes.append(watchPass)
        }

        clearPending()
        log(.info, "Cartão adicionado à Wallet (\(requestId)).")
        let event = AWProvisioningResultEvent(requestId: requestId, status: .added)
        event.pass = pass
        emit(event)
    }

    func cancelProvisioning() -> Bool {
        guard let requestId = pendingRequestId else {
            log(.warn, AWMessage.nothingToCancel)
            return false
        }
        finishCanceled(
            requestId: requestId,
            errorCode: .systemCanceled,
            message: AWMessage.canceledByApp
        )
        return true
    }

    private func finishCanceled(requestId: String, errorCode: AWErrorCode, message: String) {
        guard pendingRequestId == requestId else { return }
        log(.warn, message)
        clearPending()
        let event = AWProvisioningResultEvent(requestId: requestId, status: .canceled)
        event.errorCode = errorCode
        event.errorMessage = message
        emit(event)
    }

    private func clearPending() {
        pendingRequestId = nil
        pendingConfig = nil
        pendingIncludesWatch = false
        pendingStage = nil
    }

    // MARK: - Passes

    func listPasses() -> [AWPaymentPass] {
        passes.filter { !$0.isRemote }
    }

    func listRemotePasses() -> [AWPaymentPass] {
        passes.filter(\.isRemote)
    }

    var isWatchPaired: Bool { scenario.hasWatch }

    // MARK: - Verification

    func activatePass(
        deviceAccountIdentifier: String,
        activationData: String,
        completion: @escaping (Bool, NSError?) -> Void
    ) {
        guard Data(base64Encoded: activationData) != nil else {
            completion(
                false,
                AWError.make(.invalidSecurePayload, "activationData não é Base64 válido.")
            )
            return
        }

        guard let pass = passes.first(where: {
            $0.deviceAccountIdentifier == deviceAccountIdentifier
        }) else {
            let message = "Nenhum pass com esse deviceAccountIdentifier na Wallet."
            log(.error, message)
            completion(false, AWError.make(.passNotFound, message))
            return
        }

        log(.info, "activatePass() — ativando.")
        let fails = scenario == .activationFailed
        afterLatency { [weak self] in
            if fails {
                self?.log(.error, "Ativação falhou (cenário).")
                completion(false, AWError.make(.activationFailed, "[MOCK] Ativação recusada."))
                return
            }
            pass.activationState = .activated
            self?.log(.info, "Pass ativado.")
            completion(true, nil)
        }
    }

    // MARK: - Utilitários

    func openWallet() -> Bool {
        log(.info, "openWallet() simulado.")
        return true
    }

    // MARK: - AppleWalletMockControlling

    func configure(_ config: [String: Any]) -> [String: Any] {
        if let ids = config["primaryAccountIdentifiers"] as? [String], !ids.isEmpty {
            primaryAccountIdentifiers = ids
        }
        if let value = config["latencyMs"] as? NSNumber {
            latencyMs = max(0, value.intValue)
        }
        if let raw = config["scenario"] as? String, let parsed = AWScenario(rawValue: raw) {
            scenario = parsed
            clearPending()
            passes = MockProvisioningFixtures.seed(
                for: parsed,
                primaryAccountIdentifiers: primaryAccountIdentifiers
            )
            log(.info, "Cenário: \(raw) (\(passes.count) pass(es) semeado(s)).")
        }
        return stateSnapshot()
    }

    func reset() {
        pendingSheet?.dismissSheet()
        pendingSheet = nil
        clearPending()
        scenario = .happyPush
        latencyMs = Self.defaultLatencyMs
        primaryAccountIdentifiers = MockProvisioningFixtures.defaultPrimaryAccountIdentifiers()
        passes.removeAll()
        log(.info, "Mock resetado.")
    }

    func stateSnapshot() -> [String: Any] {
        [
            "implementation": AWConst.implementationMock,
            "scenario": scenario.rawValue,
            "scenarios": AWScenario.allCases.map(\.rawValue),
            "latencyMs": latencyMs,
            "isWatchPaired": isWatchPaired,
            "passes": passes.map { $0.toDictionary() },
            "pendingRequestId": pendingRequestId ?? NSNull(),
            "pendingStage": pendingStage?.rawValue ?? NSNull(),
        ]
    }
}

#endif
