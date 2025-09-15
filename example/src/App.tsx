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
  GoogleWalletClient,
  GoogleWalletEventEmitter,
} from '@platformbuilders/wallet-bridge-react-native';
import type {
  AndroidCardData,
  WalletData,
  GoogleWalletIntentEvent,
} from '@platformbuilders/wallet-bridge-react-native';
import { useState, useEffect } from 'react';

import { SafeAreaView } from 'react-native-safe-area-context';


// Mapeamento de códigos de erro para descrições em português
const ERROR_DESCRIPTIONS: Record<string, string> = {
  // Common Status Codes
  '0': 'Operação cancelada pelo usuário',//Na doc do Google Wallet é - Operação realizada com sucesso
  '-1': 'Operação realizada com sucesso (usando cache do dispositivo)',
  '2': 'A versão instalada do Google Play Services está desatualizada. Atualize o aplicativo.',
  '3': 'O Google Play Services foi desabilitado neste dispositivo',
  '4': 'É necessário fazer login no Google para usar esta funcionalidade',
  '5': 'Conta inválida especificada. Verifique sua conta do Google',
  '6': 'É necessária uma resolução adicional para completar a operação',
  '7': 'Erro de rede. Verifique sua conexão com a internet e tente novamente',
  '8': 'Erro interno do sistema. Tente novamente em alguns instantes',
  '10': 'Aplicativo mal configurado. Entre em contato com o suporte',
  '13': 'Operação falhou sem informações detalhadas. Tente novamente',
  '14': 'Operação foi interrompida. Tente novamente',
  '15': 'Tempo limite excedido. Verifique sua conexão e tente novamente',
  '16': 'Operação foi cancelada pelo usuário',
  '17': 'API não conectada. Verifique se o Google Play Services está funcionando',
  '19': 'Erro de comunicação com o serviço. Tente novamente',
  '20': 'Conexão suspensa durante a chamada. Tente novamente',
  '21': 'Conexão expirou durante atualização. Tente novamente',
  '22': 'Conexão expirou ao tentar reconectar. Tente novamente',
  
  // Google Wallet Specific Status Codes
  '15002': 'Não há carteira ativa. Crie uma carteira primeiro',
  '15003': 'Token não encontrado na carteira ativa',
  '15004': 'Token encontrado mas em estado inválido',
  '15005': 'Falha na verificação de compatibilidade do dispositivo',
  '15009': 'API TapAndPay não disponível para este aplicativo',
};


// Função para tratar erros do Google Wallet
const handleGoogleWalletError = (error: any): string => {
  console.log('🔍 [JS] Analisando erro:', error);
  
  const errorMessage = error instanceof Error ? error.message : String(error);
  console.log('🔍 [JS] Mensagem de erro:', errorMessage);
  
  // Procurar por padrão result_code:{error_code} na string de erro
  const resultCodeMatch = errorMessage.match(/result_code:(\d+)/);
  
  if (resultCodeMatch && resultCodeMatch[1]) {
    const errorCode = resultCodeMatch[1];
    console.log('🎯 [JS] Código de erro encontrado:', errorCode);
    
    const description = ERROR_DESCRIPTIONS[errorCode];
    if (description) {
      console.log('✅ [JS] Descrição encontrada:', description);
      return `Erro ${errorCode}: ${description}`;
    } else {
      console.log('⚠️ [JS] Código de erro não mapeado:', errorCode);
      return `Erro ${errorCode}: Código de erro não reconhecido`;
    }
  }
  
  // Se não encontrar o padrão result_code, procurar por outros padrões comuns
  const statusCodeMatch = errorMessage.match(/status[_\s]*code[:\s]*(\d+)/i);
  if (statusCodeMatch && statusCodeMatch[1]) {
    const errorCode = statusCodeMatch[1];
    console.log('🎯 [JS] Status code encontrado:', errorCode);
    
    const description = ERROR_DESCRIPTIONS[errorCode];
    if (description) {
      console.log('✅ [JS] Descrição encontrada:', description);
      return `Erro ${errorCode}: ${description}`;
    }
  }
  
  // Procurar por códigos numéricos no final da mensagem
  const numericCodeMatch = errorMessage.match(/(\d{4,5})$/);
  if (numericCodeMatch && numericCodeMatch[1]) {
    const errorCode = numericCodeMatch[1];
    console.log('🎯 [JS] Código numérico encontrado:', errorCode);
    
    const description = ERROR_DESCRIPTIONS[errorCode];
    if (description) {
      console.log('✅ [JS] Descrição encontrada:', description);
      return `Erro ${errorCode}: ${description}`;
    }
  }
  
  // Se não encontrar nenhum código específico, retornar a mensagem original
  console.log('⚠️ [JS] Nenhum código de erro específico encontrado');
  return `Erro ao adicionar cartão: ${errorMessage}`;
};

