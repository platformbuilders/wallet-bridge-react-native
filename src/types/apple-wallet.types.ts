// ============================================================================
// APPLE WALLET TYPES
// ============================================================================
// Contrato público do módulo Apple Wallet (iOS). O TurboModule implementa esta
// spec (src/specs/NativeAppleWallet.ts) e o stub de Android também.
//
// A lib só fala PassKit: não chama backend, não decide elegibilidade e não
// conhece emissor. O app chama o emissor com os dados do passo 1 e devolve os
// blobs do passo 2 em `submitProvisioningData`.

import type {
  AppleCanAddCardReason,
  AppleDeviceType,
  ApplePassActivationState,
  AppleProvisioningStatus,
  AppleWalletErrorCode,
} from '../enums/apple-wallet.enum';

export type AppleWalletLogEvent = {
  level: string;
  tag: string;
  message: string;
};

export type AppleWalletConstants = {
  PROVISIONING_ENTITLEMENT: string;
  APPLE_LEAF_CERT_OID: string;
  WALLET_URL_SCHEME: string;
  EVENT_LOG: string;
};

export interface AppleWalletSubscription {
  remove(): void;
}

/** Dados do cartão para a sheet do PassKit (`PKAddPaymentPassRequestConfiguration`). */
export interface AppleProvisioningConfig {
  cardholderName: string;
  /** Últimos 4 dígitos do PAN. */
  primaryAccountSuffix: string;
  /** Nome do cartão exibido na sheet. */
  localizedDescription: string;
  /** Valor de `PKPaymentNetwork` (ex.: `Visa`, `MasterCard`, `Elo`). */
  paymentNetwork: string;
  /** FPANID. Quando informado, a Wallet usa para detectar cartão já adicionado. */
  primaryAccountIdentifier?: string;
  /** Validade `MM/YY`, enviada em `cardDetails`. */
  expiration?: string;
}

/**
 * Passo 1 do handshake, entregue pelo PassKit. Base64 padrão (RFC 4648 §4);
 * `certificates` traz o leaf primeiro e a sub-CA depois.
 */
export interface AppleProvisioningCertificatesEvent {
  requestId: string;
  certificates: string[];
  nonce: string;
  nonceSignature: string;
}

/** Passo 2 do handshake: os 3 blobs que o emissor devolve, em Base64. */
export interface AppleSecurePayload {
  activationData: string;
  encryptedPassData: string;
  ephemeralPublicKey: string;
}

/** Pass de pagamento na Wallet (`PKSecureElementPass`). */
export interface ApplePaymentPass {
  /** FPANID: identifica o cartão (o mesmo no iPhone e no Watch). */
  primaryAccountIdentifier: string;
  primaryAccountNumberSuffix: string;
  /** DPANID: identifica o token neste device. Argumento de `activatePass`. */
  deviceAccountIdentifier: string;
  deviceAccountNumberSuffix: string;
  serialNumber: string;
  passTypeIdentifier: string;
  passActivationState: ApplePassActivationState;
  deviceType: AppleDeviceType;
  /** `true` quando o pass está em um device pareado. */
  isRemote: boolean;
  deviceName: string;
  localizedDescription: string;
}

export interface AppleCanAddCardResult {
  canAdd: boolean;
  reason: AppleCanAddCardReason;
  message: string;
  /** DPANID do pass existente (`ALREADY_ADDED` / `REQUIRES_ACTIVATION`). */
  deviceAccountIdentifier?: string;
}

/** Transição de estágio ou resultado final do provisioning. */
export interface AppleProvisioningResultEvent {
  requestId: string;
  status: AppleProvisioningStatus;
  /** Pass adicionado (status `ADDED`). */
  pass?: ApplePaymentPass;
  /** Passes adicionados junto (ex.: o mesmo cartão no Apple Watch). */
  additionalPasses: ApplePaymentPass[];
  errorCode?: AppleWalletErrorCode;
  errorMessage?: string;
}

/** Erro de um método do módulo: `code` é um AppleWalletErrorCode. */
export interface AppleWalletError extends Error {
  code: AppleWalletErrorCode | string;
}

export interface AppleWalletSpec {
  getConstants(): AppleWalletConstants;

  /** `ios-device` ou `ios-simulator`; `android` no stub. */
  getEnvironment(): Promise<string>;

  /** Abre o app Wallet. `false` se não for possível neste device. */
  openWallet(): Promise<boolean>;

  // --- Provisioning -------------------------------------------------------

  /** `true` quando o device consegue adicionar cartão (sem olhar um cartão específico). */
  checkWalletAvailability(): Promise<boolean>;

  /** Disponibilidade para um cartão (FPANID), com o motivo da negativa. */
  canAddCard(
    primaryAccountIdentifier?: string | null,
  ): Promise<AppleCanAddCardResult>;

  /**
   * Apresenta a sheet do PassKit e resolve com o `requestId`. Os passos
   * seguintes chegam por `onProvisioningCertificates` e `onProvisioningResult`.
   * Não consulta `canAddCard`: decidir se o fluxo deve começar (cartão já na
   * Wallet, pendente de ativação) é do app. Rejeita só quando o PassKit não
   * consegue seguir: `ENTITLEMENT_MISSING`, `NO_SECURE_ELEMENT`,
   * `ALREADY_IN_PROGRESS` ou `INVALID_CONFIGURATION`.
   */
  startInAppProvisioning(config: AppleProvisioningConfig): Promise<string>;

  /**
   * Entrega o passo 2 ao PassKit. Resolve `true` quando o PassKit recebeu os
   * dados; o pass adicionado chega em `onProvisioningResult` (status `ADDED`).
   * Rejeita com `NOT_IN_PROGRESS` ou `INVALID_SECURE_PAYLOAD`.
   */
  submitProvisioningData(payload: AppleSecurePayload): Promise<boolean>;

  /** Cancela o provisioning em andamento. `false` se não havia nenhum. */
  cancelProvisioning(): Promise<boolean>;

  // --- Passes -------------------------------------------------------------

  /** Passes do iPhone e dos devices pareados (Apple Watch). */
  listPasses(): Promise<ApplePaymentPass[]>;

  /** `true` se o cartão (FPANID) já está na Wallet, em qualquer device. */
  isCardTokenized(primaryAccountIdentifier: string): Promise<boolean>;

  isWatchPaired(): Promise<boolean>;

  // --- Verification -------------------------------------------------------

  /**
   * Ativa um pass `REQUIRES_ACTIVATION` com o `activationData` do emissor
   * (Base64). Rejeita com `PASS_NOT_FOUND`, `INVALID_SECURE_PAYLOAD` ou
   * `ACTIVATION_FAILED`.
   */
  activatePass(
    deviceAccountIdentifier: string,
    activationData: string,
  ): Promise<boolean>;

  // --- Eventos ------------------------------------------------------------

  /** Passo 1: repassar ao emissor e devolver o passo 2 em `submitProvisioningData`. */
  onProvisioningCertificates(
    listener: (event: AppleProvisioningCertificatesEvent) => void,
  ): AppleWalletSubscription;

  onProvisioningResult(
    listener: (event: AppleProvisioningResultEvent) => void,
  ): AppleWalletSubscription;

  setLogListener(): Promise<boolean>;
  removeLogListener(): Promise<boolean>;

  /** Assina os logs de diagnóstico (só emitidos em build Debug). */
  onLog(
    listener: (event: AppleWalletLogEvent) => void,
  ): AppleWalletSubscription;
}
