import { Platform } from 'react-native';
import { AppleWalletAndroid } from './apple-wallet.android';
import { GoogleWalletIOS } from './google-wallet.ios';
import {
  getNativeAppleWallet,
  getNativeGoogleWallet,
  getNativeSamsungWallet,
} from './native-modules';
import { SamsungWalletIOS } from './samsung-wallet.ios';
import type {
  AppleWalletSpec,
  GoogleWalletSpec,
  SamsungWalletSpec,
} from './types/index';

// Re-exporta todos os types
export * from './types/index';

// Re-exporta os event emitters
export * from './event-emitters/index';

// ============================================================================
// MÓDULOS ESPECÍFICOS
// ============================================================================

/**
 * Objeto que resolve o módulo só quando um método/constante é acessado.
 * Mantém o contrato público (`GoogleWalletModule.metodo()`) sem carregar o
 * TurboModule no import da lib.
 */
const lazyModule = <T extends object>(load: () => object | null): T =>
  new Proxy({} as T, {
    get(_target, property) {
      // Métodos de TurboModule (e dos stubs iOS) não dependem de `this`.
      return (load() as Record<PropertyKey, unknown> | null)?.[property];
    },
  });

// A spec do codegen tipa retornos como `Object`; o contrato público continua
// sendo GoogleWalletSpec / SamsungWalletSpec.
export const GoogleWalletModule = lazyModule<GoogleWalletSpec>(() =>
  Platform.OS === 'ios' ? GoogleWalletIOS : getNativeGoogleWallet(),
);

export const SamsungWalletModule = lazyModule<SamsungWalletSpec>(() =>
  Platform.OS === 'ios' ? SamsungWalletIOS : getNativeSamsungWallet(),
);

// Só existe no iOS; nas outras plataformas responde pelo stub.
export const AppleWalletModule = lazyModule<AppleWalletSpec>(() =>
  Platform.OS === 'ios' ? getNativeAppleWallet() : AppleWalletAndroid,
);
