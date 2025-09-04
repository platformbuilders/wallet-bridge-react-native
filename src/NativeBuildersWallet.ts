import { NativeModules } from 'react-native';

export interface UserAddress {
  address1: string;
  address2?: string;
  countryCode: string;
  locality: string;
  administrativeArea: string;
  name: string;
  phoneNumber: string;
  postalCode: string;
}

export interface PaymentCard {
  opaquePaymentCard: string;
  network: number;
  tokenServiceProvider: number;
  displayName: string;
  lastDigits: string;
}

export interface PushTokenizeRequest {
  address: UserAddress;
  card: PaymentCard;
}

export interface Spec {
  multiply(a: number, b: number): Promise<number>;
  pushTokenize(request: PushTokenizeRequest): Promise<string>;
}

const { BuildersWallet } = NativeModules;

if (!BuildersWallet) {
  throw new Error(
    'BuildersWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.'
  );
}

export default BuildersWallet as Spec;
