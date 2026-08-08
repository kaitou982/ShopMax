import { defineConfig, type Plugin } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'
import fs from 'fs'
import path from 'path'

/**
 * 在 H5 dev 模式下直接提供 static/ 目录的静态文件。
 * UniApp 插件将 publicDir 设为 __static__，导致 static/ 文件无法被 Vite 正常提供。
 */
function serveStaticPlugin(): Plugin {
  const staticDir = resolve(__dirname, 'static')
  return {
    name: 'serve-static',
    configureServer(server) {
      server.middlewares.use('/static', (req, res, next) => {
        const filePath = path.join(staticDir, req.url || '')
        // 防止目录遍历攻击
        if (!filePath.startsWith(staticDir)) {
          res.statusCode = 403
          res.end()
          return
        }
        if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
          const ext = path.extname(filePath).toLowerCase()
          const mimeTypes: Record<string, string> = {
            '.png': 'image/png',
            '.jpg': 'image/jpeg',
            '.jpeg': 'image/jpeg',
            '.gif': 'image/gif',
            '.svg': 'image/svg+xml',
            '.webp': 'image/webp',
            '.ico': 'image/x-icon',
          }
          res.setHeader('Content-Type', mimeTypes[ext] || 'application/octet-stream')
          res.setHeader('Cache-Control', 'no-cache')
          fs.createReadStream(filePath).pipe(res)
          return
        }
        next()
      })
    },
  }
}

export default defineConfig({
  plugins: [uni(), serveStaticPlugin()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@shop/shared': resolve(__dirname, '../shared/src'),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@import "@/styles/variables.scss";`,
      },
    },
  },
  server: {
    port: 3200,
    host: '0.0.0.0',
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
