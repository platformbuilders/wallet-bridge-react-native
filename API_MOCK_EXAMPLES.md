3
# 🌐 API Mock Local - Exemplos de cURL e Respostas

Este arquivo contém todos os exemplos de cURL para testar a API mock local do Google Wallet, incluindo as respostas padrão esperadas.

## 🌍 Configuração via Variável de Ambiente

O mock pode ser configurado para usar um servidor local através da variável de ambiente `GOOGLE_WALLET_MOCK_API_URL`:

```bash
# Configurar URL do servidor mock
export GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000

# Para emulador Android (usar IP do host)
export GOOGLE_WALLET_MOCK_API_URL=http://10.0.2.2:3000

# Para dispositivo físico (usar IP da rede local)
export GOOGLE_WALLET_MOCK_API_URL=http://192.168.1.100:3000
```

**Comportamento**:
- **Se configurada**: O mock fará requisições HTTP para o servidor especificado
- **Se não configurada**: O mock usará apenas valores padrão simulados (sem requisições HTTP)

## 📋 Endpoints Disponíveis

## 🔧 Constantes do Google Wallet Mock

O mock retorna as seguintes constantes via `getConstants()`:

```json
{
  "SDK_NAME": "GoogleWallet",
  "TOKEN_PROVIDER_ELO": 14,
  "CARD_NETWORK_ELO": 12,
  "TOKEN_STATE_UNTOKENIZED": 1,
  "TOKEN_STATE_PENDING": 2,
  "TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION": 3,
  "TOKEN_STATE_SUSPENDED": 4,
  "TOKEN_STATE_ACTIVE": 5,
  "TOKEN_STATE_FELICA_PENDING_PROVISIONING": 6
}
```

### 1. **Verificar Disponibilidade da Carteira**

```bash
curl -X GET "http://localhost:3000/wallet/availability" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "available": true
}
```

**Resposta Padrão (Fallback):**
```json
true
```

---

### 2. **Obter Informações Seguras da Carteira**

```bash
curl -X GET "http://localhost:3000/wallet/info" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "deviceID": "real_device_123",
  "walletAccountID": "real_wallet_456"
}
```

**Resposta Padrão (Fallback):**
```json
{
  "deviceID": "mock_device_12345",
  "walletAccountID": "mock_wallet_67890"
}
```

---

### 3. **Verificar Status do Token**

```bash
curl -X GET "http://localhost:3000/wallet/token/status?provider=1&refId=abc123" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "tokenState": 5,
  "isSelected": true
}
```

**Resposta Padrão (Fallback):**
```json
{
  "tokenState": 5,
  "isSelected": true
}
```

**Resposta de Erro - Token Não Encontrado:**
```json
{
  "error": "Token não encontrado na carteira ativa",
  "errorCode": "TOKEN_NOT_FOUND"
}
```

**Resposta de Erro - Calling Package Não Verificado:**
```json
{
  "error": "15009: Calling package not verified",
  "errorCode": "CALLING_PACKAGE_NOT_VERIFIED"
}
```

**Resposta de Erro - Estado do Token Inválido:**
```json
{
  "error": "15004: Token encontrado mas em estado inválido",
  "errorCode": "INVALID_TOKEN_STATE"
}
```

**Resposta de Erro - Nenhuma Carteira Ativa:**
```json
{
  "error": "15002: Nenhuma carteira ativa encontrada",
  "errorCode": "NO_ACTIVE_WALLET"
}
```


---

### 4. **Listar Tokens da Carteira**

```bash
curl -X GET "http://localhost:3000/wallet/tokens" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "tokens": [
    {
      "issuerTokenId": "real_token_001",
      "issuerName": "Banco Exemplo",
      "fpanLastFour": "1234",
      "dpanLastFour": "4321",
      "tokenServiceProvider": 1,
      "network": 1,
      "tokenState": 5,
      "isDefaultToken": true,
      "portfolioName": "Carteira Principal"
    },
    {
      "issuerTokenId": "real_token_002",
      "issuerName": "Banco Exemplo",
      "fpanLastFour": "5678",
      "dpanLastFour": "8765",
      "tokenServiceProvider": 1,
      "network": 2,
      "tokenState": 5,
      "isDefaultToken": false,
      "portfolioName": "Outros Cartões"
    }
  ]
}
```

