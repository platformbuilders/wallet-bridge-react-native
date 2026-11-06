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

/// Teto da fila de eventos emitidos antes do JS se ligar ao módulo. Só o
/// suficiente para um cold start.
static NSUInteger const kPendingEventLimit = 32;

@interface AppleWallet () <AppleWalletProviderDelegate>
@property (nonatomic, strong) id<AppleWalletProviding> provider;
@property (nonatomic, assign) BOOL logListenerActive;
/// Eventos emitidos antes de o JS ligar o emitter. Ver -emitLog:
@property (nonatomic, strong) NSMutableArray<NSDictionary *> *pendingEvents;
@end

@implementation AppleWallet

+ (NSString *)moduleName {
  return @"AppleWallet";
}

- (instancetype)init {
  self = [super init];
  if (self) {
    _pendingEvents = [NSMutableArray array];
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

  // Cold start: um evento pode nascer antes de o JS ligar o emitter. Sem o
  // flush ele se perderia.
  if (self.pendingEvents.count == 0) return;

  NSArray<NSDictionary *> *queued = [self.pendingEvents copy];
  [self.pendingEvents removeAllObjects];
  for (NSDictionary *body in queued) {
    [self emitOnLog:body];
  }
}

- (void)emitLog:(NSDictionary *)body {
  if (_eventEmitterCallback) {
    [self emitOnLog:body];
    return;
  }

  if (self.pendingEvents.count >= kPendingEventLimit) {
    [self.pendingEvents removeObjectAtIndex:0];
  }
  [self.pendingEvents addObject:body];
}

#pragma mark - AppleWalletProviderDelegate

- (void)provider:(id<AppleWalletProviding>)provider didLog:(AWLogEvent *)event {
  // Igual a Google/Samsung: log só sobe para o JS depois de setLogListener().
  if (!self.logListenerActive) return;
  [self emitLog:[event toDictionary]];
}

#pragma mark - Utilitários

- (void)getEnvironment:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve(self.provider.environment);
}

- (void)openWallet:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve(@([self.provider openWallet]));
}

- (void)setLogListener:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  self.logListenerActive = YES;
  resolve(@(YES));
}

- (void)removeLogListener:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  self.logListenerActive = NO;
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
