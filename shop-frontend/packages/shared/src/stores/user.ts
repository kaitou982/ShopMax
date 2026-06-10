import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '../types'
import { userApi } from '../api/user'

export function createUserStore(storage: {
  get(key: string): string | null
  set(key: string, value: string): void
  remove(key: string): void
}) {
  const TOKEN_KEY = 'token'
  const USER_KEY = 'userInfo'

  return defineStore('user', () => {
    const token = ref<string>(storage.get(TOKEN_KEY) || '')
    const _initUserInfo = (): UserInfo | null => {
      try {
        const raw = storage.get(USER_KEY)
        return raw ? JSON.parse(raw) : null
      } catch { return null }
    }
    const userInfo = ref<UserInfo | null>(_initUserInfo())

    const isLoggedIn = computed(() => !!token.value)
    const userName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
    const userRole = computed(() => userInfo.value?.role || '')
    const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
    const isStore = computed(() => userInfo.value?.role === 'STORE')
    const storeStatus = computed(() => userInfo.value?.storeStatus)

    function setToken(t: string) { token.value = t; storage.set(TOKEN_KEY, t) }
    function setUserInfo(info: UserInfo | null) {
      userInfo.value = info
      if (info) storage.set(USER_KEY, JSON.stringify(info))
      else storage.remove(USER_KEY)
    }

    async function login(username: string, password: string) {
      const res = await userApi.login({ username, password })
      setToken(res.token)
      setUserInfo(res)
      return res
    }

    async function loginByPhone(phone: string, verifyCode: string) {
      const res = await userApi.loginByPhone({ phone, verifyCode })
      setToken(res.token)
      setUserInfo(res)
      return res
    }

    async function loginByWx(openid: string) {
      const res = await userApi.loginByWx({ openid })
      setToken(res.token)
      setUserInfo(res)
      return res
    }

    async function loginByEmail(email: string, verifyCode: string) {
      const res = await userApi.loginByEmail({ email, verifyCode })
      setToken(res.token)
      setUserInfo(res)
      return res
    }

    async function register(data: { phone?: string; email?: string; password: string; verifyCode: string; username?: string; nickname?: string }) {
      const res = await userApi.register(data)
      return res
    }

    async function fetchUserInfo() {
      if (!token.value) return
      const info = await userApi.getCurrentUser()
      setUserInfo(info)
    }

    async function updateUserInfo(data: { nickname?: string; avatar?: string; gender?: number; birthday?: string }) {
      const info = await userApi.updateUserInfo(data)
      setUserInfo(info)
    }

    function logout() {
      token.value = ''
      userInfo.value = null
      storage.remove(TOKEN_KEY)
      storage.remove(USER_KEY)
    }

    return {
      token, userInfo, isLoggedIn, userName, userRole, isAdmin, isStore, storeStatus,
      setToken, setUserInfo, login, loginByPhone, loginByWx, loginByEmail, register, fetchUserInfo, updateUserInfo, logout,
    }
  })
}

export type UserStore = ReturnType<ReturnType<typeof createUserStore>>
