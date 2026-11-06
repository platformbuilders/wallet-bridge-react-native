const loadSpec = (specPath: string, registered: unknown) => {
  const get = jest.fn().mockReturnValue(registered);
  const getEnforcing = jest.fn();
  let spec: { default: unknown } | undefined;

  jest.isolateModules(() => {
    jest.doMock('react-native', () => ({
      TurboModuleRegistry: { get, getEnforcing },
    }));
    spec = require(specPath);
  });

  return { spec, get, getEnforcing };
};

describe.each([
  ['../specs/NativeAppleWallet', 'AppleWallet', 'Android'],
  ['../specs/NativeGoogleWallet', 'GoogleWallet', 'iOS'],
  ['../specs/NativeSamsungWallet', 'SamsungWallet', 'iOS'],
])('%s', (specPath, moduleName, missingOn) => {
  afterEach(() => {
    jest.resetModules();
  });

  it(`looks up the ${moduleName} TurboModule without enforcing it`, () => {
    const fakeModule = { getConstants: jest.fn() };
    const { spec, get, getEnforcing } = loadSpec(specPath, fakeModule);

    expect(spec?.default).toBe(fakeModule);
    expect(get).toHaveBeenCalledWith(moduleName);
    expect(getEnforcing).not.toHaveBeenCalled();
  });

  it(`resolves to null where the module is not registered (${missingOn})`, () => {
    const { spec } = loadSpec(specPath, null);

    expect(spec?.default).toBeNull();
  });
});
