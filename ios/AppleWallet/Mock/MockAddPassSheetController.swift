// ============================================================================
// APPLE WALLET MOCK — SHEET LOOK-ALIKE (só Debug)
// ============================================================================
// Imita PKAddPaymentPassViewController para o fluxo ter interação real do
// usuário. NÃO é UI da Apple: exibe um badge "MOCK" fixo.
//
// Como no PassKit, a sheet fica na tela durante o handshake (carregando) e só
// sai no estado terminal, fechada pelo provider.

#if DEBUG

import UIKit

enum MockAddPassSheetOutcome {
    case confirmed
    case canceled
}

final class MockAddPassSheetController: UIViewController {

    private let config: AWProvisioningRequestConfig
    private let allowDeviceSelection: Bool
    private let completion: (MockAddPassSheetOutcome, Bool) -> Void

    private var watchSwitch: UISwitch?
    private var didFinish = false

    private lazy var stack: UIStackView = {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 16
        stack.alignment = .fill
        stack.translatesAutoresizingMaskIntoConstraints = false
        return stack
    }()

    private lazy var spinner: UIActivityIndicatorView = {
        let spinner = UIActivityIndicatorView(style: .large)
        spinner.translatesAutoresizingMaskIntoConstraints = false
        return spinner
    }()

    private lazy var progressLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15)
        label.textColor = .secondaryLabel
        label.textAlignment = .center
        label.numberOfLines = 0
        return label
    }()

    /// - Parameter allowDeviceSelection: oferece também o Apple Watch, como o
    ///   iOS faz sozinho quando há um Watch pareado.
    init(
        config: AWProvisioningRequestConfig,
        allowDeviceSelection: Bool,
        completion: @escaping (MockAddPassSheetOutcome, Bool) -> Void
    ) {
        self.config = config
        self.allowDeviceSelection = allowDeviceSelection
        self.completion = completion
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) não suportado")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        // Sem swipe-to-dismiss: no PassKit o usuário responde na sheet.
        isModalInPresentation = true

        view.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            stack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24),
            stack.centerYAnchor.constraint(equalTo: view.centerYAnchor),
        ])

        stack.addArrangedSubview(makeMockBadge())
        stack.addArrangedSubview(makeLabel("Adicionar à Apple Wallet", size: 24, weight: .bold))
        stack.addArrangedSubview(makeCardPreview())
        if allowDeviceSelection {
            stack.addArrangedSubview(makeDeviceSelectionRow())
        }
        stack.addArrangedSubview(makeButton("Adicionar cartão", primary: true, action: #selector(didTapConfirm)))
        stack.addArrangedSubview(makeButton("Cancelar", primary: false, action: #selector(didTapCancel)))
    }

    // MARK: - Estados

    func showProgress(_ message: String) {
        DispatchQueue.main.async { [weak self] in
            guard let self, self.isViewLoaded else { return }
            if self.spinner.superview == nil {
                self.stack.arrangedSubviews.forEach { $0.removeFromSuperview() }
                self.stack.addArrangedSubview(self.makeMockBadge())
                self.stack.addArrangedSubview(self.spinner)
                self.stack.addArrangedSubview(self.progressLabel)
                self.spinner.startAnimating()
            }
            self.progressLabel.text = message
        }
    }

    /// Fecha a sheet e chama `completion` depois da animação.
    func dismissSheet(completion: (() -> Void)? = nil) {
        DispatchQueue.main.async { [weak self] in
            guard let self, self.presentingViewController != nil else {
                completion?()
                return
            }
            self.spinner.stopAnimating()
            self.dismiss(animated: true, completion: completion)
        }
    }

    // MARK: - Subviews

    private func makeMockBadge() -> UIView {
        let label = UILabel()
        label.text = "  MOCK — não é UI da Apple  "
        label.font = .monospacedSystemFont(ofSize: 12, weight: .bold)
        label.textColor = .white
        label.backgroundColor = .systemOrange
        label.textAlignment = .center
        label.layer.cornerRadius = 10
        label.clipsToBounds = true
        label.heightAnchor.constraint(equalToConstant: 28).isActive = true
        return label
    }

    private func makeLabel(_ text: String, size: CGFloat, weight: UIFont.Weight) -> UILabel {
        let label = UILabel()
        label.text = text
        label.font = .systemFont(ofSize: size, weight: weight)
        label.textColor = .label
        label.textAlignment = .center
        label.numberOfLines = 0
        return label
    }

    private func makeDetailLabel(_ text: String, bold: Bool) -> UILabel {
        let label = UILabel()
        label.text = text
        label.font = .systemFont(ofSize: bold ? 16 : 14, weight: bold ? .semibold : .regular)
        label.textColor = bold ? .label : .secondaryLabel
        return label
    }

    private func makeCardPreview() -> UIView {
        let card = UIView()
        card.backgroundColor = .secondarySystemBackground
        card.layer.cornerRadius = 14
        card.heightAnchor.constraint(equalToConstant: 110).isActive = true

        let inner = UIStackView()
        inner.axis = .vertical
        inner.spacing = 4
        inner.translatesAutoresizingMaskIntoConstraints = false
        card.addSubview(inner)
        NSLayoutConstraint.activate([
            inner.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
            inner.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
            inner.centerYAnchor.constraint(equalTo: card.centerYAnchor),
        ])

        inner.addArrangedSubview(makeDetailLabel(config.localizedDescription, bold: true))
        inner.addArrangedSubview(makeDetailLabel(config.cardholderName, bold: false))
        inner.addArrangedSubview(
            makeDetailLabel("\(config.paymentNetwork) •••• \(config.primaryAccountSuffix)", bold: false)
        )
        return card
    }

    private func makeDeviceSelectionRow() -> UIView {
        let row = UIView()
        let label = makeDetailLabel("Adicionar também ao Apple Watch", bold: false)
        label.translatesAutoresizingMaskIntoConstraints = false
        row.addSubview(label)

        let toggle = UISwitch()
        toggle.isOn = true
        toggle.translatesAutoresizingMaskIntoConstraints = false
        row.addSubview(toggle)
        watchSwitch = toggle

        NSLayoutConstraint.activate([
            label.leadingAnchor.constraint(equalTo: row.leadingAnchor),
            label.centerYAnchor.constraint(equalTo: row.centerYAnchor),
            toggle.trailingAnchor.constraint(equalTo: row.trailingAnchor),
            toggle.centerYAnchor.constraint(equalTo: row.centerYAnchor),
            row.heightAnchor.constraint(equalToConstant: 44),
        ])
        return row
    }

    private func makeButton(_ title: String, primary: Bool, action: Selector) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        if primary {
            button.setTitleColor(.systemBackground, for: .normal)
            button.titleLabel?.font = .systemFont(ofSize: 17, weight: .semibold)
            button.backgroundColor = .label
            button.layer.cornerRadius = 12
        }
        button.heightAnchor.constraint(equalToConstant: primary ? 50 : 44).isActive = true
        button.addTarget(self, action: action, for: .touchUpInside)
        return button
    }

    // MARK: - Ações

    @objc private func didTapConfirm() {
        finish(.confirmed)
        showProgress("Adicionando cartão…")
    }

    @objc private func didTapCancel() {
        finish(.canceled)
    }

    private func finish(_ outcome: MockAddPassSheetOutcome) {
        guard !didFinish else { return }
        didFinish = true
        let includeWatch =
            allowDeviceSelection && outcome == .confirmed && (watchSwitch?.isOn ?? false)
        completion(outcome, includeWatch)
    }
}

#endif