**Resposta Padrão (Fallback):**
```json
[
  {
    "issuerTokenId": "mock_token_001",
    "issuerName": "Banco Mock",
    "fpanLastFour": "1234",
    "dpanLastFour": "4321",
    "tokenServiceProvider": 1,
    "network": 1,
    "tokenState": 5,
    "isDefaultToken": true,
    "portfolioName": "Carteira Principal"
  },
  {
    "issuerTokenId": "mock_token_002",
    "issuerName": "Banco Mock",
    "fpanLastFour": "5678",
    "dpanLastFour": "8765",
    "tokenServiceProvider": 1,
    "network": 2,
    "tokenState": 5,
    "isDefaultToken": false,
    "portfolioName": "Outros Cartões"
  }
]
```

**Nota:** Os valores `tokenServiceProvider` e `network` nos exemplos são apenas para demonstração. Os valores reais dependem das constantes configuradas no mock:
- `TOKEN_PROVIDER_ELO = 14`
- `CARD_NETWORK_ELO = 12`

---

### 5. **Verificar Tokenização**

```bash
curl -X GET "http://localhost:3000/wallet/is-tokenized?lastFour=1234&network=1&provider=1" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "isTokenized": true
}
```

**Resposta Padrão (Fallback):**
```json
true
```

---

### 6. **Visualizar Token**

```bash
curl -X GET "http://localhost:3000/wallet/view-token?provider=1&tokenId=real_token_001" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "success": true,
  "issuerTokenId": "real_token_001",
  "issuerName": "Banco Exemplo",
  "fpanLastFour": "1234",
  "dpanLastFour": "4321",
  "tokenServiceProvider": 1,
  "network": 12,
  "tokenState": 5,
  "isDefaultToken": true,
  "portfolioName": "Carteira Principal"
}
```

**Resposta Padrão (Fallback):**
```json
{
  "issuerTokenId": "mock_token_001",
  "issuerName": "Banco Mock",
  "fpanLastFour": "1234",
  "dpanLastFour": "4321",
  "tokenServiceProvider": 1,
  "network": 12,
  "tokenState": 5,
  "isDefaultToken": true,
  "portfolioName": "Carteira Principal"
}
```

**Resposta quando Token Não Encontrado:**
```json
{
  "success": false,
  "error": "Token não encontrado"
}
```

---

### 7. **Criar Carteira se Necessário**

```bash
curl -X POST "http://localhost:3000/wallet/create" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "success": true,
  "message": "Carteira criada com sucesso"
}
```

**Resposta Padrão (Fallback):**
```json
true
```

---

### 8. **Ativar Listener de Intent**

```bash
curl -X POST "http://localhost:3000/wallet/set-intent-listener" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "success": true,
  "message": "Listener de intent ativado"
}
```

**Resposta Padrão (Fallback):**
```json
true
```

---

### 9. **Remover Listener de Intent**

```bash
curl -X DELETE "http://localhost:3000/wallet/remove-intent-listener" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "success": true,
  "message": "Listener de intent removido"
}
```

**Resposta Padrão (Fallback):**
```json
true
```

---

### 10. **Definir Resultado de Ativação**

```bash
curl -X POST "http://localhost:3000/wallet/set-activation-result" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "status": "approved",
    "activationCode": "ABC123"
  }'
```

**Resposta de Sucesso:**
```json
{
  "success": true,
  "message": "Resultado de ativação definido"
}
```

**Resposta Padrão (Fallback):**
```json
true
```

---

### 11. **Finalizar Atividade**

```bash
curl -X POST "http://localhost:3000/wallet/finish-activity" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "success": true,
  "message": "Atividade finalizada"
}
```

**Resposta Padrão (Fallback):**
```json
true
```

