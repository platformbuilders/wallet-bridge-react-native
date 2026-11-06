import {
  type CodegenTypes,
  type TurboModule,
  TurboModuleRegistry,
} from 'react-native';

// Alias local em vez de `CodegenTypes.EventEmitter` direto na Spec: o codegen
// do RN 0.76–0.79 só reconhece o identificador `EventEmitter` (sem namespace),
// e a lib suporta react-native >=0.76.
type EventEmitter<T> = CodegenTypes.EventEmitter<T>;

export type AppleWalletLogEvent = {
  level: string;
  tag: string;
  message: string;
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

  setLogListener(): Promise<boolean>;
  removeLogListener(): Promise<boolean>;

  readonly onLog: EventEmitter<AppleWalletLogEvent>;
}

// `get` (e não `getEnforcing`): o módulo só existe no iOS.
export default TurboModuleRegistry.get<Spec>('AppleWallet');
