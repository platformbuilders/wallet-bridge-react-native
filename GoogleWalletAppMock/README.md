# Google Wallet App Mock

Um aplicativo Android que simula o Google Wallet para facilitar os testes do fluxo App2App durante o desenvolvimento da biblioteca BuildersWallet.

## 🎯 Propósito

Este app mock é essencial para testar o fluxo de **Manual Provisioning** do Google Pay sem depender do Google Play Services real. Ele simula o comportamento do Google Wallet ao enviar intents para ativar tokens em aplicativos de terceiros.

## 🚀 Funcionalidades

- **Simulação App2App**: Simula o envio de intents de ativação de token
- **Dados Realistas**: Envia dados em base64 com estrutura similar ao Google Wallet real
- **Interface Simples**: UI minimalista para facilitar os testes
- **Alertas Visuais**: Mostra resultado da operação com AlertDialog
- **Logs Detalhados**: Logs completos para debug e monitoramento
- **Package Mock**: Usa `com.google.android.gms_mock` para simular o Google Play Services
- **API Moderna**: Usa ActivityResultLauncher (sem deprecated warnings)

## 📱 Como Funciona

### 1. Simulação de Intent
O app envia um intent com:
- **Action**: `br.com.pefisa.pefisa.hml.action.ACTIVATE_TOKEN`
- **Package**: `br.com.pefisa.pefisa.hml`
- **Dados**: Base64 com informações de ativação de token

### 2. Dados Simulados
Os dados enviados contêm:
```json
{
  "panReferenceId": "PAN_1703048000000_1234",
  "tokenReferenceId": "TOKEN_1703048000000_12345"
}
```

**Nota**: Os IDs são gerados dinamicamente com timestamp atual e números aleatórios para simular dados únicos a cada execução. A estrutura foi simplificada para focar nos campos essenciais.

### 3. Fluxo de Teste
1. Abra o app mock
2. Clique em "Simular App 2 App"
3. O app tentará abrir seu aplicativo principal
4. Seu app receberá o intent com os dados simulados
5. O app mock aguarda o resultado da ativação
6. **Alerta visual** é exibido com o resultado da operação

## 🛠️ Instalação e Uso

### Pré-requisitos
- Android Studio
- Android SDK 23+ (Android 6.0+)
- Dispositivo Android ou emulador

### 1. Build do App

```bash
cd GoogleWalletAppMock

# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease
```

### 2. Instalação

```bash
# Instalar no dispositivo/emulador
adb install app/build/outputs/apk/debug/app-debug.apk

# Ou instalar via Android Studio
# Abra o projeto no Android Studio e clique em "Run"
```

### 3. Executar Testes

1. **Instale o app mock** no dispositivo
2. **Instale seu app principal** (com a biblioteca BuildersWallet)
3. **Abra o app mock**
4. **Clique em "Simular App 2 App"**
5. **Verifique os logs** para acompanhar o fluxo

## 📋 Configuração do App Principal

Para que seu app principal receba os intents do mock, configure o `AndroidManifest.xml`:

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

## 🔧 Personalização

### Modificar Dados Simulados

Edite o arquivo `MainActivity.kt` na função `generateSimulatedData()`:

```kotlin
private fun generateSimulatedData(): String {
    val timestamp = System.currentTimeMillis()
    
    // Personalize os IDs conforme necessário
    val panReferenceId = "SEU_PAN_${timestamp}_${(1000..9999).random()}"
    val tokenReferenceId = "SEU_TOKEN_${timestamp}_${(10000..99999).random()}"
    
    return """
    {
        "panReferenceId": "$panReferenceId",
        "tokenReferenceId": "$tokenReferenceId"
    }
    """.trimIndent()
}
```

### Personalizar Alertas

Para modificar os alertas exibidos, edite a função `showAlert()`:

```kotlin
private fun showAlert(title: String, message: String, resultCode: Int) {
    alertState = AlertState(
        show = true,
        title = title,
        message = message,
        resultCode = resultCode
    )
}
```

### Modificar Package de Destino

Para testar com um package diferente:

```kotlin
val intent = Intent("br.com.seuapp.action.ACTIVATE_TOKEN").apply {
    setPackage("br.com.seuapp")  // Seu package aqui
    putExtra(Intent.EXTRA_TEXT, simulatedData)
}
```

## 📊 Logs e Debug

