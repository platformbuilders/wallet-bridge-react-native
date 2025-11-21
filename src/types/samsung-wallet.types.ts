// ============================================================================
// TYPES ESPECÍFICOS DO SAMSUNG PAY / SAMSUNG WALLET
// ============================================================================

import type {
  SamsungActivationStatus,
  SamsungWalletDataFormat,
  SamsungWalletIntentType,
} from '../enums';

// Samsung Pay - Card (baseado na classe Card do SDK e SerializableCard)
export interface SamsungCard {
  // Campos básicos do Card
  cardId: string;
  cardStatus: string;
  cardBrand: string;

  // Campos do cardInfo Bundle (Samsung Pay específicos)
  last4FPan?: string;
  last4DPan?: string;
  app2AppPayload?: string;
  cardType?: string;
  issuerName?: string;
  isDefaultCard?: string;
  deviceType?: string;
  memberID?: string;
  countryCode?: string;
  cryptogramType?: string;
  requireCpf?: string;
  cpfHolderName?: string;
  cpfNumber?: string;
  merchantRefId?: string;
  transactionType?: string;

  // Campos de compatibilidade (outros SDKs)
  last4?: string;
  tokenizationProvider?: string | number;
  network?: string | number;
  displayName?: string;

  // Bundle cardInfo original (para compatibilidade)
  cardInfo?: Record<string, any>;
}

// Samsung Pay - WalletInfo (baseado no getWalletInfo)
export interface SamsungWalletInfo {
  walletDMId: string;
  deviceId: string;
  walletUserId: string;
}

// Samsung Pay - AddCardInfo (baseado no AddCardInfo)
export interface SamsungAddCardInfo {
  payload: string;
  issuerId: string;
  tokenizationProvider: string;
}

// Samsung Pay - Constants (apenas tipos, valores vêm da bridge)
export interface SamsungWalletConstants {
  SDK_NAME: string;
  SAMSUNG_PAY_PACKAGE: string;
  SAMSUNG_PAY_PLAY_STORE_URL: string;

  // Samsung Pay Status Codes
  SPAY_READY: number;
  SPAY_NOT_READY: number;
  SPAY_NOT_SUPPORTED: number;
  SPAY_NOT_ALLOWED_TEMPORALLY: number;
  SPAY_HAS_TRANSIT_CARD: number;
  SPAY_HAS_NO_TRANSIT_CARD: number;

  // Samsung Card Types (da classe Card)
  CARD_TYPE: string;
  CARD_TYPE_CREDIT_DEBIT: string;
  CARD_TYPE_GIFT: string;
  CARD_TYPE_LOYALTY: string;
  CARD_TYPE_CREDIT: string;
  CARD_TYPE_DEBIT: string;
  CARD_TYPE_TRANSIT: string;
  CARD_TYPE_VACCINE_PASS: string;

  // Samsung Card States (da classe Card)
  ACTIVE: string;
  DISPOSED: string;
  EXPIRED: string;
  PENDING_ENROLLED: string;
  PENDING_PROVISION: string;
  SUSPENDED: string;
  PENDING_ACTIVATION: string;

  // Samsung Tokenization Providers
  PROVIDER_VISA: string;
  PROVIDER_MASTERCARD: string;
  PROVIDER_AMEX: string;
  PROVIDER_DISCOVER: string;
  PROVIDER_PLCC: string;
  PROVIDER_GIFT: string;
  PROVIDER_LOYALTY: string;
  PROVIDER_PAYPAL: string;
  PROVIDER_GEMALTO: string;
  PROVIDER_NAPAS: string;
  PROVIDER_MIR: string;
  PROVIDER_PAGOBANCOMAT: string;
  PROVIDER_VACCINE_PASS: string;
  PROVIDER_MADA: string;
  PROVIDER_ELO: string;

