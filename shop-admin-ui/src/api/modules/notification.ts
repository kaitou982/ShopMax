import { get, put, post } from '../request'
import type { PageResult, PageParams } from '@/types/api'

export interface Notification {
  id: number
  type: number        // 1退款 2入驻 3内容 4库存
  title: string
  content: string
  refId?: number
  refType?: string
  isRead: number
  createTime: string
}

export const getNotifications = (params?: PageParams & { type?: number; isRead?: number }) => {
  return get<PageResult<Notification>>('/api/v1/admin/notifications', params)
}

export const getUnreadCount = () => {
  return get<number>('/api/v1/admin/notifications/unread-count')
}

export const markRead = (id: number) => {
  return put(`/api/v1/admin/notifications/${id}/read`)
}

export const markUnread = (id: number) => {
  return put(`/api/v1/admin/notifications/${id}/unread`)
}

export const markAllRead = () => {
  return put('/api/v1/admin/notifications/read-all')
}

export const createNotification = (data: { type: number; title: string; content?: string; refId?: number; refType?: string }) => {
  return post('/api/v1/admin/notifications', data as Record<string, unknown>)
}
