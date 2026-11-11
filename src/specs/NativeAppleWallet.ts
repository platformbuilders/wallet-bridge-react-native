import {
  type CodegenTypes,
  type TurboModule,
  TurboModuleRegistry,
} from 'react-native';

// `CodegenTypes` só é exportado por `react-native` a partir da 0.80 (piso da
// lib). Alias local para o codegen ver o identificador `EventEmitter` sem
// namespace.
type EventEmitter<T> = CodegenTypes.EventEmitter<T>;

// O codegen não aceita enum TS: estados e códigos trafegam como `string`, e
// objetos aninhados como `Object`. O contrato público, com os enums, é o
// AppleWalletSpec (src/types/apple-wallet.types.ts).

export type AppleWalletLogEvent = {
  level: string;
  tag: string;
  message: string;
};

export type AppleProvisioningCertificatesEvent = {
  requestId: string;
  certificates: ReadonlyArray<string>;
  nonce: string;
  nonceSignature: string;
};

export type AppleProvisioningResultEvent = {
  requestId: string;
  status: string;
  pass?: Object;
  errorCode?: string;
  errorMessage?: string;
};

export type AppleWalletConstants = {
  PROVISIONING_ENTITLEMENT: string;
  APPLE_LEAF_CERT_OID: string;
  WALLET_URL_SCHEME: string;
  EVENT_LOG: string;
};

export interface Spec extends TurboModule {
  getConstants(): AppleWalletConstants;

  getEnvironment(): Promise<string>;
  openWallet(): Promise<boolean>;

  // Provisioning
  canAddPaymentPass(): Promise<boolean>;
  canAddSecureElementPass(primaryAccountIdentifier: string): Promise<boolean>;
  startInAppProvisioning(config: Object): Promise<string>;
  submitProvisioningData(payload: Object): Promise<boolean>;
  cancelProvisioning(): Promise<boolean>;

  // Passes
  listPasses(): Promise<Array<Object>>;
  listRemotePasses(): Promise<Array<Object>>;
  isWatchPaired(): Promise<boolean>;

  // Verification
  activatePass(
    deviceAccountIdentifier: string,
    activationData: string,
  ): Promise<boolean>;

  // Logs
  setLogListener(): Promise<boolean>;
  removeLogListener(): Promise<boolean>;

  readonly onProvisioningCertificates: EventEmitter<AppleProvisioningCertificatesEvent>;
  readonly onProvisioningResult: EventEmitter<AppleProvisioningResultEvent>;
  readonly onLog: EventEmitter<AppleWalletLogEvent>;
}

// `get` (e não `getEnforcing`): o módulo só existe no iOS.
export default TurboModuleRegistry.get<Spec>('AppleWallet');
