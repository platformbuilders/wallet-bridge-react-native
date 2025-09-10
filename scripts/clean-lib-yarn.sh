#!/bin/bash
# Script rápido para limpeza de cache do Yarn da lib

echo "🧹 Limpeza rápida da lib iniciada..."

# Ir para o diretório raiz da lib
cd "$(dirname "$0")/.."

# Limpar cache do Yarn
echo "🧹 Limpando cache do Yarn..."
yarn cache clean

# Remover node_modules
echo "🗑️  Removendo node_modules..."
rm -rf node_modules

# Reinstalar dependências
echo "📥 Reinstalando dependências..."
yarn install

# Build da lib
echo "🔨 Fazendo build da lib..."
yarn prepare

echo "✅ Limpeza rápida da lib concluída!"
