#!/bin/bash
# Script master para limpeza rápida de tudo

echo "🚀 Limpeza rápida de todo o projeto iniciada..."

# Ir para o diretório raiz
cd "$(dirname "$0")/.."

# Limpar lib principal
echo "📦 Limpando lib principal..."
./scripts/clean-lib-yarn.sh

# Limpar example
echo "📱 Limpando example..."
./scripts/clean-example-yarn.sh

echo "🎉 Limpeza rápida de todo o projeto concluída!"
echo "🚀 Execute 'cd example && npx react-native run-android' para testar"
