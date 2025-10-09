import React, {
  useState,
  useEffect,
  useImperativeHandle,
  forwardRef,
} from 'react';
import {
  Text,
  TouchableOpacity,
  ScrollView,
  View,
  Alert,
  StyleSheet,
  TextInput,
  Modal,
  FlatList,
  Clipboard,
} from 'react-native';
import {
  SamsungWalletModule as SamsungWalletClient,
  SamsungActivationStatus,
  SamsungWalletDataFormat,
  SamsungWalletIntentType,
  type SamsungWalletConstants,
  type SamsungCard,
  type SamsungWalletIntentEvent,
} from '@platformbuilders/wallet-bridge-react-native';

// Função simples para mostrar erros da wallet
const handleSamsungPayError = (error: unknown): string => {
  console.log('🔍 [JS] Erro Samsung Pay:', error);
  const errorMessage = error instanceof Error ? error.message : String(error);
  return errorMessage;
};

// Interface para o useImperativeHandle
export interface SamsungPayExampleRef {
  processSamsungWalletIntent: (walletEvent: SamsungWalletIntentEvent) => void;
}

export const SamsungPayExample = forwardRef<SamsungPayExampleRef>(
  (_props, ref): React.JSX.Element => {
    // Instanciar o SamsungWalletClient
    const samsungWalletClient = SamsungWalletClient;
    const constants: SamsungWalletConstants = (
      samsungWalletClient as any
    ).getConstants() as SamsungWalletConstants;

    const [serviceId, setServiceId] = useState<string>('SERVICE_ID_DE_EXEMPLO');
    const [payload, setPayload] = useState<string>('PAYLOAD_BASE64_AQUI');
    const [issuerId, setIssuerId] = useState<string>('ISSUER_ID_EXEMPLO');
    const [tokenizationProvider, setTokenizationProvider] = useState<string>(
      constants.PROVIDER_ELO
    ); // Usar código real do provider
    const [cardType, setCardType] = useState<string>(
      constants.CARD_TYPE_CREDIT_DEBIT
    );

    // Estados para controlar os modais
    const [showProviderModal, setShowProviderModal] = useState<boolean>(false);
    const [showCardTypeModal, setShowCardTypeModal] = useState<boolean>(false);

    // Estados para controlar os dados da intent
    const [intentResult, setIntentResult] =
      useState<SamsungWalletIntentEvent | null>(null);
    const [decodedData, setDecodedData] = useState<
      Record<string, any> | string | null
    >(null);
    const [isCheckingPendingData, setIsCheckingPendingData] = useState(false);

    // Opções de providers baseadas nas constantes do Samsung Wallet
    const providerOptions = [
      { value: constants.PROVIDER_VISA, label: 'Visa' },
      { value: constants.PROVIDER_MASTERCARD, label: 'Mastercard' },
      { value: constants.PROVIDER_AMEX, label: 'American Express' },
      { value: constants.PROVIDER_DISCOVER, label: 'Discover' },
      { value: constants.PROVIDER_ELO, label: 'Elo' },
      { value: constants.PROVIDER_PLCC, label: 'Private Label Credit Card' },
      { value: constants.PROVIDER_GIFT, label: 'Gift Card' },
      { value: constants.PROVIDER_LOYALTY, label: 'Loyalty Card' },
      { value: constants.PROVIDER_PAYPAL, label: 'PayPal' },
      { value: constants.PROVIDER_GEMALTO, label: 'Gemalto' },
      { value: constants.PROVIDER_NAPAS, label: 'NAPAS' },
      { value: constants.PROVIDER_MIR, label: 'MIR' },
      { value: constants.PROVIDER_PAGOBANCOMAT, label: 'PagoBANCOMAT' },
      { value: constants.PROVIDER_VACCINE_PASS, label: 'Vaccine Pass' },
      { value: constants.PROVIDER_MADA, label: 'MADA' },
    ];

    // Opções de tipos de cartão baseadas nas constantes
    const cardTypeOptions = [
      { value: constants.CARD_TYPE_CREDIT, label: 'Crédito' },
      { value: constants.CARD_TYPE_DEBIT, label: 'Débito' },
      { value: constants.CARD_TYPE_CREDIT_DEBIT, label: 'Crédito/Débito' },
      { value: constants.CARD_TYPE_GIFT, label: 'Cartão Presente' },
      { value: constants.CARD_TYPE_LOYALTY, label: 'Fidelidade' },
      { value: constants.CARD_TYPE_TRANSIT, label: 'Trânsito' },
      { value: constants.CARD_TYPE_VACCINE_PASS, label: 'Passe de Vacinação' },
    ];

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

    // Função para processar eventos de intent da carteira Samsung (reutilizável)
    const processSamsungWalletIntent = (
      walletEvent: SamsungWalletIntentEvent
    ): void => {
      console.log('🎯 Processando intent da carteira Samsung:', walletEvent);

      // Atualizar estado com o resultado do intent
      setIntentResult(walletEvent);

      // Processar diferentes tipos de intent e mostrar alert
      switch (walletEvent.type) {
        case SamsungWalletIntentType.LAUNCH_A2A_IDV:
          if (
            walletEvent.data &&
            walletEvent.dataFormat === SamsungWalletDataFormat.BASE64_DECODED
          ) {
            // Dados já decodificados automaticamente pelo nativo
            console.log('✅ Dados já decodificados automaticamente');
            showDecodedData(
              walletEvent.data,
              'A2A IDV Samsung',
              walletEvent.action
            );
          } else if (
            walletEvent.data &&
            walletEvent.dataFormat === SamsungWalletDataFormat.RAW
          ) {
            // Dados em formato raw, tentar decodificar manualmente
            decodeAndShowData(
              walletEvent.data,
              'A2A IDV Samsung',
              walletEvent.action
            );
          } else {
            Alert.alert(
              'A2A IDV Samsung',
              `Intent de ativação Samsung recebido!\nAção: ${walletEvent.action}\nFormato: ${walletEvent.dataFormat || 'N/A'}`,
              [{ text: 'OK' }]
            );
          }
          break;

        case SamsungWalletIntentType.WALLET_INTENT:
          if (
            walletEvent.data &&
            walletEvent.dataFormat === SamsungWalletDataFormat.BASE64_DECODED
          ) {
            // Dados já decodificados automaticamente pelo nativo
            console.log('✅ Dados já decodificados automaticamente');
            showDecodedData(
              walletEvent.data,
              'Intent da Carteira Samsung',
              walletEvent.action
            );
          } else if (
            walletEvent.data &&
            walletEvent.dataFormat === SamsungWalletDataFormat.RAW
          ) {
            // Dados em formato raw, tentar decodificar manualmente
            decodeAndShowData(
              walletEvent.data,
              'Intent da Carteira Samsung',
              walletEvent.action
            );
          } else {
            Alert.alert(
              'Intent da Carteira Samsung',
              `Intent relacionado à carteira Samsung recebido!\nAção: ${walletEvent.action}\nFormato: ${walletEvent.dataFormat || 'N/A'}`,
              [{ text: 'OK' }]
            );
          }
          break;

        case SamsungWalletIntentType.INVALID_CALLER:
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
      processSamsungWalletIntent,
    }));

    // Configurar listener de intent automaticamente
    useEffect(() => {
      let isMounted = true;

      console.log(
        '🔍 [Samsung] Componente Samsung montado, configurando listener...'
      );

      // Ativar listener automaticamente
      const activateListener = async () => {
        try {
          setIsCheckingPendingData(true);
          await samsungWalletClient.setIntentListener();
          console.log(
            '✅ [Samsung] Listener de intent Samsung ativado automaticamente'
          );
        } catch (error) {
          console.error('❌ [Samsung] Erro ao ativar listener Samsung:', error);
        } finally {
          if (isMounted) {
            setIsCheckingPendingData(false);
          }
        }
      };

      activateListener();

      // Cleanup quando o componente for desmontado
      return () => {
        console.log(
          '🧹 [Samsung] Componente Samsung desmontado, removendo listener...'
        );
        isMounted = false;

        // Desativar listener nativo
        samsungWalletClient.removeIntentListener().catch((error) => {
          console.error(
            '❌ [Samsung] Erro ao remover listener nativo Samsung:',
            error
          );
        });
      };
    }, []);

    const handleInit = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando inicialização do Samsung Pay...');
        console.log('🔍 [JS] Service ID:', serviceId);

        const initialized = await samsungWalletClient.init(serviceId);
        console.log('✅ [JS] Inicialização concluída:', initialized);

        Alert.alert('Init', `Inicializado: ${initialized ? 'Sim' : 'Não'}`);
      } catch (err) {
        console.log('❌ [JS] Erro ao inicializar:', err);
        const errorMessage = handleSamsungPayError(err);
        Alert.alert('Erro', errorMessage);
      }
    };

    const handleGetStatus = async (): Promise<void> => {
      try {
        console.log(
          '🔍 [JS] Iniciando verificação de status do Samsung Pay...'
        );
        const status = await samsungWalletClient.getSamsungPayStatus();
        console.log('✅ [JS] Status obtido:', status);

        Alert.alert('Status do Samsung Pay', `Status: ${status}`);
      } catch (err) {
        console.log('❌ [JS] Erro ao obter status:', err);
        const errorMessage = handleSamsungPayError(err);
        Alert.alert('Erro', errorMessage);
      }
    };

    const handleGoToUpdatePage = (): void => {
      try {
        SamsungWalletClient.goToUpdatePage();
      } catch (err) {
        Alert.alert(
          'Erro',
          `Falha ao abrir página de atualização: ${String(err)}`
        );
      }
    };

    const handleActivateSamsungPay = (): void => {
      try {
        SamsungWalletClient.activateSamsungPay();
      } catch (err) {
        Alert.alert('Erro', `Falha ao ativar: ${String(err)}`);
      }
    };

    const handleGetAllCards = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando listagem de cartões...');
        const cards: SamsungCard[] = await samsungWalletClient.getAllCards();
        console.log('✅ [JS] Cartões obtidos:', cards);

        if (cards.length > 0) {
          const cardInfo = cards
            .map((card: SamsungCard, index: number) => {
              return (
                `${index + 1}. ${card.displayName ?? 'Sem nome'}\n` +
                `   ••••${card.last4 ?? card.last4FPan ?? ''}\n` +
                `   Brand: ${card.cardBrand}\n` +
                `   Tipo: ${card.cardType}\n` +
                `   Provedor: ${card.tokenizationProvider}\n` +
                `   Status: ${card.cardStatus}`
              );
            })
            .join('\n\n');

          Alert.alert(
            'Cartões na Carteira',
            `Encontrados ${cards.length} cartão(ões):\n\n${cardInfo}`
          );
        } else {
          Alert.alert(
            'Cartões na Carteira',
            'Nenhum cartão encontrado na carteira.'
          );
        }
      } catch (err) {
        console.log('❌ [JS] Erro ao listar cartões:', err);
        const errorMessage = handleSamsungPayError(err);
        Alert.alert('Erro', errorMessage);
      }
    };

    const handleGetWalletInfo = async (): Promise<void> => {
      try {
        const info = await SamsungWalletClient.getWalletInfo();
        Alert.alert(
          'Wallet Info',
          `walletDMId: ${info.walletDMId}\ndeviceId: ${info.deviceId}\nwalletUserId: ${info.walletUserId}`
        );
      } catch (err) {
        Alert.alert('Erro', `Falha ao obter wallet info: ${String(err)}`);
      }
    };

    const handleAddCard = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando processo de adição de cartão...');
        console.log('🔍 [JS] Dados do cartão:', {
          payload: payload.substring(0, 50) + '...',
          issuerId,
          tokenizationProvider,
          cardType,
        });

        const card = await samsungWalletClient.addCard(
          payload,
          issuerId,
          tokenizationProvider,
          cardType
        );

        console.log('✅ [JS] Cartão adicionado com sucesso:', card);

        Alert.alert(
          'Cartão Adicionado',
          `ID: ${card.cardId}\n` +
            `Brand: ${card.cardBrand}\n` +
            `Status: ${card.cardStatus}\n` +
            `Tipo: ${card.cardType || cardType}\n` +
            `Provedor: ${tokenizationProvider}`
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao adicionar cartão:', err);
        const errorMessage = handleSamsungPayError(err);
        Alert.alert('Erro', errorMessage);
      }
    };

    const handleCheckAvailability = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando verificação de disponibilidade...');
        const isAvailable = await samsungWalletClient.checkWalletAvailability();
        console.log('✅ [JS] Disponibilidade verificada:', isAvailable);

        Alert.alert(
          'Disponibilidade',
          `Samsung Pay disponível: ${isAvailable ? 'Sim' : 'Não'}`
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao verificar disponibilidade:', err);
        const errorMessage = handleSamsungPayError(err);
        Alert.alert('Erro', errorMessage);
      }
    };

    const handleShowConstants = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Obtendo constantes do Samsung Pay...');
        console.log('✅ [JS] Constantes obtidas:', constants);

        Alert.alert(
          'Constantes Samsung Pay',
          `SDK: ${constants.SDK_NAME}\n\n` +
            `📊 Constantes disponíveis:\n\n` +
            JSON.stringify(constants, null, 2)
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao obter constantes:', err);
        Alert.alert('Erro', `Falha ao obter constantes: ${String(err)}`);
      }
    };

    const handleSetActivationResult = async (
      status: SamsungActivationStatus,
      activationCode?: string
    ): Promise<void> => {
      try {
        console.log(
          '🔍 [JS] Iniciando definição de resultado de ativação Samsung...'
        );
        console.log(
          '🔍 [JS] Status:',
          status,
          'ActivationCode:',
          activationCode
        );

        const result = await samsungWalletClient.setActivationResult(
          status,
          activationCode
        );
        console.log('✅ [JS] Resultado de ativação Samsung definido:', result);

        Alert.alert(
          'Resultado de Ativação Samsung Definido',
          `Status: ${status}\n${activationCode ? `ActivationCode: ${activationCode}` : 'Sem ActivationCode'}`
        );
      } catch (err) {
        console.log(
          '❌ [JS] Erro ao definir resultado de ativação Samsung:',
          err
        );
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleSamsungPayError(err);
        Alert.alert(
          'Erro',
          `Erro ao definir resultado de ativação Samsung: ${errorMessage}`
        );
      }
    };

    const handleFinishActivity = async (): Promise<void> => {
      try {
        console.log('🔍 [JS] Iniciando finalização da atividade Samsung...');

        const result = await samsungWalletClient.finishActivity();
        console.log('✅ [JS] Atividade Samsung finalizada:', result);

        Alert.alert(
          'Atividade Samsung Finalizada',
          'A atividade foi finalizada e você voltará para o Samsung Pay.'
        );
      } catch (err) {
        console.log('❌ [JS] Erro ao finalizar atividade Samsung:', err);
        console.log(
          '❌ [JS] Stack trace:',
          err instanceof Error ? err.stack : 'N/A'
        );
        const errorMessage = handleSamsungPayError(err);
        Alert.alert(
          'Erro',
          `Erro ao finalizar atividade Samsung: ${errorMessage}`
        );
      }
    };

    // Componente do Modal de Provider
    const ProviderModal = () => (
      <Modal
        visible={showProviderModal}
        transparent={true}
        animationType="slide"
        onRequestClose={() => setShowProviderModal(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Selecionar Provedor</Text>
            <FlatList
              data={providerOptions}
              keyExtractor={(item) => item.value}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={[
                    styles.modalOption,
                    tokenizationProvider === item.value &&
                      styles.modalOptionSelected,
                  ]}
                  onPress={() => {
                    setTokenizationProvider(item.value);
                    setShowProviderModal(false);
                  }}
                >
                  <Text
                    style={[
                      styles.modalOptionText,
                      tokenizationProvider === item.value &&
                        styles.modalOptionTextSelected,
                    ]}
                  >
                    {item.label}
                  </Text>
                  <Text style={styles.modalOptionValue}>{item.value}</Text>
                </TouchableOpacity>
              )}
            />
            <TouchableOpacity
              style={styles.modalCloseButton}
              onPress={() => setShowProviderModal(false)}
            >
              <Text style={styles.modalCloseButtonText}>Cancelar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    );

    // Componente do Modal de Card Type
    const CardTypeModal = () => (
      <Modal
        visible={showCardTypeModal}
        transparent={true}
        animationType="slide"
        onRequestClose={() => setShowCardTypeModal(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Selecionar Tipo de Cartão</Text>
            <FlatList
              data={cardTypeOptions}
              keyExtractor={(item) => item.value}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={[
                    styles.modalOption,
                    cardType === item.value && styles.modalOptionSelected,
                  ]}
                  onPress={() => {
                    setCardType(item.value);
                    setShowCardTypeModal(false);
                  }}
                >
                  <Text
                    style={[
                      styles.modalOptionText,
                      cardType === item.value && styles.modalOptionTextSelected,
                    ]}
                  >
                    {item.label}
                  </Text>
                  <Text style={styles.modalOptionValue}>{item.value}</Text>
                </TouchableOpacity>
              )}
            />
            <TouchableOpacity
              style={styles.modalCloseButton}
              onPress={() => setShowCardTypeModal(false)}
            >
              <Text style={styles.modalCloseButtonText}>Cancelar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    );

    return (
      <ScrollView
        style={styles.container}
        contentContainerStyle={{ paddingBottom: 40 }}
      >
        <Text style={styles.title}>Samsung Pay - Exemplo</Text>

        {/* Componente unificado de status e resultado do intent */}
        <View style={styles.intentStatusSection}>
          <Text style={styles.intentStatusTitle}>Samsung Pay - App 2 App</Text>
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
                : 'O app está escutando por intents do Samsung Pay'}
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
                      SamsungWalletDataFormat.BASE64_DECODED && ' (Automático)'}
                    {intentResult.dataFormat === SamsungWalletDataFormat.RAW &&
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

              {/* Seção para testar resultado de ativação Samsung - só aparece quando há intent result */}
              <View style={styles.activationResultSection}>
                <View style={styles.decodedDataDivider} />
                <Text style={styles.activationResultTitle}>
                  🎯 Definir Resultado de Ativação Samsung
                </Text>
                <Text style={styles.activationResultDescription}>
                  Use os botões abaixo para definir o resultado da ativação do
                  token para o Samsung Pay:
                </Text>

                <View style={styles.opcButtonsContainer}>
                  <TouchableOpacity
                    style={[styles.clearButton, { backgroundColor: '#4caf50' }]}
                    onPress={() =>
                      handleSetActivationResult(
                        SamsungActivationStatus.ACCEPTED
                      )
                    }
                  >
                    <Text style={styles.clearButtonText}>✅ Aceitar</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={[styles.pasteButton, { backgroundColor: '#ff9800' }]}
                    onPress={() =>
                      handleSetActivationResult(
                        SamsungActivationStatus.DECLINED
                      )
                    }
                  >
                    <Text style={styles.pasteButtonText}>❌ Recusar</Text>
                  </TouchableOpacity>
                </View>

                <View style={styles.opcButtonsContainer}>
                  <TouchableOpacity
                    style={[styles.clearButton, { backgroundColor: '#f44336' }]}
                    onPress={() =>
                      handleSetActivationResult(SamsungActivationStatus.FAILURE)
                    }
                  >
                    <Text style={styles.clearButtonText}>💥 Falha</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={[styles.pasteButton, { backgroundColor: '#2196f3' }]}
                    onPress={() =>
                      handleSetActivationResult(
                        SamsungActivationStatus.ACCEPTED,
                        'ACTIVATION_CODE_12345'
                      )
                    }
                  >
                    <Text style={styles.pasteButtonText}>
                      ✅ Aceitar + Código
                    </Text>
                  </TouchableOpacity>
                </View>

                <View style={styles.opcButtonsContainer}>
                  <TouchableOpacity
                    style={[styles.clearButton, { backgroundColor: '#9e9e9e' }]}
                    onPress={() =>
                      handleSetActivationResult(
                        SamsungActivationStatus.APP_NOT_READY
                      )
                    }
                  >
                    <Text style={styles.clearButtonText}>
                      ⚠️ App Não Pronto
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

        {/* Seção inicialização */}
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Inicialização</Text>
          <Text style={styles.inputLabel}>Service ID:</Text>
          <TextInput
            style={styles.input}
            value={serviceId}
            onChangeText={setServiceId}
            placeholder="Informe o Service ID"
          />
          <TouchableOpacity style={styles.button} onPress={handleInit}>
            <Text style={styles.buttonText}>Inicializar SDK</Text>
          </TouchableOpacity>
        </View>

        {/* Seção cartão */}
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Adicionar Cartão</Text>
          <Text style={styles.inputLabel}>Payload (Base64):</Text>
          <TextInput
            style={[styles.input, styles.multiline]}
            value={payload}
            onChangeText={setPayload}
            placeholder="Cole o payload base64"
            multiline
            numberOfLines={3}
          />
          <Text style={styles.inputLabel}>Issuer ID:</Text>
          <TextInput
            style={styles.input}
            value={issuerId}
            onChangeText={setIssuerId}
            placeholder="Issuer ID"
          />
          <Text style={styles.inputLabel}>Tokenization Provider:</Text>
          <TouchableOpacity
            style={styles.selectorButton}
            onPress={() => setShowProviderModal(true)}
          >
            <Text style={styles.selectorButtonText}>
              {providerOptions.find((p) => p.value === tokenizationProvider)
                ?.label || 'Selecionar Provedor'}
            </Text>
            <Text style={styles.selectorButtonValue}>
              {tokenizationProvider}
            </Text>
          </TouchableOpacity>

          <Text style={styles.inputLabel}>Card Type:</Text>
          <TouchableOpacity
            style={styles.selectorButton}
            onPress={() => setShowCardTypeModal(true)}
          >
            <Text style={styles.selectorButtonText}>
              {cardTypeOptions.find((c) => c.value === cardType)?.label ||
                'Selecionar Tipo'}
            </Text>
            <Text style={styles.selectorButtonValue}>{cardType}</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handleAddCard}>
            <Text style={styles.buttonText}>Adicionar Cartão</Text>
          </TouchableOpacity>
        </View>

        {/* Seção consultas */}
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Consultas</Text>
          <TouchableOpacity style={styles.button} onPress={handleGetStatus}>
            <Text style={styles.buttonText}>Status do Samsung Pay</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.button}
            onPress={handleCheckAvailability}
          >
            <Text style={styles.buttonText}>Verificar Disponibilidade</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handleGetAllCards}>
            <Text style={styles.buttonText}>Listar Cartões</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handleGetWalletInfo}>
            <Text style={styles.buttonText}>Obter Wallet Info</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handleShowConstants}>
            <Text style={styles.buttonText}>Mostrar Constantes</Text>
          </TouchableOpacity>
        </View>

        {/* Seção ações */}
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Ações</Text>
          <TouchableOpacity
            style={styles.button}
            onPress={handleGoToUpdatePage}
          >
            <Text style={styles.buttonText}>Abrir Página de Atualização</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.button}
            onPress={handleActivateSamsungPay}
          >
            <Text style={styles.buttonText}>Ativar Samsung Pay</Text>
          </TouchableOpacity>
        </View>

        {/* Modais */}
        <ProviderModal />
        <CardTypeModal />
      </ScrollView>
    );
  }
);

