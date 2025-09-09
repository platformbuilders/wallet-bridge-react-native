// ============================================================================
// TYPES ESPECÍFICOS DO GOOGLE PAY
// ============================================================================

export enum GOOGLE_WALLET_STATUS {
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

export enum GOOGLE_WALLET_STATUS_CODE {
  TAP_AND_PAY_NO_ACTIVE_WALLET = '15002',
  TAP_AND_PAY_TOKEN_NOT_FOUND = '15003',
  TAP_AND_PAY_INVALID_TOKEN_STATE = '15004',
  TAP_AND_PAY_ATTESTATION_ERROR = '15005',
  TAP_AND_PAY_UNAVAILABLE = '15009',
}

export enum GOOGLE_STATUS_TOKEN {
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION = 'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION',
  TOKEN_STATE_PENDING = 'TOKEN_STATE_PENDING',
  TOKEN_STATE_SUSPENDED = 'TOKEN_STATE_SUSPENDED',
  TOKEN_STATE_ACTIVE = 'TOKEN_STATE_ACTIVE',
  TOKEN_STATE_FELICA_PENDING_PROVISIONING = 'TOKEN_STATE_FELICA_PENDING_PROVISIONING',
  TOKEN_STATE_UNTOKENIZED = 'TOKEN_STATE_UNTOKENIZED',
}

export enum GOOGLE_CONSTANTS {
  'TOKEN_PROVIDER_ELO' = 'TOKEN_PROVIDER_ELO',
  'CARD_NETWORK_ELO' = 'CARD_NETWORK_ELO',
  'TOKEN_STATE_UNTOKENIZED' = 'TOKEN_STATE_UNTOKENIZED',
  'TOKEN_STATE_PENDING' = 'TOKEN_STATE_PENDING',
  'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION' = 'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION',
  'TOKEN_STATE_SUSPENDED' = 'TOKEN_STATE_SUSPENDED',
  'TOKEN_STATE_ACTIVE' = 'TOKEN_STATE_ACTIVE',
  'TOKEN_STATE_FELICA_PENDING_PROVISIONING' = 'TOKEN_STATE_FELICA_PENDING_PROVISIONING',
}

export enum GOOGLE_ENVIRONMENT {
  PROD = 'PROD',
  SANDBOX = 'SANDBOX',
  DEV = 'DEV',
}

export enum GOOGLE_TOKEN_PROVIDER {
  TOKEN_PROVIDER_AMEX = 'TOKEN_PROVIDER_AMEX',
  TOKEN_PROVIDER_DISCOVER = 'TOKEN_PROVIDER_DISCOVER',
  TOKEN_PROVIDER_JCB = 'TOKEN_PROVIDER_JCB',
  TOKEN_PROVIDER_MASTERCARD = 'TOKEN_PROVIDER_MASTERCARD',
  TOKEN_PROVIDER_VISA = 'TOKEN_PROVIDER_VISA',
}

export enum GOOGLE_CARD_NETWORK {
  CARD_NETWORK_AMEX = 'CARD_NETWORK_AMEX',
  CARD_NETWORK_DISCOVER = 'CARD_NETWORK_DISCOVER',
  CARD_NETWORK_MASTERCARD = 'CARD_NETWORK_MASTERCARD',
  CARD_NETWORK_QUICPAY = 'CARD_NETWORK_QUICPAY',
  CARD_NETWORK_PRIVATE_LABEL = 'CARD_NETWORK_PRIVATE_LABEL',
  CARD_NETWORK_VISA = 'CARD_NETWORK_VISA',
}

// ============================================================================
// TYPES GENÉRICOS DA API UNIFICADA
// ============================================================================

export type CardStatus =
  | 'not found'
  | 'active'
  | 'requireAuthorization'
  | 'pending'
  | 'suspended'
  | 'deactivated';

export interface WalletData {
  deviceID: string;
  walletAccountID: string;
}

export interface AndroidCardData {
  network: string;
  tokenServiceProvider: number;
  opaquePaymentCard: string;
  cardHolderName: string;
  lastDigits: string;
  userAddress: UserAddress;
}

export interface UserAddress {
  name: string;
  addressOne: string;
  addressTwo?: string;
  city: string;
  administrativeArea: string;
  countryCode: string;
  postalCode: string;
  phoneNumber: string;
}

// ============================================================================
// TYPES DE COMPATIBILIDADE COM API EXISTENTE
// ============================================================================

export interface PaymentCard {
  opaquePaymentCard: string;
  network: number;
  tokenServiceProvider: number;
  displayName: string;
  lastDigits: string;
}

export interface PushTokenizeRequest {
  address: UserAddress;
  card: PaymentCard;
}

export interface GetTokenStatusParams {
  tokenServiceProvider: number;
  tokenReferenceId: string;
}

export interface ViewTokenParams {
  tokenServiceProvider: number;
  issuerTokenId: string;
}

export interface Address {
  address1: string;
  address2: string;
  countryCode: string;
  locality: string;
  administrativeArea: string;
  name: string;
  phoneNumber: string;
  postalCode: string;
}

export interface Card {
  opaquePaymentCard: string;
  network: number;
  tokenServiceProvider: number;
  displayName: string;
  lastDigits: string;
}

export interface PushTokenizeParams {
  address: Address;
  card: Card;
}

export interface GetConstantsResponse {
  CARD_NETWORK_ELO: 12;
  TOKEN_PROVIDER_ELO: 14;
  TOKEN_STATE_ACTIVE: 5;
  TOKEN_STATE_FELICA_PENDING_PROVISIONING: 6;
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION: 3;
  TOKEN_STATE_PENDING: 2;
  TOKEN_STATE_SUSPENDED: 4;
  TOKEN_STATE_UNTOKENIZED: 1;
}

export interface Token {
  issuerTokenId: string;
  issuerName: string;
  fpanLastFour: string;
  dpanLastFour: string;
  tokenServiceProvider: GOOGLE_TOKEN_PROVIDER;
  network: GOOGLE_CARD_NETWORK;
  tokenState: GOOGLE_WALLET_STATUS_CODE;
  isDefaultToken: boolean;
  portifolioName: string;
}

export interface IsTokenizedParams {
  fpanLastFour: string;
  cardNetwork: number;
  tokenServiceProvider: number;
}

// ============================================================================
// ALIASES PARA COMPATIBILIDADE
// ============================================================================

// Mantém compatibilidade com código existente
export const WALLET_STATUS = GOOGLE_WALLET_STATUS;
export const WALLET_STATUS_CODE = GOOGLE_WALLET_STATUS_CODE;
export const STATUS_TOKEN = GOOGLE_STATUS_TOKEN;
export const CONSTANTS = GOOGLE_CONSTANTS;
export const ENVIRONMENT = GOOGLE_ENVIRONMENT;
export const TOKEN_PROVIDER = GOOGLE_TOKEN_PROVIDER;
export const CARD_NETWORK = GOOGLE_CARD_NETWORK;
