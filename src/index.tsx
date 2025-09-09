import BuildersWallet, { GoogleWalletModule, SamsungWalletModule } from './NativeBuildersWallet';
import type {
  GetConstantsResponse,
  WalletData,
  AndroidCardData,
  CardStatus,
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

// GoogleWallet - Classe específica para Google Pay
export class GoogleWallet implements GoogleWalletCompatibilitySpec {
  checkWalletAvailability(): Promise<boolean> {
    return GoogleWalletModule.checkWalletAvailability();
  }

  getSecureWalletInfo(): Promise<WalletData> {
    return GoogleWalletModule.getSecureWalletInfo();
  }

  getCardStatusBySuffix(lastDigits: string): Promise<any> {
    return GoogleWalletModule.getCardStatusBySuffix(lastDigits);
  }

  getCardStatusByIdentifier(identifier: string, tsp: number): Promise<any> {
    return GoogleWalletModule.getCardStatusByIdentifier(identifier, tsp);
  }

  addCardToWallet(cardData: any): Promise<string> {
    return GoogleWalletModule.addCardToWallet(cardData);
  }

  createWalletIfNeeded(): Promise<boolean> {
    return GoogleWalletModule.createWalletIfNeeded();
  }

  getConstants(): Promise<any> {
    return GoogleWalletModule.getConstants();
  }
}

// SamsungWallet - Classe específica para Samsung Pay
export class SamsungWallet implements SamsungWalletCompatibilitySpec {
  checkWalletAvailability(): Promise<boolean> {
    return SamsungWalletModule.checkWalletAvailability();
  }

  getSecureWalletInfo(): Promise<WalletData> {
    return SamsungWalletModule.getSecureWalletInfo();
  }

  getCardStatusBySuffix(lastDigits: string): Promise<any> {
    return SamsungWalletModule.getCardStatusBySuffix(lastDigits);
  }

  getCardStatusByIdentifier(identifier: string, tsp: number): Promise<any> {
    return SamsungWalletModule.getCardStatusByIdentifier(identifier, tsp);
  }

  addCardToWallet(cardData: any): Promise<string> {
    return SamsungWalletModule.addCardToWallet(cardData);
  }

  createWalletIfNeeded(): Promise<boolean> {
    return SamsungWalletModule.createWalletIfNeeded();
  }

  getConstants(): Promise<any> {
    return SamsungWalletModule.getConstants();
  }
}

// ============================================================================
// API UNIFICADA (MANTIDA PARA COMPATIBILIDADE)
// ============================================================================

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

export function createWalletIfNeeded(): Promise<boolean> {
  return BuildersWallet.createWalletIfNeeded();
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
