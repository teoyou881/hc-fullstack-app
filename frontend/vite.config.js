import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/',
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 실제 백엔드 서버 주소
        changeOrigin: true,
      },
      '/api/admin':{ // /api/admin 프록시 (admin API 요청)
        target      :'http://localhost:8080', // 실제 백엔드 서버 주소
        changeOrigin:true,
      }
    }
  }
})
