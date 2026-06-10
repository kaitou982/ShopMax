import { getHttpClient } from '../utils/http'
import type { AddressInfo, CreateAddressForm, UpdateAddressForm } from '../types'

export const addressApi = {
  getList: () =>
    getHttpClient().get<AddressInfo[]>('/api/v1/users/addresses'),

  getDetail: (id: number) =>
    getHttpClient().get<AddressInfo>(`/api/v1/users/addresses/${id}`),

  create: (data: CreateAddressForm) =>
    getHttpClient().post<AddressInfo>('/api/v1/users/addresses', data as unknown as Record<string, unknown>),

  update: (id: number, data: UpdateAddressForm) =>
    getHttpClient().put<AddressInfo>(`/api/v1/users/addresses/${id}`, data as unknown as Record<string, unknown>),

  delete: (id: number) =>
    getHttpClient().delete<void>(`/api/v1/users/addresses/${id}`),

  getDefault: () =>
    getHttpClient().get<AddressInfo>('/api/v1/users/addresses/default'),

  setDefault: (id: number) =>
    getHttpClient().put<void>(`/api/v1/users/addresses/${id}/default`),
}
