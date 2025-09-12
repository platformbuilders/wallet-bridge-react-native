// ============================================================================
// TYPES ESPECÍFICOS DO SAMSUNG PAY / SAMSUNG WALLET
// ============================================================================

export enum SamsungPayStatus {
  SUCCESS = 'SUCCESS',
  FAIL = 'FAIL',
  CANCEL = 'CANCEL',
  ERROR = 'ERROR',
}

export enum SamsungPayErrorCode {
  ERROR_NONE = 'ERROR_NONE',
  ERROR_NOT_SUPPORTED = 'ERROR_NOT_SUPPORTED',
  ERROR_NOT_AVAILABLE = 'ERROR_NOT_AVAILABLE',
  ERROR_INVALID_PARAMETER = 'ERROR_INVALID_PARAMETER',
  ERROR_NETWORK_ERROR = 'ERROR_NETWORK_ERROR',
  ERROR_SERVICE_DISCONNECTED = 'ERROR_SERVICE_DISCONNECTED',
  ERROR_SERVICE_UNAVAILABLE = 'ERROR_SERVICE_UNAVAILABLE',
  ERROR_UNKNOWN = 'ERROR_UNKNOWN',
}

export enum SamsungCardBrand {
  VISA = 'VISA',
  MASTERCARD = 'MASTERCARD',
  AMEX = 'AMEX',
  DISCOVER = 'DISCOVER',
  JCB = 'JCB',
  ELO = 'ELO',
}

export enum SamsungCardType {
  CREDIT = 'CREDIT',
  DEBIT = 'DEBIT',
  PREPAID = 'PREPAID',
}

export enum SamsungCardStatus {
  ACTIVE = 'ACTIVE',
  PENDING = 'PENDING',
  SUSPENDED = 'SUSPENDED',
  DEACTIVATED = 'DEACTIVATED',
  NOT_FOUND = 'NOT_FOUND',
}

// Samsung Pay - CardData (baseado no SDK do Samsung Pay)
export interface SamsungCardData {
  cardId: string;
  cardBrand: SamsungCardBrand;
  cardType: SamsungCardType;
  cardLast4Fpan: string;
  cardLast4Dpan: string;
  cardIssuer: string;
  cardStatus: SamsungCardStatus;
  isSamsungPayCard: boolean;
}

// Samsung Pay - TokenInfo simplificado para listTokens
export interface SamsungTokenInfoSimple {
  cardId: string;
  cardLast4Fpan: string;
  cardIssuer: string;
  cardStatus: string;
  cardBrand: string;
}

// Samsung Pay - UserInfo (baseado no SDK do Samsung Pay)
export interface SamsungUserInfo {
  userId: string;
  userName: string;
  userEmail: string;
  userPhone: string;
}

// Samsung Pay - Address (baseado no SDK do Samsung Pay)
export interface SamsungAddress {
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  countryCode: string;
  postalCode: string;
}

// Samsung Pay - WalletData
export interface SamsungWalletData {
  deviceID: string;
  walletAccountID: string;
  userInfo: SamsungUserInfo;
}

// Samsung Pay - Constants
export interface SamsungWalletConstants {
  SDK_AVAILABLE: boolean;
  SDK_NAME: string;
  MODULE_NAME: string;
}

// Samsung Pay - TokenStatus
export interface SamsungTokenStatus {
  tokenState: number;
  isSelected: boolean;
}

// Samsung Pay - Interface do Módulo
export interface SamsungWalletSpec {
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<SamsungWalletData>;
  getTokenStatus(tokenServiceProvider: number, tokenReferenceId: string): Promise<SamsungTokenStatus>;
  getEnvironment(): Promise<string>;
  isTokenized(fpanLastFour: string, cardNetwork: number, tokenServiceProvider: number): Promise<boolean>;
  addCardToWallet(cardData: SamsungCardData): Promise<string>;
  createWalletIfNeeded(): Promise<boolean>;
  listTokens(): Promise<SamsungTokenInfoSimple[]>;
  getConstants(): Promise<SamsungWalletConstants>;
}

// Samsung Pay - Interface de Compatibilidade (para código existente)
export interface SamsungWalletCompatibilitySpec {
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<any>; // Aceita qualquer tipo para compatibilidade
  getTokenStatus(tokenServiceProvider: number, tokenReferenceId: string): Promise<SamsungTokenStatus>;
  getEnvironment(): Promise<string>;
  isTokenized(fpanLastFour: string, cardNetwork: number, tokenServiceProvider: number): Promise<boolean>;
  addCardToWallet(cardData: any): Promise<string>; // Aceita qualquer tipo para compatibilidade
  createWalletIfNeeded(): Promise<boolean>;
  listTokens(): Promise<SamsungTokenInfoSimple[]>;
  getConstants(): Promise<any>; // Aceita qualquer tipo para compatibilidade
}
