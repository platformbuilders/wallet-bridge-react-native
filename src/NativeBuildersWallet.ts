import { NativeModules } from 'react-native';

export enum WALLET_STATUS {
  /** Não há carteira ativa. */
  TAP_AND_PAY_NO_ACTIVE_WALLET = 'TAP_AND_PAY_NO_ACTIVE_WALLET',
  /** O ID do token do emissor indicado não corresponde a um token na carteira ativa. Esse status pode ser retornado por chamadas que especificam um ID de token do emissor. */
  TAP_AND_PAY_TOKEN_NOT_FOUND = 'TAP_AND_PAY_TOKEN_NOT_FOUND',
  /** O token especificado foi encontrado, mas não estava em um estado válido para que a operação fosse bem-sucedida. Por exemplo, isso pode acontecer quando se tenta selecionar como padrão um token que não está no estado TOKEN_STATE_ACTIVE. */
  TAP_AND_PAY_INVALID_TOKEN_STATE = 'TAP_AND_PAY_INVALID_TOKEN_STATE',
  /** A tokenização falhou porque o dispositivo não foi aprovado em uma verificação de compatibilidade. */
  TAP_AND_PAY_ATTESTATION_ERROR = 'TAP_AND_PAY_ATTESTATION_ERROR',
  /** Não é possível chamar a API TapAndPay pelo aplicativo atual. Se você receber esse erro, verifique se está chamando a API usando um nome de pacote e uma impressão digital que adicionamos à nossa lista de permissões. */
  TAP_AND_PAY_UNAVAILABLE = 'TAP_AND_PAY_UNAVAILABLE',
}

export enum WALLET_STATUS_CODE {
  /** Não há carteira ativa. */
  TAP_AND_PAY_NO_ACTIVE_WALLET = '15002',
  /** O ID do token do emissor indicado não corresponde a um token na carteira ativa. Esse status pode ser retornado por chamadas que especificam um ID de token do emissor. */
  TAP_AND_PAY_TOKEN_NOT_FOUND = '15003',
  /** O token especificado foi encontrado, mas não estava em um estado válido para que a operação fosse bem-sucedida. Por exemplo, isso pode acontecer quando se tenta selecionar como padrão um token que não está no estado TOKEN_STATE_ACTIVE. */
  TAP_AND_PAY_INVALID_TOKEN_STATE = '15004',
  /** A tokenização falhou porque o dispositivo não foi aprovado em uma verificação de compatibilidade. */
  TAP_AND_PAY_ATTESTATION_ERROR = '15005',
  /** Não é possível chamar a API TapAndPay pelo aplicativo atual. Se você receber esse erro, verifique se está chamando a API usando um nome de pacote e uma impressão digital que adicionamos à nossa lista de permissões. */
  TAP_AND_PAY_UNAVAILABLE = '15009',
}

export enum STATUS_TOKEN {
  /** O token está na carteira ativa, mas requer autenticação extra do usuário para ser usado (caminho amarelo). */
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION = 'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION',
  /** O token não está disponível para pagamentos no momento, mas vai estar depois de algum tempo. */
  TOKEN_STATE_PENDING = 'TOKEN_STATE_PENDING',
  /** O token foi temporariamente suspenso. */
  TOKEN_STATE_SUSPENDED = 'TOKEN_STATE_SUSPENDED',
  /** O token está ativo e disponível para pagamentos. */
  TOKEN_STATE_ACTIVE = 'TOKEN_STATE_ACTIVE',
  /** O token foi emitido pelo TSP, mas o provisionamento do Felica não foi concluído. */
  TOKEN_STATE_FELICA_PENDING_PROVISIONING = 'TOKEN_STATE_FELICA_PENDING_PROVISIONING',
  /** Esse estado é visível no SDK, mas não é possível para provisionamento por push. É possível ignorar esse estado com segurança. */
  TOKEN_STATE_UNTOKENIZED = 'TOKEN_STATE_UNTOKENIZED',
}

export enum CONSTANTS {
  'TOKEN_PROVIDER_ELO' = 'TOKEN_PROVIDER_ELO',
  'CARD_NETWORK_ELO' = 'CARD_NETWORK_ELO',
  'TOKEN_STATE_UNTOKENIZED' = 'TOKEN_STATE_UNTOKENIZED',
  'TOKEN_STATE_PENDING' = 'TOKEN_STATE_PENDING',
  'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION' = 'TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION',
  'TOKEN_STATE_SUSPENDED' = 'TOKEN_STATE_SUSPENDED',
  'TOKEN_STATE_ACTIVE' = 'TOKEN_STATE_ACTIVE',
  'TOKEN_STATE_FELICA_PENDING_PROVISIONING' = 'TOKEN_STATE_FELICA_PENDING_PROVISIONING',
}

export enum ENVIRONMENT {
  PROD = 'PROD',
  SANDBOX = 'SANDBOX',
  DEV = 'DEV',
}

