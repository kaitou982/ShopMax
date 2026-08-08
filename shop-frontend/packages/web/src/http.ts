import axios from 'axios'
import { setHttpClient, type HttpClient } from '@shop/shared'
import { createMockClient } from '@shop/shared/src/mock/client'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

const NO_AUTH_PATHS = ['/api/v1/auth/login', '/api/v1/auth/register', '/api/v1/auth/sms/send', '/api/v1/auth/email/send-code', '/api/v1/auth/check-email', '/api/v1/auth/reset-password']

api.interceptors.request.use((config) => {
  if (config.data instanceof FormData) delete config.headers['Content-Type']
  if (config.url && NO_AUTH_PATHS.some(p => config.url!.includes(p))) return config
  const raw = localStorage.getItem('token')
  if (raw) config.headers.Authorization = `Bearer ${raw}`
  return config
})

api.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code === 200) return body.data
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token'); localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  },
)

const realClient: HttpClient = {
  get: (url, params) => api.get(url, { params }) as Promise<unknown>,
  post: (url, data) => api.post(url, data) as Promise<unknown>,
  put: (url, data) => api.put(url, data) as Promise<unknown>,
  delete: (url) => api.delete(url) as Promise<unknown>,
}

const useMock = import.meta.env.VITE_MOCK === 'true'
setHttpClient(useMock ? createMockClient(realClient) : realClient)
export { api }
