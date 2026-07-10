import { describe, it, expect, beforeEach } from 'vitest'
import { storage, session } from '@/utils/storage'

describe('storage', () => {
  beforeEach(() => {
    localStorage.clear()
    sessionStorage.clear()
  })

  describe('localStorage', () => {
    it('should store and retrieve string values', () => {
      storage.set('name', 'test')
      expect(storage.get<string>('name')).toBe('test')
    })

    it('should store and retrieve object values', () => {
      const obj = { id: 1, name: 'test' }
      storage.set('user', obj)
      expect(storage.get<typeof obj>('user')).toEqual(obj)
    })

    it('should return null for non-existent keys', () => {
      expect(storage.get('nonexistent')).toBeNull()
    })

    it('should remove items', () => {
      storage.set('key', 'value')
      storage.remove('key')
      expect(storage.get('key')).toBeNull()
    })

    it('should clear all items', () => {
      storage.set('key1', 'value1')
      storage.set('key2', 'value2')
      storage.clear()
      expect(storage.get('key1')).toBeNull()
      expect(storage.get('key2')).toBeNull()
    })
  })

  describe('sessionStorage', () => {
    it('should store and retrieve string values', () => {
      session.set('token', 'abc123')
      expect(session.get<string>('token')).toBe('abc123')
    })

    it('should store and retrieve object values', () => {
      const obj = { role: 'admin' }
      session.set('info', obj)
      expect(session.get<typeof obj>('info')).toEqual(obj)
    })

    it('should return null for non-existent keys', () => {
      expect(session.get('nonexistent')).toBeNull()
    })
  })
})
