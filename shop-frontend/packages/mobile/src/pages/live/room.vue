<script setup lang="ts">
/**
 * 直播间页面 - 沉浸式全屏布局（抖音风格）- 移动端
 */
defineOptions({ name: 'LiveRoomPage' })

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { liveRoomApi } from '@shop/shared'
import type { LiveRoom, LiveProduct, Gift } from '@shop/shared'

const roomId = ref(0)
const room = ref<LiveRoom | null>(null)
const products = ref<LiveProduct[]>([])
const gifts = ref<Gift[]>([])
const currentProduct = ref<LiveProduct | null>(null)
const coinBalance = ref(0)

// UI 状态
const showGiftPanel = ref(false)
const showProductList = ref(false)
const danmakuInput = ref('')
const onlineCount = ref(0)
const messages = ref<{ type: string; data: Record<string, unknown> }[]>([])

// WebSocket
let ws: UniApp.SocketTask | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let reconnectCount = 0
const maxReconnect = 3

// 拉流地址（HLS）
const hlsUrl = computed(() => {
  if (!room.value) return ''
  return `http://localhost:8085/live/${roomId.value}.m3u8`
})

// 连接 WebSocket
const connectWebSocket = () => {
  const token = uni.getStorageSync('token')
  ws = uni.connectSocket({
    url: `ws://localhost:8080/ws/live/${roomId.value}`,
    header: {
      Authorization: token ? `Bearer ${token}` : '',
    },
  })

  ws.onOpen(() => {
    console.log('[LiveSocket] 连接成功')
    reconnectCount = 0
  })

  ws.onMessage((res) => {
    try {
      const msg = JSON.parse(res.data as string)
      messages.value.push(msg)

      if (msg.type === 'online') {
        onlineCount.value = msg.data.count
      }
    } catch (e) {
      console.error('[LiveSocket] 解析消息失败:', e)
    }
  })

  ws.onClose(() => {
    console.log('[LiveSocket] 连接关闭')
    attemptReconnect()
  })

  ws.onError(() => {
    console.error('[LiveSocket] 连接错误')
  })
}

const attemptReconnect = () => {
  if (reconnectCount >= maxReconnect) return
  reconnectCount++
  reconnectTimer = setTimeout(() => {
    connectWebSocket()
  }, 3000)
}

const disconnectWebSocket = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (ws) {
    ws.close()
    ws = null
  }
}

const sendMessage = (type: string, data: Record<string, unknown>) => {
  if (!ws) return
  ws.send({
    data: JSON.stringify({ type, data, timestamp: Date.now() }),
  })
}

// 发送弹幕
const handleSendDanmaku = () => {
  if (!danmakuInput.value.trim()) return
  sendMessage('danmaku', { content: danmakuInput.value, nickname: '用户' })
  danmakuInput.value = ''
}

// 点赞
const handleLike = () => {
  sendMessage('like', {})
  // 前端动画效果
  uni.showToast({ title: '❤️', icon: 'none', duration: 500 })
}

// 送礼
const handleSendGift = (giftId: number, count: number) => {
  sendMessage('gift', { giftId, count, nickname: '用户' })
  showGiftPanel.value = false
}

// 购买商品
const handleBuy = (product: LiveProduct) => {
  uni.navigateTo({
    url: `/pages/order/confirm?productId=${product.productId}&skuId=${product.skuId}&liveRoomId=${roomId.value}`,
  })
}

// 加载数据
const loadData = async () => {
  try {
    const [roomData, productData, giftData, balance] = await Promise.all([
      liveRoomApi.getRoomDetail(roomId.value),
      liveRoomApi.getRoomProducts(roomId.value),
      liveRoomApi.getGifts(),
      liveRoomApi.getCoinBalance(),
    ])
    room.value = roomData
    products.value = productData
    gifts.value = giftData
    coinBalance.value = balance

    // 找到讲解中的商品
    const explaining = productData.find(p => p.status === 2)
    if (explaining) {
      currentProduct.value = explaining
    }
  } catch (e) {
    console.error('加载数据失败:', e)
  }
}

