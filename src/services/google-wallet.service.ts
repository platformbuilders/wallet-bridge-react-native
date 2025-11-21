import { Platform } from 'react-native';
import { GoogleWalletEventEmitter } from '../event-emitters/google-wallet.ee';
import { GoogleWalletModule } from '../NativeBuildersWallet';

class GoogleWalletServiceImpl {
  public walletEventEmitter = new GoogleWalletEventEmitter();

  async startIntentListener(): Promise<boolean> {
    if (Platform.OS !== 'android') return false;
    try {
      return await GoogleWalletModule.setIntentListener();
    } catch (e) {
      return false;
    }
  }

  async stopIntentListener(): Promise<boolean> {
    if (Platform.OS !== 'android') return false;
    try {
      return await GoogleWalletModule.removeIntentListener();
    } catch (e) {
      return false;
    }
  }

  async startLogListener(): Promise<boolean> {
    if (Platform.OS !== 'android') return false;
    try {
      return await GoogleWalletModule.setLogListener();
    } catch (e) {
      return false;
    }
  }

  async stopLogListener(): Promise<boolean> {
    if (Platform.OS !== 'android') return false;
    try {
      return await GoogleWalletModule.removeLogListener();
    } catch (e) {
      return false;
    }
  }
}

export const GoogleWalletService = new GoogleWalletServiceImpl();
