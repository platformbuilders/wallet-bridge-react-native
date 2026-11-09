import { useEffect, useRef, useState } from 'react';
import { Platform } from 'react-native';
import { GoogleWalletService } from '../services/google-wallet.service';
import type { WalletListenerOptions } from '../types/common.types';
import type { GoogleWalletLogEvent } from '../types/google-wallet.types';

/**
 * Escuta os logs nativos do Google Wallet (Android; no-op nas outras
 * plataformas). Registra o listener JS antes de ligar o log no nativo e
 * desliga tudo ao desmontar.
 *
 * Use `options.enabled` para não assinar o log quando não for necessário
 * (ex.: `enabled: !isProduction`).
 */
export const useGoogleWalletLogListener = (
  onLogReceived: (event: GoogleWalletLogEvent) => void,
  options: WalletListenerOptions = {},
) => {
  const { enabled = true } = options;
  const [isListeningLogs, setIsListeningLogs] = useState(false);
  const callback = useRef(onLogReceived);
  callback.current = onLogReceived;

  useEffect(() => {
    if (!enabled || Platform.OS !== 'android') return undefined;

    const removeLogListener =
      GoogleWalletService.walletEventEmitter.addLogListener((event) =>
        callback.current(event),
      );

    let mounted = true;
    const startLogListener = async () => {
      setIsListeningLogs(true);
      await GoogleWalletService.startLogListener();
      if (mounted) setIsListeningLogs(false);
    };
    void startLogListener();

    return () => {
      mounted = false;
      removeLogListener();
      void GoogleWalletService.stopLogListener();
    };
  }, [enabled]);

  return { isListeningLogs };
};
