import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import path from "path";

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/tests/setup.js'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/tests/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/coverage/**'
      ]
    }
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  define: {
    'import.meta.env.VITE_API_BASE_URL': '"http://localhost:8080/api"',
    'import.meta.env.VITE_WS_BASE_URL': '"ws://localhost:8080/ws"',
    'import.meta.env.VITE_NODE_ENV': '"test"',
    'import.meta.env.VITE_DEBUG': 'true',
    'import.meta.env.VITE_ENABLE_WEBSOCKET': 'false',
    'import.meta.env.VITE_ENABLE_FILE_UPLOAD': 'true',
    'import.meta.env.VITE_ENABLE_NOTIFICATIONS': 'false',
    'import.meta.env.VITE_CACHE_TTL': '1000',
    'import.meta.env.VITE_MAX_FILE_SIZE': '1048576',
    'import.meta.env.VITE_ALLOWED_FILE_TYPES': '"image/jpeg,image/png"',
    'import.meta.env.VITE_MAX_RETRIES': '1',
    'import.meta.env.VITE_RETRY_DELAY': '100',
    'import.meta.env.VITE_MOCK_API': 'false',
    'import.meta.env.VITE_SEED_TEST_DATA': 'true'
  }
});