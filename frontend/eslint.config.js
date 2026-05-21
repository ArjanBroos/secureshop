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
      'boundaries/element-types': ['error', {
        default: 'disallow',
        rules: [
          { from: 'domain',   allow: [] },
          { from: 'api',      allow: ['domain'] },
          { from: 'shared',   allow: [] },
          { from: 'features', allow: ['domain', 'api', 'shared'] },
          { from: 'app',      allow: ['domain', 'api', 'shared', 'features'] },
        ],
      }],
      'boundaries/no-unknown': 'error',
    },
  },
])
