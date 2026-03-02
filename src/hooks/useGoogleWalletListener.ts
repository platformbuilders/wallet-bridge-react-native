import { useEffect } from 'react';
import { GoogleWalletService } from '../services/google-wallet.service';
import type { GoogleWalletIntentEvent } from '../types/google-wallet.types';
import { usePromise } from '../utils/usePromise';

/**
 * Hook para gerenciar o listener de intent do Google Wallet.
 * Ativa automaticamente o listener nativo, escuta eventos e faz cleanup ao desmontar.
 */
export const useGoogleWalletListener = (
  onIntentReceived: (event: GoogleWalletIntentEvent) => void,
  onNoIntentReceived?: () => void,
  onValidCallerNoIntentReceived?: () => void,
) => {
  const { loading, callPromise } = usePromise(
    GoogleWalletService.startIntentListener,
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
          console.log(
            '🎯 [Hook] Intent received from Google Wallet:',
            walletEvent,
          );
          onIntentReceived(walletEvent);
        },
      );

    const removeNoIntentListener = onNoIntentReceived
      ? GoogleWalletService.walletEventEmitter.addNoIntentListener(() => {
          console.log('🎯 [Hook] No intent received from Google Wallet');
          onNoIntentReceived();
        })
      : () => {};

    const removeValidCallerNoIntentListener = onValidCallerNoIntentReceived
      ? GoogleWalletService.walletEventEmitter.addValidCallerNoIntentListener(
          () => {
            console.log(
              '⚠️ [Hook] Valid caller no intent received from Google Wallet',
            );
            onValidCallerNoIntentReceived();
          },
        )
      : () => {};

    return () => {
      try {
        // eslint-disable-next-line promise/prefer-await-to-then
        GoogleWalletService.stopIntentListener().catch(() => {});
      } catch {}
      removeIntentListener();
      removeNoIntentListener();
      removeValidCallerNoIntentListener();
    };
  }, []);

  return { isCheckingPendingData: loading };
};
