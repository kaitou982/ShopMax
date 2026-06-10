import { createUserStore, createCartStore } from '@shop/shared'

const webStorage = {
  get: (key: string) => localStorage.getItem(key),
  set: (key: string, value: string) => localStorage.setItem(key, value),
  remove: (key: string) => localStorage.removeItem(key),
}

export const useUserStore = createUserStore(webStorage)
export const useCartStore = createCartStore(webStorage)
