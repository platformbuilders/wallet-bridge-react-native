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
} from '@platformbuilders/wallet-bridge-react-native';
import type {
  AndroidCardData,
  WalletData,
  CardStatus,
} from '@platformbuilders/wallet-bridge-react-native';
import { useState } from 'react';

export default function App() {
  const googleWallet = new GoogleWalletClient();
  
  // Estado para o OPC (Opaque Payment Card)
  const [opcValue, setOpcValue] = useState(
    'M0VGNkZENjRFMEM1MTdEOTgwOEU4N0RGMzRCNkE0M0U4QURBNUEyNjIzQjgyQzEzODZEQkZGN0JEQzM3NzI4NjQ0ODMzRDhBODlFREEwODhDREI2NkMwODM2NkQxRERCN0EzQ0U0RkZFMjJERUZFMEYwM0VCQjlBRkVGNDEzNUQxMjhFODg4NkIzMjBFREZENzk5OUMyODQ4ODRCMzNBMURCNDA0MjQwRDYxMEJDNzRFMjQzMTcwRkNBQzEzRjgzQ0Y4ODI0RTc1QkE4RENGRTY3MjRDQ0U4MEIxM0RCOUMwRjA2MkYzQkIzMjJBNjlE'
  );
  
  // Verificar se o módulo está disponível
  if (!googleWallet) {
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
      const isAvailable = await googleWallet.checkWalletAvailability();
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
      const walletInfo: WalletData = await googleWallet.getSecureWalletInfo();
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

  const handleGetCardStatus = async () => {
    try {
      console.log('🔍 [JS] Iniciando verificação de status do cartão...');
      const status: CardStatus = await googleWallet.getCardStatusBySuffix('6890');
      console.log('✅ [JS] Status do cartão obtido:', status);
      Alert.alert('Status do Cartão', `Status: ${status}`);
    } catch (err) {
      console.log('❌ [JS] Erro ao obter status do cartão:', err);
      console.log(
        '❌ [JS] Stack trace:',
        err instanceof Error ? err.stack : 'N/A'
      );
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao obter status: ${errorMessage}`);
    }
  };

  const handleAddCard = async (opc?: string) => {
    try {
      console.log('🔍 [JS] Iniciando processo de adição de cartão...');

      console.log('🔍 [JS] Obtendo constantes...');
      const constants = googleWallet.getConstants();
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
      const tokenId = await googleWallet.addCardToWallet(cardData);
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
      const errorMessage = err instanceof Error ? err.message : String(err);
      Alert.alert('Erro', `Erro ao adicionar cartão: ${errorMessage}`);
    }
  };

  const handleCreateWallet = async () => {
    try {
      console.log('🔍 [JS] Iniciando criação de carteira...');
      const walletCreated = await googleWallet.createWalletIfNeeded();
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
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Google Wallet - Exemplo</Text>

      <TouchableOpacity style={styles.button} onPress={handleCheckAvailability}>
        <Text style={styles.buttonText}>Verificar Disponibilidade</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleCreateWallet}>
        <Text style={styles.buttonText}>Criar Google Wallet</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleGetWalletInfo}>
        <Text style={styles.buttonText}>Obter Informações da Wallet</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={handleGetCardStatus}>
        <Text style={styles.buttonText}>Status do Cartão</Text>
      </TouchableOpacity>

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
    marginTop: 20,
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
});
