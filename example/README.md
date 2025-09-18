# Google Wallet - App de Exemplo

Este é um aplicativo de exemplo que demonstra todas as funcionalidades da biblioteca `@platformbuilders/wallet-bridge-react-native`, incluindo as melhorias implementadas no fluxo de adicionar cartão e processamento de intents.

## 🚀 Funcionalidades Demonstradas

### Google Pay
- ✅ Verificação de disponibilidade do Google Pay
- ✅ Criação de carteira Google Wallet
- ✅ Obtenção de informações do wallet
- ✅ **Adição de cartão com estrutura otimizada** (Push Provisioning)
- ✅ Listagem de tokens existentes
- ✅ Verificação de status de token específico
- ✅ Verificação se cartão está tokenizado
- ✅ Visualização de token no Google Pay
- ✅ Obtenção de environment (PROD/SANDBOX/DEV)
- ✅ **Listener de intents App2App com decodificação automática**
- ✅ **Tratamento de dados base64 decodificados automaticamente**
- ✅ Tratamento de erros detalhado com códigos específicos
- ✅ Definição de resultado de ativação de token

### Melhorias Implementadas
- 🔄 **Estrutura de dados corrigida** para seguir padrão oficial do Google Pay
- 🔄 **Validação robusta** de campos obrigatórios
- 🔄 **Decodificação automática** de dados base64 em intents
- 🔄 **Interface melhorada** com indicadores visuais claros
- 🔄 **Tratamento de erros específicos** com mensagens descritivas

---

Este é um projeto [**React Native**](https://reactnative.dev) bootstrapped usando [`@react-native-community/cli`](https://github.com/react-native-community/cli).

# Getting Started

> **Note**: Make sure you have completed the [Set Up Your Environment](https://reactnative.dev/docs/set-up-your-environment) guide before proceeding.

## Step 1: Start Metro

First, you will need to run **Metro**, the JavaScript build tool for React Native.

To start the Metro dev server, run the following command from the root of your React Native project:

```sh
# Using npm
npm start

# OR using Yarn
yarn start
```

## Step 2: Build and run your app

With Metro running, open a new terminal window/pane from the root of your React Native project, and use one of the following commands to build and run your Android or iOS app:

### Android

```sh
# Using npm
npm run android

# OR using Yarn
yarn android
```

### iOS

For iOS, remember to install CocoaPods dependencies (this only needs to be run on first clone or after updating native deps).

The first time you create a new project, run the Ruby bundler to install CocoaPods itself:

```sh
bundle install
```

Then, and every time you update your native dependencies, run:

```sh
bundle exec pod install
```

For more information, please visit [CocoaPods Getting Started guide](https://guides.cocoapods.org/using/getting-started.html).

```sh
# Using npm
npm run ios

# OR using Yarn
yarn ios
```

If everything is set up correctly, you should see your new app running in the Android Emulator, iOS Simulator, or your connected device.

This is one way to run your app — you can also build it directly from Android Studio or Xcode.

## Step 3: Explore the Features

### 🎯 Interface Principal

O app de exemplo possui uma interface completa que demonstra:

#### **Seção de Status do Intent**
- Indicador visual do status do listener de intents
- Exibição de dados decodificados automaticamente
- Informações detalhadas sobre o formato dos dados
- Botões para definir resultado de ativação de token

#### **Seção de Adicionar Cartão**
- Campo para inserir OPC (Opaque Payment Card) personalizado
- Botões para limpar e colar OPC da área de transferência
- Integração com o badge oficial do Google Wallet
- Validação em tempo real dos dados

#### **Botões de Funcionalidades**
- Verificação de disponibilidade do Google Pay
- Criação de carteira Google Wallet
- Obtenção de informações do wallet
- Listagem de tokens existentes
- Verificação de status de tokens
- Visualização de tokens no Google Pay

### 🔄 Melhorias Implementadas

#### **Decodificação Automática de Intents**
```typescript
// Dados são decodificados automaticamente pelo nativo
if (event.dataFormat === GoogleWalletDataFormat.BASE64_DECODED) {
  // Dados já prontos para uso
  const activationParams = JSON.parse(event.data);
} else if (event.dataFormat === GoogleWalletDataFormat.RAW) {
  // Decodificação manual necessária
  const decodedData = atob(event.data);
  const activationParams = JSON.parse(decodedData);
}
```

#### **Estrutura de Dados Otimizada**
```typescript
// Nova estrutura seguindo padrão oficial do Google Pay
const cardData = {
  address: {
    address1: 'Rua das Flores, 123',
    countryCode: 'BR',
    locality: 'São Paulo',
    // ... outros campos
  },
  card: {
    opaquePaymentCard: 'base64-encoded-data',
    network: constants.CARD_NETWORK_ELO,
    tokenServiceProvider: constants.TOKEN_PROVIDER_ELO,
    displayName: 'João Silva - Visa',
    lastDigits: '1234'
  }
};
```

#### **Validação Robusta**
- Validação de campos obrigatórios
- Verificação de formato base64
- Validação de `lastDigits` (4 dígitos)
- Códigos de erro específicos

### 📱 Como Testar

1. **Teste de Disponibilidade**: Clique em "Verificar Disponibilidade"
2. **Criação de Carteira**: Clique em "Criar Google Wallet"
3. **Adição de Cartão**: Insira um OPC válido e clique no badge do Google Wallet
4. **Teste de Intent**: Use o app mock para simular intents App2App
5. **Verificação de Tokens**: Liste e visualize tokens existentes

### 🔧 Modificar o App

Open `App.tsx` in your text editor of choice and make some changes. When you save, your app will automatically update and reflect these changes — this is powered by [Fast Refresh](https://reactnative.dev/docs/fast-refresh).

When you want to forcefully reload, for example to reset the state of your app, you can perform a full reload:

- **Android**: Press the <kbd>R</kbd> key twice or select **"Reload"** from the **Dev Menu**, accessed via <kbd>Ctrl</kbd> + <kbd>M</kbd> (Windows/Linux) or <kbd>Cmd ⌘</kbd> + <kbd>M</kbd> (macOS).
- **iOS**: Press <kbd>R</kbd> in iOS Simulator.

## Congratulations! :tada:

You've successfully run and modified your React Native App. :partying_face:

### Now what?

- If you want to add this new React Native code to an existing application, check out the [Integration guide](https://reactnative.dev/docs/integration-with-existing-apps).
- If you're curious to learn more about React Native, check out the [docs](https://reactnative.dev/docs/getting-started).

# Troubleshooting

If you're having issues getting the above steps to work, see the [Troubleshooting](https://reactnative.dev/docs/troubleshooting) page.

# Learn More

To learn more about React Native, take a look at the following resources:

- [React Native Website](https://reactnative.dev) - learn more about React Native.
- [Getting Started](https://reactnative.dev/docs/environment-setup) - an **overview** of React Native and how setup your environment.
- [Learn the Basics](https://reactnative.dev/docs/getting-started) - a **guided tour** of the React Native **basics**.
- [Blog](https://reactnative.dev/blog) - read the latest official React Native **Blog** posts.
- [`@facebook/react-native`](https://github.com/facebook/react-native) - the Open Source; GitHub **repository** for React Native.
