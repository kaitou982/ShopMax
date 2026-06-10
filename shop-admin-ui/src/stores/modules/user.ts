import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/api'
import { getUserInfo } from '@/api/modules/user'

const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

function loadToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

function loadUserInfo(): UserInfo | null {
  try {
    const raw = localStorage.getItem(USER_INFO_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function saveToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

function saveUserInfo(info: UserInfo | null) {
  if (info) {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  } else {
    localStorage.removeItem(USER_INFO_KEY)
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(loadToken())
  const userInfo = ref<UserInfo | null>(loadUserInfo())

  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const userRole = computed(() => (userInfo.value?.role || ''))
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isStore = computed(() => userInfo.value?.role === 'STORE')
  const storeStatus = computed(() => userInfo.value?.storeStatus)

  const setToken = (newToken: string) => {
    token.value = newToken
    saveToken(newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    saveUserInfo(info)
  }

  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      setUserInfo(res)
    } catch {
      logout()
    }
  }

  const refreshUserInfo = async () => {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      setUserInfo(res)
    } catch {
      // silently fail - don't logout on refresh
    }
  }

  const initUser = async () => {
    if (token.value) {
      await fetchUserInfo()
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userName,
    avatar,
    userRole,
    isAdmin,
    isStore,
    storeStatus,
    setToken,
    setUserInfo,
    fetchUserInfo,
    refreshUserInfo,
    initUser,
    logout
  }
})
