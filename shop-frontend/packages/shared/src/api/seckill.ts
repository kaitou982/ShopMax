import { getHttpClient } from '../utils/http'
import type { SeckillSession, SeckillProduct } from '../types'

export const seckillApi = {
  getActiveSessions: () =>
    getHttpClient().get<SeckillSession[]>('/api/v1/marketing/seckill/sessions/active'),

  getProducts: (sessionId: number) =>
    getHttpClient().get<SeckillProduct[]>(`/api/v1/marketing/seckill/products?sessionId=${sessionId}`),

  executeSeckill: (productId: number, sessionId: number) =>
    getHttpClient().post<void>('/api/v1/marketing/seckill/execute', { productId, sessionId }),
}
