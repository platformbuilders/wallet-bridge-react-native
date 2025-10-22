// Google Wallet - Status de Ativação
export enum GoogleActivationStatus {
  APPROVED = 'approved',
  DECLINED = 'declined',
  FAILURE = 'failure',
}

// Google Wallet - DataFormat
export enum GoogleWalletDataFormat {
  BASE64_DECODED = 'base64_decoded',
  RAW = 'raw',
}

export enum GoogleEnvironment {
  PROD = 'PROD',
  SANDBOX = 'SANDBOX',
  DEV = 'DEV',
}

export enum GoogleWalletIntentType {
  ACTIVATE_TOKEN = 'ACTIVATE_TOKEN',
  WALLET_INTENT = 'WALLET_INTENT',
  INVALID_CALLER = 'INVALID_CALLER',
}
