# Módulos de Wallet - BuildersWallet

Esta biblioteca agora suporta múltiplos provedores de wallet (Google Pay, Samsung Pay, etc.) de forma modular e opcional, utilizando os módulos existentes através de adapters.

## Estrutura dos Módulos

### 1. Interface Comum (`WalletModuleInterface`)
Define os métodos que todos os módulos de wallet devem implementar.

### 2. Módulos Existentes (em suas respectivas pastas)
- **`googletapandpay/GoogleTapAndPayModule`**: Módulo original do Google Pay
- **`samsungpay/SamsungPayModule`**: Módulo original do Samsung Pay

### 3. Adapters (Bridge Pattern)
- **`GoogleTapAndPayAdapter`**: Adapter que implementa `WalletModuleInterface` para Google Pay
- **`SamsungPayAdapter`**: Adapter que implementa `WalletModuleInterface` para Samsung Pay
- **`StubWalletModule`**: Módulo fallback quando nenhum SDK está disponível

### 4. Factory Pattern (`WalletModuleFactory`)
Responsável por detectar quais SDKs estão disponíveis e instanciar os adapters apropriados.

### 5. Módulo Principal (`BuildersWalletModule`)
Interface unificada que delega chamadas para o módulo ativo através dos adapters.

## Como Funciona

### Detecção Automática de SDKs
A biblioteca usa reflection para verificar se as classes dos SDKs estão disponíveis:

```kotlin
// Google Pay
Class.forName("com.google.android.gms.tapandpay.TapAndPay")

// Samsung Pay
Class.forName("com.samsung.android.sdk.samsungpay.v2.SpaySdk")
```

### Módulos Disponíveis
O `BuildersWalletPackage` registra automaticamente:
1. **BuildersWalletModule** (sempre disponível)
2. **GooglePayWalletModule** (se Google Pay SDK estiver disponível)
3. **SamsungPayWalletModule** (se Samsung Pay SDK estiver disponível)

## Uso no React Native

### Verificar Wallets Disponíveis
```javascript
import { NativeModules } from 'react-native';

const { BuildersWallet } = NativeModules;

// Verificar quais wallets estão disponíveis
const availableWallets = await BuildersWallet.getAvailableWallets();
console.log('Wallets disponíveis:', availableWallets);
```

### Usar Funcionalidades
```javascript
// A biblioteca automaticamente usa o SDK disponível
const isDefault = await BuildersWallet.isGooglePayDefaultNFCPayment();
const tokens = await BuildersWallet.listTokens();
```

### Usar Módulos Específicos Diretamente
```javascript
import { NativeModules } from 'react-native';

const { GoogleTapAndPay, SamsungPay } = NativeModules;

// Usar Google Pay diretamente
if (GoogleTapAndPay) {
  const tokens = await GoogleTapAndPay.listTokens();
  const isDefault = await GoogleTapAndPay.isGooglePayDefaultNFCPayment();
}

// Usar Samsung Pay diretamente
if (SamsungPay) {
  await SamsungPay.init('seu-service-id');
  const status = await SamsungPay.getSamsungPayStatus();
  const cards = await SamsungPay.getAllCards();
}
```

## Adicionando Novos Módulos

Para adicionar um novo provedor de wallet (ex: Apple Pay, PayPal):

1. **Criar a classe do módulo**:
```kotlin
@ReactModule(name = "NovoWalletModule")
class NovoWalletModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), WalletModuleInterface {
  
  private val isSDKAvailable: Boolean by lazy {
    try {
      Class.forName("com.novo.sdk.ClassePrincipal")
      true
    } catch (e: ClassNotFoundException) {
      false
    }
  }
  
  // Implementar todos os métodos da interface
}
```

2. **Atualizar o Factory**:
```kotlin
private fun isNovoWalletSDKAvailable(): Boolean {
  return try {
    Class.forName("com.novo.sdk.ClassePrincipal")
    true
  } catch (e: ClassNotFoundException) {
    false
  }
}

fun createWalletModule(context: ReactApplicationContext, moduleType: String = "auto"): WalletModuleInterface {
  when (moduleType) {
    "novo" -> {
      if (isNovoWalletSDKAvailable()) {
        NovoWalletModule(context)
      } else {
        StubWalletModule()
      }
    }
    // ... outros casos
  }
}
```

3. **Atualizar o Package**:
```kotlin
if (availableModules.contains("NovoWallet")) {
  modules.add(NovoWalletModule(reactContext))
}
```

## Vantagens desta Abordagem

1. **Build Independente**: A biblioteca compila mesmo sem os SDKs
2. **Detecção Automática**: Usuário não precisa configurar nada
3. **Extensível**: Fácil adicionar novos provedores
4. **Fallback Seguro**: Sempre funciona, mesmo sem SDKs
5. **Múltiplos Módulos**: Usuário pode ter vários SDKs instalados
6. **Reutilização de Código**: Usa os módulos existentes sem duplicação
7. **Flexibilidade**: Permite usar módulos específicos diretamente ou através da interface unificada
8. **Bridge Pattern**: Adapters permitem adaptar interfaces diferentes para uma interface comum

## Tratamento de Erros

Quando um SDK não está disponível:
- Métodos retornam erros específicos com código `SDK_NOT_AVAILABLE`
- Logs informativos são gerados
- Aplicação não quebra, apenas informa a indisponibilidade

## Constantes Disponíveis

Cada módulo expõe suas constantes específicas:
- **GooglePayWallet**: Constantes do Google Pay (TOKEN_PROVIDER_ELO, etc.)
- **SamsungPayWallet**: Constantes do Samsung Pay
- **BuildersWallet**: Informações sobre módulos disponíveis
