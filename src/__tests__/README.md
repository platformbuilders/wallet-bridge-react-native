# Testes Unitários - BuildersWallet

Este diretório contém os testes unitários para a biblioteca BuildersWallet React Native.

## Estrutura

- `jest.setup.ts` - Configuração inicial dos testes e mocks
- `__mocks__/` - Diretório com mocks exportáveis dos módulos nativos
  - `index.ts` - Mocks exportáveis para uso em aplicações externas
  - `README.md` - Documentação dos mocks exportáveis
- `*.test.ts` - Arquivos de teste específicos
  - `index.test.tsx` - Teste de setup básico
  - `NativeBuildersWallet.test.ts` - Testes dos módulos nativos
  - `types.test.ts` - Testes dos tipos e enums

## Configuração

Os testes estão configurados para:

- Mockar os módulos nativos do React Native
- Mockar as implementações específicas do iOS
- Mockar os event emitters
- Testar apenas a lógica TypeScript da biblioteca

## Executando os Testes

```bash
# Executar todos os testes
yarn test

# Executar testes em modo watch
yarn test:watch

# Executar testes com coverage
yarn test:coverage

# Executar testes para CI
yarn test:ci
```

## Mocks

### Módulos Nativos (React Native)
- `GoogleWallet` - Mock do módulo nativo do Google Wallet
- `SamsungWallet` - Mock do módulo nativo do Samsung Wallet

### Módulos iOS
- `GoogleWalletIOS` - Mock da implementação iOS do Google Wallet
- `SamsungWalletIOS` - Mock da implementação iOS do Samsung Wallet

### Event Emitters
- `GoogleWalletEventEmitter` - Mock do event emitter do Google Wallet
- `SamsungWalletEventEmitter` - Mock do event emitter do Samsung Wallet

### Mocks Exportáveis
Todos os mocks estão disponíveis em `__mocks__/index.ts` para uso em aplicações externas:
- `mockGoogleWalletModule` - Mock completo do GoogleWalletModule
- `mockSamsungWalletModule` - Mock completo do SamsungWalletModule
- `mockGoogleWalletData` - Dados de exemplo para Google Wallet
- `mockSamsungCard` - Cartão de exemplo do Samsung
- `resetAllMocks()` - Função para resetar todos os mocks
- `setupSuccessMocks()` - Configura mocks para cenário de sucesso
- `setupErrorMocks()` - Configura mocks para cenário de erro
- `setupUnavailableMocks()` - Configura mocks para carteiras não disponíveis

## Testes Incluídos

1. **Setup Test** - Verifica se o setup básico está funcionando
2. **NativeBuildersWallet Test** - Testa os métodos dos módulos nativos
3. **Types Test** - Testa os tipos e enums da biblioteca

## Usando Mocks em Aplicações Externas

Para usar os mocks em aplicações que utilizam a biblioteca:

```typescript
// 1. Importar os mocks
import {
  mockGoogleWalletModule,
  mockSamsungWalletModule,
  setupSuccessMocks,
  resetAllMocks,
} from '@platformbuilders/wallet-bridge-react-native/src/__tests__/__mocks__';

// 2. Configurar no jest.setup.ts
import { mockReactNative } from '@platformbuilders/wallet-bridge-react-native/src/__tests__/__mocks__';
jest.mock('react-native', () => mockReactNative);

// 3. Usar nos testes
describe('Meu App - Testes de Wallet', () => {
  beforeEach(() => {
    resetAllMocks();
    setupSuccessMocks();
  });

  it('deve verificar se o Google Wallet está disponível', async () => {
    const isAvailable = await GoogleWalletModule.checkWalletAvailability();
    expect(isAvailable).toBe(true);
  });
});
```

## Adicionando Novos Testes

Para adicionar novos testes:

1. Crie um arquivo `*.test.ts` no diretório `src/__tests__/`
2. Importe os módulos necessários
3. Use os mocks já configurados
4. Siga o padrão de nomenclatura existente

Exemplo:
```typescript
import { GoogleWalletModule } from '../NativeBuildersWallet';

describe('Meu Teste', () => {
  it('deve fazer algo', () => {
    // teste aqui
  });
});
```
