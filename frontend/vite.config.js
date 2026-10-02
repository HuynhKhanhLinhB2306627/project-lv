import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'


export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    strictPort: true,
    allowedHosts: true,

    proxy: {
      // REST API endpoints
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // AI consulting endpoint
      '/ai-consulting/ask': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // Heart disease prediction
      '/predict-heart-disease': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // File uploads (now requires authentication via FileAccessController)
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // WebSocket for real-time features (chat, notifications)
      '/ws': {
        target: 'http://localhost:8080',
        ws: true, // Enable WebSocket proxying
      },
      // Logout endpoint (Thymeleaf form-based logout)
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    }
  }
})
