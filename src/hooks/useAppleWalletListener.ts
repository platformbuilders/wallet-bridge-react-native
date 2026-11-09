import { useEffect, useRef } from 'react';
import { Platform } from 'react-native';
import { AppleWalletModule } from '../NativeBuildersWallet';
import type {
  AppleProvisioningCertificatesEvent,
  AppleProvisioningResultEvent,
} from '../types/apple-wallet.types';
import type { WalletListenerOptions } from '../types/common.types';

export interface AppleWalletListenerCallbacks {
  /** Passo 1: repassar ao emissor e chamar `submitProvisioningData` com a resposta. */
  onProvisioningCertificates: (
    event: AppleProvisioningCertificatesEvent,
  ) => void;
  /** Transições de estágio e resultado final do provisioning. */
  onProvisioningResult?: (event: AppleProvisioningResultEvent) => void;
}

/**
 * Escuta os eventos do In-App Provisioning do Apple Wallet (iOS; no-op nas
 * outras plataformas).
 *
 * - Os callbacks podem mudar a cada render; o hook sempre chama a versão mais
 *   recente, sem registrar os listeners de novo.
 * - `options.enabled: false` não assina nada.
 */
export const useAppleWalletListener = (
  callbacks: AppleWalletListenerCallbacks,
  options: WalletListenerOptions = {},
) => {
  const { enabled = true } = options;
  const latest = useRef(callbacks);
  latest.current = callbacks;

  useEffect(() => {
    if (!enabled || Platform.OS !== 'ios') return undefined;

    const subscriptions = [
      AppleWalletModule.onProvisioningCertificates((event) =>
        latest.current.onProvisioningCertificates(event),
      ),
      AppleWalletModule.onProvisioningResult((event) =>
        latest.current.onProvisioningResult?.(event),
      ),
    ];

    return () => {
      subscriptions.forEach((subscription) => subscription.remove());
    };
  }, [enabled]);
};
