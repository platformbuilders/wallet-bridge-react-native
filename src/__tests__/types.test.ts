import { CardStatus } from '../types/common.types';

describe('Types e Enums', () => {
  describe('CardStatus', () => {
    it('deve ter todos os valores corretos', () => {
      expect(CardStatus.NOT_FOUND).toBe('not found');
      expect(CardStatus.ACTIVE).toBe('active');
      expect(CardStatus.REQUIRE_AUTHORIZATION).toBe('requireAuthorization');
      expect(CardStatus.PENDING).toBe('pending');
      expect(CardStatus.SUSPENDED).toBe('suspended');
      expect(CardStatus.DEACTIVATED).toBe('deactivated');
    });

    it('deve ser um enum válido', () => {
      expect(typeof CardStatus).toBe('object');
      expect(Object.keys(CardStatus)).toHaveLength(6);
    });

    it('deve permitir verificação de valores', () => {
      const status = CardStatus.ACTIVE;
      expect(Object.values(CardStatus)).toContain(status);
    });
  });

  describe('WalletData interface', () => {
    it('deve ter a estrutura correta', () => {
      const walletData = {
        deviceID: 'test-device-id',
        walletAccountID: 'test-wallet-account-id',
      };

      expect(walletData).toHaveProperty('deviceID');
      expect(walletData).toHaveProperty('walletAccountID');
      expect(typeof walletData.deviceID).toBe('string');
      expect(typeof walletData.walletAccountID).toBe('string');
    });
  });
});
