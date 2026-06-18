<script setup lang="ts">
defineOptions({ name: 'CustomerServiceChat' })
import { ref, nextTick } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { csApi, type CsMessage, type CsSession } from '@shop/shared'
import { BASE_URL } from '@/http'

interface DisplayMessage {
  id: number
  role: 'user' | 'assistant' | 'system'
  content: string
  createTime: string
}

const session = ref<CsSession | null>(null)
const messages = ref<DisplayMessage[]>([])
const inputText = ref('')
const loading = ref(false)
const sending = ref(false)
const wsConnected = ref(false)

const seenIds = new Set<number>()
let socketTask: UniApp.SocketTask | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null

const quickQuestions = [
  '如何查看订单物流？',
  '如何申请退款？',
  '如何使用优惠券？',
  '商品质量问题怎么办？',
]

onLoad(() => { init() })
onUnload(() => { cleanup() })

async function init() {
  try {
    loading.value = true
    const sessions = await csApi.getMySessions()
    if (sessions?.length) {
      session.value = sessions[0]
      await loadMessages()
    } else {
      session.value = await csApi.createSession()
    }
    if (session.value) connectWs()
  } finally {
    loading.value = false
  }
}

async function loadMessages() {
  if (!session.value) return
  try {
    const res = await csApi.getMessages(session.value.sessionNo, 1, 100)
    if (res?.records) {
      const list: DisplayMessage[] = res.records.map(m => ({
        id: m.id, role: m.role as DisplayMessage['role'], content: m.content, createTime: m.createTime,
      }))
      for (const m of list) {
        if (!seenIds.has(m.id)) { seenIds.add(m.id); messages.value.push(m) }
      }
      scrollToBottom()
    }
  } catch {}
}

function connectWs() {
  if (!session.value) return
  const token = uni.getStorageSync('token') || ''
  const wsUrl = `${BASE_URL.replace('http', 'ws')}/ws/cs/${session.value.sessionNo}?token=${token}`

  socketTask = uni.connectSocket({
    url: wsUrl,
    success: () => {},
    fail: () => {},
  })

  socketTask.onOpen(() => { wsConnected.value = true })

  socketTask.onMessage((res) => {
    try {
      const data = JSON.parse(res.data as string)
      if (data?.messageId && !seenIds.has(data.messageId)) {
        seenIds.add(data.messageId)
        messages.value.push({
          id: data.messageId,
          role: data.role || 'assistant',
          content: data.content || '',
          createTime: data.createTime || new Date().toISOString(),
        })
        scrollToBottom()
      }
    } catch {}
  })

  socketTask.onClose(() => {
    wsConnected.value = false
    socketTask = null
    reconnectTimer = setTimeout(() => { if (session.value) connectWs() }, 3000)
  })

  socketTask.onError(() => { wsConnected.value = false })
}

function cleanup() {
  if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
  if (socketTask) { socketTask.close({}); socketTask = null }
}

async function sendMessage(text?: string) {
  const content = text || inputText.value.trim()
  if (!content || !session.value || sending.value) return
  inputText.value = ''

  const tempId = Date.now()
  messages.value.push({ id: tempId, role: 'user', content, createTime: new Date().toISOString() })
  scrollToBottom()

  try {
    sending.value = true
    const resp = await csApi.sendMessage(session.value.sessionNo, content)
    if (resp?.messageId && !seenIds.has(resp.messageId)) {
      seenIds.add(resp.messageId)
      messages.value.push({
        id: resp.messageId, role: 'assistant', content: resp.content, createTime: resp.createTime,
      })
      scrollToBottom()
    }
  } catch {
    messages.value.push({
      id: Date.now() + 1, role: 'assistant', content: '抱歉，消息发送失败，请稍后重试。', createTime: new Date().toISOString(),
    })
  } finally {
    sending.value = false
  }
}

function sendQuickQuestion(q: string) { sendMessage(q) }

function scrollToBottom() {
  nextTick(() => {
    uni.pageScrollTo({ scrollTop: 999999, duration: 200 })
  })
}
</script>

<template>
  <view class="cs">
    <!-- Loading -->
    <view v-if="loading" class="loading">正在连接客服...</view>

    <!-- Messages -->
    <view class="msg-list" v-else>
      <!-- Quick Questions -->
      <view class="quick-qs" v-if="!messages.length">
        <text class="quick-title">常见问题</text>
        <view class="quick-item" v-for="(q, i) in quickQuestions" :key="i" @click="sendQuickQuestion(q)">
          <text>{{ q }}</text>
        </view>
      </view>

      <!-- Message Bubbles -->
      <view class="msg-item" v-for="m in messages" :key="m.id" :class="m.role">
        <view class="bubble" :class="m.role">
          <text class="bubble-text">{{ m.content }}</text>
        </view>
      </view>

      <!-- Typing Indicator -->
      <view class="msg-item assistant" v-if="sending">
        <view class="bubble assistant">
          <text class="typing-dots">正在输入<text class="dots">...</text></text>
        </view>
      </view>
    </view>

    <!-- Input Area -->
    <view class="input-bar">
      <input
        class="input-field"
        v-model="inputText"
        placeholder="输入消息..."
        confirm-type="send"
        :disabled="sending"
        @confirm="sendMessage()"
      />
      <button class="send-btn" :disabled="!inputText.trim() || sending" @click="sendMessage()">发送</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.cs { min-height: 100vh; background: #f5f5f5; display: flex; flex-direction: column; }

.loading { text-align: center; padding: 200rpx 0; color: #999; }

.msg-list { flex: 1; padding: 20rpx; padding-bottom: 140rpx; }

.quick-qs { text-align: center; padding: 60rpx 0; }
.quick-title { font-size: 28rpx; color: #999; display: block; margin-bottom: 24rpx; }
.quick-item {
  display: inline-block; background: #fff; padding: 16rpx 28rpx; border-radius: 32rpx;
  margin: 8rpx; font-size: 26rpx; color: #FF5000; border: 1rpx solid #FFE0CC;
}

.msg-item { display: flex; margin-bottom: 20rpx; }
.msg-item.user { justify-content: flex-end; }
.msg-item.assistant { justify-content: flex-start; }

.bubble {
  max-width: 75%; padding: 20rpx 24rpx; border-radius: 16rpx; word-break: break-all;
}
.bubble.user {
  background: #FF5000; color: #fff; border-radius: 16rpx 4rpx 16rpx 16rpx;
}
.bubble.assistant {
  background: #fff; color: #333; border-radius: 4rpx 16rpx 16rpx 16rpx;
}
.bubble-text { font-size: 28rpx; line-height: 1.6; }

.typing-dots { font-size: 28rpx; color: #999; }
.dots { animation: blink 1s infinite; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: .3; } }

.input-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; align-items: center; gap: 12rpx;
  padding: 16rpx 20rpx; background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,.05);
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
.input-field {
  flex: 1; height: 72rpx; background: #f5f5f5; border-radius: 36rpx;
  padding: 0 24rpx; font-size: 28rpx;
}
.send-btn {
  width: 120rpx; height: 72rpx; line-height: 72rpx; background: #FF5000;
  color: #fff; border: none; border-radius: 36rpx; font-size: 28rpx;
}
.send-btn[disabled] { opacity: .5; }
</style>
