import { get, post, put, del } from '../request'
import type { UserInfo, PageResult, PageParams, UserUpdateRequest, ChangePasswordRequest, LoginResponse } from '@/types/api'

export const getUserList = (params?: PageParams) => {
  return get<PageResult<UserInfo>>('/api/v1/users', params)
}

export const getUserDetail = (id: number) => {
  return get<UserInfo>(`/api/v1/users/${id}`)
}

export const createUser = (data: {
    password: string;
    phone: string;
    memberLevel: number;
    nickname: string;
    email: string;
    username: string;
    status: number
}) => {
  return post<UserInfo>('/api/v1/users', data)
}

export const updateUser = (id: number, data: Partial<UserInfo>) => {
  return put<UserInfo>(`/api/v1/users/${id}`, data)
}

export const deleteUser = (id: number) => {
  return del(`/api/v1/users/${id}`)
}

export const getUserInfo = () => {
  return get<UserInfo>('/api/v1/users/me')
}

export const updateCurrentUser = (data: UserUpdateRequest) => {
  return put<UserInfo>('/api/v1/users/me', data as unknown as Record<string, unknown>)
}

export const changePassword = (data: ChangePasswordRequest) => {
  return put<void>('/api/v1/users/me/password', data as unknown as Record<string, unknown>)
}

export const login = (data: { username: string; password: string }) => {
  return post<LoginResponse>('/api/v1/auth/login', data)
}

export const applyStore = (data: { storeName: string; storeLogo?: string; storeDescription?: string }) => {
  return post<void>('/api/v1/auth/store/apply', data as unknown as Record<string, unknown>)
}

export const getStoreApplications = (pageNum: number, pageSize: number) => {
  return get<PageResult<UserInfo>>('/api/v1/users/store-applications', { pageNum, pageSize })
}

export const auditStore = (userId: number, data: { status: number; rejectReason?: string }) => {
  return put<void>(`/api/v1/users/${userId}/store-audit`, data as unknown as Record<string, unknown>)
}
