<script setup lang="ts">
defineOptions({ name: 'HomePage' })

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCarousel, NIcon } from 'naive-ui'
import { FlashOutline, DiamondOutline, FlameOutline, ChatbubbleOutline, VideocamOutline, TicketOutline, CubeOutline, StarOutline } from '@vicons/ionicons5'
import { productApi, seckillApi, bannerApi, type ProductDetail, type SeckillSession, type Banner } from '@shop/shared'

const router = useRouter()

const iconList = [
  { name: '限时秒杀', icon: FlashOutline, color: '#FF5000' },
  { name: '会员中心', icon: DiamondOutline, color: '#FF8F1F' },
  { name: '百亿补贴', icon: FlameOutline, color: '#FF3B3B' },
  { name: '社区', icon: ChatbubbleOutline, color: '#00B578', path: '/community' },
  { name: '直播', icon: VideocamOutline, color: '#409EFF', path: '/live' },
  { name: '领券中心', icon: TicketOutline, color: '#FF5000' },
  { name: '新品首发', icon: CubeOutline, color: '#1C1C1E' },
  { name: '我的收藏', icon: StarOutline, color: '#FF8F1F' },
]

// Banner
const banners = ref<Banner[]>([])

// 商品
const products = ref<ProductDetail[]>([])
const loading = ref(false)
const hasMore = ref(true)
let page = 1

const fetchProducts = async () => {
  if (loading.value || !hasMore.value) return
  loading.value = true
  try {
    const data = await productApi.getPage({ pageNum: page, pageSize: 20 })
    const res = data as unknown as { records: ProductDetail[]; current: number; pages: number }
    products.value = page === 1 ? res.records : [...products.value, ...res.records]
    hasMore.value = res.current < res.pages
    page++
  } catch { /* noop */ } finally { loading.value = false }
}

// 秒杀
const seckillSessions = ref<SeckillSession[]>([])

onMounted(async () => {
  await fetchProducts()
  bannerApi.getActive().then(data => { banners.value = data }).catch(() => {})
  try { seckillSessions.value = await seckillApi.getActiveSessions() } catch { /* noop */ }
})

const goProduct = (id: number) => router.push(`/product/${id}`)
const goCategory = (id: number) => router.push(`/category/${id}`)
</script>

<template>
  <div class="home-page">
    <!-- Banner 轮播 -->
    <NCarousel
      v-if="banners.length"
      autoplay
      :interval="3000"
      :show-arrow="banners.length > 1"
      :show-dots="banners.length > 1"
      class="banner-carousel"
    >
      <div
        class="banner-slide"
        v-for="b in banners"
        :key="b.id"
        :style="{ backgroundImage: `url(${b.imageUrl})` }"
        @click="b.linkUrl && router.push(b.linkUrl)"
      >
        <div class="banner-text">
          <h2>{{ b.title }}</h2>
        </div>
      </div>
    </NCarousel>

    <!-- 金刚区 -->
    <div class="icon-zone">
      <div class="icon-item" v-for="item in iconList" :key="item.name" @click="router.push(item.path || '/')">
        <span class="icon-circle" :style="{ background: item.color }">
          <n-icon :size="24" color="#fff">
            <component :is="item.icon" />
          </n-icon>
        </span>
        <span class="icon-label">{{ item.name }}</span>
      </div>
    </div>

    <!-- 秒杀跑马灯 -->
    <div class="seckill-strip" v-if="seckillSessions.length">
      <span class="seckill-tag">
        <n-icon :size="18" color="#FF5000"><FlashOutline /></n-icon>
        限时秒杀
      </span>
      <span class="seckill-item" v-for="s in seckillSessions" :key="s.id">
        {{ s.name }} {{ s.startTime?.slice(11, 16) }}
      </span>
    </div>

    <!-- 商品瀑布流 -->
    <div class="product-section">
      <div class="section-header"><h3>为你推荐</h3></div>
      <div class="product-grid">
        <div class="product-card" v-for="p in products" :key="p.id" @click="goProduct(p.id)">
          <img :src="p.mainImage || '/api/v1/files/default/product'" class="product-img" />
          <div class="product-body">
            <div class="product-name">{{ p.name }}</div>
            <div class="product-bottom">
              <span class="product-price">¥{{ p.salePrice }}</span>
              <span class="product-sales">已售 {{ (p.sales || 0) > 10000 ? ((p.sales / 10000).toFixed(1) + '万') : p.sales }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="load-more" v-if="loading">加载中...</div>
      <div class="load-more" v-else-if="!hasMore">— 没有更多了 —</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.home-page { max-width: 100%; }

// Banner
.banner-carousel { margin-bottom: $spacing-xl; border-radius: $radius-md; overflow: hidden; }
.banner-slide {
  height: 360px; border-radius: $radius-md;
  background-size: cover; background-position: center; background-color: #f5f5f5;
  display: flex; align-items: flex-end; padding: $spacing-2xl; color: #fff; overflow: hidden;
  cursor: pointer;
}
.banner-text h2 { font-size: $font-size-2xl; margin-bottom: $spacing-sm; text-shadow: 0 2px 8px rgba(0,0,0,0.4); }
.banner-text p { font-size: $font-size-base; opacity: 0.85; }

// 金刚区
.icon-zone {
  display: flex; justify-content: space-around;
  padding: $spacing-xl 0; margin-bottom: $spacing-xl;
  border-bottom: 1px solid $border-color;
}
.icon-item { display: flex; flex-direction: column; align-items: center; gap: $spacing-sm; cursor: pointer; }
.icon-circle {
  width: 56px; height: 56px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff;
}
.icon-label { font-size: $font-size-xs; color: $text-secondary; }

// 秒杀
.seckill-strip {
  background: linear-gradient(90deg, #FFF3EC, #FFF); padding: $spacing-md $spacing-xl;
  border-radius: $radius-md; margin-bottom: $spacing-xl; overflow-x: auto; white-space: nowrap;
  display: flex; align-items: center; gap: $spacing-lg;
}
.seckill-tag { display: flex; align-items: center; gap: 6px; font-weight: 700; color: $brand-orange; font-size: $font-size-md; }
.seckill-item { font-size: $font-size-sm; color: $text-secondary; }

// 商品
.product-section { margin-top: 32px; }
.section-header { margin-bottom: $spacing-xl; }
.section-header h3 { font-size: $font-size-lg; font-weight: 700; }
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-base;
  @media (max-width: 1199px) { grid-template-columns: repeat(3, 1fr); }
  @media (max-width: 767px) { grid-template-columns: repeat(2, 1fr); gap: $spacing-sm; }
}
.product-card {
  background: $bg-card; border-radius: $radius-md; overflow: hidden; cursor: pointer;
  transition: all $transition-fast;
  &:hover { transform: translateY(-4px); box-shadow: $shadow-md; }
}
.product-img { width: 100%; aspect-ratio: 1; object-fit: cover; background: $bg-input; }
.product-body { padding: $spacing-md; }
.product-name {
  font-size: $font-size-sm; color: $text-primary; overflow: hidden;
  text-overflow: ellipsis; white-space: nowrap; margin-bottom: $spacing-sm;
}
.product-bottom { display: flex; justify-content: space-between; align-items: center; }
.product-price { font-size: $font-size-md; font-weight: 700; color: $brand-orange; }
.product-sales { font-size: $font-size-xs; color: $text-hint; }
.load-more { text-align: center; padding: $spacing-2xl; color: $text-hint; font-size: $font-size-sm; }
</style>
