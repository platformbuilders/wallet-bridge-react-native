#!/bin/bash
# Script para limpar e executar Android

echo "🧹 Limpando e executando Android..."

# Executa limpeza
./scripts/clean.sh

# Executa Android
echo "🚀 Iniciando React Native Android..."
npx react-native run-android
