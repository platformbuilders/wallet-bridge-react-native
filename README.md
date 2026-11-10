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
| Stable Version | 1.3.0 (2.0.0 in progress) |
| Tests | ✅ Jest + native build in CI |
| Documentation | ✅ Complete |
| iOS Support | ✅ Apple Wallet In-App Provisioning (PassKit) |
| Open Issues | [View on GitHub](https://github.com/platformbuilders/wallet-bridge-react-native/issues) |

---

## 📋 Requirements

| Technology | Minimum Version | Recommended |
|------------|---------------|-------------|
| Node.js | 20.19.4 | 22.x |
| React Native | 0.80.0 | 0.86.x |
| React | 18.0.0 | 19.2.x |
| Java (Android build) | 17 | 17 |
| Android SDK | API 24 | API 36 |

> ⚠️ **New Architecture is required.** Starting with 2.0.0 the library targets the React Native New Architecture only. Apps on React Native 0.80–0.81 must keep `newArchEnabled=true` (Android) and `RCT_NEW_ARCH_ENABLED=1` (iOS). Apps that still run the legacy architecture should stay on the 1.x line.

### Migrating from 1.x

The native modules are TurboModules in 2.0.0. Method names, constants, event names and event payloads did not change, but **events are no longer emitted through `RCTDeviceEventEmitter`**. Listeners registered directly with `DeviceEventEmitter` or `NativeEventEmitter` stop receiving App2App intents and logs:

```typescript
// ❌ 1.x: no longer receives anything in 2.0.0
DeviceEventEmitter.addListener('GoogleWalletIntentReceived', handleIntent);
DeviceEventEmitter.addListener('WalletLog', handleLog);
```

Use the library hooks (recommended) or the service event emitters instead:

```typescript
// ✅ 2.0.0: hooks (register the listeners before the pending intent is requested)
useGoogleWalletListener(handleIntent, handleNoIntent, handleValidCallerNoIntent);
useGoogleWalletLogListener(handleLog, { enabled: __DEV__ });

// ✅ 2.0.0: event emitter, outside React
const removeListener =
  GoogleWalletService.walletEventEmitter.addIntentListener(handleIntent);
await GoogleWalletService.startIntentListener();
```

The same applies to Samsung Wallet (`useSamsungWalletListener`, `useSamsungWalletLogListener`, `SamsungWalletService.walletEventEmitter`).

Other changes:

- Peer dependencies are `react >= 18` and `react-native >= 0.80` with the New Architecture enabled.
- Enums can be imported from `@platformbuilders/wallet-bridge-react-native/enums` without a `patch-package` patch. The `/src/enums` path used with the 1.x patch keeps working.

### Tested Versions

The example app is built and tested with:

- ✅ React Native 0.86.3 / React 19.2.3 (New Architecture enabled)
- ✅ Android API 36, Java 17

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

#### Optional Endpoints (for Advanced Testing)

> ⚠️ **Important:** These endpoints are **completely optional**. The mock mode works perfectly without them, using default simulated values that can be customized to simulate different scenarios.

**When to use endpoints:**
- **Main benefit:** Change simulations without rebuilding the app - you can modify mock responses on the server and test different scenarios instantly
- You want to test with dynamic responses from a server
- You need to simulate complex scenarios that require server-side logic
- You want to facilitate testing by centralizing mock data in a server

**When you don't need endpoints:**
- The mocks already have default values that work out of the box
- You can modify the mock implementation directly to simulate different scenarios
- You prefer a simpler setup without running a server

If you choose to implement a mock HTTP server for advanced testing, your implementation should provide the following endpoints:

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

**Mock Behavior (with or without endpoints):**

- `checkWalletAvailability()`: Returns default simulated value, or queries mock server in real-time (if configured)
- `getSecureWalletInfo()`: Returns default simulated data, or from local API (if configured)
- `addCardToWallet()`: Validates data and simulates different scenarios based on last digits (default behavior), or uses server response (if configured)
- `listTokens()`: Returns 2 default simulated tokens (Visa and Mastercard), or from local API (if configured)
- `getConstants()`: Returns correct constants (ELO = 14/12, TOKEN_STATE_* = 1-6)

**Default Mock Values:**
The library includes default simulated values that work immediately without any server setup. These values can be modified directly in the mock implementation to simulate different scenarios (success, errors, edge cases, etc.) without needing to implement any endpoints.

**Using Local API (Optional):**
If you configure `GOOGLE_WALLET_MOCK_API_URL` or `SAMSUNG_WALLET_MOCK_API_URL` in `gradle.properties`, the mocks will query your server for responses. If not configured, the mocks will use only the default simulated values (no HTTP requests are made).

## 🎯 Basic Usage

### Enums

Enums are exported from the package root and from the `enums` subpath, which only loads the enums (no native module access). `src/enums` is kept as an alias for apps that used the 1.x patch:

```javascript
import { GoogleActivationStatus, SamsungActivationStatus } from '@platformbuilders/wallet-bridge-react-native/enums';
```

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
const result: string | null = await GoogleWalletModule.addCardToWallet(cardData);
// result pode ser null se o tokenId não estiver disponível

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

### Apple Wallet (iOS)

`AppleWalletModule` wraps PassKit only. The library **does not call any backend, does not decide eligibility and knows no issuer**: the app sends the step 1 data to its issuer and hands the step 2 response back. On other platforms the module answers through a stub (queries resolve neutral values, actions reject with `NOT_SUPPORTED_ON_PLATFORM`).

#### Setup

- The pod links `PassKit`, `UIKit` and `WatchConnectivity` for every iOS app, also apps that do not use Apple Wallet. Linking PassKit alone needs no entitlement.
- In-App Provisioning needs the restricted entitlement `com.apple.developer.payment-pass-provisioning`, granted by Apple, in the app provisioning profile. Without it `startInAppProvisioning` rejects with `ENTITLEMENT_MISSING` and `canAddCard` answers `ENTITLEMENT_MISSING` on a device.
- `listPasses`, `isCardTokenized` and `activatePass` see only the passes whose `associatedApplicationIdentifiers` include the app (configured by the issuer with Apple).

#### In-App Provisioning (push)

```typescript
import {
  AppleProvisioningStatus,
  AppleWalletModule,
  useAppleWalletListener,
} from '@platformbuilders/wallet-bridge-react-native';

useAppleWalletListener({
  // Step 1: certificates (leaf first), nonce and nonceSignature, standard Base64.
  onProvisioningCertificates: async (event) => {
    const payload = await myIssuerApi.pushProvisioning(event); // your backend
    // Step 2: the three Base64 blobs from the issuer.
    await AppleWalletModule.submitProvisioningData({
      activationData: payload.activationData,
      encryptedPassData: payload.encryptedPassData,
      ephemeralPublicKey: payload.ephemeralPublicKey,
    });
  },
  onProvisioningResult: (event) => {
    if (event.status === AppleProvisioningStatus.ADDED) {
      // event.pass (+ event.additionalPasses when also added to the Watch)
    }
  },
});

const availability = await AppleWalletModule.canAddCard(fpanId);
// availability.reason: AVAILABLE | ALREADY_ADDED | REQUIRES_ACTIVATION | ...

await AppleWalletModule.startInAppProvisioning({
  cardholderName: 'NAME ON CARD',
  primaryAccountSuffix: '1234',
  localizedDescription: 'My card',
  paymentNetwork: 'Visa', // PKPaymentNetwork raw value
  primaryAccountIdentifier: fpanId, // optional, FPANID
  expiration: '12/30', // optional, MM/YY
});
```

`startInAppProvisioning` does not check `canAddCard` first: deciding whether to start (card already in Wallet, pending activation) is up to the app. It only rejects when PassKit itself cannot proceed. `submitProvisioningData` resolves `true` once PassKit received the data; the added pass arrives in `onProvisioningResult` with status `ADDED`. The encryption scheme is always `ECC_V2`.

#### Passes and In-App Verification

```typescript
const passes = await AppleWalletModule.listPasses(); // PKPassLibrary.passes(of: .secureElement)
const remotePasses = await AppleWalletModule.listRemotePasses(); // remoteSecureElementPasses
// Each pass, as PassKit returns it: primaryAccountIdentifier (FPANID),
// deviceAccountIdentifier (DPANID), passActivationState (ACTIVATED |
// REQUIRES_ACTIVATION | ACTIVATING | SUSPENDED | DEACTIVATED), isRemote,
// deviceName, suffixes. Merging the lists and telling iPhone from Apple Watch
// is up to the app.

const watchPaired = await AppleWalletModule.isWatchPaired();

// In-App Verification: Wallet opens the app through the pass appLaunchURL
// (handled by the app with Linking). After ID&V, get the activation data from
// the issuer and activate the pass on that device:
await AppleWalletModule.activatePass(deviceAccountIdentifier, activationData);
```

#### API

| Method | Returns | Notes |
|---|---|---|
| `checkWalletAvailability()` | `Promise<boolean>` | Device can add cards |
| `canAddCard(fpanId?)` | `Promise<AppleCanAddCardResult>` | `canAdd`, `reason`, `deviceAccountIdentifier` of an existing pass |
| `startInAppProvisioning(config)` | `Promise<string>` | `requestId`; presents the PassKit sheet |
| `submitProvisioningData(payload)` | `Promise<boolean>` | Step 2; pass arrives in `onProvisioningResult` |
| `cancelProvisioning()` | `Promise<boolean>` | `false` when nothing is in progress |
| `listPasses()` | `Promise<ApplePaymentPass[]>` | `passes(of: .secureElement)` |
| `listRemotePasses()` | `Promise<ApplePaymentPass[]>` | `remoteSecureElementPasses` (paired devices) |
| `isCardTokenized(fpanId)` | `Promise<boolean>` | Any device |
| `isWatchPaired()` | `Promise<boolean>` | |
| `activatePass(dpanId, activationData)` | `Promise<boolean>` | In-App Verification |
| `openWallet()` / `getEnvironment()` | | |
| `onProvisioningCertificates` / `onProvisioningResult` / `onLog` | subscription | or `useAppleWalletListener` / `useAppleWalletLogListener` |

Errors carry `code` from `AppleWalletErrorCode` (`ENTITLEMENT_MISSING`, `NO_SECURE_ELEMENT`, `ALREADY_IN_PROGRESS`, `NOT_IN_PROGRESS`, `INVALID_CONFIGURATION`, `INVALID_SECURE_PAYLOAD`, `HOST_TIMEOUT`, `USER_CANCELED`, `PASS_NOT_FOUND`, `ACTIVATION_FAILED`, ...).

Wallet Extensions (the issuer entries shown inside the Wallet app) run in a separate process and are native Swift in the app; they are not part of this library.

#### Mock (Debug builds)

In Debug builds where PassKit cannot add cards (Simulator, or a device without the entitlement) the library uses a native mock: a look-alike sheet (with a `MOCK` badge) and an in-memory pass library. Release builds do not compile the mock.

```typescript
import {
  AppleWalletImplementation,
  AppleWalletMockControl,
  AppleWalletMockScenario,
} from '@platformbuilders/wallet-bridge-react-native/testing';

await AppleWalletMockControl.configure({
  scenario: AppleWalletMockScenario.MULTIPLE_PENDING,
  primaryAccountIdentifiers: ['FPANID_OF_MY_CARD'], // match your cards
});
await AppleWalletMockControl.setImplementation(AppleWalletImplementation.REAL);
```

Scenarios: `HAPPY_PUSH`, `PHONE_AND_WATCH`, `MIXED_DEVICE_STATES`, `MULTIPLE_PENDING`, `DEACTIVATED`, `ACTIVATING`, `USER_CANCELED`, `ENTITLEMENT_MISSING`, `NO_SECURE_ELEMENT`, `ALREADY_ADDED`, `INVALID_ACTIVATION_DATA`, `HOST_TIMEOUT`, `ACTIVATION_FAILED`. In Release the control methods reject with `NOT_SUPPORTED_ON_PLATFORM`.

For Jest, `mockAppleWalletModule` and local + remote pass fixtures are exported from `src/__tests__/__mocks__`.

#### Example app and Wallet mock app

- `yarn build:app-example:ios` runs the example; the Apple screen has Push, Verification and Passes flows and the mock panel.
- `yarn build:apple-wallet-mock` builds the Wallet mock app on the booted Simulator. It opens `builderswalletexample://apple-wallet/verify?deviceAccountIdentifier=<DPANID>` as the Wallet would through the pass `appLaunchURL`. Set `APPLE_WALLET_MOCK_TARGET_SCHEME` to target another app.

### App2App (Manual Provisioning)

```javascript
import { GoogleWalletModule, GoogleWalletEventEmitter } from '@platformbuilders/wallet-bridge-react-native';

// Listen to events first: setIntentListener delivers the pending intent
const eventEmitter = new GoogleWalletEventEmitter();
const removeListener = eventEmitter.addIntentListener((event) => {
  if (event.type === 'ACTIVATE_TOKEN') {
    const activationParams = JSON.parse(event.data);
    // Process activation
  }
});

// Then activate the listener
await GoogleWalletModule.setIntentListener();

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

All hooks are no-ops outside Android.

> 🔒 Intent events carry provisioning data. Never log, persist or send them to analytics: hand them to your provisioning flow only.

### useGoogleWalletListener

Hook to handle Google Wallet App2App intents.

```typescript
import { useGoogleWalletListener } from '@platformbuilders/wallet-bridge-react-native';

function App() {
  const { isCheckingPendingData } = useGoogleWalletListener(
    (event) => {
      if (event.type === 'ACTIVATE_TOKEN') {
        startManualProvisioning(event);
      }
    },
    () => clearWalletFlow(), // optional: the app was opened without an intent
    () => showNoIntentError(), // optional: valid caller, but no intent data
    { enabled: isGoogleWalletEnabled }, // optional, default true
  );

  return <View>...</View>;
}
```

| Parameter | Description |
|-----------|-------------|
| `onIntentReceived(event)` | Called with the intent that opened the app |
| `onNoIntentReceived()` | Optional. The app was opened without a wallet intent |
| `onValidCallerNoIntentReceived()` | Optional. The caller is a valid wallet, but no intent data arrived |
| `options.enabled` | Optional, default `true`. When `false`, nothing is subscribed and the native listener is not started |

Returns `{ isCheckingPendingData }`, which is `true` while the pending intent is being requested from the native side.

**Behavior:**

- Registers the JS listeners **before** `setIntentListener`, so the pending intent that opened the app is not lost
- Always calls the latest callbacks, without subscribing again on every render
- Removes the listeners and calls `removeIntentListener` on unmount

### useGoogleWalletLogListener

Hook to capture Google Wallet diagnostic logs. Keep it disabled in production builds.

```typescript
import { useGoogleWalletLogListener } from '@platformbuilders/wallet-bridge-react-native';

function DebugPanel() {
  const [logs, setLogs] = useState([]);

  useGoogleWalletLogListener(
    (log) => setLogs((prev) => [...prev, log]),
    { enabled: __DEV__ },
  );

  return <LogView logs={logs} />;
}
```

| Parameter | Description |
|-----------|-------------|
| `onLogReceived(log)` | Called for each native log event |
| `options.enabled` | Optional, default `true`. When `false`, nothing is subscribed and native logging is not started |

Returns `{ isListeningLogs }`, which is `true` while native logging is active (after the native listener starts, until the hook is disabled or unmounted).

### useSamsungWalletListener

Same signature and behavior as `useGoogleWalletListener`, for Samsung Wallet App2App intents.

### useSamsungWalletLogListener

Same signature and behavior as `useGoogleWalletLogListener`, for Samsung Wallet logs.

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
// Listen to logs via EventEmitter (before activating)
const logEmitter = new GoogleWalletEventEmitter();
const removeLogListener = logEmitter.addLogListener((log) => {
  debugLogStore.add(log); // your dev-only log viewer
});

// Activate logging
await GoogleWalletModule.setLogListener();

// Deactivate when not needed
removeLogListener();
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

For development, enable the log hooks only in debug builds and send the logs to a dev-only viewer:

```typescript
// At the root of the app (App.tsx)
useGoogleWalletLogListener(debugLogStore.add, { enabled: __DEV__ });
useSamsungWalletLogListener(debugLogStore.add, { enabled: __DEV__ });
```

Native logs go through `WalletLogger`, which is suppressed in non-debuggable builds. Do not forward wallet events or logs to the console, crash reporting or analytics in production.

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
