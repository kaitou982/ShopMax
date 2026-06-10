import { get, post, put, del } from '../request'
import type { PageParams, PageResult } from '@/types/api'

export interface Coupon {
  id: number
  name: string
  type: number
  minAmount: number
  discountAmount: number
  discountRate: number
  totalCount: number
  receivedCount: number
  usedCount: number
  perLimit: number
  validDays: number
  useStartTime?: string
  useEndTime?: string
  applicableType: number
  applicableIds?: string
  integralCost: number
  description?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface CouponReceive {
  id: number
  couponId: number
  userId: number
  receiveTime: string
  useTime?: string
  orderId?: number
  orderNo?: string
  status: number
  couponName: string
  couponType: number
  minAmount: number
  discountAmount: number
  discountRate: number
  useEndTime?: string
  createTime: string
}

export interface Promotion {
  id: number
  name: string
  description?: string
  type: number
  minAmount: number
  discountAmount: number
  discountRate: number
  startTime: string
  endTime: string
  applicableType: number
  applicableIds?: string
  status: number
  createTime?: string
  updateTime?: string
}

// 优惠券 API
export const getCouponList = (params?: PageParams) =>
  get<PageResult<Coupon>>('/api/v1/marketing/coupons', params as Record<string, unknown>)

export const getAvailableCoupons = () =>
  get<PageResult<Coupon>>('/api/v1/marketing/coupons/list')

export const getCouponDetail = (id: number) =>
  get<Coupon>(`/api/v1/marketing/coupons/${id}`)

export const createCoupon = (data: Partial<Coupon>) =>
  post<Coupon>('/api/v1/marketing/coupons', data as Record<string, unknown>)

export const updateCoupon = (id: number, data: Partial<Coupon>) =>
  put<Coupon>(`/api/v1/marketing/coupons/${id}`, data as Record<string, unknown>)

export const deleteCoupon = (id: number) =>
  del(`/api/v1/marketing/coupons/${id}`)

export const receiveCoupon = (couponId: number, userId: number) =>
  post('/api/v1/marketing/coupons/receive', { couponId, userId })

export const getMyCoupons = (userId: number, params?: PageParams & { status?: number }) =>
  get<PageResult<CouponReceive>>('/api/v1/marketing/coupons/my', { userId, ...params } as Record<string, unknown>)

// 促销活动 API
export const getPromotionList = (params?: PageParams & { status?: number }) =>
  get<PageResult<Promotion>>('/api/v1/marketing/promotions', params as Record<string, unknown>)

export const getActivePromotions = () =>
  get<Promotion[]>('/api/v1/marketing/promotions/list')

export const getPromotionDetail = (id: number) =>
  get<Promotion>(`/api/v1/marketing/promotions/${id}`)

export const createPromotion = (data: Partial<Promotion>) =>
  post<Promotion>('/api/v1/marketing/promotions', data as Record<string, unknown>)

export const updatePromotion = (id: number, data: Partial<Promotion>) =>
  put<Promotion>(`/api/v1/marketing/promotions/${id}`, data as Record<string, unknown>)

export const deletePromotion = (id: number) =>
  del(`/api/v1/marketing/promotions/${id}`)

export const enablePromotion = (id: number) =>
  put(`/api/v1/marketing/promotions/${id}/enable`)

export const disablePromotion = (id: number) =>
  put(`/api/v1/marketing/promotions/${id}/disable`)

// ============ 秒杀 ============
export interface SeckillSession {
  id: number
  name: string
  startTime: string
  endTime: string
  status: number
  createTime?: string
}

export interface SeckillProduct {
  id: number
  sessionId: number
  productId: number
  skuId: number
  seckillPrice: number
  seckillStock: number
  limitPerUser: number
  sortOrder: number
  status: number
  createTime?: string
}

export const getSeckillSessions = (params?: PageParams) =>
  get<PageResult<SeckillSession>>('/api/v1/marketing/seckill/sessions', params as Record<string, unknown>)

export const getActiveSessions = () =>
  get<SeckillSession[]>('/api/v1/marketing/seckill/sessions/active')

export const createSeckillSession = (data: Partial<SeckillSession>) =>
  post<SeckillSession>('/api/v1/marketing/seckill/sessions', data as Record<string, unknown>)

export const addSeckillProduct = (sessionId: number, data: Partial<SeckillProduct>) =>
  post<SeckillProduct>(`/api/v1/marketing/seckill/sessions/${sessionId}/products`, data as Record<string, unknown>)

export const getSeckillProducts = (sessionId: number) =>
  get<SeckillProduct[]>('/api/v1/marketing/seckill/products', { sessionId } as Record<string, unknown>)

export const loadSeckillStock = (sessionId: number) =>
  post(`/api/v1/marketing/seckill/sessions/${sessionId}/load-stock`)

// ============ 拼团 ============
export interface GroupBuyActivity {
  id: number
  name: string
  productId: number
  skuId: number
  groupPrice: number
  requiredCount: number
  expireHours: number
  stock: number
  startTime: string
  endTime: string
  status: number
  createTime?: string
}

export interface GroupBuyGroup {
  id: number
  activityId: number
  leaderId: number
  currentCount: number
  requiredCount: number
  status: number
  expireTime: string
  completeTime?: string
  createTime: string
  members?: GroupBuyMember[]
}

export interface GroupBuyMember {
  id: number
  groupId: number
  userId: number
  orderId?: number
  orderNo?: string
  isLeader: number
  joinTime: string
}

export const getGroupBuyActivities = (params?: PageParams) =>
  get<PageResult<GroupBuyActivity>>('/api/v1/marketing/group-buy/activities', params as Record<string, unknown>)

export const getActiveGroupBuyActivities = () =>
  get<GroupBuyActivity[]>('/api/v1/marketing/group-buy/activities/active')

export const createGroupBuyActivity = (data: Partial<GroupBuyActivity>) =>
  post<GroupBuyActivity>('/api/v1/marketing/group-buy/activities', data as Record<string, unknown>)

export const getGroupBuyGroupDetail = (id: number) =>
  get<GroupBuyGroup>(`/api/v1/marketing/group-buy/groups/${id}`)

// Admin: coupon redemption records & stats
export const getCouponRedemptionRecords = (couponId: number, params?: PageParams) =>
  get<PageResult<Record<string, unknown>>>(`/api/v1/marketing/coupons/${couponId}/records`, params as Record<string, unknown>)

export const getCouponStats = (couponId: number) =>
  get<Record<string, unknown>>(`/api/v1/marketing/coupons/${couponId}/stats`)

export const grantCouponToUsers = (couponId: number, userIds: number[]) =>
  post<{ success: number; failed: number; total: number }>(`/api/v1/marketing/coupons/${couponId}/grant`, { userIds } as Record<string, unknown>)

export const getCouponTrend = (couponId: number) =>
  get<{ date: string; count: number }[]>(`/api/v1/marketing/coupons/${couponId}/trend`)
