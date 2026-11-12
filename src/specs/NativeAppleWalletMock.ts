import { type TurboModule, TurboModuleRegistry } from 'react-native';

// Controle do mock nativo do Apple Wallet. Fica fora do módulo AppleWallet
// para o contrato público refletir só o PassKit. O mock só existe em build
// Debug com APPLE_WALLET_USE_MOCK no Info.plist do app. Em Release o módulo
// existe (o codegen o registra) e as classes do mock nem são compiladas:
// getImplementation resolve "real", setImplementation("real") resolve true e
// os demais métodos rejeitam com NOT_SUPPORTED_ON_PLATFORM.
//
// Exposto só pelo entrypoint de testes
// (`@platformbuilders/wallet-bridge-react-native/testing`).

export interface Spec extends TurboModule {
  getImplementation(): Promise<string>;
  setImplementation(name: string): Promise<boolean>;
  configure(config: Object): Promise<Object>;
  reset(): Promise<boolean>;
  getState(): Promise<Object>;
}

export default TurboModuleRegistry.get<Spec>('AppleWalletMock');