export default function App() {
  
  // Estado para o OPC (Opaque Payment Card)
  const [opcValue, setOpcValue] = useState(
    'M0VGNkZENjRFMEM1MTdEOTgwOEU4N0RGMzRCNkE0M0U4QURBNUEyNjIzQjgyQzEzODZEQkZGN0JEQzM3NzI4NjQ0ODMzRDhBODlFREEwODhDREI2NkMwODM2NkQxRERCN0EzQ0U0RkZFMjJERUZFMEYwM0VCQjlBRkVGNDEzNUQxMjhFODg4NkIzMjBFREZENzk5OUMyODQ4ODRCMzNBMURCNDA0MjQwRDYxMEJDNzRFMjQzMTcwRkNBQzEzRjgzQ0Y4ODI0RTc1QkE4RENGRTY3MjRDQ0U4MEIxM0RCOUMwRjA2MkYzQkIzMjJBNjlE'
  );
  
  // Estado para o resultado do intent
  const [intentResult, setIntentResult] = useState<GoogleWalletIntentEvent | null>(null);
  
  // Estado para os dados decodificados
  const [decodedData, setDecodedData] = useState<any>(null);
  
  // Estado para indicar se está verificando dados pendentes
  const [isCheckingPendingData, setIsCheckingPendingData] = useState(false);
  
  // Instanciar o GoogleWalletClient e EventEmitter
  const googleWalletClient = new GoogleWalletClient();
  const eventEmitter = new GoogleWalletEventEmitter();

  // Função para decodificar dados base64 e mostrar resultado
  const decodeAndShowData = (data: string, eventType: string, action: string) => {
    try {
      console.log('🔍 [JS] Decodificando dados base64...');
      console.log('🔍 [JS] Dados originais (base64):', data.substring(0, 100) + '...');
      
      // Decodificar dados base64
      const decodedData = atob(data);
      console.log('🔍 [JS] Dados decodificados (string):', decodedData);
      
      // Tentar fazer parse como JSON
      let parsedData;
      try {
        parsedData = JSON.parse(decodedData);
        console.log('✅ [JS] Dados parseados como JSON:', parsedData);
      } catch (jsonError) {
        console.log('⚠️ [JS] Dados não são JSON válido, mostrando como string');
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
      Alert.alert(
        `✅ ${eventType}`,
        message,
        [
          { text: 'OK' },
          { 
            text: '📋 Copiar Dados', 
            onPress: () => {
              const dataToCopy = typeof parsedData === 'object' ? 
                JSON.stringify(parsedData, null, 2) : 
                String(parsedData);
              Clipboard.setString(dataToCopy);
              Alert.alert('Sucesso', 'Dados copiados para a área de transferência!');
            }
          }
        ]
      );
      
      return parsedData;
    } catch (error) {
      console.error('❌ [JS] Erro ao decodificar dados base64:', error);
      const errorMessage = error instanceof Error ? error.message : 'Erro desconhecido';
      
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
  const processWalletIntent = (walletEvent: GoogleWalletIntentEvent) => {
    console.log('🎯 Processando intent da carteira:', walletEvent);
    
    // Atualizar estado com o resultado do intent
    setIntentResult(walletEvent);
    
    // Processar diferentes tipos de intent e mostrar alert
    switch (walletEvent.type) {
      case 'ACTIVATE_TOKEN':
        if (walletEvent.data && walletEvent.dataFormat === 'base64') {
          decodeAndShowData(walletEvent.data, 'Token Ativado', walletEvent.action);
        } else {
          Alert.alert(
            'Token Ativado',
            `Intent de ativação de token recebido!\nAção: ${walletEvent.action}\nNota: ${walletEvent.dataNote || 'Nenhum dado base64 encontrado'}`,
            [{ text: 'OK' }]
          );
        }
        break;
        
      case 'WALLET_INTENT':
        if (walletEvent.data && walletEvent.dataFormat === 'base64') {
          decodeAndShowData(walletEvent.data, 'Intent da Carteira', walletEvent.action);
        } else {
          Alert.alert(
            'Intent da Carteira',
            `Intent relacionado à carteira recebido!\nAção: ${walletEvent.action}\nNota: ${walletEvent.dataNote || 'Nenhum dado base64 encontrado'}`,
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
  
  // Configurar listener de intent automaticamente
  useEffect(() => {
    // Ativar listener automaticamente
    const activateListener = async () => {
      try {
        setIsCheckingPendingData(true);
        await googleWalletClient.setIntentListener();
        console.log('✅ [JS] Listener de intent ativado automaticamente');
      } catch (error) {
        console.error('❌ [JS] Erro ao ativar listener:', error);
      } finally {
        setIsCheckingPendingData(false);
      }
    };
    
    activateListener();
    
    // Registrar listener para eventos de intent da carteira usando a biblioteca
    const removeListener = eventEmitter.addIntentListener((walletEvent: GoogleWalletIntentEvent) => {
      console.log('🎯 Intent recebido da carteira:', walletEvent);
      
      // Usar a função reutilizável para processar o evento
      processWalletIntent(walletEvent);
    });

    // Cleanup do listener quando o componente for desmontado
    return () => {
      removeListener();
    };
  }, []);
  
  // Verificar se o módulo está disponível
  if (!googleWalletClient) {
    return (
      <ScrollView style={styles.container}>
        <Text style={styles.errorText}>
          GoogleWalletClient não está disponível. Verifique se o módulo nativo foi instalado corretamente.
        </Text>
      </ScrollView>
    );
  }

  const handleCheckAvailability = async () => {
    try {
      console.log('🔍 [JS] Iniciando verificação de disponibilidade...');
      const isAvailable = await googleWalletClient.checkWalletAvailability();
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
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao verificar disponibilidade: ${errorMessage}`);
    }
  };

  const handleGetWalletInfo = async () => {
    try {
      console.log('🔍 [JS] Iniciando obtenção de informações da wallet...');
      const walletInfo: WalletData = await googleWalletClient.getSecureWalletInfo();
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
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter informações: ${errorMessage}`);
    }
  };

  const handleGetTokenStatus = async () => {
    try {
      console.log('🔍 [JS] Iniciando verificação de status do token...');
      
      // Obter constantes para usar o tokenServiceProvider
      const constants = googleWalletClient.getConstants();
      const tokenServiceProvider = constants.TOKEN_PROVIDER_ELO;
      const tokenReferenceId = 'test-token-id'; // ID de exemplo
      
      const tokenStatus = await googleWalletClient.getTokenStatus(tokenServiceProvider, tokenReferenceId);
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
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter status do token: ${errorMessage}`);
    }
  };

  const handleGetEnvironment = async () => {
    try {
      console.log('🔍 [JS] Iniciando obtenção do environment...');
      const environment = await googleWalletClient.getEnvironment();
      console.log('✅ [JS] Environment obtido:', environment);
      Alert.alert('Environment', `Environment: ${environment}`);
    } catch (err) {
      console.log('❌ [JS] Erro ao obter environment:', err);
      console.log(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter environment: ${errorMessage}`);
    }
  };

  const handleIsTokenized = async () => {
    try {
      console.log('🔍 [JS] Iniciando verificação se está tokenizado...');
      
      // Obter constantes para usar os valores corretos
      const constants = googleWalletClient.getConstants();
      const cardNetwork = constants.CARD_NETWORK_ELO;
      const tokenServiceProvider = constants.TOKEN_PROVIDER_ELO;
      const fpanLastFour = '6890'; // Últimos 4 dígitos de exemplo
      
      const isTokenized = await googleWalletClient.isTokenized(fpanLastFour, cardNetwork, tokenServiceProvider);
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
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao verificar tokenização: ${errorMessage}`);
    }
  };

  const handleViewToken = async () => {
    try {
      console.log('🔍 [JS] Iniciando visualização de token...');
      
      // Obter constantes para usar os valores corretos
      const constants = googleWalletClient.getConstants();
      const tokenServiceProvider = constants.TOKEN_PROVIDER_ELO;
      const issuerTokenId = 'test-token-id'; // ID de exemplo
      
      const success = await googleWalletClient.viewToken(tokenServiceProvider, issuerTokenId);
      console.log('✅ [JS] Resultado viewToken:', success);
      
      if (success) {
        Alert.alert('Sucesso', 'Google Pay foi aberto para visualizar o token!');
      } else {
        Alert.alert('Aviso', 'Não foi possível abrir o Google Pay');
      }
    } catch (err) {
      console.log('❌ [JS] Erro ao visualizar token:', err);
      console.log(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao visualizar token: ${errorMessage}`);
    }
  };

  const handleAddCard = async (opc?: string) => {
    try {
      console.log('🔍 [JS] Iniciando processo de adição de cartão...');

      console.log('🔍 [JS] Obtendo constantes...');
      const constants = googleWalletClient.getConstants();
      console.log('✅ [JS] Constantes obtidas:', constants);

      const cardData: AndroidCardData = {
        network: constants.CARD_NETWORK_ELO.toString(),
        tokenServiceProvider: constants.TOKEN_PROVIDER_ELO,
        opaquePaymentCard: opc || opcValue,
        cardHolderName: 'Caleb Pedro Souza',
        lastDigits: '6890',
        userAddress: {
          name: 'Caleb Pedro Souza',
          addressOne: 'Rua Jardineira',
          addressTwo: '',
          city: 'Natal',
          administrativeArea: 'RN',
          countryCode: 'BR',
          postalCode: '59139444',
          phoneNumber: '73996489673',
        },
      };

      console.log('🔍 [JS] Dados do cartão preparados:', {
        network: cardData.network,
        tokenServiceProvider: cardData.tokenServiceProvider,
        cardHolderName: cardData.cardHolderName,
        lastDigits: cardData.lastDigits,
        userAddress: cardData.userAddress,
        opaquePaymentCardLength: cardData.opaquePaymentCard.length,
      });

      console.log('🔍 [JS] Chamando addCardToWallet...');
      const tokenId = await googleWalletClient.addCardToWallet(cardData);
      console.log('✅ [JS] Cartão adicionado com sucesso! Token ID:', tokenId);
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

  const handleCreateWallet = async () => {
    try {
      console.log('🔍 [JS] Iniciando criação de carteira...');
      const walletCreated = await googleWalletClient.createWalletIfNeeded();
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
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao criar carteira: ${errorMessage}`);
    }
  };

  const handleListTokens = async () => {
    try {
      console.log('🔍 [JS] Iniciando listagem de tokens...');
      const tokens = await googleWalletClient.listTokens();
      console.log('✅ [JS] Tokens obtidos:', tokens);
      
      if (tokens && tokens.length > 0) {
        const tokenInfo = tokens.map((token, index) => 
          `${index + 1}. ID: ${token.issuerTokenId}\n   Últimos dígitos: ${token.lastDigits}\n   Nome: ${token.displayName}\n   Estado: ${token.tokenState}\n   Rede: ${token.network}`
        ).join('\n\n');
        
        Alert.alert(
          'Tokens na Carteira', 
          `Encontrados ${tokens.length} token(s):\n\n${tokenInfo}`
        );
      } else {
        Alert.alert('Tokens na Carteira', 'Nenhum token encontrado na carteira.');
      }
    } catch (err) {
      console.log('❌ [JS] Erro ao listar tokens:', err);
      console.log(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao listar tokens: ${errorMessage}`);
    }
  };


  const handleClearOPC = () => {
    setOpcValue('');
    console.log('🧹 [JS] OPC limpo');
  };

  const handlePasteOPC = async () => {
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
    <ScrollView style={styles.container} contentContainerStyle={{paddingBottom: 40}}>
      <Text style={styles.title}>Google Wallet - Exemplo</Text>

      {/* Componente unificado de status e resultado do intent */}
      <View style={styles.intentStatusSection}>
        <Text style={styles.intentStatusTitle}>Google Wallet - App 2 App</Text>
        <View style={[
          styles.intentStatusIndicator, 
          intentResult ? styles.intentStatusActive : 
          isCheckingPendingData ? styles.intentStatusChecking : 
          styles.intentStatusInactive
        ]}>
          <Text style={[
            styles.intentStatusText,
            intentResult ? styles.intentStatusTextActive : 
            isCheckingPendingData ? styles.intentStatusTextChecking :
            styles.intentStatusTextInactive
          ]}>
            {intentResult ? '🎯 Intent Recebido' : 
             isCheckingPendingData ? '🔍 Verificando Dados...' : 
             '⏳ Aguardando Intent'}
          </Text>
        </View>
        <Text style={styles.intentStatusDescription}>
          {intentResult 
            ? `Último intent recebido em ${new Date().toLocaleTimeString()}`
            : isCheckingPendingData
            ? 'Verificando se há dados pendentes da MainActivity...'
            : 'O app está escutando por intents da carteira do Google'
          }
        </Text>

        {/* Seção de detalhes do intent quando disponível */}
        {intentResult && (
          <View style={styles.intentResultContent}>
            <Text style={styles.intentResultText}>
              <Text style={styles.intentResultLabel}>Tipo:</Text> {intentResult.type}
            </Text>
            <Text style={styles.intentResultText}>
              <Text style={styles.intentResultLabel}>Ação:</Text> {intentResult.action}
            </Text>
            {intentResult.callingPackage && (
              <Text style={styles.intentResultText}>
                <Text style={styles.intentResultLabel}>Package:</Text> {intentResult.callingPackage}
              </Text>
            )}
            {intentResult.data && (
              <Text style={styles.intentResultText}>
                <Text style={styles.intentResultLabel}>Dados (Base64):</Text> {intentResult.data.substring(0, 50)}...
              </Text>
            )}
            {intentResult.error && (
              <Text style={[styles.intentResultText, styles.intentResultError]}>
                <Text style={styles.intentResultLabel}>Erro:</Text> {intentResult.error}
              </Text>
            )}
            <Text style={styles.intentResultText}>
              <Text style={styles.intentResultLabel}>Timestamp:</Text> {new Date().toLocaleString()}
            </Text>

            {/* Dados decodificados integrados */}
            {decodedData && (
              <>
                <View style={styles.decodedDataDivider} />
                <Text style={styles.decodedDataTitle}>📋 Dados Decodificados</Text>
                <View style={styles.decodedDataContent}>
                  <Text style={styles.decodedDataText}>
                    {typeof decodedData === 'object' && decodedData !== null
                      ? JSON.stringify(decodedData, null, 2)
                      : String(decodedData)
                    }
                  </Text>
                </View>
                <TouchableOpacity 
                  style={styles.copyButton}
                  onPress={() => {
                    const dataToCopy = typeof decodedData === 'object' ? 
                      JSON.stringify(decodedData, null, 2) : 
                      String(decodedData);
                    Clipboard.setString(dataToCopy);
                    Alert.alert('Sucesso', 'Dados decodificados copiados para a área de transferência!');
                  }}
                >
                  <Text style={styles.copyButtonText}>📋 Copiar Dados Decodificados</Text>
                </TouchableOpacity>
              </>
            )}
          </View>
        )}
      </View>

      {/* Seção para adicionar cartão com OPC personalizado */}
      <View style={styles.addCardSection}>
        <Text style={styles.sectionTitle}>Adicionar Cartão à Google Wallet</Text>
        
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
            source={require('./assets/br_add_to_google_wallet_add-wallet-badge.png')} 
            style={styles.googleWalletBadge}
            resizeMode="contain"
          />
        </TouchableOpacity>
      </View>

      <TouchableOpacity style={styles.button} onPress={handleCheckAvailability}>
        <Text style={styles.buttonText}>Verificar Disponibilidade</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleCreateWallet}>
        <Text style={styles.buttonText}>Criar Google Wallet</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleGetWalletInfo}>
        <Text style={styles.buttonText}>Obter Informações da Wallet</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleGetTokenStatus}>
        <Text style={styles.buttonText}>Status do Token</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleGetEnvironment}>
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
});
