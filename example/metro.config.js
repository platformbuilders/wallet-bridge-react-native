const path = require('path');
const { getDefaultConfig } = require('@react-native/metro-config');
const { withMetroConfig } = require('react-native-monorepo-config');
const pkg = require('../package.json');

const root = path.resolve(__dirname, '..');

const config = withMetroConfig(getDefaultConfig(__dirname), {
  root,
  dirname: __dirname,
});

// Subpaths da lib (`/enums`, `/testing`) direto do código-fonte: o
// react-native-monorepo-config só aplica a condição `source` ao nome exato
// do pacote, e o `lib/` não existe no example.
const sourceSubpaths = Object.fromEntries(
  Object.entries(pkg.exports)
    .filter(([subpath, entry]) => subpath !== '.' && entry && entry.source)
    .map(([subpath, entry]) => [
      `${pkg.name}${subpath.slice(1)}`,
      path.join(root, entry.source),
    ])
);

const { resolveRequest } = config.resolver;

/**
 * Metro configuration
 * https://facebook.github.io/metro/docs/configuration
 *
 * @type {import('metro-config').MetroConfig}
 */
module.exports = {
  ...config,
  resolver: {
    ...config.resolver,
    resolveRequest: (context, moduleName, platform) => {
      const source = sourceSubpaths[moduleName];
      if (source) {
        return { type: 'sourceFile', filePath: source };
      }
      return resolveRequest(context, moduleName, platform);
    },
  },
};
