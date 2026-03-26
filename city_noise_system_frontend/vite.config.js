import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // 只在需要时启用 Vue DevTools
    // 可以通过 ENABLE_DEVTOOLS=true pnpm dev 启用
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    },
    // 增加文件系统访问速度
    fs: {
      strict: false
    }
  },
  // 优化依赖项处理
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'element-plus', '@element-plus/icons-vue'],
    // 强制预构建这些依赖
    force: false
  },
  // 增加缓存
  build: {
    cacheDir: '.vite/cache'
  }
})
