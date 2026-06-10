import { get, post, put, del } from '../request'
import type { PageParams, PageResult } from '@/types/api'

export interface Anchor {
  id: number
  userId: number
  realName: string
  phone: string
  nickname: string
  avatar?: string
  cover?: string
  introduction?: string
  status: number
  rejectReason?: string
  level: number
  fansCount: number
  totalLiveCount: number
  totalDuration: number
  auditTime?: string
  createTime?: string
  updateTime?: string
}

export interface LiveRoom {
  id: number
  anchorId: number
  title: string
  cover?: string
  notice?: string
  type: number
  startTime: string
  actualStartTime?: string
  endTime?: string
  pushUrl?: string
  pullUrl?: string
  status: number
  onlineCount: number
  totalViewCount: number
  peakOnlineCount: number
  likeCount: number
  duration: number
  replayUrl?: string
  anchorNickname?: string
  anchorAvatar?: string
  createTime?: string
  updateTime?: string
}

// 主播 API
export const getAnchorList = (params?: PageParams & { status?: number }) =>
  get<PageResult<Anchor>>('/api/v1/live/anchors', params as Record<string, unknown>)

export const getAnchorDetail = (id: number) =>
  get<Anchor>(`/api/v1/live/anchors/${id}`)

export const applyAnchor = (userId: number, data: Partial<Anchor>) =>
  post<Anchor>(`/api/v1/live/anchors/apply?userId=${userId}`, data as Record<string, unknown>)

export const auditAnchor = (id: number, data: { status: number; rejectReason?: string }) =>
  put<Anchor>(`/api/v1/live/anchors/${id}/audit`, data as Record<string, unknown>)

// 直播间 API
export const getLiveRoomList = (params?: PageParams & { type?: number; status?: number }) =>
  get<PageResult<LiveRoom>>('/api/v1/live/rooms', params as Record<string, unknown>)

export const getLivingRooms = () =>
  get<LiveRoom[]>('/api/v1/live/rooms/living')

export const getLiveRoomDetail = (id: number) =>
  get<LiveRoom>(`/api/v1/live/rooms/${id}`)

export const createLiveRoom = (data: Partial<LiveRoom>) =>
  post<LiveRoom>('/api/v1/live/rooms', data as Record<string, unknown>)

export const updateLiveRoom = (id: number, data: Partial<LiveRoom>) =>
  put<LiveRoom>(`/api/v1/live/rooms/${id}`, data as Record<string, unknown>)

export const deleteLiveRoom = (id: number) =>
  del(`/api/v1/live/rooms/${id}`)

export const startLive = (id: number) =>
  put<LiveRoom>(`/api/v1/live/rooms/${id}/start`)

export const endLive = (id: number) =>
  put<LiveRoom>(`/api/v1/live/rooms/${id}/end`)

// ============ 直播商品 ============
export interface LiveProduct {
  id: number
  roomId: number
  productId: number
  skuId: number
  livePrice: number
  sortOrder: number
  status: number
  createTime?: string
}

export const getRoomProducts = (roomId: number) =>
  get<LiveProduct[]>(`/api/v1/live/products/room/${roomId}`)

export const addLiveProduct = (data: Partial<LiveProduct>) =>
  post<LiveProduct>('/api/v1/live/products', data as Record<string, unknown>)

export const updateLiveProduct = (id: number, data: Partial<LiveProduct>) =>
  put<LiveProduct>(`/api/v1/live/products/${id}`, data as Record<string, unknown>)

export const removeLiveProduct = (id: number) =>
  del(`/api/v1/live/products/${id}`)

export const setExplaining = (id: number) =>
  put(`/api/v1/live/products/${id}/explain`)

export const unexplain = (id: number) =>
  put(`/api/v1/live/products/${id}/unexplain`)

// ============ 礼物管理 ============
export interface Gift {
  id: number
  name: string
  icon: string
  animationUrl?: string
  price: number
  sortOrder: number
  createTime?: string
  updateTime?: string
}

export const getGiftList = () =>
  get<Gift[]>('/api/v1/live/gifts')

export const createGift = (data: Partial<Gift>) =>
  post<Gift>('/api/v1/live/gifts', data as Record<string, unknown>)

export const updateGift = (id: number, data: Partial<Gift>) =>
  put<Gift>(`/api/v1/live/gifts/${id}`, data as Record<string, unknown>)

export const deleteGift = (id: number) =>
  del(`/api/v1/live/gifts/${id}`)
