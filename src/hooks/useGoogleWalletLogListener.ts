import { useEffect } from 'react';
import { GoogleWalletService } from '../services/google-wallet.service';
import type { GoogleWalletLogEvent } from '../types/google-wallet.types';
import { usePromise } from '../utils/usePromise';

/**
 * Hook para ouvir logs do Google Wallet.
 * Inicia o listener nativo e registra o listener JS, limpando tudo ao desmontar.
 */
export const useGoogleWalletLogListener = (
  onLogReceived: (event: GoogleWalletLogEvent) => void,
) => {
  const { loading, callPromise } = usePromise(
    GoogleWalletService.startLogListener,
  );

  useEffect(() => {
    const activateListener = async () => {
      try {
        await callPromise();
      } catch {}
    };

    activateListener();

    const removeLogListener =
      GoogleWalletService.walletEventEmitter.addLogListener(
        (event: GoogleWalletLogEvent) => {
          console.log('📝 [Hook] Google Wallet log:', event);
          onLogReceived(event);
        },
      );

    return () => {
      try {
        // eslint-disable-next-line promise/prefer-await-to-then
        GoogleWalletService.stopLogListener().catch(() => {});
      } catch {}
      removeLogListener();
    };
  }, []);

  return { isListeningLogs: loading };
};
