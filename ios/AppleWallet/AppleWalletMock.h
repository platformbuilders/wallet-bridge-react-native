#import <BuildersWalletSpec/BuildersWalletSpec.h>

NS_ASSUME_NONNULL_BEGIN

// TurboModule de controle do mock nativo (src/specs/NativeAppleWalletMock.ts).
// Em Release todos os métodos rejeitam: o mock não é compilado.
@interface AppleWalletMock : NativeAppleWalletMockSpecBase <NativeAppleWalletMockSpec>
@end

NS_ASSUME_NONNULL_END
