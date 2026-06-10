<script setup lang="ts">defineOptions({ name: 'CategoryPage' })
import { ref, onMounted } from 'vue'
import { categoryApi, productApi, type Category, type ProductDetail } from '@shop/shared'
const cats = ref<Category[]>([])
const activeId = ref(0)
const products = ref<ProductDetail[]>([])
const loading = ref(false)
const loadingMore = ref(false)
let page = 1
const hasMore = ref(true)

onMounted(async () => {
  const t = await categoryApi.getTree()
  cats.value = t || []
  if (cats.value[0]) { activeId.value = cats.value[0].id; load(true) }
})

const select = (id: number) => { activeId.value = id; load(true) }

const load = async (refresh = false) => {
  if (refresh) { page = 1; hasMore.value = true }
  if (!hasMore.value && !refresh) return
  if (refresh) loading.value = true; else loadingMore.value = true
  try {
    const d = await productApi.getPage({ categoryId: activeId.value, pageNum: page, pageSize: 20 })
    const res = d as any
    if (refresh) {
      products.value = res.records || []
    } else {
      products.value.push(...(res.records || []))
    }
    hasMore.value = res.current < res.pages
    page++
  } catch (e) {
    if (!refresh) uni.showToast({ title: '加载失败', icon: 'none' })
  } finally { loading.value = false; loadingMore.value = false }
}

const onLoadMore = () => { if (!loading.value && !loadingMore.value && hasMore.value) load() }
const goP = (id: number) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
</script>
<template>
  <view class="cp">
    <scroll-view scroll-y class="sb">
      <view v-for="c in cats" :key="c.id" class="si" :class="{on:c.id===activeId}" @click="select(c.id)">{{c.name}}</view>
    </scroll-view>
    <scroll-view scroll-y class="ct" @scrolltolower="onLoadMore">
      <view class="sg" v-if="!loading">
        <view class="sc" v-for="p in products" :key="p.id" @click="goP(p.id)">
          <image :src="p.mainImage" mode="aspectFill" lazy-load />
          <text class="sn">{{p.name}}</text>
          <text class="sp">¥{{p.salePrice}}</text>
        </view>
      </view>
      <view class="ld" v-if="loading">加载中...</view>
      <view class="ld" v-else-if="loadingMore">加载中...</view>
      <view class="ld" v-else-if="!hasMore && products.length">— 没有更多了 —</view>
      <view class="ld" v-else-if="!loading && !products.length">暂无商品</view>
    </scroll-view>
  </view>
</template>
<style scoped lang="scss">
.cp{display:flex;height:100vh}.sb{width:160rpx;background:#F8F8FA}.si{padding:28rpx 16rpx;font-size:26rpx;color:#666;text-align:center;position:relative;&.on{background:#fff;color:#FF5000;font-weight:700;&::after{content:'';position:absolute;left:0;top:20%;height:60%;width:4rpx;background:#FF5000;border-radius:2rpx}}}.ct{flex:1;padding:16rpx}.sg{display:flex;flex-wrap:wrap;gap:10rpx}.sc{width:calc(50% - 5rpx);background:#fff;border-radius:12rpx;overflow:hidden;padding-bottom:12rpx;image{width:100%;height:240rpx;background:#f0f0f0}}.sn{font-size:24rpx;padding:8rpx 12rpx 0;display:block;overflow:hidden;white-space:nowrap;text-overflow:ellipsis}.sp{font-size:28rpx;font-weight:700;color:#FF5000;padding:4rpx 12rpx}.ld{text-align:center;padding:30rpx;color:#999;font-size:26rpx}
</style>
