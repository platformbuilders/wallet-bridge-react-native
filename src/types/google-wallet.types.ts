// ============================================================================
// TYPES ESPECÍFICOS DO GOOGLE PAY / GOOGLE WALLET
// ============================================================================

export enum GoogleEnvironment {
  PROD = 'PROD',
  SANDBOX = 'SANDBOX',
  DEV = 'DEV',
}

export enum GoogleWalletIntentType {
  ACTIVATE_TOKEN = 'ACTIVATE_TOKEN',
  WALLET_INTENT = 'WALLET_INTENT',
  INVALID_CALLER = 'INVALID_CALLER',
}

// Google Wallet - UserAddress (baseado no SDK do Google Pay)
export interface GoogleUserAddress {
  name: string;
  address1: string;
  address2?: string;
  locality: string; // city
  administrativeArea: string; // state/province
  countryCode: string;
  postalCode: string;
  phoneNumber: string;
}

// Google Wallet - PaymentCard (baseado no SDK do Google Pay)
export interface GooglePaymentCard {
  opaquePaymentCard: string;
  network: number; // GoogleCardNetwork value
  tokenServiceProvider: number; // GoogleTokenProvider value
  displayName: string;
  lastDigits: string;
}

// Google Wallet - PushTokenizeRequest (baseado no SDK do Google Pay)
export interface GooglePushTokenizeRequest {
  address: GoogleUserAddress;
  card: GooglePaymentCard;
}

// Google Wallet - UserAddress para addCardToWallet (compatível com SDK)
export interface GoogleUserAddressForCard {
  address1: string;
  address2?: string;
  countryCode: string;
  locality: string; // city
  administrativeArea: string; // state/province
  name: string;
  phoneNumber: string;
  postalCode: string;
}

// Google Wallet - PaymentCard para addCardToWallet (compatível com SDK)
export interface GooglePaymentCardForCard {
  opaquePaymentCard: string;
  network: number; // GoogleCardNetwork value
  tokenServiceProvider: number; // GoogleTokenProvider value
  displayName: string;
  lastDigits: string;
}

// Google Wallet - PushTokenizeRequest para addCardToWallet
export interface GooglePushTokenizeRequestForCard {
  address: GoogleUserAddressForCard;
  card: GooglePaymentCardForCard;
}

// Google Wallet - TokenInfo (baseado no SDK do Google Pay)
export interface GoogleTokenInfo {
  issuerTokenId: string;
  issuerName: string;
  fpanLastFour: string;
  dpanLastFour: string;
  tokenServiceProvider: number;
  network: number;
  tokenState: number;
  isDefaultToken: boolean;
  portfolioName: string;
}

// Google Wallet - TokenInfo simplificado para listTokens
// Removido: GoogleTokenInfoSimple (campos legados não são mais usados)

// Google Wallet - WalletData
export interface GoogleWalletData {
  deviceID: string;
  walletAccountID: string;
}

// Google Wallet - Constants

export interface GoogleWalletConstants {
  SDK_NAME: string;
  GOOGLE_WALLET_PACKAGE: string;
  GOOGLE_WALLET_APP_PACKAGE: string;
  GOOGLE_WALLET_PLAY_STORE_URL: string;

  // Google Token Provider
  TOKEN_PROVIDER_AMEX: number;
  TOKEN_PROVIDER_DISCOVER: number;
  TOKEN_PROVIDER_JCB: number;
  TOKEN_PROVIDER_MASTERCARD: number;
  TOKEN_PROVIDER_VISA: number;
  TOKEN_PROVIDER_ELO: number;

  // Google Card Network
  CARD_NETWORK_AMEX: number;
  CARD_NETWORK_DISCOVER: number;
  CARD_NETWORK_MASTERCARD: number;
  CARD_NETWORK_QUICPAY: number;
  CARD_NETWORK_PRIVATE_LABEL: number;
  CARD_NETWORK_VISA: number;
  CARD_NETWORK_ELO: number;

