// ============================================================================
// WALLET MOCK — TELA
// ============================================================================
// Monta a URL de In-App Verification e abre o app alvo:
//
//   <scheme>://apple-wallet/verify?deviceAccountIdentifier=<DPANID>
//
// O DPANID vem do pass pendente no app alvo (no example: tela "Passes", em
// modo mock com o cenário MULTIPLE_PENDING). O formato da URL é do app, não da
// lib: no aparelho real ele é o `appLaunchURL` que o emissor grava no pass.

import SwiftUI
import UIKit

struct ContentView: View {

    @State private var scheme: String =
        (Bundle.main.object(forInfoDictionaryKey: "TargetURLScheme") as? String) ?? ""
    @State private var deviceAccountIdentifier = ""
    @State private var status: String?

    private var url: URL? {
        var components = URLComponents()
        components.scheme = scheme.trimmingCharacters(in: .whitespaces)
        components.host = "apple-wallet"
        components.path = "/verify"
        components.queryItems = [
            URLQueryItem(
                name: "deviceAccountIdentifier",
                value: deviceAccountIdentifier.trimmingCharacters(in: .whitespaces)
            ),
        ]
        return components.url
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Text("MOCK — não é a Apple Wallet")
                        .font(.caption.monospaced().bold())
                        .foregroundStyle(.white)
                        .padding(6)
                        .background(Color.orange, in: Capsule())
                }

                Section("App do emissor") {
                    TextField("URL scheme", text: $scheme)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                }

                Section("Pass pendente de ativação") {
                    TextField("deviceAccountIdentifier (DPANID)", text: $deviceAccountIdentifier)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                    Button("Colar da área de transferência") {
                        deviceAccountIdentifier = UIPasteboard.general.string ?? ""
                    }
                }

                Section {
                    Button("Abrir app para verificação") { openTargetApp() }
                        .disabled(scheme.isEmpty || deviceAccountIdentifier.isEmpty)
                    if let url {
                        Text(url.absoluteString)
                            .font(.footnote.monospaced())
                            .foregroundStyle(.secondary)
                    }
                    if let status {
                        Text(status).font(.footnote)
                    }
                }
            }
            .navigationTitle("Wallet Mock")
        }
    }

    private func openTargetApp() {
        guard let url else {
            status = "URL inválida."
            return
        }
        UIApplication.shared.open(url) { opened in
            status = opened
                ? "App aberto."
                : "Não abriu: o app com o scheme '\(scheme)' está instalado?"
        }
    }
}
