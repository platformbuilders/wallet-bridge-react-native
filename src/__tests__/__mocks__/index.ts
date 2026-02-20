// ============================================================================
// MOCKS EXPORTÁVEIS DA BIBLIOTECA BUILDERS WALLET
// ============================================================================
// Este arquivo exporta todos os mocks necessários para testar aplicações
// que utilizam a biblioteca BuildersWallet

import type {
  GoogleTokenInfo,
  GoogleTokenStatus,
  GoogleWalletConstants,
  GoogleWalletData,
} from '../../types/google-wallet.types';
import type {
  SamsungCard,
  SamsungWalletConstants,
  SamsungWalletInfo,
} from '../../types/samsung-wallet.types';

// ============================================================================
// MOCKS DO GOOGLE WALLET
// ============================================================================

export const mockGoogleWalletConstants: GoogleWalletConstants = {
  SDK_NAME: 'GoogleWallet',
  GOOGLE_WALLET_PACKAGE: 'com.google.android.gms',
  GOOGLE_WALLET_APP_PACKAGE: 'com.google.android.apps.walletnfcrel',
  GOOGLE_WALLET_PLAY_STORE_URL:
    'https://play.google.com/store/apps/details?id=com.google.android.apps.walletnfcrel',

  // Token Providers
  TOKEN_PROVIDER_AMEX: 1,
  TOKEN_PROVIDER_DISCOVER: 2,
  TOKEN_PROVIDER_JCB: 3,
  TOKEN_PROVIDER_MASTERCARD: 4,
  TOKEN_PROVIDER_VISA: 5,
  TOKEN_PROVIDER_ELO: 6,

  // Card Networks
  CARD_NETWORK_AMEX: 1,
  CARD_NETWORK_DISCOVER: 2,
  CARD_NETWORK_MASTERCARD: 3,
  CARD_NETWORK_QUICPAY: 4,
  CARD_NETWORK_PRIVATE_LABEL: 5,
  CARD_NETWORK_VISA: 6,
  CARD_NETWORK_ELO: 7,

  // Tap and Pay Status
  TAP_AND_PAY_NO_ACTIVE_WALLET: 1,
  TAP_AND_PAY_TOKEN_NOT_FOUND: 2,
  TAP_AND_PAY_INVALID_TOKEN_STATE: 3,
  TAP_AND_PAY_ATTESTATION_ERROR: 4,
  TAP_AND_PAY_UNAVAILABLE: 5,
  TAP_AND_PAY_SAVE_CARD_ERROR: 6,
  TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION: 7,
  TAP_AND_PAY_TOKENIZATION_DECLINED: 8,
  TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR: 9,
  TAP_AND_PAY_TOKENIZE_ERROR: 10,
  TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED: 11,
  TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT: 12,
  TAP_AND_PAY_USER_CANCELED_FLOW: 13,
  TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED: 14,

  // Token States
  TOKEN_STATE_ACTIVE: 1,
  TOKEN_STATE_PENDING: 2,
  TOKEN_STATE_SUSPENDED: 3,
  TOKEN_STATE_UNTOKENIZED: 4,
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION: 5,
  TOKEN_STATE_FELICA_PENDING_PROVISIONING: 6,

  // Common Status Codes
  SUCCESS: 0,
  SUCCESS_CACHE: 1,
  SERVICE_VERSION_UPDATE_REQUIRED: 2,
  SERVICE_DISABLED: 3,
  SIGN_IN_REQUIRED: 4,
  INVALID_ACCOUNT: 5,
  RESOLUTION_REQUIRED: 6,
  NETWORK_ERROR: 7,
  INTERNAL_ERROR: 8,
  DEVELOPER_ERROR: 9,
  ERROR: 10,
  INTERRUPTED: 11,
  TIMEOUT: 12,
  CANCELED: 13,
  API_NOT_CONNECTED: 14,
  REMOTE_EXCEPTION: 15,
  CONNECTION_SUSPENDED_DURING_CALL: 16,
  RECONNECTION_TIMED_OUT_DURING_UPDATE: 17,
  RECONNECTION_TIMED_OUT: 18,
};

