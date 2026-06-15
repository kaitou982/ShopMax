<script setup lang="ts">
/**
 * LiveGiftPanel 礼物面板组件
 * 抖音风格礼物选择面板
 */
import { ref, onMounted } from 'vue'
import { NIcon } from 'naive-ui'
import { GiftOutline, WalletOutline, CloseOutline } from '@vicons/ionicons5'
import { liveRoomApi } from '@shop/shared'
import type { Gift } from '@shop/shared'

defineOptions({ name: 'LiveGiftPanel' })

interface Emits {
  (e: 'send', giftId: number, count: number): void
  (e: 'close'): void
}

const emit = defineEmits<Emits>()

const gifts = ref<Gift[]>([])
const selectedGift = ref<Gift | null>(null)
const selectedCount = ref(1)
const coinBalance = ref(0)
const countOptions = [1, 10, 66, 99]

const loadGifts = async () => {
  try {
    gifts.value = await liveRoomApi.getGifts()
    if (gifts.value.length > 0) {
      selectedGift.value = gifts.value[0]
    }
  } catch (e) {
    console.error('加载礼物列表失败:', e)
  }
}

const loadBalance = async () => {
  try {
    coinBalance.value = await liveRoomApi.getCoinBalance()
  } catch (e) {
    console.error('加载余额失败:', e)
  }
}

const selectGift = (gift: Gift) => {
  selectedGift.value = gift
}

const selectCount = (count: number) => {
  selectedCount.value = count
}

const handleSend = () => {
  if (!selectedGift.value) return
  emit('send', selectedGift.value.id, selectedCount.value)
}

const totalCost = () => {
  if (!selectedGift.value) return 0
  return selectedGift.value.price * selectedCount.value
}

onMounted(() => {
  loadGifts()
  loadBalance()
})
</script>

<template>
  <div class="live-gift-panel">
    <div class="live-gift-panel__header">
      <span class="live-gift-panel__title">
        <n-icon :size="18" color="#ffa726"><GiftOutline /></n-icon>
        送礼物
      </span>
      <span class="live-gift-panel__balance">
        <n-icon :size="14" color="#ffa726"><WalletOutline /></n-icon>
        余额: {{ coinBalance }} 币
      </span>
      <n-icon :size="18" color="#888" class="live-gift-panel__close" @click="emit('close')"><CloseOutline /></n-icon>
    </div>

    <div class="live-gift-panel__grid">
      <div
        v-for="gift in gifts"
        :key="gift.id"
        class="live-gift-panel__item"
        :class="{ 'is-selected': selectedGift?.id === gift.id }"
        @click="selectGift(gift)"
      >
        <div class="live-gift-panel__icon">
          <img v-if="gift.icon" :src="gift.icon" :alt="gift.name" />
          <n-icon v-else :size="28" color="#ffa726"><GiftOutline /></n-icon>
        </div>
        <div class="live-gift-panel__name">{{ gift.name }}</div>
        <div class="live-gift-panel__price">{{ gift.price }} 币</div>
      </div>
    </div>

    <div class="live-gift-panel__footer">
      <div class="live-gift-panel__counts">
        <span
          v-for="count in countOptions"
          :key="count"
          class="live-gift-panel__count"
          :class="{ 'is-selected': selectedCount === count }"
          @click="selectCount(count)"
        >
          x{{ count }}
        </span>
      </div>
      <button
        class="live-gift-panel__send"
        :disabled="!selectedGift || totalCost() > coinBalance"
        @click="handleSend"
      >
        赠送 ({{ totalCost() }} 币)
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.live-gift-panel {
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 16px 16px 0 0;
  padding: 16px;
  color: #fff;

  &__header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
  }

  &__balance {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-left: auto;
    font-size: 13px;
    color: #ffa726;
  }

  &__close {
    margin-left: 12px;
    background: none;
    border: none;
    color: #888;
    font-size: 16px;
    cursor: pointer;

    &:hover {
      color: #fff;
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px;
    border-radius: 12px;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    &.is-selected {
      border-color: #ffa726;
      background: rgba(255, 167, 38, 0.1);
    }
  }

  &__icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-bottom: 6px;

    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }

  &__name {
    font-size: 12px;
    color: #ccc;
    margin-bottom: 2px;
  }

  &__price {
    font-size: 11px;
    color: #ffa726;
  }

  &__footer {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__counts {
    display: flex;
    gap: 8px;
  }

  &__count {
    padding: 4px 12px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    font-size: 12px;
    color: #aaa;
    cursor: pointer;
    transition: all 0.2s;

    &:hover,
    &.is-selected {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
    }
  }

  &__send {
    margin-left: auto;
    background: linear-gradient(135deg, #ff6b6b, #ffa726);
    border: none;
    color: #fff;
    padding: 8px 24px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.9;
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}
</style>
