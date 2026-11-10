// Testa o contrato Apple fora dos hooks: stub Android, Proxy do
// AppleWalletModule no iOS e o entrypoint de testes (controle do mock nativo).
import {
  AppleCanAddCardReason,
  AppleWalletErrorCode,
} from '../enums/apple-wallet.enum';
import {
  mockApplePasses,
  mockAppleRemotePasses,
  mockAppleWalletModule,
  mockReactNative,
} from './__mocks__/index';

type Bridge = typeof import('../NativeBuildersWallet');
type Testing = typeof import('../testing');

/** Carrega um módulo do zero, com as specs Apple devolvendo os módulos dados. */
const load = <T>(
  path: string,
  modules: { apple?: unknown; appleMock?: unknown } = {},
) => {
  let loaded: T | undefined;
  jest.isolateModules(() => {
    jest.doMock('../specs/NativeAppleWallet', () => ({
      __esModule: true,
      default: modules.apple ?? null,
    }));
    // Os services criam os emitters no import; o jest.setup os substitui por
    // objetos, então ficam de fora aqui.
    jest.doMock('../services/google-wallet.service', () => ({}));
    jest.doMock('../services/samsung-wallet.service', () => ({}));
    jest.doMock('../specs/NativeAppleWalletMock', () => ({
      __esModule: true,
      default: modules.appleMock ?? null,
    }));
    loaded = require(path);
  });
  return loaded as T;
};

afterEach(() => {
  mockReactNative.Platform.OS = 'ios';
  jest.clearAllMocks();
  jest.resetModules();
});

describe('AppleWalletModule on Android (stub)', () => {
  beforeEach(() => {
    mockReactNative.Platform.OS = 'android';
  });

  it('does not warn when the library is imported', () => {
    load<Bridge>('../NativeBuildersWallet');
    expect(console.warn).not.toHaveBeenCalled();
  });

  it('answers the queries with neutral values', async () => {
    const { AppleWalletModule } = load<Bridge>('../NativeBuildersWallet');

    await expect(AppleWalletModule.checkWalletAvailability()).resolves.toBe(
      false,
    );
    await expect(AppleWalletModule.canAddCard('FPANID')).resolves.toEqual(
      expect.objectContaining({
        canAdd: false,
        reason: AppleCanAddCardReason.UNKNOWN,
      }),
    );
    await expect(AppleWalletModule.listPasses()).resolves.toEqual([]);
    await expect(AppleWalletModule.listRemotePasses()).resolves.toEqual([]);
    await expect(AppleWalletModule.isCardTokenized('FPANID')).resolves.toBe(
      false,
    );
    await expect(AppleWalletModule.isWatchPaired()).resolves.toBe(false);
    await expect(AppleWalletModule.cancelProvisioning()).resolves.toBe(false);
    expect(AppleWalletModule.getConstants().EVENT_LOG).toBe('');
  });

  it.each([
    [
      'startInAppProvisioning',
      (module: Bridge['AppleWalletModule']) =>
        module.startInAppProvisioning({
          cardholderName: 'NOME',
          primaryAccountSuffix: '1234',
          localizedDescription: 'Cartão',
          paymentNetwork: 'Visa',
        }),
    ],
    [
      'submitProvisioningData',
      (module: Bridge['AppleWalletModule']) =>
        module.submitProvisioningData({
          activationData: 'a',
          encryptedPassData: 'b',
          ephemeralPublicKey: 'c',
        }),
    ],
    [
      'activatePass',
      (module: Bridge['AppleWalletModule']) =>
        module.activatePass('DPANID', 'a'),
    ],
  ])('rejects %s with NOT_SUPPORTED_ON_PLATFORM', async (_name, call) => {
    const { AppleWalletModule } = load<Bridge>('../NativeBuildersWallet');

    await expect(call(AppleWalletModule)).rejects.toMatchObject({
      code: AppleWalletErrorCode.NOT_SUPPORTED_ON_PLATFORM,
    });
  });

  it('returns removable subscriptions for every event', () => {
    const { AppleWalletModule } = load<Bridge>('../NativeBuildersWallet');

    [
      AppleWalletModule.onProvisioningCertificates(jest.fn()),
      AppleWalletModule.onProvisioningResult(jest.fn()),
      AppleWalletModule.onLog(jest.fn()),
    ].forEach((subscription) => {
      expect(() => subscription.remove()).not.toThrow();
    });
  });
});

