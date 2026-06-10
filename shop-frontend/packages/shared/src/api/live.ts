import { getHttpClient } from '../utils/http'
import type { LiveRoom, LiveProduct, Gift } from '../types'

export const liveRoomApi = {
  // 直播间列表
  getLivingRooms: () =>
    getHttpClient().get<LiveRoom[]>('/api/v1/live/rooms/living'),

  getRoomDetail: (id: number) =>
    getHttpClient().get<LiveRoom>(`/api/v1/live/rooms/${id}`),

  getRoomList: () =>
    getHttpClient().get<{ records: LiveRoom[] }>('/api/v1/live/rooms/public').then(res => res.records),

  // 直播间商品
  getRoomProducts: (roomId: number) =>
    getHttpClient().get<LiveProduct[]>(`/api/v1/live/products/room/${roomId}`),

  // 礼物相关
  getGifts: () =>
    getHttpClient().get<Gift[]>('/api/v1/live/gifts'),

  getCoinBalance: () =>
    getHttpClient().get<number>('/api/v1/live/gifts/balance'),

  // 回放相关
  getReplayList: () =>
    getHttpClient().get<LiveRoom[]>('/api/v1/live/replays'),
}
