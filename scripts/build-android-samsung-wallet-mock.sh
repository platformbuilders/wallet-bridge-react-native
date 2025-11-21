#!/bin/bash

# Script para buildar e instalar o samsung-wallet-app-mock
# Executa: gradlew assembleDebug -> adb install

set -e  # Para o script se algum comando falhar

echo "🚀 Iniciando build e instalação do samsung-wallet-app-mock..."

# Navega para o diretório samsung-wallet-app-mock
cd samsung-wallet-app-mock

# Limpa o build anterior
echo "🧹 Limpando build anterior..."
./gradlew clean

# Builda o APK
echo "🔨 Buildando o APK do samsung-wallet-app-mock..."
./gradlew assembleDebug

# Verifica se o APK foi gerado
APK_PATH="./app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "❌ Erro: APK não foi gerado em $APK_PATH"
    exit 1
fi

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

# Instala o APK
echo "📱 Instalando o APK no dispositivo..."
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "✅ samsung-wallet-app-mock instalado com sucesso!"
    echo "📱 APK instalado: $APK_PATH"
    
    # Abre o app automaticamente
    echo "🚀 Abrindo o samsung-wallet-app-mock..."
    adb shell am start -n com.samsung.android.spay_mock/.MainActivity
    
    if [ $? -eq 0 ]; then
        echo "✅ App aberto com sucesso!"
    else
        echo "⚠️  App instalado, mas não foi possível abrir automaticamente"
        echo "Abra manualmente o samsung-wallet-app-mock no dispositivo"
    fi
else
    echo "❌ Erro ao instalar o APK"
    exit 1
fi

# Volta para o diretório raiz
cd ..

echo "🎉 Build e instalação do samsung-wallet-app-mock concluídos com sucesso!"


