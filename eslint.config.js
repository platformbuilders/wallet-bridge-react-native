// ESLint Flat Config (moderno)
// https://eslint.org/docs/latest/use/configure/configuration-files-new

const tsPlugin = require('@typescript-eslint/eslint-plugin');
const tsParser = require('@typescript-eslint/parser');
const reactPlugin = require('eslint-plugin-react');
const jsxA11yPlugin = require('eslint-plugin-jsx-a11y');
const sonarjsPlugin = require('eslint-plugin-sonarjs');
const promisePlugin = require('eslint-plugin-promise');
const importPlugin = require('eslint-plugin-import');
const prettierPlugin = require('eslint-plugin-prettier');

/** @type {import('eslint').Linter.FlatConfig[]} */
module.exports = [
  {
    ignores: [
      'node_modules/',
      'e2e',
      '__mocks__',
      'coverage',
      '_templates',
      'lib/',
      'build/'
    ],
  },
  {
    files: ['src/**/*.{js,jsx,ts,tsx}'],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        project: './tsconfig.eslint.json',
        tsconfigRootDir: __dirname,
        ecmaVersion: 2020,
        sourceType: 'module',
        ecmaFeatures: { jsx: true },
      },
      globals: {
        Atomics: 'readonly',
        SharedArrayBuffer: 'readonly',
        __DEV__: true,
      },
    },
    plugins: {
      '@typescript-eslint': tsPlugin,
      react: reactPlugin,
      'jsx-a11y': jsxA11yPlugin,
      sonarjs: sonarjsPlugin,
      promise: promisePlugin,
      import: importPlugin,
      prettier: prettierPlugin,
    },
    rules: {
      // @TODO: Remover essa regra assim que for feito o fix na lib do CheckBox
      '@typescript-eslint/ban-ts-comment': 'off',
      'react/jsx-filename-extension': [
        'error',
        { extensions: ['.jsx', '.tsx'] },
      ],
      'react/prop-types': 0,
      'react/display-name': 0,
      '@typescript-eslint/member-delimiter-style': 0,
      '@typescript-eslint/no-empty-function': 0,
      '@typescript-eslint/no-explicit-any': 0,
      'import/prefer-default-export': 0,
      'import/no-named-as-default': 0,
      'import/no-unresolved': 0,
      'import/no-extraneous-dependencies': 0,
      'import/extensions': 0,
      'import/order': [
        'error',
        {
          pathGroups: [
            { pattern: 'react', group: 'external', position: 'before' },
            { pattern: '~/**', group: 'parent', position: 'before' },
            { pattern: '@*/**', group: 'external', position: 'after' },
          ],
          pathGroupsExcludedImportTypes: ['react'],
          alphabetize: { order: 'asc', caseInsensitive: true },
        },
      ],
      'sort-imports': ['error', { ignoreDeclarationSort: true }],
      'jsx-a11y/no-noninteractive-element-interactions': 'off',
      'jsx-a11y/no-static-element-interactions': 'off',
      'jsx-a11y/click-events-have-key-events': 'off',
      'jsx-a11y/no-autofocus': 'off',
      'class-methods-use-this': 0,
      'no-nested-ternary': 0,
      'consistent-return': 0,
      'array-callback-return': 0,
      'react/jsx-props-no-spreading': 0,
      'no-duplicate-imports': 'error',
      'promise/prefer-await-to-callbacks': 'error',
      'promise/prefer-await-to-then': 'error',
      'react/state-in-constructor': 'off',
      'react/no-unescaped-entities': 'off',
      '@typescript-eslint/explicit-module-boundary-types': 'off',
      '@typescript-eslint/no-non-null-assertion': 0,
    },
    settings: {
      react: {
        version: 'detect',
      },
    },
  },
];
