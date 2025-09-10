import { NativeModules } from 'react-native';
import type {
  // Types genéricos da API unificada
  CardStatus,
  WalletData,
  AndroidCardData,
  UserAddress,
  // Types de compatibilidade
  PaymentCard,
  PushTokenizeRequest,
  GetTokenStatusParams,
  ViewTokenParams,
  Address,
  Card,
  PushTokenizeParams,
  GetConstantsResponse,
  Token,
  IsTokenizedParams,
  // Types para módulos específicos
  GoogleWalletSpec,
  SamsungWalletSpec,
} from './types/index';
import {
  // Types específicos do Google Pay
  GOOGLE_WALLET_STATUS,
  GOOGLE_WALLET_STATUS_CODE,
  GOOGLE_STATUS_TOKEN,
  GOOGLE_CONSTANTS,
  GOOGLE_ENVIRONMENT,
  GOOGLE_TOKEN_PROVIDER,
  GOOGLE_CARD_NETWORK,
} from './types/index';

// ============================================================================
// RE-EXPORTS PARA COMPATIBILIDADE
// ============================================================================

// Re-exporta os enums com nomes originais para manter compatibilidade
export {
  GOOGLE_WALLET_STATUS as WALLET_STATUS,
  GOOGLE_WALLET_STATUS_CODE as WALLET_STATUS_CODE,
  GOOGLE_STATUS_TOKEN as STATUS_TOKEN,
  GOOGLE_CONSTANTS as CONSTANTS,
  GOOGLE_ENVIRONMENT as ENVIRONMENT,
  GOOGLE_TOKEN_PROVIDER as TOKEN_PROVIDER,
  GOOGLE_CARD_NETWORK as CARD_NETWORK,
};

// Re-exporta types
export type {
  CardStatus,
  WalletData,
  AndroidCardData,
  UserAddress,
  PaymentCard,
  PushTokenizeRequest,
  GetTokenStatusParams,
  ViewTokenParams,
  Address,
  Card,
  PushTokenizeParams,
  GetConstantsResponse,
  Token,
  IsTokenizedParams,
  GoogleWalletSpec,
  SamsungWalletSpec,
};

// ============================================================================
// MÓDULOS ESPECÍFICOS
// ============================================================================

const { GoogleWallet, SamsungWallet } = NativeModules;

// Verificação de disponibilidade dos módulos
if (!GoogleWallet) {
  console.warn(
    'GoogleWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.'
  );
}

if (!SamsungWallet) {
  console.warn(
    'SamsungWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.'
  );
}

// Exporta os módulos específicos
export const GoogleWalletModule = GoogleWallet as GoogleWalletSpec;
export const SamsungWalletModule = SamsungWallet as SamsungWalletSpec;

// ============================================================================
// INTERFACE PRINCIPAL DA BIBLIOTECA (MANTIDA PARA COMPATIBILIDADE)
// ============================================================================

// Interface simplificada com apenas os métodos essenciais
export interface Spec {
  // Métodos da nova API simplificada
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<WalletData>;
  getCardStatusBySuffix(lastDigits: string): Promise<CardStatus>;
  getCardStatusByIdentifier(
    identifier: string,
    tsp: number
  ): Promise<CardStatus>;
  addCardToWallet(cardData: AndroidCardData): Promise<string>;
  createWalletIfNeeded(): Promise<boolean>;

  // Métodos de gerenciamento de módulos
  getAvailableWallets(): Promise<{
    modules: string[];
    moduleNames: string[];
    currentModule: string;
  }>;
  switchWallet(walletType: string): Promise<string>;
  // Método para obter constantes
  getConstants(): Promise<GetConstantsResponse>;
}