---

### 12. **Obter Environment**

```bash
curl -X GET "http://localhost:3000/wallet/environment" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**Resposta de Sucesso:**
```json
{
  "environment": "PRODUCTION"
}
```

**Resposta Padrão (Fallback):**
```json
"PRODUCTION"
```

---

### 13. **Adicionar Cartão à Carteira**

#### **Sucesso Padrão:**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Visa",
      "lastDigits": "1234"
    }
  }'
```

**Resposta de Sucesso:**
```json
{
  "tokenId": "real_token_001",
  "success": true,
  "message": "Cartão adicionado com sucesso"
}
```

#### **Simular Diferentes Cenários de Erro:**

**1. Cartão Inválido (lastDigits: 0000):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Inválido",
      "lastDigits": "0000"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Cartão inválido",
  "errorCode": "INVALID_CARD_DATA"
}
```

**2. SDK Não Disponível (lastDigits: 1111):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão SDK Error",
      "lastDigits": "1111"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Google Pay SDK não está disponível",
  "errorCode": "SDK_NOT_AVAILABLE"
}
```

**3. Cliente Não Disponível (lastDigits: 2222):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Client Error",
      "lastDigits": "2222"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Cliente TapAndPay não foi inicializado",
  "errorCode": "TAP_AND_PAY_CLIENT_NOT_AVAILABLE"
}
```

**4. Atividade Não Disponível (lastDigits: 3333):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Activity Error",
      "lastDigits": "3333"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Nenhuma atividade disponível",
  "errorCode": "NO_ACTIVITY"
}
```

**5. Erro de Tokenização (lastDigits: 4444):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Tokenize Error",
      "lastDigits": "4444"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Falha ao processar tokenização",
  "errorCode": "PUSH_TOKENIZE_ERROR"
}
```

**6. Timeout (lastDigits: 5555):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Timeout",
      "lastDigits": "5555"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Timeout ao adicionar cartão",
  "errorCode": "ADD_CARD_TIMEOUT"
}
```

**7. Erro de Rede (lastDigits: 6666):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Network Error",
      "lastDigits": "6666"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Erro de conexão",
  "errorCode": "NETWORK_ERROR"
}
```

**8. Permissão Negada (lastDigits: 7777):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Permission Error",
      "lastDigits": "7777"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Permissão negada",
  "errorCode": "PERMISSION_DENIED"
}
```

**9. Cartão Já Existe (lastDigits: 8888):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Duplicate",
      "lastDigits": "8888"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Cartão já existe na carteira",
  "errorCode": "CARD_ALREADY_EXISTS"
}
```

**10. Limite Excedido (lastDigits: 9999):**
```bash
curl -X POST "http://localhost:3000/wallet/add-card" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "address": {
      "address1": "Rua das Flores, 123",
      "address2": "Apto 45",
      "countryCode": "BR",
      "locality": "São Paulo",
      "administrativeArea": "SP",
      "name": "João Silva",
      "phoneNumber": "+5511999999999",
      "postalCode": "01234-567"
    },
    "card": {
      "opaquePaymentCard": "dGVzdF9jYXJkX2RhdGE=",
      "network": 1,
      "tokenServiceProvider": 1,
      "displayName": "Cartão Limit Error",
      "lastDigits": "9999"
    }
  }'
```

**Resposta de Erro:**
```json
{
  "error": "Limite de cartões excedido",
  "errorCode": "CARD_LIMIT_EXCEEDED"
}
```

**Resposta Padrão (Fallback):**
```json
{
  "tokenId": "mock_token_1234567890",
  "success": true,
  "message": "Cartão adicionado com sucesso"
}
```

---

## 🧪 Script de Teste Completo

```bash
#!/bin/bash

BASE_URL="http://localhost:3000"
echo "🧪 Testando API Mock Local - Google Wallet"
echo "=========================================="

