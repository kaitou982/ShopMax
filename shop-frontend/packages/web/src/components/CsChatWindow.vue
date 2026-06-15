<script setup lang="ts">
defineOptions({ name: 'CsChatWindow' })

import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { NIcon } from 'naive-ui'
import { ChatbubbleEllipsesOutline, CloseOutline } from '@vicons/ionicons5'
import { csApi, type CsMessage, type CsSession } from '@shop/shared'

interface Emits {
  (e: 'close'): void
}
const emit = defineEmits<Emits>()

const session = ref<CsSession | null>(null)
const messages = ref<CsMessage[]>([])
const inputText = ref('')
const loading = ref(false)
const messagesEl = ref<HTMLElement | null>(null)
const seenIds = new Set<number>()

let ws: WebSocket | null = null

const quickQuestions = [
  '支持哪些支付方式？',
  '如何退货？',
  '多久能发货？',
  '有什么优惠活动？',
]

const addMessage = (msg: CsMessage) => {
  if (seenIds.has(msg.id)) return
  seenIds.add(msg.id)
  messages.value.push(msg)
  scrollToBottom()
}

const init = async () => {
  try {
    const sessions = await csApi.getMySessions()
    if (sessions && sessions.length > 0) {
      session.value = sessions[0]
      await loadMessages()
    } else {
      session.value = await csApi.createSession()
    }
    connectWs()
  } catch (e) {
    console.error('初始化客服会话失败:', e)
  }
}

const loadMessages = async () => {
  if (!session.value) return
  try {
    const page = await csApi.getMessages(session.value.sessionNo, 1, 100)
    const list = page.records || []
    list.forEach(m => seenIds.add(m.id))
    messages.value = list
    await scrollToBottom()
  } catch (e) {
    console.error('加载历史消息失败:', e)
  }
}