onLoad((opts?: any) => {
  roomId.value = Number(opts?.id)
  loadData()
  connectWebSocket()
})

onUnload(() => {
  disconnectWebSocket()
})
</script>

<template>
  <view class="live-room">
    <!-- 视频播放器 -->
    <video
      :src="hlsUrl"
      :poster="room?.cover"
      :autoplay="true"
      :muted="true"
      :controls="false"
      object-fit="contain"
      class="live-room__video"
      @error="() => console.log('视频播放错误')"
    />

    <!-- 顶部信息栏 -->
    <view class="live-room__header">
      <view class="live-room__anchor">
        <view class="live-room__avatar">
          <text>{{ room?.anchorNickname?.[0] || '👤' }}</text>
        </view>
        <view class="live-room__info">
          <text class="live-room__name">{{ room?.anchorNickname || '主播' }}</text>
          <text class="live-room__fans">粉丝 {{ room?.totalViewCount || 0 }}</text>
        </view>
      </view>
      <view class="live-room__stats">
        <view class="live-room__live-badge">
          <view class="live-room__live-dot" />
          <text>LIVE</text>
        </view>
        <text class="live-room__viewers">👁 {{ onlineCount }}</text>
      </view>
    </view>

    <!-- 弹幕区域 -->
    <view class="live-room__danmaku">
      <view
        v-for="(msg, index) in messages.filter(m => m.type === 'danmaku').slice(-10)"
        :key="index"
        class="live-room__danmaku-item"
      >
        <text class="live-room__danmaku-user">{{ msg.data.nickname || '用户' }}:</text>
        <text class="live-room__danmaku-text">{{ msg.data.content }}</text>
      </view>
    </view>

    <!-- 商品浮窗 -->
    <view v-if="currentProduct" class="live-room__product-float">
      <view class="live-room__float-header">
        <text class="live-room__float-badge">讲解中</text>
        <text class="live-room__float-close" @click="currentProduct = null">✕</text>
      </view>
      <view class="live-room__float-content">
        <image
          v-if="currentProduct.productImage"
          :src="currentProduct.productImage"
          class="live-room__float-image"
          mode="aspectFill"
        />
        <view class="live-room__float-info">
          <text class="live-room__float-name">{{ currentProduct.productName }}</text>
          <view class="live-room__float-price">
            <text class="live-room__live-price">¥{{ currentProduct.livePrice }}</text>
            <text class="live-room__original-price">¥{{ currentProduct.originalPrice }}</text>
          </view>
        </view>
      </view>
      <view class="live-room__float-actions">
        <view class="live-room__float-btn live-room__float-btn--buy" @click="handleBuy(currentProduct)">
          <text>抢购</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="live-room__footer">
      <input
        v-model="danmakuInput"
        class="live-room__input"
        placeholder="说点什么..."
        @confirm="handleSendDanmaku"
      />
      <view class="live-room__action-btn" @click="showProductList = !showProductList">
        <text>🛒</text>
        <view v-if="products.length" class="live-room__badge">
          <text>{{ products.length }}</text>
        </view>
      </view>
      <view class="live-room__action-btn" @click="handleLike">
        <text>❤️</text>
      </view>
      <view class="live-room__action-btn" @click="showGiftPanel = !showGiftPanel">
        <text>🎁</text>
      </view>
    </view>

    <!-- 礼物面板 -->
    <view v-if="showGiftPanel" class="live-room__gift-panel">
      <view class="live-room__gift-header">
        <text class="live-room__gift-title">🎁 送礼物</text>
        <text class="live-room__gift-balance">💰 余额: {{ coinBalance }} 币</text>
        <text class="live-room__gift-close" @click="showGiftPanel = false">✕</text>
      </view>
      <view class="live-room__gift-grid">
        <view
          v-for="gift in gifts"
          :key="gift.id"
          class="live-room__gift-item"
          @click="handleSendGift(gift.id, 1)"
        >
          <image v-if="gift.icon" :src="gift.icon" class="live-room__gift-icon" mode="aspectFit" />
          <text v-else class="live-room__gift-icon-text">🎁</text>
          <text class="live-room__gift-name">{{ gift.name }}</text>
          <text class="live-room__gift-price">{{ gift.price }} 币</text>
        </view>
      </view>
    </view>

    <!-- 商品列表面板 -->
    <view v-if="showProductList" class="live-room__product-list">
      <view class="live-room__product-header">
        <text class="live-room__product-title">直播商品 ({{ products.length }})</text>
        <text class="live-room__product-close" @click="showProductList = false">收起 ▼</text>
      </view>
      <scroll-view scroll-y class="live-room__product-scroll">
        <view
          v-for="product in products"
          :key="product.id"
          class="live-room__product-item"
          :class="{ 'is-explaining': product.status === 2 }"
          @click="handleBuy(product)"
        >
          <image
            v-if="product.productImage"
            :src="product.productImage"
            class="live-room__product-image"
            mode="aspectFill"
          />
          <view class="live-room__product-info">
            <view v-if="product.status === 2" class="live-room__product-badge">
              <text>讲解中</text>
            </view>
            <text class="live-room__product-name">{{ product.productName }}</text>
            <text class="live-room__product-price">¥{{ product.livePrice }}</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.live-room {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #000;
  overflow: hidden;

  &__video {
    width: 100%;
    height: 100%;
  }

  &__header {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    padding: 20rpx 24rpx;
    padding-top: calc(20rpx + env(safe-area-inset-top));
    display: flex;
    align-items: center;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent);
    z-index: 5;
  }

  &__anchor {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  &__avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #e74c3c, #f39c12);
    display: flex;
    align-items: center;
    justify-content: center;

    text {
      font-size: 28rpx;
      color: #fff;
      font-weight: 600;
    }
  }

  &__info {
    display: flex;
    flex-direction: column;
  }

  &__name {
    font-size: 28rpx;
    font-weight: 600;
    color: #fff;
  }

  &__fans {
    font-size: 20rpx;
    color: #aaa;
  }

  &__stats {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  &__live-badge {
    display: flex;
    align-items: center;
    gap: 6rpx;
    background: #e74c3c;
    padding: 6rpx 14rpx;
    border-radius: 8rpx;

    text {
      font-size: 20rpx;
      font-weight: 600;
      color: #fff;
    }
  }

  &__live-dot {
    width: 10rpx;
    height: 10rpx;
    background: #fff;
    border-radius: 50%;
    animation: pulse 1.5s infinite;
  }

  &__viewers {
    font-size: 22rpx;
    color: #fff;
    background: rgba(0, 0, 0, 0.4);
    padding: 6rpx 14rpx;
    border-radius: 16rpx;
  }

  &__danmaku {
    position: absolute;
    bottom: 160rpx;
    left: 24rpx;
    right: 24rpx;
    z-index: 5;
  }

  &__danmaku-item {
    margin-bottom: 8rpx;
    background: rgba(0, 0, 0, 0.3);
    padding: 6rpx 16rpx;
    border-radius: 20rpx;
    display: inline-block;
  }

  &__danmaku-user {
    font-size: 22rpx;
    color: #4fc3f7;
  }

  &__danmaku-text {
    font-size: 22rpx;
    color: #fff;
    margin-left: 8rpx;
  }

  &__product-float {
    position: absolute;
    right: 24rpx;
    bottom: 200rpx;
    width: 360rpx;
    background: rgba(0, 0, 0, 0.75);
    backdrop-filter: blur(10px);
    border-radius: 20rpx;
    overflow: hidden;
    border: 2rpx solid rgba(255, 167, 38, 0.3);
    z-index: 10;
  }

  &__float-header {
    display: flex;
    align-items: center;
    padding: 10rpx 16rpx;
    background: rgba(255, 167, 38, 0.1);
  }

  &__float-badge {
    font-size: 20rpx;
    color: #ffa726;
    font-weight: 600;
  }

  &__float-close {
    margin-left: auto;
    font-size: 22rpx;
    color: #888;
  }

  &__float-content {
    display: flex;
    gap: 16rpx;
    padding: 16rpx;
  }

  &__float-image {
    width: 100rpx;
    height: 100rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }

  &__float-info {
    flex: 1;
    min-width: 0;
  }

  &__float-name {
    font-size: 24rpx;
    color: #fff;
    font-weight: 500;
    margin-bottom: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__float-price {
    display: flex;
    align-items: baseline;
    gap: 8rpx;
  }

  &__live-price {
    font-size: 32rpx;
    color: #ef5350;
    font-weight: 700;
  }

  &__original-price {
    font-size: 20rpx;
    color: #888;
    text-decoration: line-through;
  }

  &__float-actions {
    padding: 0 16rpx 16rpx;
  }

  &__float-btn {
    width: 100%;
    padding: 14rpx 0;
    border-radius: 28rpx;
    text-align: center;

    &--buy {
      background: linear-gradient(135deg, #ef5350, #ffa726);

      text {
        font-size: 26rpx;
        color: #fff;
        font-weight: 600;
      }
    }
  }

  &__footer {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 20rpx 24rpx;
    padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
    display: flex;
    align-items: center;
    gap: 16rpx;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
    z-index: 5;
  }

  &__input {
    flex: 1;
    background: rgba(255, 255, 255, 0.15);
    border: none;
    border-radius: 32rpx;
    padding: 16rpx 24rpx;
    font-size: 26rpx;
    color: #fff;
  }

  &__action-btn {
    position: relative;
    width: 64rpx;
    height: 64rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    text {
      font-size: 28rpx;
    }
  }

  &__badge {
    position: absolute;
    top: -6rpx;
    right: -6rpx;
    background: #e74c3c;
    padding: 2rpx 8rpx;
    border-radius: 12rpx;
    min-width: 24rpx;
    text-align: center;

    text {
      font-size: 18rpx;
      color: #fff;
    }
  }

  &__gift-panel {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.85);
    backdrop-filter: blur(10px);
    border-radius: 32rpx 32rpx 0 0;
    padding: 24rpx;
    z-index: 20;
  }

  &__gift-header {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
  }

  &__gift-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #fff;
  }

  &__gift-balance {
    margin-left: auto;
    font-size: 22rpx;
    color: #ffa726;
  }

  &__gift-close {
    margin-left: 20rpx;
    font-size: 28rpx;
    color: #888;
  }

  &__gift-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
  }

  &__gift-item {
    width: 25%;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16rpx;
    border-radius: 16rpx;
  }

  &__gift-icon {
    width: 80rpx;
    height: 80rpx;
    margin-bottom: 8rpx;
  }

  &__gift-icon-text {
    font-size: 48rpx;
    margin-bottom: 8rpx;
  }

  &__gift-name {
    font-size: 22rpx;
    color: #ccc;
    margin-bottom: 4rpx;
  }

  &__gift-price {
    font-size: 20rpx;
    color: #ffa726;
  }

  &__product-list {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.85);
    backdrop-filter: blur(10px);
    border-radius: 32rpx 32rpx 0 0;
    padding: 24rpx;
    z-index: 15;
    max-height: 60vh;
  }

  &__product-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }

  &__product-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #fff;
  }

  &__product-close {
    font-size: 22rpx;
    color: #888;
  }

  &__product-scroll {
    max-height: 50vh;
  }

  &__product-item {
    display: flex;
    gap: 20rpx;
    padding: 16rpx;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 16rpx;
    margin-bottom: 16rpx;

    &.is-explaining {
      border-left: 4rpx solid #ffa726;
      background: rgba(255, 167, 38, 0.1);
    }
  }

  &__product-image {
    width: 100rpx;
    height: 100rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }

  &__product-info {
    flex: 1;
    min-width: 0;
  }

  &__product-badge {
    display: inline-block;
    margin-bottom: 6rpx;

    text {
      font-size: 18rpx;
      color: #ffa726;
      background: rgba(255, 167, 38, 0.15);
      padding: 4rpx 10rpx;
      border-radius: 6rpx;
    }
  }

  &__product-name {
    font-size: 26rpx;
    color: #fff;
    font-weight: 500;
    margin-bottom: 6rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__product-price {
    font-size: 30rpx;
    color: #ef5350;
    font-weight: 700;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
