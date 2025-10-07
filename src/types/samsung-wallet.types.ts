// ============================================================================
// TYPES ESPECÍFICOS DO SAMSUNG PAY / SAMSUNG WALLET
// ============================================================================

// Samsung Pay Status (baseado no SpaySdk)
export enum SamsungPayStatus {
  SPAY_NOT_SUPPORTED = 0,
  SPAY_NOT_READY = 1,
  SPAY_READY = 2,
  SPAY_NOT_ALLOWED_TEMPORALLY = 3,
  SPAY_HAS_TRANSIT_CARD = 10,
  SPAY_HAS_NO_TRANSIT_CARD = 11,
}

// Samsung Pay Error Codes (baseado no SpaySdk)
export enum SamsungPayErrorCode {
  ERROR_NONE = 0,
  ERROR_SPAY_INTERNAL = -1,
  ERROR_INVALID_INPUT = -2,
  ERROR_NOT_SUPPORTED = -3,
  ERROR_NOT_FOUND = -4,
  ERROR_ALREADY_DONE = -5,
  ERROR_NOT_ALLOWED = -6,
  ERROR_USER_CANCELED = -7,
  ERROR_PARTNER_SDK_API_LEVEL = -10,
  ERROR_PARTNER_SERVICE_TYPE = -11,
  ERROR_INVALID_PARAMETER = -12,
  ERROR_NO_NETWORK = -21,
  ERROR_SERVER_NO_RESPONSE = -22,
  ERROR_PARTNER_INFO_INVALID = -99,
  ERROR_INITIATION_FAIL = -103,
  ERROR_REGISTRATION_FAIL = -104,
  ERROR_DUPLICATED_SDK_API_CALLED = -105,
  ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION = -300,
  ERROR_SERVICE_ID_INVALID = -301,
  ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION = -302,
  ERROR_PARTNER_APP_SIGNATURE_MISMATCH = -303,
  ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED = -304,
  ERROR_PARTNER_APP_BLOCKED = -305,
  ERROR_USER_NOT_REGISTERED_FOR_DEBUG = -306,
  ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE = -307,
  ERROR_PARTNER_NOT_APPROVED = -308,
  ERROR_UNAUTHORIZED_REQUEST_TYPE = -309,
  ERROR_EXPIRED_OR_INVALID_DEBUG_KEY = -310,
  ERROR_SERVER_INTERNAL = -311,
  ERROR_DEVICE_NOT_SAMSUNG = -350,
  ERROR_SPAY_PKG_NOT_FOUND = -351,
  ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE = -352,
  ERROR_DEVICE_INTEGRITY_CHECK_FAIL = -353,
  ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL = -360,
  ERROR_ANDROID_PLATFORM_CHECK_FAIL = -361,
  ERROR_MISSING_INFORMATION = -354,
  ERROR_SPAY_SETUP_NOT_COMPLETED = -356,
  ERROR_SPAY_APP_NEED_TO_UPDATE = -357,
  ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED = -358,
  ERROR_UNABLE_TO_VERIFY_CALLER = -359,
  ERROR_SPAY_FMM_LOCK = -604,
  ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY = -605,
}

// Samsung Card Brand (baseado no SpaySdk.Brand)
export enum SamsungCardBrand {
  AMERICANEXPRESS = 'AMERICANEXPRESS',
  MASTERCARD = 'MASTERCARD',
  VISA = 'VISA',
  DISCOVER = 'DISCOVER',
  CHINAUNIONPAY = 'CHINAUNIONPAY',
  UNKNOWN_CARD = 'UNKNOWN_CARD',
  OCTOPUS = 'OCTOPUS',
  ECI = 'ECI',
  PAGOBANCOMAT = 'PAGOBANCOMAT',
  ELO = 'ELO',
  MADA = 'MADA',
}

