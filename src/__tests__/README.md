# Testes Unitários - BuildersWallet

Este diretório contém os testes unitários para a biblioteca BuildersWallet React Native.

## Estrutura

- `jest.setup.ts` - Configuração inicial dos testes e mocks
- `__mocks__/` - Mocks internos dos módulos nativos (não vão no pacote publicado)
  - `index.ts` - Mocks usados pelos testes da lib
  - `README.md` - Documentação dos mocks
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

### Mocks internos

Todos os mocks estão em `__mocks__/index.ts`, para os testes da lib (não vão no pacote publicado):

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

## Usando os mocks nos testes da lib

Os mocks são internos: o pacote publicado exclui `__tests__`, então um app consumidor não consegue importá-los. Nos testes da lib (imports relativos a `src/__tests__/`):

```typescript
// 1. Importar os mocks
import {
  mockGoogleWalletModule,
  mockSamsungWalletModule,
  setupSuccessMocks,
  resetAllMocks,
} from './__mocks__';

// 2. Configurar no jest.setup.ts
import { mockReactNative } from './__mocks__';
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
