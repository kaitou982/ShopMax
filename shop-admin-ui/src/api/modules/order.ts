import { get, post, put, del } from '../request'
import type { PageResult, PageParams } from '@/types/api'

export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  productImage: string
  skuSpecs: string
  price: number
  quantity: number
  subtotal: number
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  payAmount: number
  freightAmount: number
  couponAmount: number
  status: number
  payType: number
  payTime: string
  deliveryTime: string
  receiveTime: string
  cancelTime: string
  cancelReason: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark: string
  sourceType: number
  createTime: string
}

export const getOrderList = (params?: PageParams & { userId?: number; status?: number; orderNo?: string }) => {
  return get<PageResult<Order>>('/api/v1/orders', params)
}

export const getOrderDetail = (id: number) => {
  return get<Order>(`/api/v1/orders/${id}`)
}

export const createOrder = (data: Partial<Order>) => {
  return post<Order>('/api/v1/orders', data)
}

export const cancelOrder = (id: number, reason: string) => {
  return put(`/api/v1/orders/${id}/cancel`, { reason })
}

export const payOrder = (id: number, payType: number) => {
  return put(`/api/v1/orders/${id}/pay`, { payType })
}

export const shipOrder = (id: number) => {
  return put(`/api/v1/orders/${id}/ship`)
}

export const confirmReceive = (id: number) => {
  return put(`/api/v1/orders/${id}/receive`)
}

export const deleteOrder = (id: number) => {
  return del(`/api/v1/orders/${id}`)
}
