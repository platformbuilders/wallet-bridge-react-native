import {
  type CodegenTypes,
  type TurboModule,
  TurboModuleRegistry,
} from 'react-native';

// `CodegenTypes` só é exportado por `react-native` a partir da 0.80 (piso da
// lib). Alias local para o codegen ver o identificador `EventEmitter` sem
// namespace. Inteiros vão como `number` (double no nativo, sem `Int32`): o
// módulo Kotlin converte com toInt().
type EventEmitter<T> = CodegenTypes.EventEmitter<T>;

export type SamsungWalletIntentEvent = {
  action: string;
  type: string;
  data?: string;
  dataFormat?: string;
  callingPackage?: string;
  originalData?: string;
  error?: string;
};

export type SamsungWalletLogEvent = {
  level: string;
  tag: string;
  message: string;
  error?: string;
  stackTrace?: string;
};

// Todas opcionais: implementação real, stub e mock expõem conjuntos diferentes
// (ex.: SDK_ENABLED só no stub; pacotes e URLs só no real e no mock).
export type SamsungWalletConstants = {
  SDK_NAME?: string;
  SAMSUNG_PAY_PACKAGE?: string;
  SAMSUNG_PAY_PLAY_STORE_URL?: string;
  SPAY_READY?: number;
  SPAY_NOT_READY?: number;
  SPAY_NOT_SUPPORTED?: number;
  SPAY_NOT_ALLOWED_TEMPORALLY?: number;
  SPAY_HAS_TRANSIT_CARD?: number;
  SPAY_HAS_NO_TRANSIT_CARD?: number;
  CARD_TYPE?: string;
  CARD_TYPE_CREDIT_DEBIT?: string;
  CARD_TYPE_GIFT?: string;
  CARD_TYPE_LOYALTY?: string;
  CARD_TYPE_CREDIT?: string;
  CARD_TYPE_DEBIT?: string;
  CARD_TYPE_TRANSIT?: string;
  CARD_TYPE_VACCINE_PASS?: string;
  ACTIVE?: string;
  DISPOSED?: string;
  EXPIRED?: string;
  PENDING_ENROLLED?: string;
  PENDING_PROVISION?: string;
  SUSPENDED?: string;
  PENDING_ACTIVATION?: string;
  PROVIDER_VISA?: string;
  PROVIDER_MASTERCARD?: string;
  PROVIDER_AMEX?: string;
  PROVIDER_DISCOVER?: string;
  PROVIDER_PLCC?: string;
  PROVIDER_GIFT?: string;
  PROVIDER_LOYALTY?: string;
  PROVIDER_PAYPAL?: string;
  PROVIDER_GEMALTO?: string;
  PROVIDER_NAPAS?: string;
  PROVIDER_MIR?: string;
  PROVIDER_PAGOBANCOMAT?: string;
  PROVIDER_VACCINE_PASS?: string;
  PROVIDER_MADA?: string;
  PROVIDER_ELO?: string;
  ERROR_NONE?: number;
  ERROR_SPAY_INTERNAL?: number;
  ERROR_INVALID_INPUT?: number;
  ERROR_NOT_SUPPORTED?: number;
  ERROR_NOT_FOUND?: number;
  ERROR_ALREADY_DONE?: number;
  ERROR_NOT_ALLOWED?: number;
  ERROR_USER_CANCELED?: number;
  ERROR_PARTNER_SDK_API_LEVEL?: number;
  ERROR_PARTNER_SERVICE_TYPE?: number;
  ERROR_INVALID_PARAMETER?: number;
  ERROR_NO_NETWORK?: number;
  ERROR_SERVER_NO_RESPONSE?: number;
  ERROR_PARTNER_INFO_INVALID?: number;
  ERROR_INITIATION_FAIL?: number;
  ERROR_REGISTRATION_FAIL?: number;
  ERROR_DUPLICATED_SDK_API_CALLED?: number;
  ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION?: number;
  ERROR_SERVICE_ID_INVALID?: number;
  ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION?: number;
  ERROR_PARTNER_APP_SIGNATURE_MISMATCH?: number;
  ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED?: number;
  ERROR_PARTNER_APP_BLOCKED?: number;
  ERROR_USER_NOT_REGISTERED_FOR_DEBUG?: number;
  ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE?: number;
  ERROR_PARTNER_NOT_APPROVED?: number;
  ERROR_UNAUTHORIZED_REQUEST_TYPE?: number;
  ERROR_EXPIRED_OR_INVALID_DEBUG_KEY?: number;
  ERROR_SERVER_INTERNAL?: number;
  ERROR_DEVICE_NOT_SAMSUNG?: number;
  ERROR_SPAY_PKG_NOT_FOUND?: number;
  ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE?: number;
  ERROR_DEVICE_INTEGRITY_CHECK_FAIL?: number;
  ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL?: number;
  ERROR_ANDROID_PLATFORM_CHECK_FAIL?: number;
  ERROR_MISSING_INFORMATION?: number;
  ERROR_SPAY_SETUP_NOT_COMPLETED?: number;
  ERROR_SPAY_APP_NEED_TO_UPDATE?: number;
  ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED?: number;
  ERROR_UNABLE_TO_VERIFY_CALLER?: number;
  ERROR_SPAY_FMM_LOCK?: number;
  ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY?: number;
  useMock?: boolean;
  SDK_ENABLED?: boolean;
};

export interface Spec extends TurboModule {
  getConstants(): SamsungWalletConstants;

  init(serviceId: string): Promise<boolean>;
  getSamsungPayStatus(): Promise<number>;
  goToUpdatePage(): void;
  activateSamsungPay(): void;
  getAllCards(): Promise<Array<Object>>;
  getWalletInfo(): Promise<Object>;
  addCard(
    payload: string,
    issuerId: string,
    tokenizationProvider: string,
    cardType: string,
  ): Promise<Object>;
  checkWalletAvailability(): Promise<boolean>;

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

  readonly onIntentReceived: EventEmitter<SamsungWalletIntentEvent>;
  readonly onNoIntentReceived: EventEmitter<void>;
  readonly onValidCallerNoIntentReceived: EventEmitter<void>;
  readonly onLog: EventEmitter<SamsungWalletLogEvent>;
}

// `get` (e não `getEnforcing`): o módulo só existe no Android.
export default TurboModuleRegistry.get<Spec>('SamsungWallet');
