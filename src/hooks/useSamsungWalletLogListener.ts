import { useEffect } from 'react';
import { SamsungWalletService } from '../services/samsung-wallet.service';
import type { SamsungWalletLogEvent } from '../types/samsung-wallet.types';
import { usePromise } from '../utils/usePromise';

/**
 * Hook para ouvir logs do Samsung Wallet.
 * Inicia o listener nativo e registra o listener JS, limpando tudo ao desmontar.
 */
export const useSamsungWalletLogListener = (
  onLogReceived: (event: SamsungWalletLogEvent) => void,
) => {
  const { loading, callPromise } = usePromise(
    SamsungWalletService.startLogListener,
  );

  useEffect(() => {
    const activateListener = async () => {
      try {
        await callPromise();
      } catch {}
    };

    activateListener();

    const removeLogListener =
      SamsungWalletService.walletEventEmitter.addLogListener(
        (event: SamsungWalletLogEvent) => {
          console.log('📝 [Hook] Samsung Wallet log:', event);
          onLogReceived(event);
        },
      );

    return () => {
      try {
        // eslint-disable-next-line promise/prefer-await-to-then
        SamsungWalletService.stopLogListener().catch(() => {});
      } catch {}
      removeLogListener();
    };
  }, []);

  return { isListeningLogs: loading };
};
