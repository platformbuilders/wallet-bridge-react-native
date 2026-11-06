import { Platform } from 'react-native';
import { GoogleWalletIOS } from './google-wallet.ios';
import { SamsungWalletIOS } from './samsung-wallet.ios';
import GoogleWallet from './specs/NativeGoogleWallet';
import SamsungWallet from './specs/NativeSamsungWallet';
import type { GoogleWalletSpec, SamsungWalletSpec } from './types/index';

// Re-exporta todos os types
export * from './types/index';

// Re-exporta os event emitters
export * from './event-emitters/index';
// ============================================================================
// MÓDULOS ESPECÍFICOS
// ============================================================================

// Verificação de disponibilidade dos módulos
if (!GoogleWallet) {
  console.warn(
    'GoogleWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.',
  );
}

if (!SamsungWallet) {
  console.warn(
    'SamsungWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.',
  );
}

// Exporta os módulos específicos
// A spec do codegen tipa retornos como `Object`; o contrato público continua
// sendo GoogleWalletSpec / SamsungWalletSpec.
export const GoogleWalletModule = Platform.select<unknown>({
  ios: GoogleWalletIOS,
  android: GoogleWallet,
}) as GoogleWalletSpec;
export const SamsungWalletModule = Platform.select<unknown>({
  ios: SamsungWalletIOS,
  android: SamsungWallet,
}) as SamsungWalletSpec;
