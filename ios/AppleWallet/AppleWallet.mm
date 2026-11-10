#import "AppleWallet.h"

// Header gerado pelo compilador Swift. O nome do módulo vem do nome do pod
// (`wallet-bridge-react-native`) com os hifens trocados por underscore.
// O __has_include cobre pod como framework (import angular) e como static lib.
#if __has_include(<wallet_bridge_react_native/wallet_bridge_react_native-Swift.h>)
#import <wallet_bridge_react_native/wallet_bridge_react_native-Swift.h>
#else
#import "wallet_bridge_react_native-Swift.h"
#endif

// Ponte RN do módulo Apple (TurboModule): converte JS ↔ provider e repassa
// delegate → evento. Sem regra de negócio — toda lógica fica no provider Swift.
// A interface vem do codegen (src/specs/NativeAppleWallet.ts); o registro do
// módulo vem do `codegenConfig.ios.modulesProvider` do package.json.

@interface AppleWallet () <AppleWalletProviderDelegate>
@property (nonatomic, strong) id<AppleWalletProviding> provider;
/// Eventos a caminho do JS. A fila guarda o que nasce antes de o JS ligar o
/// emitter (cold start); chamar o emitter antes disso derruba o app.
@property (nonatomic, strong) AWEventBuffer *logEvents;
@property (nonatomic, strong) AWEventBuffer *certificatesEvents;
@property (nonatomic, strong) AWEventBuffer *resultEvents;
@end

@implementation AppleWallet

+ (NSString *)moduleName {
  return @"AppleWallet";
}

- (instancetype)init {
  self = [super init];
  if (self) {
    _logEvents = [[AWEventBuffer alloc] initWithLimit:32];
    _certificatesEvents = [[AWEventBuffer alloc] initWithLimit:4];
    _certificatesEvents.enabled = YES;
    _resultEvents = [[AWEventBuffer alloc] initWithLimit:16];
    _resultEvents.enabled = YES;

    _provider = [AppleWalletProviderFactory currentProvider];
    _provider.delegate = self;

    // Em Debug o módulo AppleWalletMock pode trocar a implementação.
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(providerDidChange:)
                                                 name:AWConst.providerDidChangeNotification
                                               object:nil];
  }
  return self;
}

- (void)dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)providerDidChange:(NSNotification *)notification {
  id<AppleWalletProviding> provider = notification.object;
  if (provider == nil) return;
  self.provider = provider;
  self.provider.delegate = self;
}

+ (BOOL)requiresMainQueueSetup {
  // PassKit e a apresentação de view controllers tocam UIKit.
  return YES;
}

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeAppleWalletSpecJSI>(params);
}

#pragma mark - Eventos

- (void)setEventEmitterCallback:(EventEmitterCallbackWrapper *)eventEmitterCallbackWrapper {
  [super setEventEmitterCallback:eventEmitterCallbackWrapper];

  // O JS ligou o emitter: as filas entregam o que nasceu antes.
  __weak AppleWallet *weakSelf = self;
  [self.logEvents attach:^(NSDictionary *body) {
    [weakSelf emitOnLog:body];
  }];
  [self.certificatesEvents attach:^(NSDictionary *body) {
    [weakSelf emitOnProvisioningCertificates:body];
  }];
  [self.resultEvents attach:^(NSDictionary *body) {
    [weakSelf emitOnProvisioningResult:body];
  }];
}

#pragma mark - AppleWalletProviderDelegate

- (void)provider:(id<AppleWalletProviding>)provider
    didReceiveCertificates:(AWProvisioningCertificatesEvent *)event {
  [self.certificatesEvents push:[event toDictionary]];
}

- (void)provider:(id<AppleWalletProviding>)provider
    didUpdateProvisioning:(AWProvisioningResultEvent *)event {
  [self.resultEvents push:[event toDictionary]];
}

- (void)provider:(id<AppleWalletProviding>)provider didLog:(AWLogEvent *)event {
  // Igual a Google/Samsung: só sobe para o JS depois de setLogListener().
  [self.logEvents push:[event toDictionary]];
}

#pragma mark - Helpers

static void AWReject(RCTPromiseRejectBlock reject, NSError *error) {
  reject([AWError codeOf:error], error.localizedDescription, error);
}

