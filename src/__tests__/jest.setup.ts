// Importa os mocks dos módulos iOS
import {
  mockGoogleWalletEventEmitter,
  mockGoogleWalletModule,
  mockReactNative,
  mockSamsungWalletEventEmitter,
  mockSamsungWalletModule,
} from './__mocks__/index';

// Mock do React Native usando os mocks exportáveis
jest.mock('react-native', () => mockReactNative);

// Mock dos módulos específicos do iOS
jest.mock('../google-wallet.ios', () => ({
  GoogleWalletIOS: mockGoogleWalletModule,
}));

jest.mock('../samsung-wallet.ios', () => ({
  SamsungWalletIOS: mockSamsungWalletModule,
}));

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
