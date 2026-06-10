<script setup lang="ts">
defineOptions({ name: 'SearchPage' })
import { ref, watch, onMounted } from 'vue'
import { productApi, searchApi, type ProductDetail, type HotKeyword } from '@shop/shared'

const keyword = ref('')
const results = ref<ProductDetail[]>([])
const loading = ref(false)
const loadingMore = ref(false)
let page = 1
const hasMore = ref(true)
const searched = ref(false)

// 热搜
const hotKeywords = ref<HotKeyword[]>([])

// 搜索历史
const HISTORY_KEY = 'search_history'
const MAX_HISTORY = 10
const history = ref<string[]>([])

// 搜索建议
const suggestions = ref<string[]>([])
const hotSuggests = ref<string[]>([])
const showSuggest = ref(false)
let suggestTimer: ReturnType<typeof setTimeout> | null = null

// 排序
const sortBy = ref<string>('')
const sortOptions = [
  { label: '综合', value: '' },
  { label: '价格↑', value: 'price_asc' },
  { label: '价格↓', value: 'price_desc' },
  { label: '销量', value: 'sales' },
  { label: '新品', value: 'newest' },
]

// 初始化
onMounted(async () => {
  loadHistory()
  try {
    hotKeywords.value = await searchApi.getHot(10)
  } catch { /* noop */ }
})

// 搜索历史管理
const loadHistory = () => {
  try {
    history.value = JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
  } catch { history.value = [] }
}

const saveHistory = (kw: string) => {
  const list = history.value.filter(h => h !== kw)
  list.unshift(kw)
  history.value = list.slice(0, MAX_HISTORY)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}

const removeHistory = (kw: string) => {
  history.value = history.value.filter(h => h !== kw)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}

const clearHistory = () => {
  history.value = []
  localStorage.removeItem(HISTORY_KEY)
}

// 搜索建议（防抖）
watch(keyword, (val) => {
  if (suggestTimer) clearTimeout(suggestTimer)
  if (!val.trim()) {
    showSuggest.value = false
    suggestions.value = []
    hotSuggests.value = []
    return
  }
  suggestTimer = setTimeout(async () => {
    try {
      const res = await searchApi.getSuggest(val.trim())
      suggestions.value = res.products || []
      hotSuggests.value = res.hotWords || []
      showSuggest.value = true
    } catch { /* noop */ }
  }, 300)
})

// 执行搜索
const doSearch = (kw?: string) => {
  const term = (kw || keyword.value).trim()
  if (!term) return
  if (kw) keyword.value = kw
  showSuggest.value = false
  searched.value = true
  sortBy.value = ''
  saveHistory(term)
  searchApi.record(term).catch(() => {})
  fetchResults(true)
}

// 搜索结果
const fetchResults = async (refresh = false) => {
  if (refresh) { page = 1; hasMore.value = true }
  if (!hasMore.value && !refresh) return
  if (refresh) loading.value = true; else loadingMore.value = true
  try {
    const params: Record<string, unknown> = {
      keyword: keyword.value.trim(),
      pageNum: page,
      pageSize: 20,
    }
    if (sortBy.value) params.sortBy = sortBy.value
    const res = await productApi.getPage(params) as any
    if (refresh) {
      results.value = res.records || []
    } else {
      results.value.push(...(res.records || []))
    }
    hasMore.value = res.current < res.pages
    page++
  } catch (e) {
    if (!refresh) uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 切换排序
const changeSort = (val: string) => {
  if (sortBy.value === val) return
  sortBy.value = val
  fetchResults(true)
}

// 加载更多
const onLoadMore = () => {
  if (!loading.value && !loadingMore.value && hasMore.value) fetchResults()
}

// 跳转商品
const goProduct = (id: number) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })

// 返回
const goBack = () => uni.navigateBack()
</script>

