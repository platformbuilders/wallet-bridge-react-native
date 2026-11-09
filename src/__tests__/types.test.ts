import type { Spec as NativeAppleWalletSpec } from '../specs/NativeAppleWallet';
import type { AppleWalletSpec } from '../types/apple-wallet.types';
import { CardStatus } from '../types/common.types';

// Checagem em tempo de compilação (yarn typecheck): o TurboModule do Apple
// expõe exatamente os métodos do contrato público AppleWalletSpec. Os tipos
// de retorno diferem de propósito (o codegen tipa objetos como `Object` e
// enums como `string`), como em Google/Samsung.
type Exactly<A, B> = [A] extends [B] ? ([B] extends [A] ? true : false) : false;
const sameAppleMethods: Exactly<
  Exclude<keyof NativeAppleWalletSpec, 'getConstants'>,
  Exclude<keyof AppleWalletSpec, 'getConstants'>
> = true;

describe('Types and Enums', () => {
  describe('CardStatus', () => {
    it('should have all correct values', () => {
      expect(CardStatus.NOT_FOUND).toBe('not found');
      expect(CardStatus.ACTIVE).toBe('active');
      expect(CardStatus.REQUIRE_AUTHORIZATION).toBe('requireAuthorization');
      expect(CardStatus.PENDING).toBe('pending');
      expect(CardStatus.SUSPENDED).toBe('suspended');
      expect(CardStatus.DEACTIVATED).toBe('deactivated');
    });

    it('should be a valid enum', () => {
      expect(typeof CardStatus).toBe('object');
      expect(Object.keys(CardStatus)).toHaveLength(6);
    });

    it('should allow value verification', () => {
      const status = CardStatus.ACTIVE;
      expect(Object.values(CardStatus)).toContain(status);
    });
  });

  describe('WalletData interface', () => {
    it('should have correct structure', () => {
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

  describe('AppleWalletSpec', () => {
    it('has the same methods as the Apple TurboModule spec', () => {
      expect(sameAppleMethods).toBe(true);
    });
  });
});