export const mockGoogleWalletData: GoogleWalletData = {
  deviceID: 'test-device-id-12345',
  walletAccountID: 'test-wallet-account-67890',
};

export const mockGoogleTokenInfo: GoogleTokenInfo = {
  issuerTokenId: 'test-issuer-token-id',
  issuerName: 'Test Bank',
  fpanLastFour: '1234',
  dpanLastFour: '5678',
  tokenServiceProvider: 1,
  network: 1,
  tokenState: 1,
  isDefaultToken: true,
  portfolioName: 'Test Portfolio',
};

export const mockGoogleTokenStatus: GoogleTokenStatus = {
  tokenState: 1,
  isSelected: true,
};

export const mockGoogleWalletModule = {
  checkWalletAvailability: jest.fn().mockResolvedValue(true),
  getSecureWalletInfo: jest.fn().mockResolvedValue(mockGoogleWalletData),
  getTokenStatus: jest.fn().mockResolvedValue(mockGoogleTokenStatus),
  getEnvironment: jest.fn().mockResolvedValue('PRODUCTION'),
  isTokenized: jest.fn().mockResolvedValue(true),
  viewToken: jest.fn().mockResolvedValue(mockGoogleTokenInfo),
  addCardToWallet: jest.fn().mockResolvedValue(null),
  createWalletIfNeeded: jest.fn().mockResolvedValue(true),
  listTokens: jest.fn().mockResolvedValue([mockGoogleTokenInfo]),
  getConstants: jest.fn().mockReturnValue(mockGoogleWalletConstants),
  setIntentListener: jest.fn().mockResolvedValue(true),
  removeIntentListener: jest.fn().mockResolvedValue(true),
  setActivationResult: jest.fn().mockResolvedValue(true),
  finishActivity: jest.fn().mockResolvedValue(true),
  openWallet: jest.fn().mockResolvedValue(true),
  setLogListener: jest.fn().mockResolvedValue(true),
  removeLogListener: jest.fn().mockResolvedValue(true),
};

// ============================================================================
// MOCKS DO SAMSUNG WALLET
// ============================================================================

