import { useEffect } from 'react';
import type { GoogleWalletIntentEvent } from '../types/google-wallet.types';
import { GoogleWalletService } from '../services/google-wallet.service';
import { usePromise } from '../utils/usePromise';

/**
 * Hook para gerenciar o listener de intent do Google Wallet.
 * Ativa automaticamente o listener nativo, escuta eventos e faz cleanup ao desmontar.
 */
export const useGoogleWalletListener = (
  onIntentReceived: (event: GoogleWalletIntentEvent) => void,
  onNoIntentReceived?: () => void
) => {
  const { loading, callPromise } = usePromise(
    GoogleWalletService.startIntentListener
  );

  useEffect(() => {
    const activateListener = async () => {
      try {
        await callPromise();
      } catch (e) {
        // Silenciar erro para iOS/ambientes sem suporte
      }
    };

    activateListener();

    const removeIntentListener =
      GoogleWalletService.walletEventEmitter.addIntentListener(
        (walletEvent: GoogleWalletIntentEvent) => {
          // eslint-disable-next-line no-console
          console.log(
            '🎯 [Hook] Intent received from Google Wallet:',
            walletEvent
          );
          onIntentReceived(walletEvent);
        }
      );

    const removeNoIntentListener = onNoIntentReceived
      ? GoogleWalletService.walletEventEmitter.addNoIntentListener(() => {
          // eslint-disable-next-line no-console
          console.log('🎯 [Hook] No intent received from Google Wallet');
          onNoIntentReceived();
        })
      : () => {};

    return () => {
      try {
        GoogleWalletService.stopIntentListener().catch(() => {});
      } catch {}
      removeIntentListener();
      removeNoIntentListener();
    };
  }, []);

  return { isCheckingPendingData: loading };
};
