# Configuração do Google Pay Tap and Pay SDK

## Como Configurar

### 1. Baixar o Google Pay Tap and Pay SDK
- Acesse a página oficial do Google Developers: [Android Push Provisioning API - SDK releases](https://developers.google.com/pay/issuers/apis/push-provisioning/android/releases?authuser=1)
- Clique no botão **"Download SDK"** para baixar o pacote
- O arquivo baixado terá o formato: `tapandpay_sdk.m2repo_2023-06-21_v18.3.3.zip`

### 2. Descompactar e Colocar o SDK
- Descompacte o arquivo ZIP baixado
- Copie todo o conteúdo descompactado para a pasta `android/libs` do seu projeto:

```
seu-app/
├── android/
│   └── libs/
│       └── com/
│           └── google/
│               └── android/
│                   └── gms/
│                       └── play-services-tapandpay/
│                           └── 18.3.3/
│                               ├── maven-metadata.xml
│                               ├── maven-metadata.xml.md5
│                               └── maven-metadata.xml.sha1
```

### 3. Configurar as Propriedades
Crie ou edite o arquivo `android/gradle.properties`:

```properties
# Habilitar Google Pay Tap and Pay
includeGooglePlayServices=true
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
- ✅ `Google Play Services Tap and Pay incluído`
- ⚠️ `Google Play Services Tap and Pay não incluído (defina includeGooglePlayServices=true para incluir)`

## Vantagens desta Abordagem
- 🎯 **Flexível**: Cada app pode ter sua própria versão do Google Pay SDK
- 📱 **Independente**: A biblioteca não precisa incluir o SDK
- 🔍 **Detectável**: Logs claros mostram se o SDK foi incluído
- ⚙️ **Configurável**: Pode ser habilitado/desabilitado via propriedades

## Notas Importantes
- O Google Pay Tap and Pay SDK **não está disponível** na versão pública do Google Play Services
- Você **deve** baixar e incluir o SDK manualmente conforme descrito acima
- O SDK é compatível com versões anteriores, então uma versão mais antiga continuará funcionando com versões mais novas do Google Play Services
- Se um usuário tiver uma versão mais antiga do Google Play Services, ele será solicitado a atualizar

## Links Úteis
- [Página oficial de releases do Google Pay Tap and Pay SDK](https://developers.google.com/pay/issuers/apis/push-provisioning/android/releases?authuser=1)
- [Documentação do Android Push Provisioning API](https://developers.google.com/pay/issuers/apis/push-provisioning/android)
