# @platformbuilders/wallet-bridge-react-native

Este repositório contém um pacote React Native para integração com carteiras digitais. Ele atua como uma ponte (bridge) que se conecta aos SDKs nativos de cada carteira, abstraindo a complexidade dos fluxos de provisionamento (Push e App2App). Inicialmente compatível com Google Pay Wallet, com planos de expansão para Samsung Pay e Apple Wallet.

## Installation


```sh
npm install @platformbuilders/wallet-bridge-react-native
```


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
