import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUnreadCount, getNotifications, markRead, markUnread, markAllRead, type Notification } from '@/api/modules/notification'
import { useUserStore } from '@/stores/modules/user'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const notifications = ref<Notification[]>([])
  const pinnedIds = ref<Set<number>>(new Set())
  let pollingTimer: ReturnType<typeof setInterval> | null = null

  /** 排序后的通知列表：置顶在前，其余按时间倒序 */
  const sortedNotifications = computed(() => {
    const list = [...notifications.value]
    list.sort((a, b) => {
      const aPinned = pinnedIds.value.has(a.id) ? 1 : 0
      const bPinned = pinnedIds.value.has(b.id) ? 1 : 0
      if (aPinned !== bPinned) return bPinned - aPinned
      return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
    })
    return list
  })

  const fetchUnreadCount = async () => {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) return
    try { unreadCount.value = await getUnreadCount() } catch { /* optional */ }
  }

  const fetchNotifications = async () => {
    try {
      const res = await getNotifications({ pageNum: 1, pageSize: 20 })
      notifications.value = res.records || []
    } catch { /* optional */ }
  }

  const markItemRead = async (id: number) => {
    const item = notifications.value.find(n => n.id === id)
    if (!item || item.isRead === 1) return
    // 乐观更新：先改本地
    item.isRead = 1
    if (unreadCount.value > 0) unreadCount.value--
    // 后台同步
    markRead(id).catch(() => {
      // 失败则回滚
      item.isRead = 0
      unreadCount.value++
    })
  }

  const markItemUnread = async (id: number) => {
    const item = notifications.value.find(n => n.id === id)
    if (!item || item.isRead === 0) return
    // 乐观更新：先改本地
    item.isRead = 0
    unreadCount.value++
    // 后台同步
    markUnread(id).catch(() => {
      // 失败则回滚
      item.isRead = 1
      if (unreadCount.value > 0) unreadCount.value--
    })
  }

  const togglePin = (id: number) => {
    const s = new Set(pinnedIds.value)
    if (s.has(id)) s.delete(id)
    else s.add(id)
    pinnedIds.value = s
  }

  const markAllAsRead = async () => {
    const backup = notifications.value.map(n => ({ id: n.id, isRead: n.isRead }))
    const backupCount = unreadCount.value
    // 乐观更新
    notifications.value.forEach(n => { n.isRead = 1 })
    unreadCount.value = 0
    markAllRead().catch(() => {
      // 失败则回滚
      backup.forEach(b => {
        const item = notifications.value.find(n => n.id === b.id)
        if (item) item.isRead = b.isRead
      })
      unreadCount.value = backupCount
    })
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
    unreadCount, notifications, sortedNotifications, pinnedIds,
    fetchUnreadCount, fetchNotifications,
    markItemRead, markItemUnread, togglePin, markAllAsRead,
    startPolling, stopPolling,
  }
})
