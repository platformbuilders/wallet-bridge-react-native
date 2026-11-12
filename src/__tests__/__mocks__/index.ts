// ============================================================================
// MOCKS EXPORTÁVEIS DA BIBLIOTECA BUILDERS WALLET
// ============================================================================
// Este arquivo exporta todos os mocks necessários para testar aplicações
// que utilizam a biblioteca BuildersWallet

import {
  ApplePassActivationState,
  AppleWalletErrorCode,
} from '../../enums/apple-wallet.enum';
import type {
  ApplePaymentPass,
  AppleWalletConstants,
} from '../../types/apple-wallet.types';
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
  checkWalletAvailability: jest.fn(),
  getSecureWalletInfo: jest.fn(),
  getTokenStatus: jest.fn(),
  getEnvironment: jest.fn(),
  isTokenized: jest.fn(),
  viewToken: jest.fn(),
  addCardToWallet: jest.fn(),
  createWalletIfNeeded: jest.fn(),
  listTokens: jest.fn(),
  getConstants: jest.fn(),
  setIntentListener: jest.fn(),
  removeIntentListener: jest.fn(),
  setActivationResult: jest.fn(),
  finishActivity: jest.fn(),
  openWallet: jest.fn(),
  setLogListener: jest.fn(),
  removeLogListener: jest.fn(),
};

// Retornos padrão: aplicados na criação e de novo em `resetAllMocks`.
const applyGoogleDefaults = () => {
  const m = mockGoogleWalletModule;
  m.checkWalletAvailability.mockResolvedValue(true);
  m.getSecureWalletInfo.mockResolvedValue(mockGoogleWalletData);
  m.getTokenStatus.mockResolvedValue(mockGoogleTokenStatus);
  m.getEnvironment.mockResolvedValue('PRODUCTION');
  m.isTokenized.mockResolvedValue(true);
  m.viewToken.mockResolvedValue(mockGoogleTokenInfo);
  m.addCardToWallet.mockResolvedValue(null);
  m.createWalletIfNeeded.mockResolvedValue(true);
  m.listTokens.mockResolvedValue([mockGoogleTokenInfo]);
  m.getConstants.mockReturnValue(mockGoogleWalletConstants);
  m.setIntentListener.mockResolvedValue(true);
  m.removeIntentListener.mockResolvedValue(true);
  m.setActivationResult.mockResolvedValue(true);
  m.finishActivity.mockResolvedValue(true);
  m.openWallet.mockResolvedValue(true);
  m.setLogListener.mockResolvedValue(true);
  m.removeLogListener.mockResolvedValue(true);
};
applyGoogleDefaults();

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
  init: jest.fn(),
  getSamsungPayStatus: jest.fn(),
  goToUpdatePage: jest.fn(),
  activateSamsungPay: jest.fn(),
  getAllCards: jest.fn(),
  getWalletInfo: jest.fn(),
  addCard: jest.fn(),
  checkWalletAvailability: jest.fn(),
  getConstants: jest.fn(),
  setIntentListener: jest.fn(),
  removeIntentListener: jest.fn(),
  setActivationResult: jest.fn(),
  finishActivity: jest.fn(),
  openWallet: jest.fn(),
  setLogListener: jest.fn(),
  removeLogListener: jest.fn(),
};

// Retornos padrão: aplicados na criação e de novo em `resetAllMocks`.
const applySamsungDefaults = () => {
  const m = mockSamsungWalletModule;
  m.init.mockResolvedValue(true);
  m.getSamsungPayStatus.mockResolvedValue(0);
  m.getAllCards.mockResolvedValue([mockSamsungCard]);
  m.getWalletInfo.mockResolvedValue(mockSamsungWalletInfo);
  m.addCard.mockResolvedValue(mockSamsungCard);
  m.checkWalletAvailability.mockResolvedValue(true);
  m.getConstants.mockReturnValue(mockSamsungWalletConstants);
  m.setIntentListener.mockResolvedValue(true);
  m.removeIntentListener.mockResolvedValue(true);
  m.setActivationResult.mockResolvedValue(true);
  m.finishActivity.mockResolvedValue(true);
  m.openWallet.mockResolvedValue(true);
  m.setLogListener.mockResolvedValue(true);
  m.removeLogListener.mockResolvedValue(true);
};
applySamsungDefaults();

