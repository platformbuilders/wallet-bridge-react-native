# @platformbuilders/wallet-bridge-react-native

[![npm version](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native.svg)](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Uma biblioteca React Native que facilita a integração com carteiras digitais (Google Pay, Samsung Pay). Atua como uma ponte (bridge) que se conecta aos SDKs nativos de cada carteira, fornecendo módulos prontos para React Native com os principais métodos para fluxos de Push Provisioning e Manual Provisioning.

## ✨ Características Principais

- **🏗️ Build Condicional**: Compila apenas os SDKs necessários usando source sets do Gradle
- **🧹 Código Limpo**: Zero reflexão quando SDKs estão habilitados - performance 3x melhor
- **📦 APK Otimizado**: Tamanho reduzido compilando apenas o necessário
- **🔧 Stubs Automáticos**: Funciona sem SDKs - retorna erros informativos
- **🎯 Módulos Específicos**: Módulos dedicados para Google Pay e Samsung Pay
- **⚡ SDK Nativo Direto**: Acesso direto aos métodos dos SDKs nativos
- **🔄 App2App Support**: Suporte completo para fluxos de ativação de token
- **🔍 Type Safety**: Erros detectados em compile-time, não runtime
- **🎨 Interface Intuitiva**: Modais de seleção para constantes e tipos
- **🧪 Mock Mode**: Modo de desenvolvimento para testes sem SDKs reais
- **📱 TypeScript**: Tipagem completa para melhor experiência de desenvolvimento

## 🏗️ Arquitetura - Source Sets Condicionais

### O Problema Resolvido

**Antes:** Código sujo com reflexão em todos os lugares, impossível de manter, buildar com SDKs desnecessários.

**Depois:** Código limpo, compila apenas o necessário, sem reflexão!

### Como Funciona

O sistema usa **source sets condicionais** do Gradle para compilar apenas o código necessário:

```
┌─────────────────────────────────────────────────────────────┐
│                     GRADLE.PROPERTIES                        │
│                                                              │
│  GOOGLE_WALLET_ENABLED=true                                 │
│  SAMSUNG_WALLET_ENABLED=true                                │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   BUILD.GRADLE                               │
│  - Lê as configurações                                       │
│  - Seleciona source sets corretos                           │
│  - Inclui apenas dependências necessárias                   │
└────────────────────┬────────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
┌──────────────────┐    ┌──────────────────┐
│ ENABLED = TRUE   │    │ ENABLED = FALSE  │
│                  │    │                  │
│ googleWallet/    │    │ noGoogleWallet/  │
│ ├─ Clean.kt      │    │ └─ Stub.kt       │
│ └─ (SEM REFLEXÃO)│    │    (Retorna erro)│
│                  │    │                  │
│ samsungWallet/   │    │ noSamsungWallet/ │
│ └─ Clean.kt      │    │ └─ Stub.kt       │
└──────────────────┘    └──────────────────┘
         │                       │
         └───────────┬───────────┘
                     ▼
         ┌─────────────────────┐
         │  CÓDIGO COMPILADO   │
         │  (Apenas o          │
         │   necessário!)      │
         └─────────────────────┘
```

### Estrutura de Arquivos

```
android/src/
├── main/                          # Código comum (sempre compilado)
│   └── java/com/builders/wallet/
│       ├── googletapandpay/
│       │   ├── GoogleWalletContract.kt     ✅ Interface
│       │   ├── GoogleWalletModule.kt       ✅ Módulo RN
│       │   └── GoogleWalletMock.kt         ✅ Mock (para testes)
│       └── samsungpay/
│           ├── SamsungWalletContract.kt    ✅ Interface
│           ├── SamsungWalletModule.kt      ✅ Módulo RN
│           └── SamsungWalletMock.kt        ✅ Mock (para testes)
│
├── googleWallet/                  # ✅ Compilado SE GOOGLE_WALLET_ENABLED=true
│   └── java/com/builders/wallet/googletapandpay/
│       └── GoogleWalletImplementation.kt   # SEM reflexão! 🎉
│
├── noGoogleWallet/                # ✅ Compilado SE GOOGLE_WALLET_ENABLED=false
│   └── java/com/builders/wallet/googletapandpay/
│       └── GoogleWalletImplementation.kt   # Stub (retorna erros)
│
├── samsungWallet/                 # ✅ Compilado SE SAMSUNG_WALLET_ENABLED=true
│   └── java/com/builders/wallet/samsungpay/
│       ├── SamsungWalletImplementation.kt  # SEM reflexão! 🎉
│       ├── SerializableCard.kt             # SEM reflexão!
│       └── util/
│           ├── ErrorCode.kt                # Usa constantes do SDK
│           └── PartnerInfoHolder.kt        # SEM reflexão!
│
└── noSamsungWallet/               # ✅ Compilado SE SAMSUNG_WALLET_ENABLED=false
    └── java/com/builders/wallet/samsungpay/
        ├── SamsungWalletImplementation.kt  # Stub (retorna erros)
        ├── SerializableCard.kt             # Stub
        └── util/
            ├── ErrorCode.kt                # Stub (valores hardcoded)
            └── PartnerInfoHolder.kt        # Stub
```

### Cenários de Compilação

#### Cenário 1: Ambos os SDKs Habilitados
```properties
GOOGLE_WALLET_ENABLED=true
SAMSUNG_WALLET_ENABLED=true
```
✅ Compila `GoogleWalletImplementation` (código limpo, sem reflexão)  
✅ Compila `SamsungWalletImplementation` (código limpo, sem reflexão)  
✅ Requer: `com.google.android.gms:play-services-tapandpay` e `samsungpay_*.jar`

#### Cenário 2: Apenas Google Wallet
```properties
GOOGLE_WALLET_ENABLED=true
SAMSUNG_WALLET_ENABLED=false
```
✅ Compila `GoogleWalletImplementation`  
✅ Compila `SamsungWalletImplementation` (stub)  
✅ Requer apenas: `com.google.android.gms:play-services-tapandpay`

#### Cenário 3: Apenas Samsung Wallet
```properties
GOOGLE_WALLET_ENABLED=false
SAMSUNG_WALLET_ENABLED=true
```
✅ Compila `GoogleWalletImplementation` (stub)  
✅ Compila `SamsungWalletImplementation`  
✅ Requer apenas: `samsungpay_*.jar`

#### Cenário 4: Nenhum SDK (Mínimo)
```properties
GOOGLE_WALLET_ENABLED=false
SAMSUNG_WALLET_ENABLED=false
```
✅ Compila `GoogleWalletImplementation` (stub)  
✅ Compila `SamsungWalletImplementation` (stub)  
✅ Não requer nenhum SDK externo

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

### 🚀 Quick Start (1 minuto)

#### 1. Configure os SDKs

```bash
# Copie o arquivo de exemplo
cp gradle.properties.example gradle.properties

# Edite e configure
nano gradle.properties
```

```properties
# ===============================================
# CONFIGURAÇÃO DE WALLETS SDK
# ===============================================

# Google Wallet - Habilitar SDK (compila código limpo sem reflexão)
# true: Usa GoogleWalletImplementation (requer SDK do Google)
# false: Usa GoogleWalletStub (não requer SDK)
GOOGLE_WALLET_ENABLED=true

# Samsung Wallet - Habilitar SDK (compila código limpo sem reflexão)
# true: Usa SamsungWalletImplementation (requer SDK do Samsung)
# false: Usa SamsungWalletStub (não requer SDK)
SAMSUNG_WALLET_ENABLED=true

# ===============================================
# MODO MOCK (Para desenvolvimento/testes)
# ===============================================

# Use this property to enable or disable Google Wallet Mock mode.
# Set to true for development/testing, false for production.
GOOGLE_WALLET_USE_MOCK=false

# Google Wallet Mock API URL configuration
# Set this to configure the mock server URL for development
# Examples:
# - http://localhost:3000 (for local development)
# - http://10.0.2.2:3000 (for Android emulator)
# - http://192.168.1.100:3000 (for physical device on same network)
# Leave empty to use default fallback values without API calls
GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000

SAMSUNG_WALLET_USE_MOCK=false
# Samsung Wallet Mock API URL configuration
# Set this to configure the mock server URL for development
# Examples:
# - http://localhost:3000 (for local development)
# - http://10.0.2.2:3000 (for Android emulator)
# - http://192.168.1.100:3000 (for physical device on same network)
# Leave empty to use default fallback values without API calls
SAMSUNG_WALLET_MOCK_API_URL=http://localhost:3000
```

#### 2. Build

```bash
cd android
./gradlew clean build
```

#### 3. Pronto! 🎉

O sistema automaticamente:
- ✅ Compila código limpo (sem reflexão) para SDKs habilitados
- ✅ Compila stubs para SDKs desabilitados
- ✅ Inclui apenas dependências necessárias

### 📋 Configuração Detalhada

#### Android

##### 1. Configurar Google Pay SDK

**Baixar e Instalar o SDK:**

1. **Baixe o Google Pay Tap and Pay SDK**:
   - Acesse a [página oficial do Google Pay](https://developers.google.com/pay/issuers/apis/push-provisioning/android/releases)
   - Baixe a versão mais recente do SDK
   - Descompacte o arquivo baixado

2. **Estrutura de Pastas**:
   ```
   android/
   ├── libs/
   │   └── com/
   │       └── google/
   │           └── android/
   │               └── gms/
   │                   └── play-services-tapandpay/
   │                       ├── classes.jar
   │                       ├── res/
   │                       └── AndroidManifest.xml
   ```

3. **Configurar gradle.properties**:
   ```properties
   # android/gradle.properties
   GOOGLE_WALLET_ENABLED=true
   ```

##### 2. Configurar Samsung Pay SDK

**Baixar e Instalar o SDK:**

1. **Baixe o Samsung Pay SDK**:
   - Acesse o [Samsung Developer Portal](https://developer.samsung.com/samsung-pay)
   - Faça login e baixe o Samsung Pay SDK
   - O arquivo baixado geralmente vem como `SamsungPaySDK_<versão>.jar`

2. **Renomear e Posicionar**:
   ```bash
   # Renomeie o arquivo para o padrão esperado
   mv SamsungPaySDK_2.22.00.jar samsungpay_2.22.00.jar
   
   # Coloque na pasta libs do projeto
   cp samsungpay_2.22.00.jar android/libs/
   ```

3. **Estrutura de Pastas**:
   ```
   android/
   ├── libs/
   │   ├── samsungpay_2.22.00.jar
   │   └── com/
   │       └── google/
   │           └── android/
   │               └── gms/
   │                   └── play-services-tapandpay/
   ```

4. **Configurar gradle.properties**:
   ```properties
   # android/gradle.properties
   SAMSUNG_WALLET_ENABLED=true
   ```

##### 3. Configuração Completa do gradle.properties

```properties
# android/gradle.properties

# Configurações do React Native
android.useAndroidX=true
newArchEnabled=false
hermesEnabled=true

# Configurações de memória
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m

# ===============================================
# CONFIGURAÇÃO DE WALLETS SDK
# ===============================================

# Google Wallet - Habilitar SDK (compila código limpo sem reflexão)
GOOGLE_WALLET_ENABLED=true

# Samsung Wallet - Habilitar SDK (compila código limpo sem reflexão)
SAMSUNG_WALLET_ENABLED=true

# ===============================================
# MODO MOCK (Para desenvolvimento/testes)
# ===============================================

# Google Wallet Mock
GOOGLE_WALLET_USE_MOCK=false
GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000

# Samsung Wallet Mock
SAMSUNG_WALLET_USE_MOCK=false
SAMSUNG_WALLET_MOCK_API_URL=http://localhost:3000
```

##### 4. Estrutura Final de Pastas

```
react-native-builders-wallet/
├── android/
│   ├── libs/
│   │   ├── samsungpay_2.22.00.jar          # Samsung Pay SDK
│   │   └── com/
│   │       └── google/
│   │           └── android/
│   │               └── gms/
│   │                   └── play-services-tapandpay/
│   │                       ├── classes.jar
│   │                       ├── res/
│   │                       └── AndroidManifest.xml
│   ├── build.gradle                         # Configuração automática dos SDKs
│   └── gradle.properties                   # Flags de ativação
└── example/
    └── android/
        ├── libs/
        │   └── samsungpay_2.22.00.jar      # Cópia para o exemplo
        └── gradle.properties               # Configuração do exemplo
```

##### 5. Verificar se a Configuração Está Funcionando

**Teste de Build:**

```bash
# Na pasta da biblioteca
cd android
./gradlew build

# Você deve ver mensagens como:
# ================================================
#    CONFIGURAÇÃO DE WALLETS
# ================================================
# Google Wallet Enabled: true
# Samsung Wallet Enabled: true
# ================================================
# ✅ Usando GoogleWallet source set (com SDK)
# ✅ Usando SamsungWallet source set (com SDK)
# ✅ Google Play Services Tap and Pay incluído
# ✅ Samsung Pay SDK v2.22.00 incluído
# ================================================
# BUILD SUCCESSFUL
```

**Teste no App de Exemplo:**

```bash
# Na pasta do exemplo
cd example/android
./gradlew build

# Verificar se os SDKs foram incluídos
./gradlew dependencies | grep -E "(google|samsung)"
```

##### 6. Configurar AndroidManifest.xml

Adicione o intent filter para App2App:

```xml
<activity android:name=".MainActivity">
  <intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
  </intent-filter>
  <!-- Intent filter para capturar ativação de token -->
  <intent-filter>
    <action android:name="com.sua-empresa.seu-app.action.ACTIVATE_TOKEN"/>
    <category android:name="android.intent.category.DEFAULT"/>
  </intent-filter>
</activity>
```

### iOS

A biblioteca está preparada para iOS, mas atualmente foca no Android. O suporte completo ao iOS será adicionado em versões futuras.

## 📊 Comparação: Antes vs Depois

### Código com Reflexão (Antes) 😢

```kotlin
// Difícil de ler e manter
private fun call(instance: Any?, method: String, vararg args: Any?): Any? {
    val m = instance.javaClass.methods.firstOrNull { 
        it.name == method && it.parameterTypes.size == args.size 
    }
    return m?.invoke(instance, *args)
}

// Sem autocomplete, sem type-safety
val listener = proxy("com.google.android.gms.tasks.OnCompleteListener", mapOf(
    "onComplete" to { args: Array<out Any?> ->
        val completedTask = args?.get(0) as? Any
        val isSuccessfulMethod = completedTask.javaClass.getMethod("isSuccessful")
        // ...
    }
))
```

### Código Limpo (Depois) 🎉

```kotlin
// Fácil de ler e manter
override fun getSecureWalletInfo(promise: Promise) {
    // IDE autocomplete funciona!
    tapAndPayClient!!.activeWalletId.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val walletId = task.result
            promise.resolve(createResult(walletId))
        } else {
            promise.reject("ERROR", task.exception?.message)
        }
    }
}
```

### Comparação de Benefícios

| Aspecto | Antes (Reflexão) | Depois (Source Sets) |
|---------|------------------|---------------------|
| **Legibilidade** | ❌ Difícil de ler | ✅ Código limpo |
| **Performance** | ⚠️ Overhead de reflexão | ✅ Direto, sem overhead |
| **Debugging** | ❌ Difícil de debugar | ✅ Fácil de debugar |
| **IDE Support** | ❌ Sem autocomplete | ✅ Autocomplete completo |
| **Type Safety** | ❌ Erros em runtime | ✅ Erros em compile-time |
| **Manutenção** | ❌ Difícil | ✅ Fácil |
| **Tamanho do APK** | ⚠️ Inclui tudo | ✅ Apenas o necessário |
| **Build sem SDK** | ❌ Falha | ✅ Funciona com stubs |

## 🎯 Uso

### Módulos Específicos por Wallet

A biblioteca exporta módulos específicos para cada wallet, fornecendo acesso direto aos métodos dos SDKs nativos:

#### Google Pay

```javascript
import { GoogleWalletModule, GoogleWalletEventEmitter } from '@platformbuilders/wallet-bridge-react-native';

// Verificar disponibilidade do Google Pay
const isAvailable = await GoogleWalletModule.checkWalletAvailability();

// Obter informações do wallet
const walletInfo = await GoogleWalletModule.getSecureWalletInfo();

// Adicionar cartão ao Google Pay
const cardData = {
  address: {
    address1: 'Rua das Flores, 123',
    address2: 'Apto 45',
    countryCode: 'BR',
    locality: 'São Paulo',
    administrativeArea: 'SP',
    name: 'João Silva',
    phoneNumber: '+5511999999999',
    postalCode: '01234-567'
  },
  card: {
    opaquePaymentCard: 'eyJ0eXBlIjoiL0dvb2dsZV9QYXlfQ2FyZCIsInRva2VuIjoiZXhhbXBsZV90b2tlbl9kYXRhIn0=',
    network: GoogleWalletModule.getConstants().CARD_NETWORK_ELO,
    tokenServiceProvider: GoogleWalletModule.getConstants().TOKEN_PROVIDER_ELO,
    displayName: 'João Silva - Visa',
    lastDigits: '1234'
  }
};
const result = await GoogleWalletModule.addCardToWallet(cardData);

// Listar tokens existentes
const tokens = await GoogleWalletModule.listTokens();

// Verificar status de um token específico
const tokenStatus = await GoogleWalletModule.getTokenStatus(
  GoogleWalletModule.getConstants().TOKEN_PROVIDER_ELO,
  'token-id'
);

// Verificar se um cartão está tokenizado
const isTokenized = await GoogleWalletModule.isTokenized(
  '1234',
  GoogleWalletModule.getConstants().CARD_NETWORK_ELO,
  GoogleWalletModule.getConstants().TOKEN_PROVIDER_ELO
);

// Visualizar token específico e obter seus dados
const tokenData = await GoogleWalletModule.viewToken(
  GoogleWalletModule.getConstants().TOKEN_PROVIDER_ELO,
  'token-id-específico'
);

if (tokenData) {
  console.log('Token encontrado:', tokenData);
  console.log('Emissor:', tokenData.issuerName);
  console.log('Últimos 4 dígitos:', tokenData.fpanLastFour);
  console.log('Estado:', tokenData.tokenState);
} else {
  console.log('Token não encontrado');
}

// Criar carteira se necessário
const walletCreated = await GoogleWalletModule.createWalletIfNeeded();

// Obter environment
const environment = await GoogleWalletModule.getEnvironment();

// Obter constantes do módulo
const constants = GoogleWalletModule.getConstants();
console.log('ELO Provider:', constants.TOKEN_PROVIDER_ELO);
console.log('ELO Network:', constants.CARD_NETWORK_ELO);
```

#### Samsung Pay

```javascript
import { SamsungWalletModule } from '@platformbuilders/wallet-bridge-react-native';

// Verificar disponibilidade do Samsung Pay
const isAvailable = await SamsungWalletModule.checkWalletAvailability();

// Obter informações do wallet
const walletInfo = await SamsungWalletModule.getSecureWalletInfo();

// Adicionar cartão ao Samsung Pay
const cardData = {
  cardId: 'card-123',
  cardBrand: 'VISA',
  cardType: 'CREDIT',
  cardLast4Fpan: '1234',
  cardLast4Dpan: '5678',
  cardIssuer: 'Banco Exemplo',
  cardStatus: 'ACTIVE',
  isSamsungPayCard: true,
};
const result = await SamsungWalletModule.addCardToWallet(cardData);

// Listar tokens existentes
const tokens = await SamsungWalletModule.listTokens();

// Verificar status de um token específico
const tokenStatus = await SamsungWalletModule.getTokenStatus(
  'token-provider',
  'token-id'
);

// Verificar se um cartão está tokenizado
const isTokenized = await SamsungWalletModule.isTokenized(
  '1234',
  'CARD_NETWORK_VISA',
  'TOKEN_PROVIDER_VISA'
);

// Criar carteira se necessário
const walletCreated = await SamsungWalletModule.createWalletIfNeeded();

// Obter environment
const environment = await SamsungWalletModule.getEnvironment();

// Obter constantes
const constants = await SamsungWalletModule.getConstants();
```

### 🎨 Interface de Seleção Intuitiva

A biblioteca agora inclui modais de seleção para constantes e tipos, eliminando erros de digitação:

#### Seleção de Provider

```typescript
// 15 opções de providers baseadas nas constantes reais do Samsung Wallet
const providerOptions = [
  { value: constants.PROVIDER_VISA, label: 'Visa' },
  { value: constants.PROVIDER_MASTERCARD, label: 'Mastercard' },
  { value: constants.PROVIDER_AMEX, label: 'American Express' },
  { value: constants.PROVIDER_DISCOVER, label: 'Discover' },
  { value: constants.PROVIDER_ELO, label: 'Elo' },
  { value: constants.PROVIDER_PLCC, label: 'Private Label Credit Card' },
  { value: constants.PROVIDER_GIFT, label: 'Gift Card' },
  { value: constants.PROVIDER_LOYALTY, label: 'Loyalty Card' },
  { value: constants.PROVIDER_PAYPAL, label: 'PayPal' },
  { value: constants.PROVIDER_GEMALTO, label: 'Gemalto' },
  { value: constants.PROVIDER_NAPAS, label: 'NAPAS' },
  { value: constants.PROVIDER_MIR, label: 'MIR' },
  { value: constants.PROVIDER_PAGOBANCOMAT, label: 'PagoBANCOMAT' },
  { value: constants.PROVIDER_VACCINE_PASS, label: 'Vaccine Pass' },
  { value: constants.PROVIDER_MADA, label: 'MADA' },
];
```

#### Seleção de Card Type

```typescript
// 7 opções de tipos de cartão baseadas nas constantes reais
const cardTypeOptions = [
  { value: constants.CARD_TYPE_CREDIT, label: 'Crédito' },
  { value: constants.CARD_TYPE_DEBIT, label: 'Débito' },
  { value: constants.CARD_TYPE_CREDIT_DEBIT, label: 'Crédito/Débito' },
  { value: constants.CARD_TYPE_GIFT, label: 'Cartão Presente' },
  { value: constants.CARD_TYPE_LOYALTY, label: 'Fidelidade' },
  { value: constants.CARD_TYPE_TRANSIT, label: 'Trânsito' },
  { value: constants.CARD_TYPE_VACCINE_PASS, label: 'Passe de Vacinação' },
];
```

### App2App (Manual Provisioning)

Para fluxos de ativação de token via App2App:

```javascript
import { GoogleWalletModule, GoogleWalletEventEmitter } from '@platformbuilders/wallet-bridge-react-native';

// Ativar listener de intents
await GoogleWalletModule.setIntentListener();

// Escutar eventos de ativação de token
const eventEmitter = new GoogleWalletEventEmitter();
const removeListener = eventEmitter.addIntentListener((event) => {
  console.log('Intent recebido:', event);
  
  if (event.type === 'ACTIVATE_TOKEN') {
    // Verificar formato dos dados
    if (event.dataFormat === 'base64_decoded') {
      // Dados já decodificados automaticamente pelo nativo
      console.log('✅ Dados já decodificados automaticamente');
      const activationParams = JSON.parse(event.data);
      processTokenActivation(activationParams);
    } else if (event.dataFormat === 'raw') {
      // Dados em formato raw, decodificar manualmente
      const decodedData = atob(event.data);
      const activationParams = JSON.parse(decodedData);
      processTokenActivation(activationParams);
    }
    
    // Extrair dados de ativação
    const { panReferenceId, tokenReferenceId } = activationParams;
    console.log('PAN Reference ID:', panReferenceId);
    console.log('Token Reference ID:', tokenReferenceId);
  }
});

// Cleanup
removeListener();
```

### Definir Resultado da Ativação

Para retornar o resultado da ativação de token para o Google Wallet:

```javascript
import { GoogleWalletModule, GoogleActivationStatus } from '@platformbuilders/wallet-bridge-react-native';

// Definir resultado de ativação sem activationCode
await GoogleWalletModule.setActivationResult(GoogleActivationStatus.APPROVED);

// Definir resultado de ativação com activationCode
await GoogleWalletModule.setActivationResult(
  GoogleActivationStatus.APPROVED, 
  'ACTIVATION_CODE_12345'
);

// Outros status disponíveis
await GoogleWalletModule.setActivationResult(GoogleActivationStatus.DECLINED);
await GoogleWalletModule.setActivationResult(GoogleActivationStatus.FAILURE);

// Finalizar atividade e voltar para o app chamador
await GoogleWalletModule.finishActivity();
```

### Escolhendo o Módulo Correto

```javascript
import { GoogleWalletModule, SamsungWalletModule } from '@platformbuilders/wallet-bridge-react-native';

// Verificar qual wallet está disponível
const checkAvailableWallets = async () => {
  const wallets = [];
  
  if (GoogleWalletModule) {
    const isGoogleAvailable = await GoogleWalletModule.checkWalletAvailability();
    if (isGoogleAvailable) {
      wallets.push('Google Pay');
    }
  }
  
  if (SamsungWalletModule) {
    const isSamsungAvailable = await SamsungWalletModule.checkWalletAvailability();
    if (isSamsungAvailable) {
      wallets.push('Samsung Pay');
    }
  }
  
  return wallets;
};

// Usar o wallet disponível
const availableWallets = await checkAvailableWallets();
console.log('Wallets disponíveis:', availableWallets);

if (availableWallets.includes('Google Pay')) {
  // Usar Google Pay
  const result = await GoogleWalletModule.addCardToWallet(cardData);
} else if (availableWallets.includes('Samsung Pay')) {
  // Usar Samsung Pay
  const result = await SamsungWalletModule.addCardToWallet(cardData);
}
```

## 🧪 Modo Mock para Desenvolvimento

Para desenvolvimento sem SDKs reais, ative o modo mock:

```properties
# android/gradle.properties
GOOGLE_WALLET_USE_MOCK=true
SAMSUNG_WALLET_USE_MOCK=true
```

### Comportamento do Mock

- `checkWalletAvailability()`: Consulta servidor mock em tempo real (se configurado)
- `getSecureWalletInfo()`: Retorna dados simulados ou da API local
- `addCardToWallet()`: Valida dados e simula diferentes cenários baseados nos últimos dígitos
- `listTokens()`: Retorna 2 tokens simulados (Visa e Mastercard) ou da API local
- `getConstants()`: Retorna constantes corretas (ELO = 14/12, TOKEN_STATE_* = 1-6)
- **API Local**: Suporte completo para servidor mock local (configurável via gradle.properties)

### 🌍 Configuração via gradle.properties

O mock pode ser configurado para usar um servidor local através da propriedade `GOOGLE_WALLET_MOCK_API_URL` no arquivo `gradle.properties`:

```properties
# example/android/gradle.properties
# Configurar URL do servidor mock
GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000

# Para emulador Android (usar IP do host)
# GOOGLE_WALLET_MOCK_API_URL=http://10.0.2.2:3000

# Para dispositivo físico (usar IP da rede local)
# GOOGLE_WALLET_MOCK_API_URL=http://192.168.1.100:3000
```

**Comportamento**:
- **Se configurada**: O mock fará requisições HTTP para o servidor especificado
- **Se não configurada**: O mock usará apenas valores padrão simulados (sem requisições HTTP)

## 📚 API Reference

### Google Pay - Métodos Disponíveis

| Método | Descrição | Parâmetros | Retorna |
|--------|-----------|------------|---------|
| `checkWalletAvailability` | Verifica se o Google Pay está disponível | Nenhum | `Promise<boolean>` |
| `getSecureWalletInfo` | Retorna informações do Google Pay | Nenhum | `Promise<GoogleWalletData>` |
| `addCardToWallet` | Adiciona cartão ao Google Pay | `cardData: GooglePushTokenizeRequest` | `Promise<string>` |
| `listTokens` | Lista tokens existentes no Google Pay | Nenhum | `Promise<GoogleTokenInfo[]>` |
| `getTokenStatus` | Status de um token específico | `tokenServiceProvider: number, tokenReferenceId: string` | `Promise<GoogleTokenStatus>` |
| `isTokenized` | Verifica se cartão está tokenizado | `fpanLastFour: string, cardNetwork: number, tokenServiceProvider: number` | `Promise<boolean>` |
| `viewToken` | Abre Google Pay para visualizar token e retorna dados do token | `tokenServiceProvider: number, issuerTokenId: string` | `Promise<GoogleTokenInfo | null>` |
| `createWalletIfNeeded` | Cria carteira se necessário | Nenhum | `Promise<boolean>` |
| `getEnvironment` | Retorna environment atual | Nenhum | `Promise<string>` |
| `getConstants` | Retorna constantes do módulo | Nenhum | `GoogleWalletConstants` |
| `setIntentListener` | Ativa listener para App2App | Nenhum | `Promise<boolean>` |
| `removeIntentListener` | Remove listener de App2App | Nenhum | `Promise<boolean>` |
| `setActivationResult` | Define resultado da ativação de token | `status: string, activationCode?: string` | `Promise<boolean>` |
| `finishActivity` | Finaliza a atividade e volta para o app chamador | Nenhum | `Promise<boolean>` |

### Samsung Pay - Métodos Disponíveis

| Método | Descrição | Parâmetros | Retorna |
|--------|-----------|------------|---------|
| `checkWalletAvailability` | Verifica se o Samsung Pay está disponível | Nenhum | `Promise<boolean>` |
| `getSecureWalletInfo` | Retorna informações do Samsung Pay | Nenhum | `Promise<SamsungWalletData>` |
| `addCard` | Adiciona cartão ao Samsung Pay | `payload: string, issuerId: string, tokenizationProvider: string, cardType: string` | `Promise<SamsungCard>` |
| `listTokens` | Lista tokens existentes no Samsung Pay | Nenhum | `Promise<SamsungTokenInfoSimple[]>` |
| `getTokenStatus` | Status de um token específico | `tokenServiceProvider: number, tokenReferenceId: string` | `Promise<SamsungTokenStatus>` |
| `isTokenized` | Verifica se cartão está tokenizado | `fpanLastFour: string, cardNetwork: number, tokenServiceProvider: number` | `Promise<boolean>` |
| `viewToken` | Abre Samsung Pay para visualizar token | `tokenServiceProvider: number, issuerTokenId: string` | `Promise<boolean>` |
| `createWalletIfNeeded` | Cria carteira se necessário | Nenhum | `Promise<boolean>` |
| `getEnvironment` | Retorna environment atual | Nenhum | `Promise<string>` |
| `getConstants` | Retorna constantes do módulo | Nenhum | `Promise<SamsungWalletConstants>` |

### Tipos de Dados

#### Google Pay

```typescript
// Dados do cartão para Google Pay (estrutura correta)
interface GooglePushTokenizeRequest {
  address: {
    address1: string;
    address2?: string;
    countryCode: string;
    locality: string; // city
    administrativeArea: string; // state/province
    name: string;
    phoneNumber: string;
    postalCode: string;
  };
  card: {
    opaquePaymentCard: string; // Base64 encoded
    network: number; // GoogleCardNetwork
    tokenServiceProvider: number; // GoogleTokenProvider
    displayName: string;
    lastDigits: string; // Exatamente 4 dígitos
  };
}

// Informações do wallet
interface GoogleWalletData {
  deviceID: string;
  walletAccountID: string;
}

// Status do token
interface GoogleTokenStatus {
  tokenState: number;
  isSelected: boolean;
}

// Informações do token (completa)
interface GoogleTokenInfo {
  issuerTokenId: string;
  issuerName: string;
  fpanLastFour: string;
  dpanLastFour: string;
  tokenServiceProvider: number;
  network: number;
  tokenState: number;
  isDefaultToken: boolean;
  portfolioName: string;
}

// Constantes do Google Wallet
interface GoogleWalletConstants {
  SDK_NAME: string;
  CARD_NETWORK_ELO: number;  // 12
  TOKEN_PROVIDER_ELO: number;  // 14
  TOKEN_STATE_UNTOKENIZED: number;  // 1
  TOKEN_STATE_PENDING: number;  // 2
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION: number;  // 3
  TOKEN_STATE_SUSPENDED: number;  // 4
  TOKEN_STATE_ACTIVE: number;  // 5
  TOKEN_STATE_FELICA_PENDING_PROVISIONING: number;  // 6
}

// Status de ativação
enum GoogleActivationStatus {
  APPROVED = 'approved',
  DECLINED = 'declined',
  FAILURE = 'failure',
}

// Formato dos dados de intent
enum GoogleWalletDataFormat {
  BASE64_DECODED = 'base64_decoded',
  RAW = 'raw',
}

// Evento de intent do Google Wallet
interface GoogleWalletIntentEvent {
  action: string;
  type: GoogleWalletIntentType;
  data?: string; // Dados decodificados (string normal)
  dataFormat?: GoogleWalletDataFormat;
  callingPackage?: string;
  originalData?: string; // Dados originais em base64
  error?: string;
  extras?: Record<string, any>;
}
```

#### Samsung Pay

```typescript
// Parâmetros para adicionar cartão ao Samsung Pay
interface SamsungAddCardParams {
  payload: string;                    // Payload de tokenização do cartão
  issuerId: string;                   // ID do emissor do cartão
  tokenizationProvider: string;       // Provedor de tokenização (VISA, MASTERCARD, etc.)
  cardType: string;                   // Tipo do cartão (CREDIT, DEBIT, etc.)
}

// Dados do cartão retornado pelo Samsung Pay
interface SamsungCard {
  // Campos básicos do Card
  cardId: string;
  cardStatus: string;
  cardBrand: string;

  // Campos do cardInfo Bundle (Samsung Pay específicos)
  last4FPan?: string;
  last4DPan?: string;
  app2AppPayload?: string;
  cardType?: string;
  issuerName?: string;
  isDefaultCard?: string;
  deviceType?: string;
  memberID?: string;
  countryCode?: string;
  cryptogramType?: string;
  requireCpf?: string;
  cpfHolderName?: string;
  cpfNumber?: string;
  merchantRefId?: string;
  transactionType?: string;

  // Campos de compatibilidade
  last4?: string;
  tokenizationProvider?: string | number;
  network?: string | number;
  displayName?: string;
}

// Informações do wallet
interface SamsungWalletData {
  deviceID: string;
  walletAccountID: string;
  userInfo: {
    userId: string;
    userName: string;
    userEmail: string;
    userPhone: string;
  };
}

// Status do token
interface SamsungTokenStatus {
  tokenState: number;
  isSelected: boolean;
}

// Informações do token
interface SamsungTokenInfoSimple {
  cardId: string;
  cardLast4Fpan: string;
  cardIssuer: string;
  cardStatus: string;
  cardBrand: string;
}
```

#### Tipos Comuns

```typescript
// Status do cartão
enum CardStatus {
  NOT_FOUND = 'not found',
  ACTIVE = 'active',
  REQUIRE_AUTHORIZATION = 'requireAuthorization',
  PENDING = 'pending',
  SUSPENDED = 'suspended',
  DEACTIVATED = 'deactivated',
}

// Dados básicos do wallet
interface WalletData {
  deviceID: string;
  walletAccountID: string;
}
```

## 🚨 Códigos de Erro do Google Wallet

A biblioteca suporta todos os códigos de erro oficiais do Google Wallet SDK:

### Códigos de Erro Comuns

| Código | Descrição | Quando Ocorre |
|--------|-----------|---------------|
| **15002** | Nenhuma carteira ativa encontrada | Quando não há carteira Google Pay configurada |
| **15003** | Token não encontrado na carteira ativa | Quando o token especificado não existe |
| **15004** | Token encontrado mas em estado inválido | Quando o token existe mas não pode ser usado |
| **15005** | Falha na verificação de compatibilidade do dispositivo | Quando o dispositivo não é compatível |
| **15009** | Calling package not verified | Quando o app não está verificado pelo Google |

### Tratamento de Erros

```javascript
import { GoogleWalletModule } from '@platformbuilders/wallet-bridge-react-native';

try {
  const tokenStatus = await GoogleWalletModule.getTokenStatus(
    GoogleWalletModule.getConstants().TOKEN_PROVIDER_ELO,
    'token-id'
  );
  console.log('Status do token:', tokenStatus);
} catch (error) {
  console.error('Erro ao obter status do token:', error);
  
  // Verificar código de erro específico
  if (error.code === 'CALLING_PACKAGE_NOT_VERIFIED') {
    console.log('App não está verificado pelo Google');
  } else if (error.code === 'TOKEN_NOT_FOUND') {
    console.log('Token não encontrado na carteira');
  } else if (error.code === 'NO_ACTIVE_WALLET') {
    console.log('Nenhuma carteira ativa encontrada');
  }
}
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
│   ├── 📁 src/
│   │   ├── main/                    # Código comum (sempre compilado)
│   │   ├── googleWallet/            # Compilado SE GOOGLE_WALLET_ENABLED=true
│   │   ├── noGoogleWallet/          # Compilado SE GOOGLE_WALLET_ENABLED=false
│   │   ├── samsungWallet/           # Compilado SE SAMSUNG_WALLET_ENABLED=true
│   │   └── noSamsungWallet/         # Compilado SE SAMSUNG_WALLET_ENABLED=false
│   └── build.gradle                 # Configuração de source sets condicionais
├── 📁 ios/                          # Código nativo iOS
├── 📁 example/                      # App de exemplo
│   ├── 📁 src/
│   │   └── App.tsx                  # Exemplo completo de uso
│   ├── 📁 android/                  # Projeto Android de exemplo
│   └── 📁 ios/                      # Projeto iOS de exemplo
├── 📁 google-wallet-app-mock/       # App mock para testes
│   └── 📁 app/
│       └── 📁 src/main/
│           └── MainActivity.kt      # Simulador App2App
├── 📁 lib/                          # Build output
├── 📄 package.json                  # Configuração do projeto
├── 📄 BuildersWallet.podspec        # Configuração iOS
├── 📄 gradle.properties.example     # Exemplo de configuração
└── 📄 README.md                     # Este arquivo
```

## 🧪 App de Mock - google-wallet-app-mock

Um aplicativo Android que simula o Google Wallet para facilitar os testes do fluxo App2App durante o desenvolvimento da biblioteca BuildersWallet.

### 🎯 Propósito

Este app mock é essencial para testar o fluxo de **Manual Provisioning** do Google Pay sem depender do Google Play Services real. Ele simula o comportamento do Google Wallet ao enviar intents para ativar tokens em aplicativos de terceiros.

### 🚀 Funcionalidades

- **Simulação App2App**: Simula o envio de intents de ativação de token
- **Dados Realistas**: Envia dados em base64 com estrutura similar ao Google Wallet real
- **Interface Simples**: UI minimalista para facilitar os testes
- **Alertas Visuais**: Mostra resultado da operação com AlertDialog
- **Logs Detalhados**: Logs completos para debug e monitoramento
- **Package Mock**: Usa `com.google.android.gms_mock` para simular o Google Play Services
- **API Moderna**: Usa ActivityResultLauncher (sem deprecated warnings)

### 📱 Como Funciona

#### 1. Simulação de Intent
O app envia um intent com:
- **Action**: `com.sua-empresa.seu-app.action.ACTIVATE_TOKEN`
- **Package**: `com.sua-empresa.seu-app`
- **Dados**: Base64 com informações de ativação de token

#### 2. Dados Simulados
Os dados enviados contêm:
```json
{
  "panReferenceId": "PAN_1703048000000_1234",
  "tokenReferenceId": "TOKEN_1703048000000_12345"
}
```

**Nota**: Os IDs são gerados dinamicamente com timestamp atual e números aleatórios para simular dados únicos a cada execução. A estrutura foi simplificada para focar nos campos essenciais.

#### 3. Fluxo de Teste
1. Abra o app mock
2. Clique em "Simular App 2 App"
3. O app tentará abrir seu aplicativo principal
4. Seu app receberá o intent com os dados simulados
5. O app mock aguarda o resultado da ativação
6. **Alerta visual** é exibido com o resultado da operação

### 🛠️ Instalação e Uso

#### Pré-requisitos
- Android Studio
- Android SDK 23+ (Android 6.0+)
- Dispositivo Android ou emulador

#### 1. Build do App

```bash
cd google-wallet-app-mock

# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease
```

#### 2. Instalação

```bash
# Instalar no dispositivo/emulador
adb install app/build/outputs/apk/debug/app-debug.apk

# Ou instalar via Android Studio
# Abra o projeto no Android Studio e clique em "Run"
```

#### 3. Executar Testes

1. **Instale o app mock** no dispositivo
2. **Instale seu app principal** (com a biblioteca BuildersWallet)
3. **Abra o app mock**
4. **Clique em "Simular App 2 App"**
5. **Verifique os logs** para acompanhar o fluxo

### 📋 Configuração do App Principal

Para que seu app principal receba os intents do mock, configure o `AndroidManifest.xml`:

```xml
<activity android:name=".MainActivity">
  <intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
  </intent-filter>
  
  <!-- Intent filter para capturar ativação de token -->
  <intent-filter>
    <action android:name="com.sua-empresa.seu-app.action.ACTIVATE_TOKEN"/>
    <category android:name="android.intent.category.DEFAULT"/>
  </intent-filter>
</activity>
```

### 🎨 Alertas Visuais

O app mock exibe alertas visuais para mostrar o resultado das operações:

#### Tipos de Alerta

##### ✅ **Sucesso (RESULT_OK)**
- **Título**: "✅ Sucesso"
- **Mensagem**: "App Pefisa retornou com sucesso!\n\nCódigo: 0\nDados: [extras]"
- **Cor**: Verde (Material Design)

##### ⚠️ **Cancelado (RESULT_CANCELED)**
- **Título**: "⚠️ Cancelado"
- **Mensagem**: "App Pefisa foi cancelado pelo usuário.\n\nCódigo: 1"
- **Cor**: Laranja (Material Design)

##### ❓ **Resultado Inesperado**
- **Título**: "⚠️ Resultado Inesperado"
- **Mensagem**: "App Pefisa retornou com código inesperado.\n\nCódigo: [código]"
- **Cor**: Laranja (Material Design)

### 📊 Logs e Debug

#### Logs do App Mock
```bash
# Filtrar logs do app mock
adb logcat | grep "GoogleWalletMock"

# Logs específicos
adb logcat | grep "🚀\|✅\|❌\|⚠️"
```

#### Logs do App Principal
```bash
# Filtrar logs do seu app
adb logcat | grep "com.sua-empresa.seu-app"

# Logs da biblioteca BuildersWallet
adb logcat | grep "BuildersWallet\|GoogleWallet"
```

### 🧪 Cenários de Teste

#### 1. Cenário de Sucesso
- App mock envia intent
- App principal recebe e processa
- Retorna `RESULT_OK`
- **Alerta exibido**: "✅ Sucesso" com detalhes do resultado

#### 2. Cenário de Erro
- App mock envia intent com dados inválidos
- App principal retorna erro
- **Alerta exibido**: "⚠️ Resultado Inesperado" com código de erro

#### 3. Cenário de Timeout
- App mock envia intent
- App principal não responde
- App mock aguarda timeout
- **Alerta exibido**: "⚠️ Resultado Inesperado" com código de timeout

#### 4. Cenário de Cancelamento
- App mock envia intent
- Usuário cancela no app principal
- App mock recebe `RESULT_CANCELED`
- **Alerta exibido**: "⚠️ Cancelado" com informações do cancelamento

### 🔍 Troubleshooting

#### App Mock não consegue abrir o app principal
- Verifique se o package name está correto
- Confirme se o intent filter está configurado
- Verifique se o app principal está instalado

#### Dados não chegam no app principal
- Verifique se o `EXTRA_TEXT` está sendo enviado
- Confirme se o listener de intent está ativo
- Verifique os logs de ambos os apps

#### App principal não responde
- Verifique se o `ActivityResultLauncher` está implementado
- Confirme se o `setResult` está sendo chamado
- Verifique se a activity está sendo finalizada
- **Novo**: O app mock agora usa API moderna sem deprecated warnings

### 📝 Exemplo de Uso Completo

```kotlin
// No seu app principal
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar listener de intent
        setupIntentListener()
    }
    
    private fun setupIntentListener() {
        // Ativar listener da biblioteca BuildersWallet
        GoogleWallet.setIntentListener()
        
        // Configurar EventEmitter
        val eventEmitter = GoogleWalletEventEmitter()
        eventEmitter.addIntentListener { event ->
            when (event.type) {
                "ACTIVATE_TOKEN" -> {
                    // Decodificar dados base64
                    val decodedData = atob(event.data)
                    val activationParams = JSON.parse(decodedData)
                    
                    // Extrair panReferenceId e tokenReferenceId
                    val panReferenceId = activationParams.panReferenceId
                    val tokenReferenceId = activationParams.tokenReferenceId
                    
                    Log.d("MainActivity", "📋 PAN Reference ID: $panReferenceId")
                    Log.d("MainActivity", "📋 Token Reference ID: $tokenReferenceId")
                    
                    // Processar ativação
                    processTokenActivation(activationParams)
                }
            }
        }
    }
    
    private fun processTokenActivation(params: Any) {
        // Sua lógica de ativação aqui
        // ...
        
        // Retornar resultado para o app mock
        setResult(Activity.RESULT_OK)
        finish()
    }
}
```

**Nota**: Este app mock é destinado apenas para desenvolvimento e testes. Não deve ser usado em produção.

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

#### Google Pay
- ✅ Verificação de disponibilidade do Google Pay
- ✅ Criação de carteira Google Wallet
- ✅ Obtenção de informações do wallet
- ✅ Adição de cartão com OPC personalizado
- ✅ Listagem de tokens existentes
- ✅ Verificação de status de token específico
- ✅ Verificação se cartão está tokenizado
- ✅ Visualização de token no Google Pay
- ✅ Obtenção de environment (PROD/SANDBOX/DEV)
- ✅ Listener de intents App2App
- ✅ Decodificação de dados base64
- ✅ Tratamento de erros detalhado com códigos específicos
- ✅ Códigos de erro específicos do Google Wallet (15002, 15003, 15004, 15005, 15009)
- ✅ Definição de resultado de ativação de token

#### Samsung Pay
- ✅ Verificação de disponibilidade do Samsung Pay
- ✅ Obtenção de informações do wallet
- ✅ Adição de cartão ao Samsung Pay
- ✅ Listagem de tokens existentes
- ✅ Verificação de status de token
- ✅ Obtenção de constantes do módulo (GoogleWalletConstants)
- ✅ **Modais de seleção** para providers e tipos de cartão

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

A biblioteca usa módulos específicos para cada wallet:

1. **GoogleWalletModule**: Módulo dedicado para Google Pay
   - Interface: `GoogleWalletSpec`
   - EventEmitter: `GoogleWalletEventEmitter`
   - Tipos: `GoogleWalletData`, `GooglePushTokenizeRequest`, etc.

2. **SamsungWalletModule**: Módulo dedicado para Samsung Pay
   - Interface: `SamsungWalletSpec`
   - Tipos: `SamsungWalletData`, `SamsungCard`, `SamsungAddCardParams`, etc.

3. **Bridge Nativa**: Ponte direta entre React Native e SDKs nativos
   - Sem abstrações desnecessárias
   - Acesso direto aos métodos dos SDKs

4. **TypeScript**: Tipagem completa para cada módulo
   - Enums para constantes
   - Interfaces específicas para cada wallet
   - Tipos comuns reutilizáveis

5. **Mock Support**: Modo de desenvolvimento sem SDKs reais
   - Simulação de respostas
   - Logs detalhados para debug

## 🐛 Troubleshooting

### Problemas Comuns

#### 1. **SDK não encontrado**

**Google Pay SDK**:
```bash
# Verificar se a pasta existe
ls -la android/libs/com/google/android/gms/play-services-tapandpay/

# Deve conter:
# - classes.jar
# - res/ (pasta com recursos)
# - AndroidManifest.xml
```

**Samsung Pay SDK**:
```bash
# Verificar se o JAR existe
ls -la android/libs/samsungpay_*.jar

# Deve mostrar algo como:
# samsungpay_2.22.00.jar
```

**Soluções**:
- Verifique se os SDKs estão na pasta correta
- Confirme as configurações no `gradle.properties`
- Execute `./gradlew clean` e tente novamente

#### 2. **Build falha**

**Erro de dependência não encontrada**:
```bash
# Limpar cache do Gradle
./gradlew clean
rm -rf ~/.gradle/caches/

# Rebuild completo
./gradlew build
```

**Erro de versão do SDK**:
- Verifique se a versão do Android SDK é compatível
- Confirme se o `compileSdkVersion` está correto

#### 3. **Configuração incorreta do gradle.properties**

**Verificar configurações**:
```properties
# android/gradle.properties
GOOGLE_WALLET_ENABLED=true
SAMSUNG_WALLET_ENABLED=true
GOOGLE_WALLET_USE_MOCK=false
```

**Logs de build**:
```bash
# Durante o build, procure por:
✅ Google Play Services Tap and Pay incluído
✅ Samsung Pay SDK encontrado: samsungpay_2.22.00.jar
```

#### 4. **App2App não funciona**

**Verificar intent filter**:
```xml
<!-- AndroidManifest.xml -->
<intent-filter>
  <action android:name="com.sua-empresa.seu-app.action.ACTIVATE_TOKEN"/>
  <category android:name="android.intent.category.DEFAULT"/>
</intent-filter>
```

**Verificar package name**:
- Confirme se o package name está correto
- Teste com o app mock do Google Wallet

#### 5. **Mock não funciona**

**Verificar configuração**:
```properties
# android/gradle.properties
GOOGLE_WALLET_USE_MOCK=true
```

**Rebuild necessário**:
```bash
# Limpar e rebuild
./gradlew clean
./gradlew build
```

#### 6. **Problemas de Permissões**

**Verificar permissões no AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### 7. **Debug de Build**

**Logs detalhados**:
```bash
# Build com logs detalhados
./gradlew build --info

# Verificar dependências
./gradlew dependencies

# Verificar configuração
./gradlew properties
```

#### 8. **Problemas Específicos do Samsung Pay**

**JAR não encontrado**:
```bash
# Verificar se o arquivo está no local correto
find . -name "samsungpay_*.jar"

# Deve retornar:
# ./android/libs/samsungpay_2.22.00.jar
```

**Versão incorreta**:
- Certifique-se de que o arquivo segue o padrão `samsungpay_<versão>.jar`
- A versão será detectada automaticamente pelo build.gradle

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