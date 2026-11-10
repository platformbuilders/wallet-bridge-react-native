// ============================================================================
// APPLE WALLET ENUMS
// ============================================================================
// Espelham os enums Swift de ios/AppleWallet/Core/AppleWalletTypes.swift.
// Os valores são estáveis: o app traduz para o contrato do próprio backend.

/** Estado de ativação do pass (`PKSecureElementPass.passActivationState`). */
export enum ApplePassActivationState {
  ACTIVATED = 'ACTIVATED',
  REQUIRES_ACTIVATION = 'REQUIRES_ACTIVATION',
  ACTIVATING = 'ACTIVATING',
  SUSPENDED = 'SUSPENDED',
  DEACTIVATED = 'DEACTIVATED',
}

/** Estágios do In-App Provisioning, entregues no evento de resultado. */
export enum AppleProvisioningStatus {
  IDLE = 'IDLE',
  /** A sheet do PassKit está sendo apresentada. */
  PRESENTING = 'PRESENTING',
  /** Passo 1 entregue: o app precisa chamar o emissor. */
  AWAITING_HOST = 'AWAITING_HOST',
  /** Passo 2 entregue ao PassKit. */
  SUBMITTING = 'SUBMITTING',
  ADDED = 'ADDED',
  CANCELED = 'CANCELED',
  FAILED = 'FAILED',
}

/** Motivo da resposta de `canAddCard`. */
export enum AppleCanAddCardReason {
  AVAILABLE = 'AVAILABLE',
  ENTITLEMENT_MISSING = 'ENTITLEMENT_MISSING',
  NO_SECURE_ELEMENT = 'NO_SECURE_ELEMENT',
  ALREADY_ADDED = 'ALREADY_ADDED',
  /** Já está na Wallet, mas pendente de ativação: o app oferece a ativação. */
  REQUIRES_ACTIVATION = 'REQUIRES_ACTIVATION',
  UNKNOWN = 'UNKNOWN',
}

/** `code` dos erros (reject e evento de resultado). */
export enum AppleWalletErrorCode {
  UNKNOWN = 'UNKNOWN',
  USER_CANCELED = 'USER_CANCELED',
  SYSTEM_CANCELED = 'SYSTEM_CANCELED',
  ENTITLEMENT_MISSING = 'ENTITLEMENT_MISSING',
  NO_SECURE_ELEMENT = 'NO_SECURE_ELEMENT',
  INVALID_CONFIGURATION = 'INVALID_CONFIGURATION',
  INVALID_SECURE_PAYLOAD = 'INVALID_SECURE_PAYLOAD',
  HOST_TIMEOUT = 'HOST_TIMEOUT',
  NOT_IN_PROGRESS = 'NOT_IN_PROGRESS',
  ALREADY_IN_PROGRESS = 'ALREADY_IN_PROGRESS',
  PASS_NOT_FOUND = 'PASS_NOT_FOUND',
  ACTIVATION_FAILED = 'ACTIVATION_FAILED',
  PASS_LIBRARY_UNAVAILABLE = 'PASS_LIBRARY_UNAVAILABLE',
  NOT_SUPPORTED_ON_PLATFORM = 'NOT_SUPPORTED_ON_PLATFORM',
}
