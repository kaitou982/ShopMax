import { get, post, put } from '../request'

export interface Logistics {
  id: number
  orderId: number
  logisticsNo: string
  company: string
  companyCode: string
  status: number
  senderName: string
  senderPhone: string
  senderAddress: string
  senderLongitude?: number
  senderLatitude?: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  receiverLongitude?: number
  receiverLatitude?: number
  lastQueryTime: string
  createTime: string
}

export interface LogisticsTrace {
  id: number
  logisticsId: number
  traceTime: string
  content: string
  location: string
  latitude: number
  longitude: number
  createTime: string
}

export interface CreateLogisticsRequest {
  orderId: number
  logisticsNo: string
  company: string
  senderName?: string
  senderPhone?: string
  senderAddress?: string
  senderLongitude?: number
  senderLatitude?: number
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  receiverLongitude?: number
  receiverLatitude?: number
}

// 物流公司列表
export const logisticsCompanies = [
  { name: '顺丰速运', code: 'shunfeng' },
  { name: '韵达快递', code: 'yunda' },
  { name: '圆通速递', code: 'yuantong' },
  { name: '中通快递', code: 'zhongtong' },
  { name: '申通快递', code: 'shentong' },
  { name: '京东物流', code: 'jd' },
  { name: 'EMS', code: 'ems' },
  { name: '德邦快递', code: 'debang' },
  { name: '极兔速递', code: 'jtexpress' },
  { name: '百世快递', code: 'huitongkuaidi' }
]

// 创建物流
export const createLogistics = (data: CreateLogisticsRequest) => {
  return post<Logistics>('/api/v1/logistics', data)
}

// 查询订单物流
export const getLogisticsByOrderId = (orderId: number) => {
  return get<Logistics>(`/api/v1/logistics/order/${orderId}`)
}

// 获取物流详情
export const getLogisticsDetail = (id: number) => {
  return get<Logistics>(`/api/v1/logistics/${id}`)
}

// 获取物流轨迹
export const getLogisticsTraces = (id: number) => {
  return get<LogisticsTrace[]>(`/api/v1/logistics/${id}/traces`)
}

// 刷新物流
export const refreshLogistics = (id: number) => {
  return post(`/api/v1/logistics/${id}/refresh`)
}