# Função para testar endpoint
test_endpoint() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    
    echo -e "\n📡 Testando: $name"
    echo "URL: $method $url"
    
    if [ -n "$data" ]; then
        curl -s -X "$method" "$url" \
          -H "Content-Type: application/json" \
          -H "Accept: application/json" \
          -d "$data" | jq .
    else
        curl -s -X "$method" "$url" \
          -H "Content-Type: application/json" \
          -H "Accept: application/json" | jq .
    fi
}

# Testes
test_endpoint "Verificar Disponibilidade" "GET" "$BASE_URL/wallet/availability"
test_endpoint "Obter Informações da Carteira" "GET" "$BASE_URL/wallet/info"
test_endpoint "Status do Token" "GET" "$BASE_URL/wallet/token/status?provider=1&refId=abc123"
test_endpoint "Listar Tokens" "GET" "$BASE_URL/wallet/tokens"
test_endpoint "Verificar Tokenização" "GET" "$BASE_URL/wallet/is-tokenized?lastFour=1234&network=1&provider=1"
test_endpoint "Visualizar Token" "GET" "$BASE_URL/wallet/view-token?provider=1&tokenId=real_token_001"
test_endpoint "Criar Carteira" "POST" "$BASE_URL/wallet/create"
test_endpoint "Ativar Listener" "POST" "$BASE_URL/wallet/set-intent-listener"
test_endpoint "Remover Listener" "DELETE" "$BASE_URL/wallet/remove-intent-listener"
test_endpoint "Definir Resultado Ativação" "POST" "$BASE_URL/wallet/set-activation-result" '{"status":"approved","activationCode":"ABC123"}'
test_endpoint "Finalizar Atividade" "POST" "$BASE_URL/wallet/finish-activity"
test_endpoint "Obter Environment" "GET" "$BASE_URL/wallet/environment"
test_endpoint "Adicionar Cartão (Sucesso)" "POST" "$BASE_URL/wallet/add-card" '{"address":{"address1":"Rua das Flores, 123","address2":"Apto 45","countryCode":"BR","locality":"São Paulo","administrativeArea":"SP","name":"João Silva","phoneNumber":"+5511999999999","postalCode":"01234-567"},"card":{"opaquePaymentCard":"dGVzdF9jYXJkX2RhdGE=","network":1,"tokenServiceProvider":1,"displayName":"Cartão Visa","lastDigits":"1234"}}'
test_endpoint "Adicionar Cartão (Erro - lastDigits 0000)" "POST" "$BASE_URL/wallet/add-card" '{"address":{"address1":"Rua das Flores, 123","address2":"Apto 45","countryCode":"BR","locality":"São Paulo","administrativeArea":"SP","name":"João Silva","phoneNumber":"+5511999999999","postalCode":"01234-567"},"card":{"opaquePaymentCard":"dGVzdF9jYXJkX2RhdGE=","network":1,"tokenServiceProvider":1,"displayName":"Cartão Inválido","lastDigits":"0000"}}'

echo -e "\n✅ Testes concluídos!"
```

## 🚀 Como Criar um Servidor Mock Local

### 📦 Opção 1: Express.js (Node.js)

#### **1. Instalação e Configuração**

```bash
# Criar diretório para o servidor mock
mkdir google-wallet-mock-server
cd google-wallet-mock-server

# Inicializar projeto Node.js
npm init -y

# Instalar dependências
npm install express cors morgan
npm install --save-dev nodemon
```

#### **2. Criar servidor Express (server.js)**

```javascript
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');

const app = express();
const PORT = 3000;

// Middleware
app.use(cors());
app.use(morgan('combined'));
app.use(express.json());

