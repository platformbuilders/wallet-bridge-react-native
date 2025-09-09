module.exports = {
  dependencies: {
    '@platformbuilders/wallet-bridge-react-native': {
      platforms: {
        android: {
          sourceDir: '../android/',
          packageImportPath: 'import com.builders.wallet.BuildersWalletPackage;',
        },
      },
    },
  },
  assets: ['./android/src/main/assets/'],
};
