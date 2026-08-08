<script setup lang="ts">
defineOptions({ name: 'NewProductPage' })
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCarousel, NIcon, NEmpty } from 'naive-ui'
import { productApi, newProductBannerApi, categoryApi, type ProductDetail, type NewProductBanner, type Category } from '@shop/shared'

const router = useRouter()

// Banner
const banners = ref<NewProductBanner[]>([])

// 分类
const categories = ref<Category[]>([])
const activeCategoryId = ref<number | undefined>(undefined)

// 商品列表
const products = ref<ProductDetail[]>([])
const loading = ref(false)
const hasMore = ref(true)
let page = 1

const fetchBanners = async () => {
  try { banners.value = await newProductBannerApi.getList() } catch { /* ignore */ }
}

const fetchCategories = async () => {
  try { categories.value = await categoryApi.getTree() } catch { /* ignore */ }
}

const fetchProducts = async (refresh = false) => {
  if (refresh) { page = 1; hasMore.value = true }
  if (loading.value || !hasMore.value) return
  loading.value = true
  try {
    const data = await productApi.getNewPage({
      pageNum: page,
      pageSize: 20,
      categoryId: activeCategoryId.value,
      sortBy: 'sort'
    })
    const res = data as unknown as { records: ProductDetail[]; current: number; pages: number }
    products.value = page === 1 ? res.records : [...products.value, ...res.records]
    hasMore.value = res.current < res.pages
    page++
  } catch { /* noop */ } finally { loading.value = false }
}

const switchCategory = (id: number | undefined) => {
  activeCategoryId.value = id
  fetchProducts(true)
}

const goProduct = (id: number) => router.push(`/product/${id}`)
const goBanner = (banner: NewProductBanner) => {
  if (banner.productId) goProduct(banner.productId)
  else if (banner.linkUrl) router.push(banner.linkUrl)
}

onMounted(() => {
  fetchBanners()
  fetchCategories()
  fetchProducts(true)
})
</script>

<template>
  <div class="new-product-page">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <h1 class="page-title">新品首发</h1>

    <!-- Banner 轮播 -->
    <NCarousel v-if="banners.length" autoplay :interval="3000" class="banner-carousel">
      <div
        v-for="b in banners"
        :key="b.id"
        class="banner-slide"
        @click="goBanner(b)"
      >
        <img :src="b.imageUrl" class="banner-img" />
        <div class="banner-overlay" v-if="b.title">{{ b.title }}</div>
      </div>
    </NCarousel>

    <!-- 分类 Tab -->
    <div class="cat-tabs" v-if="categories.length">
      <span
        class="cat-tab"
        :class="{ active: activeCategoryId === undefined }"
        @click="switchCategory(undefined)"
      >全部</span>
      <span
        v-for="cat in categories"
        :key="cat.id"
        class="cat-tab"
        :class="{ active: activeCategoryId === cat.id }"
        @click="switchCategory(cat.id)"
      >{{ cat.name }}</span>
    </div>

    <!-- 商品列表 -->
    <div class="product-grid" v-if="products.length">
      <div class="product-card" v-for="p in products" :key="p.id" @click="goProduct(p.id)">
        <div class="card-img-wrap">
          <img :src="p.mainImage || '/api/v1/files/default/product'" class="card-img" />
          <span class="new-badge">NEW</span>
        </div>
        <div class="card-body">
          <div class="card-name">{{ p.name }}</div>
          <div class="card-bottom">
            <span class="card-price">¥{{ p.salePrice }}</span>
            <span class="card-orig" v-if="p.originalPrice > p.salePrice">¥{{ p.originalPrice }}</span>
            <span class="card-sales" v-if="p.sales > 0">已售 {{ p.sales > 10000 ? (p.sales / 10000).toFixed(1) + '万' : p.sales }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="loading-more" v-if="loading">加载中...</div>
    <div class="loading-more" v-else-if="products.length > 0 && !hasMore">— 已经到底了 —</div>

    <NEmpty v-if="!loading && products.length === 0" description="暂无新品，敬请期待" class="empty" />
  </div>
</template>

<style scoped lang="scss">
.back-btn { display: inline-block; padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; margin-bottom: 12px; color: #666; &:hover { border-color: $brand-orange; color: $brand-orange; } }
.new-product-page { max-width: 100%; }

.page-title {
  font-size: 28px; font-weight: 700; color: #1C1C1E;
  margin-bottom: 24px; text-align: center;
}

// Banner
.banner-carousel { margin-bottom: 24px; border-radius: 12px; overflow: hidden; }
.banner-slide { position: relative; cursor: pointer; }
.banner-img { width: 100%; height: 280px; object-fit: cover; display: block; }
.banner-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 12px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
  color: #fff; font-size: 16px; font-weight: 500;
}

// 分类 Tab
.cat-tabs {
  display: flex; gap: 8px; flex-wrap: wrap;
  margin-bottom: 24px; padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.cat-tab {
  padding: 6px 20px; border-radius: 20px;
  font-size: 14px; color: #666; background: #f5f5f5;
  cursor: pointer; transition: all 0.2s;
  &:hover { color: #FF5000; }
  &.active { background: #FF5000; color: #fff; }
}

// 商品网格
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.product-card {
  background: #fff; border-radius: 10px; overflow: hidden;
  cursor: pointer; transition: transform 0.2s, box-shadow 0.2s;
  &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
}
.card-img-wrap { position: relative; width: 100%; padding-bottom: 100%; overflow: hidden; }
.card-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; }
.new-badge {
  position: absolute; top: 8px; left: 0;
  background: #FF5000; color: #fff; font-size: 11px; font-weight: 600;
  padding: 2px 10px; border-radius: 0 10px 10px 0;
}
.card-body { padding: 12px; }
.card-name {
  font-size: 14px; color: #333; line-height: 1.4;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  min-height: 39px;
}
.card-bottom { margin-top: 8px; display: flex; align-items: baseline; gap: 6px; flex-wrap: wrap; }
.card-price { font-size: 18px; font-weight: 700; color: #FF5000; }
.card-orig { font-size: 12px; color: #999; text-decoration: line-through; }
.card-sales { font-size: 12px; color: #999; margin-left: auto; }

.loading-more { text-align: center; padding: 24px; font-size: 14px; color: #999; }
.empty { padding: 80px 0; }

@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
