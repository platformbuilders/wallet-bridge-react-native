#!/bin/bash
# Script para limpeza completa da lib principal

echo "🧹 Limpeza da lib principal iniciada..."

# Ir para o diretório raiz da lib
cd "$(dirname "$0")/.."

# Limpar cache do Yarn
echo "🧹 Limpando cache do Yarn..."
yarn cache clean --all

# Remover node_modules
echo "🗑️  Removendo node_modules..."
rm -rf node_modules

# Limpar builds
echo "🗑️  Limpando builds..."
rm -rf lib
rm -rf android/build
rm -rf android/app/build

# Reinstalar dependências
echo "📥 Reinstalando dependências..."
yarn install

# Build da lib
echo "🔨 Fazendo build da lib..."
yarn prepare

echo "✅ Limpeza da lib concluída!"
