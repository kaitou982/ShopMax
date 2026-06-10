import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    if (code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login').catch(err => {
          // 这里可以记录跳转失败的日志，或做降级处理
          console.error('跳转到登录页失败:', err)
      })
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(message || '请求失败')
    }
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login').catch(err => {
            console.error('跳转到登录页失败:', err)
        })
        ElMessage.error('登录已过期，请重新登录')
        return Promise.reject(error)
      }
      if (status === 403) {
        ElMessage.error('权限不足，请尝试重新登录以更新角色权限')
        return Promise.reject(error)
      }
      ElMessage.error(data?.message || '请求失败')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request

export const get = <T>(url: string, params?: Record<string, unknown>) => {
  return request.get<T, T>(url, { params })
}

export const post = <T>(url: string, data?: Record<string, unknown>) => {
  return request.post<T, T>(url, data)
}

export const put = <T>(url: string, data?: Record<string, unknown>) => {
  return request.put<T, T>(url, data)
}

export const del = <T>(url: string) => {
  return request.delete<T, T>(url)
}
