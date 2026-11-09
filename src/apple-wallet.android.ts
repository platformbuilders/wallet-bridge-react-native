// ============================================================================
// APPLE WALLET ANDROID STUB
// ============================================================================
// O Apple Wallet só existe no iOS. Fora dele, consultas respondem com valores
// neutros (nada disponível, nenhum pass, nenhum evento) e as ações rejeitam
// com NOT_SUPPORTED_ON_PLATFORM, como os stubs de Google e Samsung no iOS.

import {
  AppleCanAddCardReason,
  AppleWalletErrorCode,
} from './enums/apple-wallet.enum';
import type {
  AppleWalletConstants,
  AppleWalletError,
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

const notSupported = async (method: string): Promise<never> => {
  const error = new Error(
    `AppleWalletModule.${method} is only available on iOS.`,
  ) as AppleWalletError;
  error.code = AppleWalletErrorCode.NOT_SUPPORTED_ON_PLATFORM;
  throw error;
};

export const AppleWalletAndroid: AppleWalletSpec = {
  getConstants: () => STUB_CONSTANTS,
  getEnvironment: async () => 'android',
  openWallet: async () => false,

  checkWalletAvailability: async () => false,
  canAddCard: async () => ({
    canAdd: false,
    reason: AppleCanAddCardReason.UNKNOWN,
    message: 'Apple Wallet is only available on iOS.',
  }),
  startInAppProvisioning: () => notSupported('startInAppProvisioning'),
  submitProvisioningData: () => notSupported('submitProvisioningData'),
  cancelProvisioning: async () => false,

  listPasses: async () => [],
  isCardTokenized: async () => false,
  isWatchPaired: async () => false,

  activatePass: () => notSupported('activatePass'),

  onProvisioningCertificates: () => NO_SUBSCRIPTION,
  onProvisioningResult: () => NO_SUBSCRIPTION,
  setLogListener: async () => false,
  removeLogListener: async () => false,
  onLog: () => NO_SUBSCRIPTION,
};
