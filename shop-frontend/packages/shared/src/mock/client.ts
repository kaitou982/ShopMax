// Mock HTTP Client — 拦截 API 调用并返回演示数据
import type { HttpClient } from '../utils/http'
import * as M from './data'

const delay = (ms = 200) => new Promise(r => setTimeout(r, ms))

const ok = <T>(data: T): T => data as T

export function createMockClient(real: HttpClient): HttpClient {
  // 自动登录
  localStorage.setItem('token', M.demoUser.token)
  localStorage.setItem('userInfo', JSON.stringify(M.demoUser))

  const mock: HttpClient = {
    async get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
      await delay()
      // 用户
      if (url === '/api/v1/users/me' || url.includes('/api/v1/users/profile')) return ok(M.demoUser) as T
      if (url.includes('/api/v1/users/member-info') || url.includes('/api/v1/wallet/member-info')) return ok(M.memberInfo) as T
      if (url.includes('/api/v1/wallet/integral-logs')) return ok({ records: [], total: 0, pages: 0, current: 1, size: 10 }) as T
      if (url.includes('/api/v1/wallet/balance-logs')) return ok({ records: [], total: 0, pages: 0, current: 1, size: 10 }) as T
      if (url.includes('/api/v1/addresses')) return ok(M.addresses) as T

      // 商品
      if (url.match(/\/api\/v1\/products\/\d+$/)) {
        const id = Number(url.split('/').pop())
        return ok(M.products.find(p => p.id === id) || null) as T
      }
      if (url.includes('/api/v1/products/recommend')) return ok(M.products.slice(0, 8)) as T
      if (url.includes('/api/v1/products/new')) return ok(M.products.filter(p => p.isNew)) as T
      if (url.includes('/api/v1/products/new-page')) return ok({ records: M.products.filter(p => p.isNew), total: M.products.filter(p => p.isNew).length, pages: 1, current: 1, size: 20 }) as T
      if (url === '/api/v1/products') {
        const catId = params?.categoryId ? Number(params.categoryId) : 0
        const list = catId ? M.products.filter(p => p.categoryId === catId || M.categories.find(c => c.id === catId)?.children?.some(ch => ch.id === p.categoryId)) : M.products
        return ok({ records: list, total: list.length, pages: 1, current: 1, size: 20 }) as T
      }
      if (url.includes('/api/v1/categories/tree')) return ok(M.categories) as T
      if (url.includes('/api/v1/brands')) return ok([{ id: 1, name: 'ShopMax精选', logo: '' }]) as T
      if (url.includes('/api/v1/products/new-product-banners')) return ok(M.newProductBanners) as T

      // Banner
      if (url.includes('/api/v1/banners/active') || url.includes('/api/v1/banners')) return ok(M.banners) as T

      // 社区
      if (url.match(/\/api\/v1\/community\/notes\/\d+\/comments/)) {
        const noteId = Number(url.split('/')[5])
        return ok({ records: M.makeComments(noteId), total: M.makeComments(noteId).length, pages: 1, current: 1, size: 20 }) as T
      }
      if (url.match(/\/api\/v1\/community\/notes\/\d+$/)) {
        const noteId = Number(url.split('/').pop())
        return ok(M.notes.find(n => n.id === noteId) || null) as T
      }
      if (url.includes('/api/v1/community/notes')) return ok({ records: M.notes, total: M.notes.length, pages: 1, current: 1, size: 20 }) as T
      if (url.includes('/api/v1/community/users/me/favorites')) return ok({ records: M.notes.filter(n => n.isFavorited), total: 1, pages: 1, current: 1, size: 10 }) as T

      // 直播
      if (url.includes('/api/v1/live/rooms')) return ok(M.liveRooms) as T

      // 秒杀
      if (url.includes('/api/v1/seckill/sessions')) return ok(M.seckillSessions) as T
      if (url.includes('/api/v1/seckill/products')) return ok(M.seckillProducts) as T

      // 优惠券
      if (url.includes('/api/v1/coupons/available')) return ok({ records: M.coupons, total: M.coupons.length, pages: 1, current: 1, size: 10 }) as T
      if (url.includes('/api/v1/coupons/my')) return ok({ records: [M.coupons[0]], total: 1, pages: 1, current: 1, size: 10 }) as T

      // 订单
      if (url.match(/\/api\/v1\/orders\/\d+$/)) {
        const id = Number(url.split('/').pop())
        return ok(M.orders.find(o => o.id === id) || null) as T
      }
      if (url.includes('/api/v1/orders')) return ok(M.orders) as T

      // 搜索
      if (url.includes('/api/v1/products/search') || url.includes('/api/v1/search')) {
        const q = (params?.keyword || params?.q || '') as string
        const results = q ? M.products.filter(p => p.name.includes(q)) : M.products
        return ok({ records: results, total: results.length, pages: 1, current: 1, size: 20 }) as T
      }
      if (url.includes('/api/v1/search/hot-keywords')) return ok(['iPhone 16', '华为 Mate 70', 'AirPods', '春季新款', '三只松鼠', '兰蔻']) as T

      // 新品 Banner
      if (url.includes('/api/v1/products/new-product-banners')) return ok(M.newProductBanners) as T

      return real.get<T>(url, params)
    },

    async post<T>(url: string, data?: Record<string, unknown>): Promise<T> {
      await delay()
      // 登录
      if (url.includes('/api/v1/auth/login') || url.includes('/api/v1/auth/phone-login') || url.includes('/api/v1/auth/email-login'))
        return ok(M.demoUser) as T
      if (url.includes('/api/v1/auth/register'))
        return ok({ ...M.demoUser, userId: 2, nickname: '新用户' }) as T
      if (url.includes('/api/v1/auth/sms/send') || url.includes('/api/v1/auth/email/send-code'))
        return ok(true) as T
      if (url.includes('/api/v1/auth/reset-password') || url.includes('/api/v1/auth/check-email'))
        return ok(true) as T

      // 点赞/收藏 (toggle, return boolean)
      if (url.match(/\/api\/v1\/community\/notes\/\d+\/like$/)) return ok(true) as T
      if (url.match(/\/api\/v1\/community\/notes\/\d+\/favorite$/)) return ok(true) as T
      // 评论
      if (url.match(/\/api\/v1\/community\/notes\/\d+\/comments$/)) {
        return ok({ id: Date.now(), noteId: Number(url.split('/')[5]), userId: 1, userNickname: M.demoUser.nickname, content: data?.content || '', likeCount: 0, createTime: new Date().toISOString(), children: [], replyToUserNickname: '', parentId: null, replyToUserId: null }) as T
      }

      // 收货地址
      if (url.includes('/api/v1/addresses')) return ok(1) as T

      // 领券
      if (url.includes('/api/v1/coupons/receive') || url.includes('/api/v1/coupons/exchange')) return ok(true) as T

      // 下单
      if (url.includes('/api/v1/orders')) return ok({ id: Date.now(), orderNo: 'SM' + Date.now() }) as T

      return real.post<T>(url, data)
    },

    async put<T>(url: string, data?: Record<string, unknown>): Promise<T> {
      await delay()
      if (url.includes('/api/v1/users/me') || url.includes('/api/v1/users/profile')) return ok(M.demoUser) as T
      if (url.includes('/api/v1/addresses')) return ok(true) as T
      return real.put<T>(url, data)
    },

    async delete<T>(url: string): Promise<T> {
      await delay()
      if (url.includes('/api/v1/addresses')) return ok(true) as T
      return real.delete<T>(url)
    },
  }

  return mock
}
