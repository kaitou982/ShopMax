import { get, post, put, del } from '../request'

export interface Banner {
  id?: number
  title: string
  imageUrl: string
  linkUrl: string
  sort: number
  status: number
  createTime?: string
  updateTime?: string
}

export const getBannerList = () =>
  get<Banner[]>('/api/v1/admin/banners')

export const createBanner = (data: Banner) =>
  post<Banner>('/api/v1/admin/banners', data as unknown as Record<string, unknown>)

export const updateBanner = (id: number, data: Banner) =>
  put<Banner>(`/api/v1/admin/banners/${id}`, data as unknown as Record<string, unknown>)

export const deleteBanner = (id: number) =>
  del<void>(`/api/v1/admin/banners/${id}`)
