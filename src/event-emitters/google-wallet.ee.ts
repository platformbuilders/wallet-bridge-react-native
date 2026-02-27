/* eslint-disable promise/prefer-await-to-callbacks */
// ============================================================================
// GOOGLE WALLET EVENT EMITTER - ANDROID ONLY
// ============================================================================
// Este arquivo contém uma versão do GoogleWalletEventEmitter
// que funciona apenas em dispositivos Android.

import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import {
  type GoogleWalletIntentEvent,
  type GoogleWalletLogEvent,
} from '../types/google-wallet.types';

export class GoogleWalletEventEmitter {
  private eventEmitter: NativeEventEmitter | null = null;
  private listeners: Map<string, (event: GoogleWalletIntentEvent) => void> =
    new Map();
  private noIntentListeners: Map<string, () => void> = new Map();
  private validCallerNoIntentListeners: Map<string, () => void> = new Map();
  private logListeners: Map<string, (event: GoogleWalletLogEvent) => void> =
    new Map();
  private isIOS: boolean;

  constructor() {
    this.isIOS = Platform.OS === 'ios';

    // Em iOS, não inicializar o EventEmitter
    if (this.isIOS) {
      console.warn(
        '⚠️ [GoogleWalletEventEmitter] iOS detectado - EventEmitter desabilitado',
      );
      return;
    }

    try {
      // Verificar se o módulo está disponível (apenas em Android)
      const GoogleWalletModule = NativeModules.GoogleWallet;
      if (GoogleWalletModule) {
        this.eventEmitter = new NativeEventEmitter(GoogleWalletModule);
        console.log(
          '✅ [GoogleWalletEventEmitter] EventEmitter inicializado com sucesso',
        );
      } else {
        console.warn(
          '⚠️ [GoogleWalletEventEmitter] Módulo GoogleWallet não está disponível',
        );
      }
    } catch (error) {
      console.error(
        '❌ [GoogleWalletEventEmitter] Erro ao inicializar EventEmitter:',
        error,
      );
    }
  }

  /**
   * Adiciona um listener para eventos de intent do Google Wallet
   * Em iOS, retorna uma função vazia que não faz nada
   * @param callback Função que será chamada quando um evento for recebido
   * @returns Função para remover o listener
   */
  addIntentListener(
    callback: (event: GoogleWalletIntentEvent) => void,
  ): () => void {
    // Em iOS, retornar função vazia imediatamente
    if (this.isIOS) {
      console.warn(
        '⚠️ [GoogleWalletEventEmitter] addIntentListener chamado em iOS - operação ignorada',
      );
      return () => {}; // Retornar função vazia para iOS
    }

    const listenerId = `listener_${Date.now()}_${Math.random()}`;

    // Verificar se o EventEmitter está disponível
    if (!this.eventEmitter) {
      console.error(
        '❌ [GoogleWalletEventEmitter] EventEmitter não está disponível',
      );
      return () => {}; // Retornar função vazia para evitar erros
    }

    // Armazenar o callback
    this.listeners.set(listenerId, callback);

    // Criar o listener do NativeEventEmitter
    const subscription = this.eventEmitter.addListener(
      'GoogleWalletIntentReceived',
      (event: any) => {
        const walletEvent = event as GoogleWalletIntentEvent;
        console.log(
          '🎯 [GoogleWalletEventEmitter] Intent recebido:',
          walletEvent,
        );
        callback(walletEvent);
      },
    );

    console.log(
      `✅ [GoogleWalletEventEmitter] Listener adicionado: ${listenerId}`,
    );

    // Retornar função de cleanup
    return () => {
      this.listeners.delete(listenerId);
      subscription.remove();
      console.log(
        `🧹 [GoogleWalletEventEmitter] Listener removido: ${listenerId}`,
      );
    };
  }

  /**
   * Adiciona um listener para eventos de nenhuma intent recebida do Google Wallet
   * Em iOS, retorna uma função vazia que não faz nada
   * @param callback Função que será chamada quando nenhuma intent for recebida
   * @returns Função para remover o listener
   */
  addNoIntentListener(callback: () => void): () => void {
    // Em iOS, retornar função vazia imediatamente
    if (this.isIOS) {
      console.warn(
        '⚠️ [GoogleWalletEventEmitter] addNoIntentListener chamado em iOS - operação ignorada',
      );
      return () => {}; // Retornar função vazia para iOS
    }

    const listenerId = `no_intent_listener_${Date.now()}_${Math.random()}`;

    // Verificar se o EventEmitter está disponível
    if (!this.eventEmitter) {
      console.error(
        '❌ [GoogleWalletEventEmitter] EventEmitter não está disponível',
      );
      return () => {}; // Retornar função vazia para evitar erros
    }

    // Armazenar o callback
    this.noIntentListeners.set(listenerId, callback);

    // Criar o listener do NativeEventEmitter
    const subscription = this.eventEmitter.addListener(
      'GoogleWalletNoIntentReceived',
      () => {
        console.log('🎯 [GoogleWalletEventEmitter] Nenhuma intent recebida');
        callback();
      },
    );

    console.log(
      `✅ [GoogleWalletEventEmitter] NoIntent Listener adicionado: ${listenerId}`,
    );

    // Retornar função de cleanup
    return () => {
      this.noIntentListeners.delete(listenerId);
      subscription.remove();
      console.log(
        `🧹 [GoogleWalletEventEmitter] NoIntent Listener removido: ${listenerId}`,
      );
    };
  }

