import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

// 后端基路径 /api/v1（见 contracts/openapi.yaml），开发时代理到本地 Spring Boot。
// 8080 被本机其他服务占用时，用 VITE_PROXY_TARGET 指到别的端口。
const proxyTarget = process.env.VITE_PROXY_TARGET ?? 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
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
