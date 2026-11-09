/* eslint-disable promise/prefer-await-to-callbacks */
// Testa os hooks de listener sem renderer: `react` é mockado para capturar o
// efeito e rodá-lo/limpá-lo na mão, o que basta para checar ordem e cleanup.
import { mockReactNative } from './__mocks__/index';

type Effect = () => (() => void) | undefined;

let mockEffects: Effect[] = [];
// Refs persistem entre "renders" pela ordem de chamada, como no React.
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

const mockCalls: string[] = [];
const mockEmitted: Record<string, (event?: unknown) => void> = {};

const mockEmitter = (prefix: string) => ({
  addIntentListener: jest.fn((cb) => {
    mockCalls.push(`${prefix}:addIntentListener`);
    mockEmitted[`${prefix}:intent`] = cb;
    return () => mockCalls.push(`${prefix}:removeIntentListener`);
  }),
  addNoIntentListener: jest.fn((cb) => {
    mockEmitted[`${prefix}:noIntent`] = cb;
    return () => {};
  }),
  addValidCallerNoIntentListener: jest.fn((cb) => {
    mockEmitted[`${prefix}:validCaller`] = cb;
    return () => {};
  }),
  addLogListener: jest.fn((cb) => {
    mockCalls.push(`${prefix}:addLogListener`);
    mockEmitted[`${prefix}:log`] = cb;
    return () => mockCalls.push(`${prefix}:removeLogListener`);
  }),
});

const mockNative = (name: string) =>
  jest.fn(async () => {
    mockCalls.push(name);
    return true;
  });

jest.mock('../services/google-wallet.service', () => ({
  GoogleWalletService: {
    walletEventEmitter: mockEmitter('google'),
    startIntentListener: mockNative('google:setIntentListener'),
    stopIntentListener: mockNative('google:removeIntentListener'),
    startLogListener: mockNative('google:setLogListener'),
    stopLogListener: mockNative('google:removeLogListener'),
  },
}));

jest.mock('../services/samsung-wallet.service', () => ({
  SamsungWalletService: {
    walletEventEmitter: mockEmitter('samsung'),
    startWalletIntentListener: mockNative('samsung:setIntentListener'),
    stopWalletIntentListener: mockNative('samsung:removeIntentListener'),
    startLogListener: mockNative('samsung:setLogListener'),
    stopLogListener: mockNative('samsung:removeLogListener'),
  },
}));

const {
  useGoogleWalletListener,
  useGoogleWalletLogListener,
  useSamsungWalletListener,
  useSamsungWalletLogListener,
} = require('../hooks') as typeof import('../hooks');

const render = (hook: () => unknown) => {
  mockRefIndex = 0;
  hook();
};

const runEffects = () => mockEffects.map((effect) => effect());

describe('wallet listener hooks', () => {
  const originalOS = mockReactNative.Platform.OS;

  beforeEach(() => {
    mockEffects = [];
    mockRefs.length = 0;
    mockCalls.length = 0;
    mockSetters.length = 0;
    mockReactNative.Platform.OS = 'android';
  });

  afterAll(() => {
    mockReactNative.Platform.OS = originalOS;
  });

  it.each([
    ['google', useGoogleWalletListener],
    ['samsung', useSamsungWalletListener],
  ] as const)(
    '%s: registra os listeners antes de pedir o intent pendente',
    async (prefix, useListener) => {
      useListener(jest.fn());
      const [cleanup] = runEffects();
      await Promise.resolve();

      expect(mockCalls).toEqual([
        `${prefix}:addIntentListener`,
        `${prefix}:setIntentListener`,
      ]);

      cleanup?.();
      expect(mockCalls).toEqual([
        `${prefix}:addIntentListener`,
        `${prefix}:setIntentListener`,
        `${prefix}:removeIntentListener`,
        `${prefix}:removeIntentListener`,
      ]);
    },
  );

  it('chama sempre os callbacks mais recentes', () => {
    const first = jest.fn();
    const latest = jest.fn();
    const noIntent = jest.fn();
    render(() => useGoogleWalletListener(first));
    // O efeito roda uma vez; o segundo render só atualiza o ref.
    const [effect] = mockEffects;
    mockEffects = [];
    render(() => useGoogleWalletListener(latest, noIntent));
    effect?.();

    mockEmitted['google:intent']?.({ action: 'x' });
    mockEmitted['google:noIntent']?.();

    expect(first).not.toHaveBeenCalled();
    expect(latest).toHaveBeenCalledWith({ action: 'x' });
    expect(noIntent).toHaveBeenCalled();
  });

  it.each([
    ['enabled: false', 'android', false],
    ['fora do Android', 'ios', true],
  ] as const)('não faz nada com %s', (_label, os, enabled) => {
    mockReactNative.Platform.OS = os;
    useGoogleWalletListener(jest.fn(), undefined, undefined, { enabled });
    useSamsungWalletLogListener(jest.fn(), { enabled });
    runEffects();
    expect(mockCalls).toEqual([]);
  });

  it.each([
    ['google', useGoogleWalletLogListener],
    ['samsung', useSamsungWalletLogListener],
  ] as const)(
    '%s: log listener registra antes de ligar o log nativo',
    async (prefix, useLogListener) => {
      const onLog = jest.fn();
      useLogListener(onLog);
      const [cleanup] = runEffects();
      await Promise.resolve();

      expect(mockCalls).toEqual([
        `${prefix}:addLogListener`,
        `${prefix}:setLogListener`,
      ]);
      mockEmitted[`${prefix}:log`]?.({ message: 'ok' });
      expect(onLog).toHaveBeenCalledWith({ message: 'ok' });

      cleanup?.();
      expect(mockCalls).toContain(`${prefix}:removeLogListener`);
    },
  );

  it.each([
    ['google', useGoogleWalletLogListener],
    ['samsung', useSamsungWalletLogListener],
  ] as const)(
    '%s: isListeningLogs reflete o log nativo ligado',
    async (_prefix, useLogListener) => {
      useLogListener(jest.fn());
      const [setIsListeningLogs] = mockSetters;
      const [cleanup] = runEffects();
      await Promise.resolve();
      await Promise.resolve();

      expect(setIsListeningLogs).toHaveBeenLastCalledWith(true);

      cleanup?.();
      expect(setIsListeningLogs).toHaveBeenLastCalledWith(false);
    },
  );
});
