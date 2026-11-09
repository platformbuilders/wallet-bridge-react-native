// ============================================================================
// TYPES COMUNS
// ============================================================================

export enum CardStatus {
  NOT_FOUND = 'not found',
  ACTIVE = 'active',
  REQUIRE_AUTHORIZATION = 'requireAuthorization',
  PENDING = 'pending',
  SUSPENDED = 'suspended',
  DEACTIVATED = 'deactivated',
}

export interface WalletData {
  deviceID: string;
  walletAccountID: string;
}

/** Opções dos hooks de listener (intent e log) das wallets Android. */
export interface WalletListenerOptions {
  /**
   * Liga o listener. Com `false` o hook não assina nada e não chama o nativo
   * (ex.: logs desligados em produção). Padrão: `true`.
   */
  enabled?: boolean;
}
