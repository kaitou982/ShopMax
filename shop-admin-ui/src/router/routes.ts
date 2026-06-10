import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/index.vue'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', roles: ['ADMIN', 'STORE'] }
      },
      {
        path: '/user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: ['ADMIN'] }
      },
      {
        path: '/product',
        name: 'Product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '商品管理', icon: 'GoodsFilled', roles: ['ADMIN', 'STORE'] }
      },
      {
        path: '/order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理', icon: 'List', roles: ['ADMIN', 'STORE'] }
      },
      {
        path: '/marketing',
        name: 'Marketing',
        component: () => import('@/views/marketing/index.vue'),
        meta: { title: '营销活动', icon: 'Ticket', roles: ['ADMIN'] }
      },
      {
        path: '/live',
        name: 'Live',
        component: () => import('@/views/live/index.vue'),
        meta: { title: '直播管理', icon: 'VideoCameraFilled', roles: ['ADMIN'] }
      },
      {
        path: '/community',
        name: 'Community',
        component: () => import('@/views/community/index.vue'),
        meta: { title: '内容社区', icon: 'ChatDotRound', roles: ['ADMIN', 'STORE'] }
      },
      {
        path: '/customer-service/faq',
        name: 'CustomerServiceFaq',
        component: () => import('@/views/customer-service/faq.vue'),
        meta: { title: 'FAQ管理', icon: 'ChatLineSquare', roles: ['ADMIN'] }
      },
      {
        path: '/customer-service/sessions',
        name: 'CustomerServiceSessions',
        component: () => import('@/views/customer-service/sessions.vue'),
        meta: { title: '客服会话', icon: 'ChatDotSquare', roles: ['ADMIN'] }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User', roles: ['ADMIN', 'STORE'] }
      },
      {
        path: '/system',
        name: 'System',
        component: () => import('@/views/system/index.vue'),
        meta: { title: '系统设置', icon: 'Setting', roles: ['ADMIN'] }
      },
      {
        path: '/banner',
        name: 'Banner',
        component: () => import('@/views/banner/index.vue'),
        meta: { title: '轮播图管理', icon: 'PictureFilled', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]