// Dados mock para simular diferentes cenários
const mockData = {
  wallet: {
    available: true,
    deviceID: "real_device_123",
    walletAccountID: "real_wallet_456",
    environment: "PRODUCTION"
  },
  tokens: [
    {
      issuerTokenId: "real_token_001",
      issuerName: "Banco Exemplo",
      fpanLastFour: "1234",
      dpanLastFour: "4321",
      tokenServiceProvider: 14, // TOKEN_PROVIDER_ELO
      network: 12, // CARD_NETWORK_ELO
      tokenState: 5, // TOKEN_STATE_ACTIVE
      isDefaultToken: true,
      portfolioName: "Carteira Principal"
    },
    {
      issuerTokenId: "real_token_002",
      issuerName: "Banco Exemplo",
      fpanLastFour: "5678",
      dpanLastFour: "8765",
      tokenServiceProvider: 14, // TOKEN_PROVIDER_ELO
      network: 12, // CARD_NETWORK_ELO
      tokenState: 5, // TOKEN_STATE_ACTIVE
      isDefaultToken: false,
      portfolioName: "Outros Cartões"
    }
  ],
  tokenStatus: {
    tokenState: 5, // TOKEN_STATE_ACTIVE
    isSelected: true
  }
};

// Simular delay de rede (opcional)
const simulateDelay = (req, res, next) => {
  const delay = Math.random() * 1000; // 0-1 segundo
  setTimeout(next, delay);
};

// Middleware para simular cenários de erro (opcional)
const simulateError = (req, res, next) => {
  // 10% de chance de erro para simular falhas de rede
  if (Math.random() < 0.1) {
    return res.status(500).json({ error: "Erro simulado do servidor" });
  }
  next();
};

// Aplicar middlewares
app.use(simulateDelay);
app.use(simulateError);

// ============================================================================
// ENDPOINTS DO GOOGLE WALLET MOCK
// ============================================================================

// 1. Verificar Disponibilidade da Carteira
app.get('/wallet/availability', (req, res) => {
  console.log('📱 [API] checkWalletAvailability chamado');
  res.json({ available: mockData.wallet.available });
});

// 2. Obter Informações Seguras da Carteira
app.get('/wallet/info', (req, res) => {
  console.log('📱 [API] getSecureWalletInfo chamado');
  res.json({
    deviceID: mockData.wallet.deviceID,
    walletAccountID: mockData.wallet.walletAccountID
  });
});

// 3. Verificar Status do Token
app.get('/wallet/token/status', (req, res) => {
  const { provider, refId } = req.query;
  console.log(`📱 [API] getTokenStatus chamado - Provider: ${provider}, RefId: ${refId}`);
  
  // Simular diferentes cenários baseados no refId
  if (refId && refId.includes('not_found')) {
    return res.status(400).json({
      error: "Token não encontrado na carteira ativa",
      errorCode: "TOKEN_NOT_FOUND"
    });
  }
  
  if (refId && refId.includes('unverified')) {
    return res.status(400).json({
      error: "15009: Calling package not verified",
      errorCode: "CALLING_PACKAGE_NOT_VERIFIED"
    });
  }
  
  if (refId && refId.includes('invalid_state')) {
    return res.status(400).json({
      error: "15004: Token encontrado mas em estado inválido",
      errorCode: "INVALID_TOKEN_STATE"
    });
  }
  
  if (refId && refId.includes('no_wallet')) {
    return res.status(400).json({
      error: "15002: Nenhuma carteira ativa encontrada",
      errorCode: "NO_ACTIVE_WALLET"
    });
  }
  
  if (refId && refId.includes('suspended')) {
    return res.json({
      tokenState: 4, // TOKEN_STATE_SUSPENDED
      isSelected: false
    });
  }
  
  if (refId && refId.includes('pending')) {
    return res.json({
      tokenState: 2, // TOKEN_STATE_PENDING
      isSelected: false
    });
  }
  
  // Resposta padrão (token ativo)
  res.json(mockData.tokenStatus);
});

// 4. Listar Tokens da Carteira
app.get('/wallet/tokens', (req, res) => {
  console.log('📱 [API] listTokens chamado');
  res.json({ tokens: mockData.tokens });
});

// 5. Verificar Tokenização
app.get('/wallet/is-tokenized', (req, res) => {
  const { lastFour, network, provider } = req.query;
  console.log(`📱 [API] isTokenized chamado - LastFour: ${lastFour}, Network: ${network}, Provider: ${provider}`);
  
  // Simular lógica: tokenizado se lastFour for "1234"
  const isTokenized = lastFour === "1234";
  res.json({ isTokenized });
});