static NSArray<NSDictionary *> *AWPassesToArray(NSArray<AWPaymentPass *> *passes) {
  NSMutableArray<NSDictionary *> *result = [NSMutableArray arrayWithCapacity:passes.count];
  for (AWPaymentPass *pass in passes) {
    [result addObject:[pass toDictionary]];
  }
  return result;
}

#pragma mark - Provisioning

- (void)canAddPaymentPass:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve(@([self.provider canAddPaymentPass]));
}

- (void)canAddSecureElementPass:(NSString *)primaryAccountIdentifier
                        resolve:(RCTPromiseResolveBlock)resolve
                         reject:(RCTPromiseRejectBlock)reject {
  resolve(@([self.provider canAddSecureElementPassWithPrimaryAccountIdentifier:primaryAccountIdentifier]));
}

- (void)startInAppProvisioning:(NSDictionary *)config
                       resolve:(RCTPromiseResolveBlock)resolve
                        reject:(RCTPromiseRejectBlock)reject {
  NSError *error = nil;
  AWProvisioningRequestConfig *requestConfig =
      [AWProvisioningRequestConfig fromDictionary:config error:&error];
  if (requestConfig == nil) {
    AWReject(reject, error);
    return;
  }

  NSString *requestId = [self.provider startInAppProvisioningWithConfig:requestConfig
                                                                  error:&error];
  if (requestId == nil) {
    AWReject(reject, error);
    return;
  }
  resolve(requestId);
}

- (void)submitProvisioningData:(NSDictionary *)payload
                       resolve:(RCTPromiseResolveBlock)resolve
                        reject:(RCTPromiseRejectBlock)reject {
  NSError *error = nil;
  AWSecurePayload *securePayload = [AWSecurePayload fromDictionary:payload error:&error];
  if (securePayload == nil) {
    AWReject(reject, error);
    return;
  }

  if (![self.provider submitProvisioningDataWithPayload:securePayload error:&error]) {
    AWReject(reject, error);
    return;
  }
  // O pass chega pelo evento de resultado (status ADDED).
  resolve(@(YES));
}

- (void)cancelProvisioning:(RCTPromiseResolveBlock)resolve
                    reject:(RCTPromiseRejectBlock)reject {
  resolve(@([self.provider cancelProvisioning]));
}

#pragma mark - Passes

- (void)listPasses:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve(AWPassesToArray([self.provider listPasses]));
}

- (void)listRemotePasses:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject {
  resolve(AWPassesToArray([self.provider listRemotePasses]));
}

- (void)isWatchPaired:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve(@(self.provider.isWatchPaired));
}

#pragma mark - Verification

- (void)activatePass:(NSString *)deviceAccountIdentifier
      activationData:(NSString *)activationData
             resolve:(RCTPromiseResolveBlock)resolve
              reject:(RCTPromiseRejectBlock)reject {
  [self.provider activatePassWithDeviceAccountIdentifier:deviceAccountIdentifier
                                          activationData:activationData
                                              completion:^(BOOL success, NSError *error) {
                                                if (error != nil) {
                                                  AWReject(reject, error);
                                                  return;
                                                }
                                                resolve(@(success));
                                              }];
}

#pragma mark - Utilitários

- (void)getEnvironment:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve(self.provider.environment);
}

- (void)openWallet:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve(@([self.provider openWallet]));
}

- (void)setLogListener:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  self.logEvents.enabled = YES;
  resolve(@(YES));
}

- (void)removeLogListener:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  self.logEvents.enabled = NO;
  resolve(@(YES));
}

#pragma mark - Constantes

- (facebook::react::ModuleConstants<JS::NativeAppleWallet::Constants>)constantsToExport {
  return [self getConstants];
}

- (facebook::react::ModuleConstants<JS::NativeAppleWallet::Constants>)getConstants {
  return facebook::react::typedConstants<JS::NativeAppleWallet::Constants>({
      .PROVISIONING_ENTITLEMENT = AWConst.provisioningEntitlement,
      .APPLE_LEAF_CERT_OID = AWConst.appleLeafCertOID,
      .WALLET_URL_SCHEME = AWConst.walletURLScheme,
      .EVENT_LOG = AWConst.eventLog,
  });
}

@end