  // Samsung Error Codes
  ERROR_NONE: number;
  ERROR_SPAY_INTERNAL: number;
  ERROR_INVALID_INPUT: number;
  ERROR_NOT_SUPPORTED: number;
  ERROR_NOT_FOUND: number;
  ERROR_ALREADY_DONE: number;
  ERROR_NOT_ALLOWED: number;
  ERROR_USER_CANCELED: number;
  ERROR_PARTNER_SDK_API_LEVEL: number;
  ERROR_PARTNER_SERVICE_TYPE: number;
  ERROR_INVALID_PARAMETER: number;
  ERROR_NO_NETWORK: number;
  ERROR_SERVER_NO_RESPONSE: number;
  ERROR_PARTNER_INFO_INVALID: number;
  ERROR_INITIATION_FAIL: number;
  ERROR_REGISTRATION_FAIL: number;
  ERROR_DUPLICATED_SDK_API_CALLED: number;
  ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION: number;
  ERROR_SERVICE_ID_INVALID: number;
  ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION: number;
  ERROR_PARTNER_APP_SIGNATURE_MISMATCH: number;
  ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED: number;
  ERROR_PARTNER_APP_BLOCKED: number;
  ERROR_USER_NOT_REGISTERED_FOR_DEBUG: number;
  ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE: number;
  ERROR_PARTNER_NOT_APPROVED: number;
  ERROR_UNAUTHORIZED_REQUEST_TYPE: number;
  ERROR_EXPIRED_OR_INVALID_DEBUG_KEY: number;
  ERROR_SERVER_INTERNAL: number;
  ERROR_DEVICE_NOT_SAMSUNG: number;
  ERROR_SPAY_PKG_NOT_FOUND: number;
  ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE: number;
  ERROR_DEVICE_INTEGRITY_CHECK_FAIL: number;
  ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL: number;
  ERROR_ANDROID_PLATFORM_CHECK_FAIL: number;
  ERROR_MISSING_INFORMATION: number;
  ERROR_SPAY_SETUP_NOT_COMPLETED: number;
  ERROR_SPAY_APP_NEED_TO_UPDATE: number;
  ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED: number;
  ERROR_UNABLE_TO_VERIFY_CALLER: number;
  ERROR_SPAY_FMM_LOCK: number;
  ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY: number;
}

// Samsung Wallet - Evento de Intent
export interface SamsungWalletIntentEvent {
  action: string;
  type: SamsungWalletIntentType;
  data?: string; // Dados decodificados (string normal)
  dataFormat?: SamsungWalletDataFormat;
  callingPackage?: string;
  originalData?: string; // Dados originais em base64
  error?: string;
  extras?: Record<string, any>;
  // Campos específicos do Samsung (Mastercard/Visa)
  cardType?: string;
  // Campos específicos do Mastercard
  paymentAppProviderId?: string;
  paymentAppInstanceId?: string;
  tokenUniqueReference?: string;
  accountPanSuffix?: string;
  accountExpiry?: string;
  // Campos específicos do Visa
  panId?: string;
  trId?: string;
  tokenReferenceId?: string;
  last4Digits?: string;
  deviceId?: string;
  walletAccountId?: string;
}

// Samsung Wallet - Evento de Log
export interface SamsungWalletLogEvent {
  level: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'VERBOSE';
  tag: string;
  message: string;
  error?: string;
  stackTrace?: string;
}

// Samsung Pay - Interface do Módulo (baseado nos métodos do SamsungWalletModule)
export interface SamsungWalletSpec {
  // Métodos principais do Samsung Pay
  init(serviceId: string): Promise<boolean>;
  getSamsungPayStatus(): Promise<number>;
  goToUpdatePage(): void;
  activateSamsungPay(): void;
  getAllCards(): Promise<SamsungCard[]>;
  getWalletInfo(): Promise<SamsungWalletInfo>;
  addCard(
    payload: string,
    issuerId: string,
    tokenizationProvider: string,
    cardType: string,
  ): Promise<SamsungCard>;

  // Métodos de compatibilidade
  checkWalletAvailability(): Promise<boolean>;

  // Constantes
  getConstants(): SamsungWalletConstants;

  // Métodos de listener de intent
  setIntentListener(): Promise<boolean>;
  removeIntentListener(): Promise<boolean>;

  // Método de resultado de ativação
  setActivationResult(
    status: SamsungActivationStatus,
    activationCode?: string,
  ): Promise<boolean>;

  // Método para finalizar atividade
  finishActivity(): Promise<boolean>;

  // Método para abrir wallet
  openWallet(): Promise<boolean>;

  // Métodos de log listener
  setLogListener(): Promise<boolean>;
  removeLogListener(): Promise<boolean>;
}
