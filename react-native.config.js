module.exports = {
  dependencies: {
    'react-native-builders-wallet': {
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
