import { getHttpClient } from '../utils/http'
import type { PageResult } from '../types'

export interface ProductReview {
  id: number
  userId: number
  orderId: number
  productId: number
  rating: number
  content: string
  images: string
  replyContent: string
  replyTime: string
  isAnonymous: number
  status: number
  createTime: string
  userNickname?: string
  userAvatar?: string
}

export interface ReviewStats {
  totalCount: number
  avgRating: number
  goodRate: number
  rating1Count: number
  rating2Count: number
  rating3Count: number
  rating4Count: number
  rating5Count: number
}

export interface CreateReviewParams {
  orderId: number
  productId: number
  rating: number
  content?: string
  images?: string
  isAnonymous?: number
}

export const reviewApi = {
  getProductReviews: (productId: number, pageNum = 1, pageSize = 10) =>
    getHttpClient().get<PageResult<ProductReview>>(`/api/v1/products/${productId}/reviews`, { pageNum, pageSize } as Record<string, unknown>),

  getReviewStats: (productId: number) =>
    getHttpClient().get<ReviewStats>(`/api/v1/products/${productId}/reviews/stats`),

  createReview: (data: CreateReviewParams) =>
    getHttpClient().post<ProductReview>('/api/v1/products/reviews', data as unknown as Record<string, unknown>),

  getMyReviews: (pageNum = 1, pageSize = 10) =>
    getHttpClient().get<PageResult<ProductReview>>('/api/v1/products/reviews/my', { pageNum, pageSize } as Record<string, unknown>),

  deleteReview: (id: number) =>
    getHttpClient().delete<void>(`/api/v1/products/reviews/${id}`),
}
