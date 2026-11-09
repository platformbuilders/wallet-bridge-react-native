import { Platform } from 'react-native';
import type { Spec as NativeGoogleWalletSpec } from './specs/NativeGoogleWallet';
import type { Spec as NativeSamsungWalletSpec } from './specs/NativeSamsungWallet';

// Os TurboModules só são carregados no primeiro uso, não no import da lib:
// importar `@platformbuilders/wallet-bridge-react-native` não inicializa nada
// nativo, e o app decide quando cada wallet entra em uso.

const warned = new Set<string>();

const warnUnavailable = (name: string): void => {
  if (warned.has(name)) return;
  warned.add(name);
  console.warn(
    `${name} native module is not available. Make sure you have properly installed the library and rebuilt your app.`,
  );
};

/** TurboModule do Google Wallet, ou `null` fora do Android / sem o módulo. */
export const getNativeGoogleWallet = (): NativeGoogleWalletSpec | null => {
  if (Platform.OS !== 'android') return null;
  const nativeModule =
    (
      require('./specs/NativeGoogleWallet') as typeof import('./specs/NativeGoogleWallet')
    ).default ?? null;
  if (!nativeModule) warnUnavailable('GoogleWallet');
  return nativeModule;
};

/** TurboModule do Samsung Wallet, ou `null` fora do Android / sem o módulo. */
export const getNativeSamsungWallet = (): NativeSamsungWalletSpec | null => {
  if (Platform.OS !== 'android') return null;
  const nativeModule =
    (
      require('./specs/NativeSamsungWallet') as typeof import('./specs/NativeSamsungWallet')
    ).default ?? null;
  if (!nativeModule) warnUnavailable('SamsungWallet');
  return nativeModule;
};
