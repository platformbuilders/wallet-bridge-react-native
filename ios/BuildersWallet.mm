#import "BuildersWallet.h"

@implementation BuildersWallet
RCT_EXPORT_MODULE()

// Métodos básicos para Google Wallet
RCT_EXPORT_METHOD(checkWalletAvailability:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  // Implementação básica - retorna false por enquanto
  resolve(@(NO));
}

RCT_EXPORT_METHOD(getEnvironment:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  // Implementação básica - retorna "development"
  resolve(@"development");
}

RCT_EXPORT_METHOD(openWallet:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  // Implementação básica - retorna false por enquanto
  resolve(@(NO));
}

// Métodos básicos para Samsung Wallet
RCT_EXPORT_METHOD(init:(NSString *)serviceId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  // Implementação básica - retorna true por enquanto
  resolve(@(YES));
}

RCT_EXPORT_METHOD(getSamsungPayStatus:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  // Implementação básica - retorna 0 (não disponível)
  resolve(@(0));
}

@end
