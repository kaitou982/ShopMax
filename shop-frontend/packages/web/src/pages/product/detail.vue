<script setup lang="ts">
defineOptions({ name: 'ProductDetail' })

import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, type ProductDetail } from '@shop/shared'
import { useCartStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const product = ref<ProductDetail | null>(null)
const loading = ref(true)
const showSku = ref(false)
const quantity = ref(1)
const actionType = ref<'cart' | 'buy'>('cart')
const activeImg = ref(0)
const activeTab = ref<'desc' | 'spec'>('desc')

const imageList = computed(() => {
  if (!product.value) return []
  const subs = product.value.subImages ? product.value.subImages.split(',').filter(Boolean) : []
  return [product.value.mainImage, ...subs]
})

onMounted(async () => {
  try {
    product.value = await productApi.getDetail(Number(route.params.id))
  } catch { /* noop */ } finally { loading.value = false }
})

const openSku = (action: 'cart' | 'buy') => {
  actionType.value = action; showSku.value = true; quantity.value = 1
}
const confirmSku = () => {
  if (!product.value) return
  showSku.value = false
  if (actionType.value === 'cart') {
    cartStore.addToCart({
      productId: product.value.id, name: product.value.name,
      image: product.value.mainImage, price: product.value.salePrice,
      quantity: quantity.value,
    })
  } else {
    cartStore.addToCart({
      productId: product.value.id, name: product.value.name,
      image: product.value.mainImage, price: product.value.salePrice,
      quantity: quantity.value,
    })
    router.push('/cart')
  }
}

// 加购飞入动画
const flyAnim = ref(false)
const onAddCart = () => {
  openSku('cart')
  flyAnim.value = true
  setTimeout(() => flyAnim.value = false, 600)
}
</script>

<template>
  <div class="product-detail-page">
    <!-- 面包屑 -->
    <div class="breadcrumb">
      <router-link to="/">首页</router-link> /
      <router-link :to="`/category/${product?.categoryId}`">分类</router-link> /
      <span>{{ product?.name }}</span>
    </div>

    <div v-if="loading" class="state">加载中...</div>
    <div v-else-if="!product" class="state">商品不存在或已下架</div>

    <template v-else>
      <!-- 图 + 信息 -->
      <div class="detail-top">
        <div class="gallery">
          <img :src="imageList[activeImg]" class="main-img" />
          <div class="thumb-list" v-if="imageList.length > 1">
            <img v-for="(img, i) in imageList" :key="i" :src="img"
              class="thumb" :class="{ active: i === activeImg }" @mouseenter="activeImg = i" />
          </div>
        </div>

        <div class="info">
          <h1>{{ product.name }}</h1>
          <p class="subtitle">{{ product.subtitle }}</p>

          <div class="price-block">
            <span class="price">¥{{ product.salePrice }}</span>
            <span class="original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
            <span class="discount" v-if="product.originalPrice">
              {{ Math.round((1 - product.salePrice / product.originalPrice) * 100) }}% OFF
            </span>
          </div>

          <div class="meta">
            <span>已售 {{ product.sales || 0 }}</span>
            <span>库存 {{ product.stock || 0 }} 件</span>
          </div>

          <div class="actions">
            <button class="btn-cart" @click="onAddCart">加入购物车</button>
            <button class="btn-buy" @click="openSku('buy')">立即购买</button>
          </div>
        </div>
      </div>

      <!-- 详情 Tab -->
      <div class="detail-bottom">
        <div class="tabs">
          <span :class="{ active: activeTab === 'desc' }" @click="activeTab = 'desc'">商品详情</span>
          <span :class="{ active: activeTab === 'spec' }" @click="activeTab = 'spec'">规格参数</span>
        </div>
        <div class="tab-content" v-if="activeTab === 'desc'" v-html="product.detail || product.description || '暂无详情'"></div>
        <div class="tab-content" v-else>
          <table class="spec-table">
            <tr><td>品牌</td><td>{{ (product as any).brandName || '-' }}</td></tr>
            <tr><td>库存</td><td>{{ product.stock }} 件</td></tr>
            <tr><td>销量</td><td>{{ product.sales || 0 }}</td></tr>
          </table>
        </div>
      </div>
    </template>

    <!-- SKU 弹窗 -->
    <div class="sku-overlay" v-if="showSku" @click="showSku = false" />
    <div class="sku-popup" v-if="showSku">
      <div class="sku-header">
        <img :src="product?.mainImage" class="sku-img" />
        <div><span class="sku-price">¥{{ product?.salePrice }}</span><span class="sku-stock">库存 {{ product?.stock }} 件</span></div>
      </div>
      <div class="sku-row"><span>数量</span>
        <div class="qty-stepper">
          <button @click="quantity > 1 && quantity--" :disabled="quantity <= 1">−</button>
          <span>{{ quantity }}</span>
          <button @click="quantity < (product?.stock || 1) && quantity++" :disabled="quantity >= (product?.stock || 1)">+</button>
        </div>
      </div>
      <button class="sku-confirm" @click="confirmSku">{{ actionType === 'cart' ? '加入购物车' : '立即购买' }}</button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.product-detail-page { max-width: 1200px; margin: 0 auto; }

.breadcrumb { font-size: $font-size-xs; color: $text-hint; margin-bottom: $spacing-xl;
  a { color: $text-secondary; &:hover { color: $brand-orange; } }
  span { color: $text-primary; }
}

.state { text-align: center; padding: 120px 0; color: $text-hint; font-size: $font-size-base; }

.detail-top { display: flex; gap: 40px; margin-bottom: 48px;
  @media (max-width: 767px) { flex-direction: column; gap: 20px; }
}

.gallery { flex: 1; min-width: 0; }
.main-img { width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: $radius-md; background: $bg-input; }
.thumb-list { display: flex; gap: 8px; margin-top: 12px; }
.thumb { width: 60px; height: 60px; border-radius: $radius-sm; object-fit: cover; border: 2px solid transparent; cursor: pointer; opacity: 0.6; transition: all $transition-fast;
  &.active, &:hover { border-color: $brand-orange; opacity: 1; }
}

.info { flex: 1;
  h1 { font-size: 22px; font-weight: 700; margin-bottom: 8px; }
  .subtitle { font-size: $font-size-sm; color: $text-hint; margin-bottom: 20px; }
}

.price-block { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; background: $brand-light; padding: 16px; border-radius: $radius-md; }
.price { font-size: 32px; font-weight: 800; color: $brand-orange; }
.original { font-size: $font-size-sm; color: $text-hint; text-decoration: line-through; }
.discount { font-size: $font-size-xs; background: $brand-orange; color: #fff; padding: 2px 8px; border-radius: 4px; }

.meta { display: flex; gap: 24px; font-size: $font-size-sm; color: $text-secondary; margin-bottom: 24px; }

.actions { display: flex; gap: 12px; }
.btn-cart, .btn-buy { flex: 1; height: 48px; border-radius: 24px; border: none; font-size: 16px; font-weight: 600; cursor: pointer; transition: all $transition-fast;
  &:hover { transform: translateY(-1px); box-shadow: $shadow-md; }
}
.btn-cart { background: #FFF3EC; color: $brand-orange; border: 1.5px solid $brand-orange; }
.btn-buy { background: $brand-gradient; color: #fff; }

// Tabs
.detail-bottom { margin-top: 32px; }
.tabs { display: flex; border-bottom: 2px solid $border-color; margin-bottom: 24px;
  span { padding: 12px 24px; font-size: $font-size-base; color: $text-secondary; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all $transition-fast;
    &.active { color: $brand-orange; border-bottom-color: $brand-orange; font-weight: 600; }
  }
}
.tab-content { min-height: 200px; line-height: 1.8; font-size: $font-size-base; color: $text-secondary; }
.spec-table { width: 100%; max-width: 400px; border-collapse: collapse;
  tr { border-bottom: 1px solid $border-light; }
  td { padding: 10px 12px; font-size: $font-size-sm; &:first-child { color: $text-hint; width: 80px; } }
}

// SKU popup
.sku-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 300; }
.sku-popup { position: fixed; bottom: 0; left: 50%; transform: translateX(-50%); max-width: 480px; width: 100%; background: #fff; border-radius: 16px 16px 0 0; padding: 24px; z-index: 301; }
.sku-header { display: flex; gap: 16px; margin-bottom: 24px; }
.sku-img { width: 90px; height: 90px; border-radius: 8px; object-fit: cover; }
.sku-price { font-size: 22px; font-weight: 700; color: $brand-orange; display: block; }
.sku-stock { font-size: 13px; color: $text-hint; }
.sku-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; font-size: $font-size-base; }
.qty-stepper { display: flex; align-items: center; gap: 0; border: 1px solid $border-color; border-radius: 8px; overflow: hidden;
  button { width: 40px; height: 36px; border: none; background: $bg-input; font-size: 18px; cursor: pointer; &:disabled { opacity: 0.3; cursor: not-allowed; } }
  span { width: 48px; text-align: center; font-weight: 600; }
}
.sku-confirm { width: 100%; height: 48px; background: $brand-orange; color: #fff; border: none; border-radius: 24px; font-size: 16px; font-weight: 600; cursor: pointer; }
</style>
