#!/bin/bash

# Script para logar os logs de ambos os wallets (Samsung e Google)
# Executa: adb logcat com filtros específicos para ambos os wallets

set -e  # Para o script se algum comando falhar

echo "📱 Iniciando log dos wallets (Samsung + Google)..."

# Verifica se o adb está disponível
if ! command -v adb &> /dev/null; then
    echo "❌ Erro: adb não encontrado. Certifique-se de que o Android SDK está instalado e no PATH"
    exit 1
fi

# Verifica se há dispositivos conectados
DEVICE_COUNT=$(adb devices | grep -c "device$")
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "❌ Erro: Nenhum dispositivo Android conectado"
    echo "Conecte um dispositivo ou emulador e tente novamente"
    exit 1
fi

# Verifica se há mais de um dispositivo
if [ "$DEVICE_COUNT" -gt 1 ]; then
    echo "⚠️  Múltiplos dispositivos conectados. Usando o primeiro dispositivo."
fi

echo "🔍 Dispositivos conectados:"
adb devices

echo ""
echo "📋 Iniciando log dos Wallets..."
echo "🔍 Tags filtradas:"
echo "   - SamsungWallet"
echo "   - SamsungWalletMock"
echo "   - GoogleWallet"
echo "   - GoogleWalletMock"
echo "   - MainActivity"
echo "   - WalletIntentProcessor"
echo "   - WalletOpener"
echo ""
echo "💡 Dica: Use Ctrl+C para parar o log"
echo ""

# Inicia o logcat com tags específicas para ambos os wallets
# Usando -s para filtrar apenas as tags específicas e :* para todos os níveis
# Tags filtradas:
# - SamsungWallet: Logs da implementação real da Samsung Wallet
# - SamsungWalletMock: Logs do mock da Samsung Wallet
# - GoogleWallet: Logs da implementação real da Google Wallet
# - GoogleWalletMock: Logs do mock da Google Wallet
# - MainActivity: Logs da MainActivity (para ver o processamento de intents)
# - WalletIntentProcessor: Logs do processador centralizado
# - WalletOpener: Logs da classe nativa para abertura de wallets

adb logcat -c  # Limpa o buffer de logs

echo "🚀 Iniciando captura de logs..."
echo "📱 Para parar, pressione Ctrl+C"
echo ""

# Captura logs com filtros específicos usando tags do adb logcat
# Usando filtros mais específicos para evitar logs desnecessários
adb logcat -s SamsungWallet:* SamsungWalletMock:* GoogleWallet:* GoogleWalletMock:* MainActivity:* WalletIntentProcessor:* WalletOpener:*

echo ""
echo "✅ Log finalizado!"
