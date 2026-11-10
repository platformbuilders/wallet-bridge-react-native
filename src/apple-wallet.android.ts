// ============================================================================
// APPLE WALLET ANDROID STUB
// ============================================================================
// O Apple Wallet só existe no iOS. Fora dele, o módulo responde com valores
// neutros (nada disponível, nenhum log), como os stubs de Google e Samsung
// fazem no iOS.

import type {
  AppleWalletConstants,
  AppleWalletSpec,
  AppleWalletSubscription,
} from './types/apple-wallet.types';

const STUB_CONSTANTS: AppleWalletConstants = {
  PROVISIONING_ENTITLEMENT: '',
  APPLE_LEAF_CERT_OID: '',
  WALLET_URL_SCHEME: '',
  EVENT_LOG: '',
};

const NO_SUBSCRIPTION: AppleWalletSubscription = { remove: () => {} };

export const AppleWalletAndroid: AppleWalletSpec = {
  getConstants: () => STUB_CONSTANTS,
  getEnvironment: async () => 'android',
  openWallet: async () => false,
  setLogListener: async () => false,
  removeLogListener: async () => false,
  onLog: () => NO_SUBSCRIPTION,
};