// Samsung Card Type (baseado no Card constants)
export enum SamsungCardType {
  CARD_TYPE_CREDIT_DEBIT = 'PAYMENT',
  CARD_TYPE_GIFT = 'GIFT',
  CARD_TYPE_LOYALTY = 'LOYALTY',
  CARD_TYPE_CREDIT = 'CREDIT',
  CARD_TYPE_DEBIT = 'DEBIT',
  CARD_TYPE_TRANSIT = 'TRANSIT',
  CARD_TYPE_VACCINE_PASS = 'VACCINE_PASS',
}

// Samsung Card Status (baseado no Card constants)
export enum SamsungCardStatus {
  ACTIVE = 'ACTIVE',
  DISPOSED = 'DISPOSED',
  EXPIRED = 'EXPIRED',
  PENDING_ENROLLED = 'ENROLLED',
  PENDING_PROVISION = 'PENDING_PROVISION',
  SUSPENDED = 'SUSPENDED',
  PENDING_ACTIVATION = 'PENDING_ACTIVATION',
}

// Samsung Device Type (baseado no SpaySdk)
export enum SamsungDeviceType {
  PHONE = 'phone',
  GEAR = 'gear',
}

// Samsung Cryptogram Type (baseado no SpaySdk)
export enum SamsungCryptogramType {
  UCAF = 'UCAF',
  ICC = 'ICC',
}

// Samsung Transaction Type (baseado no SpaySdk)
export enum SamsungTransactionType {
  PURCHASE = 'PURCHASE',
  PREAUTHORIZATION = 'PREAUTHORIZATION',
}

// Samsung Pay - Card (baseado na classe Card do SDK e SerializableCard)
export interface SamsungCard {
  // Campos básicos do Card
  cardId: string;
  cardStatus: SamsungCardStatus;
  cardBrand: SamsungCardBrand;

  // Campos do cardInfo Bundle (Samsung Pay específicos)
  last4FPan?: string;
  last4DPan?: string;
  app2AppPayload?: string;
  cardType?: SamsungCardType;
  issuerName?: string;
  isDefaultCard?: string;
  deviceType?: SamsungDeviceType;
  memberID?: string;
  countryCode?: string;
  cryptogramType?: SamsungCryptogramType;
  requireCpf?: string;
  cpfHolderName?: string;
  cpfNumber?: string;
  merchantRefId?: string;
  transactionType?: SamsungTransactionType;

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

// Samsung Pay - CardData para addCardToWallet (compatibilidade)
export interface SamsungCardData {
  payload: string;
  issuerId: string;
  tokenizationProvider: string;
}

// Samsung Pay - Constants (baseado no getConstants)
export interface SamsungWalletConstants {
  SDK_NAME: string;
  useMock: boolean;
  // Constantes do SDK
  SPAY_READY: number;
  SPAY_NOT_READY: number;
  SPAY_NOT_SUPPORTED: number;
  CARD_TYPE_CREDIT_DEBIT: number;
  CARD_TYPE_CREDIT: number;
  CARD_TYPE_DEBIT: number;
  CARD_STATE_ACTIVE: number;
  CARD_STATE_PENDING: number;
  CARD_STATE_SUSPENDED: number;
  PROVIDER_VISA: string;
  PROVIDER_MASTERCARD: string;
  PROVIDER_AMEX: string;
  PROVIDER_ELO: string;
  ERROR_NONE: number;
  ERROR_SDK_NOT_AVAILABLE: number;
  ERROR_INIT_FAILED: number;
  ERROR_CARD_ADD_FAILED: number;
  ERROR_WALLET_NOT_AVAILABLE: number;
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
    progress: (currentCount: number, totalCount: number) => void
  ): Promise<SamsungCard>;

  // Métodos de compatibilidade
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<SamsungWalletInfo>;
  addCardToWallet(cardData: SamsungCardData): Promise<SamsungCard>;
  getCardStatusBySuffix(lastDigits: string): Promise<string>;
  getCardStatusByIdentifier(identifier: string, tsp: string): Promise<string>;
  createWalletIfNeeded(): Promise<boolean>;

  // Constantes
  getConstants(): Promise<SamsungWalletConstants>;
}
