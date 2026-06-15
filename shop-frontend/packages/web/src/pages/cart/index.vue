<script setup lang="ts">defineOptions({ name: 'CartPage' })
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon, useMessage, useDialog } from 'naive-ui'
import { CartOutline, CloseOutline } from '@vicons/ionicons5'
import { useCartStore } from '@/stores'

const router = useRouter()
const cartStore = useCartStore()
const message = useMessage()
const dialog = useDialog()

const allSelected = computed({
  get: () => cartStore.cartList.length > 0 && cartStore.cartList.every(i => i.selected),
  set: (v: boolean) => cartStore.cartList.forEach(i => { i.selected = v }),
})

const clearCart = () => {
  dialog.warning({
    title: '清空购物车',
    content: '确定要清空购物车中的所有商品吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      cartStore.cartList = []
      message.success('购物车已清空')
    }
  })
}

const goProduct = (id: number) => router.push(`/product/${id}`)
const goHome = () => router.push('/')
</script>

<template>
  <div class="cart-page">
    <h2>购物车 <span class="count" v-if="cartStore.totalCount">({{ cartStore.totalCount }})</span></h2>

    <div v-if="!cartStore.cartList.length" class="empty">
      <n-icon :size="60" color="#ccc" class="empty-icon"><CartOutline /></n-icon>
      <p>购物车空空如也</p>
      <button class="empty-btn" @click="goHome">去逛逛</button>
    </div>

    <template v-else>
      <!-- 全选 -->
      <div class="select-all">
        <label class="cb-label">
          <input type="checkbox" v-model="allSelected" /> 全选
        </label>
        <button class="batch-del" @click="clearCart">清空</button>
      </div>

      <!-- 商品列表 -->
      <div class="cart-items">
        <div class="cart-item" v-for="item in cartStore.cartList" :key="item.id">
          <input type="checkbox" :checked="item.selected" @change="cartStore.toggleSelected(item.id)" />
          <img :src="item.image || '/api/v1/files/default/product'" @click="goProduct(item.productId)" class="item-img" />
          <div class="item-info" @click="goProduct(item.productId)">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-price">¥{{ item.price }}</div>
          </div>
          <div class="qty-stepper">
            <button @click="cartStore.updateQuantity(item.id, item.quantity - 1)">−</button>
            <span>{{ item.quantity }}</span>
            <button @click="cartStore.updateQuantity(item.id, item.quantity + 1)">+</button>
          </div>
          <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          <n-icon :size="18" class="item-remove" @click="cartStore.removeFromCart(item.id)"><CloseOutline /></n-icon>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-bar">
        <div class="bar-left">
          <label><input type="checkbox" v-model="allSelected" /> 全选</label>
        </div>
        <div class="bar-right">
          <span class="total-label">合计:</span>
          <span class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
          <button class="checkout-btn" :disabled="cartStore.selectedCount === 0" @click="router.push('/order/confirm')">
            去结算{{ cartStore.selectedCount ? ` (${cartStore.selectedCount})` : '' }}
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.cart-page h2 { margin-bottom: 20px; .count { font-size: 14px; color: #999; font-weight: 400; } }

.empty { text-align: center; padding: 100px 0; }
.empty-icon { font-size: 60px; margin-bottom: 16px; }
.empty-btn { padding: 10px 32px; background: $brand-orange; color: #fff; border: none; border-radius: 20px; cursor: pointer; margin-top: 16px; font-size: 14px; }

.select-all { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid $border-light; margin-bottom: 8px; font-size: 13px; }
.cb-label { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.batch-del { color: $text-hint; background: none; border: none; cursor: pointer; font-size: 13px; &:hover { color: $color-danger; } }

.cart-items { margin-bottom: 100px; }
.cart-item { display: flex; align-items: center; gap: 16px; padding: 16px; background: #fff; border-radius: 12px; margin-bottom: 8px;
  input[type=checkbox] { width: 18px; height: 18px; accent-color: $brand-orange; cursor: pointer; flex-shrink: 0; }
}
.item-img { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; background: $bg-input; cursor: pointer; flex-shrink: 0; }
.item-info { flex: 1; min-width: 0; cursor: pointer; }
.item-name { font-size: 14px; color: $text-primary; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 8px; }
.item-price { font-size: 14px; font-weight: 600; color: $brand-orange; }
.qty-stepper { display: flex; align-items: center; border: 1px solid $border-color; border-radius: 6px; overflow: hidden; flex-shrink: 0;
  button { width: 28px; height: 28px; border: none; background: $bg-input; cursor: pointer; font-size: 14px; }
  span { width: 36px; text-align: center; font-size: 13px; font-weight: 600; }
}
.item-subtotal { font-size: 14px; font-weight: 600; color: $text-primary; width: 70px; text-align: right; flex-shrink: 0; }
.item-remove { background: none; border: none; color: $text-hint; cursor: pointer; font-size: 16px; flex-shrink: 0; &:hover { color: $color-danger; } }

.cart-bar { position: fixed; bottom: 0; left: $category-width; right: 0; height: 64px; background: #fff; border-top: 1px solid $border-color; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; z-index: 50; }
.bar-left { font-size: 13px; label { display: flex; align-items: center; gap: 6px; cursor: pointer; } input { accent-color: $brand-orange; } }
.bar-right { display: flex; align-items: center; gap: 16px; }
.total-label { font-size: 14px; color: $text-secondary; }
.total-price { font-size: 22px; font-weight: 700; color: $brand-orange; }
.checkout-btn { padding: 10px 32px; background: $brand-gradient; color: #fff; border: none; border-radius: 20px; font-size: 15px; font-weight: 600; cursor: pointer; &:disabled { opacity: 0.4; cursor: not-allowed; } }
</style>
