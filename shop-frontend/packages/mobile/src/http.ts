import { setHttpClient, type HttpClient } from '@shop/shared'

// 手机测试时，改成电脑的局域网 IP
export const BASE_URL = 'http://localhost:8080'
// export const BASE_URL = 'http://10.200.99.214:8080'


/** 深拷贝：将 uni.request 返回的可能冻结对象转为可变普通对象 */
const unfreeze = <T>(obj: T): T => {
  if (obj === null || typeof obj !== 'object') return obj
  try { return JSON.parse(JSON.stringify(obj)) } catch { return obj }
}

/** 401 统一处理：清除登录态 + 跳转登录页 */
let isRedirecting = false
const handleUnauthorized = () => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
  if (isRedirecting) return
  isRedirecting = true
  uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/login/index' })
    isRedirecting = false
  }, 1500)
}

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

const request = <T>(method: HttpMethod, url: string, data?: Record<string, unknown>): Promise<T> =>
  new Promise<T>((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    const header: Record<string, string> = { 'Authorization': token ? `Bearer ${token}` : '' }
    if (method !== 'GET') header['Content-Type'] = 'application/json'

    uni.request({
      url: `${BASE_URL}${url}`, method, data, header,
      success: (res) => {
        const d = res.data as Record<string, unknown>
        // HTTP 401 或业务码 401 都处理
        if (res.statusCode === 401 || d?.code === 401) {
          handleUnauthorized()
          reject(d || { code: 401, message: '未授权' })
          return
        }
        if (d?.code === 200) {
          resolve(unfreeze(d.data))
        } else {
          uni.showToast({ title: d?.message || '请求失败', icon: 'none' })
          reject(d)
        }
      },
      fail: (err) => {
        // 网络错误或 HTTP 错误（如 401、500等）
        console.error('请求失败:', err)
        reject(err)
      },
    })
  })

const httpClient: HttpClient = {
  get:    <T>(url: string, params?: Record<string, unknown>) => request<T>('GET', url, params),
  post:   <T>(url: string, data?: Record<string, unknown>) => request<T>('POST', url, data),
  put:    <T>(url: string, data?: Record<string, unknown>) => request<T>('PUT', url, data),
  delete: <T>(url: string) => request<T>('DELETE', url),
}

setHttpClient(httpClient)