export const mockSamsungWalletConstants: SamsungWalletConstants = {
  SDK_NAME: 'SamsungPay',
  SAMSUNG_PAY_PACKAGE: 'com.samsung.android.spay',
  SAMSUNG_PAY_PLAY_STORE_URL:
    'https://play.google.com/store/apps/details?id=com.samsung.android.spay',

  // Status Codes
  SPAY_READY: 0,
  SPAY_NOT_READY: 1,
  SPAY_NOT_SUPPORTED: 2,
  SPAY_NOT_ALLOWED_TEMPORALLY: 3,
  SPAY_HAS_TRANSIT_CARD: 4,
  SPAY_HAS_NO_TRANSIT_CARD: 5,

  // Card Types
  CARD_TYPE: 'CARD_TYPE',
  CARD_TYPE_CREDIT_DEBIT: 'CREDIT_DEBIT',
  CARD_TYPE_GIFT: 'GIFT',
  CARD_TYPE_LOYALTY: 'LOYALTY',
  CARD_TYPE_CREDIT: 'CREDIT',
  CARD_TYPE_DEBIT: 'DEBIT',
  CARD_TYPE_TRANSIT: 'TRANSIT',
  CARD_TYPE_VACCINE_PASS: 'VACCINE_PASS',

  // Card States
  ACTIVE: 'ACTIVE',
  DISPOSED: 'DISPOSED',
  EXPIRED: 'EXPIRED',
  PENDING_ENROLLED: 'PENDING_ENROLLED',
  PENDING_PROVISION: 'PENDING_PROVISION',
  SUSPENDED: 'SUSPENDED',
  PENDING_ACTIVATION: 'PENDING_ACTIVATION',

  // Tokenization Providers
  PROVIDER_VISA: 'VISA',
  PROVIDER_MASTERCARD: 'MASTERCARD',
  PROVIDER_AMEX: 'AMEX',
  PROVIDER_DISCOVER: 'DISCOVER',
  PROVIDER_PLCC: 'PLCC',
  PROVIDER_GIFT: 'GIFT',
  PROVIDER_LOYALTY: 'LOYALTY',
  PROVIDER_PAYPAL: 'PAYPAL',
  PROVIDER_GEMALTO: 'GEMALTO',
  PROVIDER_NAPAS: 'NAPAS',
  PROVIDER_MIR: 'MIR',
  PROVIDER_PAGOBANCOMAT: 'PAGOBANCOMAT',
  PROVIDER_VACCINE_PASS: 'VACCINE_PASS',
  PROVIDER_MADA: 'MADA',
  PROVIDER_ELO: 'ELO',

  // Error Codes
  ERROR_NONE: 0,
  ERROR_SPAY_INTERNAL: 1,
  ERROR_INVALID_INPUT: 2,
  ERROR_NOT_SUPPORTED: 3,
  ERROR_NOT_FOUND: 4,
  ERROR_ALREADY_DONE: 5,
  ERROR_NOT_ALLOWED: 6,
  ERROR_USER_CANCELED: 7,
  ERROR_PARTNER_SDK_API_LEVEL: 8,
  ERROR_PARTNER_SERVICE_TYPE: 9,
  ERROR_INVALID_PARAMETER: 10,
  ERROR_NO_NETWORK: 11,
  ERROR_SERVER_NO_RESPONSE: 12,
  ERROR_PARTNER_INFO_INVALID: 13,
  ERROR_INITIATION_FAIL: 14,
  ERROR_REGISTRATION_FAIL: 15,
  ERROR_DUPLICATED_SDK_API_CALLED: 16,
  ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION: 17,
  ERROR_SERVICE_ID_INVALID: 18,
  ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION: 19,
  ERROR_PARTNER_APP_SIGNATURE_MISMATCH: 20,
  ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED: 21,
  ERROR_PARTNER_APP_BLOCKED: 22,
  ERROR_USER_NOT_REGISTERED_FOR_DEBUG: 23,
  ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE: 24,
  ERROR_PARTNER_NOT_APPROVED: 25,
  ERROR_UNAUTHORIZED_REQUEST_TYPE: 26,
  ERROR_EXPIRED_OR_INVALID_DEBUG_KEY: 27,
  ERROR_SERVER_INTERNAL: 28,
  ERROR_DEVICE_NOT_SAMSUNG: 29,
  ERROR_SPAY_PKG_NOT_FOUND: 30,
  ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE: 31,
  ERROR_DEVICE_INTEGRITY_CHECK_FAIL: 32,
  ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL: 33,
  ERROR_ANDROID_PLATFORM_CHECK_FAIL: 34,
  ERROR_MISSING_INFORMATION: 35,
  ERROR_SPAY_SETUP_NOT_COMPLETED: 36,
  ERROR_SPAY_APP_NEED_TO_UPDATE: 37,
  ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED: 38,
  ERROR_UNABLE_TO_VERIFY_CALLER: 39,
  ERROR_SPAY_FMM_LOCK: 40,
  ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY: 41,
};

export const mockSamsungWalletInfo: SamsungWalletInfo = {
  walletDMId: 'test-wallet-dm-id',
  deviceId: 'test-device-id-samsung',
  walletUserId: 'test-wallet-user-id',
};

export const mockSamsungCard: SamsungCard = {
  cardId: 'test-samsung-card-id',
  cardStatus: 'ACTIVE',
  cardBrand: 'VISA',
  last4FPan: '1234',
  last4DPan: '5678',
  issuerName: 'Test Bank Samsung',
  isDefaultCard: 'true',
  memberID: 'test-member-id-samsung',
  countryCode: 'BR',
  cardType: 'CREDIT_DEBIT',
  tokenizationProvider: 'VISA',
  network: 'VISA',
  displayName: 'Test Samsung Card',
};

export const mockSamsungWalletModule = {
  init: jest.fn().mockResolvedValue(true),
  getSamsungPayStatus: jest.fn().mockResolvedValue(0),
  goToUpdatePage: jest.fn(),
  activateSamsungPay: jest.fn(),
  getAllCards: jest.fn().mockResolvedValue([mockSamsungCard]),
  getWalletInfo: jest.fn().mockResolvedValue(mockSamsungWalletInfo),
  addCard: jest.fn().mockResolvedValue(mockSamsungCard),
  checkWalletAvailability: jest.fn().mockResolvedValue(true),
  getConstants: jest.fn().mockReturnValue(mockSamsungWalletConstants),
  setIntentListener: jest.fn().mockResolvedValue(true),
  removeIntentListener: jest.fn().mockResolvedValue(true),
  setActivationResult: jest.fn().mockResolvedValue(true),
  finishActivity: jest.fn().mockResolvedValue(true),
  openWallet: jest.fn().mockResolvedValue(true),
  setLogListener: jest.fn().mockResolvedValue(true),
  removeLogListener: jest.fn().mockResolvedValue(true),
};

