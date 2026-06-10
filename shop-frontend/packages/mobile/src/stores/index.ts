import { createUserStore, createCartStore } from '@shop/shared'

const uniStorage = {
  get: (key: string) => uni.getStorageSync(key) || null,
  set: (key: string, value: string) => uni.setStorageSync(key, value),
  remove: (key: string) => uni.removeStorageSync(key),
}

export const useUserStore = createUserStore(uniStorage)
export const useCartStore = createCartStore(uniStorage)
