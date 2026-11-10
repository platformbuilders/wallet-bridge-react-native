import { existsSync } from 'fs';
import { join } from 'path';

import * as enums from '../enums';

const root = join(__dirname, '..', '..');
const pkg = require(join(root, 'package.json')) as {
  exports: Record<string, { source: string } | string>;
  typesVersions?: Record<string, Record<string, string[]>>;
};

describe('package.json exports', () => {
  it('points every source entry to an existing file', () => {
    Object.values(pkg.exports).forEach((entry) => {
      if (typeof entry === 'string') return;
      expect(existsSync(join(root, entry.source))).toBe(true);
    });
  });

  it.each(['./enums', './src/enums'])(
    'exposes the %s subpath used by consumer apps',
    (subpath) => {
      expect(pkg.exports[subpath]).toEqual({
        source: './src/enums/index.ts',
        types: './lib/typescript/src/enums/index.d.ts',
        default: './lib/module/enums/index.js',
      });
    },
  );

  it('maps the enums subpath types for TypeScript without exports support', () => {
    expect(pkg.typesVersions?.['*']?.enums).toEqual([
      './lib/typescript/src/enums/index.d.ts',
    ]);
  });

  it('exports the Google and Samsung enums from the enums entry', () => {
    expect(enums).toEqual(
      expect.objectContaining({
        GoogleActivationStatus: expect.any(Object),
        SamsungActivationStatus: expect.any(Object),
      }),
    );
  });
});
