// ============================================================================
// TYPES ESPECÍFICOS DO SAMSUNG PAY / SAMSUNG WALLET
// ============================================================================

// Samsung Pay - Card (baseado na classe Card do SDK e SerializableCard)
export interface SamsungCard {
  // Campos básicos do Card
  cardId: string;
  cardStatus: string;
  cardBrand: string;

  // Campos do cardInfo Bundle (Samsung Pay específicos)
  last4FPan?: string;
  last4DPan?: string;
  app2AppPayload?: string;
  cardType?: string;
  issuerName?: string;
  isDefaultCard?: string;
  deviceType?: string;
  memberID?: string;
  countryCode?: string;
  cryptogramType?: string;
  requireCpf?: string;
  cpfHolderName?: string;
  cpfNumber?: string;
  merchantRefId?: string;
  transactionType?: string;

  // Campos de compatibilidade (outros SDKs)
  last4?: string;
  tokenizationProvider?: string | number;
  network?: string | number;
  displayName?: string;

  // Bundle cardInfo original (para compatibilidade)
  cardInfo?: Record<string, any>;
}

// Samsung Pay - WalletInfo (baseado no getWalletInfo)
export interface SamsungWalletInfo {
  walletDMId: string;
  deviceId: string;
  walletUserId: string;
}

// Samsung Pay - AddCardInfo (baseado no AddCardInfo)
export interface SamsungAddCardInfo {
  payload: string;
  issuerId: string;
  tokenizationProvider: string;
}

// Samsung Pay - Constants (apenas tipos, valores vêm da bridge)
export interface SamsungWalletConstants {
  SDK_NAME: string;
  useMock: boolean;

  // Samsung Pay Status Codes
  SPAY_READY: number;
  SPAY_NOT_READY: number;
  SPAY_NOT_SUPPORTED: number;
  SPAY_NOT_ALLOWED_TEMPORALLY: number;
  SPAY_HAS_TRANSIT_CARD: number;
  SPAY_HAS_NO_TRANSIT_CARD: number;

  // Samsung Card Types (da classe Card)
  CARD_TYPE: string;
  CARD_TYPE_CREDIT_DEBIT: string;
  CARD_TYPE_GIFT: string;
  CARD_TYPE_LOYALTY: string;
  CARD_TYPE_CREDIT: string;
  CARD_TYPE_DEBIT: string;
  CARD_TYPE_TRANSIT: string;
  CARD_TYPE_VACCINE_PASS: string;

  // Samsung Card States (da classe Card)
  ACTIVE: string;
  DISPOSED: string;
  EXPIRED: string;
  PENDING_ENROLLED: string;
  PENDING_PROVISION: string;
  SUSPENDED: string;
  PENDING_ACTIVATION: string;

  // Samsung Tokenization Providers
  PROVIDER_VISA: string;
  PROVIDER_MASTERCARD: string;
  PROVIDER_AMEX: string;
  PROVIDER_DISCOVER: string;
  PROVIDER_PLCC: string;
  PROVIDER_GIFT: string;
  PROVIDER_LOYALTY: string;
  PROVIDER_PAYPAL: string;
  PROVIDER_GEMALTO: string;
  PROVIDER_NAPAS: string;
  PROVIDER_MIR: string;
  PROVIDER_PAGOBANCOMAT: string;
  PROVIDER_VACCINE_PASS: string;
  PROVIDER_MADA: string;
  PROVIDER_ELO: string;

  // Samsung Error Codes
  ERROR_NONE: number;
  ERROR_SPAY_INTERNAL: number;
  ERROR_INVALID_INPUT: number;
  ERROR_NOT_SUPPORTED: number;
  ERROR_NOT_FOUND: number;
  ERROR_ALREADY_DONE: number;
  ERROR_NOT_ALLOWED: number;
  ERROR_USER_CANCELED: number;
  ERROR_PARTNER_SDK_API_LEVEL: number;
  ERROR_PARTNER_SERVICE_TYPE: number;
  ERROR_INVALID_PARAMETER: number;
  ERROR_NO_NETWORK: number;
  ERROR_SERVER_NO_RESPONSE: number;
  ERROR_PARTNER_INFO_INVALID: number;
  ERROR_INITIATION_FAIL: number;
  ERROR_REGISTRATION_FAIL: number;
  ERROR_DUPLICATED_SDK_API_CALLED: number;
  ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION: number;
  ERROR_SERVICE_ID_INVALID: number;
  ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION: number;
  ERROR_PARTNER_APP_SIGNATURE_MISMATCH: number;
  ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED: number;
  ERROR_PARTNER_APP_BLOCKED: number;
  ERROR_USER_NOT_REGISTERED_FOR_DEBUG: number;
  ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE: number;
  ERROR_PARTNER_NOT_APPROVED: number;
  ERROR_UNAUTHORIZED_REQUEST_TYPE: number;
  ERROR_EXPIRED_OR_INVALID_DEBUG_KEY: number;
  ERROR_SERVER_INTERNAL: number;
  ERROR_DEVICE_NOT_SAMSUNG: number;
  ERROR_SPAY_PKG_NOT_FOUND: number;
  ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE: number;
  ERROR_DEVICE_INTEGRITY_CHECK_FAIL: number;
  ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL: number;
  ERROR_ANDROID_PLATFORM_CHECK_FAIL: number;
  ERROR_MISSING_INFORMATION: number;
  ERROR_SPAY_SETUP_NOT_COMPLETED: number;
  ERROR_SPAY_APP_NEED_TO_UPDATE: number;
  ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED: number;
  ERROR_UNABLE_TO_VERIFY_CALLER: number;
  ERROR_SPAY_FMM_LOCK: number;
  ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY: number;
}

