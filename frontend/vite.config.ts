import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

const proxyTarget = process.env.VITE_PROXY_TARGET || 'http://localhost:8080'
const port = Number(process.env.VITE_PORT) || 5176

export default defineConfig({
  plugins: [vue()],
  server: {
    port,
    strictPort: true,
    host: 'localhost',
    proxy: {
      '/api': {
        target: proxyTarget,
        changeOrigin: true
      }
    }
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}']
  }
})
