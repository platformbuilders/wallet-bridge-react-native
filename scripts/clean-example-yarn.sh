#!/bin/bash
# Script rápido para limpeza de cache do Yarn e reinstalação do example

echo "🧹 Limpeza rápida do example iniciada..."

# Ir para o diretório do example
cd "$(dirname "$0")/../example"

# Limpar cache do Yarn
echo "🧹 Limpando cache do Yarn..."
yarn cache clean

# Remover node_modules
echo "🗑️  Removendo node_modules..."
rm -rf node_modules

# Reinstalar dependências
echo "📥 Reinstalando dependências..."
yarn install

# Reinstalar a lib localmente
echo "📦 Reinstalando lib localmente..."
yarn add @platformbuilders/wallet-bridge-react-native@file:../

echo "✅ Limpeza rápida do example concluída!"
