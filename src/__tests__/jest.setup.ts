// Importa os mocks exportáveis
import { mockReactNative } from './__mocks__/index';

// Mock do React Native usando os mocks exportáveis
jest.mock('react-native', () => mockReactNative);

// Importa os mocks dos módulos iOS
import {
  mockGoogleWalletModule,
  mockSamsungWalletModule,
} from './__mocks__/index';

// Mock dos módulos específicos do iOS
jest.mock('../google-wallet.ios', () => ({
  GoogleWalletIOS: mockGoogleWalletModule,
}));

jest.mock('../samsung-wallet.ios', () => ({
  SamsungWalletIOS: mockSamsungWalletModule,
}));

// Importa os mocks dos event emitters
import {
  mockGoogleWalletEventEmitter,
  mockSamsungWalletEventEmitter,
} from './__mocks__/index';

// Mock dos event emitters
jest.mock('../event-emitters/google-wallet.ee', () => ({
  GoogleWalletEventEmitter: mockGoogleWalletEventEmitter,
}));

jest.mock('../event-emitters/samsung-wallet.ee', () => ({
  SamsungWalletEventEmitter: mockSamsungWalletEventEmitter,
}));

// Configurações globais do Jest
global.console = {
  ...console,
  warn: jest.fn(),
  error: jest.fn(),
};
