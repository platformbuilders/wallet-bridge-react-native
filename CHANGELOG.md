# Changelog

Todas as mudanças relevantes da lib. A partir da próxima release, as entradas
são geradas pelo `release-it` (Conventional Commits, preset angular) a partir
dos commits.

## Unreleased

### ⚠ BREAKING CHANGES

- **React Native 0.80+ com New Architecture.** Os módulos nativos são
  TurboModules. Apps na arquitetura legada ficam na linha 1.x.
- **Eventos fora do `RCTDeviceEventEmitter`.** Listeners registrados direto com
  `DeviceEventEmitter` / `NativeEventEmitter` (`GoogleWalletIntentReceived`,
  `WalletLog`, ...) param de receber eventos. Use os hooks
  (`useGoogleWalletListener`, `useSamsungWalletListener`, `*LogListener`) ou os
  emitters dos services. Ver "Migrating from 1.x" no README.
- **iOS: o pod passa a compilar Swift e linkar `PassKit`, `UIKit` e
  `WatchConnectivity`** para todo app iOS, inclusive os que não usam Apple
  Wallet (`DEFINES_MODULE = YES`). Linkar o PassKit não exige entitlement.

### Features

- **Apple Wallet (iOS, PassKit)** — `AppleWalletModule`:
  - In-App Provisioning: `checkWalletAvailability`, `canAddCard`,
    `startInAppProvisioning`, `submitProvisioningData` (resolve `boolean`; o
    pass chega no evento `onProvisioningResult`), `cancelProvisioning`.
    Esquema de criptografia fixo em `ECC_V2` (sem `encryptionScheme` na config).
    `startInAppProvisioning` não consulta `canAddCard`: decidir se o fluxo
    começa é do app; a lib só rejeita quando o PassKit não consegue seguir.
  - Passes: `listPasses` (`passes(of: .secureElement)`) e `listRemotePasses`
    (`remoteSecureElementPasses`), com DPANID, FPANID, `passActivationState`,
    `isRemote` e `deviceName` como o PassKit devolve; `isCardTokenized`,
    `isWatchPaired`. Juntar as listas e identificar o device é do app.
  - In-App Verification: `activatePass`.
  - Eventos `onProvisioningCertificates`, `onProvisioningResult` e `onLog`, e os
    hooks `useAppleWalletListener` / `useAppleWalletLogListener`.
  - Enums `ApplePassActivationState`, `AppleProvisioningStatus`, `AppleCanAddCardReason`, `AppleWalletErrorCode`.
  - Stub fora do iOS: consultas com valores neutros, ações rejeitam com
    `NOT_SUPPORTED_ON_PLATFORM`, sem `console.warn` no import.
  - Sem API de Wallet Extension: status, pass entries e autorização são do app
    (Swift nativo, outro processo).
- **Mock nativo do Apple Wallet (só Debug)**, com cenários de iPhone + Watch,
  vários pendentes, `DEACTIVATED`, `ACTIVATING` e de erro. Controlado por
  `AppleWalletMockControl`, exportado só em
  `@platformbuilders/wallet-bridge-react-native/testing`. O build Release não
  compila o mock.
- Mocks de Jest do Apple Wallet (`mockAppleWalletModule`, passes locais e remotos)
  em `src/__tests__/__mocks__`.
- Enums também em `@platformbuilders/wallet-bridge-react-native/enums`.
- Evento de log da Apple com nome próprio (`AppleWalletLog`), sem colidir com
  o `WalletLog` de Google/Samsung.

### Tooling

- Example iOS com telas por fluxo (Push, Verificação, Passes) e painel do mock.
- `apple-wallet-app-mock`: simula a Wallet abrindo o app pelo `appLaunchURL`
  (`yarn build:apple-wallet-mock`); o projeto Xcode é gerado, não versionado.
- CI `native-build.yml`: build iOS (Debug e Release, falha se o mock chegar no
  Release) e Android (stubs) quando o PR mexe em código nativo.
