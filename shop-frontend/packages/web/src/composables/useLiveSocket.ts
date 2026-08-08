import { ref, watch, onMounted, onUnmounted, type Ref } from 'vue'

export interface LiveSocketMessage {
  type: string
  data: Record<string, unknown>
  timestamp: number
}

export function useLiveSocket(roomId: string | number | Ref<string> | Ref<number>) {
  const isConnected = ref(false)
  const onlineCount = ref(0)
  const messages = ref<LiveSocketMessage[]>([])

  let ws: WebSocket | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let reconnectCount = 0
  const maxReconnect = 3

  const getRoomId = (): string => {
    const id = typeof roomId === 'object' ? roomId.value : roomId
    return String(id)
  }

  const connect = () => {
    const id = getRoomId()
    if (!id || id === '0') return
    if (ws?.readyState === WebSocket.OPEN) return

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const url = `${protocol}//${host}/ws/live/${id}`

    ws = new WebSocket(url)

    ws.onopen = () => {
      isConnected.value = true
      reconnectCount = 0
      console.log('[LiveSocket] 连接成功')
    }

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data) as LiveSocketMessage
        messages.value.push(msg)

        // 更新在线人数
        if (msg.type === 'online' && msg.data?.count != null) {
          onlineCount.value = Number(msg.data.count) || 0
        }
      } catch (e) {
        console.error('[LiveSocket] 解析消息失败:', e)
      }
    }

    ws.onclose = () => {
      isConnected.value = false
      console.log('[LiveSocket] 连接关闭')
      attemptReconnect()
    }

    ws.onerror = (error) => {
      console.error('[LiveSocket] 连接错误:', error)
    }
  }

  const attemptReconnect = () => {
    if (reconnectCount >= maxReconnect) {
      console.log('[LiveSocket] 重连次数已达上限')
      return
    }

    reconnectCount++
    console.log(`[LiveSocket] ${reconnectCount}/${maxReconnect} 尝试重连...`)

    reconnectTimer = setTimeout(() => {
      connect()
    }, 3000)
  }

  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      ws.close()
      ws = null
    }
  }

  const send = (type: string, data: Record<string, unknown>) => {
    if (ws?.readyState !== WebSocket.OPEN) {
      console.warn('[LiveSocket] 连接未就绪')
      return
    }

    const message = { type, data, timestamp: Date.now() }
    ws.send(JSON.stringify(message))
  }

  const sendDanmaku = (content: string, nickname: string) => {
    send('danmaku', { content, nickname })
  }

  const sendLike = () => {
    send('like', {})
  }

  const sendGift = (giftId: number, count: number, nickname: string) => {
    send('gift', { giftId, count, nickname })
  }

  // 监听 roomId 变化，重新连接
  if (typeof roomId === 'object') {
    watch(roomId, (newId, oldId) => {
      if (newId && newId !== oldId) {
        disconnect()
        connect()
      }
    })
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    isConnected,
    onlineCount,
    messages,
    send,
    sendDanmaku,
    sendLike,
    sendGift,
    disconnect,
  }
}
