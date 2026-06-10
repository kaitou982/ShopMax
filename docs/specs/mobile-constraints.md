# 移动端开发约束规范 (UniApp)

## 一、技术栈约束

### 1.1 必须使用的技术
- **UniApp**: Vue 3 + 组合式API (强制)
- **TypeScript**: 5.x (强制)
- **UI框架**: uView Plus / UniUI (强制)
- **状态管理**: Pinia 2.x (强制)
- **HTTP库**: uni.request 封装
- **构建工具**: HBuilderX / CLI

### 1.2 支持的平台
- H5
- 微信小程序
- App (Android/iOS)

### 1.3 禁止使用的技术
- 禁止使用 Vue 2.x / Options API
- 禁止使用 Vuex
- 禁止使用 JavaScript (必须用 TypeScript)
- 禁止使用原生插件优先于UniApp API

---

## 二、项目结构约束

### 2.1 移动端项目结构
```
shop-mobile/
├── src/
│   ├── api/                    # API接口
│   │   ├── modules/
│   │   │   ├── user.ts
│   │   │   ├── product.ts
│   │   │   └── order.ts
│   │   └── request.ts          # 请求封装
│   ├── components/             # 公共组件
│   │   ├── common/             # 通用组件
│   │   └── business/           # 业务组件
│   ├── composables/            # 组合式函数
│   ├── pages/                  # 页面
│   │   ├── index/              # 首页
│   │   ├── category/           # 分类
│   │   ├── cart/               # 购物车
│   │   ├── user/               # 个人中心
│   │   ├── product/            # 商品详情
│   │   ├── order/              # 订单相关
│   │   ├── live/               # 直播
│   │   └── community/          # 社区
│   ├── static/                 # 静态资源
│   │   ├── images/
│   │   ├── icons/
│   │   └── fonts/
│   ├── stores/                 # Pinia状态管理
│   │   ├── modules/
│   │   └── index.ts
│   ├── types/                  # 类型定义
│   ├── utils/                  # 工具函数
│   │   ├── index.ts
│   │   ├── storage.ts
│   │   └── platform.ts
│   ├── App.vue
│   ├── main.ts
│   ├── manifest.json
│   ├── pages.json
│   └── uni.scss
├── .env
├── package.json
├── tsconfig.json
└── vite.config.ts
```

---

## 三、编码规范约束

### 3.1 文件命名规范
| 类型 | 规范 | 示例 |
|------|------|------|
| 页面 | 小写下划线 | pages/user/index.vue |
| 组件 | 大驼峰 | ProductCard.vue, UserAvatar.vue |
| API文件 | 小写 | user.ts, order.ts |
| 工具函数 | 小驼峰 | formatDate.ts |

### 3.2 页面开发规范
```vue
<script setup lang="ts">
// pages/product/detail.vue
import { ref, onLoad, onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/stores/modules/cart'
import ProductCard from '@/components/business/ProductCard.vue'
import type { ProductDetail } from '@/types/api'
import { getProductDetail } from '@/api/modules/product'

// 页面参数
type PageParams = {
  id: string
}

// 状态
const productId = ref<string>('')
const productDetail = ref<ProductDetail | null>(null)
const loading = ref<boolean>(false)
const selectedSku = ref<Record<string, string>>({})

// Store
const cartStore = useCartStore()

// 页面生命周期
onLoad((options: PageParams) => {
  productId.value = options.id
  fetchProductDetail()
})

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    const res = await getProductDetail(productId.value)
    productDetail.value = res
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 加入购物车
const handleAddToCart = () => {
  if (!selectedSku.value.id) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  cartStore.addToCart({
    productId: productId.value,
    skuId: selectedSku.value.id,
    quantity: 1
  })
  uni.showToast({ title: '已加入购物车', icon: 'success' })
}

// 立即购买
const handleBuyNow = () => {
  if (!selectedSku.value.id) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/order/confirm?productId=${productId.value}&skuId=${selectedSku.value.id}`
  })
}

// 分享
const handleShare = () => {
  // #ifdef MP-WEIXIN
  // 微信小程序分享
  // #endif
}

// 设置分享
onShareAppMessage(() => {
  return {
    title: productDetail.value?.name || '商品详情',
    path: `/pages/product/detail?id=${productId.value}`
  }
})
</script>

