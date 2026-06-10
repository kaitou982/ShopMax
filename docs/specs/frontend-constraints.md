# 前端开发约束规范 (Vue 3)

## 一、技术栈约束

### 1.1 必须使用的技术
- **Vue**: 3.4.x (Composition API 强制)
- **TypeScript**: 5.x (强制)
- **Vite**: 5.x (构建工具，强制)
- **状态管理**: Pinia 2.x (强制)
- **UI框架**: Element Plus 2.x (后台)，uView/UniUI (移动端)
- **HTTP库**: Axios (强制)
- **路由**: Vue Router 4.x (强制)

### 1.2 禁止使用的技术
- 禁止使用 Vue 2.x / Options API
- 禁止使用 Vuex (统一用 Pinia)
- 禁止使用 JavaScript (必须用 TypeScript)
- 禁止使用 Element UI (用 Element Plus)
- 禁止使用 Moment.js (用 Day.js)
- 禁止使用 Lodash (使用 ES6+ 原生方法)

---

## 二、项目结构约束

### 2.1 后台管理项目结构
```
shop-admin-ui/
├── src/
│   ├── api/                    # API接口
│   │   ├── modules/
│   │   │   ├── user.ts
│   │   │   ├── product.ts
│   │   │   └── order.ts
│   │   └── request.ts          # axios封装
│   ├── assets/                 # 静态资源
│   │   ├── images/
│   │   └── styles/
│   ├── components/             # 公共组件
│   │   ├── common/             # 通用组件
│   │   └── business/           # 业务组件
│   ├── composables/            # 组合式函数
│   ├── directives/             # 自定义指令
│   ├── layouts/                # 布局组件
│   ├── router/                 # 路由配置
│   │   ├── index.ts
│   │   └── routes.ts
│   ├── stores/                 # Pinia状态管理
│   │   ├── modules/
│   │   │   ├── user.ts
│   │   │   └── app.ts
│   │   └── index.ts
│   ├── types/                  # 类型定义
│   │   ├── api.d.ts
│   │   └── common.d.ts
│   ├── utils/                  # 工具函数
│   │   ├── index.ts
│   │   ├── storage.ts
│   │   └── validate.ts
│   ├── views/                  # 页面视图
│   │   ├── login/
│   │   ├── dashboard/
│   │   ├── user/
│   │   ├── product/
│   │   └── order/
│   ├── App.vue
│   └── main.ts
├── .env
├── .env.development
├── .env.production
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

---

## 三、编码规范约束

### 3.1 文件命名规范
| 类型 | 规范 | 示例 |
|------|------|------|
| 组件 | 大驼峰 | UserList.vue, ProductDetail.vue |
| 页面 | 小写中横线 | user-list.vue, product-detail.vue |
| API文件 | 小写 | user.ts, product.ts |
| 工具函数 | 小驼峰 | formatDate.ts, validatePhone.ts |
| 类型定义 | 小驼峰 + .d.ts | user.d.ts, api.d.ts |
| 常量 | 大写下划线 | constants.ts |

### 3.2 组件开发规范
```vue
<!-- 组件名必须多个单词，避免与HTML标签冲突 -->
<script setup lang="ts">
// 1. 导入顺序：Vue -> 第三方 -> 内部模块
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import type { UserInfo } from '@/types/api'

// 2. 组件名
defineOptions({
  name: 'UserList'
})

// 3. Props定义 (必须指定类型和默认值)
interface Props {
  userId: number
  showDelete?: boolean
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  showDelete: false,
  title: '用户列表'
})

// 4. Emits定义 (必须指定类型)
interface Emits {
  (e: 'update', id: number): void
  (e: 'delete', id: number): void
}

const emit = defineEmits<Emits>()

// 5. 状态定义 (ref类型必须明确)
const loading = ref<boolean>(false)
const userList = ref<UserInfo[]>([])
const currentPage = ref<number>(1)

// 6. 计算属性
const totalPages = computed(() => Math.ceil(userList.value.length / 10))

// 7. 方法
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList({ page: currentPage.value })
    userList.value = res.data
  } finally {
    loading.value = false
  }
}

const handleDelete = (id: number) => {
  emit('delete', id)
}

// 8. 生命周期
onMounted(() => {
  fetchUserList()
})
</script>

<template>
  <div class="user-list">
    <h2>{{ title }}</h2>
    <el-table v-loading="loading" :data="userList">
      <!-- 表格内容 -->
    </el-table>
  </div>
