import React, { useState, useEffect, useRef } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { FlatList, View, Dimensions } from 'react-native';
import { AnimatedToggleButton } from './components/AnimatedToggleButton';
import {
  GooglePayExample,
  type GooglePayExampleRef,
} from './components/GooglePayExample';
import {
  SamsungPayExample,
  type SamsungPayExampleRef,
} from './components/SamsungPayExample';
import {
  GoogleWalletEventEmitter,
  SamsungWalletEventEmitter,
  GoogleWalletModule,
  SamsungWalletModule,
  type GoogleWalletIntentEvent,
  type SamsungWalletIntentEvent,
  type GoogleWalletLogEvent,
  type SamsungWalletLogEvent,
} from '@platformbuilders/wallet-bridge-react-native';

export default function App(): React.JSX.Element {
  const [isGooglePay, setIsGooglePay] = useState(true);

  // Refs para os componentes
  const googlePayRef = useRef<GooglePayExampleRef>(null);
  const samsungPayRef = useRef<SamsungPayExampleRef>(null);
  const flatListRef = useRef<FlatList>(null);

  // Instanciar os EventEmitters
  const googleEventEmitter = new GoogleWalletEventEmitter();
  const samsungEventEmitter = new SamsungWalletEventEmitter();

  // Obter largura da tela para o FlatList
  const screenWidth = Dimensions.get('window').width;

  // Dados para o FlatList - ambos componentes sempre montados
  const walletComponents = [
    {
      id: 'google',
      component: <GooglePayExample ref={googlePayRef} />,
    },
    {
      id: 'samsung',
      component: <SamsungPayExample ref={samsungPayRef} />,
    },
  ];

  const handleToggle = (): void => {
    const newIsGooglePay = !isGooglePay;
    setIsGooglePay(newIsGooglePay);

    // Scroll para o componente correto no FlatList
    const targetIndex = newIsGooglePay ? 0 : 1;
    flatListRef.current?.scrollToIndex({
      index: targetIndex,
      animated: true,
    });
  };

  // Função para detectar mudança de página quando o usuário arrasta
  const handleMomentumScrollEnd = (event: any) => {
    const contentOffsetX = event.nativeEvent.contentOffset.x;
    const pageIndex = Math.round(contentOffsetX / screenWidth);

    console.log(
      '🔄 [App] Scroll manual detectado - página:',
      pageIndex,
      'offset:',
      contentOffsetX
    );

    // Atualizar estado baseado na página atual
    const newIsGooglePay = pageIndex === 0;
    if (pageIndex >= 0 && pageIndex <= 1) {
      if (newIsGooglePay !== isGooglePay) {
        console.log(
          '🔄 [App] Atualizando toggle para:',
          newIsGooglePay ? 'Google Pay' : 'Samsung Pay'
        );
        setIsGooglePay(newIsGooglePay);
      } else {
        console.log('🔄 [App] Estado já está correto, não atualizando');
      }
    } else {
      console.log('⚠️ [App] Índice de página inválido:', pageIndex);
    }
  };

  // Configurar listeners para ambas as wallets
  useEffect(() => {
    console.log('🔍 [App] Configurando listeners das wallets...');

    // Ativar listeners de log para Google Wallet
    GoogleWalletModule.setLogListener().catch((error) => {
      console.log('❌ [App] Erro ao ativar log listener do Google:', error.message);
    });

    // Ativar listeners de log para Samsung Wallet
    SamsungWalletModule.setLogListener().catch((error) => {
      console.log('❌ [App] Erro ao ativar log listener do Samsung:', error.message);
    });

    // Listener para logs do Google Wallet
    const removeGoogleLogListener = googleEventEmitter.addLogListener(
      (logEvent: GoogleWalletLogEvent) => {
        console.log(
          `[${logEvent.level}] ${logEvent.tag}: ${logEvent.message}`,
          logEvent.error ? `\nErro: ${logEvent.error}` : '',
        );
      }
    );

    // Listener para logs do Samsung Wallet
    const removeSamsungLogListener = samsungEventEmitter.addLogListener(
      (logEvent: SamsungWalletLogEvent) => {
        console.log(
          `[${logEvent.level}] ${logEvent.tag}: ${logEvent.message}`,
          logEvent.error ? `\nErro: ${logEvent.error}` : '',
        );
      }
    );

    // Listener para Google Wallet
    const removeGoogleListener = googleEventEmitter.addIntentListener(
      (walletEvent: GoogleWalletIntentEvent) => {
        console.log('🎯 [App] Intent Google Wallet recebido:', walletEvent);
        console.log('🔍 [App] Google ref disponível:', !!googlePayRef.current);

        // Atualizar toggle para Google e navegar para o conteúdo do Google
        console.log(
          '🔄 [App] Atualizando toggle para Google Pay devido à intent recebida'
        );
        setIsGooglePay(true);

        // Navegar para o conteúdo do Google no FlatList
        const googleIndex = 0; // Google é o índice 0
        flatListRef.current?.scrollToIndex({
          index: googleIndex,
          animated: true,
        });

        // Chamar a função do componente Google Pay (sempre montado agora)
        if (googlePayRef.current) {
          console.log('✅ [App] Chamando processWalletIntent...');
          googlePayRef.current.processWalletIntent(walletEvent);
        } else {
          console.log('⚠️ [App] Google Pay ref não disponível');
        }
      }
    );

    // Listener para Samsung Wallet
    const removeSamsungListener = samsungEventEmitter.addIntentListener(
      (walletEvent: SamsungWalletIntentEvent) => {
        console.log('🎯 [App] Intent Samsung Wallet recebido:', walletEvent);
        console.log(
          '🔍 [App] Samsung ref disponível:',
          !!samsungPayRef.current
        );

        // Atualizar toggle para Samsung e navegar para o conteúdo da Samsung
        console.log(
          '🔄 [App] Atualizando toggle para Samsung Pay devido à intent recebida'
        );
        setIsGooglePay(false);

        // Navegar para o conteúdo da Samsung no FlatList
        const samsungIndex = 1; // Samsung é o índice 1
        flatListRef.current?.scrollToIndex({
          index: samsungIndex,
          animated: true,
        });

        // Chamar a função do componente Samsung Pay (sempre montado agora)
        if (samsungPayRef.current) {
          console.log('✅ [App] Chamando processSamsungWalletIntent...');
          samsungPayRef.current.processSamsungWalletIntent(walletEvent);
        } else {
          console.log('⚠️ [App] Samsung Pay ref não disponível');
        }
      }
    );

    // Listener para eventos de nenhuma intent do Google Wallet
    const removeGoogleNoIntentListener = googleEventEmitter.addNoIntentListener(
      () => {
        console.log('🎯 [App] Nenhuma intent recebida do Google Wallet');
        console.log('🔍 [App] Google Wallet não recebeu nenhuma intent válida');
      }
    );

    // Listener para eventos de nenhuma intent do Samsung Wallet
    const removeSamsungNoIntentListener =
      samsungEventEmitter.addNoIntentListener(() => {
        console.log('🎯 [App] Nenhuma intent recebida do Samsung Wallet');
        console.log(
          '🔍 [App] Samsung Wallet não recebeu nenhuma intent válida'
        );
      });

    // Listener para Google Wallet: caller válido sem payload
    const removeGoogleValidCallerNoIntentListener =
      googleEventEmitter.addValidCallerNoIntentListener(() => {
        console.log(
          '⚠️ [App] Google Wallet chamou o app mas não enviou payload'
        );

        // Navegar para o conteúdo do Google
        setIsGooglePay(true);
        flatListRef.current?.scrollToIndex({ index: 0, animated: true });

        if (googlePayRef.current) {
          googlePayRef.current.handleValidCallerNoIntent();
        }
      });

    // Listener para Samsung Wallet: caller válido sem payload
    const removeSamsungValidCallerNoIntentListener =
      samsungEventEmitter.addValidCallerNoIntentListener(() => {
        console.log(
          '⚠️ [App] Samsung Pay chamou o app mas não enviou payload'
        );

        // Navegar para o conteúdo da Samsung
        setIsGooglePay(false);
        flatListRef.current?.scrollToIndex({ index: 1, animated: true });

        if (samsungPayRef.current) {
          samsungPayRef.current.handleValidCallerNoIntent();
        }
      });

    // Cleanup dos listeners
    return () => {
      console.log('🧹 [App] Removendo listeners das wallets...');
      removeGoogleListener();
      removeSamsungListener();
      removeGoogleNoIntentListener();
      removeSamsungNoIntentListener();
      removeGoogleValidCallerNoIntentListener();
      removeSamsungValidCallerNoIntentListener();
      removeGoogleLogListener();
      removeSamsungLogListener();

      // Remover listeners de log
      GoogleWalletModule.removeLogListener().catch((error) => {
        console.log(
          '❌ [App] Erro ao remover log listener do Google:',
          error.message
        );
      });

      SamsungWalletModule.removeLogListener().catch((error) => {
        console.log(
          '❌ [App] Erro ao remover log listener do Samsung:',
          error.message
        );
      });
    };
  }, []); // Executar apenas uma vez na montagem

  // Função para renderizar cada item do FlatList
  const renderWalletComponent = ({
    item,
  }: {
    item: (typeof walletComponents)[0];
  }) => {
    return (
      <View style={{ flex: 1, width: screenWidth }}>{item.component}</View>
    );
  };

  // Função para obter o layout de cada item
  const getItemLayout = (_: any, index: number) => ({
    length: screenWidth, // Largura da tela para scroll horizontal
    offset: screenWidth * index,
    index,
  });

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: '#f5f5f5' }}>
      {/* Botão animado para alternar entre Google Pay e Samsung Pay */}
      <AnimatedToggleButton isGooglePay={isGooglePay} onToggle={handleToggle} />

      {/* FlatList com ambos os componentes sempre montados */}
      <FlatList
        ref={flatListRef}
        data={walletComponents}
        renderItem={renderWalletComponent}
        keyExtractor={(item) => item.id}
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        getItemLayout={getItemLayout}
        initialScrollIndex={0}
        onMomentumScrollEnd={handleMomentumScrollEnd}
        onScrollToIndexFailed={(info) => {
          console.log('⚠️ [App] Falha ao scroll para índice:', info.index);
          // Fallback: scroll para o índice mais próximo
          setTimeout(() => {
            flatListRef.current?.scrollToIndex({
              index: info.index,
              animated: true,
            });
          }, 100);
        }}
      />
    </SafeAreaView>
  );
}