<template>
  <view class="sp">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="back" @click="goBack">
        <text>‹</text>
      </view>
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索商品"
          :focus="true"
          @confirm="doSearch()"
          confirm-type="search"
        />
        <text v-if="keyword" class="clear" @click="keyword = ''; searched = false">✕</text>
      </view>
      <text class="search-btn" @click="doSearch()">搜索</text>
    </view>

    <!-- 初始态：热搜 + 历史 -->
    <view v-if="!searched && !showSuggest" class="init-state">
      <!-- 热门搜索 -->
      <view v-if="hotKeywords.length" class="section">
        <view class="section-hd">
          <text class="section-title">🔥 热门搜索</text>
        </view>
        <view class="tags">
          <view
            v-for="item in hotKeywords"
            :key="item.keyword"
            class="tag"
            @click="doSearch(item.keyword)"
          >
            <text>{{ item.keyword }}</text>
          </view>
        </view>
      </view>

      <!-- 搜索历史 -->
      <view v-if="history.length" class="section">
        <view class="section-hd">
          <text class="section-title">🕐 搜索历史</text>
          <text class="section-action" @click="clearHistory">清空</text>
        </view>
        <view class="tags">
          <view v-for="h in history" :key="h" class="tag history-tag">
            <text @click="doSearch(h)">{{ h }}</text>
            <text class="tag-del" @click.stop="removeHistory(h)">✕</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 输入态：搜索建议 -->
    <view v-if="showSuggest" class="suggest-list">
      <view
        v-for="s in suggestions"
        :key="s"
        class="suggest-item"
        @click="doSearch(s)"
      >
        <text class="suggest-icon">📱</text>
        <text class="suggest-text">{{ s }}</text>
      </view>
      <view
        v-for="h in hotSuggests"
        :key="'hot-' + h"
        class="suggest-item hot"
        @click="doSearch(h)"
      >
        <text class="suggest-icon">🔥</text>
        <text class="suggest-text">热搜：{{ h }}</text>
      </view>
      <view v-if="!suggestions.length && !hotSuggests.length" class="suggest-empty">
        <text>无匹配建议</text>
      </view>
    </view>

    <!-- 结果态：排序 + 商品列表 -->
    <view v-if="searched" class="result-state">
      <!-- 排序Tab -->
      <view class="sort-bar">
        <view
          v-for="opt in sortOptions"
          :key="opt.value"
          class="sort-item"
          :class="{ active: sortBy === opt.value }"
          @click="changeSort(opt.value)"
        >
          <text>{{ opt.label }}</text>
        </view>
      </view>

      <!-- 商品列表 -->
      <scroll-view scroll-y class="result-scroll" @scrolltolower="onLoadMore">
        <view v-if="loading && !results.length" class="loading-state">
          <text>加载中...</text>
        </view>
        <view v-else-if="!loading && !results.length" class="empty-state">
          <text>未找到相关商品</text>
        </view>
        <view v-else class="grid">
          <view v-for="p in results" :key="p.id" class="card" @click="goProduct(p.id)">
            <image :src="p.mainImage" mode="aspectFill" class="card-img" lazy-load />
            <view class="card-body">
              <text class="card-name">{{ p.name }}</text>
              <view class="card-foot">
                <text class="card-price">¥{{ p.salePrice }}</text>
                <text class="card-sales">已售{{ (p.sales||0) > 10000 ? ((p.sales/1e4).toFixed(1)+'万') : p.sales }}</text>
              </view>
            </view>
          </view>
        </view>
        <view v-if="loadingMore" class="more"><text>加载中...</text></view>
        <view v-else-if="!hasMore && results.length" class="more"><text>— 没有更多了 —</text></view>
      </scroll-view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.sp { height: 100vh; display: flex; flex-direction: column; background: #f5f5f5; }

.search-bar {
  display: flex; align-items: center; gap: 12rpx;
  padding: 12rpx 16rpx; background: #fff;
  .back { font-size: 40rpx; color: #333; padding: 0 8rpx; }
  .search-input-wrap {
    flex: 1; display: flex; align-items: center;
    background: #f5f5f5; border-radius: 32rpx; padding: 0 20rpx; height: 64rpx;
    .search-icon { font-size: 28rpx; margin-right: 8rpx; }
    .search-input { flex: 1; font-size: 26rpx; }
    .clear { font-size: 24rpx; color: #999; padding: 0 8rpx; }
  }
  .search-btn { font-size: 28rpx; color: #FF5000; font-weight: 600; padding: 0 8rpx; }
}

.section { background: #fff; margin-top: 16rpx; padding: 20rpx 24rpx;
  .section-hd { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
  .section-title { font-size: 28rpx; font-weight: 600; color: #333; }
  .section-action { font-size: 24rpx; color: #999; }
}

.tags { display: flex; flex-wrap: wrap; gap: 12rpx;
  .tag {
    background: #f5f5f5; padding: 10rpx 20rpx; border-radius: 24rpx; font-size: 24rpx; color: #333;
    display: flex; align-items: center; gap: 8rpx;
  }
  .history-tag { padding-right: 12rpx; }
  .tag-del { font-size: 20rpx; color: #ccc; }
}

.suggest-list { background: #fff; margin-top: 4rpx;
  .suggest-item {
    display: flex; align-items: center; gap: 12rpx;
    padding: 20rpx 24rpx; border-bottom: 1rpx solid #f5f5f5;
    &.hot { color: #FF5000; }
    .suggest-icon { font-size: 28rpx; }
    .suggest-text { font-size: 26rpx; }
  }
  .suggest-empty { padding: 40rpx; text-align: center; color: #999; font-size: 26rpx; }
}

.sort-bar {
  display: flex; background: #fff; padding: 16rpx 24rpx; gap: 8rpx;
  .sort-item {
    flex: 1; text-align: center; padding: 10rpx 0; border-radius: 24rpx;
    font-size: 24rpx; color: #666; background: #f5f5f5;
    &.active { background: #FF5000; color: #fff; }
  }
}

.result-scroll { flex: 1; padding: 16rpx; }

.grid { display: flex; flex-wrap: wrap; gap: 10rpx; }
.card { width: calc(50% - 5rpx); background: #fff; border-radius: 16rpx; overflow: hidden; }
.card-img { width: 100%; height: 340rpx; background: #f0f0f0; }
.card-body { padding: 14rpx; }
.card-name { font-size: 26rpx; color: #1C1C1E; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
.card-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 8rpx; }
.card-price { font-size: 32rpx; font-weight: 700; color: #FF5000; }
.card-sales { font-size: 20rpx; color: #999; }

.more { text-align: center; padding: 30rpx; font-size: 24rpx; color: #999; }
.loading-state, .empty-state { text-align: center; padding: 120rpx 0; color: #999; font-size: 28rpx; }
</style>
