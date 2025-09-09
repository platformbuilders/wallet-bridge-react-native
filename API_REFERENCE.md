# BuildersWallet - API Reference

Esta biblioteca oferece uma interface unificada para integração com Google Pay e Samsung Pay, focando nos métodos essenciais para push provisioning.

## Funções Disponíveis

| Função | Descrição | Parâmetros | Retorna | Google Pay | Samsung Pay |
|--------|-----------|------------|---------|------------|-------------|
| `checkWalletAvailability` | Verifica se o wallet está disponível e inicializa se possível | Nenhum | `boolean` | ✅ | ✅ |
| `getSecureWalletInfo` | Retorna informações específicas do wallet para transações seguras | Nenhum | `WalletData` | ✅ | ✅ |
| `getCardStatusBySuffix` | Recupera o status atual de um cartão no wallet | `lastDigits: string` | `CardStatus` | ✅ | ✅ |
| `getCardStatusByIdentifier` | Retorna o estado de um cartão baseado em identificador específico | `identifier: string, tsp: string` | `CardStatus` | ✅ | ✅ |
| `addCardToWallet` | Inicia o fluxo de Push Provisioning para adicionar cartão | `cardData: ReadableMap` | `TokenizationStatus` | ✅ | ✅ |

## Tipos de Dados

### AndroidCardData
Dados relacionados a um cartão que será adicionado nas carteiras Android.

```typescript
interface AndroidCardData {
  network: string;                    // Rede do cartão (VISA, MASTERCARD, etc.)
  opaquePaymentCard: string;          // Dados criptografados do cartão
  cardHolderName: string;             // Nome do portador
  lastDigits: string;                 // Últimos 4 dígitos
  userAddress: UserAddress;           // Endereço para verificação
  issuerId?: string;                  // ID do emissor (opcional)
  tokenizationProvider?: string;      // Provedor de tokenização (opcional)
}
```

### UserAddress
Endereço estruturado usado para verificação do portador.

```typescript
interface UserAddress {
  name: string;                       // Nome completo
  addressOne: string;                 // Endereço linha 1
  addressTwo?: string;                // Endereço linha 2 (opcional)
  city: string;                       // Cidade
  administrativeArea: string;         // Estado/Província
  countryCode: string;                // Código do país (ISO 3166-1)
  postalCode: string;                 // CEP/Código postal
  phoneNumber?: string;               // Telefone (opcional)
}
```

### CardStatus
Status possível de um cartão.

```typescript
type CardStatus = 
  | 'not found'      // Cartão não encontrado
  | 'active'         // Cartão ativo
  | 'requireAuthorization' // Requer autorização
  | 'pending'        // Pendente
  | 'suspended'      // Suspenso
  | 'deactivated';   // Desativado
```

## Uso

### Verificar Wallets Disponíveis

```javascript
import { NativeModules } from 'react-native';

const { BuildersWallet } = NativeModules;

// Verificar quais wallets estão disponíveis
const availableWallets = await BuildersWallet.getAvailableWallets();
console.log('Wallets disponíveis:', availableWallets);
// Output: { modules: ['GooglePay', 'SamsungPay'], moduleNames: ['GoogleTapAndPay', 'SamsungPay'], currentModule: 'GoogleTapAndPay' }
```

### Verificar Disponibilidade do Wallet

```javascript
// Verificar se o wallet está disponível
const isAvailable = await BuildersWallet.checkWalletAvailability();
console.log('Wallet disponível:', isAvailable);
```

### Obter Informações do Wallet

```javascript
// Obter informações para transações seguras
const walletInfo = await BuildersWallet.getSecureWalletInfo();
console.log('Informações do wallet:', walletInfo);
```

### Verificar Status do Cartão

```javascript
// Por últimos dígitos
const cardStatus = await BuildersWallet.getCardStatusBySuffix('1234');
console.log('Status do cartão:', cardStatus);

// Por identificador
const cardStatusById = await BuildersWallet.getCardStatusByIdentifier('token123', '1');
console.log('Status do cartão por ID:', cardStatusById);
```

### Adicionar Cartão ao Wallet

```javascript
const cardData = {
  network: 'VISA',
  opaquePaymentCard: 'encrypted_card_data',
  cardHolderName: 'João Silva',
  lastDigits: '1234',
  userAddress: {
    name: 'João Silva',
    addressOne: 'Rua das Flores, 123',
    city: 'São Paulo',
    administrativeArea: 'SP',
    countryCode: 'BR',
    postalCode: '01234-567',
    phoneNumber: '+5511999999999'
  },
  issuerId: 'issuer123',
  tokenizationProvider: 'VISA'
};

// Adicionar cartão
const result = await BuildersWallet.addCardToWallet(cardData);
console.log('Cartão adicionado:', result);
```

## Tratamento de Erros

A biblioteca retorna erros específicos para diferentes situações:

- `SDK_NOT_AVAILABLE`: SDK do wallet não está disponível
- `WALLET_CHECK_ERROR`: Erro ao verificar disponibilidade do wallet
- `CARD_NOT_FOUND`: Cartão não encontrado
- `TOKENIZATION_ERROR`: Erro durante tokenização
- `WALLET_NOT_AVAILABLE`: Wallet específico não está disponível

## Módulos Específicos

Você também pode usar os módulos específicos diretamente:

```javascript
import { NativeModules } from 'react-native';

const { GoogleTapAndPay, SamsungPay } = NativeModules;

// Google Pay específico
if (GoogleTapAndPay) {
  const tokens = await GoogleTapAndPay.listTokens();
  const isDefault = await GoogleTapAndPay.isGooglePayDefaultNFCPayment();
}

// Samsung Pay específico
if (SamsungPay) {
  await SamsungPay.init('seu-service-id');
  const status = await SamsungPay.getSamsungPayStatus();
  const cards = await SamsungPay.getAllCards();
}
```

## Constantes Disponíveis

Cada módulo expõe suas constantes específicas:

- **GoogleTapAndPay**: Constantes do Google Pay (TOKEN_PROVIDER_ELO, CARD_NETWORK_ELO, etc.)
- **SamsungPay**: Constantes do Samsung Pay
- **BuildersWallet**: Informações sobre módulos disponíveis

## Vantagens da Abordagem

1. **Interface Unificada**: Uma única API para Google Pay e Samsung Pay
2. **Detecção Automática**: Escolhe automaticamente o wallet disponível
3. **Fallback Seguro**: Funciona mesmo sem SDKs instalados
4. **Extensível**: Fácil adicionar novos provedores
5. **Foco no Essencial**: Apenas métodos necessários para push provisioning
