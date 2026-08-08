<script setup lang="ts">
defineOptions({ name: 'SeckillPage' })

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon, NEmpty, NSpin, NButton, NProgress } from 'naive-ui'
import { FlashOutline, TimeOutline } from '@vicons/ionicons5'
import { seckillApi, type SeckillSession, type SeckillProduct } from '@shop/shared'

const router = useRouter()

// 状态
const loading = ref(true)
const sessions = ref<SeckillSession[]>([])
const products = ref<SeckillProduct[]>([])
const activeSessionId = ref<number | null>(null)
const countdown = ref('')
const seckillLoading = ref<Record<number, boolean>>({})

// 定时器
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 加载场次
const loadSessions = async () => {
  try {
    sessions.value = await seckillApi.getActiveSessions()
    if (sessions.value.length > 0) {
      activeSessionId.value = sessions.value[0].id
      await loadProducts(sessions.value[0].id)
    }
  } catch (error) {
    console.error('加载场次失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载商品
const loadProducts = async (sessionId: number) => {
  try {
    products.value = await seckillApi.getProducts(sessionId)
  } catch (error) {
    console.error('加载商品失败:', error)
    products.value = []
  }
}

// 切换场次
const switchSession = async (sessionId: number) => {
  activeSessionId.value = sessionId
  await loadProducts(sessionId)
  startCountdown()
}

// 开始倒计时
const startCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }

  const activeSession = sessions.value.find(s => s.id === activeSessionId.value)
  if (!activeSession) return

  const updateCountdown = () => {
    const now = new Date().getTime()
    const endTime = new Date(activeSession.endTime).getTime()
    const diff = endTime - now

    if (diff <= 0) {
      countdown.value = '已结束'
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
      return
    }

    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    countdown.value = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }

  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

// 计算进度
const getProgress = (product: SeckillProduct) => {
  const total = product.seckillStock + (product.soldCount || 0)
  if (total === 0) return 0
  return Math.round(((product.soldCount || 0) / total) * 100)
}

// 是否已售罄
const isSoldOut = (product: SeckillProduct) => {
  return product.seckillStock <= 0
}

// 执行秒杀
const handleSeckill = async (product: SeckillProduct) => {
  if (seckillLoading.value[product.id] || isSoldOut(product)) return

  seckillLoading.value[product.id] = true

  try {
    const result = await seckillApi.executeSeckill(product.productId, activeSessionId.value!)
    // 秒杀成功，跳转到订单确认页
    router.push(`/order/confirm?seckillOrderNo=${result.orderNo}`)
  } catch (error: any) {
    console.error('秒杀失败:', error)
    // 可以显示错误提示
  } finally {
    seckillLoading.value[product.id] = false
  }
}

// 格式化价格
const formatPrice = (price: number) => {
  return `¥${price.toFixed(2)}`
}

onMounted(() => {
  loadSessions()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<template>
  <div class="seckill-page">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <div class="seckill-header">
      <h1>
        <n-icon :size="24" color="#FF5000"><FlashOutline /></n-icon>
        限时秒杀
      </h1>
    </div>

    <!-- 场次选择 -->
    <div class="session-tabs" v-if="sessions.length > 0">
      <div
        v-for="session in sessions"
        :key="session.id"
        class="session-tab"
        :class="{ active: activeSessionId === session.id }"
        @click="switchSession(session.id)"
      >
        <div class="session-name">{{ session.name }}</div>
        <div class="session-time">
          {{ session.startTime?.slice(11, 16) }} - {{ session.endTime?.slice(11, 16) }}
        </div>
      </div>
    </div>

    <!-- 倒计时 -->
    <div class="countdown-area" v-if="activeSessionId && countdown">
      <n-icon :size="18" color="#FF5000"><TimeOutline /></n-icon>
      <span class="countdown-label">距离结束</span>
      <span class="countdown-value">{{ countdown }}</span>
    </div>

    <!-- 商品列表 -->
    <div class="product-area" v-if="!loading">
      <div v-if="products.length === 0" class="empty-area">
        <n-empty description="暂无秒杀商品" />
      </div>

      <div v-else class="product-grid">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
        >
          <div class="product-image">
            <img
              :src="product.productImage || '/api/v1/files/default/product'"
              :alt="product.productName"
            />
          </div>

          <div class="product-info">
            <div class="product-name">{{ product.productName || '商品' }}</div>

            <div class="product-price">
              <span class="seckill-price">{{ formatPrice(product.seckillPrice) }}</span>
              <span class="original-price" v-if="product.originalPrice">
                {{ formatPrice(product.originalPrice) }}
              </span>
            </div>

            <div class="product-progress">
              <n-progress
                type="line"
                :percentage="getProgress(product)"
                :show-indicator="false"
                :height="8"
                :border-radius="4"
                color="#FF5000"
              />
              <span class="progress-text">
                已抢 {{ getProgress(product) }}%
              </span>
            </div>

            <n-button
              type="error"
              block
              :disabled="isSoldOut(product)"
              :loading="seckillLoading[product.id]"
              @click="handleSeckill(product)"
            >
              {{ isSoldOut(product) ? '已抢光' : '立即秒杀' }}
            </n-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-else class="loading-area">
      <n-spin size="large" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.back-btn { display: inline-block; padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; margin-bottom: 12px; color: #666; &:hover { border-color: $brand-orange; color: $brand-orange; } }
.seckill-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.seckill-header {
  margin-bottom: 24px;

  h1 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 28px;
    font-weight: 700;
    color: #1c1c1e;
    margin: 0;
  }
}

.session-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.session-tab {
  flex-shrink: 0;
  padding: 16px 24px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;

  &:hover {
    border-color: #FF5000;
  }

  &.active {
    background: #FFF3EC;
    border-color: #FF5000;
  }
}

.session-name {
  font-size: 16px;
  font-weight: 600;
  color: #1c1c1e;
  margin-bottom: 4px;
}

.session-time {
  font-size: 13px;
  color: #666;
}

.countdown-area {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: linear-gradient(90deg, #FFF3EC, #FFF);
  border-radius: 12px;
  margin-bottom: 24px;
}

.countdown-label {
  font-size: 14px;
  color: #666;
}

.countdown-value {
  font-size: 24px;
  font-weight: 700;
  color: #FF5000;
  font-family: monospace;
}

.product-area {
  min-height: 400px;
}

.empty-area {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-area {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 1199px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 767px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.product-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f5f5f5;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 14px;
  color: #1c1c1e;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.seckill-price {
  font-size: 20px;
  font-weight: 700;
  color: #FF5000;
}

.original-price {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.product-progress {
  margin-bottom: 16px;
}

.progress-text {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
