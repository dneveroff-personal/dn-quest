import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { createHash } from 'node:crypto'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue({
      script: {
        // патчим getHash, чтобы явно использовать createHash
        defineModel: true
      }
    })
  ],
  base: './', // важно! чтобы пути были относительные
  define: {
    'crypto.hash': undefined, // на всякий случай убираем лишнее
  },
  resolve: {
    alias: {
      crypto: 'node:crypto'
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://backend:8080', // backend — имя контейнера в docker-compose
        changeOrigin: true
      }
    }
  }
})
