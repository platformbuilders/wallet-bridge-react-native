#!/bin/bash

# Script para verificar se Google Play Services Tap and Pay está disponível
# e definir a propriedade para o build.gradle

echo "🔍 Verificando disponibilidade do Google Play Services Tap and Pay..."

# Verifica se o repositório Google está acessível
if curl -s --head https://maven.google.com/ | head -n 1 | grep -q "200 OK"; then
    echo "✅ Repositório Google Maven acessível"
    
    # Tenta resolver a dependência sem baixar
    if ./gradlew -q dependencies --configuration implementation | grep -q "play-services-tapandpay"; then
        echo "✅ Google Play Services Tap and Pay está disponível"
        echo "googlePlayServicesAvailable=true" > gradle.properties
    else
        echo "⚠️ Google Play Services Tap and Pay não encontrado no repositório"
        echo "googlePlayServicesAvailable=false" > gradle.properties
    fi
else
    echo "❌ Repositório Google Maven não acessível"
    echo "googlePlayServicesAvailable=false" > gradle.properties
fi

echo "📝 Propriedade definida em gradle.properties"
