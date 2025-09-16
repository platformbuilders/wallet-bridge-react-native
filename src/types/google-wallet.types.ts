// ============================================================================
// TYPES ESPECÍFICOS DO GOOGLE PAY / GOOGLE WALLET
// ============================================================================

export enum GoogleWalletStatus {
  /** Não há carteira ativa. */
  TAP_AND_PAY_NO_ACTIVE_WALLET = 'TAP_AND_PAY_NO_ACTIVE_WALLET',
  /** O ID do token do emissor indicado não corresponde a um token na carteira ativa. */
  TAP_AND_PAY_TOKEN_NOT_FOUND = 'TAP_AND_PAY_TOKEN_NOT_FOUND',
  /** O token especificado foi encontrado, mas não estava em um estado válido. */
  TAP_AND_PAY_INVALID_TOKEN_STATE = 'TAP_AND_PAY_INVALID_TOKEN_STATE',
  /** A tokenização falhou porque o dispositivo não foi aprovado em uma verificação de compatibilidade. */
  TAP_AND_PAY_ATTESTATION_ERROR = 'TAP_AND_PAY_ATTESTATION_ERROR',
  /** Não é possível chamar a API TapAndPay pelo aplicativo atual. */
  TAP_AND_PAY_UNAVAILABLE = 'TAP_AND_PAY_UNAVAILABLE',
}

export enum GoogleWalletStatusCode {
  /** Não há carteira ativa. */
  TAP_AND_PAY_NO_ACTIVE_WALLET = '15002',
  /** O ID do token do emissor indicado não corresponde a um token na carteira ativa. Este status pode ser retornado por chamadas que especificam um ID de token do emissor. */
  TAP_AND_PAY_TOKEN_NOT_FOUND = '15003',
  /** O token especificado foi encontrado, mas não estava em um estado válido para a operação ter sucesso. Por exemplo, isso pode acontecer ao tentar selecionar como padrão um token que não está no estado TOKEN_STATE_ACTIVE. */
  TAP_AND_PAY_INVALID_TOKEN_STATE = '15004',
  /** A tokenização falhou porque o dispositivo não passou em uma verificação de compatibilidade. */
  TAP_AND_PAY_ATTESTATION_ERROR = '15005',
  /** A API TapAndPay não pode ser chamada pelo aplicativo atual. Se você receber este erro, certifique-se de que está chamando a API usando um nome de pacote e impressão digital que adicionamos à nossa lista de permissões. */
  TAP_AND_PAY_UNAVAILABLE = '15009',
}

export enum CommonStatusCode {
  /** A operação foi bem-sucedida. */
  SUCCESS = '0',  //ALERTA: Quando é cancelado, ele retorna 0 também
  /** A operação foi bem-sucedida, mas usou o cache do dispositivo. */
  SUCCESS_CACHE = '-1',
  /** A versão instalada do Google Play services está desatualizada. */
  SERVICE_VERSION_UPDATE_REQUIRED = '2',
  /** A versão instalada do Google Play services foi desabilitada neste dispositivo. */
  SERVICE_DISABLED = '3',
  /** O cliente tentou conectar ao serviço, mas o usuário não está logado. */
  SIGN_IN_REQUIRED = '4',
  /** O cliente tentou conectar ao serviço com um nome de conta inválido especificado. */
  INVALID_ACCOUNT = '5',
  /** Completar a operação requer alguma forma de resolução. */
  RESOLUTION_REQUIRED = '6',
  /** Ocorreu um erro de rede. Tentar novamente deve resolver o problema. */
  NETWORK_ERROR = '7',
  /** Ocorreu um erro interno. Tentar novamente deve resolver o problema. */
  INTERNAL_ERROR = '8',
  /** O aplicativo está mal configurado. Este erro não é recuperável. */
  DEVELOPER_ERROR = '10',
  /** A operação falhou sem informações mais detalhadas. */
  ERROR = '13',
  /** Uma chamada bloqueante foi interrompida enquanto aguardava e não foi executada até a conclusão. */
  INTERRUPTED = '14',
  /** Tempo limite enquanto aguardava o resultado. */
  TIMEOUT = '15',
  /** O resultado foi cancelado devido à desconexão do cliente ou cancelamento. */
  CANCELED = '16',
  /** O cliente tentou chamar um método de uma API que falhou ao conectar. */
  API_NOT_CONNECTED = '17',
  /** Houve uma RemoteException não-DeadObjectException ao chamar um serviço conectado. */
  REMOTE_EXCEPTION = '19',
  /** A conexão foi suspensa enquanto a chamada estava em andamento. */
  CONNECTION_SUSPENDED_DURING_CALL = '20',
  /** A conexão expirou enquanto aguardava o Google Play services atualizar. */
  RECONNECTION_TIMED_OUT_DURING_UPDATE = '21',
  /** A conexão expirou ao tentar reconectar. */
  RECONNECTION_TIMED_OUT = '22',
}

