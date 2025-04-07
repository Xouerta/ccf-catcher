import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    proxy: {
      // 代理配置
      '/user': {
        target: 'http://100.118.110.15:80',
        changeOrigin: true,

      },
      '/files': {
        target: 'http://100.118.110.15:80',
        changeOrigin: true,

      },
      '/logs': {
        target: 'http://100.118.110.15:80',
        changeOrigin: true,

      },
      '/traffic': {
        target: 'http://100.118.110.15:80',
        changeOrigin: true,
      },
      '/deepStudyLog': {
        target: 'http://100.118.110.15:80',
        changeOrigin: true,
      }
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
