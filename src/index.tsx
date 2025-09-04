import BuildersWallet from './NativeBuildersWallet';
import type {
  PushTokenizeRequest,
  UserAddress,
  PaymentCard,
} from './NativeBuildersWallet';

export type { PushTokenizeRequest, UserAddress, PaymentCard };

export function pushTokenize(request: PushTokenizeRequest): Promise<string> {
  return BuildersWallet.pushTokenize(request);
}
