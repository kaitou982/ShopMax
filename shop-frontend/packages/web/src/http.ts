import axios from 'axios'
import { setHttpClient, type HttpClient } from '@shop/shared'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

// 不需要携带 Token 的路径
const NO_AUTH_PATHS = ['/api/v1/auth/login', '/api/v1/auth/register', '/api/v1/auth/sms/send', '/api/v1/auth/email/send-code', '/api/v1/auth/check-email', '/api/v1/auth/reset-password']

// 请求拦截 — 注入 Token（跳过公开接口）
api.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  if (config.url && NO_AUTH_PATHS.some(p => config.url!.includes(p))) {
    return config
  }
  const raw = localStorage.getItem('token')
  if (raw) {
    config.headers.Authorization = `Bearer ${raw}`
  }
  return config
})

// 响应拦截 — 统一解包 { code, data }
api.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code === 200) return body.data
    const msg = body.message || '请求失败'
    return Promise.reject(new Error(msg))
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    if (err.response?.status === 403) {
      console.error('403 Forbidden — 请检查后端网关和认证服务是否正常运行')
    }
    return Promise.reject(err)
  },
)

// 适配器：将 axios 实例包装为 HttpClient 接口
const httpClient: HttpClient = {
  get: (url, params) => api.get(url, { params }).then(r => r as any),
  post: (url, data) => api.post(url, data).then(r => r as any),
  put: (url, data) => api.put(url, data).then(r => r as any),
  delete: (url) => api.delete(url).then(r => r as any),
}

setHttpClient(httpClient)
export { api }
