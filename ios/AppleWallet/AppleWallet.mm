#import "AppleWallet.h"

// Header gerado pelo compilador Swift. O nome do módulo vem do nome do pod
// (`wallet-bridge-react-native`) com os hifens trocados por underscore.
// O __has_include cobre pod como framework (import angular) e como static lib.
#if __has_include(<wallet_bridge_react_native/wallet_bridge_react_native-Swift.h>)
#import <wallet_bridge_react_native/wallet_bridge_react_native-Swift.h>
#else
#import "wallet_bridge_react_native-Swift.h"
#endif

// Ponte RN do módulo Apple: converte JS ↔ provider e repassa delegate → evento.
// Sem regra de negócio — toda lógica fica no provider Swift.

/// Teto da fila de eventos emitidos antes do JS assinar. Se o JS nunca assinar,
/// não vale segurar histórico — só o suficiente para um cold start.
static NSUInteger const kPendingEventLimit = 32;

@interface AppleWallet () <AppleWalletProviderDelegate>
@property (nonatomic, strong) id<AppleWalletProviding> provider;
@property (nonatomic, assign) BOOL hasListeners;
@property (nonatomic, assign) BOOL logListenerActive;
/// Eventos emitidos antes do JS assinar. Ver -emit:body:
@property (nonatomic, strong) NSMutableArray<NSDictionary *> *pendingEvents;
@end

@implementation AppleWallet

RCT_EXPORT_MODULE()

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

#pragma mark - RCTEventEmitter

- (NSArray<NSString *> *)supportedEvents {
  return @[ AWConst.eventLog ];
}

- (void)startObserving {
  self.hasListeners = YES;

  // Cold start: um evento pode nascer antes do JS montar e assinar. Sem o flush
  // ele se perderia.
  if (self.pendingEvents.count == 0) return;

  NSArray<NSDictionary *> *queued = [self.pendingEvents copy];
  [self.pendingEvents removeAllObjects];
  for (NSDictionary *entry in queued) {
    [self sendEventWithName:entry[@"name"] body:entry[@"body"]];
  }
}

- (void)stopObserving {
  self.hasListeners = NO;
}

- (void)emit:(NSString *)name body:(nullable id)body {
  if (self.hasListeners) {
    [self sendEventWithName:name body:body];
    return;
  }

  // Enfileira em vez de descartar. `body` pode ser nil (evento sem payload),
  // então o dicionário o omite e o flush entrega nil de volta.
  NSMutableDictionary *entry = [@{@"name" : name} mutableCopy];
  if (body) entry[@"body"] = body;

  if (self.pendingEvents.count >= kPendingEventLimit) {
    [self.pendingEvents removeObjectAtIndex:0];
  }
  [self.pendingEvents addObject:entry];
}

#pragma mark - AppleWalletProviderDelegate

- (void)provider:(id<AppleWalletProviding>)provider didLog:(AWLogEvent *)event {
  // Igual a Google/Samsung: log só sobe para o JS depois de setLogListener().
  if (!self.logListenerActive) return;
  [self emit:AWConst.eventLog body:[event toDictionary]];
}

#pragma mark - Utilitários

RCT_EXPORT_METHOD(getEnvironment:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  resolve(self.provider.environment);
}

RCT_EXPORT_METHOD(openWallet:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  resolve(@([self.provider openWallet]));
}

RCT_EXPORT_METHOD(setLogListener:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  self.logListenerActive = YES;
  resolve(@(YES));
}

RCT_EXPORT_METHOD(removeLogListener:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  self.logListenerActive = NO;
  resolve(@(YES));
}

#pragma mark - Constantes

- (NSDictionary *)constantsToExport {
  return @{
    @"PROVISIONING_ENTITLEMENT" : AWConst.provisioningEntitlement,
    @"APPLE_LEAF_CERT_OID" : AWConst.appleLeafCertOID,
    @"WALLET_URL_SCHEME" : AWConst.walletURLScheme,
    @"EVENT_LOG" : AWConst.eventLog,
  };
}

@end
