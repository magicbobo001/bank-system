import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import prettierConfig from 'eslint-config-prettier'; // 新增 Prettier 集成

export default [
  { ignores: ['dist'] },
  {
    files: ['**/*.{js,jsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: {
        ...globals.browser,
        ...globals.node // 允许 Node.js 全局变量（如 module, process）
      },
      parserOptions: {
        ecmaVersion: 'latest',
        ecmaFeatures: { jsx: true },
        sourceType: 'module',
      },
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    // 扩展推荐规则集
    extends: [
      js.configs.recommended,
      reactHooks.configs.recommended,
      prettierConfig // 禁用与 Prettier 冲突的规则
    ],
    rules: {
      // 自定义规则
      'no-unused-vars': ['error', { varsIgnorePattern: '^[A-Z_]' }],
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      'react/prop-types': 'off' // 关闭 PropTypes 检查
    },
    settings: {
      react: {
        version: 'detect' // 自动检测 React 版本
      }
    }
  }
];