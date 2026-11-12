import { AppleWalletErrorCode } from '../enums/apple-wallet.enum';
import {
  mockApplePasses,
  mockAppleRemotePasses,
  mockAppleWalletModule,
  mockGoogleWalletModule,
  mockSamsungWalletModule,
  resetAllMocks,
  setupErrorMocks,
  setupSuccessMocks,
  setupUnavailableMocks,
} from './__mocks__';

describe('mock helpers', () => {
  beforeEach(resetAllMocks);

  it('setupSuccessMocks configures every wallet', async () => {
    setupSuccessMocks();

    await expect(
      mockGoogleWalletModule.checkWalletAvailability(),
    ).resolves.toBe(true);
    await expect(
      mockSamsungWalletModule.checkWalletAvailability(),
    ).resolves.toBe(true);
    await expect(mockAppleWalletModule.canAddPaymentPass()).resolves.toBe(true);
    await expect(mockAppleWalletModule.listPasses()).resolves.toBe(
      mockApplePasses,
    );
    await expect(mockAppleWalletModule.listRemotePasses()).resolves.toBe(
      mockAppleRemotePasses,
    );
    await expect(mockAppleWalletModule.startInAppProvisioning()).resolves.toBe(
      'FAKE_REQUEST_ID',
    );
  });

  it('setupErrorMocks rejects the Apple actions with an error code', async () => {
    setupErrorMocks();

    await expect(
      mockGoogleWalletModule.checkWalletAvailability(),
    ).rejects.toThrow();
    const expected = [
      [
        mockAppleWalletModule.startInAppProvisioning,
        AppleWalletErrorCode.ENTITLEMENT_MISSING,
      ],
      [
        mockAppleWalletModule.submitProvisioningData,
        AppleWalletErrorCode.NOT_IN_PROGRESS,
      ],
      [
        mockAppleWalletModule.activatePass,
        AppleWalletErrorCode.ACTIVATION_FAILED,
      ],
    ] as const;
    for (const [action, code] of expected) {
      await expect(action()).rejects.toMatchObject({ code });
    }
    // As consultas do PassKit não rejeitam.
    await expect(mockAppleWalletModule.canAddPaymentPass()).resolves.toBe(true);
  });

  it('setupUnavailableMocks makes every wallet unavailable', async () => {
    setupUnavailableMocks();

    await expect(
      mockGoogleWalletModule.checkWalletAvailability(),
    ).resolves.toBe(false);
    await expect(
      mockSamsungWalletModule.checkWalletAvailability(),
    ).resolves.toBe(false);
    await expect(mockAppleWalletModule.canAddPaymentPass()).resolves.toBe(
      false,
    );
    await expect(mockAppleWalletModule.canAddSecureElementPass()).resolves.toBe(
      false,
    );
    await expect(mockAppleWalletModule.isWatchPaired()).resolves.toBe(false);
    await expect(mockAppleWalletModule.listPasses()).resolves.toEqual([]);
    await expect(mockAppleWalletModule.listRemotePasses()).resolves.toEqual([]);
  });
});
