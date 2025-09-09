import BuildersWallet from './NativeBuildersWallet';
import type {
  GetConstantsResponse,
  WalletData,
  AndroidCardData,
  CardStatus,
} from './types';

// Re-exporta types utilizados
export type {
  GetConstantsResponse,
  WalletData,
  AndroidCardData,
  CardStatus,
  // Types específicos do Google Pay
  GOOGLE_WALLET_STATUS,
  GOOGLE_WALLET_STATUS_CODE,
  GOOGLE_STATUS_TOKEN,
  GOOGLE_CONSTANTS,
  GOOGLE_ENVIRONMENT,
  GOOGLE_TOKEN_PROVIDER,
  GOOGLE_CARD_NETWORK,
} from './types';

// Re-exporta enums com nomes de compatibilidade
export {
  WALLET_STATUS,
  WALLET_STATUS_CODE,
  STATUS_TOKEN,
  CONSTANTS,
  ENVIRONMENT,
  TOKEN_PROVIDER,
  CARD_NETWORK,
} from './NativeBuildersWallet';

// Novos métodos da API simplificada
export function checkWalletAvailability(): Promise<boolean> {
  return BuildersWallet.checkWalletAvailability();
}

export function getSecureWalletInfo(): Promise<WalletData> {
  return BuildersWallet.getSecureWalletInfo();
}

export function getCardStatusBySuffix(lastDigits: string): Promise<CardStatus> {
  return BuildersWallet.getCardStatusBySuffix(lastDigits);
}

export function getCardStatusByIdentifier(
  identifier: string,
  tsp: number
): Promise<CardStatus> {
  return BuildersWallet.getCardStatusByIdentifier(identifier, tsp);
}

export function addCardToWallet(cardData: AndroidCardData): Promise<string> {
  return BuildersWallet.addCardToWallet(cardData);
}

export function getAvailableWallets(): Promise<{
  modules: string[];
  moduleNames: string[];
  currentModule: string;
}> {
  return BuildersWallet.getAvailableWallets();
}

export function switchWallet(walletType: string): Promise<string> {
  return BuildersWallet.switchWallet(walletType);
}

export function getConstants(): Promise<GetConstantsResponse> {
  return BuildersWallet.getConstants();
}
