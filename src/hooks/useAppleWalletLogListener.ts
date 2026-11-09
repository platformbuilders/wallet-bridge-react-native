import { useEffect, useRef, useState } from 'react';
import { Platform } from 'react-native';
import { AppleWalletModule } from '../NativeBuildersWallet';
import type { AppleWalletLogEvent } from '../types/apple-wallet.types';
import type { WalletListenerOptions } from '../types/common.types';

/**
 * Escuta os logs nativos do Apple Wallet (iOS; no-op nas outras plataformas).
 * Registra o listener JS antes de ligar o log no nativo e desliga tudo ao
 * desmontar. `isListeningLogs` fica `true` enquanto o log nativo estiver
 * ligado. O nativo só emite logs em build Debug.
 *
 * Use `options.enabled` para não assinar o log quando não for necessário
 * (ex.: `enabled: !isProduction`).
 */
export const useAppleWalletLogListener = (
  onLogReceived: (event: AppleWalletLogEvent) => void,
  options: WalletListenerOptions = {},
) => {
  const { enabled = true } = options;
  const [isListeningLogs, setIsListeningLogs] = useState(false);
  const callback = useRef(onLogReceived);
  callback.current = onLogReceived;

  useEffect(() => {
    if (!enabled || Platform.OS !== 'ios') return undefined;

    const subscription = AppleWalletModule.onLog((event) =>
      callback.current(event),
    );

    let mounted = true;
    const startLogListener = async () => {
      const started = await AppleWalletModule.setLogListener();
      if (mounted) setIsListeningLogs(started);
    };
    void startLogListener();

    return () => {
      mounted = false;
      setIsListeningLogs(false);
      subscription.remove();
      void AppleWalletModule.removeLogListener();
    };
  }, [enabled]);

  return { isListeningLogs };
};
