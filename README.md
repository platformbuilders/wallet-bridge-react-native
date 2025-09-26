# @platformbuilders/wallet-bridge-react-native

[![npm version](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native.svg)](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Uma biblioteca React Native que facilita a integração com carteiras digitais (Google Pay, Samsung Pay). Atua como uma ponte (bridge) que se conecta aos SDKs nativos de cada carteira, fornecendo módulos prontos para React Native com os principais métodos para fluxos de Push Provisioning e Manual Provisioning.

## 🚀 Características

- **Módulos Específicos**: Módulos dedicados para Google Pay e Samsung Pay
- **SDK Nativo Direto**: Acesso direto aos métodos dos SDKs nativos
- **Métodos Principais**: Foco nos métodos essenciais para Push e Manual Provisioning
- **Bridge Simplificada**: Ponte direta entre React Native e SDKs nativos
- **App2App Support**: Suporte completo para fluxos de ativação de token
- **Decodificação Automática**: Dados base64 decodificados automaticamente pelo nativo
- **Validação Robusta**: Validação completa de dados de entrada
- **Tratamento de Erros**: Códigos de erro específicos e mensagens claras
- **Mock Mode**: Modo de desenvolvimento para testes sem SDKs reais
- **TypeScript**: Tipagem completa para melhor experiência de desenvolvimento

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

##### Baixar e Instalar o SDK

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
   includeGooglePay=true
   ```

##### Verificação da Instalação

O build.gradle detecta automaticamente se o SDK está instalado:
```bash
# Durante o build, você verá:
✅ Google Play Services Tap and Pay incluído
# ou
⚠️ Google Play Services Tap and Pay não incluído (defina includeGooglePay=true para incluir)
```

#### 2. Configurar Samsung Pay SDK

##### Baixar e Instalar o SDK

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
   enableSamsungPay=true
   ```

##### Verificação da Instalação

O build.gradle detecta automaticamente o JAR do Samsung Pay:
```bash
# Durante o build, você verá:
✅ Samsung Pay SDK encontrado: samsungpay_2.22.00.jar
✅ Versão detectada: 2.22.00
✅ Samsung Pay SDK v2.22.00 incluído de: /caminho/para/samsungpay_2.22.00.jar
# ou
⚠️ Nenhum arquivo samsungpay_*.jar encontrado em: /caminho/para/libs
```

#### 3. Configuração Completa do gradle.properties

```properties
# android/gradle.properties

# Configurações do React Native
android.useAndroidX=true
newArchEnabled=false
hermesEnabled=true

# Configurações de memória
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m

# Configurações dos SDKs de Wallet
includeGooglePay=true
enableSamsungPay=true

# Modo Mock (opcional - para desenvolvimento)
GOOGLE_WALLET_USE_MOCK=false
```

#### 4. Estrutura Final de Pastas

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

#### 5. Verificar se a Configuração Está Funcionando

##### Teste de Build

```bash
# Na pasta da biblioteca
cd android
./gradlew build

# Você deve ver mensagens como:
# ✅ Google Play Services Tap and Pay incluído
# ✅ Samsung Pay SDK encontrado: samsungpay_2.22.00.jar
# ✅ Versão detectada: 2.22.00
# ✅ Samsung Pay SDK v2.22.00 incluído de: /caminho/para/samsungpay_2.22.00.jar
```

##### Teste no App de Exemplo

```bash
# Na pasta do exemplo
cd example/android
./gradlew build

# Verificar se os SDKs foram incluídos
./gradlew dependencies | grep -E "(google|samsung)"
```

##### Verificação de Dependências

```bash
# Verificar dependências do Google Pay
./gradlew dependencies | grep "play-services-tapandpay"

# Verificar dependências do Samsung Pay
./gradlew dependencies | grep "samsungpay"
```

##### Teste de Funcionalidade

```javascript
// No seu app React Native
import { NativeModules } from 'react-native';

const { BuildersWallet } = NativeModules;

// Verificar se os módulos estão disponíveis
console.log('BuildersWallet disponível:', !!BuildersWallet);

// Verificar wallets disponíveis
const availableWallets = await BuildersWallet.getAvailableWallets();
console.log('Wallets disponíveis:', availableWallets);
```

#### 6. Configurar AndroidManifest.xml

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

### Melhorias no Fluxo de Adicionar Cartão

A biblioteca foi otimizada para seguir as melhores práticas do Push Provisioning do Google Pay:

#### ✅ **Estrutura de Dados Corrigida**
- **Antes**: Estrutura plana com campos misturados
- **Depois**: Estrutura hierárquica com `address` e `card` separados
- **Benefício**: Compatibilidade total com o SDK oficial do Google Pay

#### ✅ **Validação Robusta**
- Validação de campos obrigatórios (`opaquePaymentCard`, `displayName`, `lastDigits`)
- Verificação de formato base64 para `opaquePaymentCard`
- Validação de `lastDigits` (deve ter exatamente 4 dígitos)
- Códigos de erro específicos para cada tipo de problema

#### ✅ **Decodificação Automática de Intents**
- Dados base64 decodificados automaticamente pelo nativo
- Fallback para decodificação manual quando necessário
- Informações completas sobre o formato dos dados
- Dados originais preservados para referência

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
├── 📁 google-wallet-app-mock/          # App mock para testes
│   └── 📁 app/
│       └── 📁 src/main/
│           └── MainActivity.kt      # Simulador App2App
├── 📁 lib/                          # Build output
├── 📄 package.json                  # Configuração do projeto
├── 📄 BuildersWallet.podspec        # Configuração iOS
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

## 🔧 Modo Mock para Desenvolvimento

Para desenvolvimento sem SDKs reais, ative o modo mock:

```properties
# android/gradle.properties
GOOGLE_WALLET_USE_MOCK=true
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

### 📋 Configuração Detalhada

#### Propriedade Disponível

##### `GOOGLE_WALLET_MOCK_API_URL`

**Descrição**: URL do servidor mock local para desenvolvimento  
**Tipo**: String  
**Padrão**: `http://localhost:3000`  
**Obrigatória**: Não  
**Arquivo**: `example/android/gradle.properties`

#### Exemplos de Uso

```properties
# Desenvolvimento local
GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000

# Servidor em IP específico
GOOGLE_WALLET_MOCK_API_URL=http://192.168.1.100:3000

# Para emulador Android (usar IP do host)
GOOGLE_WALLET_MOCK_API_URL=http://10.0.2.2:3000

# Servidor HTTPS
GOOGLE_WALLET_MOCK_API_URL=https://mock-api.example.com
```

#### Configuração por Ambiente

##### Desenvolvimento Local
```properties
# example/android/gradle.properties
GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000
```

##### Emulador Android
```properties
# example/android/gradle.properties
GOOGLE_WALLET_MOCK_API_URL=http://10.0.2.2:3000
```

##### Dispositivo Físico
```properties
# example/android/gradle.properties
GOOGLE_WALLET_MOCK_API_URL=http://192.168.1.100:3000
```

#### Configuração no Projeto

##### Android Studio

1. Abra o projeto no Android Studio
2. Navegue até `example/android/gradle.properties`
3. Adicione ou modifique a linha:
   ```properties
   GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000
   ```

##### Gradle

```gradle
// android/build.gradle
android {
    defaultConfig {
        // Configuração da URL da API Mock
        buildConfigField "String", "GOOGLE_WALLET_MOCK_API_URL", 
          project.hasProperty('GOOGLE_WALLET_MOCK_API_URL') ? 
            "\"${project.property('GOOGLE_WALLET_MOCK_API_URL')}\"" : 
            "\"\""
    }
}
```

**Como Funciona**:
- A propriedade do `gradle.properties` é automaticamente convertida em `BuildConfig.GOOGLE_WALLET_MOCK_API_URL`
- O código Kotlin acessa via `BuildConfig.GOOGLE_WALLET_MOCK_API_URL`
- Se não configurado, retorna string vazia (usa valores padrão)

#### Troubleshooting

##### Problema: Mock não conecta com servidor

**Sintomas**:
- Logs mostram "API URL não configurada"
- Apenas valores padrão são retornados

**Soluções**:
1. Verificar se a propriedade está configurada no `gradle.properties`:
   ```properties
   GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000
   ```

2. Verificar se o servidor está rodando:
   ```bash
   curl http://localhost:3000/health
   ```

3. Verificar logs do Android:
   ```bash
   adb logcat | grep "GoogleWalletMock"
   ```

##### Problema: URL incorreta no emulador

**Sintomas**:
- Erro de conexão no emulador
- Servidor funciona no host mas não no emulador

**Solução**:
```properties
# example/android/gradle.properties
# Usar IP do host para emulador
GOOGLE_WALLET_MOCK_API_URL=http://10.0.2.2:3000
```

##### Problema: Propriedade não é carregada

**Sintomas**:
- Propriedade configurada mas não é detectada
- Logs mostram "API URL não configurada"

**Soluções**:
1. Verificar se o arquivo `gradle.properties` está no local correto
2. Verificar se o `buildConfigField` está configurado no `build.gradle`
3. Limpar cache do Gradle: `./gradlew clean`
4. Rebuild do projeto: `yarn android`
5. Verificar se o `BuildConfig` foi gerado corretamente

### 🌐 API Mock Local

Para desenvolvimento avançado, a biblioteca suporta um servidor mock local que simula o comportamento real do Google Wallet:

#### Configuração Rápida
```bash
# 1. Criar servidor Express.js
mkdir google-wallet-mock-server
cd google-wallet-mock-server
npm init -y
npm install express cors morgan

# 2. Criar server.js (veja API_MOCK_EXAMPLES.md para código completo)
# 3. Iniciar servidor
node server.js

# 4. Configurar propriedade no gradle.properties
# Adicionar em example/android/gradle.properties:
# GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000

# 5. Testar
curl http://localhost:3000/health
```

#### Funcionalidades da API Mock
- **Endpoints Completos**: Todos os métodos do Google Wallet
- **Cenários de Erro**: Simulação de diferentes tipos de erro
- **Códigos de Erro Específicos**: Suporte completo aos códigos 15002, 15003, 15004, 15005, 15009
- **Dados Realistas**: Respostas baseadas em dados reais do Google Pay
- **Logs Detalhados**: Monitoramento completo das requisições
- **Fallback Automático**: Se API falhar, usa valores padrão

#### Exemplos de Uso
```bash
# Verificar disponibilidade
curl http://localhost:3000/wallet/availability

# Listar tokens
curl http://localhost:3000/wallet/tokens

# Adicionar cartão (sucesso)
curl -X POST http://localhost:3000/wallet/add-card \
  -H "Content-Type: application/json" \
  -d '{"address": {...}, "card": {"lastDigits": "1234", ...}}'

# Adicionar cartão (erro simulado)
curl -X POST http://localhost:3000/wallet/add-card \
  -H "Content-Type: application/json" \
  -d '{"address": {...}, "card": {"lastDigits": "0000", ...}}'

# Testar status do token (sucesso)
curl "http://localhost:3000/wallet/token/status?provider=1&refId=abc123"

# Testar status do token (erro 15009 - calling package não verificado)
curl "http://localhost:3000/wallet/token/status?provider=1&refId=abc123-unverified"

# Testar status do token (erro 15003 - token não encontrado)
curl "http://localhost:3000/wallet/token/status?provider=1&refId=abc123-not_found"
```

Para documentação completa da API mock, consulte [API_MOCK_EXAMPLES.md](API_MOCK_EXAMPLES.md).

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
| `addCardToWallet` | Adiciona cartão ao Samsung Pay | `cardData: SamsungCardData` | `Promise<string>` |
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
// Dados do cartão para Samsung Pay
interface SamsungCardData {
  cardId: string;
  cardBrand: 'VISA' | 'MASTERCARD' | 'AMEX' | 'DISCOVER' | 'JCB' | 'ELO';
  cardType: 'CREDIT' | 'DEBIT' | 'PREPAID';
  cardLast4Fpan: string;
  cardLast4Dpan: string;
  cardIssuer: string;
  cardStatus: 'ACTIVE' | 'PENDING' | 'SUSPENDED' | 'DEACTIVATED' | 'NOT_FOUND';
  isSamsungPayCard: boolean;
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
   - Tipos: `SamsungWalletData`, `SamsungCardData`, etc.

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
includeGooglePay=true
enableSamsungPay=true
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