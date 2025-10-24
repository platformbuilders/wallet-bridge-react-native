import {
  GoogleWalletModule,
  SamsungWalletModule,
} from '../NativeBuildersWallet';
import { CardStatus } from '../types/common.types';

describe('BuildersWallet - Setup Test', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Native modules', () => {
    it('should export GoogleWalletModule', () => {
      expect(GoogleWalletModule).toBeDefined();
      expect(typeof GoogleWalletModule).toBe('object');
    });

    it('should export SamsungWalletModule', () => {
      expect(SamsungWalletModule).toBeDefined();
      expect(typeof SamsungWalletModule).toBe('object');
    });
  });

  describe('Types and Enums', () => {
    it('should export CardStatus enum', () => {
      expect(CardStatus).toBeDefined();
      expect(CardStatus.ACTIVE).toBe('active');
      expect(CardStatus.NOT_FOUND).toBe('not found');
      expect(CardStatus.REQUIRE_AUTHORIZATION).toBe('requireAuthorization');
    });
  });

  describe('Native modules mocks', () => {
    it('should have mocked methods in GoogleWalletModule', () => {
      expect(GoogleWalletModule.checkWalletAvailability).toBeDefined();
      expect(GoogleWalletModule.getSecureWalletInfo).toBeDefined();
      expect(GoogleWalletModule.getTokenStatus).toBeDefined();
      expect(GoogleWalletModule.getEnvironment).toBeDefined();
      expect(GoogleWalletModule.isTokenized).toBeDefined();
      expect(GoogleWalletModule.viewToken).toBeDefined();
      expect(GoogleWalletModule.addCardToWallet).toBeDefined();
      expect(GoogleWalletModule.createWalletIfNeeded).toBeDefined();
      expect(GoogleWalletModule.listTokens).toBeDefined();
      expect(GoogleWalletModule.getConstants).toBeDefined();
    });

    it('should have mocked methods in SamsungWalletModule', () => {
      expect(SamsungWalletModule.init).toBeDefined();
      expect(SamsungWalletModule.getSamsungPayStatus).toBeDefined();
      expect(SamsungWalletModule.goToUpdatePage).toBeDefined();
      expect(SamsungWalletModule.activateSamsungPay).toBeDefined();
      expect(SamsungWalletModule.getAllCards).toBeDefined();
      expect(SamsungWalletModule.getWalletInfo).toBeDefined();
      expect(SamsungWalletModule.addCard).toBeDefined();
      expect(SamsungWalletModule.checkWalletAvailability).toBeDefined();
      expect(SamsungWalletModule.getConstants).toBeDefined();
    });
  });
});
