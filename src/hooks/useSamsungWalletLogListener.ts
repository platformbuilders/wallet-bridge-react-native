import { useEffect, useRef, useState } from 'react';
import { Platform } from 'react-native';
import { SamsungWalletService } from '../services/samsung-wallet.service';
import type { WalletListenerOptions } from '../types/common.types';
import type { SamsungWalletLogEvent } from '../types/samsung-wallet.types';

/**
 * Escuta os logs nativos do Samsung Wallet (Android; no-op nas outras
 * plataformas). Registra o listener JS antes de ligar o log no nativo e
 * desliga tudo ao desmontar. `isListeningLogs` fica `true` enquanto o log
 * nativo estiver ligado.
 *
 * Use `options.enabled` para não assinar o log quando não for necessário
 * (ex.: `enabled: !isProduction`).
 */
export const useSamsungWalletLogListener = (
  onLogReceived: (event: SamsungWalletLogEvent) => void,
  options: WalletListenerOptions = {},
) => {
  const { enabled = true } = options;
  const [isListeningLogs, setIsListeningLogs] = useState(false);
  const callback = useRef(onLogReceived);
  callback.current = onLogReceived;

  useEffect(() => {
    if (!enabled || Platform.OS !== 'android') return undefined;

    const removeLogListener =
      SamsungWalletService.walletEventEmitter.addLogListener((event) =>
        callback.current(event),
      );

    let mounted = true;
    const startLogListener = async () => {
      const started = await SamsungWalletService.startLogListener();
      if (mounted) setIsListeningLogs(started);
    };
    void startLogListener();

    return () => {
      mounted = false;
      setIsListeningLogs(false);
      removeLogListener();
      void SamsungWalletService.stopLogListener();
    };
  }, [enabled]);

  return { isListeningLogs };
};