describe('AppleWalletModule on iOS', () => {
  it('forwards the provisioning and passes calls to the TurboModule', async () => {
    const { AppleWalletModule } = load<Bridge>('../NativeBuildersWallet', {
      apple: mockAppleWalletModule,
    });

    const payload = {
      activationData: 'QQ==',
      encryptedPassData: 'Qg==',
      ephemeralPublicKey: 'Qw==',
    };
    await expect(
      AppleWalletModule.submitProvisioningData(payload),
    ).resolves.toBe(true);
    expect(mockAppleWalletModule.submitProvisioningData).toHaveBeenCalledWith(
      payload,
    );

    await expect(AppleWalletModule.listPasses()).resolves.toBe(mockApplePasses);
    await expect(AppleWalletModule.listRemotePasses()).resolves.toBe(
      mockAppleRemotePasses,
    );
    await expect(
      AppleWalletModule.activatePass('FAKE_DPANID_1_WATCH', 'QQ=='),
    ).resolves.toBe(true);
    expect(mockAppleWalletModule.activatePass).toHaveBeenCalledWith(
      'FAKE_DPANID_1_WATCH',
      'QQ==',
    );
  });
});

describe('AppleWalletMockControl (testing entrypoint)', () => {
  const nativeMock = () => ({
    getImplementation: jest.fn().mockResolvedValue('mock'),
    setImplementation: jest.fn().mockResolvedValue(true),
    configure: jest.fn().mockResolvedValue({ scenario: 'MULTIPLE_PENDING' }),
    reset: jest.fn().mockResolvedValue(true),
    getState: jest.fn().mockResolvedValue({ scenario: 'HAPPY_PUSH' }),
  });

  it('is not exported from the main entry', () => {
    const main = load<Record<string, unknown>>('../index');
    expect(main.AppleWalletMockControl).toBeUndefined();
    expect(main.AppleWalletMockScenario).toBeUndefined();
  });

  it('delegates to the native mock module on iOS', async () => {
    const appleMock = nativeMock();
    const { AppleWalletMockControl, AppleWalletMockScenario } = load<Testing>(
      '../testing',
      { appleMock },
    );

    await expect(AppleWalletMockControl.getImplementation()).resolves.toBe(
      'mock',
    );
    await expect(
      AppleWalletMockControl.configure({
        scenario: AppleWalletMockScenario.MULTIPLE_PENDING,
        primaryAccountIdentifiers: ['FPANID_A'],
      }),
    ).resolves.toEqual({ scenario: 'MULTIPLE_PENDING' });
    expect(appleMock.configure).toHaveBeenCalledWith({
      scenario: 'MULTIPLE_PENDING',
      primaryAccountIdentifiers: ['FPANID_A'],
    });
    await expect(AppleWalletMockControl.reset()).resolves.toBe(true);
    await expect(AppleWalletMockControl.getState()).resolves.toEqual({
      scenario: 'HAPPY_PUSH',
    });
  });

  it('rejects with NOT_SUPPORTED_ON_PLATFORM outside iOS', async () => {
    mockReactNative.Platform.OS = 'android';
    const { AppleWalletMockControl } = load<Testing>('../testing', {
      appleMock: nativeMock(),
    });

    await expect(AppleWalletMockControl.reset()).rejects.toMatchObject({
      code: AppleWalletErrorCode.NOT_SUPPORTED_ON_PLATFORM,
    });
  });

  it('rejects with NOT_SUPPORTED_ON_PLATFORM without the native module', async () => {
    const { AppleWalletMockControl } = load<Testing>('../testing');

    await expect(AppleWalletMockControl.getState()).rejects.toThrow(
      'AppleWalletMock is only available on iOS.',
    );
  });
});