// Samsung Wallet - Intent Types
export enum SamsungWalletIntentType {
  LAUNCH_A2A_IDV = 'LAUNCH_A2A_IDV',
  WALLET_INTENT = 'WALLET_INTENT',
  INVALID_CALLER = 'INVALID_CALLER',
}

// Samsung Wallet - DataFormat
export enum SamsungWalletDataFormat {
  BASE64_DECODED = 'base64_decoded',
  RAW = 'raw',
}

// Samsung Wallet - Activation Status
export enum SamsungActivationStatus {
  ACCEPTED = 'accepted',
  DECLINED = 'declined',
  FAILURE = 'failure',
  APP_NOT_READY = 'appNotReady',
}

// Samsung Wallet - Evento de Intent
export interface SamsungWalletIntentEvent {
  action: string;
  type: SamsungWalletIntentType;
  data?: string; // Dados decodificados (string normal)
  dataFormat?: SamsungWalletDataFormat;
  callingPackage?: string;
  originalData?: string; // Dados originais em base64
  error?: string;
  extras?: Record<string, any>;
  // Campos específicos do Samsung (Mastercard/Visa)
  cardType?: string;
  // Campos específicos do Mastercard
  paymentAppProviderId?: string;
  paymentAppInstanceId?: string;
  tokenUniqueReference?: string;
  accountPanSuffix?: string;
  accountExpiry?: string;
  // Campos específicos do Visa
  panId?: string;
  trId?: string;
  tokenReferenceId?: string;
  last4Digits?: string;
  deviceId?: string;
  walletAccountId?: string;
}

// Samsung Pay - Interface do Módulo (baseado nos métodos do SamsungWalletModule)
export interface SamsungWalletSpec {
  // Métodos principais do Samsung Pay
  init(serviceId: string): Promise<boolean>;
  getSamsungPayStatus(): Promise<number>;
  goToUpdatePage(): void;
  activateSamsungPay(): void;
  getAllCards(): Promise<SamsungCard[]>;
  getWalletInfo(): Promise<SamsungWalletInfo>;
  addCard(
    payload: string,
    issuerId: string,
    tokenizationProvider: string,
    cardType: string
  ): Promise<SamsungCard>;

  // Métodos de compatibilidade
  checkWalletAvailability(): Promise<boolean>;

  // Constantes
  getConstants(): Promise<SamsungWalletConstants>;

  // Métodos de listener de intent
  setIntentListener(): Promise<boolean>;
  removeIntentListener(): Promise<boolean>;

  // Método de resultado de ativação
  setActivationResult(
    status: SamsungActivationStatus,
    activationCode?: string
  ): Promise<boolean>;

  // Método para finalizar atividade
  finishActivity(): Promise<boolean>;
}

// ============================================================================
// SAMSUNG WALLET EVENT EMITTER
// ============================================================================

import { NativeEventEmitter, NativeModules } from 'react-native';

export class SamsungWalletEventEmitter {
  private eventEmitter: NativeEventEmitter | null = null;
  private listeners: Map<string, (event: SamsungWalletIntentEvent) => void> =
    new Map();

  constructor() {
    try {
      // Verificar se o módulo está disponível
      const SamsungWalletModule = NativeModules.SamsungWallet;
      if (SamsungWalletModule) {
        this.eventEmitter = new NativeEventEmitter(SamsungWalletModule);
        console.log(
          '✅ [SamsungWalletEventEmitter] EventEmitter inicializado com sucesso'
        );
      } else {
        console.warn(
          '⚠️ [SamsungWalletEventEmitter] Módulo SamsungWallet não está disponível'
        );
      }
    } catch (error) {
      console.error(
        '❌ [SamsungWalletEventEmitter] Erro ao inicializar EventEmitter:',
        error
      );
    }
  }

  /**
   * Adiciona um listener para eventos de intent do Samsung Wallet
   * @param callback Função que será chamada quando um evento for recebido
   * @returns Função para remover o listener
   */
  addIntentListener(
    callback: (event: SamsungWalletIntentEvent) => void
  ): () => void {
    const listenerId = `listener_${Date.now()}_${Math.random()}`;

    // Verificar se o EventEmitter está disponível
    if (!this.eventEmitter) {
      console.error(
        '❌ [SamsungWalletEventEmitter] EventEmitter não está disponível'
      );
      return () => {}; // Retornar função vazia para evitar erros
    }

    // Armazenar o callback
    this.listeners.set(listenerId, callback);

    // Criar o listener do NativeEventEmitter
    const subscription = this.eventEmitter.addListener(
      'SamsungWalletIntentReceived',
      (event: any) => {
        const walletEvent = event as SamsungWalletIntentEvent;
        console.log(
          '🎯 [SamsungWalletEventEmitter] Intent recebido:',
          walletEvent
        );
        callback(walletEvent);
      }
    );

    console.log(
      `✅ [SamsungWalletEventEmitter] Listener adicionado: ${listenerId}`
    );

    // Retornar função de cleanup
    return () => {
      this.listeners.delete(listenerId);
      subscription.remove();
      console.log(
        `🧹 [SamsungWalletEventEmitter] Listener removido: ${listenerId}`
      );
    };
  }

  /**
   * Remove todos os listeners ativos
   */
  removeAllListeners(): void {
    this.listeners.clear();
    if (this.eventEmitter) {
      this.eventEmitter.removeAllListeners('SamsungWalletIntentReceived');
      console.log(
        '🧹 [SamsungWalletEventEmitter] Todos os listeners foram removidos'
      );
    }
  }

  /**
   * Obtém o número de listeners ativos
   */
  getListenerCount(): number {
    return this.listeners.size;
  }

  /**
   * Verifica se o EventEmitter está disponível
   */
  isAvailable(): boolean {
    return this.eventEmitter !== null;
  }
}