  // TapAndPay Status Codes
  /** Não há carteira ativa. */
  TAP_AND_PAY_NO_ACTIVE_WALLET: number;
  /** O ID do token do emissor indicado não corresponde a um token na carteira ativa. Este status pode ser retornado por chamadas que especificam um ID de token do emissor. */
  TAP_AND_PAY_TOKEN_NOT_FOUND: number;
  /** O token especificado foi encontrado, mas não estava em um estado válido para a operação ter sucesso. Por exemplo, isso pode acontecer ao tentar selecionar como padrão um token que não está no estado TOKEN_STATE_ACTIVE. */
  TAP_AND_PAY_INVALID_TOKEN_STATE: number;
  /** A tokenização falhou porque o dispositivo não passou em uma verificação de compatibilidade. */
  TAP_AND_PAY_ATTESTATION_ERROR: number;
  /** A API TapAndPay não pode ser chamada pelo aplicativo atual. Se você receber este erro, certifique-se de que está chamando a API usando um nome de pacote e impressão digital que adicionamos à nossa lista de permissões. */
  TAP_AND_PAY_UNAVAILABLE: number;

  //GOGLE TOKEN STATE
  /** O token está ativo e disponível para pagamentos. */
  TOKEN_STATE_ACTIVE: number;
  /** O token não está disponível para pagamentos no momento, mas vai estar depois de algum tempo. */
  TOKEN_STATE_PENDING: number;
  /** O token foi temporariamente suspenso. */
  TOKEN_STATE_SUSPENDED: number;
  /** Esse estado é visível no SDK, mas não é possível para provisionamento por push. É possível ignorar esse estado com segurança. */
  TOKEN_STATE_UNTOKENIZED: number;
  /** O token está na carteira ativa, mas requer autenticação extra do usuário para ser usado (acompanhamento do caminho amarelo). */
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION: number;
  /** O token foi emitido pelo TSP, mas o provisionamento do Felica não foi concluído. */
  TOKEN_STATE_FELICA_PENDING_PROVISIONING: number;

  // GOOGLE COMMON STATUS CODES
  /** A operação foi bem-sucedida. */
  SUCCESS: number;
  /** A operação foi bem-sucedida, mas usou o cache do dispositivo. */
  SUCCESS_CACHE: number;
  /** A versão instalada do Google Play services está desatualizada. */
  SERVICE_VERSION_UPDATE_REQUIRED: number;
  /** A versão instalada do Google Play services foi desabilitada neste dispositivo. */
  SERVICE_DISABLED: number;
  /** O cliente tentou conectar ao serviço, mas o usuário não está logado. */
  SIGN_IN_REQUIRED: number;
  /** O cliente tentou conectar ao serviço com um nome de conta inválido especificado. */
  INVALID_ACCOUNT: number;
  /** Completar a operação requer alguma forma de resolução. */
  RESOLUTION_REQUIRED: number;
  /** Ocorreu um erro de rede. Tentar novamente deve resolver o problema. */
  NETWORK_ERROR: number;
  /** Ocorreu um erro interno. Tentar novamente deve resolver o problema. */
  INTERNAL_ERROR: number;
  /** O aplicativo está mal configurado. Este erro não é recuperável. */
  DEVELOPER_ERROR: number;
  /** A operação falhou sem informações mais detalhadas. */
  ERROR: number;
  /** Uma chamada bloqueante foi interrompida enquanto aguardava e não foi executada até a conclusão. */
  INTERRUPTED: number;
  /** Tempo limite enquanto aguardava o resultado. */
  TIMEOUT: number;
  /** O resultado foi cancelado devido à desconexão do cliente ou cancelamento. */
  CANCELED: number;
  /** O cliente tentou chamar um método de uma API que falhou ao conectar. */
  API_NOT_CONNECTED: number;
  /** Houve uma RemoteException não-DeadObjectException ao chamar um serviço conectado. */
  REMOTE_EXCEPTION: number;
  /** A conexão foi suspensa enquanto a chamada estava em andamento. */
  CONNECTION_SUSPENDED_DURING_CALL: number;
  /** A conexão expirou enquanto aguardava o Google Play services atualizar. */
  RECONNECTION_TIMED_OUT_DURING_UPDATE: number;
  /** A conexão expirou ao tentar reconectar. */
  RECONNECTION_TIMED_OUT: number;
}

// Google Wallet - TokenStatus
export interface GoogleTokenStatus {
  tokenState: number;
  isSelected: boolean;
}

// Google Wallet - DataFormat
export enum GoogleWalletDataFormat {
  BASE64_DECODED = 'base64_decoded',
  RAW = 'raw',
}

// Google Wallet - Evento de Intent
export interface GoogleWalletIntentEvent {
  action: string;
  type: GoogleWalletIntentType;
  data?: string; // Dados decodificados (string normal)
  dataFormat?: GoogleWalletDataFormat;
  callingPackage?: string;
  originalData?: string; // Dados originais em base64
  error?: string;
  extras?: Record<string, any>;
}

