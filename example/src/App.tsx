import { Text, View, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { pushTokenize } from 'react-native-builders-wallet';
import type { PushTokenizeRequest } from 'react-native-builders-wallet';

export default function App() {
  const handleGPayPush = async () => {
    const data_card = {
      push_data: {
        sender: 'pefisa',
        cardDescriptor:
          'MUNEQzlBQUFBODM2MjRFQTg0RTBCNEVEMkU4NjI2MEQ5OEU3NEIyODA5NTJCMjMzMTlEODYyNzkxQzUwRjQ2RTM0NERFMjAxMDUzOUU2ODQxNjAxNzQxNjI4QTk1NEU2MDgwOTA0Mjc0NjBFNkFCN0ZDM0MxMzc3OTUwOEI2RDlBMDc4RDQ5NzJDQ0YyMjk0MDRDNEMzMzRCQzc1NDc0N0E2MzNCNTFEMDM2RjdFMjJCQjQxOEFDMzkwRTNGODVERDQxRjNGM0ZDRjVEQTA4ODBEQzJFMEVBNjc1MjA0MjVEQzJBQ0MyMUREQTExMzRDRjdGNkU0MEJDRUEzNEVDMg==',
      },
      additional_info: {
        name: 'Caleb Pedro Souza',
        address: 'Rua Jardineira',
        country: 'BR',
        city: 'Natal',
        phone: '73996489673',
        state: 'RN',
        zip_code: '59139444',
        printed_name: 'Caleb Pedro Souza',
        last_4_digits: '6890',
      },
      has_address_info: true,
      has_phone_info: true,
    };

    try {
      // Mock data para o push tokenize
      const mockRequest: PushTokenizeRequest = {
        address: {
          address1: '123 Main St',
          address2: 'Apt 4B',
          countryCode: 'BR',
          locality: 'São Paulo',
          administrativeArea: 'SP',
          name: 'João Silva',
          phoneNumber: '+5511999999999',
          postalCode: '01234-567',
        },
        card: {
          opaquePaymentCard: data_card.push_data.cardDescriptor,
          network: 12, // Mock network value
          tokenServiceProvider: 14, // Mock token service provider
          displayName: data_card.additional_info.name,
          lastDigits: data_card.additional_info.last_4_digits,
        },
      };

      console.log('Iniciando push tokenize...', mockRequest);
      const tokenId = await pushTokenize({
        address: {
          address1: 'Rua Jardineira',
          address2: '',
          countryCode: 'BR',
          locality: 'Natal',
          administrativeArea: '',
          name: 'Caleb Pedro Souza',
          phoneNumber: '73996489673',
          postalCode: '59139444',
        },
        card: {
          opaquePaymentCard: data_card.push_data.cardDescriptor, // Representando um array de bytes
          network: 12,
          tokenServiceProvider: 14,
          displayName: data_card.additional_info.name,
          lastDigits: data_card.additional_info.last_4_digits,
        },
      });
      Alert.alert('Sucesso', `Token criado com ID: ${tokenId}`);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      console.log('Erro no push tokenize:', errorMessage);
      Alert.alert('Erro', `Falha ao criar token: ${errorMessage}`);
    }
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.button} onPress={() => handleGPayPush()}>
        <Text style={styles.buttonText}>G Pay Push</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  resultText: {
    fontSize: 18,
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#4285F4',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    marginTop: 20,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
});
