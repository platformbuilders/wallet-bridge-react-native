import { Platform } from 'react-native';
import {
  type AppleProvisioningStatus,
  AppleWalletErrorCode,
} from '../enums/apple-wallet.enum';
import { getNativeAppleWalletMock } from '../native-modules';
import type {
  ApplePaymentPass,
  AppleWalletError,
} from '../types/apple-wallet.types';

/** Implementação nativa ativa. `MOCK` só existe em build Debug. */
export enum AppleWalletImplementation {
  REAL = 'real',
  MOCK = 'mock',
}

/** Cenários do mock nativo. Espelha `AWScenario` (Swift). */
export enum AppleWalletMockScenario {
  /** Wallet vazia; push conclui no iPhone. */
  HAPPY_PUSH = 'HAPPY_PUSH',
  /** Watch pareado; a sheet oferece adicionar também ao Watch. */
  PHONE_AND_WATCH = 'PHONE_AND_WATCH',
  /** Mesmo cartão no iPhone (ativo) e no Watch (pendente). */
  MIXED_DEVICE_STATES = 'MIXED_DEVICE_STATES',
  /** Vários passes pendentes de ativação, em devices diferentes. */
  MULTIPLE_PENDING = 'MULTIPLE_PENDING',
  /** Um pass desativado e um ativo. */
  DEACTIVATED = 'DEACTIVATED',
  /** Um pass em ativação. */
  ACTIVATING = 'ACTIVATING',
  /** Usuário cancela na sheet. */
  USER_CANCELED = 'USER_CANCELED',
  ENTITLEMENT_MISSING = 'ENTITLEMENT_MISSING',
  NO_SECURE_ELEMENT = 'NO_SECURE_ELEMENT',
  /** `canAddCard` responde ALREADY_ADDED (o provisioning segue: decidir é do app). */
  ALREADY_ADDED = 'ALREADY_ADDED',
  /** O PassKit rejeita o passo 2. */
  INVALID_ACTIVATION_DATA = 'INVALID_ACTIVATION_DATA',
  /** O emissor não responde o passo 2 a tempo. */
  HOST_TIMEOUT = 'HOST_TIMEOUT',
  /** `activatePass` falha. */
  ACTIVATION_FAILED = 'ACTIVATION_FAILED',
}

export interface AppleWalletMockConfig {
  scenario?: AppleWalletMockScenario;
  /** Latência simulada das transições, em ms. */
  latencyMs?: number;
  /** Prazo para o emissor responder o passo 2, em ms. */
  hostTimeoutMs?: number;
  /** FPANIDs usados nos passes semeados pelo cenário (casar com os cartões do app). */
  primaryAccountIdentifiers?: string[];
}

export interface AppleWalletMockState {
  implementation: AppleWalletImplementation;
  scenario: AppleWalletMockScenario;
  scenarios: AppleWalletMockScenario[];
  latencyMs: number;
  hostTimeoutMs: number;
  isWatchPaired: boolean;
  passes: ApplePaymentPass[];
  pendingRequestId: string | null;
  pendingStage: AppleProvisioningStatus;
}

const nativeMock = () => {
  const module = Platform.OS === 'ios' ? getNativeAppleWalletMock() : null;
  if (!module) {
    const error = new Error(
      'AppleWalletMock is only available on iOS.',
    ) as AppleWalletError;
    error.code = AppleWalletErrorCode.NOT_SUPPORTED_ON_PLATFORM;
    throw error;
  }
  return module;
};

/**
 * Controle do mock nativo do Apple Wallet (build Debug). No Simulator e em
 * device sem o entitlement de provisioning, o mock já é a implementação
 * padrão; `setImplementation` troca em tempo de execução.
 */
export const AppleWalletMockControl = {
  getImplementation: async (): Promise<AppleWalletImplementation> =>
    (await nativeMock().getImplementation()) as AppleWalletImplementation,

  setImplementation: async (
    name: AppleWalletImplementation,
  ): Promise<boolean> => nativeMock().setImplementation(name),

  /** Ajusta cenário / latência e semeia os passes do cenário. */
  configure: async (
    config: AppleWalletMockConfig,
  ): Promise<AppleWalletMockState> =>
    (await nativeMock().configure(config)) as AppleWalletMockState,

  reset: async (): Promise<boolean> => nativeMock().reset(),

  getState: async (): Promise<AppleWalletMockState> =>
    (await nativeMock().getState()) as AppleWalletMockState,
};
