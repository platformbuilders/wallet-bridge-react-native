// ============================================================================
// SAMSUNG WALLET iOS STUB
// ============================================================================
// Este arquivo fornece implementações stub para iOS, onde o Samsung Wallet
// não está disponível nativamente. Todas as funções retornam valores padrão
// ou rejeitam promises com mensagens apropriadas.

import type {
  SamsungWalletSpec,
  SamsungCard,
  SamsungWalletInfo,
  SamsungWalletConstants,
} from './types/samsung-wallet.types';
import type { SamsungActivationStatus } from './enums';

// ============================================================================
// CONSTANTES STUB PARA iOS
// ============================================================================

const iOS_STUB_CONSTANTS: SamsungWalletConstants = {
  SDK_NAME: 'SamsungWallet-iOS-Stub',
  SAMSUNG_PAY_PACKAGE: 'com.samsung.android.spay',
  SAMSUNG_PAY_PLAY_STORE_URL:
    'https://play.google.com/store/apps/details?id=com.samsung.android.spay',

  // Samsung Pay Status Codes
  SPAY_READY: 0,
  SPAY_NOT_READY: 1,
  SPAY_NOT_SUPPORTED: 2,
  SPAY_NOT_ALLOWED_TEMPORALLY: 3,
  SPAY_HAS_TRANSIT_CARD: 4,
  SPAY_HAS_NO_TRANSIT_CARD: 5,

  // Samsung Card Types
  CARD_TYPE: 'CARD_TYPE',
  CARD_TYPE_CREDIT_DEBIT: 'CREDIT_DEBIT',
  CARD_TYPE_GIFT: 'GIFT',
  CARD_TYPE_LOYALTY: 'LOYALTY',
  CARD_TYPE_CREDIT: 'CREDIT',
  CARD_TYPE_DEBIT: 'DEBIT',
  CARD_TYPE_TRANSIT: 'TRANSIT',
  CARD_TYPE_VACCINE_PASS: 'VACCINE_PASS',

  // Samsung Card States
  ACTIVE: 'ACTIVE',
  DISPOSED: 'DISPOSED',
  EXPIRED: 'EXPIRED',
  PENDING_ENROLLED: 'PENDING_ENROLLED',
  PENDING_PROVISION: 'PENDING_PROVISION',
  SUSPENDED: 'SUSPENDED',
  PENDING_ACTIVATION: 'PENDING_ACTIVATION',

  // Samsung Tokenization Providers
  PROVIDER_VISA: 'VISA',
  PROVIDER_MASTERCARD: 'MASTERCARD',
  PROVIDER_AMEX: 'AMEX',
  PROVIDER_DISCOVER: 'DISCOVER',
  PROVIDER_PLCC: 'PLCC',
  PROVIDER_GIFT: 'GIFT',
  PROVIDER_LOYALTY: 'LOYALTY',
  PROVIDER_PAYPAL: 'PAYPAL',
  PROVIDER_GEMALTO: 'GEMALTO',
  PROVIDER_NAPAS: 'NAPAS',
  PROVIDER_MIR: 'MIR',
  PROVIDER_PAGOBANCOMAT: 'PAGOBANCOMAT',
  PROVIDER_VACCINE_PASS: 'VACCINE_PASS',
  PROVIDER_MADA: 'MADA',
  PROVIDER_ELO: 'ELO',

  // Samsung Error Codes
  ERROR_NONE: 0,
  ERROR_SPAY_INTERNAL: 1,
  ERROR_INVALID_INPUT: 2,
  ERROR_NOT_SUPPORTED: 3,
  ERROR_NOT_FOUND: 4,
  ERROR_ALREADY_DONE: 5,
  ERROR_NOT_ALLOWED: 6,
  ERROR_USER_CANCELED: 7,
  ERROR_PARTNER_SDK_API_LEVEL: 8,
  ERROR_PARTNER_SERVICE_TYPE: 9,
  ERROR_INVALID_PARAMETER: 10,
  ERROR_NO_NETWORK: 11,
  ERROR_SERVER_NO_RESPONSE: 12,
  ERROR_PARTNER_INFO_INVALID: 13,
  ERROR_INITIATION_FAIL: 14,
  ERROR_REGISTRATION_FAIL: 15,
  ERROR_DUPLICATED_SDK_API_CALLED: 16,
  ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION: 17,
  ERROR_SERVICE_ID_INVALID: 18,
  ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION: 19,
  ERROR_PARTNER_APP_SIGNATURE_MISMATCH: 20,
  ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED: 21,
  ERROR_PARTNER_APP_BLOCKED: 22,
  ERROR_USER_NOT_REGISTERED_FOR_DEBUG: 23,
  ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE: 24,
  ERROR_PARTNER_NOT_APPROVED: 25,
  ERROR_UNAUTHORIZED_REQUEST_TYPE: 26,
  ERROR_EXPIRED_OR_INVALID_DEBUG_KEY: 27,
  ERROR_SERVER_INTERNAL: 28,
  ERROR_DEVICE_NOT_SAMSUNG: 29,
  ERROR_SPAY_PKG_NOT_FOUND: 30,
  ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE: 31,
  ERROR_DEVICE_INTEGRITY_CHECK_FAIL: 32,
  ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL: 33,
  ERROR_ANDROID_PLATFORM_CHECK_FAIL: 34,
  ERROR_MISSING_INFORMATION: 35,
  ERROR_SPAY_SETUP_NOT_COMPLETED: 36,
  ERROR_SPAY_APP_NEED_TO_UPDATE: 37,
  ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED: 38,
  ERROR_UNABLE_TO_VERIFY_CALLER: 39,
  ERROR_SPAY_FMM_LOCK: 40,
  ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY: 41,
};

