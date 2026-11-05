#!/bin/bash

# Script para buildar o app Android example
# Executa: yalc publish -> yalc update -> limpa cache Android -> yarn android

set -e  # Para o script se algum comando falhar

# --ios: delega para o build iOS, repassando as demais flags
# (ex.: yarn build:app-example --ios --device "iPhone 17")
if [ "$1" = "--ios" ]; then
  shift
  exec "$(dirname "$0")/build-ios-example.sh" "$@"
fi

echo "🚀 Iniciando build do app Android example..."

# Navega para o diretório raiz do repositório (relativo ao script)
echo "📦 Publicando com yalc no diretório raiz..."
cd "$(dirname "$0")/.." # volta para a pasta raiz do projeto
yalc publish

# Navega para o diretório example
echo "🔄 Atualizando dependências com yalc no diretório example..."
cd example
yalc update

# Instala as dependências após o yalc update
echo "📥 Instalando dependências..."
yarn install

# Limpa o cache do Android
echo "🧹 Limpando cache do Android..."
cd android
./gradlew clean
cd ..

# Builda o app Android
echo "🔨 Buildando o app Android..."
yarn android

echo "✅ Build do app Android example concluído com sucesso!"
