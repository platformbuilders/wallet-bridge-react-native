// ============================================================================
// APPLE WALLET — FACTORY DO PROVIDER
// ============================================================================
// Único ponto de criação do provider. A ponte não instancia classe concreta.
//
// - Release: sempre o real (PassKit). As classes do mock nem são compiladas.
// - Debug: o mock é o padrão quando o PassKit não consegue adicionar cartão
//   (Simulator, ou device sem o entitlement); senão, o real. O módulo
//   `AppleWalletMock` troca a implementação em tempo de execução.

import Foundation
import PassKit

@objc public final class AppleWalletProviderFactory: NSObject {

    private static var shared: AppleWalletProviding?

    /// Provider ativo. Devolve o protocolo: o header Swift gerado do pod só
    /// expõe declarações `public`, e as classes concretas são internas.
    @objc public static func currentProvider() -> AppleWalletProviding {
        if let shared { return shared }
        let provider = makeProvider(named: defaultImplementationName())
        shared = provider
        return provider
    }

    @objc public static func defaultImplementationName() -> String {
        #if DEBUG
        return PKAddPaymentPassViewController.canAddPaymentPass()
            ? AWConst.implementationReal
            : AWConst.implementationMock
        #else
        return AWConst.implementationReal
        #endif
    }

    /// Troca a implementação ativa. Em Release só aceita `real`.
    /// - Returns: `false` quando o nome não é suportado neste build.
    @objc public static func useImplementation(_ name: String) -> Bool {
        #if DEBUG
        guard name == AWConst.implementationReal || name == AWConst.implementationMock
        else { return false }
        #else
        guard name == AWConst.implementationReal else { return false }
        #endif

        if shared?.implementationName == name { return true }
        let delegate = shared?.delegate
        let provider = makeProvider(named: name)
        provider.delegate = delegate
        shared = provider
        NotificationCenter.default.post(
            name: AWConst.providerDidChangeNotification,
            object: provider
        )
        return true
    }

    /// Controles do mock, ou `nil` quando o real está ativo (e sempre em Release).
    @objc public static func mockControls() -> AppleWalletMockControlling? {
        currentProvider() as? AppleWalletMockControlling
    }

    private static func makeProvider(named name: String) -> AppleWalletProviding {
        #if DEBUG
        if name == AWConst.implementationMock {
            return MockAppleWalletProvider()
        }
        #endif
        return RealAppleWalletProvider()
    }

    private override init() { super.init() }
}
