import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from "path"

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
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // API Gateway
        changeOrigin: true,
        secure: false
      },
      '/ws': {
        target: 'ws://localhost:8080', // WebSocket Gateway
        ws: true,
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',   // куда билдит (по умолчанию dist)
    assetsDir: 'assets', // папка для js/css/img
    emptyOutDir: true,   // очищает dist перед билдом
  }
})
