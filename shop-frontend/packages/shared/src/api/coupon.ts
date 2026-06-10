import { getHttpClient } from '../utils/http'
import type { Coupon, CouponReceive, PageResult } from '../types'

export const couponApi = {
  getAvailableCoupons: () =>
    getHttpClient().get<PageResult<Coupon>>('/api/v1/marketing/coupons/list'),

  receiveCoupon: (couponId: number) =>
    getHttpClient().post<void>('/api/v1/marketing/coupons/receive', { couponId }),

  exchangeCoupon: (couponId: number) =>
    getHttpClient().post<void>('/api/v1/marketing/coupons/exchange', { couponId }),

  getMyCoupons: (status?: number) =>
    getHttpClient().get<PageResult<CouponReceive>>('/api/v1/marketing/coupons/my', { status } as Record<string, unknown>),
}
