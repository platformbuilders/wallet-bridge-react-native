// ============================================================================
// ENTRYPOINT DE TESTES — @platformbuilders/wallet-bridge-react-native/testing
// ============================================================================
// Controle do mock nativo do Apple Wallet. Fica fora do export principal:
// o mock só existe em build Debug, e o app não deve depender dele em produção.
// Em Release (ou com a implementação real ativa) os métodos rejeitam com
// NOT_SUPPORTED_ON_PLATFORM.

export * from './apple-wallet-mock';
