# Mocks Exportáveis - BuildersWallet

Este diretório contém mocks exportáveis que podem ser utilizados em aplicações que fazem uso da biblioteca BuildersWallet para facilitar os testes.

## Scripts de Teste Disponíveis

A biblioteca inclui os seguintes scripts de teste:

```bash
# Executar todos os testes
yarn test

# Executar testes em modo watch (desenvolvimento)
yarn test:watch

# Executar testes com relatório de cobertura
yarn test:coverage

# Executar testes para integração contínua
yarn test:ci
```

## Como Usar

### 1. Importar os Mocks

```typescript
import {
  mockGoogleWalletModule,
  mockSamsungWalletModule,
  mockGoogleWalletData,
  mockSamsungCard,
  resetAllMocks,
  setupSuccessMocks,
  setupErrorMocks,
  setupUnavailableMocks,
} from '@platformbuilders/wallet-bridge-react-native/src/__tests__/__mocks__';
```

### 2. Configurar o Jest

No seu `jest.setup.ts` ou arquivo de configuração de testes:

```typescript
import { mockReactNative } from '@platformbuilders/wallet-bridge-react-native/src/__tests__/__mocks__';

jest.mock('react-native', () => mockReactNative);
```

### 3. Usar nos Testes

```typescript
import { GoogleWalletModule, SamsungWalletModule } from '@platformbuilders/wallet-bridge-react-native';
import { setupSuccessMocks, resetAllMocks } from '@platformbuilders/wallet-bridge-react-native/src/__tests__/__mocks__';

describe('Meu App - Testes de Wallet', () => {
  beforeEach(() => {
    resetAllMocks();
    setupSuccessMocks();
  });

  it('deve verificar se o Google Wallet está disponível', async () => {
    const isAvailable = await GoogleWalletModule.checkWalletAvailability();
    expect(isAvailable).toBe(true);
  });

  it('deve listar cartões do Samsung Pay', async () => {
    const cards = await SamsungWalletModule.getAllCards();
    expect(cards).toHaveLength(1);
    expect(cards[0].cardBrand).toBe('VISA');
  });
});
```

## Mocks Disponíveis

### Módulos de Wallet
- `mockGoogleWalletModule` - Mock completo do GoogleWalletModule
- `mockSamsungWalletModule` - Mock completo do SamsungWalletModule

### Dados de Exemplo
- `mockGoogleWalletData` - Dados de exemplo para Google Wallet
- `mockGoogleTokenInfo` - Informações de token do Google
- `mockGoogleTokenStatus` - Status de token do Google
- `mockSamsungCard` - Cartão de exemplo do Samsung
- `mockSamsungWalletInfo` - Informações da carteira Samsung

### Constantes
- `mockGoogleWalletConstants` - Constantes do Google Wallet
- `mockSamsungWalletConstants` - Constantes do Samsung Wallet

### Event Emitters
- `mockGoogleWalletEventEmitter` - Event emitter do Google Wallet
- `mockSamsungWalletEventEmitter` - Event emitter do Samsung Wallet

### React Native
- `mockReactNative` - Mock completo do React Native com os módulos

## Funções Utilitárias

### `resetAllMocks()`
Reseta todos os mocks para um estado limpo.

### `setupSuccessMocks()`
Configura todos os mocks para retornar sucesso.

### `setupErrorMocks()`
Configura todos os mocks para retornar erro.

### `setupUnavailableMocks()`
Configura todos os mocks para simular carteiras não disponíveis.

## Exemplos de Cenários

### Cenário de Sucesso
```typescript
beforeEach(() => {
  setupSuccessMocks();
});

it('deve funcionar normalmente', async () => {
  const isAvailable = await GoogleWalletModule.checkWalletAvailability();
  expect(isAvailable).toBe(true);
});
```

### Cenário de Erro
```typescript
beforeEach(() => {
  setupErrorMocks();
});

it('deve lidar com erro', async () => {
  await expect(GoogleWalletModule.checkWalletAvailability()).rejects.toThrow();
});
```

### Cenário de Carteira Não Disponível
```typescript
beforeEach(() => {
  setupUnavailableMocks();
});

it('deve detectar carteira não disponível', async () => {
  const isAvailable = await GoogleWalletModule.checkWalletAvailability();
  expect(isAvailable).toBe(false);
});
```

### Mock Personalizado
```typescript
it('deve usar mock personalizado', async () => {
  mockGoogleWalletModule.checkWalletAvailability.mockResolvedValue(false);
  
  const isAvailable = await GoogleWalletModule.checkWalletAvailability();
  expect(isAvailable).toBe(false);
});
```

## Configuração Avançada

### Jest Configuration
```javascript
// jest.config.js
module.exports = {
  preset: 'react-native',
  setupFilesAfterEnv: ['<rootDir>/src/__tests__/jest.setup.ts'],
  testPathIgnorePatterns: [
    '<rootDir>/node_modules/',
    '<rootDir>/lib/',
    '<rootDir>/android/',
    '<rootDir>/ios/',
  ],
  moduleNameMapper: {
    '^@platformbuilders/wallet-bridge-react-native$': '<rootDir>/node_modules/@platformbuilders/wallet-bridge-react-native/src/index.ts',
  },
};
```

### Setup File
```typescript
// jest.setup.ts
import { mockReactNative } from '@platformbuilders/wallet-bridge-react-native/src/__tests__/__mocks__';

jest.mock('react-native', () => mockReactNative);
```
