# Guia do Listener de Intent - Google Wallet

Este guia explica como usar o sistema de listener de intent para capturar quando o app é aberto pela carteira do Google.

## Configuração

### 1. AndroidManifest.xml

O AndroidManifest.xml já está configurado com o intent filter necessário:

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

### 2. Métodos Disponíveis

#### `setIntentListener()`
Ativa o listener para capturar intents da carteira.

```javascript
import { NativeModules } from 'react-native';
const { GoogleWallet } = NativeModules;

// Ativar listener
await GoogleWallet.setIntentListener();
```

#### `removeIntentListener()`
Desativa o listener de intents.

```javascript
// Desativar listener
await GoogleWallet.removeIntentListener();
```

## Uso com EventEmitter

### 1. Configurar o EventEmitter

```javascript
import { NativeEventEmitter, NativeModules } from 'react-native';

const { GoogleWallet } = NativeModules;
const eventEmitter = new NativeEventEmitter(GoogleWallet);
```

### 2. Escutar Eventos

```javascript
useEffect(() => {
  const subscription = eventEmitter.addListener('GoogleWalletIntentReceived', (event) => {
    console.log('Intent recebido:', event);
    
    switch (event.type) {
      case 'ACTIVATE_TOKEN':
        // Processar ativação de token
        handleTokenActivation(event);
        break;
        
      case 'WALLET_INTENT':
        // Processar outros intents da carteira
        handleWalletIntent(event);
        break;
    }
  });

  return () => subscription.remove();
}, []);
```

## Estrutura do Evento

O evento `GoogleWalletIntentReceived` contém:

```javascript
{
  action: "br.com.pefisa.pefisa.hml.action.ACTIVATE_TOKEN", // Ação do intent
  type: "ACTIVATE_TOKEN", // Tipo do evento
  data: "eyJ0b2tlblJlZmVyZW5jZUlkIjoiMTIzNDU2Nzg5MCIsInRva2VuU2VydmljZVByb3ZpZGVyIjoxfQ==", // Dados em base64 (se houver)
  dataFormat: "base64", // Formato dos dados
  dataNote: "Os dados estão em formato base64 e precisam ser decodificados para obter os parâmetros de ativação como um objeto JSON", // Nota sobre os dados
  extras: { // Dados extras do intent (se houver)
    // ... dados específicos do intent
  }
}
```

### Dados Base64

Os dados importantes da carteira do Google são enviados via `Intent.EXTRA_TEXT` em formato base64. Para acessar os parâmetros de ativação:

```javascript
// Decodificar dados base64
const decodedData = atob(event.data);
const activationParams = JSON.parse(decodedData);

console.log('Parâmetros de ativação:', activationParams);
// Exemplo de saída:
// {
//   "tokenReferenceId": "1234567890",
//   "tokenServiceProvider": 1,
//   "activationCode": "ABC123"
// }
```

## Tipos de Eventos

### `ACTIVATE_TOKEN`
Disparado quando o app é aberto para ativar um token (apenas se o chamador for Google Play Services).

### `WALLET_INTENT`
Disparado para outros intents relacionados à carteira (apenas se o chamador for Google Play Services).

### `INVALID_CALLER`
Disparado quando um intent é recebido de um chamador não autorizado (não é Google Play Services).

## Exemplo Completo