// Google Wallet - Status de Ativação
export enum GoogleActivationStatus {
  APPROVED = 'approved',
  DECLINED = 'declined',
  FAILURE = 'failure',
}

// Google Wallet - Interface do Módulo
export interface GoogleWalletSpec {
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<GoogleWalletData>;
  getTokenStatus(
    tokenServiceProvider: number,
    tokenReferenceId: string
  ): Promise<GoogleTokenStatus>;
  getEnvironment(): Promise<string>;
  isTokenized(
    fpanLastFour: string,
    cardNetwork: number,
    tokenServiceProvider: number
  ): Promise<boolean>;
  viewToken(
    tokenServiceProvider: number,
    issuerTokenId: string
  ): Promise<GoogleTokenInfo | null>;
  addCardToWallet(cardData: GooglePushTokenizeRequestForCard): Promise<string>;
  createWalletIfNeeded(): Promise<boolean>;
  listTokens(): Promise<GoogleTokenInfo[]>;
  getConstants(): GoogleWalletConstants;

  // Métodos de listener de intent
  setIntentListener(): Promise<boolean>;
  removeIntentListener(): Promise<boolean>;

  // Método de resultado de ativação
  setActivationResult(
    status: GoogleActivationStatus,
    activationCode?: string
  ): Promise<boolean>;

  // Método para finalizar atividade
  finishActivity(): Promise<boolean>;

  // Método para abrir wallet
  openWallet(): Promise<boolean>;
}

// ============================================================================
// GOOGLE WALLET EVENT EMITTER
// ============================================================================

import { NativeEventEmitter, NativeModules } from 'react-native';

export class GoogleWalletEventEmitter {
  private eventEmitter: NativeEventEmitter | null = null;
  private listeners: Map<string, (event: GoogleWalletIntentEvent) => void> =
    new Map();

  constructor() {
    try {
      // Verificar se o módulo está disponível
      const GoogleWalletModule = NativeModules.GoogleWallet;
      if (GoogleWalletModule) {
        this.eventEmitter = new NativeEventEmitter(GoogleWalletModule);
        console.log(
          '✅ [GoogleWalletEventEmitter] EventEmitter inicializado com sucesso'
        );
      } else {
        console.warn(
          '⚠️ [GoogleWalletEventEmitter] Módulo GoogleWallet não está disponível'
        );
      }
    } catch (error) {
      console.error(
        '❌ [GoogleWalletEventEmitter] Erro ao inicializar EventEmitter:',
        error
      );
    }
  }

  /**
   * Adiciona um listener para eventos de intent do Google Wallet
   * @param callback Função que será chamada quando um evento for recebido
   * @returns Função para remover o listener
   */
  addIntentListener(
    callback: (event: GoogleWalletIntentEvent) => void
  ): () => void {
    const listenerId = `listener_${Date.now()}_${Math.random()}`;

    // Verificar se o EventEmitter está disponível
    if (!this.eventEmitter) {
      console.error(
        '❌ [GoogleWalletEventEmitter] EventEmitter não está disponível'
      );
      return () => {}; // Retornar função vazia para evitar erros
    }

    // Armazenar o callback
    this.listeners.set(listenerId, callback);

    // Criar o listener do NativeEventEmitter
    const subscription = this.eventEmitter.addListener(
      'GoogleWalletIntentReceived',
      (event: any) => {
        const walletEvent = event as GoogleWalletIntentEvent;
        console.log(
          '🎯 [GoogleWalletEventEmitter] Intent recebido:',
          walletEvent
        );
        callback(walletEvent);
      }
    );

    console.log(
      `✅ [GoogleWalletEventEmitter] Listener adicionado: ${listenerId}`
    );

    // Retornar função de cleanup
    return () => {
      this.listeners.delete(listenerId);
      subscription.remove();
      console.log(
        `🧹 [GoogleWalletEventEmitter] Listener removido: ${listenerId}`
      );
    };
  }

  /**
   * Remove todos os listeners ativos
   */
  removeAllListeners(): void {
    this.listeners.clear();
    if (this.eventEmitter) {
      this.eventEmitter.removeAllListeners('GoogleWalletIntentReceived');
      console.log(
        '🧹 [GoogleWalletEventEmitter] Todos os listeners foram removidos'
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
