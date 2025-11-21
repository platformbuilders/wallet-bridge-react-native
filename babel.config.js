module.exports = {
  presets: ['module:@react-native/babel-preset'],
  overrides: [
    {
      exclude: /\/node_modules\//,
      test: /\.tsx?$/,
      presets: ['module:react-native-builder-bob/babel-preset'],
    },
  ],
};
