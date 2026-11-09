// ============================================================================
// APPLE WALLET — FILA DE EVENTOS PARA O JS
// ============================================================================
// Um evento pode nascer antes de o JS ligar o emitter do TurboModule (cold
// start). A fila guarda esses eventos e os entrega quando a ponte conecta o
// "sink". A ponte (AppleWallet.mm) só repassa: liga/desliga, conecta o sink e
// empurra cada evento.

import Foundation

@objc public final class AWEventBuffer: NSObject {

    /// Teto da fila: o suficiente para um cold start. Acima disso, o evento
    /// mais antigo sai.
    private let limit: Int
    private var pending: [NSDictionary] = []
    private var sink: ((NSDictionary) -> Void)?

    /// Só aceita eventos com o listener ligado (`setLogListener`), como
    /// Google/Samsung no Android.
    @objc public var enabled = false

    @objc public init(limit: Int) {
        self.limit = limit
        super.init()
    }

    /// Conecta o destino dos eventos e entrega o que ficou na fila.
    @objc public func attach(_ sink: @escaping (NSDictionary) -> Void) {
        self.sink = sink
        let queued = pending
        pending.removeAll()
        queued.forEach(sink)
    }

    @objc public func push(_ event: NSDictionary) {
        guard enabled else { return }
        if let sink = sink {
            sink(event)
            return
        }
        if pending.count >= limit {
            pending.removeFirst()
        }
        pending.append(event)
    }
}
