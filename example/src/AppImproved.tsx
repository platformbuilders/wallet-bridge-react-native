import  { useState, useEffect } from 'react';
import {
  Text,
  View,
  StyleSheet,
  TouchableOpacity,
  Alert,
  ScrollView,
  ActivityIndicator,
  TextInput,
} from 'react-native';
import {
  checkWalletAvailability,
  getSecureWalletInfo,
  getCardStatusBySuffix,
  getCardStatusByIdentifier,
  addCardToWallet,
  getAvailableWallets,
  switchWallet,
  createWalletIfNeeded,
} from 'react-native-builders-wallet';
import type {
  AndroidCardData,
  WalletData,
  CardStatus,
} from 'react-native-builders-wallet';

interface WalletState {
  isAvailable: boolean | null;
  walletInfo: WalletData | null;
  availableWallets: string[];
  currentModule: string;
  isLoading: boolean;
}

export default function AppImproved() {
  const [walletState, setWalletState] = useState<WalletState>({
    isAvailable: null,
    walletInfo: null,
    availableWallets: [],
    currentModule: '',
    isLoading: false,
  });

  const [cardData, setCardData] = useState({
    lastDigits: '6890',
    identifier: 'test123',
    tsp: 14,
    network: 'ELO',
    cardHolderName: 'Caleb Pedro Souza',
    opaquePaymentCard:
      'MUNEQzlBQUFBODM2MjRFQTg0RTBCNEVEMkU4NjI2MEQ5OEU3NEIyODA5NTJCMjMzMTlEODYyNzkxQzUwRjQ2RTM0NERFMjAxMDUzOUU2ODQxNjAxNzQxNjI4QTk1NEU2MDgwOTA0Mjc0NjBFNkFCN0ZDM0MxMzc3OTUwOEI2RDlBMDc4RDQ5NzJDQ0YyMjk0MDRDNEMzMzRCQzc1NDc0N0E2MzNCNTFEMDM2RjdFMjJCQjQxOEFDMzkwRTNGODVERDQxRjNGM0ZDRjVEQTA4ODBEQzJFMEVBNjc1MjA0MjVEQzJBQ0MyMUREQTExMzRDRjdGNkU0MEJDRUEzNEVDMg==',
  });

  const [addressData, _setAddressData] = useState({
    name: 'Caleb Pedro Souza',
    addressOne: 'Rua Jardineira',
    addressTwo: '',
    city: 'Natal',
    administrativeArea: 'RN',
    countryCode: 'BR',
    postalCode: '59139444',
    phoneNumber: '73996489673',
  });

  useEffect(() => {
    initializeWallet();
  }, []);

  const initializeWallet = async () => {
    setWalletState((prev) => ({ ...prev, isLoading: true }));
    try {
      const [isAvailable, wallets] = await Promise.all([
        checkWalletAvailability(),
        getAvailableWallets(),
      ]);

      setWalletState((prev) => ({
        ...prev,
        isAvailable,
        availableWallets: wallets.modules,
        currentModule: wallets.currentModule,
        isLoading: false,
      }));
    } catch (error) {
      console.error('Erro ao inicializar wallet:', error);
      setWalletState((prev) => ({ ...prev, isLoading: false }));
    }
  };

  const handleCheckAvailability = async () => {
    try {
      const isAvailable = await checkWalletAvailability();
      setWalletState((prev) => ({ ...prev, isAvailable }));
      Alert.alert(
        'Disponibilidade',
        `Wallet disponível: ${isAvailable ? 'Sim' : 'Não'}`
      );
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao verificar disponibilidade: ${errorMessage}`);
    }
  };

  const handleGetWalletInfo = async () => {
    try {
      const walletInfo: WalletData = await getSecureWalletInfo();
      setWalletState((prev) => ({ ...prev, walletInfo }));
      Alert.alert(
        'Informações da Wallet',
        `Device ID: ${walletInfo.deviceID}\nWallet Account ID: ${walletInfo.walletAccountID}`
      );
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter informações: ${errorMessage}`);
    }
  };

  const handleGetCardStatusBySuffix = async () => {
    try {
      const status: CardStatus = await getCardStatusBySuffix(
        cardData.lastDigits
      );
      Alert.alert('Status do Cartão (Suffix)', `Status: ${status}`);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter status: ${errorMessage}`);
    }
  };

  const handleGetCardStatusByIdentifier = async () => {
    try {
      const status: CardStatus = await getCardStatusByIdentifier(
        cardData.identifier,
        cardData.tsp
      );
      Alert.alert('Status do Cartão (Identifier)', `Status: ${status}`);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter status: ${errorMessage}`);
    }
  };

  const handleAddCard = async () => {
    try {
      const androidCardData: AndroidCardData = {
        network: cardData.network,
        tokenServiceProvider: cardData.tsp,
        opaquePaymentCard: cardData.opaquePaymentCard,
        cardHolderName: cardData.cardHolderName,
        lastDigits: cardData.lastDigits,
        userAddress: addressData,
      };

      const tokenId = await addCardToWallet(androidCardData);
      Alert.alert('Sucesso', `Cartão adicionado com ID: ${tokenId}`);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao adicionar cartão: ${errorMessage}`);
    }
  };

  const handleRefreshWallets = async () => {
    try {
      const wallets = await getAvailableWallets();
      setWalletState((prev) => ({
        ...prev,
        availableWallets: wallets.modules,
        currentModule: wallets.currentModule,
      }));
      Alert.alert(
        'Wallets Atualizadas',
        `Módulos: ${wallets.modules.join(', ')}\n` +
          `Atual: ${wallets.currentModule}`
      );
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter wallets: ${errorMessage}`);
    }
  };

  const handleSwitchWallet = async (walletType: string) => {
    try {
      const result = await switchWallet(walletType);
      setWalletState((prev) => ({ ...prev, currentModule: walletType }));
      Alert.alert('Sucesso', result);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao trocar wallet: ${errorMessage}`);
    }
  };

  const handleCreateWallet = async () => {
    try {
      console.log('🔍 [JS] Iniciando criação de carteira...');
      const walletCreated = await createWalletIfNeeded();
      console.log('✅ [JS] Resultado da criação de carteira:', walletCreated);
      
      if (walletCreated) {
        Alert.alert('Sucesso', 'Carteira criada com sucesso!');
        // Refresh wallet info after creation
        handleGetWalletInfo();
      } else {
        Alert.alert('Informação', 'Carteira já existia.');
      }
    } catch (err) {
      console.error('❌ [JS] Erro ao criar carteira:', err);
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao criar carteira: ${errorMessage}`);
    }
  };

  if (walletState.isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#4285F4" />
        <Text style={styles.loadingText}>Inicializando Wallet...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Builders Wallet - Interface Avançada</Text>

      {/* Status da Wallet */}
      <View style={styles.statusContainer}>
        <Text style={styles.statusTitle}>Status da Wallet</Text>
        <Text style={styles.statusText}>
          Disponível: {walletState.isAvailable ? '✅ Sim' : '❌ Não'}
        </Text>
        <Text style={styles.statusText}>
          Módulo Atual: {walletState.currentModule || 'Nenhum'}
        </Text>
        <Text style={styles.statusText}>
          Wallets Disponíveis:{' '}
          {walletState.availableWallets.join(', ') || 'Nenhuma'}
        </Text>
      </View>

      {/* Ações Básicas */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Ações Básicas</Text>

        <TouchableOpacity
          style={styles.button}
          onPress={handleCheckAvailability}
        >
          <Text style={styles.buttonText}>Verificar Disponibilidade</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.button} onPress={handleGetWalletInfo}>
          <Text style={styles.buttonText}>Obter Informações da Wallet</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.button} onPress={handleCreateWallet}>
          <Text style={styles.buttonText}>Criar Carteira</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.button} onPress={handleRefreshWallets}>
          <Text style={styles.buttonText}>Atualizar Wallets</Text>
        </TouchableOpacity>
      </View>

      {/* Gerenciamento de Cartões */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Gerenciamento de Cartões</Text>

        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>Últimos 4 dígitos:</Text>
          <TextInput
            style={styles.input}
            value={cardData.lastDigits}
            onChangeText={(text) =>
              setCardData((prev) => ({ ...prev, lastDigits: text }))
            }
            placeholder="6890"
          />
        </View>

        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>Identificador:</Text>
          <TextInput
            style={styles.input}
            value={cardData.identifier}
            onChangeText={(text) =>
              setCardData((prev) => ({ ...prev, identifier: text }))
            }
            placeholder="test123"
          />
        </View>

        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>TSP:</Text>
          <TextInput
            style={styles.input}
            value={cardData.tsp.toString()}
            onChangeText={(text) =>
              setCardData((prev) => ({
                ...prev,
                tsp: parseInt(text, 10) || 14,
              }))
            }
            placeholder="14"
            keyboardType="numeric"
          />
        </View>

        <TouchableOpacity
          style={styles.button}
          onPress={handleGetCardStatusBySuffix}
        >
          <Text style={styles.buttonText}>Status por Suffix</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.button}
          onPress={handleGetCardStatusByIdentifier}
        >
          <Text style={styles.buttonText}>Status por Identifier</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, styles.primaryButton]}
          onPress={handleAddCard}
        >
          <Text style={styles.buttonText}>Adicionar Cartão</Text>
        </TouchableOpacity>
      </View>

      {/* Troca de Wallets */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Trocar Wallet</Text>

        {walletState.availableWallets.map((wallet) => (
          <TouchableOpacity
            key={wallet}
            style={[
              styles.button,
              walletState.currentModule === wallet
                ? styles.activeButton
                : styles.inactiveButton,
            ]}
            onPress={() => handleSwitchWallet(wallet.toLowerCase())}
          >
            <Text style={styles.buttonText}>
              {wallet} {walletState.currentModule === wallet ? '(Atual)' : ''}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Informações da Wallet */}
      {walletState.walletInfo && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Informações Detalhadas</Text>
          <View style={styles.infoContainer}>
            <Text style={styles.infoText}>
              Device ID: {walletState.walletInfo.deviceID}
            </Text>
            <Text style={styles.infoText}>
              Wallet Account ID: {walletState.walletInfo.walletAccountID}
            </Text>
          </View>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 10,
    fontSize: 16,
    color: '#666',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 30,
    color: '#333',
  },
  section: {
    marginBottom: 25,
    backgroundColor: 'white',
    padding: 15,
    borderRadius: 10,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 15,
    color: '#333',
  },
  statusContainer: {
    backgroundColor: '#e3f2fd',
    padding: 15,
    borderRadius: 10,
    marginBottom: 20,
  },
  statusTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#1976d2',
  },
  statusText: {
    fontSize: 14,
    marginBottom: 5,
    color: '#333',
  },
  inputContainer: {
    marginBottom: 15,
  },
  inputLabel: {
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 5,
    color: '#333',
  },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 10,
    fontSize: 16,
    backgroundColor: 'white',
  },
  button: {
    backgroundColor: '#4285F4',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 10,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  primaryButton: {
    backgroundColor: '#34a853',
  },
  activeButton: {
    backgroundColor: '#34a853',
  },
  inactiveButton: {
    backgroundColor: '#9aa0a6',
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  infoContainer: {
    backgroundColor: '#f8f9fa',
    padding: 10,
    borderRadius: 8,
  },
  infoText: {
    fontSize: 14,
    marginBottom: 5,
    color: '#333',
  },
});
