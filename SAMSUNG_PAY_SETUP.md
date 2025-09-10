# Configuração do Samsung Pay SDK

## Como Configurar

### 1. Baixar o Samsung Pay SDK
- Baixe o arquivo JAR do Samsung Pay SDK da Samsung Developer Portal
- Renomeie o arquivo para `samsungpay_2.22.00.jar` (ou a versão desejada)

### 2. Colocar o JAR no Local Correto
O build.gradle procura o arquivo JAR na pasta `android/app/libs` do seu app:

```
seu-app/
├── android/
│   └── app/
│       └── libs/
│           └── samsungpay_2.22.00.jar  ← Aqui
```

### 3. Configurar as Propriedades
Crie ou edite o arquivo `android/gradle.properties`:

```properties
# Habilitar Samsung Pay
enableSamsungPay=true

# Versão do Samsung Pay SDK (opcional, padrão: 2.22.00)
samsungPayVersion=2.22.00
```

### 4. Build
Execute o build normalmente:
```bash
cd android
./gradlew clean
./gradlew assembleDebug
```

## Logs de Debug
O build mostrará logs informativos:
- ✅ `Samsung Pay SDK encontrado em: /caminho/para/samsungpay_2.22.00.jar`
- ✅ `Samsung Pay SDK v2.22.00 incluído de: /caminho/para/samsungpay_2.22.00.jar`
- ⚠️ `Samsung Pay SDK não encontrado` (se o arquivo não estiver no local correto)

## Vantagens desta Abordagem
- 🎯 **Flexível**: Cada app pode ter sua própria versão do Samsung Pay SDK
- 📱 **Independente**: A biblioteca não precisa incluir o JAR
- 🔍 **Detectável**: Logs claros mostram onde o JAR foi encontrado
- ⚙️ **Configurável**: Versão pode ser especificada via propriedades
