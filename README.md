# @platformbuilders/wallet-bridge-react-native

[![npm version](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native.svg)](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![React Native](https://img.shields.io/badge/React%20Native-0.70+-blue.svg)](https://reactnative.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0+-blue.svg)](https://www.typescriptlang.org/)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

A React Native library that facilitates integration with digital wallets (Google Pay, Samsung Pay).

---

## 📊 Project Status

| Aspect | Status |
|---------|--------|
| Development | ✅ Active |
| Stable Version | 1.0.0 |
| Tests | ✅ 100% coverage |
| Documentation | ✅ Complete |
| iOS Support | 🚧 Planned |
| Open Issues | [View on GitHub](https://github.com/platformbuilders/wallet-bridge-react-native/issues) |

---

## 📋 Requirements

| Technology | Minimum Version | Recommended |
|------------|---------------|-------------|
| Node.js | 18.0.0 | 20.x |
| React Native | 0.70.6 | 0.81.0 |
| React | 18.1.0 | 19.1.0 |
| Android SDK | API 23 | API 33+ |

### Tested Versions

The library has been tested with:

- ✅ React Native 0.70.6 - 0.81.0
- ✅ React 18.1.0 - 19.1.0
- ✅ Android API 23-34

---

## 📦 Installation

### Installation via NPM

```bash
npm add @platformbuilders/wallet-bridge-react-native
# or
yarn add @platformbuilders/wallet-bridge-react-native
```

## ⚙️ Configuration

### 1. Enable wallet in gradle.properties

```properties
# android/gradle.properties

# Enable SDKs
GOOGLE_WALLET_ENABLED=true
SAMSUNG_WALLET_ENABLED=true
```

### 2. Install SDKs (if enabled)

**Google Pay SDK:**

- Download from: <https://developers.google.com/pay/issuers/apis/push-provisioning/android/releases>
- Place in: `android/libs/com/google/android/gms/play-services-tapandpay/`

**Samsung Pay SDK:**

- Download from: <https://developer.samsung.com/samsung-pay>
- Rename to: `samsungpay_<version>.jar`
- Place in: `android/libs/`

### 3. Build

```bash
cd android
./gradlew clean build
```

### 4. Mock Mode for Development

For development without real SDKs, or to avoid your device being added to wallet blacklists, enable mock mode:

```properties
# android/gradle.properties

# Enable mock mode
GOOGLE_WALLET_USE_MOCK=true
SAMSUNG_WALLET_USE_MOCK=true

# Configure mock server URL (optional)
# For local development
GOOGLE_WALLET_MOCK_API_URL=http://localhost:3000

# For Android emulator (use host IP)
# GOOGLE_WALLET_MOCK_API_URL=http://10.0.2.2:3000

# For physical device (use local network IP)
# GOOGLE_WALLET_MOCK_API_URL=http://192.168.1.100:3000

# Samsung Wallet Mock
SAMSUNG_WALLET_MOCK_API_URL=http://localhost:3000
```

#### Required Endpoints

Your implementation must provide the following endpoints:

1. `GET /wallet/availability` - Checks wallet availability
2. `GET /wallet/info` - Returns wallet information
3. `GET /wallet/token/status` - Checks token status
4. `GET /wallet/tokens` - Lists wallet tokens
5. `GET /wallet/is-tokenized` - Checks if card is tokenized
6. `GET /wallet/view-token` - Views specific token
7. `POST /wallet/create` - Creates wallet
8. `POST /wallet/set-intent-listener` - Activates intent listener
9. `DELETE /wallet/remove-intent-listener` - Removes listener
10. `POST /wallet/set-activation-result` - Sets activation result
11. `POST /wallet/finish-activity` - Finishes activity
12. `GET /wallet/environment` - Returns environment
13. `POST /wallet/add-card` - Adds card to wallet

> ⚠️ **Local Mock API:** The library supports integration with a mock HTTP server for advanced testing. This server must be implemented separately by the developer. Refer to the complete documentation for details about the required endpoints.

**Mock Behavior:**

- `checkWalletAvailability()`: Queries mock server in real-time (if configured)
- `getSecureWalletInfo()`: Returns simulated data or from local API
- `addCardToWallet()`: Validates data and simulates different scenarios based on last digits
- `listTokens()`: Returns 2 simulated tokens (Visa and Mastercard) or from local API
- `getConstants()`: Returns correct constants (ELO = 14/12, TOKEN_STATE_* = 1-6)
- **Local API**: Full support for local mock server (configurable via gradle.properties)

**Note**: If the mock URL is not configured, the mock will use only default simulated values (without HTTP requests).

## 🎯 Basic Usage

### Google Pay

```javascript
import { GoogleWalletModule } from '@platformbuilders/wallet-bridge-react-native';

// Check availability
const isAvailable = await GoogleWalletModule.checkWalletAvailability();

// Get wallet information
const walletInfo = await GoogleWalletModule.getSecureWalletInfo();

// Add card
const cardData = {
  address: {
    address1: '123 Main Street',
    countryCode: 'US',
    locality: 'New York',
    administrativeArea: 'NY',
    name: 'John Doe',
    phoneNumber: '+12125551234',
    postalCode: '10001'
  },
  card: {
    opaquePaymentCard: 'eyJ0eXBlIjoiL0dvb2dsZV9QYXlfQ2FyZCIsInRva2VuIjoiZXhhbXBsZV90b2tlbl9kYXRhIn0=',
    network: GoogleWalletModule.getConstants().CARD_NETWORK_ELO,
    tokenServiceProvider: GoogleWalletModule.getConstants().TOKEN_PROVIDER_ELO,
    displayName: 'John Doe - Visa',
    lastDigits: '1234'
  }
};
const result = await GoogleWalletModule.addCardToWallet(cardData);

// List tokens
const tokens = await GoogleWalletModule.listTokens();
```

### Samsung Pay

```javascript
import { SamsungWalletModule } from '@platformbuilders/wallet-bridge-react-native';

// Check availability
const isAvailable = await SamsungWalletModule.checkWalletAvailability();

// Get wallet information
const walletInfo = await SamsungWalletModule.getSecureWalletInfo();

// Add card
const result = await SamsungWalletModule.addCard(
  'payload_base64_string',
  'issuer-123',
  'VISA',
  'CREDIT'
);

// List tokens
const tokens = await SamsungWalletModule.listTokens();
```

> ⚠️ **Important:** Samsung Pay and Google Pay have different signatures for adding cards:
>
> - **Google Pay:** Accepts object with `address` and `card`
> - **Samsung Pay:** Accepts separate parameters (`payload`, `issuerId`, etc.)

### App2App (Manual Provisioning)

```javascript
import { GoogleWalletModule, GoogleWalletEventEmitter } from '@platformbuilders/wallet-bridge-react-native';

// Activate listener
await GoogleWalletModule.setIntentListener();

// Listen to events
const eventEmitter = new GoogleWalletEventEmitter();
const removeListener = eventEmitter.addIntentListener((event) => {
  if (event.type === 'ACTIVATE_TOKEN') {
    const activationParams = JSON.parse(event.data);
    // Process activation
  }
});

// Set result
await GoogleWalletModule.setActivationResult('approved', 'ACTIVATION_CODE');
await GoogleWalletModule.finishActivity();
```

**AndroidManifest.xml:**

```xml
<activity android:name=".MainActivity">
  <intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
  </intent-filter>
  <!-- This activity handles App To App ACTIVATE_TOKEN action Google Pay -->
  <!-- Replace {PACKAGE_NAME} by your app package name -->
  <intent-filter>
    <action android:name="{PACKAGE_NAME}.action.ACTIVATE_TOKEN"/>
    <category android:name="android.intent.category.DEFAULT"/>
  </intent-filter>

  <!-- This activity handles App To App LAUNCH_A2A_IDV action Samsung Pay -->
  <!-- Replace {PACKAGE_NAME} by your app package name -->
  <intent-filter>
    <action android:name="{PACKAGE_NAME}.action.LAUNCH_A2A_IDV"/>
    <category android:name="android.intent.category.DEFAULT"/>
  </intent-filter>
</activity>
```

### ‼️ To avoid breaking builds on iOS

```bash
cd ios && pod install
```

---

## 🪝 React Hooks

The library provides custom hooks to facilitate integration in React components.

### useGoogleWalletListener

Hook to manage Google Wallet intent listeners.

```typescript
import { useGoogleWalletListener } from '@platformbuilders/wallet-bridge-react-native';

function MyComponent() {
  useGoogleWalletListener((event) => {
    if (event.type === 'ACTIVATE_TOKEN') {
      console.log('Token ativado:', event);
    }
  });

  return <View>...</View>;
}
```

**Features:**

- Automatically starts/stops listener on mount/unmount
- Cleans up resources on unmount
- Type-safe with TypeScript

### useGoogleWalletLogListener

Hook to capture Google Wallet logs.

```typescript
import { useGoogleWalletLogListener } from '@platformbuilders/wallet-bridge-react-native';

function DebugPanel() {
  const [logs, setLogs] = useState([]);

  useGoogleWalletLogListener((log) => {
    setLogs(prev => [...prev, log]);
  });

  return <LogView logs={logs} />;
}
```

### useSamsungWalletListener

Similar to `useGoogleWalletListener`, but for Samsung Wallet.

### useSamsungWalletLogListener

Similar to `useGoogleWalletLogListener`, but for Samsung Wallet.

---

## 📝 Logging Methods

### GoogleWalletModule

| Method | Description | Return |
|--------|-----------|---------|
| `setLogListener` | Activates SDK log listener | `Promise<boolean>` |
| `removeLogListener` | Removes log listener | `Promise<boolean>` |

### SamsungWalletModule

| Method | Description | Return |
|--------|-----------|---------|
| `setLogListener` | Activates SDK log listener | `Promise<boolean>` |
| `removeLogListener` | Removes log listener | `Promise<boolean>` |

### Log Listeners Usage

```typescript
// Activate logging
await GoogleWalletModule.setLogListener();

// Listen to logs via EventEmitter
const logEmitter = new GoogleWalletEventEmitter();
logEmitter.addLogListener((log) => {
  console.log('[Google Wallet]', log);
});

// Deactivate when not needed
await GoogleWalletModule.removeLogListener();
```

### Available Logs

Logs include:

- SDK operations (start, end, errors)
- Token transition states
- Native SDK errors and warnings
- Network events (when applicable)

---

## 🐛 Debug and Logging

### Enable Detailed Logs

For development, activate log listeners:

```typescript
// At the beginning of the app (App.tsx)
if (__DEV__) {
  GoogleWalletModule.setLogListener();
  SamsungWalletModule.setLogListener();

  // Display logs in console
  GoogleWalletService.walletEventEmitter.addLogListener(console.log);
  SamsungWalletService.walletEventEmitter.addLogListener(console.log);
}
```

---

## Event Emitters - Available Events

### GoogleWalletEventEmitter

**1. Intent Events (addIntentListener)**

```typescript
interface GoogleWalletIntentEvent {
  action: string;                    // Intent action
  type: GoogleWalletIntentType;      // 'ACTIVATE_TOKEN', etc.
  data?: string;                     // Decoded data
  dataFormat?: GoogleWalletDataFormat; // 'base64_decoded' | 'raw'
  callingPackage?: string;           // Calling app package
  originalData?: string;             // Original data in base64
  error?: string;                    // Error message (if any)
  extras?: Record<string, any>;      // Intent extras
}
```

**2. Log Events (addLogListener)**

```typescript
interface GoogleWalletLogEvent {
  level: 'debug' | 'info' | 'warn' | 'error';
  message: string;
  timestamp: number;
  metadata?: Record<string, any>;
}
```

### SamsungWalletEventEmitter

Similar to Google Wallet, but with Samsung Pay specific types.

---

### 🔋 Running the Project

At the project root, open 2 terminals and run the following commands:

- In the first terminal, run the command below:

```bash
yarn react-native start
# or
npx react-native start
```

- Then, in the second terminal, run the command:

```bash
yarn react-native run-android
# or
npx react-native run-android
```

> **⚠️ Warning:** This second command is only for building on a physical device or emulator.

## 🔗 Useful Links

- [Google Pay Android Push Provisioning](https://developers.google.com/pay/issuers/apis/push-provisioning/android)
- [Samsung Pay SDK](https://developer.samsung.com/samsung-pay)
- [React Native Documentation](https://reactnative.dev/)

## 📄 License

MIT - see the [LICENSE](LICENSE) file for details.
