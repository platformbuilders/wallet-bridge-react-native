// Testa os hooks do Apple Wallet sem renderer: `react` é mockado para capturar
// o efeito e rodá-lo/limpá-lo na mão, o que basta para checar ordem e cleanup.
import {
  AppleProvisioningStatus,
  AppleWalletErrorCode,
} from '../enums/apple-wallet.enum';
import { mockAppleWalletModule, mockReactNative } from './__mocks__/index';

type Effect = () => (() => void) | undefined;

let mockEffects: Effect[] = [];
const mockRefs: { current: unknown }[] = [];
let mockRefIndex = 0;
const mockSetters: jest.Mock[] = [];

jest.mock('react', () => ({
  useEffect: (effect: Effect) => {
    mockEffects.push(effect);
  },
  useRef: (value: unknown) => {
    const index = mockRefIndex++;
    mockRefs[index] ??= { current: value };
    return mockRefs[index];
  },
  useState: (value: unknown) => {
    const setter = jest.fn();
    mockSetters.push(setter);
    return [value, setter];
  },
}));

jest.mock('../NativeBuildersWallet', () => ({
  AppleWalletModule: mockAppleWalletModule,
}));

// Importa os hooks Apple direto: o barrel carrega os services Google/Samsung.
const { useAppleWalletListener } =
  require('../hooks/useAppleWalletListener') as typeof import('../hooks/useAppleWalletListener');
const { useAppleWalletLogListener } =
  require('../hooks/useAppleWalletLogListener') as typeof import('../hooks/useAppleWalletLogListener');

const flush = () => new Promise((resolve) => setImmediate(resolve));

const render = (hook: () => unknown) => {
  mockRefIndex = 0;
  return hook();
};

/** Último listener que o hook registrou em `module[method]`. */
const lastListener = (method: jest.Mock) =>
  method.mock.calls[method.mock.calls.length - 1]?.[0] as (
    event: unknown,
  ) => void;

beforeEach(() => {
  mockEffects = [];
  mockRefs.length = 0;
  mockSetters.length = 0;
  mockReactNative.Platform.OS = 'ios';
  jest.clearAllMocks();
});

describe('useAppleWalletListener', () => {
  it('subscribes to both provisioning events and removes them on cleanup', () => {
    render(() =>
      useAppleWalletListener({
        onProvisioningCertificates: jest.fn(),
        onProvisioningResult: jest.fn(),
      }),
    );

    const cleanup = mockEffects[0]?.();
    expect(
      mockAppleWalletModule.onProvisioningCertificates,
    ).toHaveBeenCalledTimes(1);
    expect(mockAppleWalletModule.onProvisioningResult).toHaveBeenCalledTimes(1);

    const subscriptions = [
      mockAppleWalletModule.onProvisioningCertificates.mock.results[0]?.value,
      mockAppleWalletModule.onProvisioningResult.mock.results[0]?.value,
    ];
    cleanup?.();
    subscriptions.forEach((subscription) =>
      expect(subscription.remove).toHaveBeenCalledTimes(1),
    );
  });

  it('always calls the latest callbacks', () => {
    const first = jest.fn();
    const latest = jest.fn();
    const onResult = jest.fn();

    render(() => useAppleWalletListener({ onProvisioningCertificates: first }));
    mockEffects[0]?.();
    render(() =>
      useAppleWalletListener({
        onProvisioningCertificates: latest,
        onProvisioningResult: onResult,
      }),
    );

    const certificates = {
      requestId: 'REQ',
      certificates: ['Y2VydA=='],
      nonce: 'bm9uY2U=',
      nonceSignature: 'c2ln',
    };
    lastListener(mockAppleWalletModule.onProvisioningCertificates)(
      certificates,
    );
    const result = {
      requestId: 'REQ',
      status: AppleProvisioningStatus.FAILED,
      additionalPasses: [],
      errorCode: AppleWalletErrorCode.INVALID_SECURE_PAYLOAD,
    };
    lastListener(mockAppleWalletModule.onProvisioningResult)(result);

    expect(first).not.toHaveBeenCalled();
    expect(latest).toHaveBeenCalledWith(certificates);
    expect(onResult).toHaveBeenCalledWith(result);
  });

  it('does nothing when disabled or outside iOS', () => {
    render(() =>
      useAppleWalletListener(
        { onProvisioningCertificates: jest.fn() },
        { enabled: false },
      ),
    );
    expect(mockEffects[0]?.()).toBeUndefined();

    mockReactNative.Platform.OS = 'android';
    render(() =>
      useAppleWalletListener({ onProvisioningCertificates: jest.fn() }),
    );
    expect(mockEffects[1]?.()).toBeUndefined();

    expect(
      mockAppleWalletModule.onProvisioningCertificates,
    ).not.toHaveBeenCalled();
  });
});

describe('useAppleWalletLogListener', () => {
  it('subscribes before enabling native logs and disables them on cleanup', async () => {
    const order: string[] = [];
    mockAppleWalletModule.onLog.mockImplementationOnce(() => {
      order.push('onLog');
      return { remove: jest.fn(() => order.push('remove')) };
    });
    mockAppleWalletModule.setLogListener.mockImplementationOnce(async () => {
      order.push('setLogListener');
      return true;
    });
    mockAppleWalletModule.removeLogListener.mockImplementationOnce(async () => {
      order.push('removeLogListener');
      return true;
    });

    const onLog = jest.fn();
    render(() => useAppleWalletLogListener(onLog));
    const cleanup = mockEffects[0]?.();
    await flush();

    expect(mockSetters[0]).toHaveBeenCalledWith(true);

    const event = { level: 'INFO', tag: 'AppleWallet-Mock', message: 'ok' };
    lastListener(mockAppleWalletModule.onLog)(event);
    expect(onLog).toHaveBeenCalledWith(event);

    cleanup?.();
    expect(mockSetters[0]).toHaveBeenLastCalledWith(false);
    expect(order).toEqual([
      'onLog',
      'setLogListener',
      'remove',
      'removeLogListener',
    ]);
  });

  it('does nothing when disabled', () => {
    render(() => useAppleWalletLogListener(jest.fn(), { enabled: false }));
    expect(mockEffects[0]?.()).toBeUndefined();
    expect(mockAppleWalletModule.setLogListener).not.toHaveBeenCalled();
  });
});
