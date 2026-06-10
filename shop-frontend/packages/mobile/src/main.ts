import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './http'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)

  // 全局错误捕获：定位 "_" 只读属性报错的来源组件
  app.config.errorHandler = (err, instance, info) => {
    console.error('[GlobalErrorHandler]', err)
    if (instance) {
      console.error('[Component]', instance.$?.type?.name || instance.$?.type?.__name || 'anonymous')
      console.error('[Component data]', JSON.stringify(instance.$data || {}, null, 2)?.slice(0, 500))
    }
    console.error('[Error info]', info)
  }

  // 路由守卫
  setupRouteGuard()

  return { app, pinia }
}

async function setupRouteGuard() {
  const PUBLIC_PATHS = [
    '/pages/index/index', '/pages/category/index', '/pages/live/index',
    '/pages/cart/index', '/pages/user/index', '/pages/product/detail',
    '/pages/login/index', '/pages/login/register', '/pages/login/forgot-password',
    '/pages/community/index',
  ]
  const interceptor = (args: { url: string }) => {
    const path = args.url.split('?')[0]
    if (PUBLIC_PATHS.includes(path)) return
    const token = uni.getStorageSync('token')
    if (!token) { uni.showToast({ title: '请先登录', icon: 'none' }); return false }
  }
  uni.addInterceptor('navigateTo', { invoke: interceptor })
  uni.addInterceptor('redirectTo', { invoke: interceptor })
  uni.addInterceptor('reLaunch', { invoke: interceptor })
  uni.addInterceptor('switchTab', { invoke: interceptor })
}