</template>

<style scoped lang="scss">
.user-list {
  padding: 20px;
  
  h2 {
    margin-bottom: 16px;
  }
}
</style>
```

### 3.3 TypeScript约束
```typescript
// 1. 禁止 any 类型 (除特殊情况外)
// ❌
const data: any = response.data

// ✅
const data: UserInfo = response.data as UserInfo

// 2. 接口命名规范
interface UserInfo {
  id: number
  username: string
  email?: string  // 可选属性
}

// 3. 类型别名用于联合类型
type OrderStatus = 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled'

// 4. 枚举使用
enum UserRole {
  ADMIN = 'admin',
  USER = 'user',
  GUEST = 'guest'
}

// 5. 泛型约束
interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}
```

---

## 四、状态管理约束

### 4.1 Pinia Store规范
```typescript
// stores/modules/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/api'
import { getUserInfo } from '@/api/modules/user'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.username || '')

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const fetchUserInfo = async () => {
    if (!token.value) return
    const res = await getUserInfo()
    userInfo.value = res.data
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userName,
    setToken,
    fetchUserInfo,
    logout
  }
})
```

---

## 五、API请求约束

### 5.1 Axios封装
```typescript
// api/request.ts
import axios from 'axios'
import { useUserStore } from '@/stores/modules/user'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    if (code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

### 5.2 API模块定义
```typescript
// api/modules/user.ts
import request from '../request'
import type { UserInfo, UserListParams, UserListResult } from '@/types/api'

export const getUserList = (params: UserListParams) => {
  return request.get<UserListResult>('/api/v1/users', { params })
}

export const getUserDetail = (id: number) => {
  return request.get<UserInfo>(`/api/v1/users/${id}`)
}

export const createUser = (data: Partial<UserInfo>) => {
  return request.post<UserInfo>('/api/v1/users', data)
}

export const updateUser = (id: number, data: Partial<UserInfo>) => {
  return request.put<UserInfo>(`/api/v1/users/${id}`, data)
}

export const deleteUser = (id: number) => {
  return request.delete(`/api/v1/users/${id}`)
}
```

---

## 六、路由约束

### 6.1 路由配置
```typescript
// router/routes.ts
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'home' }
      },
      {
        path: '/user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'user' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

export default routes
```

### 6.2 路由守卫
```typescript
// router/index.ts
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - ShopMax` : 'ShopMax'
  
  // 权限校验
  if (!to.meta.public && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})
```

---

## 七、样式约束

### 7.1 SCSS规范
```scss
// 1. 变量定义
$primary-color: #409eff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;

$text-primary: #303133;
$text-regular: #606266;
$text-secondary: #909399;

// 2. 使用BEM命名
.user-card {
  padding: 20px;
  
  &__header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
  }
  
  &__avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
  }
  
  &__info {
    margin-left: 12px;
  }
  
  &--active {
    border: 2px solid $primary-color;
  }
}

// 3. 深度选择器使用
:deep(.el-table) {
  // 覆盖Element样式
}
```

---

## 八、性能优化约束

### 8.1 必须遵守的优化
```typescript
// 1. 组件懒加载
const UserList = () => import('@/views/user/index.vue')

// 2. 使用v-show代替v-if频繁切换
// 3. 使用computed缓存计算结果
// 4. 长列表使用虚拟滚动
// 5. 图片懒加载
// 6. 防抖节流处理
import { debounce, throttle } from '@/utils'

const handleSearch = debounce((keyword: string) => {
  // 搜索逻辑
}, 300)

// 7. 事件销毁
import { onUnmounted } from 'vue'

const handler = () => {}
window.addEventListener('resize', handler)

onUnmounted(() => {
  window.removeEventListener('resize', handler)
})
```

---

## 九、代码审查清单

### 9.1 提交前自查
- [ ] TypeScript类型正确定义
- [ ] 没有any类型
- [ ] 没有console.log
- [ ] 组件name属性已设置
- [ ] 样式使用scoped
- [ ] 图片资源已压缩
- [ ] 代码格式化 (eslint + prettier)

### 9.2 Code Review检查点
- [ ] 组件职责单一
- [ ] Props/Emits类型定义完整
- [ ] 没有内存泄漏风险
- [ ] 错误处理完善
- [ ] 用户体验考虑 (loading、空状态)
