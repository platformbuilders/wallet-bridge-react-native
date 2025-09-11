# Configuração do Samsung Pay SDK

## Como Configurar

### 1. Baixar o Samsung Pay SDK
- Baixe o arquivo JAR do Samsung Pay SDK da Samsung Developer Portal
- Renomeie o arquivo para `samsungpay_<versão>.jar` (ex: `samsungpay_2.22.00.jar`)

### 2. Colocar o JAR no Local Correto
O build.gradle detecta automaticamente o arquivo JAR na pasta `libs` do seu projeto:

```
seu-app/
├── libs/
│   └── samsungpay_2.22.00.jar  ← Qualquer versão detectada automaticamente
```

### 3. Configurar as Propriedades
Crie ou edite o arquivo `android/gradle.properties`:

```properties
# Habilitar Samsung Pay
enableSamsungPay=true
```

**Nota**: A versão é detectada automaticamente pelo nome do arquivo!

### 4. Build
Execute o build normalmente:
```bash
cd android
./gradlew clean
./gradlew assembleDebug
```

## Logs de Debug
O build mostrará logs informativos:
- ✅ `Samsung Pay SDK encontrado: samsungpay_2.22.00.jar`
- ✅ `Versão detectada: 2.22.00`
- ✅ `Samsung Pay SDK v2.22.00 incluído de: /caminho/para/samsungpay_2.22.00.jar`
- ⚠️ `Nenhum arquivo samsungpay_*.jar encontrado em: /caminho/para/libs` (se o arquivo não estiver no local correto)

## Vantagens desta Abordagem
- 🎯 **Flexível**: Cada app pode ter sua própria versão do Samsung Pay SDK
- 📱 **Independente**: A biblioteca não precisa incluir o JAR
- 🔍 **Detectável**: Logs claros mostram onde o JAR foi encontrado
- ⚙️ **Configurável**: Versão pode ser especificada via propriedades
