<script setup lang="ts">
/**
 * 直播间页面 - 沉浸式全屏布局（抖音风格）
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { EyeOutline, CartOutline, HeartOutline, GiftOutline, ChevronDownOutline, CubeOutline } from '@vicons/ionicons5'
import { liveRoomApi } from '@shop/shared'
import type { LiveRoom, LiveProduct } from '@shop/shared'
import { useUserStore } from '../../stores'
import LivePlayer from '../../components/LivePlayer.vue'
import LiveDanmaku from '../../components/LiveDanmaku.vue'
import LiveGiftPanel from '../../components/LiveGiftPanel.vue'
import LiveProductFloat from '../../components/LiveProductFloat.vue'
import { useLiveSocket } from '../../composables/useLiveSocket'

defineOptions({ name: 'LiveRoom' })

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const roomId = Number(route.params.id)
const room = ref<LiveRoom | null>(null)
const products = ref<LiveProduct[]>([])
const currentProduct = ref<LiveProduct | null>(null)

// UI 状态
const showGiftPanel = ref(false)
const showProductList = ref(false)
const danmakuInput = ref('')

// WebSocket 连接
const {
  isConnected,
  onlineCount,
  messages,
  sendDanmaku,
  sendLike,
  sendGift,
} = useLiveSocket(roomId)

// 拉流地址
const flvUrl = computed(() => {
  if (!room.value) return ''
  return `http://localhost:8085/live/${roomId}.flv`
})

const hlsUrl = computed(() => {
  if (!room.value) return ''
  return `http://localhost:8085/live/${roomId}.m3u8`
})

// 加载直播间信息
const loadRoom = async () => {
  try {
    room.value = await liveRoomApi.getRoomDetail(roomId)
  } catch (e) {
    console.error('加载直播间失败:', e)
  }
}

// 加载直播间商品
const loadProducts = async () => {
  try {
    products.value = await liveRoomApi.getRoomProducts(roomId)
    // 找到讲解中的商品
    const explaining = products.value.find(p => p.status === 2)
    if (explaining) {
      currentProduct.value = explaining
    }
  } catch (e) {
    console.error('加载商品失败:', e)
  }
}

// 监听 WebSocket 消息
const handleMessages = () => {
  // 监听商品讲解推送
  const productMsg = messages.value.find(m => m.type === 'product')
  if (productMsg) {
    const data = productMsg.data as { productId: number; action: string }
    if (data.action === 'highlight') {
      currentProduct.value = products.value.find(p => p.productId === data.productId) || null
    } else if (data.action === 'unhighlight') {
      currentProduct.value = null
    }
  }
}

// 发送弹幕
const handleSendDanmaku = () => {
  if (!danmakuInput.value.trim()) return
  sendDanmaku(danmakuInput.value, userStore.userInfo?.nickname || '用户')
  danmakuInput.value = ''
}

// 点赞
const handleLike = () => {
  sendLike()
}

// 送礼
const handleSendGift = (giftId: number, count: number) => {
  sendGift(giftId, count, userStore.userInfo?.nickname || '用户')
  showGiftPanel.value = false
}

// 购买商品
const handleBuy = (product: LiveProduct) => {
  router.push(`/order/confirm?productId=${product.productId}&skuId=${product.skuId}&liveRoomId=${roomId}`)
}

// 加入购物车
const handleAddCart = (product: LiveProduct) => {
  // TODO: 调用购物车 API
  console.log('加入购物车:', product)
}

onMounted(() => {
  loadRoom()
  loadProducts()
})

onUnmounted(() => {
  // 清理
})
</script>

<template>
  <div class="live-room">
    <!-- 视频播放器 -->
    <div class="live-room__video">
      <LivePlayer
        v-if="room"
        :room-id="roomId"
        :flv-url="flvUrl"
        :hls-url="hlsUrl"
      />

      <!-- 弹幕层 -->
      <LiveDanmaku :messages="messages as any" />

      <!-- 顶部信息栏 -->
      <div class="live-room__header">
        <div class="live-room__anchor">
          <div class="live-room__avatar">
            {{ room?.anchorNickname?.[0] || '?' }}
          </div>
          <div class="live-room__info">
            <div class="live-room__name">{{ room?.anchorNickname || '主播' }}</div>
            <div class="live-room__fans">粉丝 {{ room?.totalViewCount || 0 }}</div>
          </div>
        </div>
        <span class="live-room__viewers">
          <n-icon :size="14" color="#fff"><EyeOutline /></n-icon>
          {{ onlineCount }}
        </span>
      </div>

      <!-- 商品浮窗 -->
      <LiveProductFloat
        :product="currentProduct"
        :visible="!!currentProduct"
        @close="currentProduct = null"
        @buy="handleBuy"
        @cart="handleAddCart"
      />

      <!-- 底部操作栏 -->
      <div class="live-room__footer">
        <input
          v-model="danmakuInput"
          class="live-room__input"
          placeholder="说点什么..."
          @keyup.enter="handleSendDanmaku"
        />
        <button class="live-room__action-btn" @click="showProductList = !showProductList">
          <n-icon :size="20" color="#fff"><CartOutline /></n-icon>
          <span v-if="products.length" class="live-room__badge">{{ products.length }}</span>
        </button>
        <button class="live-room__action-btn" @click="handleLike">
          <n-icon :size="20" color="#e74c3c"><HeartOutline /></n-icon>
        </button>
        <button class="live-room__action-btn" @click="showGiftPanel = !showGiftPanel">
          <n-icon :size="20" color="#ffa726"><GiftOutline /></n-icon>
        </button>
      </div>
    </div>

    <!-- 礼物面板 -->
    <Transition name="slide-up">
      <div v-if="showGiftPanel" class="live-room__gift-panel">
        <LiveGiftPanel
          @send="handleSendGift"
          @close="showGiftPanel = false"
        />
      </div>
    </Transition>

    <!-- 商品列表面板 -->
    <Transition name="slide-up">
      <div v-if="showProductList" class="live-room__product-list">
        <div class="live-room__product-header">
          <span>直播商品 ({{ products.length }})</span>
          <button @click="showProductList = false">
            收起
            <n-icon :size="14" color="#888"><ChevronDownOutline /></n-icon>
          </button>
        </div>
        <div class="live-room__product-items">
          <div
            v-for="product in products"
            :key="product.id"
            class="live-room__product-item"
            :class="{ 'is-explaining': product.status === 2 }"
            @click="handleBuy(product)"
          >
            <div class="live-room__product-image">
              <img v-if="product.productImage" :src="product.productImage" :alt="product.productName" />
              <n-icon v-else :size="24" color="#ffa726"><CubeOutline /></n-icon>
            </div>
            <div class="live-room__product-info">
              <div v-if="product.status === 2" class="live-room__product-badge">讲解中</div>
              <div class="live-room__product-name">{{ product.productName }}</div>
              <div class="live-room__product-price">¥{{ product.livePrice }}</div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped lang="scss">
.live-room {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #000;
  overflow: hidden;

  &__video {
    position: relative;
    width: 100%;
    height: 100%;
  }

  &__header {
    position: absolute;
    top: 0;
    left: 0;
    right: 80px; // 避开播放器 LIVE 标记区域
    padding: 16px;
    display: flex;
    align-items: center;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent);
    z-index: 5;
  }

  &__anchor {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #e74c3c, #f39c12);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: #fff;
    font-weight: 600;
  }

  &__info {
    display: flex;
    flex-direction: column;
  }

  &__name {
    font-size: 15px;
    font-weight: 600;
    color: #fff;
  }

  &__fans {
    font-size: 11px;
    color: #aaa;
  }

  &__stats {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__live-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    background: #e74c3c;
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;
    color: #fff;
  }

  &__live-dot {
    width: 6px;
    height: 6px;
    background: #fff;
    border-radius: 50%;
    animation: pulse 1.5s infinite;
  }

  &__viewers {
    font-size: 12px;
    color: #fff;
    background: rgba(0, 0, 0, 0.4);
    padding: 3px 8px;
    border-radius: 10px;
  }

  &__footer {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 80px; // 避开播放器音量/全屏按钮区域
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 10px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
    z-index: 5;
  }

  &__input {
    flex: 1;
    background: rgba(255, 255, 255, 0.15);
    border: none;
    border-radius: 20px;
    padding: 10px 16px;
    font-size: 14px;
    color: #fff;
    outline: none;

    &::placeholder {
      color: #aaa;
    }
  }

  &__action-btn {
    position: relative;
    width: 40px;
    height: 40px;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    border-radius: 50%;
    font-size: 18px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }

  &__badge {
    position: absolute;
    top: -4px;
    right: -4px;
    background: #e74c3c;
    color: #fff;
    font-size: 10px;
    padding: 1px 5px;
    border-radius: 8px;
    min-width: 16px;
    text-align: center;
  }

  &__gift-panel {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 20;
  }

  &__product-list {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.85);
    backdrop-filter: blur(10px);
    border-radius: 16px 16px 0 0;
    padding: 16px;
    z-index: 15;
    max-height: 60vh;
    overflow-y: auto;
  }

  &__product-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    color: #fff;
    font-size: 14px;
    font-weight: 600;

    button {
      background: none;
      border: none;
      color: #888;
      font-size: 12px;
      cursor: pointer;
    }
  }

  &__product-items {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__product-item {
    display: flex;
    gap: 12px;
    padding: 10px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    &.is-explaining {
      border-left: 3px solid #ffa726;
      background: rgba(255, 167, 38, 0.1);
    }
  }

  &__product-image {
    width: 60px;
    height: 60px;
    background: #2d2d4e;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 8px;
    }
  }

  &__product-info {
    flex: 1;
    min-width: 0;
  }

  &__product-badge {
    display: inline-block;
    font-size: 10px;
    color: #ffa726;
    background: rgba(255, 167, 38, 0.15);
    padding: 2px 6px;
    border-radius: 4px;
    margin-bottom: 4px;
  }

  &__product-name {
    font-size: 14px;
    color: #fff;
    font-weight: 500;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__product-price {
    font-size: 16px;
    color: #ef5350;
    font-weight: 700;
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
