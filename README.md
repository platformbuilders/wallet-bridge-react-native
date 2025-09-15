# @platformbuilders/wallet-bridge-react-native

Este repositório contém um pacote React Native para integração com carteiras digitais. Ele atua como uma ponte (bridge) que se conecta aos SDKs nativos de cada carteira, abstraindo a complexidade dos fluxos de provisionamento (Push e App2App). Inicialmente compatível com Google Pay Wallet, com planos de expansão para Samsung Pay e Apple Wallet.

## Installation

```sh
npm install @platformbuilders/wallet-bridge-react-native
```

## Configuration

### Google Wallet Mock Mode

Para desenvolvimento e testes, você pode usar a implementação mock do Google Wallet que simula todas as funcionalidades sem depender do SDK real.

#### Como ativar o Mock:

1. **Abra o arquivo `gradle.properties`** do seu projeto Android
2. **Adicione ou modifique** a seguinte linha:

```properties
# Ativar modo mock para desenvolvimento/testes
GOOGLE_WALLET_USE_MOCK=true

# Para produção, use:
# GOOGLE_WALLET_USE_MOCK=false
```

3. **Rebuild da aplicação** para aplicar a configuração

#### Verificando o modo atual:
```js
import { NativeModules } from 'react-native';
const { GoogleWallet } = NativeModules;

const constants = GoogleWallet.getConstants();
console.log('Modo mock ativo:', constants.useMock);
console.log('SDK Name:', constants.SDK_NAME);
```

#### Comportamento do Mock:
- **checkWalletAvailability()**: Sempre retorna `true`
- **getSecureWalletInfo()**: Retorna dados simulados
- **getTokenStatus()**: Retorna status ativo
- **getEnvironment()**: Retorna "PRODUCTION"
- **isTokenized()**: Retorna `true` apenas para cartões terminados em "1234"
- **addCardToWallet()**: Simula adição com delay de 2 segundos
- **listTokens()**: Retorna 2 tokens simulados (Visa e Mastercard)

> **Importante**: A configuração é definida no arquivo `gradle.properties` e não pode ser alterada em tempo de execução. É necessário rebuild da aplicação para alterar o modo.

## Usage

```js
import { GoogleWallet, SamsungWallet } from '@platformbuilders/wallet-bridge-react-native';

// Google Wallet
const googleWallet = new GoogleWallet();
await googleWallet.checkWalletAvailability();

// Samsung Wallet
const samsungWallet = new SamsungWallet();
await samsungWallet.init('your-service-id');
await samsungWallet.checkWalletAvailability();
```


## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
