import {
  GoogleWalletModule,
  SamsungWalletModule,
} from '../NativeBuildersWallet';

// Mock dos dados de teste
const mockWalletData = {
  deviceID: 'test-device-id',
  walletAccountID: 'test-wallet-account-id',
};

const mockTokenInfo = {
  issuerTokenId: 'test-token-id',
  issuerName: 'Test Bank',
  fpanLastFour: '1234',
  dpanLastFour: '5678',
  tokenServiceProvider: 1,
  network: 1,
  tokenState: 1,
  isDefaultToken: true,
  portfolioName: 'Test Portfolio',
};

const mockSamsungCard = {
  cardId: 'test-card-id',
  cardStatus: 'ACTIVE',
  cardBrand: 'VISA',
  last4FPan: '1234',
  last4DPan: '5678',
  issuerName: 'Test Bank',
  isDefaultCard: 'true',
  memberID: 'test-member-id',
  countryCode: 'BR',
};

describe('NativeBuildersWallet', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('GoogleWalletModule', () => {
    it('deve verificar se o Google Wallet está disponível', async () => {
      (
        GoogleWalletModule.checkWalletAvailability as jest.Mock
      ).mockResolvedValue(true);

      const result = await GoogleWalletModule.checkWalletAvailability();

      expect(result).toBe(true);
      expect(GoogleWalletModule.checkWalletAvailability).toHaveBeenCalledTimes(
        1
      );
    });

    it('deve obter informações seguras da carteira', async () => {
      (GoogleWalletModule.getSecureWalletInfo as jest.Mock).mockResolvedValue(
        mockWalletData
      );

      const result = await GoogleWalletModule.getSecureWalletInfo();

      expect(result).toEqual(mockWalletData);
      expect(GoogleWalletModule.getSecureWalletInfo).toHaveBeenCalledTimes(1);
    });

    it('deve obter status do token', async () => {
      const mockTokenStatus = { tokenState: 1, isSelected: true };
      (GoogleWalletModule.getTokenStatus as jest.Mock).mockResolvedValue(
        mockTokenStatus
      );

      const result = await GoogleWalletModule.getTokenStatus(
        1,
        'test-token-id'
      );

      expect(result).toEqual(mockTokenStatus);
      expect(GoogleWalletModule.getTokenStatus).toHaveBeenCalledWith(
        1,
        'test-token-id'
      );
    });

    it('deve obter ambiente', async () => {
      (GoogleWalletModule.getEnvironment as jest.Mock).mockResolvedValue(
        'PRODUCTION'
      );

      const result = await GoogleWalletModule.getEnvironment();

      expect(result).toBe('PRODUCTION');
      expect(GoogleWalletModule.getEnvironment).toHaveBeenCalledTimes(1);
    });

    it('deve verificar se está tokenizado', async () => {
      (GoogleWalletModule.isTokenized as jest.Mock).mockResolvedValue(true);

      const result = await GoogleWalletModule.isTokenized('1234', 1, 1);

      expect(result).toBe(true);
      expect(GoogleWalletModule.isTokenized).toHaveBeenCalledWith('1234', 1, 1);
    });

    it('deve visualizar token', async () => {
      (GoogleWalletModule.viewToken as jest.Mock).mockResolvedValue(
        mockTokenInfo
      );

      const result = await GoogleWalletModule.viewToken(1, 'test-token-id');

      expect(result).toEqual(mockTokenInfo);
      expect(GoogleWalletModule.viewToken).toHaveBeenCalledWith(
        1,
        'test-token-id'
      );
    });

    it('deve adicionar cartão à carteira', async () => {
      const mockCardData = {
        address: {
          name: 'Test User',
          address1: '123 Main St',
          locality: 'City',
          administrativeArea: 'State',
          countryCode: 'BR',
          postalCode: '12345',
          phoneNumber: '123456789',
        },
        card: {
          opaquePaymentCard: 'test-card',
          network: 1,
          tokenServiceProvider: 1,
          displayName: 'Test Card',
          lastDigits: '1234',
        },
      };
      (GoogleWalletModule.addCardToWallet as jest.Mock).mockResolvedValue(
        'success'
      );

      const result = await GoogleWalletModule.addCardToWallet(mockCardData);

      expect(result).toBe('success');
      expect(GoogleWalletModule.addCardToWallet).toHaveBeenCalledWith(
        mockCardData
      );
    });

    it('deve criar carteira se necessário', async () => {
      (GoogleWalletModule.createWalletIfNeeded as jest.Mock).mockResolvedValue(
        true
      );

      const result = await GoogleWalletModule.createWalletIfNeeded();

      expect(result).toBe(true);
      expect(GoogleWalletModule.createWalletIfNeeded).toHaveBeenCalledTimes(1);
    });

    it('deve listar tokens', async () => {
      const mockTokens = [mockTokenInfo];
      (GoogleWalletModule.listTokens as jest.Mock).mockResolvedValue(
        mockTokens
      );

      const result = await GoogleWalletModule.listTokens();

      expect(result).toEqual(mockTokens);
      expect(GoogleWalletModule.listTokens).toHaveBeenCalledTimes(1);
    });

    it('deve obter constantes', () => {
      const mockConstants = { SDK_NAME: 'GoogleWallet', SUCCESS: 0 };
      (GoogleWalletModule.getConstants as jest.Mock).mockReturnValue(
        mockConstants
      );

      const result = GoogleWalletModule.getConstants();

      expect(result).toEqual(mockConstants);
      expect(GoogleWalletModule.getConstants).toHaveBeenCalledTimes(1);
    });
  });

  describe('SamsungWalletModule', () => {
    it('deve inicializar o Samsung Pay', async () => {
      (SamsungWalletModule.init as jest.Mock).mockResolvedValue(true);

      const result = await SamsungWalletModule.init('test-service-id');

      expect(result).toBe(true);
      expect(SamsungWalletModule.init).toHaveBeenCalledWith('test-service-id');
    });

    it('deve obter status do Samsung Pay', async () => {
      (SamsungWalletModule.getSamsungPayStatus as jest.Mock).mockResolvedValue(
        0
      );

      const result = await SamsungWalletModule.getSamsungPayStatus();

      expect(result).toBe(0);
      expect(SamsungWalletModule.getSamsungPayStatus).toHaveBeenCalledTimes(1);
    });

    it('deve ir para página de atualização', () => {
      SamsungWalletModule.goToUpdatePage();

      expect(SamsungWalletModule.goToUpdatePage).toHaveBeenCalledTimes(1);
    });

    it('deve ativar Samsung Pay', () => {
      SamsungWalletModule.activateSamsungPay();

      expect(SamsungWalletModule.activateSamsungPay).toHaveBeenCalledTimes(1);
    });

    it('deve obter todos os cartões', async () => {
      const mockCards = [mockSamsungCard];
      (SamsungWalletModule.getAllCards as jest.Mock).mockResolvedValue(
        mockCards
      );

      const result = await SamsungWalletModule.getAllCards();

      expect(result).toEqual(mockCards);
      expect(SamsungWalletModule.getAllCards).toHaveBeenCalledTimes(1);
    });

    it('deve obter informações da carteira', async () => {
      const mockWalletInfo = {
        walletDMId: 'test-dm-id',
        deviceId: 'test-device-id',
        walletUserId: 'test-user-id',
      };
      (SamsungWalletModule.getWalletInfo as jest.Mock).mockResolvedValue(
        mockWalletInfo
      );

      const result = await SamsungWalletModule.getWalletInfo();

      expect(result).toEqual(mockWalletInfo);
      expect(SamsungWalletModule.getWalletInfo).toHaveBeenCalledTimes(1);
    });

    it('deve adicionar cartão', async () => {
      (SamsungWalletModule.addCard as jest.Mock).mockResolvedValue(
        mockSamsungCard
      );

      const result = await SamsungWalletModule.addCard(
        'test-payload',
        'test-issuer-id',
        'VISA',
        'CREDIT_DEBIT'
      );

      expect(result).toEqual(mockSamsungCard);
      expect(SamsungWalletModule.addCard).toHaveBeenCalledWith(
        'test-payload',
        'test-issuer-id',
        'VISA',
        'CREDIT_DEBIT'
      );
    });

    it('deve verificar se o Samsung Wallet está disponível', async () => {
      (
        SamsungWalletModule.checkWalletAvailability as jest.Mock
      ).mockResolvedValue(true);

      const result = await SamsungWalletModule.checkWalletAvailability();

      expect(result).toBe(true);
      expect(SamsungWalletModule.checkWalletAvailability).toHaveBeenCalledTimes(
        1
      );
    });

    it('deve obter constantes', () => {
      const mockConstants = { SDK_NAME: 'SamsungPay', SPAY_READY: 0 };
      (SamsungWalletModule.getConstants as jest.Mock).mockReturnValue(
        mockConstants
      );

      const result = SamsungWalletModule.getConstants();

      expect(result).toEqual(mockConstants);
      expect(SamsungWalletModule.getConstants).toHaveBeenCalledTimes(1);
    });
  });
});
