import { getHttpClient } from '../utils/http'
import type { CsSession, CsMessage, ChatResponse, CsFaq } from '../types/customer-service'
import type { PageResult, ApiResponse } from '../types'

export const csApi = {
  // Session
  createSession: () =>
    getHttpClient().post<CsSession>('/api/v1/cs/sessions'),

  getMySessions: () =>
    getHttpClient().get<CsSession[]>('/api/v1/cs/sessions/my'),

  closeSession: (sessionNo: string) =>
    getHttpClient().post<void>(`/api/v1/cs/sessions/${sessionNo}/close`),

  // Message
  sendMessage: (sessionNo: string, content: string) =>
    getHttpClient().post<ChatResponse>(`/api/v1/cs/sessions/${sessionNo}/messages`, { content }),

  getMessages: (sessionNo: string, pageNum = 1, pageSize = 50) =>
    getHttpClient().get<PageResult<CsMessage>>(`/api/v1/cs/sessions/${sessionNo}/messages`, { pageNum, pageSize } as Record<string, unknown>),

  // FAQ (admin)
  getFaqs: (pageNum = 1, pageSize = 20, category?: string) =>
    getHttpClient().get<PageResult<CsFaq>>('/api/v1/cs/faqs', { pageNum, pageSize, category } as Record<string, unknown>),

  createFaq: (data: Partial<CsFaq>) =>
    getHttpClient().post<CsFaq>('/api/v1/cs/faqs', data as Record<string, unknown>),

  updateFaq: (id: number, data: Partial<CsFaq>) =>
    getHttpClient().put<CsFaq>(`/api/v1/cs/faqs/${id}`, data as Record<string, unknown>),

  deleteFaq: (id: number) =>
    getHttpClient().delete<void>(`/api/v1/cs/faqs/${id}`),

  batchImportFaq: (items: Array<{ category: string; question: string; answer: string; sortOrder?: number }>) =>
    getHttpClient().post<number>('/api/v1/cs/faqs/batch-import', { items }),

  exportFaq: () =>
    getHttpClient().get<CsFaq[]>('/api/v1/cs/faqs/export'),
}
