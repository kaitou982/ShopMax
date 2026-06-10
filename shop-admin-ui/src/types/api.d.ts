// API 通用类型
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageParams extends Record<string, unknown> {
  pageNum?: number
  pageSize?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  pages: number
  pageNum: number
  pageSize: number
}

// 用户相关
export interface UserInfo {
  userId: number
  username: string
  nickname: string
  avatar?: string
  email: string
  phone: string
  gender: number
  birthday?: string
  status: number
  memberLevel: number
  memberLevelName: string
  integral: number
  balance: number
  growthValue: number
  role: string
  storeStatus?: number
  storeName?: string
  storeLogo?: string
  storeDescription?: string
  lastLoginTime?: string
  createTime?: string
}

export interface UserUpdateRequest {
  nickname?: string
  phone?: string
  avatar?: string
  gender?: number
  birthday?: string
  email?: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface LoginRequest {
  username: string
  password: string
  captcha?: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  nickname: string
  avatar: string
  phone: string
  gender: number
  memberLevel: number
  memberLevelName: string
  integral: number
  balance: number
  growthValue: number
  role: string
  storeStatus?: number
  storeName?: string
  lastLoginTime: string
}
