module.exports = {
  dependencies: {
    '@platformbuilders/wallet-bridge-react-native': {
      platforms: {
        android: {
          sourceDir: '../android/',
          packageImportPath: 'import com.builders.wallet.BuildersWalletPackage;',
        },
        ios: {
          podspecPath: '../wallet-bridge-react-native.podspec',
        },
      },
    },
  },
  assets: ['./android/src/main/assets/'],
};
