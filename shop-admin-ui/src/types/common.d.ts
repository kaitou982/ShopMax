// 通用类型定义

export interface TreeNode {
  id: number | string
  label: string
  children?: TreeNode[]
  [key: string]: unknown
}

export interface SelectOption {
  label: string
  value: string | number
  disabled?: boolean
}

export interface UploadFile {
  name: string
  url: string
}

export type StatusType = 'success' | 'warning' | 'danger' | 'info'

export type UserRole = 'ADMIN' | 'STORE'

export interface RouteMeta {
  title: string
  icon?: string
  public?: boolean
  requiresAuth?: boolean
  roles?: UserRole[]
}
