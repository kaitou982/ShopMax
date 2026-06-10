<script setup lang="ts">
defineOptions({ name: 'SeckillPage' })
import { ref, computed, onMounted } from 'vue'
import { seckillApi, type SeckillSession, type SeckillProduct } from '@shop/shared'

const sessions = ref<SeckillSession[]>([])
const activeSession = ref<SeckillSession | null>(null)
const products = ref<SeckillProduct[]>([])
const loading = ref(true)
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  try {
    loading.value = true
    const list = await seckillApi.getActiveSessions()
    sessions.value = list || []
    if (sessions.value.length) {
      selectSession(sessions.value[0])
    }
  } finally {
    loading.value = false
  }
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})

function sessionLabel(s: SeckillSession) {
  const start = new Date(s.startTime)
  return `${start.getHours().toString().padStart(2, '0')}:00`
}

async function selectSession(s: SeckillSession) {
  activeSession.value = s
  try {
    const list = await seckillApi.getProducts(s.id)
    products.value = list || []
  } catch {
    products.value = []
  }
}

function getTimeLeft(s: SeckillSession) {
  const end = new Date(s.endTime).getTime()
  const diff = end - now.value
  if (diff <= 0) return '已结束'
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const sec = Math.floor((diff % 60000) / 1000)
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}

function getProgress(p: SeckillProduct) {
  if (!p.stock || p.stock <= 0) return 100
  return Math.min(100, Math.round(((p.soldCount || 0) / (p.stock + (p.soldCount || 0))) * 100))
}

async function doSeckill(p: SeckillProduct) {
  if (!activeSession.value) return
  const confirmed = await new Promise<boolean>(resolve =>
    uni.showModal({ title: '秒杀确认', content: `确定以 ¥${p.seckillPrice} 秒杀 "${p.productName}"？`, success: res => resolve(res.confirm) })
  )
  if (!confirmed) return
  await uni.showLoading({ title: '秒杀中...' })
  try {
    await seckillApi.executeSeckill(p.productId, activeSession.value.id)
    uni.showToast({ title: '秒杀成功！', icon: 'success' })
    if (activeSession.value) selectSession(activeSession.value)
  } catch {
    uni.showToast({ title: '秒杀失败，库存不足', icon: 'none' })
  } finally { uni.hideLoading() }
}
</script>

<template>
  <view class="sk">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="!sessions.length" class="empty">暂无秒杀活动</view>
    <template v-else>
      <!-- Session Tabs -->
      <view class="session-bar">
        <view
          v-for="s in sessions" :key="s.id"
          class="session-tab" :class="{ active: activeSession?.id === s.id }"
          @click="selectSession(s)"
        >
          <text class="session-time">{{ sessionLabel(s) }}</text>
          <text class="session-status">{{ getTimeLeft(s) }}</text>
        </view>
      </view>

      <!-- Products -->
      <view class="product-list">
        <view class="product-card" v-for="p in products" :key="p.id">
          <image :src="p.productImage || '/api/v1/files/default/product'" class="product-img" mode="aspectFill"/>
          <view class="product-info">
            <text class="product-name">{{ p.productName }}</text>
            <view class="price-row">
              <text class="seckill-price">¥{{ p.seckillPrice }}</text>
              <text class="original-price">¥{{ p.originalPrice }}</text>
            </view>
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: getProgress(p) + '%' }"/>
              <text class="progress-text">已抢 {{ getProgress(p) }}%</text>
            </view>
            <button class="seckill-btn" :class="{ disabled: getProgress(p) >= 100 }" @click="doSeckill(p)">
              {{ getProgress(p) >= 100 ? '已抢光' : '立即秒杀' }}
            </button>
          </view>
        </view>
        <view v-if="!products.length" class="empty-products">该场次暂无商品</view>
      </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.sk { min-height: 100vh; background: #f5f5f5; }
.loading, .empty { text-align: center; padding: 200rpx 0; color: #999; }

.session-bar {
  display: flex; background: #fff; padding: 16rpx; gap: 12rpx;
  overflow-x: auto; white-space: nowrap; -webkit-overflow-scrolling: touch;
}
.session-tab {
  flex-shrink: 0; padding: 16rpx 28rpx; border-radius: 12rpx;
  background: #f5f5f5; text-align: center; min-width: 140rpx;
}
.session-tab.active { background: #FF5000; }
.session-tab.active .session-time,
.session-tab.active .session-status { color: #fff; }
.session-time { font-size: 30rpx; font-weight: 700; color: #333; display: block; }
.session-status { font-size: 20rpx; color: #999; display: block; margin-top: 4rpx; }

.product-list { padding: 16rpx; }
.product-card {
  display: flex; gap: 20rpx; background: #fff; border-radius: 12rpx;
  padding: 20rpx; margin-bottom: 16rpx;
}
.product-img { width: 200rpx; height: 200rpx; border-radius: 8rpx; flex-shrink: 0; }
.product-info { flex: 1; display: flex; flex-direction: column; }
.product-name {
  font-size: 28rpx; color: #333; line-height: 1.4;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  margin-bottom: 8rpx;
}
.price-row { display: flex; align-items: baseline; gap: 12rpx; margin-bottom: 8rpx; }
.seckill-price { font-size: 36rpx; font-weight: 700; color: #FF5000; }
.original-price { font-size: 24rpx; color: #999; text-decoration: line-through; }

.progress-bar {
  position: relative; height: 28rpx; background: #FFE0CC; border-radius: 14rpx;
  overflow: hidden; margin-bottom: 12rpx;
}
.progress-fill {
  height: 100%; background: linear-gradient(90deg, #FF5000, #FF9000);
  border-radius: 14rpx; transition: width .3s;
}
.progress-text {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  font-size: 20rpx; color: #fff; font-weight: 600;
}

.seckill-btn {
  width: 100%; height: 64rpx; line-height: 64rpx; background: #FF5000;
  color: #fff; border: none; border-radius: 32rpx; font-size: 26rpx; font-weight: 600;
}
.seckill-btn.disabled { background: #ccc; }

.empty-products { text-align: center; padding: 80rpx 0; color: #999; }
</style>