// 6. Visualizar Token
app.get('/wallet/view-token', (req, res) => {
  const { provider, tokenId } = req.query;
  console.log(`📱 [API] viewToken chamado - Provider: ${provider}, TokenId: ${tokenId}`);
  
  // Simular busca do token na lista de tokens mock
  const mockTokens = mockData.tokens;
  const foundToken = mockTokens.find(token => 
    token.issuerTokenId === tokenId && token.tokenServiceProvider === provider
  );
  
  if (foundToken) {
    res.json({
      success: true,
      issuerTokenId: foundToken.issuerTokenId,
      issuerName: foundToken.issuerName,
      fpanLastFour: foundToken.fpanLastFour,
      dpanLastFour: foundToken.dpanLastFour,
      tokenServiceProvider: foundToken.tokenServiceProvider,
      network: foundToken.network,
      tokenState: foundToken.tokenState,
      isDefaultToken: foundToken.isDefaultToken,
      portfolioName: foundToken.portfolioName
    });
  } else {
    res.json({
      success: false,
      error: "Token não encontrado"
    });
  }
});

// 7. Criar Carteira se Necessário
app.post('/wallet/create', (req, res) => {
  console.log('📱 [API] createWalletIfNeeded chamado');
  res.json({
    success: true,
    message: "Carteira criada com sucesso"
  });
});

// 8. Ativar Listener de Intent
app.post('/wallet/set-intent-listener', (req, res) => {
  console.log('📱 [API] setIntentListener chamado');
  res.json({
    success: true,
    message: "Listener de intent ativado"
  });
});

// 9. Remover Listener de Intent
app.delete('/wallet/remove-intent-listener', (req, res) => {
  console.log('📱 [API] removeIntentListener chamado');
  res.json({
    success: true,
    message: "Listener de intent removido"
  });
});

// 10. Definir Resultado de Ativação
app.post('/wallet/set-activation-result', (req, res) => {
  const { status, activationCode } = req.body;
  console.log(`📱 [API] setActivationResult chamado - Status: ${status}, ActivationCode: ${activationCode}`);
  
  res.json({
    success: true,
    message: "Resultado de ativação definido"
  });
});

// 11. Finalizar Atividade
app.post('/wallet/finish-activity', (req, res) => {
  console.log('📱 [API] finishActivity chamado');
  res.json({
    success: true,
    message: "Atividade finalizada"
  });
});

// 12. Obter Environment
app.get('/wallet/environment', (req, res) => {
  console.log('📱 [API] getEnvironment chamado');
  res.json({
    environment: mockData.wallet.environment
  });
});

