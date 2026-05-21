import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import boundaries from 'eslint-plugin-boundaries'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
    },
  },
  {
    files: ['src/**/*.{ts,tsx}'],
    plugins: { boundaries },
    settings: {
      'boundaries/elements': [
        { type: 'domain',   pattern: 'src/domain/**' },
        { type: 'api',      pattern: 'src/api/**' },
        { type: 'features', pattern: 'src/features/**' },
        { type: 'shared',   pattern: 'src/shared/**' },
        { type: 'app',      pattern: 'src/app/**' },
      ],
    },
    rules: {
      'boundaries/dependencies': ['error', {
        default: 'disallow',
        rules: [
          { from: { type: 'domain' },   allow: [] },
          { from: { type: 'api' },      allow: [{ to: { type: 'domain' } }] },
          { from: { type: 'shared' },   allow: [] },
          { from: { type: 'features' }, allow: [{ to: { type: 'domain' } }, { to: { type: 'api' } }, { to: { type: 'shared' } }] },
          { from: { type: 'app' },      allow: [{ to: { type: 'domain' } }, { to: { type: 'api' } }, { to: { type: 'shared' } }, { to: { type: 'features' } }] },
        ],
      }],
    },
  },
])
