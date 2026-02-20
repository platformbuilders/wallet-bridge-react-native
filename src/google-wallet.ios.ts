// ============================================================================
// GOOGLE WALLET iOS STUB
// ============================================================================
// Este arquivo fornece implementações stub para iOS, onde o Google Wallet
// não está disponível nativamente. Todas as funções retornam valores padrão
// ou rejeitam promises com mensagens apropriadas.

import type { GoogleActivationStatus } from './enums';
import type {
    GooglePushTokenizeRequestForCard,
    GoogleTokenInfo,
    GoogleTokenStatus,
    GoogleWalletConstants,
    GoogleWalletData,
    GoogleWalletSpec,
} from './types/google-wallet.types';

// ============================================================================
// CONSTANTES STUB PARA iOS
// ============================================================================

const iOS_STUB_CONSTANTS: GoogleWalletConstants = {
  SDK_NAME: 'GoogleWallet-iOS-Stub',
  GOOGLE_WALLET_PACKAGE: 'com.google.android.gms',
  GOOGLE_WALLET_APP_PACKAGE: 'com.google.android.apps.walletnfcrel',
  GOOGLE_WALLET_PLAY_STORE_URL:
    'https://play.google.com/store/apps/details?id=com.google.android.apps.walletnfcrel',

  // Google Token Provider
  TOKEN_PROVIDER_AMEX: 1,
  TOKEN_PROVIDER_DISCOVER: 2,
  TOKEN_PROVIDER_JCB: 3,
  TOKEN_PROVIDER_MASTERCARD: 4,
  TOKEN_PROVIDER_VISA: 5,
  TOKEN_PROVIDER_ELO: 6,

  // Google Card Network
  CARD_NETWORK_AMEX: 1,
  CARD_NETWORK_DISCOVER: 2,
  CARD_NETWORK_MASTERCARD: 3,
  CARD_NETWORK_QUICPAY: 4,
  CARD_NETWORK_PRIVATE_LABEL: 5,
  CARD_NETWORK_VISA: 6,
  CARD_NETWORK_ELO: 7,

  // TapAndPay Status Codes
  TAP_AND_PAY_NO_ACTIVE_WALLET: 1,
  TAP_AND_PAY_TOKEN_NOT_FOUND: 2,
  TAP_AND_PAY_INVALID_TOKEN_STATE: 3,
  TAP_AND_PAY_ATTESTATION_ERROR: 4,
  TAP_AND_PAY_UNAVAILABLE: 5,
  TAP_AND_PAY_SAVE_CARD_ERROR: 6,
  TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION: 7,
  TAP_AND_PAY_TOKENIZATION_DECLINED: 8,
  TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR: 9,
  TAP_AND_PAY_TOKENIZE_ERROR: 10,
  TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED: 11,
  TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT: 12,
  TAP_AND_PAY_USER_CANCELED_FLOW: 13,
  TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED: 14,

  // Google Token State
  TOKEN_STATE_ACTIVE: 1,
  TOKEN_STATE_PENDING: 2,
  TOKEN_STATE_SUSPENDED: 3,
  TOKEN_STATE_UNTOKENIZED: 4,
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION: 5,
  TOKEN_STATE_FELICA_PENDING_PROVISIONING: 6,

  // Google Common Status Codes
  SUCCESS: 0,
  SUCCESS_CACHE: 1,
  SERVICE_VERSION_UPDATE_REQUIRED: 2,
  SERVICE_DISABLED: 3,
  SIGN_IN_REQUIRED: 4,
  INVALID_ACCOUNT: 5,
  RESOLUTION_REQUIRED: 6,
  NETWORK_ERROR: 7,
  INTERNAL_ERROR: 8,
  DEVELOPER_ERROR: 9,
  ERROR: 10,
  INTERRUPTED: 11,
  TIMEOUT: 12,
  CANCELED: 13,
  API_NOT_CONNECTED: 14,
  REMOTE_EXCEPTION: 15,
  CONNECTION_SUSPENDED_DURING_CALL: 16,
  RECONNECTION_TIMED_OUT_DURING_UPDATE: 17,
  RECONNECTION_TIMED_OUT: 18,
};

// ============================================================================
// IMPLEMENTAÇÃO STUB DO GOOGLE WALLET PARA iOS
// ============================================================================

class GoogleWalletiOSStub implements GoogleWalletSpec {
  private static instance: GoogleWalletiOSStub;

  private constructor() {
    console.warn(
      '⚠️ [GoogleWallet-iOS] Google Wallet não está disponível no iOS. Usando implementação stub.',
    );
  }

  public static getInstance(): GoogleWalletiOSStub {
    if (!GoogleWalletiOSStub.instance) {
      GoogleWalletiOSStub.instance = new GoogleWalletiOSStub();
    }
    return GoogleWalletiOSStub.instance;
  }

  /**
   * Verifica se o Google Wallet está disponível
   * No iOS, sempre retorna false
   */
  async checkWalletAvailability(): Promise<boolean> {
    console.log(
      '🔍 [GoogleWallet-iOS] checkWalletAvailability() - Retornando false (iOS)',
    );
    return Promise.resolve(false);
  }

  /**
   * Obtém informações seguras da carteira
   * No iOS, rejeita a promise
   */
  async getSecureWalletInfo(): Promise<GoogleWalletData> {
    console.log(
      '🔐 [GoogleWallet-iOS] getSecureWalletInfo() - Não disponível no iOS',
    );
    throw new Error('Google Wallet não está disponível no iOS');
  }

