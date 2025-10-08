import React, { useState } from 'react';
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
} from 'react-native';
import {
  SamsungWalletModule as SamsungWalletClient,
  type SamsungWalletConstants,
  type SamsungCard,
} from '@platformbuilders/wallet-bridge-react-native';

// Funções para mapear valores para descrições legíveis
const getSamsungPayStatusDescription = (
  status: number,
  constants: SamsungWalletConstants
): string => {
  const STATUS_DESCRIPTIONS = {
    [constants.SPAY_READY]: 'Pronto para uso',
    [constants.SPAY_NOT_READY]: 'Não está pronto',
    [constants.SPAY_NOT_SUPPORTED]: 'Não suportado',
    [constants.SPAY_NOT_ALLOWED_TEMPORALLY]: 'Não permitido temporariamente',
    [constants.SPAY_HAS_TRANSIT_CARD]: 'Tem cartão de trânsito',
    [constants.SPAY_HAS_NO_TRANSIT_CARD]: 'Não tem cartão de trânsito',
  } as const;

  return STATUS_DESCRIPTIONS[status] ?? `Status Desconhecido (${status})`;
};

const getSamsungCardTypeDescription = (
  cardType: string,
  constants: SamsungWalletConstants
): string => {
  const CARD_TYPE_DESCRIPTIONS = {
    [constants.CARD_TYPE_CREDIT_DEBIT]: 'Pagamento (Crédito/Débito)',
    [constants.CARD_TYPE_GIFT]: 'Cartão Presente',
    [constants.CARD_TYPE_LOYALTY]: 'Fidelidade',
    [constants.CARD_TYPE_CREDIT]: 'Crédito',
    [constants.CARD_TYPE_DEBIT]: 'Débito',
    [constants.CARD_TYPE_TRANSIT]: 'Trânsito',
    [constants.CARD_TYPE_VACCINE_PASS]: 'Passe de Vacinação',
  } as const;

  return CARD_TYPE_DESCRIPTIONS[cardType] ?? `Tipo Desconhecido (${cardType})`;
};

const getSamsungProviderDescription = (
  provider: string,
  constants: SamsungWalletConstants
): string => {
  const PROVIDER_DESCRIPTIONS = {
    [constants.PROVIDER_VISA]: 'Visa',
    [constants.PROVIDER_MASTERCARD]: 'Mastercard',
    [constants.PROVIDER_AMEX]: 'American Express',
    [constants.PROVIDER_DISCOVER]: 'Discover',
    [constants.PROVIDER_ELO]: 'Elo',
    [constants.PROVIDER_MADA]: 'Mada',
    [constants.PROVIDER_PAGOBANCOMAT]: 'PagoBancomat',
    [constants.PROVIDER_PAYPAL]: 'PayPal',
    [constants.PROVIDER_GEMALTO]: 'Gemalto',
    [constants.PROVIDER_NAPAS]: 'Napas',
    [constants.PROVIDER_MIR]: 'Mir',
    [constants.PROVIDER_VACCINE_PASS]: 'Passe de Vacinação',
    [constants.PROVIDER_PLCC]: 'PLCC',
    [constants.PROVIDER_GIFT]: 'Gift',
    [constants.PROVIDER_LOYALTY]: 'Loyalty',
  } as const;

  return (
    PROVIDER_DESCRIPTIONS[provider] ?? `Provedor Desconhecido (${provider})`
  );
};

const getSamsungCardStateDescription = (
  cardState: string,
  constants: SamsungWalletConstants
): string => {
  const CARD_STATE_DESCRIPTIONS = {
    [constants.ACTIVE]: 'Ativo',
    [constants.DISPOSED]: 'Descartado',
    [constants.EXPIRED]: 'Expirado',
    [constants.PENDING_ENROLLED]: 'Inscrito Pendente',
    [constants.PENDING_PROVISION]: 'Provisionamento Pendente',
    [constants.SUSPENDED]: 'Suspenso',
    [constants.PENDING_ACTIVATION]: 'Ativação Pendente',
  } as const;

  return (
    CARD_STATE_DESCRIPTIONS[cardState] ?? `Estado Desconhecido (${cardState})`
  );
};

