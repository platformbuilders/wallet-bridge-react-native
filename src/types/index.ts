// ============================================================================
// TYPES PRINCIPAIS - RE-EXPORT DOS TYPES ESPECÍFICOS
// ============================================================================

// Google Wallet Types
export * from './google-wallet.types';

// Samsung Wallet Types
export * from './samsung-wallet.types';

// Common Types e Compatibilidade
export * from './common.types';

// Exportação específica do GoogleWalletIntentEvent para garantir disponibilidade
export type { GoogleWalletIntentEvent } from './google-wallet.types';

// ============================================================================
// NOTA: ESTE ARQUIVO MANTÉM COMPATIBILIDADE
// ============================================================================
// 
// Este arquivo re-exporta todos os types dos arquivos separados para manter
// compatibilidade com imports existentes como:
// import { GoogleWalletStatus } from '@platformbuilders/wallet-bridge-react-native'
//
// Para melhor organização, use imports específicos:
// import { GoogleWalletStatus } from '@platformbuilders/wallet-bridge-react-native/types/google-wallet.types'
// import { SamsungPayStatus } from '@platformbuilders/wallet-bridge-react-native/types/samsung-wallet.types'
//
// ============================================================================