export enum TOKEN_PROVIDER {
  /**
   * Tokenização fornecida pela American Express
   */
  TOKEN_PROVIDER_AMEX = 'TOKEN_PROVIDER_AMEX',
  /**
   * Tokenização fornecida pela Discover
   */
  TOKEN_PROVIDER_DISCOVER = 'TOKEN_PROVIDER_DISCOVER',
  /**
   * Tokenização fornecida pela JCB
   */
  TOKEN_PROVIDER_JCB = 'TOKEN_PROVIDER_JCB',
  /**
   * Tokenização fornecida pela MasterCard
   */
  TOKEN_PROVIDER_MASTERCARD = 'TOKEN_PROVIDER_MASTERCARD',
  /**
   * Tokenização fornecida pela Visa
   */
  TOKEN_PROVIDER_VISA = 'TOKEN_PROVIDER_VISA',
}

export enum CARD_NETWORK {
  /**
   * Token na rede American Express network
   */
  CARD_NETWORK_AMEX = 'CARD_NETWORK_AMEX',
  /**
   * Token na rede Discover network
   */
  CARD_NETWORK_DISCOVER = 'CARD_NETWORK_DISCOVER',
  /**
   * Token na rede MasterCard network
   */
  CARD_NETWORK_MASTERCARD = 'CARD_NETWORK_MASTERCARD',
  /**
   * Token na rede QUICPay network
   */
  CARD_NETWORK_QUICPAY = 'CARD_NETWORK_QUICPAY',
  /**
   * Token para uma rede de transporte público ou de marca própria
   */
  CARD_NETWORK_PRIVATE_LABEL = 'CARD_NETWORK_PRIVATE_LABEL',
  /**
   * Token na rede Visa network
   */
  CARD_NETWORK_VISA = 'CARD_NETWORK_VISA',
}

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

export interface GetTokenStatusParams {
  tokenServiceProvider: number;
  tokenReferenceId: string;
}

export interface ViewTokenParams {
  tokenServiceProvider: number;
  issuerTokenId: string;
}

export interface Address {
  address1: string;
  address2: string;
  countryCode: string;
  locality: string;
  administrativeArea: string;
  name: string;
  phoneNumber: string;
  postalCode: string;
}

export interface Card {
  opaquePaymentCard: string; // Representando um array de bytes
  network: number;
  tokenServiceProvider: number;
  displayName: string;
  lastDigits: string;
}

export interface PushTokenizeParams {
  address: Address;
  card: Card;
}

export interface GetConstantsResponse {
  CARD_NETWORK_ELO: 12;
  TOKEN_PROVIDER_ELO: 14;
  TOKEN_STATE_ACTIVE: 5;
  TOKEN_STATE_FELICA_PENDING_PROVISIONING: 6;
  TOKEN_STATE_NEEDS_IDENTITY_VERIFICATION: 3;
  TOKEN_STATE_PENDING: 2;
  TOKEN_STATE_SUSPENDED: 4;
  TOKEN_STATE_UNTOKENIZED: 1;
}

export interface Token {
  /** Referência do token. */
  issuerTokenId: string;
  /** Nome do emissor. */
  issuerName: string;
  /** Últimos quatro dígitos do FPAN. */
  fpanLastFour: string;
  /** Últimos quatro dígitos do DPAN. */
  dpanLastFour: string;
  /** Provedor de token. */
  tokenServiceProvider: TOKEN_PROVIDER;
  /** Rede do cartão */
  network: CARD_NETWORK;
  /** Status do token */
  tokenState: WALLET_STATUS_CODE;
  /** Retorna true se o token for definido como padrão. */
  isDefaultToken: boolean;
  /** Retorna o portfólio de cartões. */
  portifolioName: string;
}

export interface IsTokenizedParams {
  fpanLastFour: string;
  cardNetwork: number;
  tokenServiceProvider: number;
}

export interface Spec {
  pushTokenize(request: PushTokenizeRequest): Promise<string>;
  /** Receber o ID da carteira ativa */
  getActiveWalletId(): Promise<WALLET_STATUS>;
  /** Retorna o status do token de um token na carteira ativa. */
  getTokenStatus(params: GetTokenStatusParams): Promise<STATUS_TOKEN>;
  /** Retorna o ambiente atual que o Google Pay está configurado para usar. */
  getEnvironment(): Promise<ENVIRONMENT>;
  /** Cada dispositivo Android físico tem um ID de hardware estável que é consistente entre as carteiras */
  getStableHardwareId(): Promise<string>;
  /** Retorna uma lista de tokens na carteira do Google Pay. */
  listTokens(): Promise<Token[]>;
  /**
   * Este endpoint pode ser usado para testar e determinar se um token existe na carteira ativa, dados um identificador ( CardNetwork, TokenServiceProvider) e um nome de emissor.
   */
  isTokenized(params: IsTokenizedParams): Promise<boolean>;
  /** Retorna um intent para um detalhe de cartão específico registrado na carteira ativa. */
  viewToken(params: ViewTokenParams): Promise<string>;
  /** A API Push Provisioning vai chamar imediatamente um callback do app do emissor sempre que ocorrerem os seguintes eventos */
  pushTokenizeWithParams(params: PushTokenizeParams): Promise<string>;
  getConstants(): Promise<GetConstantsResponse>;
  createWallet(): Promise<any>;
}

const { BuildersWallet } = NativeModules;

if (!BuildersWallet) {
  throw new Error(
    'BuildersWallet native module is not available. Make sure you have properly installed the library and rebuilt your app.'
  );
}

export default BuildersWallet as Spec;
