<script setup lang="ts">
defineOptions({ name: 'NewProductPage' })
import { ref, onMounted } from 'vue'
import { productApi, newProductBannerApi, categoryApi, type ProductDetail, type NewProductBanner, type Category } from '@shop/shared'

const sbh = uni.getSystemInfoSync().statusBarHeight || 0

// Banner
const banners = ref<NewProductBanner[]>([])

// 分类
const categories = ref<Category[]>([])
const activeCategoryId = ref<number | undefined>(undefined)

// 商品列表
const products = ref<ProductDetail[]>([])
const loading = ref(true)
const refreshing = ref(false)
let page = 1
const hasMore = ref(true)

const fetchBanners = async () => {
  try {
    banners.value = await newProductBannerApi.getList()
  } catch { /* ignore */ }
}

const fetchCategories = async () => {
  try {
    const tree = await categoryApi.getTree()
    // 只取一级分类
    categories.value = tree || []
  } catch { /* ignore */ }
}

const fetchProducts = async (refresh = false) => {
  if (refresh) { page = 1; hasMore.value = true }
  if (!hasMore.value && !refresh) return
  loading.value = true
  try {
    const data = await productApi.getNewPage({
      pageNum: page,
      pageSize: 20,
      categoryId: activeCategoryId.value,
      sortBy: 'sort'
    })
    const res = data as any
    if (refresh) {
      products.value = res.records || []
    } else {
      products.value.push(...(res.records || []))
    }
    hasMore.value = res.current < res.pages
    page++
  } catch (e) {
    if (!refresh) await uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const switchCategory = (id: number | undefined) => {
  activeCategoryId.value = id
  fetchProducts(true)
}

const onRefresh = () => { refreshing.value = true; fetchProducts(true) }
const onLoadMore = () => { if (!loading.value && hasMore.value) fetchProducts() }

const goBack = () => uni.navigateBack()
const goProduct = (id: number) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
const goBanner = (banner: NewProductBanner) => {
  if (banner.productId) {
    goProduct(banner.productId)
  } else if (banner.linkUrl) {
    uni.navigateTo({ url: banner.linkUrl })
  }
}

onMounted(() => {
  fetchBanners()
  fetchCategories()
  fetchProducts(true)
})
</script>

<template>
  <view class="np">
    <!-- 自定义导航栏 -->
    <view class="nav" :style="{ paddingTop: sbh + 'px' }">
      <view class="nav-inner">
        <view class="back" @click="goBack">
          <uni-icons type="back" size="24" color="#333" />
        </view>
        <text class="title">新品首发</text>
        <view class="placeholder" />
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- Banner 轮播 -->
      <swiper
        v-if="banners.length"
        class="banner"
        circular
        autoplay
        :interval="3000"
        :style="{ marginTop: (sbh + 50) + 'px' }"
      >
        <swiper-item v-for="b in banners" :key="b.id" @click="goBanner(b)">
          <view class="banner-card">
            <image :src="b.imageUrl" mode="aspectFill" class="banner-img" />
            <view class="banner-title" v-if="b.title">{{ b.title }}</view>
          </view>
        </swiper-item>
      </swiper>
      <view v-else :style="{ marginTop: (sbh + 50) + 'px' }" />

      <!-- 分类 Tab -->
      <scroll-view scroll-x class="cat-tabs" v-if="categories.length">
        <view
          class="tab-item"
          :class="{ active: activeCategoryId === undefined }"
          @click="switchCategory(undefined)"
        >
          <text>全部</text>
        </view>
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="tab-item"
          :class="{ active: activeCategoryId === cat.id }"
          @click="switchCategory(cat.id)"
        >
          <text>{{ cat.name }}</text>
        </view>
      </scroll-view>

      <!-- 商品列表 -->
      <view class="grid" v-if="products.length">
        <view class="card" v-for="p in products" :key="p.id" @click="goProduct(p.id)">
          <view class="img-wrap">
            <image :src="p.mainImage" mode="aspectFill" class="card-img" />
            <view class="new-tag">NEW</view>
          </view>
          <view class="card-info">
            <text class="card-name">{{ p.name }}</text>
            <view class="card-price">
              <text class="price">¥{{ p.salePrice }}</text>
              <text class="orig" v-if="p.originalPrice > p.salePrice">¥{{ p.originalPrice }}</text>
            </view>
            <text class="card-sales" v-if="p.sales > 0">已售 {{ p.sales }}</text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-more" v-if="loading && products.length">
        <text>加载中...</text>
      </view>

      <!-- 空状态 -->
      <view class="empty" v-if="!loading && products.length === 0">
        <uni-icons type="shop" size="60" color="#ddd" />
        <text class="empty-text">暂无新品，敬请期待</text>
      </view>

      <!-- 底部提示 -->
      <view class="bottom-tip" v-if="products.length > 0 && !hasMore">
        <text>— 已经到底了 —</text>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.np { min-height: 100vh; background: #f5f5f5; }
.main { border: none; }
:deep(.uni-scroll-view) { border: none; outline: none; }

/* 导航栏 */
.nav { position: fixed; top: 0; left: 0; right: 0; z-index: 100; background: #fff; }
.nav-inner { height: 50px; display: flex; align-items: center; padding: 0 16px; }
.back { width: 40px; }
.title { flex: 1; text-align: center; font-size: 17px; font-weight: 600; color: #333; }
.placeholder { width: 40px; }

/* Banner */
.banner { height: 180px; margin: 0 16px 12px; border-radius: 12px; overflow: hidden; }
.banner-card { position: relative; width: 100%; height: 100%; border-radius: 12px; overflow: hidden; }
.banner-img { width: 100%; height: 100%; }
.banner-title { position: absolute; bottom: 0; left: 0; right: 0; padding: 8px 12px; background: linear-gradient(transparent, rgba(0,0,0,0.5)); color: #fff; font-size: 14px; }

/* 分类 Tab */
.cat-tabs { white-space: nowrap; padding: 0 12px 12px; background: #fff; }
.tab-item { display: inline-block; padding: 6px 16px; margin: 0 4px; border-radius: 20px; font-size: 14px; color: #666; background: #f5f5f5; }
.tab-item.active { background: #FF5000; color: #fff; }

/* 商品网格 */
.grid { display: flex; flex-wrap: wrap; padding: 8px; gap: 8px; }
.card { width: calc(50% - 4px); background: #fff; border-radius: 10px; overflow: hidden; }
.img-wrap { position: relative; width: 100%; height: 0; padding-bottom: 100%; }
.card-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
.new-tag { position: absolute; top: 8px; left: 0; background: #FF5000; color: #fff; font-size: 10px; padding: 2px 8px; border-radius: 0 10px 10px 0; font-weight: 600; }
.card-info { padding: 8px 10px 12px; }
.card-name { font-size: 13px; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.4; }
.card-price { margin-top: 6px; display: flex; align-items: baseline; gap: 6px; }
.price { font-size: 16px; font-weight: 700; color: #FF5000; }
.orig { font-size: 12px; color: #999; text-decoration: line-through; }
.card-sales { font-size: 11px; color: #999; margin-top: 4px; }

/* 加载 & 空状态 */
.loading-more { text-align: center; padding: 20px; font-size: 13px; color: #999; }
.empty { display: flex; flex-direction: column; align-items: center; padding: 80px 0; }
.empty-text { font-size: 14px; color: #999; margin-top: 12px; }
.bottom-tip { text-align: center; padding: 20px; font-size: 12px; color: #ccc; }
</style>
