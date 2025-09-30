import React from 'react';
import {
  Text,
  TouchableOpacity,
  ScrollView,
  View,
  Alert,
  StyleSheet,
} from 'react-native';

export function SamsungPayExample(): React.JSX.Element {
  const handleSamsungPayAction = (): void => {
    Alert.alert(
      'Samsung Pay',
      'Funcionalidade do Samsung Pay será implementada em breve!',
      [{ text: 'OK' }]
    );
  };

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={{ paddingBottom: 40 }}
    >
      <Text style={styles.title}>Samsung Pay - Exemplo</Text>

      <View style={styles.addCardSection}>
        <Text style={styles.sectionTitle}>
          Samsung Pay - Em Desenvolvimento
        </Text>

        <Text style={styles.inputLabel}>
          Esta seção será implementada em breve com as funcionalidades do
          Samsung Pay.
        </Text>

        <TouchableOpacity
          style={styles.button}
          onPress={handleSamsungPayAction}
        >
          <Text style={styles.buttonText}>Testar Samsung Pay</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

// Estilos compartilhados são importados de styles

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
    marginBottom: 16,
    lineHeight: 20,
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
});
