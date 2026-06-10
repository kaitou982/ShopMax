<script setup lang="ts">
/**
 * LiveProductFloat 商品浮窗组件
 * 讲解中商品自动弹出浮窗
 */
import { ref, computed } from 'vue'
import type { LiveProduct } from '@shop/shared'

defineOptions({ name: 'LiveProductFloat' })

interface Props {
  product: LiveProduct | null
  visible: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'buy', product: LiveProduct): void
  (e: 'cart', product: LiveProduct): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const discount = computed(() => {
  if (!props.product) return 0
  return Math.round((1 - props.product.livePrice / props.product.originalPrice) * 100)
})
</script>

<template>
  <Transition name="float">
    <div v-if="visible && product" class="live-product-float">
      <div class="live-product-float__header">
        <span class="live-product-float__badge">讲解中</span>
        <button class="live-product-float__close" @click="emit('close')">✕</button>
      </div>

      <div class="live-product-float__content">
        <div class="live-product-float__image">
          <img v-if="product.productImage" :src="product.productImage" :alt="product.productName" />
          <span v-else>📦</span>
        </div>
        <div class="live-product-float__info">
          <div class="live-product-float__name">{{ product.productName }}</div>
          <div class="live-product-float__price">
            <span class="live-product-float__live-price">¥{{ product.livePrice }}</span>
            <span class="live-product-float__original-price">¥{{ product.originalPrice }}</span>
            <span class="live-product-float__discount">省{{ discount }}%</span>
          </div>
        </div>
      </div>

      <div class="live-product-float__actions">
        <button class="live-product-float__btn live-product-float__btn--cart" @click="emit('cart', product)">
          加入购物车
        </button>
        <button class="live-product-float__btn live-product-float__btn--buy" @click="emit('buy', product)">
          抢购
        </button>
      </div>
    </div>
  </Transition>
</template>

<style scoped lang="scss">
.live-product-float {
  position: absolute;
  right: 12px;
  bottom: 80px;
  width: 220px;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255, 167, 38, 0.3);
  z-index: 10;

  &__header {
    display: flex;
    align-items: center;
    padding: 6px 10px;
    background: rgba(255, 167, 38, 0.1);
  }

  &__badge {
    font-size: 11px;
    color: #ffa726;
    font-weight: 600;
  }

  &__close {
    margin-left: auto;
    background: none;
    border: none;
    color: #888;
    font-size: 12px;
    cursor: pointer;

    &:hover {
      color: #fff;
    }
  }

  &__content {
    display: flex;
    gap: 10px;
    padding: 10px;
  }

  &__image {
    width: 60px;
    height: 60px;
    background: #2d2d4e;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 8px;
    }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: 13px;
    color: #fff;
    font-weight: 500;
    line-height: 1.3;
    margin-bottom: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__price {
    display: flex;
    align-items: baseline;
    gap: 6px;
    flex-wrap: wrap;
  }

  &__live-price {
    font-size: 18px;
    color: #ef5350;
    font-weight: 700;
  }

  &__original-price {
    font-size: 11px;
    color: #888;
    text-decoration: line-through;
  }

  &__discount {
    font-size: 10px;
    color: #ffa726;
    background: rgba(255, 167, 38, 0.15);
    padding: 1px 4px;
    border-radius: 2px;
  }

  &__actions {
    display: flex;
    gap: 6px;
    padding: 0 10px 10px;
  }

  &__btn {
    flex: 1;
    border: none;
    padding: 8px 0;
    border-radius: 16px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.9;
    }

    &--cart {
      background: rgba(255, 255, 255, 0.15);
      color: #fff;
    }

    &--buy {
      background: linear-gradient(135deg, #ef5350, #ffa726);
      color: #fff;
      font-weight: 600;
    }
  }
}

.float-enter-active,
.float-leave-active {
  transition: all 0.3s ease;
}

.float-enter-from,
.float-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