  /**
   * Adiciona um listener para o evento de wallet válida que chamou o app sem payload
   * Em iOS, retorna uma função vazia que não faz nada
   * @param callback Função que será chamada quando a wallet chamar sem dados
   * @returns Função para remover o listener
   */
  addValidCallerNoIntentListener(callback: () => void): () => void {
    if (this.isIOS) {
      console.warn(
        '⚠️ [GoogleWalletEventEmitter] addValidCallerNoIntentListener chamado em iOS - operação ignorada',
      );
      return () => {};
    }

    const listenerId = `valid_caller_no_intent_listener_${Date.now()}_${Math.random()}`;

    if (!this.eventEmitter) {
      console.error(
        '❌ [GoogleWalletEventEmitter] EventEmitter não está disponível',
      );
      return () => {};
    }

    this.validCallerNoIntentListeners.set(listenerId, callback);

    const subscription = this.eventEmitter.addListener(
      'GoogleWalletValidCallerNoIntentReceived',
      () => {
        console.log(
          '⚠️ [GoogleWalletEventEmitter] Wallet válida chamou sem payload',
        );
        callback();
      },
    );

    console.log(
      `✅ [GoogleWalletEventEmitter] ValidCallerNoIntent Listener adicionado: ${listenerId}`,
    );

    return () => {
      this.validCallerNoIntentListeners.delete(listenerId);
      subscription.remove();
      console.log(
        `🧹 [GoogleWalletEventEmitter] ValidCallerNoIntent Listener removido: ${listenerId}`,
      );
    };
  }

  /**
   * Adiciona um listener para eventos de log do Google Wallet
   * Em iOS, retorna uma função vazia que não faz nada
   * @param callback Função que será chamada quando um log for recebido
   * @returns Função para remover o listener
   */
  addLogListener(callback: (event: GoogleWalletLogEvent) => void): () => void {
    // Em iOS, retornar função vazia imediatamente
    if (this.isIOS) {
      console.warn(
        '⚠️ [GoogleWalletEventEmitter] addLogListener chamado em iOS - operação ignorada',
      );
      return () => {}; // Retornar função vazia para iOS
    }

    const listenerId = `log_listener_${Date.now()}_${Math.random()}`;

    // Verificar se o EventEmitter está disponível
    if (!this.eventEmitter) {
      console.error(
        '❌ [GoogleWalletEventEmitter] EventEmitter não está disponível',
      );
      return () => {}; // Retornar função vazia para evitar erros
    }

    // Armazenar o callback
    this.logListeners.set(listenerId, callback);

    // Criar o listener do NativeEventEmitter
    const subscription = this.eventEmitter.addListener(
      'WalletLog',
      (event: any) => {
        const logEvent = event as GoogleWalletLogEvent;
        callback(logEvent);
      },
    );

    console.log(
      `✅ [GoogleWalletEventEmitter] Log Listener adicionado: ${listenerId}`,
    );

    // Retornar função de cleanup
    return () => {
      this.logListeners.delete(listenerId);
      subscription.remove();
      console.log(
        `🧹 [GoogleWalletEventEmitter] Log Listener removido: ${listenerId}`,
      );
    };
  }

  /**
   * Remove todos os listeners ativos
   * Em iOS, apenas limpa o Map interno
   */
  removeAllListeners(): void {
    this.listeners.clear();
    this.noIntentListeners.clear();
    this.validCallerNoIntentListeners.clear();
    this.logListeners.clear();

    // Em iOS, não tentar remover listeners do EventEmitter
    if (this.isIOS) {
      console.warn(
        '⚠️ [GoogleWalletEventEmitter] removeAllListeners chamado em iOS - apenas Maps limpos',
      );
      return;
    }

    if (this.eventEmitter) {
      this.eventEmitter.removeAllListeners('GoogleWalletIntentReceived');
      this.eventEmitter.removeAllListeners('GoogleWalletNoIntentReceived');
      this.eventEmitter.removeAllListeners(
        'GoogleWalletValidCallerNoIntentReceived',
      );
      this.eventEmitter.removeAllListeners('WalletLog');
      console.log(
        '🧹 [GoogleWalletEventEmitter] Todos os listeners foram removidos',
      );
    }
  }

  /**
   * Obtém o número de listeners ativos
   * Em iOS, sempre retorna 0
   */
  getListenerCount(): number {
    if (this.isIOS) {
      console.warn(
        '⚠️ [GoogleWalletEventEmitter] getListenerCount chamado em iOS - retornando 0',
      );
      return 0;
    }
    return (
      this.listeners.size +
      this.noIntentListeners.size +
      this.validCallerNoIntentListeners.size +
      this.logListeners.size
    );
  }

  /**
   * Verifica se o EventEmitter está disponível
   * Em iOS, sempre retorna false
   */
  isAvailable(): boolean {
    if (this.isIOS) {
      console.warn(
        '⚠️ [GoogleWalletEventEmitter] isAvailable chamado em iOS - retornando false',
      );
      return false;
    }
    return this.eventEmitter !== null;
  }

  /**
   * Obtém informações sobre a plataforma atual
   * @returns string com informações da plataforma
   */
  getPlatformInfo(): string {
    if (this.isIOS) {
      return 'iOS - GoogleWalletEventEmitter desabilitado';
    }
    return `Android - GoogleWalletEventEmitter ${this.eventEmitter ? 'disponível' : 'indisponível'}`;
  }
}
