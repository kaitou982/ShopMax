import { orderApi } from '@shop/shared'
import { BASE_URL } from '@/http'

export const payMethods = [
  { value: 3, label: '余额支付', icon: 'wallet' },
  { value: 1, label: '支付宝', icon: 'auth' },
  { value: 2, label: '微信支付', icon: 'weixin' },
]

export async function doPay(orderId: number, payType: number): Promise<void> {
  await uni.showLoading({ title: '支付中...' })
  try {
    if (payType === 3) {
      await orderApi.pay(orderId, 3)
    } else {
      const token = uni.getStorageSync('token') || ''
      const resp = await new Promise<any>((resolve, reject) => {
        uni.request({
          url: `${BASE_URL}/api/v1/payments`,
          method: 'POST',
          header: { Authorization: token ? `Bearer ${token}` : '', 'Content-Type': 'application/json' },
          data: { orderId, payMethod: payType, scene: 'mobile' },
          success: r => { const d = r.data as any; if (d?.code === 200) resolve(d.data); else reject(d) },
          fail: reject,
        })
      })
      if (resp.mode === 'simulate' || resp.mode === 'mock') {
        await new Promise<void>((resolve, reject) => {
          uni.request({
            url: `${BASE_URL}/api/v1/payments/${resp.paymentNo}/mock-confirm`,
            method: 'POST',
            header: { Authorization: token ? `Bearer ${token}` : '', 'Content-Type': 'application/json' },
            success: r => { const d = r.data as any; if (d?.code === 200) resolve(); else reject(d) },
            fail: reject,
          })
        })
      }
    }
    uni.showToast({ title: '支付成功', icon: 'success' })
  } finally {
    uni.hideLoading()
  }
}