// ============================================================================
// MOCKS DOS EVENT EMITTERS
// ============================================================================

export const mockGoogleWalletEventEmitter = {
  addListener: jest.fn(),
  removeListener: jest.fn(),
  removeAllListeners: jest.fn(),
  emit: jest.fn(),
};

export const mockSamsungWalletEventEmitter = {
  addListener: jest.fn(),
  removeListener: jest.fn(),
  removeAllListeners: jest.fn(),
  emit: jest.fn(),
};

// ============================================================================
// MOCKS DO REACT NATIVE
// ============================================================================

export const mockReactNative = {
  NativeModules: {
    GoogleWallet: mockGoogleWalletModule,
    SamsungWallet: mockSamsungWalletModule,
  },
  Platform: {
    OS: 'ios',
    select: jest.fn((obj) => obj.ios || obj.default),
  },
};

// ============================================================================
// FUNÇÕES UTILITÁRIAS PARA TESTES
// ============================================================================

/**
 * Reseta todos os mocks para um estado limpo
 */
export const resetAllMocks = () => {
  jest.clearAllMocks();

  // Reset Google Wallet mocks
  Object.values(mockGoogleWalletModule).forEach((mock) => {
    if (jest.isMockFunction(mock)) {
      mock.mockClear();
    }
  });

  // Reset Samsung Wallet mocks
  Object.values(mockSamsungWalletModule).forEach((mock) => {
    if (jest.isMockFunction(mock)) {
      mock.mockClear();
    }
  });

  // Reset Event Emitter mocks
  Object.values(mockGoogleWalletEventEmitter).forEach((mock) => {
    if (jest.isMockFunction(mock)) {
      mock.mockClear();
    }
  });

  Object.values(mockSamsungWalletEventEmitter).forEach((mock) => {
    if (jest.isMockFunction(mock)) {
      mock.mockClear();
    }
  });
};

/**
 * Configura mocks para cenário de sucesso
 */
export const setupSuccessMocks = () => {
  mockGoogleWalletModule.checkWalletAvailability.mockResolvedValue(true);
  mockGoogleWalletModule.getSecureWalletInfo.mockResolvedValue(
    mockGoogleWalletData,
  );
  mockGoogleWalletModule.listTokens.mockResolvedValue([mockGoogleTokenInfo]);

  mockSamsungWalletModule.checkWalletAvailability.mockResolvedValue(true);
  mockSamsungWalletModule.getSamsungPayStatus.mockResolvedValue(0);
  mockSamsungWalletModule.getAllCards.mockResolvedValue([mockSamsungCard]);
};

/**
 * Configura mocks para cenário de erro
 */
export const setupErrorMocks = () => {
  const error = new Error('Wallet not available');

  mockGoogleWalletModule.checkWalletAvailability.mockRejectedValue(error);
  mockGoogleWalletModule.getSecureWalletInfo.mockRejectedValue(error);
  mockGoogleWalletModule.listTokens.mockRejectedValue(error);

  mockSamsungWalletModule.checkWalletAvailability.mockRejectedValue(error);
  mockSamsungWalletModule.getSamsungPayStatus.mockRejectedValue(error);
  mockSamsungWalletModule.getAllCards.mockRejectedValue(error);
};

/**
 * Configura mocks para cenário de carteira não disponível
 */
export const setupUnavailableMocks = () => {
  mockGoogleWalletModule.checkWalletAvailability.mockResolvedValue(false);
  mockSamsungWalletModule.checkWalletAvailability.mockResolvedValue(false);
  mockSamsungWalletModule.getSamsungPayStatus.mockResolvedValue(2); // SPAY_NOT_SUPPORTED
};
