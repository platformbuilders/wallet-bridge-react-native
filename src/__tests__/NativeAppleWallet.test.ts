const loadSpec = (registered: unknown) => {
  const get = jest.fn().mockReturnValue(registered);
  const getEnforcing = jest.fn();
  let spec: { default: unknown } | undefined;

  jest.isolateModules(() => {
    jest.doMock('react-native', () => ({
      TurboModuleRegistry: { get, getEnforcing },
    }));
    spec = require('../specs/NativeAppleWallet');
  });

  return { spec, get, getEnforcing };
};

describe('NativeAppleWallet spec', () => {
  afterEach(() => {
    jest.resetModules();
  });

  it('looks up the AppleWallet TurboModule without enforcing it', () => {
    const fakeModule = { getEnvironment: jest.fn() };
    const { spec, get, getEnforcing } = loadSpec(fakeModule);

    expect(spec?.default).toBe(fakeModule);
    expect(get).toHaveBeenCalledWith('AppleWallet');
    expect(getEnforcing).not.toHaveBeenCalled();
  });

  it('resolves to null where the module is not registered (Android)', () => {
    const { spec } = loadSpec(null);

    expect(spec?.default).toBeNull();
  });
});
