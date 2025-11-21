import { Alert } from 'react-native';

/**
 * Função utilitária para mostrar resultado da abertura da wallet
 * @param success - Se a operação foi bem-sucedida
 * @param walletName - Nome da wallet (ex: "Google Wallet", "Samsung Pay")
 */
export const showWalletOpenResult = (
  success: boolean,
  walletName: string
): void => {
  if (success) {
    Alert.alert('✅ Sucesso', `${walletName} aberto com sucesso!`, [
      { text: 'OK' },
    ]);
  } else {
    Alert.alert(
      '❌ Erro',
      `Falha ao abrir ${walletName}. Verifique se o app está instalado.`,
      [{ text: 'OK' }]
    );
  }
};
