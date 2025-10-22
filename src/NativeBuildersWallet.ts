import { NativeModules, Platform } from 'react-native';
import type { GoogleWalletSpec, SamsungWalletSpec } from './types/index';
import { GoogleWalletIOS } from './google-wallet.ios';
import { SamsungWalletIOS } from './samsung-wallet.ios';

// Re-exporta todos os types
export * from './types/index';

// Re-exporta os event emitters
export * from './event-emitters/index';
// ============================================================================
// MÓDULOS ESPECÍFICOS
// ============================================================================

const { GoogleWallet, SamsungWallet } = NativeModules;

// Verificação de disponibilidade dos módulos
if (!GoogleWallet) {
  console.warn(
    'GoogleWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.'
  );
}

if (!SamsungWallet) {
  console.warn(
    'SamsungWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.'
  );
}

// Exporta os módulos específicos
export const GoogleWalletModule = Platform.select({
  ios: GoogleWalletIOS,
  android: GoogleWallet,
}) as GoogleWalletSpec;
export const SamsungWalletModule = Platform.select({
  ios: SamsungWalletIOS,
  android: SamsungWallet,
}) as SamsungWalletSpec;
