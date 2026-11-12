// ============================================================================
// WALLET MOCK — APP
// ============================================================================
// Simula a Apple Wallet abrindo o app do emissor pelo `appLaunchURL` do pass
// (In-App Verification). No aparelho real é a Wallet que faz isso, quando o
// usuário toca em um cartão pendente de ativação.
//
// Ferramenta de desenvolvimento: só Simulator, sem dado real. O app alvo trata
// a URL pelo `Linking` e chama `AppleWalletModule.activatePass`.

import SwiftUI

@main
struct AppleWalletMockApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
