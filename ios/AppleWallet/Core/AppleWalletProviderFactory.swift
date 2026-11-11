// ============================================================================
// APPLE WALLET — FACTORY DO PROVIDER
// ============================================================================
// Único ponto de criação do provider. A ponte não instancia classe concreta.
//
// - Padrão: sempre o real (PassKit), também em Debug.
// - Mock: só em Debug e só quando o app liga por extenso
//   `APPLE_WALLET_USE_MOCK` no Info.plist (como `GOOGLE_WALLET_USE_MOCK` /
//   `SAMSUNG_WALLET_USE_MOCK` no gradle.properties). Com a chave ligada, o
//   módulo `AppleWalletMock` também troca entre real e mock em runtime.
// - Release: sempre o real. As classes do mock nem são compiladas.

import Foundation

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
        return isMockEnabled() ? AWConst.implementationMock : AWConst.implementationReal
        #else
        return AWConst.implementationReal
        #endif
    }

    /// Troca a implementação ativa. Só aceita `mock` em Debug com
    /// `APPLE_WALLET_USE_MOCK` ligado; em Release só aceita `real`.
    /// - Returns: `false` quando o nome não é suportado neste build.
    @objc public static func useImplementation(_ name: String) -> Bool {
        #if DEBUG
        guard name == AWConst.implementationReal
            || (name == AWConst.implementationMock && isMockEnabled())
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

    #if DEBUG
    /// `APPLE_WALLET_USE_MOCK` no Info.plist do app: Boolean, ou String
    /// (`YES` / `true` / `1`) quando vem de uma build setting.
    private static func isMockEnabled() -> Bool {
        switch Bundle.main.object(forInfoDictionaryKey: AWConst.useMockInfoPlistKey) {
        case let flag as Bool:
            return flag
        case let text as String:
            return ["yes", "true", "1"].contains(text.lowercased())
        default:
            return false
        }
    }
    #endif

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
