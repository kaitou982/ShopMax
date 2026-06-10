import { getHttpClient } from '../utils/http'
import type { CreateOrderParams, OrderDetail } from '../types'

export const orderApi = {
  create: (data: CreateOrderParams) =>
    getHttpClient().post<OrderDetail>('/api/v1/orders', data as unknown as Record<string, unknown>),

  getMyOrders: () =>
    getHttpClient().get<OrderDetail[]>('/api/v1/orders/my'),

  getDetail: (id: number) =>
    getHttpClient().get<OrderDetail>(`/api/v1/orders/${id}`),

  cancel: (id: number, reason: string) =>
    getHttpClient().put<void>(`/api/v1/orders/${id}/cancel?reason=${encodeURIComponent(reason)}`),

  pay: (id: number, payType: number) =>
    getHttpClient().put<void>(`/api/v1/orders/${id}/pay?payType=${payType}`),

  refund: (id: number, reason: string) =>
    getHttpClient().put<void>(`/api/v1/orders/${id}/refund`, { reason }),

  confirmReceive: (id: number) =>
    getHttpClient().put<void>(`/api/v1/orders/${id}/receive`),
}