<template>
  <view class="product-detail">
    <!-- 商品图片轮播 -->
    <swiper class="swiper" circular>
      <swiper-item v-for="img in productDetail?.images" :key="img">
        <image :src="img" mode="aspectFill" />
      </swiper-item>
    </swiper>
    
    <!-- 商品信息 -->
    <view class="product-info">
      <text class="price">¥{{ productDetail?.price }}</text>
      <text class="name">{{ productDetail?.name }}</text>
    </view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="action-btn" @click="handleAddToCart">
        <text>加入购物车</text>
      </view>
      <view class="action-btn buy-btn" @click="handleBuyNow">
        <text>立即购买</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.product-detail {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

.swiper {
  height: 750rpx;
  
  image {
    width: 100%;
    height: 100%;
  }
}

.product-info {
  padding: 24rpx;
  background: #fff;
  
  .price {
    font-size: 40rpx;
    color: #ff5000;
    font-weight: bold;
  }
  
  .name {
    font-size: 32rpx;
    color: #333;
    margin-top: 16rpx;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  display: flex;
  padding: 0 24rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  
  .action-btn {
    flex: 1;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 40rpx;
    margin: 0 12rpx;
    
    &.buy-btn {
      background: linear-gradient(90deg, #ff9000, #ff5000);
      color: #fff;
    }
  }
}
</style>
```

---

## 四、API请求约束

### 4.1 请求封装
```typescript
// api/request.ts
import { useUserStore } from '@/stores/modules/user'

const BASE_URL = import.meta.env.VITE_API_BASE_URL

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, any>
  params?: Record<string, any>
  header?: Record<string, string>
  loading?: boolean
}

export const request = <T>(options: RequestOptions): Promise<T> => {
  return new Promise((resolve, reject) => {
    const userStore = useUserStore()
    
    if (options.loading) {
      uni.showLoading({ title: '加载中...' })
    }
    
    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': userStore.token ? `Bearer ${userStore.token}` : '',
        ...options.header
      },
      success: (res) => {
        const { code, message, data } = res.data as any
        
        if (code === 200) {
          resolve(data as T)
        } else if (code === 401) {
          userStore.logout()
          uni.navigateTo({ url: '/pages/login/index' })
          reject(new Error('登录已过期'))
        } else {
          uni.showToast({ title: message || '请求失败', icon: 'none' })
          reject(new Error(message))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      },
      complete: () => {
        if (options.loading) {
          uni.hideLoading()
        }
      }
    })
  })
}

// 封装常用方法
export const get = <T>(url: string, params?: Record<string, any>) => {
  return request<T>({ url, method: 'GET', params })
}

export const post = <T>(url: string, data?: Record<string, any>) => {
  return request<T>({ url, method: 'POST', data })
}

export const put = <T>(url: string, data?: Record<string, any>) => {
  return request<T>({ url, method: 'PUT', data })
}

export const del = <T>(url: string) => {
  return request<T>({ url, method: 'DELETE' })
}
```

---

## 五、平台适配约束

### 5.1 条件编译使用
```vue
<template>
  <view>
    <!-- #ifdef H5 -->
    <h5-header />
    <!-- #endif -->
    
    <!-- #ifdef MP-WEIXIN -->
    <mp-header />
    <!-- #endif -->
    
    <!-- #ifdef APP-PLUS -->
    <app-header />
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
// 平台判断
const platform = uni.getSystemInfoSync().platform

// 方法中的平台适配
const handlePay = () => {
  // #ifdef MP-WEIXIN
  wxPay()
  // #endif
  
  // #ifdef APP-PLUS
  appPay()
  // #endif
  
  // #ifdef H5
  h5Pay()
  // #endif
}
</script>

<style>
/* 平台特定样式 */
/* #ifdef H5 */
.h5-specific {
  padding-top: 44px;
}
/* #endif */

/* #ifdef MP-WEIXIN */
.mp-specific {
  padding-top: 0;
}
/* #endif */
</style>
```

---

## 六、性能优化约束

### 6.1 必须遵守的优化
```typescript
// 1. 图片懒加载
<image src="url" mode="aspectFill" lazy-load />

// 2. 列表使用虚拟滚动 (长列表)
// 使用 scroll-view 或第三方组件

// 3. 页面数据分页加载
const loadMore = async () => {
  if (loading.value || !hasMore.value) return
  page.value++
  await fetchList()
}

// 4. 避免频繁setData
// 合并数据更新
const updateData = () => {
  const newData = { ...data, ...updates }
  Object.assign(data, newData)
}

// 5. 使用onUnload清理资源
onUnload(() => {
  // 清除定时器、事件监听等
})
```

---

## 七、安全约束

### 7.1 安全规范
```typescript
// 1. 敏感数据加密存储
import { encrypt, decrypt } from '@/utils/crypto'

// 存储加密
uni.setStorageSync('sensitive', encrypt(data))

// 2. 防XSS - 用户输入过滤
const sanitizeInput = (input: string): string => {
  return input.replace(/[<>"']/g, '')
}

// 3. 请求签名 (防篡改)
const generateSign = (params: Record<string, any>): string => {
  // 按key排序后拼接，再加secret做MD5
  const sortedKeys = Object.keys(params).sort()
  const signStr = sortedKeys.map(k => `${k}=${params[k]}`).join('&')
  return md5(signStr + 'secret_key')
}
```

---

## 八、代码审查清单

### 8.1 提交前自查
- [ ] TypeScript类型正确定义
- [ ] 平台适配已处理 (H5/小程序/App)
- [ ] 图片已压缩并使用懒加载
- [ ] rpx单位使用正确
- [ ] 安全区域适配 (safe-area-inset)
- [ ] 分享功能已配置
- [ ] 加载状态处理
- [ ] 错误提示友好

### 8.2 Code Review检查点
- [ ] 页面生命周期使用正确
- [ ] 资源已正确释放
- [ ] 兼容性问题处理
- [ ] 性能优化考虑
- [ ] 用户体验细节
