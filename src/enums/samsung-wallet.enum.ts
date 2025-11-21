// Samsung Wallet - Intent Types
export enum SamsungWalletIntentType {
  LAUNCH_A2A_IDV = 'LAUNCH_A2A_IDV',
  WALLET_INTENT = 'WALLET_INTENT',
  INVALID_CALLER = 'INVALID_CALLER',
}

// Samsung Wallet - DataFormat
export enum SamsungWalletDataFormat {
  BASE64_DECODED = 'base64_decoded',
  RAW = 'raw',
}

// Samsung Wallet - Activation Status
export enum SamsungActivationStatus {
  ACCEPTED = 'accepted',
  DECLINED = 'declined',
  FAILURE = 'failure',
  APP_NOT_READY = 'appNotReady',
}
