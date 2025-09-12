import  { GoogleWalletModule, SamsungWalletModule } from './NativeBuildersWallet';
import type {
  WalletData,
  GoogleWalletCompatibilitySpec,
  SamsungWalletCompatibilitySpec,
} from './types/index';

// Re-exporta types utilizados
export type {
  GetConstantsResponse,
  WalletData,
  AndroidCardData,
  CardStatus,
  GoogleWalletCompatibilitySpec,
  SamsungWalletCompatibilitySpec,
  // Types específicos do Google Pay
  GOOGLE_WALLET_STATUS,
  GOOGLE_WALLET_STATUS_CODE,
  GOOGLE_STATUS_TOKEN,
  GOOGLE_CONSTANTS,
  GOOGLE_ENVIRONMENT,
  GOOGLE_TOKEN_PROVIDER,
  GOOGLE_CARD_NETWORK,
} from './types/index';

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

// ============================================================================
// MÓDULOS ESPECÍFICOS
// ============================================================================

// GoogleWalletClient - Classe específica para Google Pay
export class GoogleWalletClient implements GoogleWalletCompatibilitySpec {
  checkWalletAvailability(): Promise<boolean> {
    return GoogleWalletModule.checkWalletAvailability();
  }

  getSecureWalletInfo(): Promise<WalletData> {
    return GoogleWalletModule.getSecureWalletInfo();
  }

  getTokenStatus(tokenServiceProvider: number, tokenReferenceId: string): Promise<any> {
    return GoogleWalletModule.getTokenStatus(tokenServiceProvider, tokenReferenceId);
  }

  addCardToWallet(cardData: any): Promise<string> {
    return GoogleWalletModule.addCardToWallet(cardData);
  }

  createWalletIfNeeded(): Promise<boolean> {
    return GoogleWalletModule.createWalletIfNeeded();
  }

  listTokens(): Promise<any[]> {
    return GoogleWalletModule.listTokens();
  }

  getConstants(): any {
    return GoogleWalletModule.getConstants();
  }
}

// SamsungWalletClient - Classe específica para Samsung Pay
export class SamsungWalletClient implements SamsungWalletCompatibilitySpec {
  checkWalletAvailability(): Promise<boolean> {
    return SamsungWalletModule.checkWalletAvailability();
  }

  getSecureWalletInfo(): Promise<WalletData> {
    return SamsungWalletModule.getSecureWalletInfo();
  }

  getTokenStatus(tokenServiceProvider: number, tokenReferenceId: string): Promise<any> {
    return SamsungWalletModule.getTokenStatus(tokenServiceProvider, tokenReferenceId);
  }

  addCardToWallet(cardData: any): Promise<string> {
    return SamsungWalletModule.addCardToWallet(cardData);
  }

  createWalletIfNeeded(): Promise<boolean> {
    return SamsungWalletModule.createWalletIfNeeded();
  }

  listTokens(): Promise<any[]> {
    return SamsungWalletModule.listTokens();
  }

  getConstants(): Promise<any> {
    return SamsungWalletModule.getConstants();
  }
}
