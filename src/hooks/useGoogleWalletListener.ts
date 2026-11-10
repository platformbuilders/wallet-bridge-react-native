import { useEffect, useRef, useState } from 'react';
import { Platform } from 'react-native';
import { GoogleWalletService } from '../services/google-wallet.service';
import type { WalletListenerOptions } from '../types/common.types';
import type { GoogleWalletIntentEvent } from '../types/google-wallet.types';

/**
 * Escuta os eventos de App2App do Google Wallet (Android; no-op nas outras
 * plataformas).
 *
 * - Registra os listeners JS **antes** de `setIntentListener`, que entrega o
 *   intent pendente: o intent que abriu o app não se perde.
 * - Os callbacks podem mudar a cada render; o hook sempre chama a versão mais
 *   recente, sem registrar os listeners de novo.
 * - `options.enabled: false` não assina nada e não chama o nativo.
 */
export const useGoogleWalletListener = (
  onIntentReceived: (event: GoogleWalletIntentEvent) => void,
  onNoIntentReceived?: () => void,
  onValidCallerNoIntentReceived?: () => void,
  options: WalletListenerOptions = {},
) => {
  const { enabled = true } = options;
  const [isCheckingPendingData, setIsCheckingPendingData] = useState(false);
  const callbacks = useRef({
    onIntentReceived,
    onNoIntentReceived,
    onValidCallerNoIntentReceived,
  });
  callbacks.current = {
    onIntentReceived,
    onNoIntentReceived,
    onValidCallerNoIntentReceived,
  };

  useEffect(() => {
    if (!enabled || Platform.OS !== 'android') return undefined;

    const emitter = GoogleWalletService.walletEventEmitter;
    const removeListeners = [
      emitter.addIntentListener((event) =>
        callbacks.current.onIntentReceived(event),
      ),
      emitter.addNoIntentListener(() =>
        callbacks.current.onNoIntentReceived?.(),
      ),
      emitter.addValidCallerNoIntentListener(() =>
        callbacks.current.onValidCallerNoIntentReceived?.(),
      ),
    ];

    let mounted = true;
    const requestPendingIntent = async () => {
      setIsCheckingPendingData(true);
      try {
        await GoogleWalletService.startIntentListener();
      } catch {
        // O service já devolve `false` em erro; só garante o flag abaixo.
      } finally {
        if (mounted) setIsCheckingPendingData(false);
      }
    };
    void requestPendingIntent();

    return () => {
      mounted = false;
      removeListeners.forEach((remove) => remove());
      void GoogleWalletService.stopIntentListener();
    };
  }, [enabled]);

  return { isCheckingPendingData };
};
