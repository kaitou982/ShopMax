export { setHttpClient, getHttpClient, type HttpClient } from './http'

/** 会员等级折扣系数（1=普通/银卡无折扣，3=金卡98折，4=钻石95折） */
const DISCOUNT_MAP: Record<number, number> = { 1: 1, 2: 1, 3: 0.98, 4: 0.90 }
const DISCOUNT_LABEL_MAP: Record<number, string> = { 1: '无折扣', 2: '无折扣', 3: '98折', 4: '95折' }

/** 获取会员折扣系数，1 表示无折扣 */
export function getMemberDiscount(level: number): number {
  return DISCOUNT_MAP[level] || 1
}

/** 获取会员折扣描述文本 */
export function getMemberDiscountLabel(level: number): string {
  return DISCOUNT_LABEL_MAP[level] || '无折扣'
}
