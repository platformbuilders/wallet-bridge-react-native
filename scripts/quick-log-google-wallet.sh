#!/bin/bash

# Script rápido para capturar logs do Google Wallet Module
# Versão simplificada para uso rápido

set -e

# Cores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}📱 Google Wallet - Logs Rápidos${NC}"
echo -e "${YELLOW}💡 Pressione Ctrl+C para parar${NC}"
echo ""

# Verifica se há dispositivos
DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}❌ Nenhum dispositivo conectado!${NC}"
    exit 1
fi

# Captura logs com filtro específico para GoogleWallet
echo -e "${GREEN}🚀 Capturando logs do Google Wallet...${NC}"
adb logcat -s GoogleWallet:*