// 13. Adicionar Cartão à Carteira
app.post('/wallet/add-card', (req, res) => {
  console.log('📱 [API] addCardToWallet chamado');
  console.log('📱 [API] Dados do cartão:', JSON.stringify(req.body, null, 2));
  
  try {
    const { address, card } = req.body;
    const lastDigits = card?.lastDigits || "";
    
    // Simular diferentes cenários baseados nos últimos dígitos
    if (lastDigits.endsWith("0000")) {
      return res.status(400).json({
        error: "Cartão inválido",
        errorCode: "INVALID_CARD_DATA"
      });
    }
    
    if (lastDigits.endsWith("1111")) {
      return res.status(400).json({
        error: "Google Pay SDK não está disponível",
        errorCode: "SDK_NOT_AVAILABLE"
      });
    }
    
    if (lastDigits.endsWith("2222")) {
      return res.status(400).json({
        error: "Cliente TapAndPay não foi inicializado",
        errorCode: "TAP_AND_PAY_CLIENT_NOT_AVAILABLE"
      });
    }
    
    if (lastDigits.endsWith("3333")) {
      return res.status(400).json({
        error: "Nenhuma atividade disponível",
        errorCode: "NO_ACTIVITY"
      });
    }
    
    if (lastDigits.endsWith("4444")) {
      return res.status(400).json({
        error: "Falha ao processar tokenização",
        errorCode: "PUSH_TOKENIZE_ERROR"
      });
    }
    
    if (lastDigits.endsWith("5555")) {
      return res.status(408).json({
        error: "Timeout ao adicionar cartão",
        errorCode: "ADD_CARD_TIMEOUT"
      });
    }
    
    if (lastDigits.endsWith("6666")) {
      return res.status(500).json({
        error: "Erro de conexão",
        errorCode: "NETWORK_ERROR"
      });
    }
    
    if (lastDigits.endsWith("7777")) {
      return res.status(403).json({
        error: "Permissão negada",
        errorCode: "PERMISSION_DENIED"
      });
    }
    
    if (lastDigits.endsWith("8888")) {
      return res.status(409).json({
        error: "Cartão já existe na carteira",
        errorCode: "CARD_ALREADY_EXISTS"
      });
    }
    
    if (lastDigits.endsWith("9999")) {
      return res.status(429).json({
        error: "Limite de cartões excedido",
        errorCode: "CARD_LIMIT_EXCEEDED"
      });
    }
    
    // Sucesso padrão
    const tokenId = `real_token_${Date.now()}`;
    res.json({
      tokenId: tokenId,
      success: true,
      message: "Cartão adicionado com sucesso"
    });
    
  } catch (error) {
    console.error('❌ [API] Erro ao processar addCardToWallet:', error);
    res.status(500).json({
      error: "Erro interno do servidor",
      errorCode: "INTERNAL_SERVER_ERROR"
    });
  }
});

// ============================================================================
// ENDPOINTS DE DEBUG E CONTROLE
// ============================================================================

// Health check
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// Listar todos os endpoints
app.get('/endpoints', (req, res) => {
  const endpoints = [
    'GET /wallet/availability',
    'GET /wallet/info',
    'GET /wallet/token/status',
    'GET /wallet/tokens',
    'GET /wallet/is-tokenized',
    'GET /wallet/view-token',
    'POST /wallet/create',
    'POST /wallet/set-intent-listener',
    'DELETE /wallet/remove-intent-listener',
    'POST /wallet/set-activation-result',
    'POST /wallet/finish-activity',
    'GET /wallet/environment',
    'POST /wallet/add-card',
    'GET /health',
    'GET /endpoints'
  ];
  
  res.json({ endpoints });
});

// Iniciar servidor
app.listen(PORT, () => {
  console.log('🚀 Servidor Mock Google Wallet iniciado!');
  console.log(`📱 URL: http://localhost:${PORT}`);
  console.log('📋 Endpoints disponíveis: http://localhost:3000/endpoints');
  console.log('❤️  Health check: http://localhost:3000/health');
  console.log('==========================================');
});
```

#### **3. Configurar package.json**

```json
{
  "name": "google-wallet-mock-server",
  "version": "1.0.0",
  "description": "Servidor mock para Google Wallet",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js",
    "test": "curl http://localhost:3000/health"
  },
  "dependencies": {
    "express": "^4.18.2",
    "cors": "^2.8.5",
    "morgan": "^1.10.0"
  },
  "devDependencies": {
    "nodemon": "^3.0.1"
  }
}
```

#### **4. Executar o Servidor**

```bash
# Modo desenvolvimento (com auto-reload)
npm run dev

# Modo produção
npm start

# Testar se está funcionando
curl http://localhost:3000/health
```

### 📦 Opção 2: Python Flask (Alternativa)

#### **1. Instalação**

```bash
pip install flask flask-cors
```

#### **2. Servidor Flask (server.py)**

```python
from flask import Flask, jsonify, request
from flask_cors import CORS
import json
import time
import random

app = Flask(__name__)
CORS(app)

# Dados mock
mock_data = {
    "wallet": {
        "available": True,
        "deviceID": "real_device_123",
        "walletAccountID": "real_wallet_456",
        "environment": "PRODUCTION"
    },
    "tokens": [
        {
            "issuerTokenId": "real_token_001",
            "issuerName": "Banco Exemplo",
            "fpanLastFour": "1234",
            "dpanLastFour": "4321",
            "tokenServiceProvider": 1,
            "network": 1,
            "tokenState": 5,
            "isDefaultToken": True,
            "portfolioName": "Carteira Principal"
        }
    ]
}