// ============================================================================
// IMPLEMENTAÇÃO STUB DO SAMSUNG WALLET PARA iOS
// ============================================================================

class SamsungWalletiOSStub implements SamsungWalletSpec {
  private static instance: SamsungWalletiOSStub;

  private constructor() {
    console.warn(
      '⚠️ [SamsungWallet-iOS] Samsung Wallet não está disponível no iOS. Usando implementação stub.'
    );
  }

  public static getInstance(): SamsungWalletiOSStub {
    if (!SamsungWalletiOSStub.instance) {
      SamsungWalletiOSStub.instance = new SamsungWalletiOSStub();
    }
    return SamsungWalletiOSStub.instance;
  }

  /**
   * Inicializa o Samsung Wallet
   * No iOS, sempre retorna false
   */
  async init(serviceId: string): Promise<boolean> {
    console.log(
      `🚀 [SamsungWallet-iOS] init(${serviceId}) - Retornando false (iOS)`
    );
    return Promise.resolve(false);
  }

  /**
   * Obtém o status do Samsung Pay
   * No iOS, sempre retorna SPAY_NOT_SUPPORTED
   */
  async getSamsungPayStatus(): Promise<number> {
    console.log(
      '📊 [SamsungWallet-iOS] getSamsungPayStatus() - Retornando SPAY_NOT_SUPPORTED (iOS)'
    );
    return Promise.resolve(iOS_STUB_CONSTANTS.SPAY_NOT_SUPPORTED);
  }

  /**
   * Navega para a página de atualização
   * No iOS, não faz nada
   */
  goToUpdatePage(): void {
    console.log(
      '🔄 [SamsungWallet-iOS] goToUpdatePage() - Não disponível no iOS'
    );
  }

  /**
   * Ativa o Samsung Pay
   * No iOS, não faz nada
   */
  activateSamsungPay(): void {
    console.log(
      '⚡ [SamsungWallet-iOS] activateSamsungPay() - Não disponível no iOS'
    );
  }

  /**
   * Obtém todos os cartões
   * No iOS, retorna array vazio
   */
  async getAllCards(): Promise<SamsungCard[]> {
    console.log(
      '💳 [SamsungWallet-iOS] getAllCards() - Retornando array vazio (iOS)'
    );
    return Promise.resolve([]);
  }

  /**
   * Obtém informações da carteira
   * No iOS, rejeita a promise
   */
  async getWalletInfo(): Promise<SamsungWalletInfo> {
    console.log(
      '👛 [SamsungWallet-iOS] getWalletInfo() - Não disponível no iOS'
    );
    return Promise.reject('Samsung Wallet não está disponível no iOS');
  }

  /**
   * Adiciona um cartão
   * No iOS, rejeita a promise
   */
  async addCard(
    payload: string,
    issuerId: string,
    tokenizationProvider: string,
    cardType: string
  ): Promise<SamsungCard> {
    console.log('➕ [SamsungWallet-iOS] addCard() - Não disponível no iOS');
    console.log('Payload:', payload);
    console.log('IssuerId:', issuerId);
    console.log('TokenizationProvider:', tokenizationProvider);
    console.log('CardType:', cardType);
    return Promise.reject('Samsung Wallet não está disponível no iOS');
  }

  /**
   * Verifica se o Samsung Wallet está disponível
   * No iOS, sempre retorna false
   */
  async checkWalletAvailability(): Promise<boolean> {
    console.log(
      '🔍 [SamsungWallet-iOS] checkWalletAvailability() - Retornando false (iOS)'
    );
    return Promise.resolve(false);
  }

  /**
   * Obtém as constantes do módulo
   * No iOS, retorna constantes stub
   */
  getConstants(): SamsungWalletConstants {
    console.log(
      '📊 [SamsungWallet-iOS] getConstants() - Retornando constantes stub'
    );
    return iOS_STUB_CONSTANTS;
  }

  /**
   * Configura o listener de intent
   * No iOS, sempre retorna false
   */
  async setIntentListener(): Promise<boolean> {
    console.log(
      '👂 [SamsungWallet-iOS] setIntentListener() - Retornando false (iOS)'
    );
    return Promise.resolve(false);
  }

  /**
   * Remove o listener de intent
   * No iOS, sempre retorna false
   */
  async removeIntentListener(): Promise<boolean> {
    console.log(
      '🔇 [SamsungWallet-iOS] removeIntentListener() - Retornando false (iOS)'
    );
    return Promise.resolve(false);
  }

  /**
   * Define o resultado de ativação
   * No iOS, sempre retorna false
   */
  async setActivationResult(
    status: SamsungActivationStatus,
    activationCode?: string
  ): Promise<boolean> {
    console.log(
      '✅ [SamsungWallet-iOS] setActivationResult() - Retornando false (iOS)'
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
      '🏁 [SamsungWallet-iOS] finishActivity() - Retornando false (iOS)'
    );
    return Promise.resolve(false);
  }

  /**
   * Abre o Samsung Wallet
   * No iOS, sempre retorna false
   */
  async openWallet(): Promise<boolean> {
    console.log('📱 [SamsungWallet-iOS] openWallet() - Retornando false (iOS)');
    return Promise.resolve(false);
  }
}

// ============================================================================
// EXPORTAÇÃO DO MÓDULO STUB
// ============================================================================

// Exporta a instância singleton do stub
export const SamsungWalletIOS = SamsungWalletiOSStub.getInstance();

// Exporta todos os tipos relacionados
export * from './types/samsung-wallet.types';
