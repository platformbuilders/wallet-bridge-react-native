#!/bin/bash

# Script para buildar o app Android example
# Executa: yarn install -> limpa cache Android -> yarn android

set -e  # Para o script se algum comando falhar

# --ios: delega para o build iOS, repassando as demais flags
# (ex.: yarn build:app-example --ios --device "iPhone 17")
if [ "$1" = "--ios" ]; then
  shift
  exec "$(dirname "$0")/build-ios-example.sh" "$@"
fi

echo "🚀 Iniciando build do app Android example..."

# A lib entra no example pelo `root: '..'` do example/react-native.config.js
# (autolinking), então não precisa de yalc.
cd "$(dirname "$0")/../example"

# Instala as dependências
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
