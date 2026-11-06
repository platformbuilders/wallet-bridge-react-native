import {
  type CodegenTypes,
  type TurboModule,
  TurboModuleRegistry,
} from 'react-native';

// Aliases locais em vez de `CodegenTypes.*` direto na Spec: o codegen do
// RN 0.76–0.79 só reconhece o identificador `EventEmitter` (sem namespace), e a
// lib suporta react-native >=0.76. Inteiros vão como `number` (double no
// nativo) pelo mesmo motivo: o módulo Kotlin converte com toInt().
type EventEmitter<T> = CodegenTypes.EventEmitter<T>;

export type GoogleWalletIntentEvent = {
  action: string;
  type: string;
  data?: string;
  dataFormat?: string;
  callingPackage?: string;
  originalData?: string;
  error?: string;
};

export type GoogleWalletLogEvent = {
  level: string;
  tag: string;
  message: string;
  error?: string;
  stackTrace?: string;
};

// Todas opcionais: implementação real, stub e mock expõem conjuntos diferentes
// (ex.: SDK_ENABLED só no stub; pacotes e URLs só no real e no mock).
export type GoogleWalletConstants = {
  SDK_NAME?: string;
  GOOGLE_WALLET_PACKAGE?: string;
  GOOGLE_WALLET_APP_PACKAGE?: string;
  GOOGLE_WALLET_PLAY_STORE_URL?: string;
  TOKEN_PROVIDER_AMEX?: number;
  TOKEN_PROVIDER_DISCOVER?: number;
  TOKEN_PROVIDER_JCB?: number;
  TOKEN_PROVIDER_MASTERCARD?: number;
  TOKEN_PROVIDER_VISA?: number;
  TOKEN_PROVIDER_ELO?: number;
  CARD_NETWORK_AMEX?: number;
  CARD_NETWORK_DISCOVER?: number;
  CARD_NETWORK_MASTERCARD?: number;
  CARD_NETWORK_QUICPAY?: number;
  CARD_NETWORK_PRIVATE_LABEL?: number;
  CARD_NETWORK_VISA?: number;
  CARD_NETWORK_ELO?: number;
  TAP_AND_PAY_NO_ACTIVE_WALLET?: number;
  TAP_AND_PAY_TOKEN_NOT_FOUND?: number;
  TAP_AND_PAY_INVALID_TOKEN_STATE?: number;
  TAP_AND_PAY_ATTESTATION_ERROR?: number;
  TAP_AND_PAY_UNAVAILABLE?: number;
  TAP_AND_PAY_SAVE_CARD_ERROR?: number;
  TAP_AND_PAY_INELIGIBLE_FOR_TOKENIZATION?: number;
  TAP_AND_PAY_TOKENIZATION_DECLINED?: number;
  TAP_AND_PAY_CHECK_ELIGIBILITY_ERROR?: number;
  TAP_AND_PAY_TOKENIZE_ERROR?: number;
  TAP_AND_PAY_TOKEN_ACTIVATION_REQUIRED?: number;
  TAP_AND_PAY_PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT?: number;
  TAP_AND_PAY_USER_CANCELED_FLOW?: number;
  TAP_AND_PAY_ENROLL_FOR_VIRTUAL_CARDS_FAILED?: number;
  TOKEN_STATE_ACTIVE?: number;
  TOKEN_STATE_PENDING?: number;
  TOKEN_STATE_SUSPENDED?: number;
  TOKEN_STATE_UNTOKENIZED?: number;
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION?: number;
  TOKEN_STATE_FELICA_PENDING_PROVISIONING?: number;
  SUCCESS?: number;
  SUCCESS_CACHE?: number;
  SERVICE_VERSION_UPDATE_REQUIRED?: number;
  SERVICE_DISABLED?: number;
  SIGN_IN_REQUIRED?: number;
  INVALID_ACCOUNT?: number;
  RESOLUTION_REQUIRED?: number;
  NETWORK_ERROR?: number;
  INTERNAL_ERROR?: number;
  DEVELOPER_ERROR?: number;
  ERROR?: number;
  INTERRUPTED?: number;
  TIMEOUT?: number;
  CANCELED?: number;
  API_NOT_CONNECTED?: number;
  REMOTE_EXCEPTION?: number;
  CONNECTION_SUSPENDED_DURING_CALL?: number;
  RECONNECTION_TIMED_OUT_DURING_UPDATE?: number;
  RECONNECTION_TIMED_OUT?: number;
  useMock?: boolean;
  SDK_ENABLED?: boolean;
};

export interface Spec extends TurboModule {
  getConstants(): GoogleWalletConstants;

  checkWalletAvailability(): Promise<boolean>;
  getSecureWalletInfo(): Promise<Object>;
  getTokenStatus(
    tokenServiceProvider: number,
    tokenReferenceId: string,
  ): Promise<Object>;
  getEnvironment(): Promise<string>;
  isTokenized(
    fpanLastFour: string,
    cardNetwork: number,
    tokenServiceProvider: number,
  ): Promise<boolean>;
  viewToken(
    tokenServiceProvider: number,
    issuerTokenId: string,
  ): Promise<Object | null>;
  addCardToWallet(cardData: Object): Promise<string | null>;
  createWalletIfNeeded(): Promise<boolean>;
  listTokens(): Promise<Array<Object>>;

  setIntentListener(): Promise<boolean>;
  removeIntentListener(): Promise<boolean>;
  setActivationResult(
    status: string,
    activationCode?: string,
  ): Promise<boolean>;
  finishActivity(): Promise<boolean>;
  openWallet(): Promise<boolean>;

  setLogListener(): Promise<boolean>;
  removeLogListener(): Promise<boolean>;

  readonly onIntentReceived: EventEmitter<GoogleWalletIntentEvent>;
  readonly onNoIntentReceived: EventEmitter<void>;
  readonly onValidCallerNoIntentReceived: EventEmitter<void>;
  readonly onLog: EventEmitter<GoogleWalletLogEvent>;
}

// `get` (e não `getEnforcing`): o módulo só existe no Android.
export default TurboModuleRegistry.get<Spec>('GoogleWallet');
