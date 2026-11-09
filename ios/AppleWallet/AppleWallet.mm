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
/// Eventos de log a caminho do JS (fila de cold start fica no Swift).
@property (nonatomic, strong) AWEventBuffer *logEvents;
@end

@implementation AppleWallet

+ (NSString *)moduleName {
  return @"AppleWallet";
}

- (instancetype)init {
  self = [super init];
  if (self) {
    _logEvents = [[AWEventBuffer alloc] initWithLimit:32];
    _provider = [AppleWalletProviderFactory defaultProvider];
    _provider.delegate = self;
  }
  return self;
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

  // O JS ligou o emitter: a fila entrega o que nasceu antes (cold start).
  __weak AppleWallet *weakSelf = self;
  [self.logEvents attach:^(NSDictionary *body) {
    [weakSelf emitOnLog:body];
  }];
}

#pragma mark - AppleWalletProviderDelegate

- (void)provider:(id<AppleWalletProviding>)provider didLog:(AWLogEvent *)event {
  // Igual a Google/Samsung: só sobe para o JS depois de setLogListener().
  [self.logEvents push:[event toDictionary]];
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