// Estilos
const styles = StyleSheet.create({
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
  card: {
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
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
    fontSize: 14,
    backgroundColor: '#f9f9f9',
    textAlignVertical: 'top',
    marginBottom: 12,
    color: '#333',
  },
  multiline: {
    minHeight: 72,
    fontFamily: 'monospace',
  },
  button: {
    backgroundColor: '#1428a0',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 12,
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
  // Estilos para botões seletores
  selectorButton: {
    backgroundColor: '#f8f9fa',
    borderWidth: 1,
    borderColor: '#dee2e6',
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  selectorButtonText: {
    fontSize: 16,
    color: '#333',
    fontWeight: '500',
  },
  selectorButtonValue: {
    fontSize: 12,
    color: '#666',
    fontFamily: 'monospace',
  },
  // Estilos para modais
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 20,
    margin: 20,
    maxHeight: '80%',
    width: '90%',
    elevation: 5,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 4,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
    textAlign: 'center',
  },
  modalOption: {
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  modalOptionSelected: {
    backgroundColor: '#e3f2fd',
    borderRadius: 8,
    marginVertical: 2,
  },
  modalOptionText: {
    fontSize: 16,
    color: '#333',
    fontWeight: '500',
  },
  modalOptionTextSelected: {
    color: '#1976d2',
    fontWeight: 'bold',
  },
  modalOptionValue: {
    fontSize: 12,
    color: '#666',
    fontFamily: 'monospace',
  },
  modalCloseButton: {
    backgroundColor: '#6c757d',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
    marginTop: 16,
    alignSelf: 'center',
  },
  modalCloseButtonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
  // Estilos para seção de status da intent
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
  opcButtonsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
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