const connectWs = () => {
  if (!session.value) return
  const token = localStorage.getItem('token')
  if (!token) {
    console.warn('未登录，无法连接 WebSocket')
    return
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const url = `${protocol}//${host}/ws/cs/${session.value.sessionNo}?token=${token}`

  ws = new WebSocket(url)
  ws.onopen = () => console.log('WebSocket 已连接')
  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.messageId) {
        addMessage({
          id: data.messageId,
          sessionId: session.value!.id,
          role: data.role,
          content: data.content,
          tokenCount: data.tokenCount,
          createTime: data.createTime,
          toolCalls: null,
          toolCallId: null,
        })
      }
    } catch (e) {
      console.error('WebSocket 消息解析失败:', e)
    }
  }
  ws.onerror = (e) => console.error('WebSocket 错误:', e)
  ws.onclose = (e) => {
    console.log('WebSocket 断开, code:', e.code)
    setTimeout(() => { if (session.value) connectWs() }, 3000)
  }
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || !session.value || loading.value) return

  const sessionNo = session.value.sessionNo
  inputText.value = ''
  loading.value = true

  // Optimistic: add user message
  const tempId = Date.now()
  addMessage({
    id: tempId,
    sessionId: session.value.id,
    role: 'user',
    content: text,
    toolCalls: null,
    toolCallId: null,
    tokenCount: 0,
    createTime: new Date().toISOString(),
  })

  try {
    const response = await csApi.sendMessage(sessionNo, text)
    // Only add from HTTP if WebSocket didn't already deliver it
    addMessage({
      id: response.messageId,
      sessionId: session.value.id,
      role: 'assistant',
      content: response.content,
      toolCalls: null,
      toolCallId: null,
      tokenCount: response.tokenCount,
      createTime: response.createTime,
    })
  } catch (e: unknown) {
    console.error('发送消息失败:', e)
    const errMsg = e instanceof Error ? e.message : '服务暂时不可用，请稍后再试'
    addMessage({
      id: Date.now(),
      sessionId: session.value!.id,
      role: 'assistant',
      content: `抱歉，${errMsg}`,
      toolCalls: null,
      toolCallId: null,
      tokenCount: 0,
      createTime: new Date().toISOString(),
    })
  } finally {
    loading.value = false
  }
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const sendQuick = (q: string) => {
  inputText.value = q
  sendMessage()
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

const renderContent = (text: string) => {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

onMounted(() => { init() })
onUnmounted(() => { ws?.close(); ws = null })
</script>

<template>
  <div class="cs-chat-window">
    <div class="cs-header">
      <span>
        <n-icon :size="18" color="#fff"><ChatbubbleEllipsesOutline /></n-icon>
        智能客服
      </span>
      <n-icon :size="20" color="#fff" class="cs-close" @click="emit('close')"><CloseOutline /></n-icon>
    </div>

    <div class="cs-messages" ref="messagesEl">
      <div v-if="messages.length === 0 && !loading" class="cs-empty">
        <p>欢迎使用 ShopMax 智能客服！</p>
        <p>您可以咨询商品、订单、售后等问题。</p>
        <div class="cs-quick-qs">
          <button
            v-for="q in quickQuestions"
            :key="q"
            class="cs-quick-btn"
            @click="sendQuick(q)"
          >{{ q }}</button>
        </div>
      </div>

      <div v-if="messages.length === 0 && loading" class="cs-empty">
        <p>正在连接...</p>
      </div>

      <div
        v-for="msg in messages"
        :key="msg.id"
        class="cs-msg-row"
        :class="msg.role === 'user' ? 'cs-msg-user' : 'cs-msg-ai'"
      >
        <div class="cs-bubble" v-html="renderContent(msg.content)"></div>
      </div>

      <div v-if="loading" class="cs-msg-row cs-msg-ai">
        <div class="cs-bubble cs-typing">正在输入<span class="cs-dots">...</span></div>
      </div>
    </div>

    <div class="cs-input-area">
      <input
        v-model="inputText"
        class="cs-input"
        placeholder="输入您的问题..."
        @keydown="handleKeydown"
        :disabled="loading"
      />
      <button
        class="cs-send-btn"
        @click="sendMessage"
        :disabled="!inputText.trim() || loading"
      >发送</button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.cs-chat-window {
  position: fixed;
  right: 20px;
  bottom: 96px;
  width: 380px;
  height: 560px;
  z-index: 95;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.cs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #FF8F1F;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  span { display: flex; align-items: center; gap: 8px; }
}

.cs-close {
  background: none;
  border: none;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}

.cs-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f9fafb;
}

.cs-empty {
  text-align: center;
  color: #6b7280;
  padding-top: 40px;
  p { margin-bottom: 8px; }
}

.cs-quick-qs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 16px;
}

.cs-quick-btn {
  padding: 6px 14px;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  background: #fff;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    background: #FF8F1F;
    color: #fff;
    border-color: #FF8F1F;
  }
}

.cs-msg-row {
  margin-bottom: 12px;
  display: flex;
}
.cs-msg-user {
  justify-content: flex-end;
  .cs-bubble {
    background: #FF8F1F;
    color: #fff;
    border-radius: 16px 16px 4px 16px;
  }
}
.cs-msg-ai {
  justify-content: flex-start;
  .cs-bubble {
    background: #fff;
    color: #1f2937;
    border-radius: 16px 16px 16px 4px;
    border: 1px solid #e5e7eb;
  }
}

.cs-bubble {
  max-width: 80%;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.cs-typing {
  color: #9ca3af;
  font-style: italic;
}

.cs-dots {
  animation: cs-blink 1.4s infinite;
}

@keyframes cs-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.cs-input-area {
  display: flex;
  padding: 12px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
  gap: 8px;
}

.cs-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  &:focus { border-color: #FF8F1F; }
}

.cs-send-btn {
  padding: 8px 18px;
  background: #FF8F1F;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  &:disabled { opacity: 0.5; cursor: not-allowed; }
  &:not(:disabled):hover { background: #FF8F1F; }
}
</style>
