import { readFileSync } from 'fs';
import { join } from 'path';
import {
  ApplePassActivationState,
  AppleProvisioningStatus,
  AppleWalletErrorCode,
} from '../enums/apple-wallet.enum';
import {
  AppleWalletImplementation,
  AppleWalletMockScenario,
} from '../testing/apple-wallet-mock';
import type { AppleWalletLogLevel } from '../types/apple-wallet.types';

// O nativo entrega os valores crus dos enums Swift e a camada TS repassa sem
// converter. Este teste garante que os dois lados têm os mesmos valores.

const appleWalletDir = join(__dirname, '..', '..', 'ios', 'AppleWallet');

const readSwift = (relativePath: string) =>
  readFileSync(join(appleWalletDir, relativePath), 'utf8');

/** Valores crus (`case x = "VALOR"`) de um enum Swift de String. */
const swiftEnumValues = (source: string, enumName: string): string[] => {
  const body = new RegExp(
    `enum ${enumName}: String[^{]*\\{([\\s\\S]*?)\\n\\}`,
  ).exec(source)?.[1];
  if (!body) {
    throw new Error(`enum ${enumName} não encontrado`);
  }
  return [...body.matchAll(/case \w+ = "([^"]+)"/g)].map(
    (match) => match[1] as string,
  );
};

/** Valor de uma constante `static let nome = "valor"` do Swift. */
const swiftConstant = (source: string, name: string) =>
  new RegExp(`static let ${name} = "([^"]+)"`).exec(source)?.[1];

const sorted = (values: string[]) => [...values].sort();

// Record exige todas as chaves da union: um nível novo no TS quebra o typecheck.
const logLevels: Record<AppleWalletLogLevel, true> = {
  INFO: true,
  WARN: true,
  ERROR: true,
};

describe('Apple Wallet enums: TS × Swift', () => {
  const types = readSwift('Core/AppleWalletTypes.swift');
  const fixtures = readSwift('Mock/MockProvisioningFixtures.swift');

  it.each([
    ['AWStatus', types, AppleProvisioningStatus],
    ['AWErrorCode', types, AppleWalletErrorCode],
    ['AWActivationState', types, ApplePassActivationState],
    ['AWScenario', fixtures, AppleWalletMockScenario],
  ])('%s has the same values as the TS enum', (enumName, source, tsEnum) => {
    expect(sorted(swiftEnumValues(source, enumName))).toEqual(
      sorted(Object.values(tsEnum)),
    );
  });

  it('AWLogLevel has the same values as AppleWalletLogLevel', () => {
    expect(sorted(swiftEnumValues(types, 'AWLogLevel'))).toEqual(
      sorted(Object.keys(logLevels)),
    );
  });

  it('AWConst implementations match AppleWalletImplementation', () => {
    expect(
      sorted([
        swiftConstant(types, 'implementationReal') ?? '',
        swiftConstant(types, 'implementationMock') ?? '',
      ]),
    ).toEqual(sorted(Object.values(AppleWalletImplementation)));
  });
});
