import { get, post } from '../request'
import type { PageResult, PageParams } from '@/types/api'

export interface RefundRecord {
  id: number
  refundNo: string
  paymentNo: string
  orderNo: string
  userId: number
  refundAmount: number
  refundReason: string
  status: number        // 0-处理中 1-成功 2-失败
  payMethod: number     // 1-支付宝 2-微信 3-余额
  gatewayRefundNo: string | null
  failReason: string | null
  createTime: string
}

export const getRefundList = (params?: PageParams & { status?: number }) => {
  return get<PageResult<RefundRecord>>('/api/v1/admin/refunds', params)
}

export const getRefundByOrderNo = (orderNo: string) => {
  return get<RefundRecord>(`/api/v1/admin/refunds/order/${orderNo}`)
}

export const approveRefund = (refundNo: string) => {
  return post(`/api/v1/admin/refunds/${refundNo}/approve`)
}

export const rejectRefund = (refundNo: string, reason: string) => {
  return post(`/api/v1/admin/refunds/${refundNo}/reject`, { reason })
}

export const manualApproveRefund = (refundNo: string, remark: string) => {
  return post(`/api/v1/admin/refunds/${refundNo}/manual-approve`, { remark })
}