```javascript
import React, { useEffect, useState } from 'react';
import { View, Text, Button, Alert } from 'react-native';
import { NativeEventEmitter, NativeModules } from 'react-native';

const { GoogleWallet } = NativeModules;
const eventEmitter = new NativeEventEmitter(GoogleWallet);

const WalletIntentHandler = () => {
  const [listenerActive, setListenerActive] = useState(false);

  useEffect(() => {
    const subscription = eventEmitter.addListener('GoogleWalletIntentReceived', (event) => {
      console.log('🎯 Intent recebido:', event);
      
      if (event.type === 'ACTIVATE_TOKEN') {
        Alert.alert('Token Ativado', 'Intent de ativação recebido!');
      }
    });

    return () => subscription.remove();
  }, []);

  const toggleListener = async () => {
    try {
      if (listenerActive) {
        await GoogleWallet.removeIntentListener();
        setListenerActive(false);
        Alert.alert('Sucesso', 'Listener desativado');
      } else {
        await GoogleWallet.setIntentListener();
        setListenerActive(true);
        Alert.alert('Sucesso', 'Listener ativado');
      }
    } catch (error) {
      Alert.alert('Erro', `Falha: ${error}`);
    }
  };

  return (
    <View>
      <Button
        title={listenerActive ? 'Desativar Listener' : 'Ativar Listener'}
        onPress={toggleListener}
      />
    </View>
  );
};
```

## Logs de Debug

O sistema gera logs detalhados para debug:

- `🔍 [GOOGLE] setIntentListener chamado`
- `✅ [GOOGLE] Listener de intent ativado`
- `🔍 [GOOGLE] processWalletIntent chamado`
- `✅ [GOOGLE] Intent de ativação de token recebido`
- `✅ [GOOGLE] Evento enviado com sucesso`

## Considerações de Segurança

### Validação do Chamador (Implementada Automaticamente)
O sistema valida automaticamente se o chamador é realmente o Google Play Services (`com.google.android.gms`):

```kotlin
// Validação automática no Android
val callingPackage = intent.`package`
if ("com.google.android.gms" == callingPackage) {
    // Processar intent da carteira do Google
    processWalletIntent(intent)
} else {
    // Abortar - chamador não autorizado
    sendErrorEvent("Chamador não autorizado")
}
```

### Tratamento de Chamadores Inválidos
Quando um chamador não autorizado tenta acessar o app, o evento `INVALID_CALLER` é disparado:

```javascript
// Tratar chamadores inválidos
if (event.type === 'INVALID_CALLER') {
  console.warn('Tentativa de acesso não autorizada:', event);
  // Log de segurança ou notificação
  handleUnauthorizedAccess(event);
}
```

### Validação de Dados
Além da validação do chamador, sempre valide os dados recebidos:

```javascript
// Verificar se o intent é da carteira do Google
if (event.action === 'br.com.pefisa.pefisa.hml.action.ACTIVATE_TOKEN') {
  // Processar dados de ativação
  if (event.data && event.dataFormat === 'base64') {
    try {
      const decodedData = atob(event.data);
      const activationParams = JSON.parse(decodedData);
      
      // Validar parâmetros antes de processar
      if (activationParams.tokenReferenceId && activationParams.tokenServiceProvider) {
        // Processar ativação do token
        processTokenActivation(activationParams);
      }
    } catch (error) {
      console.error('Dados de ativação inválidos:', error);
    }
  }
}
```

### Práticas de Segurança
- Sempre valide os dados recebidos antes de processar
- Verifique se os campos obrigatórios estão presentes
- Use try/catch ao decodificar base64 e fazer parse do JSON
- Monitore tentativas de acesso não autorizadas
- Implemente logs de segurança para auditoria

## Considerações Importantes

1. **Ativação**: O listener deve ser ativado antes de esperar receber intents
2. **Cleanup**: Sempre remova o listener quando não precisar mais
3. **Lifecycle**: O listener funciona enquanto o app estiver ativo
4. **Threading**: Os eventos são enviados na thread principal do React Native
5. **Segurança**: Sempre valide os dados recebidos antes de processar
6. **Base64**: Os dados importantes estão em formato base64 e precisam ser decodificados

## Troubleshooting

### Listener não está funcionando
- Verifique se `setIntentListener()` foi chamado
- Confirme que o AndroidManifest.xml está configurado corretamente
- Verifique os logs para erros

### Eventos não chegam
- Verifique se o EventEmitter está configurado corretamente
- Confirme que o subscription não foi removido prematuramente
- Verifique se o app está sendo aberto pela carteira corretamente
