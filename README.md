# @platformbuilders/wallet-bridge-react-native

[![npm version](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native.svg)](https://badge.fury.io/js/%40platformbuilders%2Fwallet-bridge-react-native)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A React Native library that facilitates integration with digital wallets (Google Pay, Samsung Pay).

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
const cardData = {
  cardId: 'card-123',
  cardBrand: 'VISA',
  cardType: 'CREDIT',
  cardLast4Fpan: '1234',
  cardIssuer: 'Example Bank',
  cardStatus: 'ACTIVE'
};
const result = await SamsungWalletModule.addCardToWallet(cardData);

// List tokens
const tokens = await SamsungWalletModule.listTokens();
```

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

### 🔋 Running the project

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

### Prerequisites

- Node.js >= 18
- Yarn 3.6.1
- React Native >= 0.70.6
- Android Studio (for Android)
- Xcode (for iOS)

## 🔗 Useful Links

- [Google Pay Android Push Provisioning](https://developers.google.com/pay/issuers/apis/push-provisioning/android)
- [Samsung Pay SDK](https://developer.samsung.com/samsung-pay)
- [React Native Documentation](https://reactnative.dev/)

## 📄 License

MIT - see the [LICENSE](LICENSE) file for details.
