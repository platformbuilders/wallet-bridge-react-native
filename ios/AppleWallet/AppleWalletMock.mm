#import "AppleWalletMock.h"

#if __has_include(<wallet_bridge_react_native/wallet_bridge_react_native-Swift.h>)
#import <wallet_bridge_react_native/wallet_bridge_react_native-Swift.h>
#else
#import "wallet_bridge_react_native-Swift.h"
#endif

// Ponte do controle do mock. A troca de implementação e os controles vêm da
// AppleWalletProviderFactory, que em Release só conhece o provider real: aqui
// não há `#if DEBUG`, e mesmo assim nada do mock existe no binário de Release.

@implementation AppleWalletMock

+ (NSString *)moduleName {
  return @"AppleWalletMock";
}

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeAppleWalletMockSpecJSI>(params);
}

static void AWRejectMockUnavailable(RCTPromiseRejectBlock reject) {
  reject(AWError.notSupportedOnPlatformCode,
         @"O mock do Apple Wallet não está ativo (Release, ou implementação real).",
         nil);
}

- (void)getImplementation:(RCTPromiseResolveBlock)resolve
                   reject:(RCTPromiseRejectBlock)reject {
  resolve([AppleWalletProviderFactory currentProvider].implementationName);
}

- (void)setImplementation:(NSString *)name
                  resolve:(RCTPromiseResolveBlock)resolve
                   reject:(RCTPromiseRejectBlock)reject {
  if (![AppleWalletProviderFactory useImplementation:name]) {
    reject(AWError.notSupportedOnPlatformCode,
           [NSString stringWithFormat:@"Implementação '%@' indisponível neste build.", name],
           nil);
    return;
  }
  resolve(@(YES));
}

- (void)configure:(NSDictionary *)config
          resolve:(RCTPromiseResolveBlock)resolve
           reject:(RCTPromiseRejectBlock)reject {
  id<AppleWalletMockControlling> controls = [AppleWalletProviderFactory mockControls];
  if (controls == nil) {
    AWRejectMockUnavailable(reject);
    return;
  }
  resolve([controls configure:config]);
}

- (void)reset:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  id<AppleWalletMockControlling> controls = [AppleWalletProviderFactory mockControls];
  if (controls == nil) {
    AWRejectMockUnavailable(reject);
    return;
  }
  [controls reset];
  resolve(@(YES));
}

- (void)getState:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  id<AppleWalletMockControlling> controls = [AppleWalletProviderFactory mockControls];
  if (controls == nil) {
    AWRejectMockUnavailable(reject);
    return;
  }
  resolve([controls stateSnapshot]);
}

@end