const getSamsungErrorDescription = (
  errorCode: number,
  constants: SamsungWalletConstants
): string => {
  const ERROR_DESCRIPTIONS = {
    [constants.ERROR_NONE]: 'Nenhum erro',
    [constants.ERROR_SPAY_INTERNAL]: 'Erro interno do Samsung Pay',
    [constants.ERROR_INVALID_INPUT]: 'Entrada inválida',
    [constants.ERROR_NOT_SUPPORTED]: 'Não suportado',
    [constants.ERROR_NOT_FOUND]: 'Não encontrado',
    [constants.ERROR_ALREADY_DONE]: 'Já foi feito',
    [constants.ERROR_NOT_ALLOWED]: 'Não permitido',
    [constants.ERROR_USER_CANCELED]: 'Cancelado pelo usuário',
    [constants.ERROR_PARTNER_SDK_API_LEVEL]:
      'Nível de API do parceiro inválido',
    [constants.ERROR_PARTNER_SERVICE_TYPE]:
      'Tipo de serviço do parceiro inválido',
    [constants.ERROR_INVALID_PARAMETER]: 'Parâmetro inválido',
    [constants.ERROR_NO_NETWORK]: 'Sem conexão de rede',
    [constants.ERROR_SERVER_NO_RESPONSE]: 'Servidor sem resposta',
    [constants.ERROR_PARTNER_INFO_INVALID]: 'Informações do parceiro inválidas',
    [constants.ERROR_INITIATION_FAIL]: 'Falha na inicialização',
    [constants.ERROR_REGISTRATION_FAIL]: 'Falha no registro',
    [constants.ERROR_DUPLICATED_SDK_API_CALLED]: 'API do SDK chamada duplicada',
    [constants.ERROR_SDK_NOT_SUPPORTED_FOR_THIS_REGION]:
      'SDK não suportado para esta região',
    [constants.ERROR_SERVICE_ID_INVALID]: 'ID do serviço inválido',
    [constants.ERROR_SERVICE_UNAVAILABLE_FOR_THIS_REGION]:
      'Serviço indisponível para esta região',
    [constants.ERROR_PARTNER_APP_SIGNATURE_MISMATCH]:
      'Assinatura do app do parceiro não confere',
    [constants.ERROR_PARTNER_APP_VERSION_NOT_SUPPORTED]:
      'Versão do app do parceiro não suportada',
    [constants.ERROR_PARTNER_APP_BLOCKED]: 'App do parceiro bloqueado',
    [constants.ERROR_USER_NOT_REGISTERED_FOR_DEBUG]:
      'Usuário não registrado para debug',
    [constants.ERROR_SERVICE_NOT_APPROVED_FOR_RELEASE]:
      'Serviço não aprovado para release',
    [constants.ERROR_PARTNER_NOT_APPROVED]: 'Parceiro não aprovado',
    [constants.ERROR_UNAUTHORIZED_REQUEST_TYPE]:
      'Tipo de requisição não autorizado',
    [constants.ERROR_EXPIRED_OR_INVALID_DEBUG_KEY]:
      'Chave de debug expirada ou inválida',
    [constants.ERROR_SERVER_INTERNAL]: 'Erro interno do servidor',
    [constants.ERROR_DEVICE_NOT_SAMSUNG]: 'Dispositivo não é Samsung',
    [constants.ERROR_SPAY_PKG_NOT_FOUND]: 'Pacote Samsung Pay não encontrado',
    [constants.ERROR_SPAY_SDK_SERVICE_NOT_AVAILABLE]:
      'Serviço SDK Samsung Pay não disponível',
    [constants.ERROR_DEVICE_INTEGRITY_CHECK_FAIL]:
      'Falha na verificação de integridade do dispositivo',
    [constants.ERROR_SPAY_APP_INTEGRITY_CHECK_FAIL]:
      'Falha na verificação de integridade do app Samsung Pay',
    [constants.ERROR_ANDROID_PLATFORM_CHECK_FAIL]:
      'Falha na verificação da plataforma Android',
    [constants.ERROR_MISSING_INFORMATION]: 'Informações em falta',
    [constants.ERROR_SPAY_SETUP_NOT_COMPLETED]:
      'Configuração do Samsung Pay não concluída',
    [constants.ERROR_SPAY_APP_NEED_TO_UPDATE]:
      'App Samsung Pay precisa ser atualizado',
    [constants.ERROR_PARTNER_SDK_VERSION_NOT_ALLOWED]:
      'Versão do SDK do parceiro não permitida',
    [constants.ERROR_UNABLE_TO_VERIFY_CALLER]:
      'Não foi possível verificar o chamador',
    [constants.ERROR_SPAY_FMM_LOCK]: 'Samsung Pay bloqueado pelo FMM',
    [constants.ERROR_SPAY_CONNECTED_WITH_EXTERNAL_DISPLAY]:
      'Samsung Pay conectado com display externo',
  } as const;

  return ERROR_DESCRIPTIONS[errorCode] ?? `Erro Desconhecido (${errorCode})`;
};

