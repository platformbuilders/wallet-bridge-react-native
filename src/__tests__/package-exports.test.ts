import { existsSync } from 'fs';
import { join } from 'path';

import * as enums from '../enums';

const root = join(__dirname, '..', '..');
const pkg = require(join(root, 'package.json')) as {
  exports: Record<string, { source: string } | string>;
};

describe('package.json exports', () => {
  it('points every source entry to an existing file', () => {
    Object.values(pkg.exports).forEach((entry) => {
      if (typeof entry === 'string') return;
      expect(existsSync(join(root, entry.source))).toBe(true);
    });
  });

  it('exposes the enums subpath used by consumer apps', () => {
    expect(pkg.exports['./src/enums']).toEqual({
      source: './src/enums/index.ts',
      types: './lib/typescript/src/enums/index.d.ts',
      default: './lib/module/enums/index.js',
    });
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
