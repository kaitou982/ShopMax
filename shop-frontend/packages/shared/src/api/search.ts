import { getHttpClient } from '../utils/http'
import type { HotKeyword, SuggestResponse } from '../types'

export const searchApi = {
  record: (keyword: string) =>
    getHttpClient().post<void>('/api/v1/search/record', { keyword }),

  getHot: (limit?: number) =>
    getHttpClient().get<HotKeyword[]>('/api/v1/search/hot', { limit: limit || 10 } as Record<string, unknown>),

  getSuggest: (keyword: string, limit?: number) =>
    getHttpClient().get<SuggestResponse>('/api/v1/search/suggest', { keyword, limit: limit || 8 } as Record<string, unknown>),
}
