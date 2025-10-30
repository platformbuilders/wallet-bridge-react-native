import { Platform } from 'react-native';
import { SamsungWalletEventEmitter } from '../event-emitters/samsung-wallet.ee';
import { SamsungWalletModule } from '../NativeBuildersWallet';

class SamsungWalletServiceImpl {
  public walletEventEmitter = new SamsungWalletEventEmitter();

  async startWalletIntentListener(): Promise<boolean> {
    if (Platform.OS !== 'android') return false;
    try {
      return await SamsungWalletModule.setIntentListener();
    } catch (e) {
      return false;
    }
  }

  async stopWalletIntentListener(): Promise<boolean> {
    if (Platform.OS !== 'android') return false;
    try {
      return await SamsungWalletModule.removeIntentListener();
    } catch (e) {
      return false;
    }
  }

  async startLogListener(): Promise<boolean> {
    if (Platform.OS !== 'android') return false;
    try {
      return await SamsungWalletModule.setLogListener();
    } catch (e) {
      return false;
    }
  }

  async stopLogListener(): Promise<boolean> {
    if (Platform.OS !== 'android') return false;
    try {
      return await SamsungWalletModule.removeLogListener();
    } catch (e) {
      return false;
    }
  }
}

export const SamsungWalletService = new SamsungWalletServiceImpl();