// Função para tratar erros do Samsung Pay
const handleSamsungPayError = (
  error: unknown,
  constants: SamsungWalletConstants
): string => {
  console.log('🔍 [JS] Analisando erro Samsung Pay:', error);

  const errorMessage = error instanceof Error ? error.message : String(error);
  console.log('🔍 [JS] Mensagem de erro:', errorMessage);

  // Procurar por códigos de erro numéricos na string de erro
  const errorCodeMatch = errorMessage.match(/(\d+)/);
  if (errorCodeMatch && errorCodeMatch[1]) {
    const errorCode = parseInt(errorCodeMatch[1]);
    console.log('🎯 [JS] Código de erro encontrado:', errorCode);

    const description = getSamsungErrorDescription(errorCode, constants);
    if (description) {
      console.log('✅ [JS] Descrição encontrada:', description);
      return `Erro ${errorCode}: ${description}`;
    }
  }

  // Se não encontrar código específico, retornar a mensagem original
  console.log('⚠️ [JS] Nenhum código de erro específico encontrado');
  return `Erro Samsung Pay: ${errorMessage}`;
};

export function SamsungPayExample(): React.JSX.Element {
  // Instanciar o SamsungWalletClient e obter constantes
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

  const handleInit = async (): Promise<void> => {
    try {
      console.log('🔍 [JS] Iniciando inicialização do Samsung Pay...');
      console.log('🔍 [JS] Service ID:', serviceId);

      const initialized = await samsungWalletClient.init(serviceId);
      console.log('✅ [JS] Inicialização concluída:', initialized);

      Alert.alert('Init', `Inicializado: ${initialized ? 'Sim' : 'Não'}`);
    } catch (err) {
      console.log('❌ [JS] Erro ao inicializar:', err);
      const errorMessage = handleSamsungPayError(err, constants);
      Alert.alert('Erro', errorMessage);
    }
  };

  const handleGetStatus = async (): Promise<void> => {
    try {
      console.log('🔍 [JS] Iniciando verificação de status do Samsung Pay...');
      const status = await samsungWalletClient.getSamsungPayStatus();
      console.log('✅ [JS] Status obtido:', status);

      const statusDescription = getSamsungPayStatusDescription(
        status,
        constants
      );
      Alert.alert(
        'Status do Samsung Pay',
        `Código: ${status}\nDescrição: ${statusDescription}`
      );
    } catch (err) {
      console.log('❌ [JS] Erro ao obter status:', err);
      const errorMessage = handleSamsungPayError(err, constants);
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
            const cardTypeDesc = card.cardType
              ? getSamsungCardTypeDescription(card.cardType, constants)
              : 'Desconhecido';
            const providerDesc = card.tokenizationProvider
              ? getSamsungProviderDescription(
                  String(card.tokenizationProvider),
                  constants
                )
              : 'Desconhecido';
            const cardStateDesc = card.cardStatus
              ? getSamsungCardStateDescription(card.cardStatus, constants)
              : 'Desconhecido';

            return (
              `${index + 1}. ${card.displayName ?? 'Sem nome'}\n` +
              `   ••••${card.last4 ?? card.last4FPan ?? ''}\n` +
              `   Brand: ${card.cardBrand}\n` +
              `   Tipo: ${cardTypeDesc}\n` +
              `   Provedor: ${providerDesc}\n` +
              `   Status: ${cardStateDesc} (${card.cardStatus})`
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
      const errorMessage = handleSamsungPayError(err, constants);
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
        cardType,
        // Progresso opcional
        (current: number, total: number) => {
          console.log(`[SamsungPay] Progresso: ${current}/${total}`);
        }
      );

      console.log('✅ [JS] Cartão adicionado com sucesso:', card);

      const cardTypeDesc = getSamsungCardTypeDescription(
        card.cardType || cardType,
        constants
      );
      const providerDesc = getSamsungProviderDescription(
        tokenizationProvider,
        constants
      );

      Alert.alert(
        'Cartão Adicionado',
        `ID: ${card.cardId}\n` +
          `Brand: ${card.cardBrand}\n` +
          `Status: ${card.cardStatus}\n` +
          `Tipo: ${cardTypeDesc}\n` +
          `Provedor: ${providerDesc}`
      );
    } catch (err) {
      console.log('❌ [JS] Erro ao adicionar cartão:', err);
      const errorMessage = handleSamsungPayError(err, constants);
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
      const errorMessage = handleSamsungPayError(err, constants);
      Alert.alert('Erro', errorMessage);
    }
  };

  const handleShowConstants = async (): Promise<void> => {
    try {
      console.log('🔍 [JS] Obtendo constantes do Samsung Pay...');
      console.log('✅ [JS] Constantes obtidas:', constants);

      // Organizar constantes por categoria
      const organizedConstants = {
        'Status Codes': {
          SPAY_READY: constants.SPAY_READY,
          SPAY_NOT_READY: constants.SPAY_NOT_READY,
          SPAY_NOT_SUPPORTED: constants.SPAY_NOT_SUPPORTED,
          SPAY_NOT_ALLOWED_TEMPORALLY: constants.SPAY_NOT_ALLOWED_TEMPORALLY,
          SPAY_HAS_TRANSIT_CARD: constants.SPAY_HAS_TRANSIT_CARD,
          SPAY_HAS_NO_TRANSIT_CARD: constants.SPAY_HAS_NO_TRANSIT_CARD,
        },
        'Card Types': {
          CARD_TYPE_CREDIT_DEBIT: constants.CARD_TYPE_CREDIT_DEBIT,
          CARD_TYPE_GIFT: constants.CARD_TYPE_GIFT,
          CARD_TYPE_LOYALTY: constants.CARD_TYPE_LOYALTY,
          CARD_TYPE_CREDIT: constants.CARD_TYPE_CREDIT,
          CARD_TYPE_DEBIT: constants.CARD_TYPE_DEBIT,
          CARD_TYPE_TRANSIT: constants.CARD_TYPE_TRANSIT,
          CARD_TYPE_VACCINE_PASS: constants.CARD_TYPE_VACCINE_PASS,
        },
        'Card States': {
          ACTIVE: constants.ACTIVE,
          DISPOSED: constants.DISPOSED,
          EXPIRED: constants.EXPIRED,
          PENDING_ENROLLED: constants.PENDING_ENROLLED,
          PENDING_PROVISION: constants.PENDING_PROVISION,
          SUSPENDED: constants.SUSPENDED,
          PENDING_ACTIVATION: constants.PENDING_ACTIVATION,
        },
        'Tokenization Providers': {
          PROVIDER_VISA: constants.PROVIDER_VISA,
          PROVIDER_MASTERCARD: constants.PROVIDER_MASTERCARD,
          PROVIDER_AMEX: constants.PROVIDER_AMEX,
          PROVIDER_DISCOVER: constants.PROVIDER_DISCOVER,
          PROVIDER_ELO: constants.PROVIDER_ELO,
          PROVIDER_MADA: constants.PROVIDER_MADA,
          PROVIDER_PAGOBANCOMAT: constants.PROVIDER_PAGOBANCOMAT,
          PROVIDER_PAYPAL: constants.PROVIDER_PAYPAL,
        },
        'Error Codes (Sample)': {
          ERROR_NONE: constants.ERROR_NONE,
          ERROR_SPAY_INTERNAL: constants.ERROR_SPAY_INTERNAL,
          ERROR_INVALID_INPUT: constants.ERROR_INVALID_INPUT,
          ERROR_NOT_SUPPORTED: constants.ERROR_NOT_SUPPORTED,
          ERROR_USER_CANCELED: constants.ERROR_USER_CANCELED,
          ERROR_DEVICE_NOT_SAMSUNG: constants.ERROR_DEVICE_NOT_SAMSUNG,
        },
      };

      Alert.alert(
        'Constantes Samsung Pay',
        `SDK: ${constants.SDK_NAME}\nMock: ${constants.useMock ? 'Sim' : 'Não'}\n\n` +
          `📊 Constantes organizadas por categoria:\n\n` +
          JSON.stringify(organizedConstants, null, 2)
      );
    } catch (err) {
      console.log('❌ [JS] Erro ao obter constantes:', err);
      Alert.alert('Erro', `Falha ao obter constantes: ${String(err)}`);
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
          <Text style={styles.selectorButtonValue}>{tokenizationProvider}</Text>
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
        <TouchableOpacity style={styles.button} onPress={handleGoToUpdatePage}>
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
});
