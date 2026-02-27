import { useEffect } from 'react';
import { SamsungWalletService } from '../services/samsung-wallet.service';
import type { SamsungWalletIntentEvent } from '../types/samsung-wallet.types';
import { usePromise } from '../utils/usePromise';

/**
 * Hook para gerenciar o listener de intent do Samsung Wallet.
 * Ativa automaticamente o listener nativo, escuta eventos e faz cleanup ao desmontar.
 */
export const useSamsungWalletListener = (
  onIntentReceived: (event: SamsungWalletIntentEvent) => void,
  onNoIntentReceived?: () => void,
  onValidCallerNoIntentReceived?: () => void,
) => {
  const { loading, callPromise } = usePromise(
    SamsungWalletService.startWalletIntentListener,
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
      SamsungWalletService.walletEventEmitter.addIntentListener(
        (walletEvent: SamsungWalletIntentEvent) => {
          console.log(
            '🎯 [Hook] Intent received from Samsung Wallet:',
            walletEvent,
          );
          onIntentReceived(walletEvent);
        },
      );

    const removeNoIntentListener = onNoIntentReceived
      ? SamsungWalletService.walletEventEmitter.addNoIntentListener(() => {
          console.log('🎯 [Hook] No intent received from Samsung Wallet');
          onNoIntentReceived();
        })
      : () => {};

    const removeValidCallerNoIntentListener = onValidCallerNoIntentReceived
      ? SamsungWalletService.walletEventEmitter.addValidCallerNoIntentListener(
          () => {
            console.log(
              '⚠️ [Hook] Valid caller no intent received from Samsung Wallet',
            );
            onValidCallerNoIntentReceived();
          },
        )
      : () => {};

    return () => {
      try {
        // eslint-disable-next-line promise/prefer-await-to-then
        SamsungWalletService.stopWalletIntentListener().catch(() => {});
      } catch {}
      removeIntentListener();
      removeNoIntentListener();
      removeValidCallerNoIntentListener();
    };
  }, []);

  return { isCheckingPendingData: loading };
};
