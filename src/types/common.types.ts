// ============================================================================
// TYPES COMUNS E DE COMPATIBILIDADE
// ============================================================================

// ============================================================================
// TYPES GENÉRICOS (mantidos para compatibilidade)
// ============================================================================

export enum CardStatus {
  NOT_FOUND = 'not found',
  ACTIVE = 'active',
  REQUIRE_AUTHORIZATION = 'requireAuthorization',
  PENDING = 'pending',
  SUSPENDED = 'suspended',
  DEACTIVATED = 'deactivated',
}

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

import {
  GoogleWalletStatus,
  GoogleWalletStatusCode,
  GoogleTokenState,
  GoogleTokenProvider,
  GoogleCardNetwork,
  GoogleEnvironment,
} from './google-wallet.types';

import {
  SamsungPayStatus,
  SamsungPayErrorCode,
  SamsungCardBrand,
  SamsungCardType,
  SamsungCardStatus,
} from './samsung-wallet.types';

// ============================================================================
// ALIASES PARA COMPATIBILIDADE COM CÓDIGO EXISTENTE
// ============================================================================

// Mantém compatibilidade com código existente - Google Wallet
export const GOOGLE_WALLET_STATUS = GoogleWalletStatus;
export const GOOGLE_WALLET_STATUS_CODE = GoogleWalletStatusCode;
export const GOOGLE_STATUS_TOKEN = GoogleTokenState;
export const GOOGLE_TOKEN_PROVIDER = GoogleTokenProvider;
export const GOOGLE_CARD_NETWORK = GoogleCardNetwork;
export const GOOGLE_ENVIRONMENT = GoogleEnvironment;

// Mantém compatibilidade com código existente - Samsung Pay
export const SAMSUNG_PAY_STATUS = SamsungPayStatus;
export const SAMSUNG_PAY_ERROR_CODE = SamsungPayErrorCode;
export const SAMSUNG_CARD_BRAND = SamsungCardBrand;
export const SAMSUNG_CARD_TYPE = SamsungCardType;
export const SAMSUNG_CARD_STATUS = SamsungCardStatus;

// Aliases para compatibilidade com código existente (Google como padrão)
export const WALLET_STATUS = GoogleWalletStatus;
export const WALLET_STATUS_CODE = GoogleWalletStatusCode;
export const STATUS_TOKEN = GoogleTokenState;
export const TOKEN_PROVIDER = GoogleTokenProvider;
export const CARD_NETWORK = GoogleCardNetwork;
export const ENVIRONMENT = GoogleEnvironment;

// ============================================================================
// TYPES DE COMPATIBILIDADE (mantidos para compatibilidade com código existente)
// ============================================================================

// Mantém compatibilidade com código existente - Google Wallet
export const GOOGLE_CONSTANTS = {
  'TOKEN_PROVIDER_ELO': 'TOKEN_PROVIDER_ELO',
  'CARD_NETWORK_ELO': 'CARD_NETWORK_ELO',
  'TOKEN_STATE_UNTOKENIZED': 'TOKEN_STATE_UNTOKENIZED',
  'TOKEN_STATE_PENDING': 'TOKEN_STATE_PENDING',
  'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION': 'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION',
  'TOKEN_STATE_SUSPENDED': 'TOKEN_STATE_SUSPENDED',
  'TOKEN_STATE_ACTIVE': 'TOKEN_STATE_ACTIVE',
  'TOKEN_STATE_FELICA_PENDING_PROVISIONING': 'TOKEN_STATE_FELICA_PENDING_PROVISIONING',
} as const;

// ============================================================================
// TYPES DE COMPATIBILIDADE COM API EXISTENTE
// ============================================================================

// Mantém compatibilidade com código existente - Google Wallet
export interface PaymentCard {
  opaquePaymentCard: string;
  network: number;
  tokenServiceProvider: number;
  displayName: string;
  lastDigits: string;
}

export interface PushTokenizeRequest {
  address: {
    address1: string;
    address2: string;
    countryCode: string;
    locality: string;
    administrativeArea: string;
    name: string;
    phoneNumber: string;
    postalCode: string;
  };
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
  CARD_NETWORK_ELO: number;
  TOKEN_PROVIDER_ELO: number;
  TOKEN_STATE_ACTIVE: number;
  TOKEN_STATE_FELICA_PENDING_PROVISIONING: number;
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION: number;
  TOKEN_STATE_PENDING: number;
  TOKEN_STATE_SUSPENDED: number;
  TOKEN_STATE_UNTOKENIZED: number;
}

export interface Token {
  issuerTokenId: string;
  issuerName: string;
  fpanLastFour: string;
  dpanLastFour: string;
  tokenServiceProvider: GoogleTokenProvider;
  network: GoogleCardNetwork;
  tokenState: GoogleWalletStatusCode;
  isDefaultToken: boolean;
  portifolioName: string;
}

export interface IsTokenizedParams {
  fpanLastFour: string;
  cardNetwork: number;
  tokenServiceProvider: number;
}
