# @platformbuilders/wallet-bridge-react-native

[![npm version](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native.svg)](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Uma biblioteca React Native para integração unificada com carteiras digitais (Google Pay, Samsung Pay). Atua como uma ponte (bridge) que se conecta aos SDKs nativos de cada carteira, abstraindo a complexidade dos fluxos de provisionamento (Push e App2App).

## 🚀 Características

- **Interface Unificada**: Uma única API para Google Pay e Samsung Pay
- **Detecção Automática**: Escolhe automaticamente o wallet disponível
- **Fallback Seguro**: Funciona mesmo sem SDKs instalados
- **Extensível**: Fácil adicionar novos provedores
- **Foco no Essencial**: Apenas métodos necessários para push provisioning
- **App2App Support**: Suporte completo para fluxos de ativação de token
- **Mock Mode**: Modo de desenvolvimento para testes sem SDKs reais

## 📦 Instalação

### Instalação via NPM

```bash
npm install @platformbuilders/wallet-bridge-react-native
# ou
yarn add @platformbuilders/wallet-bridge-react-native
```

### Instalação via Yalc (Desenvolvimento Local)

Para desenvolvimento e testes locais, use o Yalc:

#### 1. Instalar Yalc Globalmente

```bash
# Instalar yarn globalmente (se não tiver)
npm install -g yarn

# Instalar yalc globalmente
npm install -g yalc
# ou
yarn global add yalc
```

#### 2. Gerar e Publicar a Biblioteca Local

```bash
# Na pasta da biblioteca
cd /caminho/para/react-native-builders-wallet

# Instalar dependências
yarn install

# Build da biblioteca
yarn prepare

# Publicar localmente com yalc
yalc publish
```

#### 3. Instalar no Seu Projeto

```bash
# No seu projeto React Native
cd /caminho/para/seu-projeto

# Adicionar a biblioteca local
yalc add @platformbuilders/wallet-bridge-react-native

# Instalar dependências
yarn install
```

#### 4. Atualizar a Biblioteca Local

```bash
# Na pasta da biblioteca, após mudanças
yarn prepare
yalc push

# No seu projeto
yalc update
```

## ⚙️ Configuração

### Android

#### 1. Configurar Google Pay SDK

Siga as instruções em [GOOGLE_PAY_SETUP.md](./GOOGLE_PAY_SETUP.md):

1. Baixe o Google Pay Tap and Pay SDK da [página oficial](https://developers.google.com/pay/issuers/apis/push-provisioning/android/releases)
2. Descompacte e coloque o conteúdo em `android/libs/com/google/android/gms/play-services-tapandpay/`
3. Configure `android/gradle.properties`:
   ```properties
   includeGooglePlayServices=true
   ```

#### 2. Configurar Samsung Pay SDK

Siga as instruções em [SAMSUNG_PAY_SETUP.md](./SAMSUNG_PAY_SETUP.md):

1. Baixe o Samsung Pay SDK JAR
2. Renomeie para `samsungpay_<versão>.jar` e coloque em `libs/`
3. Configure `android/gradle.properties`:
   ```properties
   enableSamsungPay=true
   ```

#### 3. Configurar AndroidManifest.xml

Adicione o intent filter para App2App:

```xml
<activity android:name=".MainActivity">
  <intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
  </intent-filter>
  <!-- Intent filter para capturar ativação de token -->
  <intent-filter>
    <action android:name="br.com.pefisa.pefisa.hml.action.ACTIVATE_TOKEN"/>
    <category android:name="android.intent.category.DEFAULT"/>
  </intent-filter>
</activity>
```

### iOS

A biblioteca está preparada para iOS, mas atualmente foca no Android. O suporte completo ao iOS será adicionado em versões futuras.

## 🎯 Uso

### API Unificada (Recomendado)

```javascript
import { NativeModules } from 'react-native';

const { BuildersWallet } = NativeModules;

// Verificar wallets disponíveis
const availableWallets = await BuildersWallet.getAvailableWallets();
console.log('Wallets disponíveis:', availableWallets);

// Verificar disponibilidade
const isAvailable = await BuildersWallet.checkWalletAvailability();

// Obter informações do wallet
const walletInfo = await BuildersWallet.getSecureWalletInfo();

// Adicionar cartão
const result = await BuildersWallet.addCardToWallet(cardData);
```

### Módulos Específicos

```javascript
import { NativeModules } from 'react-native';

const { GoogleWallet, SamsungWallet } = NativeModules;

// Google Pay específico
if (GoogleWallet) {
  const tokens = await GoogleWallet.listTokens();
  const isDefault = await GoogleWallet.isGooglePayDefaultNFCPayment();
}

// Samsung Pay específico
if (SamsungWallet) {
  await SamsungWallet.init('seu-service-id');
  const status = await SamsungWallet.getSamsungPayStatus();
}
```

### App2App (Ativação de Token)

```javascript
import { GoogleWalletEventEmitter } from '@platformbuilders/wallet-bridge-react-native';

const eventEmitter = new GoogleWalletEventEmitter();

// Ativar listener
await GoogleWallet.setIntentListener();

// Escutar eventos
const removeListener = eventEmitter.addIntentListener((event) => {
  console.log('Intent recebido:', event);
  
  if (event.type === 'ACTIVATE_TOKEN') {
    // Processar ativação de token
    const decodedData = atob(event.data);
    const activationParams = JSON.parse(decodedData);
    console.log('Parâmetros de ativação:', activationParams);
  }
});

// Cleanup
removeListener();
```

## 🏗️ Estrutura do Projeto

```
react-native-builders-wallet/
├── 📁 src/                          # Código fonte TypeScript
│   ├── 📁 types/                    # Definições de tipos
│   │   ├── common.types.ts          # Types comuns
│   │   ├── google-wallet.types.ts   # Types do Google Pay
│   │   ├── samsung-wallet.types.ts  # Types do Samsung Pay
│   │   └── index.ts                 # Re-exports
│   ├── NativeBuildersWallet.ts      # Interface principal
│   └── index.tsx                    # Ponto de entrada
├── 📁 android/                      # Código nativo Android
│   └── 📁 src/main/java/com/builders/wallet/
│       ├── 📁 googletapandpay/      # Módulo Google Pay
│       │   ├── GoogleWalletModule.kt
│       │   ├── GoogleWalletImplementation.kt
│       │   ├── GoogleWalletMock.kt
│       │   └── GoogleWalletContract.kt
│       ├── 📁 samsungpay/           # Módulo Samsung Pay
│       │   ├── SamsungWalletModule.kt
│       │   └── SamsungWalletPackage.kt
│       └── BuildersWalletPackage.kt # Package principal
├── 📁 ios/                          # Código nativo iOS
├── 📁 example/                      # App de exemplo
│   ├── 📁 src/
│   │   └── App.tsx                  # Exemplo completo de uso
│   ├── 📁 android/                  # Projeto Android de exemplo
│   └── 📁 ios/                      # Projeto iOS de exemplo
├── 📁 GoogleWalletAppMock/          # App mock para testes
│   └── 📁 app/
│       └── 📁 src/main/
│           └── MainActivity.kt      # Simulador App2App
├── 📁 lib/                          # Build output
├── 📄 package.json                  # Configuração do projeto
├── 📄 BuildersWallet.podspec        # Configuração iOS
└── 📄 README.md                     # Este arquivo
```

## 🧪 App de Mock - GoogleWalletAppMock

O `GoogleWalletAppMock` é um aplicativo Android que simula o Google Wallet para facilitar os testes do fluxo App2App durante o desenvolvimento.

### Como Usar o Mock

1. **Instalar o App Mock**:
   ```bash
   cd GoogleWalletAppMock
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Configurar o Intent Filter**:
   O mock usa o package `com.google.android.gms_mock` e action `br.com.pefisa.pefisa.hml.action.ACTIVATE_TOKEN`

3. **Simular App2App**:
   - Abra o app mock
   - Clique em "Simular App 2 App"
   - O app tentará abrir seu aplicativo principal com dados simulados

### Dados Simulados

O mock envia dados em base64 contendo:
```json
{
  "token": "simulated_token_1234567890",
  "activation_data": {
    "user_id": "1234567890",
    "card_id": "card_abcdefghij",
    "timestamp": 1703048000000
  }
}
```

## 📱 App de Exemplo

O app de exemplo em `example/` demonstra todas as funcionalidades da biblioteca:

### Executar o Exemplo

```bash
# Instalar dependências
cd example
yarn install

# Android
yarn android

# iOS
yarn ios
```

### Funcionalidades Demonstradas

- ✅ Verificação de disponibilidade de wallets
- ✅ Criação de carteira
- ✅ Obtenção de informações do wallet
- ✅ Adição de cartão com OPC personalizado
- ✅ Listagem de tokens
- ✅ Verificação de status de token
- ✅ Listener de intents App2App
- ✅ Decodificação de dados base64
- ✅ Tratamento de erros detalhado

## 🔧 Modo Mock para Desenvolvimento

Para desenvolvimento sem SDKs reais, ative o modo mock:

```properties
# android/gradle.properties
GOOGLE_WALLET_USE_MOCK=true
```

### Comportamento do Mock

- `checkWalletAvailability()`: Sempre retorna `true`
- `getSecureWalletInfo()`: Retorna dados simulados
- `addCardToWallet()`: Simula adição com delay de 2 segundos
- `listTokens()`: Retorna 2 tokens simulados (Visa e Mastercard)

## 📚 API Reference

### Métodos Principais

| Método | Descrição | Parâmetros | Retorna |
|--------|-----------|------------|---------|
| `checkWalletAvailability` | Verifica se o wallet está disponível | Nenhum | `boolean` |
| `getSecureWalletInfo` | Retorna informações do wallet | Nenhum | `WalletData` |
| `getCardStatusBySuffix` | Status do cartão por últimos dígitos | `lastDigits: string` | `CardStatus` |
| `addCardToWallet` | Adiciona cartão ao wallet | `cardData: ReadableMap` | `TokenizationStatus` |

### Tipos de Dados

```typescript
interface AndroidCardData {
  network: string;
  opaquePaymentCard: string;
  cardHolderName: string;
  lastDigits: string;
  userAddress: UserAddress;
  issuerId?: string;
  tokenizationProvider?: string;
}

interface UserAddress {
  name: string;
  addressOne: string;
  addressTwo?: string;
  city: string;
  administrativeArea: string;
  countryCode: string;
  postalCode: string;
  phoneNumber?: string;
}

type CardStatus = 
  | 'not found'
  | 'active'
  | 'requireAuthorization'
  | 'pending'
  | 'suspended'
  | 'deactivated';
```

## 🛠️ Desenvolvimento

### Pré-requisitos

- Node.js >= 18
- Yarn 3.6.1
- React Native 0.81.0
- Android Studio (para Android)
- Xcode (para iOS)

### Scripts Disponíveis

```bash
# Instalar dependências
yarn install

# Build da biblioteca
yarn prepare

# Executar testes
yarn test

# Verificar tipos
yarn typecheck

# Limpar builds
yarn clean

# Executar exemplo
yarn example android
yarn example ios
```

### Estrutura de Módulos

A biblioteca usa um padrão modular com:

1. **Interface Comum**: `WalletModuleInterface` define métodos padrão
2. **Módulos Específicos**: Implementações para Google Pay e Samsung Pay
3. **Adapters**: Bridge pattern para unificar interfaces
4. **Factory**: Detecção automática de SDKs disponíveis
5. **Fallback**: Módulo stub quando nenhum SDK está disponível

## 🐛 Troubleshooting

### Problemas Comuns

1. **SDK não encontrado**:
   - Verifique se os SDKs estão na pasta correta
   - Confirme as configurações no `gradle.properties`

2. **Build falha**:
   - Execute `yarn clean` e tente novamente
   - Verifique se todas as dependências estão instaladas

3. **App2App não funciona**:
   - Confirme o intent filter no AndroidManifest.xml
   - Verifique se o package name está correto

4. **Mock não funciona**:
   - Verifique se `GOOGLE_WALLET_USE_MOCK=true` está configurado
   - Faça rebuild completo do projeto

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 📞 Suporte

- **Issues**: [GitHub Issues](https://github.com/platformbuilders/wallet-bridge/issues)
- **Documentação**: [Wiki do Projeto](https://github.com/platformbuilders/wallet-bridge/wiki)
- **Email**: nei.vitor@platformbuilders.io

## 🔗 Links Úteis

- [Google Pay Android Push Provisioning](https://developers.google.com/pay/issuers/apis/push-provisioning/android)
- [Samsung Pay SDK](https://developer.samsung.com/samsung-pay)
- [React Native Documentation](https://reactnative.dev/)
- [Yalc Documentation](https://github.com/wclr/yalc)

---

Feito com ❤️ pela equipe da Platform Builders