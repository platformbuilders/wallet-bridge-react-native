#!/bin/bash

# Script para buildar o app Android example
# Executa: yalc publish -> yalc update -> limpa cache Android -> yarn android

set -e  # Para o script se algum comando falhar

echo "🚀 Iniciando build do app Android example..."

# Navega para o diretório raiz do repositório (relativo ao script)
echo "📦 Publicando com yalc no diretório raiz..."
cd "$(dirname "$0")/.." # volta para a pasta raiz do projeto
yalc publish

# Navega para o diretório example
echo "🔄 Atualizando dependências com yalc no diretório example..."
cd example
yalc update

# Limpa o cache do Android
echo "🧹 Limpando cache do Android..."
cd android
./gradlew clean
cd ..

# Builda o app Android
echo "🔨 Buildando o app Android..."
yarn android

echo "✅ Build do app Android example concluído com sucesso!"
