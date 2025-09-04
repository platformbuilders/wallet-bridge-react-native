#!/bin/bash
# Script para limpeza básica dos builds Android

echo "🧹 Limpando builds Android..."

# Remove diretórios de build
rm -rf android/build
rm -rf android/app/build
rm -rf android/app/.cxx

echo "📦 Executando Gradle clean..."
cd android
./gradlew clean
cd ..

echo "✅ Limpeza básica concluída!"
