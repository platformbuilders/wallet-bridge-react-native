#!/bin/bash
# Script para limpeza completa incluindo node_modules

echo "🧹 Limpeza completa iniciada..."

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

# Reinstala dependências
echo "📥 Reinstalando dependências..."
yarn install

echo "✅ Limpeza completa concluída!"
