import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import { useTabsStore } from '@/stores/modules/tabs'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { routes } from './routes'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ left: 0, top: 0 })
})

NProgress.configure({ showSpinner: false })

router.beforeEach((to, _from, next) => {
  NProgress.start()

  const userStore = useUserStore()

  document.title = to.meta.title ? `${to.meta.title} - ShopMax` : 'ShopMax'

  if (to.meta.requiresAuth && !userStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && userStore.token) {
    next('/')
  } else if (to.meta.roles && !(to.meta.roles as string[]).includes(userStore.userRole)) {
    next('/dashboard')
    import('element-plus').then(({ ElMessage }) => ElMessage.warning('您没有权限访问该页面'))
  } else {
    next()
  }
})

router.afterEach((to) => {
  NProgress.done()

  if (to.meta.title && !to.meta.public && to.name !== 'NotFound') {
    const tabsStore = useTabsStore()
    tabsStore.addTab(to)
  }
})

export default router
