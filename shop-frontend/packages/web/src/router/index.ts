import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/login/index.vue'),
    meta: { title: '登录', blank: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/register/index.vue'),
    meta: { title: '注册', blank: true },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/pages/forgot-password/index.vue'),
    meta: { title: '忘记密码', blank: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/pages/home/index.vue'), meta: { title: '首页' } },
      { path: 'category/:id?', name: 'Category', component: () => import('@/pages/category/index.vue'), meta: { title: '分类' } },
      { path: 'search', name: 'Search', component: () => import('@/pages/search/index.vue'), meta: { title: '搜索' } },
      { path: 'product/:id', name: 'Product', component: () => import('@/pages/product/detail.vue'), meta: { title: '商品详情' } },
      { path: 'cart', name: 'Cart', component: () => import('@/pages/cart/index.vue'), meta: { title: '购物车' } },
      { path: 'order/confirm', name: 'OrderConfirm', component: () => import('@/pages/order/confirm.vue'), meta: { title: '确认订单' } },
      { path: 'order/list', name: 'OrderList', component: () => import('@/pages/order/list.vue'), meta: { title: '我的订单' } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/pages/order/detail.vue'), meta: { title: '订单详情' } },
      { path: 'user', name: 'User', component: () => import('@/pages/user/index.vue'), meta: { title: '个人中心' } },
      { path: 'user/coupons', name: 'MyCoupons', component: () => import('@/pages/user/coupons.vue'), meta: { title: '我的优惠券' } },
      { path: 'coupons', name: 'CouponCenter', component: () => import('@/pages/coupons/index.vue'), meta: { title: '领券中心' } },
      { path: 'community', name: 'Community', component: () => import('@/pages/community/index.vue'), meta: { title: '社区' } },
      { path: 'community/:id', name: 'NoteDetail', component: () => import('@/pages/community/detail.vue'), meta: { title: '笔记详情' } },
      { path: 'live', name: 'Live', component: () => import('@/pages/live/index.vue'), meta: { title: '直播' } },
      { path: 'live/:id', name: 'LiveRoom', component: () => import('@/pages/live/room.vue'), meta: { title: '直播间' } },
      { path: 'seckill', name: 'Seckill', component: () => import('@/pages/seckill/index.vue'), meta: { title: '限时秒杀' } },
    ],
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

// 路由守卫：未登录跳转登录页
const PUBLIC_PATHS = ['/login', '/register', '/forgot-password']
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (!token && !PUBLIC_PATHS.includes(to.path)) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})
