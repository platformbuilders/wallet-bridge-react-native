#!/bin/bash

# Script rápido para capturar logs do Samsung Wallet Module
# Captura logs tanto da implementação real quanto do mock
# Versão simplificada para uso rápido

set -e

# Cores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}📱 Samsung Wallet - Logs Rápidos (Real + Mock)${NC}"
echo -e "${YELLOW}💡 Pressione Ctrl+C para parar${NC}"
echo ""

# Verifica se há dispositivos
DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}❌ Nenhum dispositivo conectado!${NC}"
    exit 1
fi

# Captura logs com filtro específico para SamsungWallet e SamsungWalletMock
echo -e "${GREEN}🚀 Capturando logs do Samsung Wallet (Real e Mock)...${NC}"
adb logcat -s SamsungWallet:* SamsungWalletMock:*