@app.route('/wallet/availability', methods=['GET'])
def check_wallet_availability():
    print('📱 [API] checkWalletAvailability chamado')
    return jsonify({"available": mock_data["wallet"]["available"]})

@app.route('/wallet/info', methods=['GET'])
def get_secure_wallet_info():
    print('📱 [API] getSecureWalletInfo chamado')
    return jsonify({
        "deviceID": mock_data["wallet"]["deviceID"],
        "walletAccountID": mock_data["wallet"]["walletAccountID"]
    })

@app.route('/wallet/tokens', methods=['GET'])
def list_tokens():
    print('📱 [API] listTokens chamado')
    return jsonify({"tokens": mock_data["tokens"]})

# Adicionar outros endpoints conforme necessário...

if __name__ == '__main__':
    print('🚀 Servidor Mock Google Wallet iniciado!')
    print('📱 URL: http://localhost:3000')
    app.run(host='0.0.0.0', port=3000, debug=True)
```

### 📦 Opção 3: Servidor Simples com http-server

```bash
# Instalar http-server globalmente
npm install -g http-server

# Criar arquivo JSON com respostas
mkdir mock-responses
cd mock-responses

# Criar arquivos JSON para cada endpoint
echo '{"available": true}' > availability.json
echo '{"deviceID": "real_device_123", "walletAccountID": "real_wallet_456"}' > info.json

# Iniciar servidor
http-server -p 3000 -c-1
```

## 📝 Notas Importantes

### 🔧 Configuração
- **URL Base**: `http://localhost:3000`
- **Timeout**: 5 segundos
- **Headers Obrigatórios**: 
  - `Content-Type: application/json`
  - `Accept: application/json`

### 🚀 Como Usar
1. **Escolha uma das opções acima** para criar seu servidor mock
2. **Inicie o servidor** na porta 3000
3. **Execute os cURLs** individualmente ou use o script de teste
4. **Verifique os logs** do `GoogleWalletMock.kt` para debugging
5. **Se a API falhar**, o mock usará automaticamente os valores padrão

### 🐛 Debugging
- Os logs do Android mostrarão se os dados vieram da API ou do fallback
- Use `adb logcat | grep GoogleWalletMock` para ver os logs em tempo real
- Logs incluem emojis para facilitar identificação:
  - 🌐 = Tentativa de API
  - ✅ = Sucesso
  - ❌ = Erro
  - 🔄 = Usando fallback

### 📊 Códigos de Status HTTP
- **200**: Sucesso
- **404**: Endpoint não encontrado
- **500**: Erro interno do servidor
- **Timeout**: 5 segundos (usa fallback)

### 🔄 Fallback Automático
Se a API local não estiver disponível ou retornar erro, o `GoogleWalletMock.kt` automaticamente usará os valores padrão simulados, garantindo que o app nunca falhe.

### 💡 Dicas Avançadas

#### **Simular Cenários Específicos:**
```javascript
// No servidor Express, adicionar query parameters para simular cenários
app.get('/wallet/token/status', (req, res) => {
  const { provider, refId, scenario } = req.query;
  
  switch(scenario) {
    case 'error':
      return res.status(500).json({ error: "Token não encontrado" });
    case 'pending':
      return res.json({ tokenState: 2, isSelected: false }); // TOKEN_STATE_PENDING
    case 'suspended':
      return res.json({ tokenState: 4, isSelected: false }); // TOKEN_STATE_SUSPENDED
    default:
      return res.json({ tokenState: 5, isSelected: true }); // TOKEN_STATE_ACTIVE
  }
});
```

#### **Testar com Diferentes Cenários:**
```bash
# Status do token
curl "http://localhost:3000/wallet/token/status?provider=1&refId=abc123"
```
