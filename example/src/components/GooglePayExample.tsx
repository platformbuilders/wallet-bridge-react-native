import {
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
  ScrollView,
  TextInput,
  Image,
  View,
  Clipboard,
} from 'react-native';
import {
  GoogleWalletModule as GoogleWalletClient,
  GoogleActivationStatus,
  GoogleWalletDataFormat,
} from '@platformbuilders/wallet-bridge-react-native';
import type {
  GooglePushTokenizeRequest,
  GoogleWalletData,
  GoogleWalletIntentEvent,
  GoogleTokenInfo,
  GoogleWalletConstants,
  GoogleTokenStatus,
} from '@platformbuilders/wallet-bridge-react-native';
import { useState, useEffect, useImperativeHandle, forwardRef } from 'react';

import { SafeAreaView } from 'react-native-safe-area-context';

// Função simples para mostrar erros da wallet
const handleGoogleWalletError = (error: unknown): string => {
  console.log('🔍 [JS] Erro Google Wallet:', error);
  const errorMessage = error instanceof Error ? error.message : String(error);
  return errorMessage;
};

// Interface para o useImperativeHandle
export interface GooglePayExampleRef {
  processWalletIntent: (walletEvent: GoogleWalletIntentEvent) => void;
}

export const GooglePayExample = forwardRef<GooglePayExampleRef>(
  (_props, ref): React.JSX.Element => {
    // Estado para o OPC (Opaque Payment Card)
    const [opcValue, setOpcValue] = useState(
      'M0VGNkZENjRFMEM1MTdEOTgwOEU4N0RGMzRCNkE0M0U4QURBNUEyNjIzQjgyQzEzODZEQkZGN0JEQzM3NzI4NjQ0ODMzRDhBODlFREEwODhDREI2NkMwODM2NkQxRERCN0EzQ0U0RkZFMjJERUZFMEYwM0VCQjlBRkVGNDEzNUQxMjhFODg4NkIzMjBFREZENzk5OUMyODQ4ODRCMzNBMURCNDA0MjQwRDYxMEJDNzRFMjQzMTcwRkNBQzEzRjgzQ0Y4ODI0RTc1QkE4RENGRTY3MjRDQ0U4MEIxM0RCOUMwRjA2MkYzQkIzMjJBNjlE'
    );

    // Estado para o resultado do intent
    const [intentResult, setIntentResult] =
      useState<GoogleWalletIntentEvent | null>(null);

    // Estado para os dados decodificados
    const [decodedData, setDecodedData] = useState<
      Record<string, any> | string | null
    >(null);

    // Estado para indicar se está verificando dados pendentes
    const [isCheckingPendingData, setIsCheckingPendingData] = useState(false);

    // Instanciar o GoogleWalletClient
    const googleWalletClient = GoogleWalletClient;
    const constants: GoogleWalletConstants = googleWalletClient.getConstants();

    // Função para mostrar dados já decodificados
    const showDecodedData = (
      data: string,
      eventType: string,
      action: string
    ): Record<string, any> | string | null => {
      try {
        console.log('🔍 [JS] Mostrando dados já decodificados...');
        console.log('🔍 [JS] Dados decodificados (string):', data);

        // Tentar fazer parse como JSON
        let parsedData: Record<string, any> | string;
        try {
          parsedData = JSON.parse(data);
          console.log('✅ [JS] Dados parseados como JSON:', parsedData);
        } catch (jsonError) {
          console.log(
            '⚠️ [JS] Dados não são JSON válido, mostrando como string'
          );
          parsedData = data;
        }

        // Armazenar dados decodificados no estado
        setDecodedData(parsedData);

        // Criar mensagem formatada
        let message = `🎯 ${eventType} Recebido!\n\n`;
        message += `📱 Ação: ${action}\n\n`;
        message += `📋 Dados Decodificados (Automático):\n`;

        if (typeof parsedData === 'object' && parsedData !== null) {
          message += JSON.stringify(parsedData, null, 2);
        } else {
          message += parsedData;
        }

        // Mostrar alert com dados decodificados
        Alert.alert(`✅ ${eventType}`, message, [
          { text: 'OK' },
          {
            text: '📋 Copiar Dados',
            onPress: () => {
              const dataToCopy =
                typeof parsedData === 'object'
                  ? JSON.stringify(parsedData, null, 2)
                  : String(parsedData);
              Clipboard.setString(dataToCopy);
              Alert.alert(
                'Sucesso',
                'Dados copiados para a área de transferência!'
              );
            },
          },
        ]);

        return parsedData;
      } catch (error) {
        console.error('❌ [JS] Erro ao processar dados decodificados:', error);
        const errorMessage =
          error instanceof Error ? error.message : 'Erro desconhecido';

        // Limpar dados decodificados em caso de erro
        setDecodedData(null);

        Alert.alert(
          `❌ ${eventType} (Erro de Processamento)`,
          `Intent recebido mas houve erro ao processar os dados!\n\nAção: ${action}\nErro: ${errorMessage}\n\nDados originais:\n${data.substring(0, 200)}...`,
          [{ text: 'OK' }]
        );

        return null;
      }
    };

    // Função para decodificar dados base64 e mostrar resultado
    const decodeAndShowData = (
      data: string,
      eventType: string,
      action: string
    ): Record<string, any> | string | null => {
      try {
        console.log('🔍 [JS] Decodificando dados base64...');
        console.log(
          '🔍 [JS] Dados originais (base64):',
          data.substring(0, 100) + '...'
        );

        // Decodificar dados base64
        const decodedData = atob(data);
        console.log('🔍 [JS] Dados decodificados (string):', decodedData);

        // Tentar fazer parse como JSON
        let parsedData: Record<string, any> | string;
        try {
          parsedData = JSON.parse(decodedData);
          console.log('✅ [JS] Dados parseados como JSON:', parsedData);
        } catch (jsonError) {
          console.log(
            '⚠️ [JS] Dados não são JSON válido, mostrando como string'
          );
          parsedData = decodedData;
        }

        // Armazenar dados decodificados no estado
        setDecodedData(parsedData);

        // Criar mensagem formatada
        let message = `🎯 ${eventType} Recebido!\n\n`;
        message += `📱 Ação: ${action}\n\n`;
        message += `📋 Dados Decodificados:\n`;

        if (typeof parsedData === 'object' && parsedData !== null) {
          message += JSON.stringify(parsedData, null, 2);
        } else {
          message += parsedData;
        }

        // Mostrar alert com dados decodificados
        Alert.alert(`✅ ${eventType}`, message, [
          { text: 'OK' },
          {
            text: '📋 Copiar Dados',
            onPress: () => {
              const dataToCopy =
                typeof parsedData === 'object'
                  ? JSON.stringify(parsedData, null, 2)
                  : String(parsedData);
              Clipboard.setString(dataToCopy);
              Alert.alert(
                'Sucesso',
                'Dados copiados para a área de transferência!'
              );
            },
          },
        ]);

        return parsedData;
      } catch (error) {
        console.error('❌ [JS] Erro ao decodificar dados base64:', error);
        const errorMessage =
          error instanceof Error ? error.message : 'Erro desconhecido';

        // Limpar dados decodificados em caso de erro
        setDecodedData(null);

        Alert.alert(
          `❌ ${eventType} (Erro de Decodificação)`,
          `Intent recebido mas houve erro ao decodificar os dados!\n\nAção: ${action}\nErro: ${errorMessage}\n\nDados originais (base64):\n${data.substring(0, 200)}...`,
          [{ text: 'OK' }]
        );

        return null;
      }
    };

    // Função para processar eventos de intent da carteira (reutilizável)
    const processWalletIntent = (
      walletEvent: GoogleWalletIntentEvent
    ): void => {
      console.log('🎯 Processando intent da carteira:', walletEvent);

      // Atualizar estado com o resultado do intent
      setIntentResult(walletEvent);

      // Processar diferentes tipos de intent e mostrar alert
      switch (walletEvent.type) {
        case 'ACTIVATE_TOKEN':
          if (
            walletEvent.data &&
            walletEvent.dataFormat === GoogleWalletDataFormat.BASE64_DECODED
          ) {
            // Dados já decodificados automaticamente pelo nativo
            console.log('✅ Dados já decodificados automaticamente');
            showDecodedData(
              walletEvent.data,
              'Token Ativado',
              walletEvent.action
            );
          } else if (
            walletEvent.data &&
            walletEvent.dataFormat === GoogleWalletDataFormat.RAW
          ) {
            // Dados em formato raw, tentar decodificar manualmente
            decodeAndShowData(
              walletEvent.data,
              'Token Ativado',
              walletEvent.action
            );
          } else {
            Alert.alert(
              'Token Ativado',
              `Intent de ativação de token recebido!\nAção: ${walletEvent.action}\nFormato: ${walletEvent.dataFormat || 'N/A'}`,
              [{ text: 'OK' }]
            );
          }
          break;

        case 'WALLET_INTENT':
          if (
            walletEvent.data &&
            walletEvent.dataFormat === GoogleWalletDataFormat.BASE64_DECODED
          ) {
            // Dados já decodificados automaticamente pelo nativo
            console.log('✅ Dados já decodificados automaticamente');
            showDecodedData(
              walletEvent.data,
              'Intent da Carteira',
              walletEvent.action
            );
          } else if (
            walletEvent.data &&
            walletEvent.dataFormat === GoogleWalletDataFormat.RAW
          ) {
            // Dados em formato raw, tentar decodificar manualmente
            decodeAndShowData(
              walletEvent.data,
              'Intent da Carteira',
              walletEvent.action
            );
          } else {
            Alert.alert(
              'Intent da Carteira',
              `Intent relacionado à carteira recebido!\nAção: ${walletEvent.action}\nFormato: ${walletEvent.dataFormat || 'N/A'}`,
              [{ text: 'OK' }]
            );
          }
          break;

        case 'INVALID_CALLER':
          Alert.alert(
            '⚠️ Chamador Inválido',
            `Tentativa de acesso não autorizada!\nAção: ${walletEvent.action}\nPackage: ${walletEvent.callingPackage}\nErro: ${walletEvent.error}`,
            [{ text: 'OK' }]
          );
          console.warn('🚨 Tentativa de acesso não autorizada:', walletEvent);
          break;

        default:
          console.log('Intent não reconhecido:', walletEvent);
      }
    };

    // Expor função para o componente pai através do useImperativeHandle
    useImperativeHandle(ref, () => ({
      processWalletIntent,
    }));

    // Configurar listener de intent automaticamente
    useEffect(() => {
      let isMounted = true;

      // Ativar listener automaticamente
      const activateListener = async () => {
        try {
          setIsCheckingPendingData(true);
          await googleWalletClient.setIntentListener();
          console.log('✅ [JS] Listener de intent ativado automaticamente');
        } catch (error) {
          console.error('❌ [JS] Erro ao ativar listener:', error);
        } finally {
          if (isMounted) {
            setIsCheckingPendingData(false);
          }
        }
      };

      activateListener();

      // Cleanup quando o componente for desmontado
      return () => {
        isMounted = false;

        // Desativar listener nativo
        googleWalletClient.removeIntentListener().catch((error) => {
          console.error('❌ [JS] Erro ao remover listener nativo:', error);
        });
      };
    }, []);

    // Verificar se o módulo está disponível
    if (!googleWalletClient) {
      return (
        <ScrollView style={styles.container}>
          <Text style={styles.errorText}>
            GoogleWalletClient não está disponível. Verifique se o módulo nativo
            foi instalado corretamente.
          </Text>
        </ScrollView>
      );
    }

    const handleCheckAvailability = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando verificação de disponibilidade...');
        const isAvailable: boolean =
          await googleWalletClient.checkWalletAvailability();
        console.log('✅ [JS] Disponibilidade verificada:', isAvailable);
        Alert.alert(
          'Disponibilidade',
          `Google Wallet disponível: ${isAvailable ? 'Sim' : 'Não'}`
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao verificar disponibilidade:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert(
          'Erro',
          `Erro ao verificar disponibilidade: ${errorMessage}`
        );
      }
    };

    const handleGetWalletInfo = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando obtenção de informações da wallet...');
        const walletInfo: GoogleWalletData =
          await googleWalletClient.getSecureWalletInfo();
        console.log('✅ [JS] Informações da wallet obtidas:', walletInfo);
        Alert.alert(
          'Informações da Google Wallet',
          `Device ID: ${walletInfo.deviceID}\nWallet Account ID: ${walletInfo.walletAccountID}`
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao obter informações da wallet:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro', `Erro ao obter informações: ${errorMessage}`);
      }
    };

    const handleGetTokenStatus = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando verificação de status do token...');

        // Obter constantes para usar o tokenServiceProvider
        const tokenServiceProvider = constants.TOKEN_PROVIDER_ELO;
        const tokenReferenceId = 'test-token-id'; // ID de exemplo

        const tokenStatus: GoogleTokenStatus =
          await googleWalletClient.getTokenStatus(
            tokenServiceProvider,
            tokenReferenceId
          );
        console.log('✅ [JS] Status do token obtido:', tokenStatus);

        Alert.alert(
          'Status do Token',
          `Estado: ${tokenStatus.tokenState}\nSelecionado: ${tokenStatus.isSelected ? 'Sim' : 'Não'}`
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao obter status do token:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro', `Erro ao obter status do token: ${errorMessage}`);
      }
    };

    const handleGetEnvironment = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando obtenção do environment...');
        const environment: string = await googleWalletClient.getEnvironment();
        console.log('✅ [JS] Environment obtido:', environment);
        Alert.alert('Environment', `Environment: ${environment}`);
      } catch (err) {
        console.log('❌ [JS] Erro ao obter environment:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro', `Erro ao obter environment: ${errorMessage}`);
      }
    };

    const handleIsTokenized = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando verificação se está tokenizado...');

        // Obter constantes para usar os valores corretos
        const cardNetwork = constants.CARD_NETWORK_ELO;
        const tokenServiceProvider = constants.TOKEN_PROVIDER_ELO;
        const fpanLastFour = '6890'; // Últimos 4 dígitos de exemplo

        const isTokenized: boolean = await googleWalletClient.isTokenized(
          fpanLastFour,
          cardNetwork,
          tokenServiceProvider
        );
        console.log('✅ [JS] Resultado isTokenized:', isTokenized);

        Alert.alert(
          'Verificação de Tokenização',
          `Cartão terminado em ${fpanLastFour} está tokenizado: ${isTokenized ? 'Sim' : 'Não'}`
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao verificar se está tokenizado:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro', `Erro ao verificar tokenização: ${errorMessage}`);
      }
    };

    const handleViewToken = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando visualização de token...');

        // Obter constantes para usar os valores corretos
        const tokenServiceProvider = constants.TOKEN_PROVIDER_ELO;
        const issuerTokenId = 'test-token-id'; // ID de exemplo

        const tokenData: GoogleTokenInfo | null =
          await googleWalletClient.viewToken(
            tokenServiceProvider,
            issuerTokenId
          );
        console.log('✅ [JS] Resultado viewToken:', tokenData);

        if (tokenData) {
          // Mostrar dados completos do token
          const tokenInfo =
            `Token encontrado e Google Pay aberto!\n\n` +
            `📋 Dados do Token:\n` +
            `ID: ${tokenData.issuerTokenId}\n` +
            `Emissor: ${tokenData.issuerName}\n` +
            `FPAN: ${tokenData.fpanLastFour}\n` +
            `DPAN: ${tokenData.dpanLastFour}\n` +
            `TSP: ${tokenData.tokenServiceProvider}\n` +
            `Rede: ${tokenData.network}\n` +
            `Estado: ${tokenData.tokenState}\n` +
            `Padrão: ${tokenData.isDefaultToken ? 'Sim' : 'Não'}\n` +
            `Portfólio: ${tokenData.portfolioName}`;

          Alert.alert('✅ Token Visualizado', tokenInfo);
        } else {
          Alert.alert(
            '⚠️ Token Não Encontrado',
            'O token especificado não foi encontrado na carteira. Verifique se o ID está correto ou se o token existe.'
          );
        }
      } catch (err) {
        console.log('❌ [JS] Erro ao visualizar token:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro', `Erro ao visualizar token: ${errorMessage}`);
      }
    };

    const handleAddCard = async (opc?: string): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando processo de adição de cartão...');

        console.log('🔍 [JS] Obtendo constantes...');
        console.log('✅ [JS] Constantes obtidas:', constants);

        const cardData: GooglePushTokenizeRequest = {
          card: {
            network: constants.CARD_NETWORK_ELO,
            tokenServiceProvider: constants.TOKEN_PROVIDER_ELO,
            opaquePaymentCard: opc || opcValue,
            displayName: 'Caleb Pedro Souza',
            lastDigits: '6890',
          },
          address: {
            name: 'Caleb Pedro Souza',
            address1: 'Rua Jardineira',
            address2: '',
            locality: 'Natal',
            administrativeArea: 'RN',
            countryCode: 'BR',
            postalCode: '59139444',
            phoneNumber: '73996489673',
          },
        };

        console.log('🔍 [JS] Dados do cartão preparados:', {
          network: cardData.card.network,
          tokenServiceProvider: cardData.card.tokenServiceProvider,
          displayName: cardData.card.displayName,
          lastDigits: cardData.card.lastDigits,
          address: cardData.address,
          opaquePaymentCardLength: cardData.card.opaquePaymentCard.length,
        });

        console.log('🔍 [JS] Chamando addCardToWallet...');
        const tokenId: string =
          await googleWalletClient.addCardToWallet(cardData);
        console.log(
          '✅ [JS] Cartão adicionado com sucesso! Token ID:',
          tokenId
        );
        Alert.alert('Sucesso', `Cartão adicionado com ID: ${tokenId}`);
      } catch (err) {
        console.log('❌ [JS] Erro ao adicionar cartão:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        console.log('❌ [JS] Error details:', {
          name: err instanceof Error ? err.name : 'Unknown',
          message: err instanceof Error ? err.message : String(err),
          code: (err as any)?.code || 'N/A',
          nativeError: (err as any)?.nativeError || 'N/A',
        });

        // Usar a função de tratamento de erro personalizada
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro ao Adicionar Cartão', errorMessage);
      }
    };

    const handleCreateWallet = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando criação de carteira...');
        const walletCreated: boolean =
          await googleWalletClient.createWalletIfNeeded();
        console.log('✅ [JS] Resultado da criação de carteira:', walletCreated);

        if (walletCreated) {
          Alert.alert('Sucesso', 'Google Wallet criada com sucesso!');
        } else {
          Alert.alert('Informação', 'Google Wallet já existia.');
        }
      } catch (err) {
        console.log('❌ [JS] Erro ao criar carteira:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro', `Erro ao criar carteira: ${errorMessage}`);
      }
    };

    const handleListTokens = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando listagem de tokens...');

        // Obter constantes para usar nas descrições
        console.log('🔍 [JS] Constantes obtidas:', constants);

        const tokens: GoogleTokenInfo[] = await googleWalletClient.listTokens();
        console.log('✅ [JS] Tokens obtidos:', tokens);

        if (tokens && tokens.length > 0) {
          const tokenInfo = tokens
            .map(
              (token: GoogleTokenInfo, index: number) =>
                `${index + 1}. ID: ${token.issuerTokenId}\n` +
                `   Emissor: ${token.issuerName}\n` +
                `   FPAN: ${token.fpanLastFour}\n` +
                `   DPAN: ${token.dpanLastFour}\n` +
                `   TSP: ${token.tokenServiceProvider}\n` +
                `   Rede: ${token.network}\n` +
                `   Estado: ${token.tokenState}\n` +
                `   Padrão: ${token.isDefaultToken ? 'Sim' : 'Não'}\n` +
                `   Portfólio: ${token.portfolioName}`
            )
            .join('\n\n');

          Alert.alert(
            'Tokens na Carteira',
            `Encontrados ${tokens.length} token(s):\n\n${tokenInfo}`
          );
        } else {
          Alert.alert(
            'Tokens na Carteira',
            'Nenhum token encontrado na carteira.'
          );
        }
      } catch (err) {
        console.log('❌ [JS] Erro ao listar tokens:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro', `Erro ao listar tokens: ${errorMessage}`);
      }
    };

    const handleSetActivationResult = async (
      status: GoogleActivationStatus,
      activationCode?: string
    ): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando definição de resultado de ativação...');
        console.log(
          '🔍 [JS] Status:',
          status,
          'ActivationCode:',
          activationCode
        );

        const result = await googleWalletClient.setActivationResult(
          status,
          activationCode
        );
        console.log('✅ [JS] Resultado de ativação definido:', result);

        Alert.alert(
          'Resultado de Ativação Definido',
          `Status: ${status}\n${activationCode ? `ActivationCode: ${activationCode}` : 'Sem ActivationCode'}`
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao definir resultado de ativação:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert(
          'Erro',
          `Erro ao definir resultado de ativação: ${errorMessage}`
        );
      }
    };

    const handleFinishActivity = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando finalização da atividade...');

        const result = await googleWalletClient.finishActivity();
        console.log('✅ [JS] Atividade finalizada:', result);

        Alert.alert(
          'Atividade Finalizada',
          'A atividade foi finalizada e você voltará para o app chamador.'
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao finalizar atividade:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleGoogleWalletError(err);
        Alert.alert('Erro', `Erro ao finalizar atividade: ${errorMessage}`);
      }
    };

    const handleClearOPC = (): void => {
      setOpcValue('');
      console.log('🧹 [JS] OPC limpo');
    };

    const handlePasteOPC = async (): Promise<void> => {
      try {
        const clipboardContent = await Clipboard.getString();
        if (clipboardContent.trim()) {
          setOpcValue(clipboardContent.trim());
          console.log('📋 [JS] OPC colado da área de transferência');
          Alert.alert('Sucesso', 'OPC colado da área de transferência!');
        } else {
          Alert.alert('Aviso', 'Área de transferência está vazia');
        }
      } catch (err) {
        console.log('❌ [JS] Erro ao colar OPC:', err);
        Alert.alert('Erro', 'Erro ao acessar área de transferência');
      }
    };

    return (
      <SafeAreaView style={styles.safeArea}>
        <ScrollView
          style={styles.container}
          contentContainerStyle={{ paddingBottom: 40 }}
        >
          <Text style={styles.title}>Google Wallet - Exemplo</Text>

          {/* Componente unificado de status e resultado do intent */}
          <View style={styles.intentStatusSection}>
            <Text style={styles.intentStatusTitle}>
              Google Wallet - App 2 App
            </Text>
            <View
              style={[
                styles.intentStatusIndicator,
                intentResult
                  ? styles.intentStatusActive
                  : isCheckingPendingData
                    ? styles.intentStatusChecking
                    : styles.intentStatusInactive,
              ]}
            >
              <Text
                style={[
                  styles.intentStatusText,
                  intentResult
                    ? styles.intentStatusTextActive
                    : isCheckingPendingData
                      ? styles.intentStatusTextChecking
                      : styles.intentStatusTextInactive,
                ]}
              >
                {intentResult
                  ? '🎯 Intent Recebido'
                  : isCheckingPendingData
                    ? '🔍 Verificando Dados...'
                    : '⏳ Aguardando Intent'}
              </Text>
            </View>
            <Text style={styles.intentStatusDescription}>
              {intentResult
                ? `Último intent recebido em ${new Date().toLocaleTimeString()}`
                : isCheckingPendingData
                  ? 'Verificando se há dados pendentes da MainActivity...'
                  : 'O app está escutando por intents da carteira do Google'}
            </Text>

            {/* Seção de detalhes do intent quando disponível */}
            {intentResult && (
              <View style={styles.intentResultContent}>
                <Text style={styles.intentResultText}>
                  <Text style={styles.intentResultLabel}>Tipo:</Text>{' '}
                  {intentResult.type}
                </Text>
                <Text style={styles.intentResultText}>
                  <Text style={styles.intentResultLabel}>Ação:</Text>{' '}
                  {intentResult.action}
                </Text>
                {intentResult.callingPackage && (
                  <Text style={styles.intentResultText}>
                    <Text style={styles.intentResultLabel}>Package:</Text>{' '}
                    {intentResult.callingPackage}
                  </Text>
                )}
                {intentResult.data && (
                  <Text style={styles.intentResultText}>
                    <Text style={styles.intentResultLabel}>Dados:</Text>{' '}
                    {intentResult.data.substring(0, 50)}...
                  </Text>
                )}
                {intentResult.dataFormat && (
                  <Text style={styles.intentResultText}>
                    <Text style={styles.intentResultLabel}>Formato:</Text>{' '}
                    {intentResult.dataFormat}
                  </Text>
                )}
                {intentResult.originalData && (
                  <Text style={styles.intentResultText}>
                    <Text style={styles.intentResultLabel}>
                      Dados Originais (Base64):
                    </Text>{' '}
                    {intentResult.originalData.substring(0, 50)}...
                  </Text>
                )}
                {intentResult.error && (
                  <Text
                    style={[styles.intentResultText, styles.intentResultError]}
                  >
                    <Text style={styles.intentResultLabel}>Erro:</Text>{' '}
                    {intentResult.error}
                  </Text>
                )}
                <Text style={styles.intentResultText}>
                  <Text style={styles.intentResultLabel}>Timestamp:</Text>{' '}
                  {new Date().toLocaleString()}
                </Text>

                {/* Dados decodificados integrados */}
                {decodedData && (
                  <>
                    <View style={styles.decodedDataDivider} />
                    <Text style={styles.decodedDataTitle}>
                      📋 Dados Decodificados
                      {intentResult.dataFormat ===
                        GoogleWalletDataFormat.BASE64_DECODED &&
                        ' (Automático)'}
                      {intentResult.dataFormat === GoogleWalletDataFormat.RAW &&
                        ' (Manual)'}
                    </Text>
                    <View style={styles.decodedDataContent}>
                      <Text style={styles.decodedDataText}>
                        {typeof decodedData === 'object' && decodedData !== null
                          ? JSON.stringify(decodedData, null, 2)
                          : String(decodedData)}
                      </Text>
                    </View>
                    <TouchableOpacity
                      style={styles.copyButton}
                      onPress={() => {
                        const dataToCopy =
                          typeof decodedData === 'object'
                            ? JSON.stringify(decodedData, null, 2)
                            : String(decodedData);
                        Clipboard.setString(dataToCopy);
                        Alert.alert(
                          'Sucesso',
                          'Dados decodificados copiados para a área de transferência!'
                        );
                      }}
                    >
                      <Text style={styles.copyButtonText}>
                        📋 Copiar Dados Decodificados
                      </Text>
                    </TouchableOpacity>
                  </>
                )}

                {/* Seção para testar resultado de ativação - só aparece quando há intent result */}
                <View style={styles.activationResultSection}>
                  <View style={styles.decodedDataDivider} />
                  <Text style={styles.activationResultTitle}>
                    🎯 Definir Resultado de Ativação
                  </Text>
                  <Text style={styles.activationResultDescription}>
                    Use os botões abaixo para definir o resultado da ativação do
                    token para o Google Wallet:
                  </Text>

                  <View style={styles.opcButtonsContainer}>
                    <TouchableOpacity
                      style={[
                        styles.clearButton,
                        { backgroundColor: '#4caf50' },
                      ]}
                      onPress={() =>
                        handleSetActivationResult(
                          GoogleActivationStatus.APPROVED
                        )
                      }
                    >
                      <Text style={styles.clearButtonText}>✅ Aprovar</Text>
                    </TouchableOpacity>

                    <TouchableOpacity
                      style={[
                        styles.pasteButton,
                        { backgroundColor: '#ff9800' },
                      ]}
                      onPress={() =>
                        handleSetActivationResult(
                          GoogleActivationStatus.DECLINED
                        )
                      }
                    >
                      <Text style={styles.pasteButtonText}>❌ Recusar</Text>
                    </TouchableOpacity>
                  </View>

                  <View style={styles.opcButtonsContainer}>
                    <TouchableOpacity
                      style={[
                        styles.clearButton,
                        { backgroundColor: '#f44336' },
                      ]}
                      onPress={() =>
                        handleSetActivationResult(
                          GoogleActivationStatus.FAILURE
                        )
                      }
                    >
                      <Text style={styles.clearButtonText}>💥 Falha</Text>
                    </TouchableOpacity>

                    <TouchableOpacity
                      style={[
                        styles.pasteButton,
                        { backgroundColor: '#2196f3' },
                      ]}
                      onPress={() =>
                        handleSetActivationResult(
                          GoogleActivationStatus.APPROVED,
                          'ACTIVATION_CODE_12345'
                        )
                      }
                    >
                      <Text style={styles.pasteButtonText}>
                        ✅ Aprovar + Código
                      </Text>
                    </TouchableOpacity>
                  </View>

                  {/* Botões para finalizar atividade */}
                  <View style={styles.finishButtonsContainer}>
                    <TouchableOpacity
                      style={[
                        styles.finishButton,
                        { backgroundColor: '#9c27b0' },
                      ]}
                      onPress={handleFinishActivity}
                    >
                      <Text style={styles.finishButtonText}>
                        🚪 Finalizar e Voltar
                      </Text>
                    </TouchableOpacity>
                  </View>
                </View>
              </View>
            )}
          </View>

          {/* Seção para adicionar cartão com OPC personalizado */}
          <View style={styles.addCardSection}>
            <Text style={styles.sectionTitle}>
              Adicionar Cartão à Google Wallet
            </Text>

            <Text style={styles.inputLabel}>OPC (Opaque Payment Card):</Text>
            <TextInput
              style={styles.opcInput}
              value={opcValue}
              onChangeText={setOpcValue}
              placeholder="Cole aqui o OPC do seu cartão"
              multiline
              numberOfLines={3}
            />

            {/* Botões de ação para o OPC */}
            <View style={styles.opcButtonsContainer}>
              <TouchableOpacity
                style={styles.clearButton}
                onPress={handleClearOPC}
              >
                <Text style={styles.clearButtonText}>🧹 Limpar OPC</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.pasteButton}
                onPress={handlePasteOPC}
              >
                <Text style={styles.pasteButtonText}>📋 Colar OPC</Text>
              </TouchableOpacity>
            </View>

            <TouchableOpacity
              style={styles.googleWalletButton}
              onPress={() => handleAddCard(opcValue)}
            >
              <Image
                source={require('./../assets/br_add_to_google_wallet_add-wallet-badge.png')}
                style={styles.googleWalletBadge}
                resizeMode="contain"
              />
            </TouchableOpacity>
          </View>

          <TouchableOpacity
            style={styles.button}
            onPress={handleCheckAvailability}
          >
            <Text style={styles.buttonText}>Verificar Disponibilidade</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.button} onPress={handleCreateWallet}>
            <Text style={styles.buttonText}>Criar Google Wallet</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.button} onPress={handleGetWalletInfo}>
            <Text style={styles.buttonText}>Obter Informações da Wallet</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.button}
            onPress={handleGetTokenStatus}
          >
            <Text style={styles.buttonText}>Status do Token</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.button}
            onPress={handleGetEnvironment}
          >
            <Text style={styles.buttonText}>Obter Environment</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.button} onPress={handleIsTokenized}>
            <Text style={styles.buttonText}>Verificar Tokenização</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.button} onPress={handleViewToken}>
            <Text style={styles.buttonText}>Visualizar Token</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.button} onPress={handleListTokens}>
            <Text style={styles.buttonText}>Listar Tokens</Text>
          </TouchableOpacity>
        </ScrollView>
      </SafeAreaView>
    );
  }
);

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
    color: '#333',
  },
  button: {
    backgroundColor: '#4285F4',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 15,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  errorText: {
    color: 'red',
    fontSize: 16,
    textAlign: 'center',
    marginTop: 50,
    padding: 20,
  },
  addCardSection: {
    marginBottom: 20,
    padding: 16,
    backgroundColor: 'white',
    borderRadius: 12,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
    textAlign: 'center',
  },
  inputLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#555',
    marginBottom: 8,
  },
  opcInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
    fontSize: 14,
    backgroundColor: '#f9f9f9',
    textAlignVertical: 'top',
    marginBottom: 16,
    fontFamily: 'monospace',
    color: '#333',
  },
  googleWalletButton: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  googleWalletBadge: {
    width: 200,
    height: 60,
  },
  opcButtonsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16,
    gap: 12,
  },
  clearButton: {
    flex: 1,
    backgroundColor: '#ff6b6b',
    paddingVertical: 10,
    paddingHorizontal: 16,
    borderRadius: 8,
    alignItems: 'center',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    justifyContent: 'center',
  },
  clearButtonText: {
    color: 'white',
    fontSize: 14,
    fontWeight: '600',
  },
  pasteButton: {
    flex: 1,
    backgroundColor: '#4ecdc4',
    paddingVertical: 10,
    paddingHorizontal: 16,
    borderRadius: 8,
    alignItems: 'center',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    justifyContent: 'center',
  },
  pasteButtonText: {
    color: 'white',
    fontSize: 14,
    fontWeight: '600',
  },
  intentResultContent: {
    backgroundColor: 'white',
    padding: 12,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#c8e6c9',
    marginTop: 16,
  },
  intentResultText: {
    fontSize: 14,
    color: '#333',
    marginBottom: 6,
    lineHeight: 20,
  },
  intentResultLabel: {
    fontWeight: 'bold',
    color: '#2e7d32',
  },
  intentResultError: {
    color: '#d32f2f',
  },
  intentStatusSection: {
    marginBottom: 20,
    padding: 16,
    backgroundColor: 'white',
    borderRadius: 12,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    alignItems: 'center',
  },
  intentStatusTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
    textAlign: 'center',
  },
  intentStatusIndicator: {
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 25,
    marginBottom: 8,
    borderWidth: 2,
    minWidth: 200,
    alignItems: 'center',
  },
  intentStatusActive: {
    backgroundColor: '#e8f5e8',
    borderColor: '#4caf50',
  },
  intentStatusInactive: {
    backgroundColor: '#fff3e0',
    borderColor: '#ff9800',
  },
  intentStatusChecking: {
    backgroundColor: '#e3f2fd',
    borderColor: '#2196f3',
  },
  intentStatusText: {
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  intentStatusTextActive: {
    color: '#2e7d32',
  },
  intentStatusTextInactive: {
    color: '#f57c00',
  },
  intentStatusTextChecking: {
    color: '#1976d2',
  },
  intentStatusDescription: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    lineHeight: 20,
  },
  decodedDataDivider: {
    height: 1,
    backgroundColor: '#e0e0e0',
    marginVertical: 12,
  },
  decodedDataTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#1976d2',
    marginBottom: 8,
    textAlign: 'left',
  },
  decodedDataContent: {
    backgroundColor: '#f8f9fa',
    padding: 10,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#e0e0e0',
    marginBottom: 10,
  },
  decodedDataText: {
    fontSize: 12,
    color: '#333',
    fontFamily: 'monospace',
    lineHeight: 18,
  },
  copyButton: {
    backgroundColor: '#2196f3',
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 6,
    alignItems: 'center',
    elevation: 1,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 1,
  },
  copyButtonText: {
    color: 'white',
    fontSize: 12,
    fontWeight: '600',
  },
  activationResultSection: {
    marginTop: 16,
    padding: 12,
    backgroundColor: '#f8f9fa',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e0e0e0',
  },
  activationResultTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1976d2',
    marginBottom: 8,
    textAlign: 'center',
  },
  activationResultDescription: {
    fontSize: 12,
    color: '#666',
    textAlign: 'center',
    marginBottom: 16,
    lineHeight: 18,
  },
  finishButtonsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 12,
    gap: 12,
  },
  finishButton: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 8,
    alignItems: 'center',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  finishButtonText: {
    color: 'white',
    fontSize: 14,
    fontWeight: '600',
  },
});
