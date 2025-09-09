// ============================================================================
// TYPES ESPECÍFICOS DO GOOGLE PAY / GOOGLE WALLET
// ============================================================================

export enum GoogleWalletStatus {
  /** Não há carteira ativa. */
  TAP_AND_PAY_NO_ACTIVE_WALLET = 'TAP_AND_PAY_NO_ACTIVE_WALLET',
  /** O ID do token do emissor indicado não corresponde a um token na carteira ativa. */
  TAP_AND_PAY_TOKEN_NOT_FOUND = 'TAP_AND_PAY_TOKEN_NOT_FOUND',
  /** O token especificado foi encontrado, mas não estava em um estado válido. */
  TAP_AND_PAY_INVALID_TOKEN_STATE = 'TAP_AND_PAY_INVALID_TOKEN_STATE',
  /** A tokenização falhou porque o dispositivo não foi aprovado em uma verificação de compatibilidade. */
  TAP_AND_PAY_ATTESTATION_ERROR = 'TAP_AND_PAY_ATTESTATION_ERROR',
  /** Não é possível chamar a API TapAndPay pelo aplicativo atual. */
  TAP_AND_PAY_UNAVAILABLE = 'TAP_AND_PAY_UNAVAILABLE',
}

export enum GoogleWalletStatusCode {
  TAP_AND_PAY_NO_ACTIVE_WALLET = '15002',
  TAP_AND_PAY_TOKEN_NOT_FOUND = '15003',
  TAP_AND_PAY_INVALID_TOKEN_STATE = '15004',
  TAP_AND_PAY_ATTESTATION_ERROR = '15005',
  TAP_AND_PAY_UNAVAILABLE = '15009',
}

export enum GoogleTokenState {
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION = 'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION',
  TOKEN_STATE_PENDING = 'TOKEN_STATE_PENDING',
  TOKEN_STATE_SUSPENDED = 'TOKEN_STATE_SUSPENDED',
  TOKEN_STATE_ACTIVE = 'TOKEN_STATE_ACTIVE',
  TOKEN_STATE_FELICA_PENDING_PROVISIONING = 'TOKEN_STATE_FELICA_PENDING_PROVISIONING',
  TOKEN_STATE_UNTOKENIZED = 'TOKEN_STATE_UNTOKENIZED',
}

export enum GoogleTokenProvider {
  TOKEN_PROVIDER_AMEX = 'TOKEN_PROVIDER_AMEX',
  TOKEN_PROVIDER_DISCOVER = 'TOKEN_PROVIDER_DISCOVER',
  TOKEN_PROVIDER_JCB = 'TOKEN_PROVIDER_JCB',
  TOKEN_PROVIDER_MASTERCARD = 'TOKEN_PROVIDER_MASTERCARD',
  TOKEN_PROVIDER_VISA = 'TOKEN_PROVIDER_VISA',
  TOKEN_PROVIDER_ELO = 'TOKEN_PROVIDER_ELO',
}

export enum GoogleCardNetwork {
  CARD_NETWORK_AMEX = 'CARD_NETWORK_AMEX',
  CARD_NETWORK_DISCOVER = 'CARD_NETWORK_DISCOVER',
  CARD_NETWORK_MASTERCARD = 'CARD_NETWORK_MASTERCARD',
  CARD_NETWORK_QUICPAY = 'CARD_NETWORK_QUICPAY',
  CARD_NETWORK_PRIVATE_LABEL = 'CARD_NETWORK_PRIVATE_LABEL',
  CARD_NETWORK_VISA = 'CARD_NETWORK_VISA',
  CARD_NETWORK_ELO = 'CARD_NETWORK_ELO',
}

export enum GoogleEnvironment {
  PROD = 'PROD',
  SANDBOX = 'SANDBOX',
  DEV = 'DEV',
}

// Google Wallet - UserAddress (baseado no SDK do Google Pay)
export interface GoogleUserAddress {
  name: string;
  address1: string;
  address2?: string;
  locality: string; // city
  administrativeArea: string; // state/province
  countryCode: string;
  postalCode: string;
  phoneNumber: string;
}

// Google Wallet - PaymentCard (baseado no SDK do Google Pay)
export interface GooglePaymentCard {
  opaquePaymentCard: string;
  network: number; // GoogleCardNetwork value
  tokenServiceProvider: number; // GoogleTokenProvider value
  displayName: string;
  lastDigits: string;
}

// Google Wallet - PushTokenizeRequest (baseado no SDK do Google Pay)
export interface GooglePushTokenizeRequest {
  address: GoogleUserAddress;
  card: GooglePaymentCard;
}

// Google Wallet - TokenInfo (baseado no SDK do Google Pay)
export interface GoogleTokenInfo {
  issuerTokenId: string;
  issuerName: string;
  fpanLastFour: string;
  dpanLastFour: string;
  tokenServiceProvider: number;
  network: number;
  tokenState: number;
  isDefaultToken: boolean;
  portfolioName: string;
}

// Google Wallet - WalletData
export interface GoogleWalletData {
  deviceID: string;
  walletAccountID: string;
}

// Google Wallet - Constants
export interface GoogleWalletConstants {
  SDK_AVAILABLE: boolean;
  SDK_NAME: string;
  CARD_NETWORK_ELO: number;
  TOKEN_PROVIDER_ELO: number;
  TOKEN_STATE_ACTIVE: number;
  TOKEN_STATE_PENDING: number;
  TOKEN_STATE_SUSPENDED: number;
  TOKEN_STATE_UNTOKENIZED: number;
}

// Google Wallet - Interface do Módulo
export interface GoogleWalletSpec {
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<GoogleWalletData>;
  getCardStatusBySuffix(lastDigits: string): Promise<string>;
  getCardStatusByIdentifier(identifier: string, tsp: number): Promise<string>;
  addCardToWallet(cardData: GooglePushTokenizeRequest): Promise<string>;
  createWalletIfNeeded(): Promise<boolean>;
  getConstants(): Promise<GoogleWalletConstants>;
}

// Google Wallet - Interface de Compatibilidade (para código existente)
export interface GoogleWalletCompatibilitySpec {
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<GoogleWalletData>;
  getCardStatusBySuffix(lastDigits: string): Promise<string>;
  getCardStatusByIdentifier(identifier: string, tsp: number): Promise<string>;
  addCardToWallet(cardData: any): Promise<string>; // Aceita qualquer tipo para compatibilidade
  createWalletIfNeeded(): Promise<boolean>;
  getConstants(): Promise<any>; // Aceita qualquer tipo para compatibilidade
}