export enum GoogleTokenState {
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION = 'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION',
  TOKEN_STATE_PENDING = 'TOKEN_STATE_PENDING',
  TOKEN_STATE_SUSPENDED = 'TOKEN_STATE_SUSPENDED',
  TOKEN_STATE_ACTIVE = 'TOKEN_STATE_ACTIVE',
  TOKEN_STATE_FELICA_PENDING_PROVISIONING = 'TOKEN_STATE_FELICA_PENDING_PROVISIONING',
  TOKEN_STATE_UNTOKENIZED = 'TOKEN_STATE_UNTOKENIZED',
}

export enum GoogleTokenProvider {
  TOKEN_PROVIDER_AMEX = 'TOKEN_PROVIDER_AMEX',
  TOKEN_PROVIDER_DISCOVER = 'TOKEN_PROVIDER_DISCOVER',
  TOKEN_PROVIDER_JCB = 'TOKEN_PROVIDER_JCB',
  TOKEN_PROVIDER_MASTERCARD = 'TOKEN_PROVIDER_MASTERCARD',
  TOKEN_PROVIDER_VISA = 'TOKEN_PROVIDER_VISA',
  TOKEN_PROVIDER_ELO = 'TOKEN_PROVIDER_ELO',
}

export enum GoogleCardNetwork {
  CARD_NETWORK_AMEX = 'CARD_NETWORK_AMEX',
  CARD_NETWORK_DISCOVER = 'CARD_NETWORK_DISCOVER',
  CARD_NETWORK_MASTERCARD = 'CARD_NETWORK_MASTERCARD',
  CARD_NETWORK_QUICPAY = 'CARD_NETWORK_QUICPAY',
  CARD_NETWORK_PRIVATE_LABEL = 'CARD_NETWORK_PRIVATE_LABEL',
  CARD_NETWORK_VISA = 'CARD_NETWORK_VISA',
  CARD_NETWORK_ELO = 'CARD_NETWORK_ELO',
}

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
export interface GoogleTokenInfoSimple {
  issuerTokenId: string;
  lastDigits: string;
  displayName: string;
  tokenState: number;
  network: number;
}

// Google Wallet - WalletData
export interface GoogleWalletData {
  deviceID: string;
  walletAccountID: string;
}

// Google Wallet - Constants
export interface GoogleWalletConstants {
  SDK_AVAILABLE: boolean;
  SDK_NAME: string;
  CARD_NETWORK_ELO: number;
  TOKEN_PROVIDER_ELO: number;
  TOKEN_STATE_ACTIVE: number;
  TOKEN_STATE_PENDING: number;
  TOKEN_STATE_SUSPENDED: number;
  TOKEN_STATE_UNTOKENIZED: number;
}

// Google Wallet - TokenStatus
export interface GoogleTokenStatus {
  tokenState: number;
  isSelected: boolean;
}

// Google Wallet - Evento de Intent
export interface GoogleWalletIntentEvent {
  action: string;
  type: GoogleWalletIntentType;
  data?: string;
  dataFormat?: 'base64';
  dataNote?: string;
  callingPackage?: string;
  error?: string;
  extras?: Record<string, any>;
}

// Google Wallet - Interface do Módulo
export interface GoogleWalletSpec {
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<GoogleWalletData>;
  getTokenStatus(tokenServiceProvider: number, tokenReferenceId: string): Promise<GoogleTokenStatus>;
  getEnvironment(): Promise<string>;
  isTokenized(fpanLastFour: string, cardNetwork: number, tokenServiceProvider: number): Promise<boolean>;
  viewToken(tokenServiceProvider: number, issuerTokenId: string): Promise<boolean>;
  addCardToWallet(cardData: GooglePushTokenizeRequest): Promise<string>;
  createWalletIfNeeded(): Promise<boolean>;
  listTokens(): Promise<GoogleTokenInfoSimple[]>;
  getConstants(): GoogleWalletConstants;
  
  // Métodos de listener de intent
  setIntentListener(): Promise<boolean>;
  removeIntentListener(): Promise<boolean>;
}

// Google Wallet - Interface de Compatibilidade (para código existente)
export interface GoogleWalletCompatibilitySpec {
  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<GoogleWalletData>;
  getTokenStatus(tokenServiceProvider: number, tokenReferenceId: string): Promise<GoogleTokenStatus>;
  getEnvironment(): Promise<string>;
  isTokenized(fpanLastFour: string, cardNetwork: number, tokenServiceProvider: number): Promise<boolean>;
  viewToken(tokenServiceProvider: number, issuerTokenId: string): Promise<boolean>;
  addCardToWallet(cardData: any): Promise<string>; // Aceita qualquer tipo para compatibilidade
  createWalletIfNeeded(): Promise<boolean>;
  listTokens(): Promise<GoogleTokenInfoSimple[]>;
  getConstants(): any; // Aceita qualquer tipo para compatibilidade
  
  // Métodos de listener de intent
  setIntentListener(): Promise<boolean>;
  removeIntentListener(): Promise<boolean>;
}
