import { getHttpClient } from '../utils/http'
import type {
  LoginResponse, LoginForm, PhoneLoginForm, EmailLoginForm, WxLoginForm,
  RegisterForm, SendEmailCodeForm, UpdateUserForm, ChangePasswordForm, UserInfo,
  MemberInfo, IntegralLog, BalanceLog, PageResult,
} from '../types'

export const userApi = {
  login: (data: LoginForm) =>
    getHttpClient().post<LoginResponse>('/api/v1/auth/login', data as unknown as Record<string, unknown>),

  loginByPhone: (data: PhoneLoginForm) =>
    getHttpClient().post<LoginResponse>('/api/v1/auth/login/phone', data as unknown as Record<string, unknown>),

  loginByWx: (data: WxLoginForm) =>
    getHttpClient().post<LoginResponse>('/api/v1/auth/login/wx', data as unknown as Record<string, unknown>),

  loginByEmail: (data: EmailLoginForm) =>
    getHttpClient().post<LoginResponse>('/api/v1/auth/login/email', data as unknown as Record<string, unknown>),

  register: (data: RegisterForm) =>
    getHttpClient().post<{ userId: number; username: string; nickname: string }>(
      '/api/v1/auth/register', data as unknown as Record<string, unknown>),

  sendSmsCode: (data: { phone: string; type: string }) =>
    getHttpClient().post<void>('/api/v1/auth/sms/send', data as unknown as Record<string, unknown>),

  sendEmailCode: (data: SendEmailCodeForm) =>
    getHttpClient().post<void>('/api/v1/auth/email/send-code', data as unknown as Record<string, unknown>),

  checkEmail: (email: string) =>
    getHttpClient().get<boolean>('/api/v1/auth/check-email', { email }),

  resetPassword: (data: { email: string; verifyCode: string; newPassword: string }) =>
    getHttpClient().post<void>('/api/v1/auth/reset-password', data as unknown as Record<string, unknown>),

  getCurrentUser: () =>
    getHttpClient().get<UserInfo>('/api/v1/users/me'),

  updateUserInfo: (data: UpdateUserForm) =>
    getHttpClient().put<UserInfo>('/api/v1/users/me', data as unknown as Record<string, unknown>),

  changePassword: (data: ChangePasswordForm) =>
    getHttpClient().put<void>('/api/v1/users/me/password', data as unknown as Record<string, unknown>),

  applyStore: (data: { storeName: string; storeLogo?: string; storeDescription?: string }) =>
    getHttpClient().post<void>('/api/v1/auth/store/apply', data as unknown as Record<string, unknown>),

  uploadAvatar: async (file: File): Promise<string> => {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    const resp = await fetch('/api/v1/users/me/avatar', {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    })
    if (!resp.ok) throw new Error('上传失败')
    const json = await resp.json()
    if (json.code !== 200) throw new Error(json.message || '上传失败')
    return json.data.url
  },
}

export const walletApi = {
  getMemberInfo: () =>
    getHttpClient().get<MemberInfo>('/api/v1/users/me/member-info'),

  getIntegralLogs: (params?: { pageNum?: number; pageSize?: number }) =>
    getHttpClient().get<PageResult<IntegralLog>>('/api/v1/users/me/integral-logs', params as Record<string, unknown>),

  getBalanceLogs: (params?: { pageNum?: number; pageSize?: number }) =>
    getHttpClient().get<PageResult<BalanceLog>>('/api/v1/users/me/balance-logs', params as Record<string, unknown>),

  recharge: (data: { amount: number; payChannel: string }) =>
    getHttpClient().post<{ message: string }>('/api/v1/users/me/recharge', data as unknown as Record<string, unknown>),
}
