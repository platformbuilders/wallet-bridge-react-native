import { type TurboModule, TurboModuleRegistry } from 'react-native';

// Controle do mock nativo do Apple Wallet. Fica fora do módulo AppleWallet
// para o contrato público refletir só o PassKit. Em build Release o módulo
// existe, mas todos os métodos rejeitam com NOT_SUPPORTED_ON_PLATFORM: as
// classes do mock nem são compiladas.
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
