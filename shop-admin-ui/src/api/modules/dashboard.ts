import { get } from '../request'

export interface DashboardStats {
  todayOrders: number
  todaySales: number
  todayNewUsers: number
  pendingOrders: number
  orderChange: number
  salesChange: number
  userChange: number
  pendingChange: number
}

export interface SalesTrendItem {
  date: string
  amount: number
}

export interface RecentOrder {
  id: number
  orderNo: string
  amount: number
  status: number
  time: string
}

export const getDashboardStats = () =>
  get<DashboardStats>('/api/v1/admin/dashboard/stats')

export const getSalesTrend = () =>
  get<SalesTrendItem[]>('/api/v1/admin/dashboard/sales-trend')

export const getRecentOrders = () =>
  get<RecentOrder[]>('/api/v1/admin/dashboard/recent-orders')
