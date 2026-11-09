// Testa o carregamento sob demanda dos TurboModules (native-modules.ts) e o
// Proxy que expõe GoogleWalletModule / SamsungWalletModule.
import { mockReactNative } from './__mocks__/index';

const SPECS = {
  Google: '../specs/NativeGoogleWallet',
  Samsung: '../specs/NativeSamsungWallet',
} as const;

type Wallet = keyof typeof SPECS;

/** Carrega um módulo do zero, com as specs devolvendo `modules[wallet]`. */
const load = <T>(path: string, modules: Partial<Record<Wallet, unknown>>) => {
  const specLoads: Wallet[] = [];
  let loaded: T | undefined;
  jest.isolateModules(() => {
    (Object.keys(SPECS) as Wallet[]).forEach((wallet) => {
      jest.doMock(SPECS[wallet], () => {
        specLoads.push(wallet);
        return { __esModule: true, default: modules[wallet] ?? null };
      });
    });
    loaded = require(path);
  });
  return { loaded: loaded as T, specLoads };
};

type NativeModules = typeof import('../native-modules');
type Bridge = typeof import('../NativeBuildersWallet');

afterEach(() => {
  mockReactNative.Platform.OS = 'ios';
  jest.clearAllMocks();
  // Os mocks das specs (doMock) ficam no registry; cada teste carrega do zero.
  jest.resetModules();
});

describe.each([
  ['Google', 'getNativeGoogleWallet', 'GoogleWallet'],
  ['Samsung', 'getNativeSamsungWallet', 'SamsungWallet'],
] as const)('native-modules: %s', (wallet, getter, name) => {
  it('returns null outside Android without loading the spec', () => {
    mockReactNative.Platform.OS = 'ios';
    const { loaded, specLoads } = load<NativeModules>('../native-modules', {
      [wallet]: { getConstants: jest.fn() },
    });

    expect(loaded[getter]()).toBeNull();
    expect(specLoads).toEqual([]);
  });

  it('returns the TurboModule on Android', () => {
    mockReactNative.Platform.OS = 'android';
    const nativeModule = { getConstants: jest.fn() };
    const { loaded } = load<NativeModules>('../native-modules', {
      [wallet]: nativeModule,
    });

    expect(loaded[getter]()).toBe(nativeModule);
    expect(console.warn).not.toHaveBeenCalled();
  });

  it('returns null and warns only once when the module is missing', () => {
    mockReactNative.Platform.OS = 'android';
    const { loaded } = load<NativeModules>('../native-modules', {});

    expect(loaded[getter]()).toBeNull();
    expect(loaded[getter]()).toBeNull();
    expect(console.warn).toHaveBeenCalledTimes(1);
    expect(console.warn).toHaveBeenCalledWith(
      expect.stringContaining(`${name} native module is not available`),
    );
  });
});

describe.each([
  ['Google', 'GoogleWalletModule'],
  ['Samsung', 'SamsungWalletModule'],
] as const)(
  'NativeBuildersWallet: %sWalletModule proxy',
  (wallet, exportName) => {
    it('does not load the TurboModule on import', () => {
      mockReactNative.Platform.OS = 'android';
      const { specLoads } = load<Bridge>('../NativeBuildersWallet', {
        [wallet]: { getConstants: jest.fn() },
      });

      expect(specLoads).toEqual([]);
    });

    it('forwards method calls to the TurboModule on Android', async () => {
      mockReactNative.Platform.OS = 'android';
      const checkWalletAvailability = jest.fn().mockResolvedValue(true);
      const { loaded } = load<Bridge>('../NativeBuildersWallet', {
        [wallet]: { checkWalletAvailability },
      });

      await expect(loaded[exportName].checkWalletAvailability()).resolves.toBe(
        true,
      );
      expect(checkWalletAvailability).toHaveBeenCalledTimes(1);
    });

    it('resolves members to undefined when the module is missing', () => {
      mockReactNative.Platform.OS = 'android';
      const { loaded } = load<Bridge>('../NativeBuildersWallet', {});

      expect(loaded[exportName].checkWalletAvailability).toBeUndefined();
    });

    it('uses the iOS stub on iOS', () => {
      mockReactNative.Platform.OS = 'ios';
      const nativeModule = { checkWalletAvailability: jest.fn() };
      const { loaded, specLoads } = load<Bridge>('../NativeBuildersWallet', {
        [wallet]: nativeModule,
      });

      expect(loaded[exportName].checkWalletAvailability).not.toBe(
        nativeModule.checkWalletAvailability,
      );
      expect(loaded[exportName].checkWalletAvailability).toBeDefined();
      expect(specLoads).toEqual([]);
    });
  },
);
