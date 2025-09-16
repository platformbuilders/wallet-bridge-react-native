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