// ============================================================================
// MOCKS DO APPLE WALLET
// ============================================================================
// Dados falsos: o mesmo cartão (FPANID) no iPhone (ativo) e num device
// pareado (pendente), com DPANIDs distintos, e um segundo cartão pendente.

export const mockAppleWalletConstants: AppleWalletConstants = {
  PROVISIONING_ENTITLEMENT: 'com.apple.developer.payment-pass-provisioning',
  APPLE_LEAF_CERT_OID: '1.2.840.113635.100.6.39',
  WALLET_URL_SCHEME: 'shoebox://',
  EVENT_LOG: 'AppleWalletLog',
};

const mockApplePass = (
  overrides: Partial<ApplePaymentPass>,
): ApplePaymentPass => ({
  primaryAccountIdentifier: 'FAKE_FPANID_1',
  primaryAccountNumberSuffix: '1111',
  deviceAccountIdentifier: 'FAKE_DPANID_1_PHONE',
  deviceAccountNumberSuffix: '9001',
  serialNumber: 'FAKE_SERIAL_1_PHONE',
  passTypeIdentifier: 'pass.com.builders.wallet.mock',
  passActivationState: ApplePassActivationState.ACTIVATED,
  isRemote: false,
  deviceName: 'iPhone',
  localizedDescription: 'Cartão 1',
  ...overrides,
});

export const mockApplePhonePass = mockApplePass({});

export const mockAppleWatchPass = mockApplePass({
  deviceAccountIdentifier: 'FAKE_DPANID_1_WATCH',
  deviceAccountNumberSuffix: '9002',
  serialNumber: 'FAKE_SERIAL_1_WATCH',
  passActivationState: ApplePassActivationState.REQUIRES_ACTIVATION,
  isRemote: true,
  deviceName: 'Apple Watch',
});

export const mockApplePendingPass = mockApplePass({
  primaryAccountIdentifier: 'FAKE_FPANID_2',
  primaryAccountNumberSuffix: '2222',
  deviceAccountIdentifier: 'FAKE_DPANID_2_PHONE',
  deviceAccountNumberSuffix: '9003',
  serialNumber: 'FAKE_SERIAL_2_PHONE',
  passActivationState: ApplePassActivationState.REQUIRES_ACTIVATION,
  localizedDescription: 'Cartão 2',
});

/** `listPasses`: passes deste device. */
export const mockApplePasses: ApplePaymentPass[] = [
  mockApplePhonePass,
  mockApplePendingPass,
];

/** `listRemotePasses`: passes de devices pareados. */
export const mockAppleRemotePasses: ApplePaymentPass[] = [mockAppleWatchPass];

const mockAppleSubscription = () => ({ remove: jest.fn() });

export const mockAppleWalletModule = {
  getConstants: jest.fn(),
  getEnvironment: jest.fn(),
  openWallet: jest.fn(),
  canAddPaymentPass: jest.fn(),
  canAddSecureElementPass: jest.fn(),
  startInAppProvisioning: jest.fn(),
  submitProvisioningData: jest.fn(),
  cancelProvisioning: jest.fn(),
  listPasses: jest.fn(),
  listRemotePasses: jest.fn(),
  isWatchPaired: jest.fn(),
  activatePass: jest.fn(),
  setLogListener: jest.fn(),
  removeLogListener: jest.fn(),
  onProvisioningCertificates: jest.fn(),
  onProvisioningResult: jest.fn(),
  onLog: jest.fn(),
};

// Retornos padrão: aplicados na criação e de novo em `resetAllMocks`.
const applyAppleDefaults = () => {
  const m = mockAppleWalletModule;
  m.getConstants.mockReturnValue(mockAppleWalletConstants);
  m.getEnvironment.mockResolvedValue('ios-simulator');
  m.openWallet.mockResolvedValue(true);
  m.canAddPaymentPass.mockResolvedValue(true);
  m.canAddSecureElementPass.mockResolvedValue(true);
  m.startInAppProvisioning.mockResolvedValue('FAKE_REQUEST_ID');
  m.submitProvisioningData.mockResolvedValue(true);
  m.cancelProvisioning.mockResolvedValue(true);
  m.listPasses.mockResolvedValue(mockApplePasses);
  m.listRemotePasses.mockResolvedValue(mockAppleRemotePasses);
  m.isWatchPaired.mockResolvedValue(true);
  m.activatePass.mockResolvedValue(true);
  m.setLogListener.mockResolvedValue(true);
  m.removeLogListener.mockResolvedValue(true);
  m.onProvisioningCertificates.mockImplementation(mockAppleSubscription);
  m.onProvisioningResult.mockImplementation(mockAppleSubscription);
  m.onLog.mockImplementation(mockAppleSubscription);
};
applyAppleDefaults();

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

