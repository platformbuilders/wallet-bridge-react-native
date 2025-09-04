import BuildersWallet from './NativeBuildersWallet';
import type {
  PushTokenizeRequest,
  UserAddress,
  PaymentCard,
  GetTokenStatusParams,
  ViewTokenParams,
  Address,
  Card,
  PushTokenizeParams,
  GetConstantsResponse,
  Token,
  IsTokenizedParams,
} from './NativeBuildersWallet';

export type {
  PushTokenizeRequest,
  UserAddress,
  PaymentCard,
  GetTokenStatusParams,
  ViewTokenParams,
  Address,
  Card,
  PushTokenizeParams,
  GetConstantsResponse,
  Token,
  IsTokenizedParams,
};

export {
  WALLET_STATUS,
  WALLET_STATUS_CODE,
  STATUS_TOKEN,
  CONSTANTS,
  ENVIRONMENT,
  TOKEN_PROVIDER,
  CARD_NETWORK,
} from './NativeBuildersWallet';

export function pushTokenize(request: PushTokenizeRequest): Promise<string> {
  return BuildersWallet.pushTokenize(request);
}

export function getActiveWalletId(): Promise<any> {
  return BuildersWallet.getActiveWalletId();
}

export function getTokenStatus(params: GetTokenStatusParams): Promise<any> {
  return BuildersWallet.getTokenStatus(params);
}

export function getEnvironment(): Promise<any> {
  return BuildersWallet.getEnvironment();
}

export function getStableHardwareId(): Promise<string> {
  return BuildersWallet.getStableHardwareId();
}

export function listTokens(): Promise<Token[]> {
  return BuildersWallet.listTokens();
}

export function isTokenized(params: IsTokenizedParams): Promise<boolean> {
  return BuildersWallet.isTokenized(params);
}

export function viewToken(params: ViewTokenParams): Promise<string> {
  return BuildersWallet.viewToken(params);
}

export function pushTokenizeWithParams(
  params: PushTokenizeParams
): Promise<string> {
  return BuildersWallet.pushTokenizeWithParams(params);
}

export function getConstants(): Promise<GetConstantsResponse> {
  return BuildersWallet.getConstants();
}

export function createWallet(): Promise<any> {
  return BuildersWallet.createWallet();
}
