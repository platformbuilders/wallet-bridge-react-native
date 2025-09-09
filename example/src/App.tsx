import {
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
  ScrollView,
} from 'react-native';
import {
  checkWalletAvailability,
  getSecureWalletInfo,
  getCardStatusBySuffix,
  addCardToWallet,
  getAvailableWallets,
  switchWallet,
  getConstants,
  createWalletIfNeeded,
} from 'react-native-builders-wallet';
import type {
  AndroidCardData,
  WalletData,
  CardStatus,
} from 'react-native-builders-wallet';

export default function App() {
  const handleCheckAvailability = async () => {
    try {
      console.log('🔍 [JS] Iniciando verificação de disponibilidade...');
      const isAvailable = await checkWalletAvailability();
      console.log('✅ [JS] Disponibilidade verificada:', isAvailable);
      Alert.alert(
        'Disponibilidade',
        `Wallet disponível: ${isAvailable ? 'Sim' : 'Não'}`
      );
    } catch (err) {
      console.error('❌ [JS] Erro ao verificar disponibilidade:', err);
      console.error(
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
      const walletInfo: WalletData = await getSecureWalletInfo();
      console.log('✅ [JS] Informações da wallet obtidas:', walletInfo);
      Alert.alert(
        'Informações da Wallet',
        `Device ID: ${walletInfo.deviceID}\nWallet Account ID: ${walletInfo.walletAccountID}`
      );
    } catch (err) {
      console.error('❌ [JS] Erro ao obter informações da wallet:', err);
      console.error(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter informações: ${errorMessage}`);
    }
  };

  const handleGetCardStatus = async () => {
    try {
      console.log('🔍 [JS] Iniciando verificação de status do cartão...');
      const status: CardStatus = await getCardStatusBySuffix('6890');
      console.log('✅ [JS] Status do cartão obtido:', status);
      Alert.alert('Status do Cartão', `Status: ${status}`);
    } catch (err) {
      console.error('❌ [JS] Erro ao obter status do cartão:', err);
      console.error(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter status: ${errorMessage}`);
    }
  };

  const handleAddCard = async () => {
    try {
      console.log('🔍 [JS] Iniciando processo de adição de cartão...');

      console.log('🔍 [JS] Obtendo constantes...');
      const constants = await getConstants();
      console.log('✅ [JS] Constantes obtidas:', constants);

      const cardData: AndroidCardData = {
        network: constants.CARD_NETWORK_ELO.toString(),
        tokenServiceProvider: constants.TOKEN_PROVIDER_ELO,
        opaquePaymentCard:
          'MUNEQzlBQUFBODM2MjRFQTg0RTBCNEVEMkU4NjI2MEQ5OEU3NEIyODA5NTJCMjMzMTlEODYyNzkxQzUwRjQ2RTM0NERFMjAxMDUzOUU2ODQxNjAxNzQxNjI4QTk1NEU2MDgwOTA0Mjc0NjBFNkFCN0ZDM0MxMzc3OTUwOEI2RDlBMDc4RDQ5NzJDQ0YyMjk0MDRDNEMzMzRCQzc1NDc0N0E2MzNCNTFEMDM2RjdFMjJCQjQxOEFDMzkwRTNGODVERDQxRjNGM0ZDRjVEQTA4ODBEQzJFMEVBNjc1MjA0MjVEQzJBQ0MyMUREQTExMzRDRjdGNkU0MEJDRUEzNEVDMg==',
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
      const tokenId = await addCardToWallet(cardData);
      console.log('✅ [JS] Cartão adicionado com sucesso! Token ID:', tokenId);
      Alert.alert('Sucesso', `Cartão adicionado com ID: ${tokenId}`);
    } catch (err) {
      console.error('❌ [JS] Erro ao adicionar cartão:', err);
      console.error(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      console.error('❌ [JS] Error details:', {
        name: err instanceof Error ? err.name : 'Unknown',
        message: err instanceof Error ? err.message : String(err),
        code: (err as any)?.code || 'N/A',
        nativeError: (err as any)?.nativeError || 'N/A',
      });
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao adicionar cartão: ${errorMessage}`);
    }
  };

  const handleGetAvailableWallets = async () => {
    try {
      console.log('🔍 [JS] Iniciando obtenção de wallets disponíveis...');
      const wallets = await getAvailableWallets();
      console.log('✅ [JS] Wallets disponíveis obtidas:', wallets);
      Alert.alert(
        'Wallets Disponíveis',
        `Módulos: ${wallets.modules.join(', ')}\n` +
          `Nomes: ${wallets.moduleNames.join(', ')}\n` +
          `Atual: ${wallets.currentModule}`
      );
    } catch (err) {
      console.error('❌ [JS] Erro ao obter wallets disponíveis:', err);
      console.error(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter wallets: ${errorMessage}`);
    }
  };

  const handleSwitchWallet = async () => {
    try {
      console.log('🔍 [JS] Iniciando troca de wallet para Google Pay...');
      const result = await switchWallet('google');
      console.log('✅ [JS] Wallet trocada com sucesso:', result);
      Alert.alert('Sucesso', result);
    } catch (err) {
      console.error('❌ [JS] Erro ao trocar wallet:', err);
      console.error(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
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
      } else {
        Alert.alert('Informação', 'Carteira já existia.');
      }
    } catch (err) {
      console.error('❌ [JS] Erro ao criar carteira:', err);
      console.error(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao criar carteira: ${errorMessage}`);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Builders Wallet - Nova API</Text>

      <TouchableOpacity style={styles.button} onPress={handleCheckAvailability}>
        <Text style={styles.buttonText}>Verificar Disponibilidade</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleCreateWallet}>
        <Text style={styles.buttonText}>Criar Carteira</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleGetWalletInfo}>
        <Text style={styles.buttonText}>Obter Informações da Wallet</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleGetCardStatus}>
        <Text style={styles.buttonText}>Status do Cartão</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleAddCard}>
        <Text style={styles.buttonText}>Adicionar Cartão</Text>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.button}
        onPress={handleGetAvailableWallets}
      >
        <Text style={styles.buttonText}>Wallets Disponíveis</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleSwitchWallet}>
        <Text style={styles.buttonText}>Trocar para Google Pay</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

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
    marginBottom: 30,
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
});
