#import <BuildersWalletSpec/BuildersWalletSpec.h>

NS_ASSUME_NONNULL_BEGIN

// TurboModule de controle do mock nativo (src/specs/NativeAppleWalletMock.ts).
// Em Release o mock não é compilado: getImplementation resolve "real",
// setImplementation("real") resolve true e os demais métodos rejeitam.
@interface AppleWalletMock : NativeAppleWalletMockSpecBase <NativeAppleWalletMockSpec>
@end

NS_ASSUME_NONNULL_END
