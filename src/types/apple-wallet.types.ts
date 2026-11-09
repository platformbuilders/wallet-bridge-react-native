// ============================================================================
// APPLE WALLET TYPES
// ============================================================================
// Contrato público do módulo Apple Wallet (iOS). O TurboModule implementa esta
// spec (src/specs/NativeAppleWallet.ts) e o stub de Android também.

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

export interface AppleWalletSubscription {
  remove(): void;
}

export interface AppleWalletSpec {
  getConstants(): AppleWalletConstants;

  /** `ios-device` ou `ios-simulator`; `android` no stub. */
  getEnvironment(): Promise<string>;

  /** Abre o app Wallet. `false` se não for possível neste device. */
  openWallet(): Promise<boolean>;

  setLogListener(): Promise<boolean>;
  removeLogListener(): Promise<boolean>;

  /** Assina os logs de diagnóstico (só emitidos em build Debug). */
  onLog(
    listener: (event: AppleWalletLogEvent) => void,
  ): AppleWalletSubscription;
}
