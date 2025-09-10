#!/bin/bash
# Script master para limpeza completa de tudo

echo "🚀 Limpeza completa de todo o projeto iniciada..."

# Ir para o diretório raiz
cd "$(dirname "$0")/.."

# Limpar lib principal
echo "📦 Limpando lib principal..."
./scripts/clean-lib.sh

# Limpar example
echo "📱 Limpando example..."
./scripts/clean-example-full.sh

echo "🎉 Limpeza completa de todo o projeto concluída!"
echo "🚀 Execute 'cd example && npx react-native run-android' para testar"
