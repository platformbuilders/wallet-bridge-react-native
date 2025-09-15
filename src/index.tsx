import { NativeEventEmitter } from 'react-native';
import  { GoogleWalletModule, SamsungWalletModule } from './NativeBuildersWallet';
import type {
  WalletData,
  GoogleWalletCompatibilitySpec,
  SamsungWalletCompatibilitySpec,
  GoogleWalletIntentEvent,
} from './types/index';

// Re-exporta types utilizados
export type {
  GetConstantsResponse,
  WalletData,
  AndroidCardData,
  CardStatus,
  GoogleWalletCompatibilitySpec,
  SamsungWalletCompatibilitySpec,
  GoogleWalletIntentEvent,
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

  getEnvironment(): Promise<string> {
    return GoogleWalletModule.getEnvironment();
  }

  isTokenized(fpanLastFour: string, cardNetwork: number, tokenServiceProvider: number): Promise<boolean> {
    return GoogleWalletModule.isTokenized(fpanLastFour, cardNetwork, tokenServiceProvider);
  }

  viewToken(tokenServiceProvider: number, issuerTokenId: string): Promise<boolean> {
    return GoogleWalletModule.viewToken(tokenServiceProvider, issuerTokenId);
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

  setIntentListener(): Promise<boolean> {
    return GoogleWalletModule.setIntentListener();
  }

  removeIntentListener(): Promise<boolean> {
    return GoogleWalletModule.removeIntentListener();
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

  getEnvironment(): Promise<string> {
    return SamsungWalletModule.getEnvironment();
  }

  isTokenized(fpanLastFour: string, cardNetwork: number, tokenServiceProvider: number): Promise<boolean> {
    return SamsungWalletModule.isTokenized(fpanLastFour, cardNetwork, tokenServiceProvider);
  }

  viewToken(tokenServiceProvider: number, issuerTokenId: string): Promise<boolean> {
    return SamsungWalletModule.viewToken(tokenServiceProvider, issuerTokenId);
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

// ============================================================================
// EVENT EMITTER PARA GOOGLE WALLET
// ============================================================================

export class GoogleWalletEventEmitter {
  private eventEmitter: NativeEventEmitter;
  private listeners: Map<string, (event: GoogleWalletIntentEvent) => void> = new Map();

  constructor() {
    // Usar o módulo nativo diretamente para o EventEmitter
    const { GoogleWallet } = require('react-native').NativeModules;
    this.eventEmitter = new NativeEventEmitter(GoogleWallet);
  }

  /**
   * Adiciona um listener para eventos de intent do Google Wallet
   * @param callback Função que será chamada quando um evento for recebido
   * @returns Função para remover o listener
   */
  addIntentListener(callback: (event: GoogleWalletIntentEvent) => void): () => void {
    const listenerId = `listener_${Date.now()}_${Math.random()}`;
    
    // Armazenar o callback
    this.listeners.set(listenerId, callback);
    
    // Criar o listener do NativeEventEmitter
    const subscription = this.eventEmitter.addListener('GoogleWalletIntentReceived', (event: any) => {
      const walletEvent = event as GoogleWalletIntentEvent;
      console.log('🎯 [GoogleWalletEventEmitter] Intent recebido:', walletEvent);
      callback(walletEvent);
    });

    // Retornar função de cleanup
    return () => {
      this.listeners.delete(listenerId);
      subscription.remove();
    };
  }

  /**
   * Remove todos os listeners ativos
   */
  removeAllListeners(): void {
    this.listeners.clear();
    this.eventEmitter.removeAllListeners('GoogleWalletIntentReceived');
  }

  /**
   * Obtém o número de listeners ativos
   */
  getListenerCount(): number {
    return this.listeners.size;
  }
}
