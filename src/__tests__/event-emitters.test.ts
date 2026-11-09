/* eslint-disable promise/prefer-await-to-callbacks */
// Testa os event emitters reais (o jest.setup os substitui por mocks) sobre
// um TurboModule falso: cada `on*` guarda o callback e devolve uma assinatura.
import { mockReactNative } from './__mocks__/index';

type Callback = (event?: unknown) => void;

const EVENTS = [
  'onIntentReceived',
  'onNoIntentReceived',
  'onValidCallerNoIntentReceived',
  'onLog',
] as const;

const createFakeModule = () => {
  const callbacks: Partial<Record<(typeof EVENTS)[number], Callback>> = {};
  const removes: jest.Mock[] = [];
  const module = Object.fromEntries(
    EVENTS.map((name) => [
      name,
      jest.fn((callback: Callback) => {
        callbacks[name] = callback;
        const remove = jest.fn();
        removes.push(remove);
        return { remove };
      }),
    ]),
  );
  return { module, callbacks, removes };
};

type Emitter = {
  addIntentListener: (cb: Callback) => () => void;
  addNoIntentListener: (cb: Callback) => () => void;
  addValidCallerNoIntentListener: (cb: Callback) => () => void;
  addLogListener: (cb: Callback) => () => void;
  removeAllListeners: () => void;
  getListenerCount: () => number;
  isAvailable: () => boolean;
};

describe.each([
  ['google-wallet.ee', 'GoogleWalletEventEmitter', 'getNativeGoogleWallet'],
  ['samsung-wallet.ee', 'SamsungWalletEventEmitter', 'getNativeSamsungWallet'],
])('%s', (file, className, getterName) => {
  let getNative: jest.Mock;

  const createEmitter = (nativeModule: object | null): Emitter => {
    getNative = jest.fn(() => nativeModule);
    let emitter: Emitter | undefined;
    jest.isolateModules(() => {
      jest.doMock('../native-modules', () => ({
        getNativeGoogleWallet:
          getterName === 'getNativeGoogleWallet' ? getNative : jest.fn(),
        getNativeSamsungWallet:
          getterName === 'getNativeSamsungWallet' ? getNative : jest.fn(),
      }));
      const EmitterClass = jest.requireActual(`../event-emitters/${file}`)[
        className
      ];
      emitter = new EmitterClass();
    });
    return emitter as Emitter;
  };

  beforeEach(() => {
    mockReactNative.Platform.OS = 'android';
    jest.spyOn(console, 'log').mockImplementation(() => {});
  });

  afterEach(() => {
    mockReactNative.Platform.OS = 'ios';
    jest.restoreAllMocks();
    jest.resetModules();
  });

  it('resolves the native module on first use, not in the constructor', () => {
    const { module } = createFakeModule();
    const emitter = createEmitter(module);

    expect(getNative).not.toHaveBeenCalled();
    emitter.addIntentListener(jest.fn());
    expect(getNative).toHaveBeenCalled();
  });

  it.each([
    ['addIntentListener', 'onIntentReceived', { type: 'ACTIVATE_TOKEN' }],
    ['addNoIntentListener', 'onNoIntentReceived', undefined],
    [
      'addValidCallerNoIntentListener',
      'onValidCallerNoIntentReceived',
      undefined,
    ],
    ['addLogListener', 'onLog', { level: 'INFO', message: 'm' }],
  ] as const)(
    '%s subscribes to %s and forwards the event',
    (method, event, payload) => {
      const { module, callbacks } = createFakeModule();
      const emitter = createEmitter(module);
      const callback = jest.fn();

      emitter[method](callback);
      callbacks[event]?.(payload);

      expect(callback).toHaveBeenCalledTimes(1);
      if (payload !== undefined) expect(callback).toHaveBeenCalledWith(payload);
      expect(emitter.getListenerCount()).toBe(1);
    },
  );

  it('removes the native subscription when the listener is removed', () => {
    const { module, removes } = createFakeModule();
    const emitter = createEmitter(module);

    const removeListener = emitter.addIntentListener(jest.fn());
    removeListener();

    expect(removes[0]).toHaveBeenCalledTimes(1);
    expect(emitter.getListenerCount()).toBe(0);
  });

  it('removeAllListeners removes every native subscription', () => {
    const { module, removes } = createFakeModule();
    const emitter = createEmitter(module);

    emitter.addIntentListener(jest.fn());
    emitter.addNoIntentListener(jest.fn());
    emitter.addValidCallerNoIntentListener(jest.fn());
    emitter.addLogListener(jest.fn());
    emitter.removeAllListeners();

    expect(removes).toHaveLength(4);
    removes.forEach((remove) => expect(remove).toHaveBeenCalledTimes(1));
    expect(emitter.getListenerCount()).toBe(0);
  });

  it('returns a no-op remover when the native module is unavailable', () => {
    const emitter = createEmitter(null);

    const removeListener = emitter.addIntentListener(jest.fn());

    expect(() => removeListener()).not.toThrow();
    expect(emitter.isAvailable()).toBe(false);
    expect(emitter.getListenerCount()).toBe(0);
  });

  it('does not touch the native module on iOS', () => {
    mockReactNative.Platform.OS = 'ios';
    const { module } = createFakeModule();
    const emitter = createEmitter(module);

    emitter.addIntentListener(jest.fn());

    expect(getNative).not.toHaveBeenCalled();
    expect(emitter.getListenerCount()).toBe(0);
  });
});
