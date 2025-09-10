#!/bin/bash
# Script para limpeza completa do example

echo "🧹 Limpeza completa do example iniciada..."

# Ir para o diretório do example
cd "$(dirname "$0")/../example"

# Limpar cache do Yarn
echo "🧹 Limpando cache do Yarn..."
yarn cache clean --all

# Primeiro executa gradle clean enquanto node_modules existe
echo "📦 Executando Gradle clean..."
cd android
./gradlew clean
cd ..

# Remove diretórios de build
echo "🗑️  Removendo builds Android..."
rm -rf android/build
rm -rf android/app/build
rm -rf android/app/.cxx

# Remove node_modules
echo "🗑️  Removendo node_modules..."
rm -rf node_modules

# Reinstalar dependências do example
echo "📥 Reinstalando dependências do example..."
yarn install

# Reinstalar a lib localmente
echo "📦 Reinstalando lib localmente..."
yarn add @platformbuilders/wallet-bridge-react-native@file:../

echo "✅ Limpeza completa do example concluída!"
echo "🚀 Execute 'npx react-native run-android' para testar"