### Logs do App Mock
```bash
# Filtrar logs do app mock
adb logcat | grep "GoogleWalletMock"

# Logs específicos
adb logcat | grep "🚀\|✅\|❌\|⚠️"
```

### Logs do App Principal
```bash
# Filtrar logs do seu app
adb logcat | grep "br.com.pefisa.pefisa.hml"

# Logs da biblioteca BuildersWallet
adb logcat | grep "BuildersWallet\|GoogleWallet"
```

## 🎨 Alertas Visuais

O app mock agora exibe alertas visuais para mostrar o resultado das operações:

### Tipos de Alerta

#### ✅ **Sucesso (RESULT_OK)**
- **Título**: "✅ Sucesso"
- **Mensagem**: "App Pefisa retornou com sucesso!\n\nCódigo: 0\nDados: [extras]"
- **Cor**: Verde (Material Design)

#### ⚠️ **Cancelado (RESULT_CANCELED)**
- **Título**: "⚠️ Cancelado"
- **Mensagem**: "App Pefisa foi cancelado pelo usuário.\n\nCódigo: 1"
- **Cor**: Laranja (Material Design)

#### ❓ **Resultado Inesperado**
- **Título**: "⚠️ Resultado Inesperado"
- **Mensagem**: "App Pefisa retornou com código inesperado.\n\nCódigo: [código]"
- **Cor**: Laranja (Material Design)

### Características dos Alertas
- **Material Design 3**: Interface moderna e consistente
- **Emojis**: Identificação visual rápida do status
- **Informações detalhadas**: Código de resultado e dados extras
- **Botão OK**: Para fechar o alerta
- **Dismiss**: Ao tocar fora do alerta

## 🏗️ Estrutura do Projeto

```
GoogleWalletAppMock/
├── 📁 app/
│   ├── 📁 src/main/
│   │   ├── 📁 java/com/google/android/gms_mock/
│   │   │   ├── MainActivity.kt              # Activity principal
│   │   │   └── 📁 ui/theme/
│   │   │       └── GoogleWalletMockTheme.kt # Tema do app
│   │   ├── 📁 res/                          # Recursos Android
│   │   └── AndroidManifest.xml              # Manifest do app
│   └── build.gradle                         # Configuração do módulo
├── 📁 gradle/
│   └── libs.versions.toml                   # Versões das dependências
├── build.gradle                             # Configuração do projeto
├── gradle.properties                        # Propriedades do Gradle
├── settings.gradle                          # Configuração do projeto
└── README.md                                # Este arquivo
```

## 🧪 Cenários de Teste

### 1. Cenário de Sucesso
- App mock envia intent
- App principal recebe e processa
- Retorna `RESULT_OK`
- **Alerta exibido**: "✅ Sucesso" com detalhes do resultado

### 2. Cenário de Erro
- App mock envia intent com dados inválidos
- App principal retorna erro
- **Alerta exibido**: "⚠️ Resultado Inesperado" com código de erro

### 3. Cenário de Timeout
- App mock envia intent
- App principal não responde
- App mock aguarda timeout
- **Alerta exibido**: "⚠️ Resultado Inesperado" com código de timeout

### 4. Cenário de Cancelamento
- App mock envia intent
- Usuário cancela no app principal
- App mock recebe `RESULT_CANCELED`
- **Alerta exibido**: "⚠️ Cancelado" com informações do cancelamento

## 🔍 Troubleshooting

### App Mock não consegue abrir o app principal
- Verifique se o package name está correto
- Confirme se o intent filter está configurado
- Verifique se o app principal está instalado

### Dados não chegam no app principal
- Verifique se o `EXTRA_TEXT` está sendo enviado
- Confirme se o listener de intent está ativo
- Verifique os logs de ambos os apps

### App principal não responde
- Verifique se o `ActivityResultLauncher` está implementado
- Confirme se o `setResult` está sendo chamado
- Verifique se a activity está sendo finalizada
- **Novo**: O app mock agora usa API moderna sem deprecated warnings

## 📝 Exemplo de Uso Completo

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

## 🤝 Contribuindo

Para contribuir com melhorias no app mock:

1. Fork o projeto
2. Crie uma branch para sua feature
3. Implemente as mudanças
4. Teste com o app principal
5. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](../LICENSE) para detalhes.

---

**Nota**: Este app mock é destinado apenas para desenvolvimento e testes. Não deve ser usado em produção.
