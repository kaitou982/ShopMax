import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount, getNotifications, markRead, markAllRead, type Notification } from '@/api/modules/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const notifications = ref<Notification[]>([])
  let pollingTimer: ReturnType<typeof setInterval> | null = null

  const fetchUnreadCount = async () => {
    try { unreadCount.value = await getUnreadCount() } catch { /* optional */ }
  }

  const fetchNotifications = async () => {
    try {
      const res = await getNotifications({ pageNum: 1, pageSize: 20 })
      notifications.value = res.records || []
    } catch { /* optional */ }
  }

  const markItemRead = async (id: number) => {
    try {
      await markRead(id)
      const item = notifications.value.find(n => n.id === id)
      if (item) item.isRead = 1
      if (unreadCount.value > 0) unreadCount.value--
    } catch { /* optional */ }
  }

  const markAllAsRead = async () => {
    try {
      await markAllRead()
      notifications.value.forEach(n => { n.isRead = 1 })
      unreadCount.value = 0
    } catch { /* optional */ }
  }

  const startPolling = () => {
    if (pollingTimer) return
    fetchUnreadCount()
    pollingTimer = setInterval(fetchUnreadCount, 30000)
  }

  const stopPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  return {
    unreadCount, notifications,
    fetchUnreadCount, fetchNotifications, markItemRead, markAllAsRead,
    startPolling, stopPolling,
  }
})
