export interface CsSession {
  id: number
  sessionNo: string
  userId: number
  status: number
  lastMessageTime: string
  createTime: string
  updateTime: string
}

export interface CsMessage {
  id: number
  sessionId: number
  role: 'user' | 'assistant' | 'system' | 'tool'
  content: string
  toolCalls: unknown
  toolCallId: string | null
  tokenCount: number
  createTime: string
}

export interface ChatResponse {
  messageId: number
  role: string
  content: string
  tokenCount: number
  createTime: string
}

export interface CsFaq {
  id: number
  category: string
  question: string
  answer: string
  sortOrder: number
  status: number
}
