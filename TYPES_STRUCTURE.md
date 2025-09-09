# Estrutura de Types da Builders Wallet

## 📁 Organização dos Arquivos

### `src/types.ts`
Arquivo principal contendo todos os types da biblioteca, organizados por categoria:

#### 🔵 **Types Específicos do Google Pay**
```typescript
// Enums específicos do Google Pay com prefixo GOOGLE_
GOOGLE_WALLET_STATUS
GOOGLE_WALLET_STATUS_CODE  
GOOGLE_STATUS_TOKEN
GOOGLE_CONSTANTS
GOOGLE_ENVIRONMENT
GOOGLE_TOKEN_PROVIDER
GOOGLE_CARD_NETWORK
```

#### 🟢 **Types Genéricos da API Unificada**
```typescript
// Types que funcionam com qualquer wallet provider
CardStatus
WalletData
AndroidCardData
UserAddress
```

#### 🟡 **Types de Compatibilidade**
```typescript
// Mantém compatibilidade com API existente
PaymentCard
PushTokenizeRequest
GetTokenStatusParams
ViewTokenParams
Address
Card
PushTokenizeParams
GetConstantsResponse
Token
IsTokenizedParams
```

#### 🔄 **Aliases para Compatibilidade**
```typescript
// Re-exporta com nomes originais para não quebrar código existente
WALLET_STATUS = GOOGLE_WALLET_STATUS
WALLET_STATUS_CODE = GOOGLE_WALLET_STATUS_CODE
STATUS_TOKEN = GOOGLE_STATUS_TOKEN
CONSTANTS = GOOGLE_CONSTANTS
ENVIRONMENT = GOOGLE_ENVIRONMENT
TOKEN_PROVIDER = GOOGLE_TOKEN_PROVIDER
CARD_NETWORK = GOOGLE_CARD_NETWORK
```

### `src/NativeBuildersWallet.ts`
Interface principal da biblioteca que:
- Importa types do `types.ts`
- Re-exporta com nomes de compatibilidade
- Define a interface `Spec` com todos os métodos

### `src/index.tsx`
Ponto de entrada da biblioteca que:
- Re-exporta todos os types
- Re-exporta todos os enums
- Implementa as funções da API

## 🎯 **Separação Clara de Responsabilidades**

### ✅ **Google Pay Específico**
- `GOOGLE_WALLET_STATUS` - Status específicos do Google Pay
- `GOOGLE_WALLET_STATUS_CODE` - Códigos de status do Google Pay
- `GOOGLE_STATUS_TOKEN` - Estados de token do Google Pay
- `GOOGLE_CONSTANTS` - Constantes específicas do Google Pay
- `GOOGLE_ENVIRONMENT` - Ambientes do Google Pay
- `GOOGLE_TOKEN_PROVIDER` - Provedores de token do Google Pay
- `GOOGLE_CARD_NETWORK` - Redes de cartão do Google Pay

### ✅ **Genérico/Unificado**
- `CardStatus` - Status de cartão genérico
- `WalletData` - Dados da wallet genéricos
- `AndroidCardData` - Dados de cartão Android genéricos
- `UserAddress` - Endereço do usuário genérico

### ✅ **Compatibilidade**
- Todos os types com nomes originais mantidos
- Aliases para não quebrar código existente
- Re-exports organizados

## 📖 **Como Usar**

### Importar Types Específicos do Google Pay
```typescript
import { GOOGLE_WALLET_STATUS, GOOGLE_TOKEN_PROVIDER } from 'react-native-builders-wallet';
```

### Importar Types Genéricos
```typescript
import { CardStatus, WalletData, AndroidCardData } from 'react-native-builders-wallet';
```

### Importar com Compatibilidade (Recomendado)
```typescript
import { WALLET_STATUS, TOKEN_PROVIDER, CardStatus } from 'react-native-builders-wallet';
```

## 🔮 **Futuro: Samsung Pay**
Quando implementarmos Samsung Pay, criaremos:
- `SAMSUNG_WALLET_STATUS`
- `SAMSUNG_WALLET_STATUS_CODE`
- `SAMSUNG_STATUS_TOKEN`
- etc.

E manteremos os types genéricos para funcionar com ambos os providers.

## ⚠️ **Importante**
- **NÃO** use os types com prefixo `GOOGLE_` diretamente em código de produção
- Use os aliases sem prefixo para manter compatibilidade
- Os types genéricos (`CardStatus`, `WalletData`, etc.) são seguros para usar
- A separação permite fácil adição de novos providers no futuro
