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
} from './types';
import {
  // Types específicos do Google Pay
  GOOGLE_WALLET_STATUS,
  GOOGLE_WALLET_STATUS_CODE,
  GOOGLE_STATUS_TOKEN,
  GOOGLE_CONSTANTS,
  GOOGLE_ENVIRONMENT,
  GOOGLE_TOKEN_PROVIDER,
  GOOGLE_CARD_NETWORK,
} from './types';

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
};

// ============================================================================
// INTERFACE PRINCIPAL DA BIBLIOTECA
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

const { BuildersWallet } = NativeModules;

if (!BuildersWallet) {
  throw new Error(
    'BuildersWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.'
  );
}

export default BuildersWallet as Spec;
