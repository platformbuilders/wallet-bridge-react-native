import React, { useMemo, useState } from 'react';
import {
  Text,
  TouchableOpacity,
  ScrollView,
  View,
  Alert,
  StyleSheet,
  TextInput,
} from 'react-native';
import {
  SamsungWalletModule as SamsungWalletClient,
  type SamsungWalletConstants,
  type SamsungCard,
} from '@platformbuilders/wallet-bridge-react-native';

export function SamsungPayExample(): React.JSX.Element {
  const [serviceId, setServiceId] = useState<string>('SERVICE_ID_DE_EXEMPLO');
  const [payload, setPayload] = useState<string>('PAYLOAD_BASE64_AQUI');
  const [issuerId, setIssuerId] = useState<string>('ISSUER_ID_EXEMPLO');
  const [tokenizationProvider, setTokenizationProvider] =
    useState<string>('VISA');
  const [lastDigits, setLastDigits] = useState<string>('1234');
  const [identifier, setIdentifier] = useState<string>('IDENTIFIER_EXEMPLO');
  const [tsp, setTsp] = useState<string>('VISA');

  // Obter constantes do módulo (compatível se for função ou objeto constante)
  const constants: SamsungWalletConstants | undefined = useMemo(() => {
    try {
      const anyClient: any = SamsungWalletClient as any;
      if (anyClient && typeof anyClient.getConstants === 'function') {
        // Alguns módulos expõem via método sync/async; aqui mantemos simples (pode ser undefined)
        return undefined as unknown as SamsungWalletConstants;
      }
      if (anyClient && typeof anyClient.getConstants === 'object') {
        return anyClient.getConstants as SamsungWalletConstants;
      }
    } catch {}
    return undefined;
  }, []);

  const handleInit = async (): Promise<void> => {
    try {
      const initialized = await SamsungWalletClient.init(serviceId);
      Alert.alert('Init', `Inicializado: ${initialized ? 'Sim' : 'Não'}`);
    } catch (err) {
      Alert.alert('Erro', `Falha ao inicializar: ${String(err)}`);
    }
  };

  const handleGetStatus = async (): Promise<void> => {
    try {
      const status = await SamsungWalletClient.getSamsungPayStatus();
      Alert.alert('Status do Samsung Pay', `Código: ${status}`);
    } catch (err) {
      Alert.alert('Erro', `Falha ao obter status: ${String(err)}`);
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
      const cards: SamsungCard[] = await SamsungWalletClient.getAllCards();
      Alert.alert(
        'Cartões',
        cards.length
          ? cards
              .map(
                (c, i) =>
                  `${i + 1}. ${c.displayName ?? 'Sem nome'} ••••${c.last4 ?? c.last4FPan ?? ''} (${c.cardBrand})`
              )
              .join('\n')
          : 'Nenhum cartão encontrado'
      );
    } catch (err) {
      Alert.alert('Erro', `Falha ao listar cartões: ${String(err)}`);
    }
  };

  const handleGetWalletInfo = async (): Promise<void> => {
    try {
      const info = await SamsungWalletClient.getSecureWalletInfo();
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
      const card = await SamsungWalletClient.addCard(
        payload,
        issuerId,
        tokenizationProvider,
        // Progresso opcional
        (current: number, total: number) => {
          console.log(`[SamsungPay] Progresso: ${current}/${total}`);
        }
      );
      Alert.alert(
        'Cartão Adicionado',
        `ID: ${card.cardId}\nBrand: ${card.cardBrand}\nStatus: ${card.cardStatus}`
      );
    } catch (err) {
      Alert.alert('Erro', `Falha ao adicionar cartão: ${String(err)}`);
    }
  };

  const handleAddCardToWallet = async (): Promise<void> => {
    try {
      const card = await SamsungWalletClient.addCardToWallet({
        payload,
        issuerId,
        tokenizationProvider,
      });
      Alert.alert(
        'Cartão Adicionado (Compatibilidade)',
        `ID: ${card.cardId}\nBrand: ${card.cardBrand}\nStatus: ${card.cardStatus}`
      );
    } catch (err) {
      Alert.alert(
        'Erro',
        `Falha ao adicionar (compatibilidade): ${String(err)}`
      );
    }
  };

  const handleCheckAvailability = async (): Promise<void> => {
    try {
      const isAvailable = await SamsungWalletClient.checkWalletAvailability();
      Alert.alert(
        'Disponibilidade',
        `Samsung Pay disponível: ${isAvailable ? 'Sim' : 'Não'}`
      );
    } catch (err) {
      Alert.alert('Erro', `Falha ao verificar disponibilidade: ${String(err)}`);
    }
  };

  const handleGetCardStatusBySuffix = async (): Promise<void> => {
    try {
      const status =
        await SamsungWalletClient.getCardStatusBySuffix(lastDigits);
      Alert.alert('Status por Sufixo', `Status: ${status}`);
    } catch (err) {
      Alert.alert('Erro', `Falha ao obter status: ${String(err)}`);
    }
  };

  const handleGetCardStatusByIdentifier = async (): Promise<void> => {
    try {
      const status = await SamsungWalletClient.getCardStatusByIdentifier(
        identifier,
        tsp
      );
      Alert.alert('Status por Identificador', `Status: ${status}`);
    } catch (err) {
      Alert.alert('Erro', `Falha ao obter status: ${String(err)}`);
    }
  };

  const handleCreateWalletIfNeeded = async (): Promise<void> => {
    try {
      const created = await SamsungWalletClient.createWalletIfNeeded();
      Alert.alert(
        'Criar Wallet',
        created ? 'Criada' : 'Já existia / Não criada'
      );
    } catch (err) {
      Alert.alert('Erro', `Falha ao criar wallet: ${String(err)}`);
    }
  };

  const handleShowConstants = async (): Promise<void> => {
    try {
      let c: any = constants;
      if (!c) {
        const maybe = (SamsungWalletClient as any)?.getConstants;
        if (typeof maybe === 'function') {
          // Caso seja exposto como método
          c = await maybe.call(SamsungWalletClient);
        } else if (typeof maybe === 'object') {
          c = maybe;
        }
      }
      Alert.alert('Constantes', JSON.stringify(c ?? {}, null, 2));
    } catch (err) {
      Alert.alert('Erro', `Falha ao obter constantes: ${String(err)}`);
    }
  };

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
        <Text style={styles.inputLabel}>Tokenization Provider (ex: VISA):</Text>
        <TextInput
          style={styles.input}
          value={tokenizationProvider}
          onChangeText={setTokenizationProvider}
          placeholder="Provedor (VISA/MASTERCARD/etc)"
        />
        <TouchableOpacity style={styles.button} onPress={handleAddCard}>
          <Text style={styles.buttonText}>Adicionar (SDK)</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={handleAddCardToWallet}>
          <Text style={styles.buttonText}>Adicionar (Compatibilidade)</Text>
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
        <TouchableOpacity
          style={styles.button}
          onPress={handleCreateWalletIfNeeded}
        >
          <Text style={styles.buttonText}>Criar Wallet (se necessário)</Text>
        </TouchableOpacity>
      </View>

      {/* Seção status por identificadores */}
      <View style={styles.card}>
        <Text style={styles.sectionTitle}>Status de Cartão</Text>
        <Text style={styles.inputLabel}>Últimos dígitos (FPAN/DPAN):</Text>
        <TextInput
          style={styles.input}
          value={lastDigits}
          onChangeText={setLastDigits}
          placeholder="Ex: 1234"
          keyboardType="numeric"
        />
        <TouchableOpacity
          style={styles.button}
          onPress={handleGetCardStatusBySuffix}
        >
          <Text style={styles.buttonText}>Status por Sufixo</Text>
        </TouchableOpacity>
        <Text style={styles.inputLabel}>Identifier:</Text>
        <TextInput
          style={styles.input}
          value={identifier}
          onChangeText={setIdentifier}
          placeholder="Identifier"
        />
        <Text style={styles.inputLabel}>TSP (ex: VISA):</Text>
        <TextInput
          style={styles.input}
          value={tsp}
          onChangeText={setTsp}
          placeholder="TSP"
        />
        <TouchableOpacity
          style={styles.button}
          onPress={handleGetCardStatusByIdentifier}
        >
          <Text style={styles.buttonText}>Status por Identificador</Text>
        </TouchableOpacity>
      </View>
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
});
