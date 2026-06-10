export const storage = {
  get: <T>(key: string): T | null => {
    const value = localStorage.getItem(key)
    if (!value) return null
    try {
      return JSON.parse(value) as T
    } catch {
      return value as unknown as T
    }
  },

  set: (key: string, value: unknown) => {
    if (typeof value === 'object') {
      localStorage.setItem(key, JSON.stringify(value))
    } else {
      localStorage.setItem(key, String(value))
    }
  },

  remove: (key: string) => {
    localStorage.removeItem(key)
  },

  clear: () => {
    localStorage.clear()
  }
}

export const session = {
  get: <T>(key: string): T | null => {
    const value = sessionStorage.getItem(key)
    if (!value) return null
    try {
      return JSON.parse(value) as T
    } catch {
      return value as unknown as T
    }
  },

  set: (key: string, value: unknown) => {
    if (typeof value === 'object') {
      sessionStorage.setItem(key, JSON.stringify(value))
    } else {
      sessionStorage.setItem(key, String(value))
    }
  },

  remove: (key: string) => {
    sessionStorage.removeItem(key)
  },

  clear: () => {
    sessionStorage.clear()
  }
}