const mockTurboModules: Record<string, unknown> = {
  AppleWallet: mockAppleWalletModule,
  GoogleWallet: mockGoogleWalletModule,
  SamsungWallet: mockSamsungWalletModule,
};

export const mockReactNative = {
  NativeModules: {
    GoogleWallet: mockGoogleWalletModule,
    SamsungWallet: mockSamsungWalletModule,
  },
  // Os módulos são TurboModules: as specs (src/specs) buscam pelo registry.
  TurboModuleRegistry: {
    get: jest.fn((name: string) => mockTurboModules[name] ?? null),
    getEnforcing: jest.fn((name: string) => mockTurboModules[name]),
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
 * Volta todos os mocks ao estado inicial: limpa as chamadas, desfaz o que um
 * `setup*Mocks` (ou o próprio teste) configurou e reaplica os retornos
 * padrão. `mockClear` não bastaria: ele mantém `mockResolvedValue` /
 * `mockRejectedValue`, e o estado de um teste vazaria para o seguinte.
 */
export const resetAllMocks = () => {
  jest.clearAllMocks();

  for (const module of [
    mockGoogleWalletModule,
    mockSamsungWalletModule,
    mockAppleWalletModule,
    mockGoogleWalletEventEmitter,
    mockSamsungWalletEventEmitter,
  ]) {
    Object.values(module).forEach((mock) => {
      if (jest.isMockFunction(mock)) {
        mock.mockReset();
      }
    });
  }

  applyGoogleDefaults();
  applySamsungDefaults();
  applyAppleDefaults();
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

  mockAppleWalletModule.canAddPaymentPass.mockResolvedValue(true);
  mockAppleWalletModule.canAddSecureElementPass.mockResolvedValue(true);
  mockAppleWalletModule.isWatchPaired.mockResolvedValue(true);
  mockAppleWalletModule.listPasses.mockResolvedValue(mockApplePasses);
  mockAppleWalletModule.listRemotePasses.mockResolvedValue(
    mockAppleRemotePasses,
  );
  mockAppleWalletModule.startInAppProvisioning.mockResolvedValue(
    'FAKE_REQUEST_ID',
  );
  mockAppleWalletModule.submitProvisioningData.mockResolvedValue(true);
  mockAppleWalletModule.activatePass.mockResolvedValue(true);
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

  // Como o nativo: as consultas do PassKit não rejeitam; cada ação rejeita
  // com um código que o nativo produz para ela.
  const appleError = (code: AppleWalletErrorCode, message: string) =>
    Object.assign(new Error(message), { code });
  mockAppleWalletModule.startInAppProvisioning.mockRejectedValue(
    appleError(
      AppleWalletErrorCode.ENTITLEMENT_MISSING,
      'Provisioning entitlement missing',
    ),
  );
  mockAppleWalletModule.submitProvisioningData.mockRejectedValue(
    appleError(
      AppleWalletErrorCode.NOT_IN_PROGRESS,
      'No provisioning awaiting step 2',
    ),
  );
  mockAppleWalletModule.activatePass.mockRejectedValue(
    appleError(AppleWalletErrorCode.ACTIVATION_FAILED, 'Activation failed'),
  );
};

/**
 * Configura mocks para cenário de carteira não disponível
 */
export const setupUnavailableMocks = () => {
  mockGoogleWalletModule.checkWalletAvailability.mockResolvedValue(false);
  mockSamsungWalletModule.checkWalletAvailability.mockResolvedValue(false);
  mockSamsungWalletModule.getSamsungPayStatus.mockResolvedValue(2); // SPAY_NOT_SUPPORTED

  mockAppleWalletModule.canAddPaymentPass.mockResolvedValue(false);
  mockAppleWalletModule.canAddSecureElementPass.mockResolvedValue(false);
  mockAppleWalletModule.isWatchPaired.mockResolvedValue(false);
  mockAppleWalletModule.listPasses.mockResolvedValue([]);
  mockAppleWalletModule.listRemotePasses.mockResolvedValue([]);
};