  /**
   * Obtém o status de um token
   * No iOS, rejeita a promise
   */
  async getTokenStatus(
    tokenServiceProvider: number,
    tokenReferenceId: string,
  ): Promise<GoogleTokenStatus> {
    console.log(
      '🎫 [GoogleWallet-iOS] getTokenStatus() - Não disponível no iOS',
    );
    console.log('TokenServiceProvider:', tokenServiceProvider);
    console.log('TokenReferenceId:', tokenReferenceId);
    return Promise.reject('Google Wallet não está disponível no iOS');
  }

  /**
   * Obtém o ambiente atual
   * No iOS, retorna 'iOS'
   */
  async getEnvironment(): Promise<string> {
    console.log('🌍 [GoogleWallet-iOS] getEnvironment() - Retornando iOS');
    return Promise.resolve('iOS');
  }

  /**
   * Verifica se um cartão está tokenizado
   * No iOS, sempre retorna false
   */
  async isTokenized(
    fpanLastFour: string,
    cardNetwork: number,
    tokenServiceProvider: number,
  ): Promise<boolean> {
    console.log('💳 [GoogleWallet-iOS] isTokenized() - Retornando false (iOS)');
    console.log('FpanLastFour:', fpanLastFour);
    console.log('CardNetwork:', cardNetwork);
    console.log('TokenServiceProvider:', tokenServiceProvider);
    return Promise.resolve(false);
  }

  /**
   * Visualiza informações de um token
   * No iOS, retorna null
   */
  async viewToken(
    tokenServiceProvider: number,
    issuerTokenId: string,
  ): Promise<GoogleTokenInfo | null> {
    console.log('👁️ [GoogleWallet-iOS] viewToken() - Retornando null (iOS)');
    console.log('TokenServiceProvider:', tokenServiceProvider);
    console.log('IssuerTokenId:', issuerTokenId);
    return Promise.resolve(null);
  }

  /**
   * Adiciona um cartão à carteira
   * No iOS, rejeita a promise
   */
  async addCardToWallet(
    cardData: GooglePushTokenizeRequestForCard,
  ): Promise<string | null> {
    console.log(
      '➕ [GoogleWallet-iOS] addCardToWallet() - Não disponível no iOS',
    );
    console.log('CardData:', cardData);
    return Promise.reject('Google Wallet não está disponível no iOS');
  }

  /**
   * Cria a carteira se necessário
   * No iOS, sempre retorna false
   */
  async createWalletIfNeeded(): Promise<boolean> {
    console.log(
      '🏗️ [GoogleWallet-iOS] createWalletIfNeeded() - Retornando false (iOS)',
    );
    return Promise.resolve(false);
  }

  /**
   * Lista todos os tokens
   * No iOS, retorna array vazio
   */
  async listTokens(): Promise<GoogleTokenInfo[]> {
    console.log(
      '📋 [GoogleWallet-iOS] listTokens() - Retornando array vazio (iOS)',
    );
    return [];
  }

  /**
   * Obtém as constantes do módulo
   * No iOS, retorna constantes stub
   */
  getConstants(): GoogleWalletConstants {
    console.log(
      '📊 [GoogleWallet-iOS] getConstants() - Retornando constantes stub',
    );
    return iOS_STUB_CONSTANTS;
  }

  /**
   * Configura o listener de intent
   * No iOS, sempre retorna false
   */
  async setIntentListener(): Promise<boolean> {
    console.log(
      '👂 [GoogleWallet-iOS] setIntentListener() - Retornando false (iOS)',
    );
    return Promise.resolve(false);
  }

  /**
   * Remove o listener de intent
   * No iOS, sempre retorna false
   */
  async removeIntentListener(): Promise<boolean> {
    console.log(
      '🔇 [GoogleWallet-iOS] removeIntentListener() - Retornando false (iOS)',
    );
    return Promise.resolve(false);
  }

  /**
   * Define o resultado de ativação
   * No iOS, sempre retorna false
   */
  async setActivationResult(
    status: GoogleActivationStatus,
    activationCode?: string,
  ): Promise<boolean> {
    console.log(
      '✅ [GoogleWallet-iOS] setActivationResult() - Retornando false (iOS)',
    );
    console.log('Status:', status);
    console.log('ActivationCode:', activationCode);
    return Promise.resolve(false);
  }

  /**
   * Finaliza a atividade atual
   * No iOS, sempre retorna false
   */
  async finishActivity(): Promise<boolean> {
    console.log(
      '🏁 [GoogleWallet-iOS] finishActivity() - Retornando false (iOS)',
    );
    return Promise.resolve(false);
  }

  /**
   * Abre o Google Wallet
   * No iOS, sempre retorna false
   */
  async openWallet(): Promise<boolean> {
    console.log('📱 [GoogleWallet-iOS] openWallet() - Retornando false (iOS)');
    return Promise.resolve(false);
  }

  /**
   * Ativa o listener de logs
   * No iOS, sempre retorna false
   */
  async setLogListener(): Promise<boolean> {
    console.log(
      '📝 [GoogleWallet-iOS] setLogListener() - Retornando false (iOS)',
    );
    return Promise.resolve(false);
  }

  /**
   * Remove o listener de logs
   * No iOS, sempre retorna false
   */
  async removeLogListener(): Promise<boolean> {
    console.log(
      '🔇 [GoogleWallet-iOS] removeLogListener() - Retornando false (iOS)',
    );
    return Promise.resolve(false);
  }
}

// ============================================================================
// EXPORTAÇÃO DO MÓDULO STUB
// ============================================================================

// Exporta a instância singleton do stub
export const GoogleWalletIOS = GoogleWalletiOSStub.getInstance();

// Exporta todos os tipos relacionados
export * from './types/google-wallet.types';
