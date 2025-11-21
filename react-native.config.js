module.exports = {
  dependencies: {
    '@platformbuilders/wallet-bridge-react-native': {
      platforms: {
        android: {
          sourceDir: '../android/',
          packageImportPath: 'import com.builders.wallet.BuildersWalletPackage;',
        },
        ios: {
          podspecPath: '../WalletBridgeReactNative.podspec',
        },
      },
    },
  },
  assets: ['./android/src/main/assets/'],
};
