<script setup lang="ts">
defineOptions({ name: 'ProductReviews' })
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { reviewApi, type ProductReview, type ReviewStats } from '@shop/shared'

const productId = ref(0)
const stats = ref<ReviewStats | null>(null)
const reviews = ref<ProductReview[]>([])
const activeTab = ref(0)
const pageNum = ref(1)
const hasMore = ref(true)
const loading = ref(false)

const tabs = [
  { label: '全部', filter: 0 },
  { label: '好评', filter: 4 },
  { label: '中评', filter: 3 },
  { label: '差评', filter: 1 },
]

onLoad((opts?: any) => {
  if (opts?.id) {
    productId.value = Number(opts.id)
    loadStats()
    loadReviews(true)
  }
})

async function loadStats() {
  try {
    stats.value = await reviewApi.getReviewStats(productId.value)
  } catch {}
}

async function loadReviews(reset = false) {
  if (loading.value) return
  if (reset) { pageNum.value = 1; hasMore.value = true; reviews.value = [] }
  if (!hasMore.value) return
  try {
    loading.value = true
    const res = await reviewApi.getProductReviews(productId.value, pageNum.value, 10)
    if (res?.records) {
      reviews.value = reset ? res.records : [...reviews.value, ...res.records]
      hasMore.value = reviews.value.length < res.total
      pageNum.value++
    }
  } finally {
    loading.value = false
  }
}

function switchTab(idx: number) {
  activeTab.value = idx
  loadReviews(true)
}

function getFilteredReviews() {
  const tab = tabs[activeTab.value]
  if (tab.filter === 0) return reviews.value
  if (tab.filter === 4) return reviews.value.filter(r => r.rating >= 4)
  if (tab.filter === 3) return reviews.value.filter(r => r.rating === 3)
  return reviews.value.filter(r => r.rating <= 2)
}

function renderStars(rating: number) {
  return rating
}

function parseImages(images?: string): string[] {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr : images.split(',').filter(Boolean)
  } catch {
    return images.split(',').filter(Boolean)
  }
}

onReachBottom(() => loadReviews())
</script>

<template>
  <view class="rv">
    <!-- 评分统计 -->
    <view class="stats-card" v-if="stats">
      <view class="stats-left">
        <text class="avg-rating">{{ stats.avgRating?.toFixed(1) }}</text>
        <text class="good-rate">好评率 {{ stats.goodRate?.toFixed(0) || 0 }}%</text>
      </view>
      <view class="stats-right">
        <view class="bar-row" v-for="i in 5" :key="i">
          <text class="bar-label">{{ 6 - i }}星</text>
          <view class="bar-track"><view class="bar-fill" :style="{ width: ((stats as any)[`rating${6-i}Count`] || 0) / Math.max(stats.totalCount, 1) * 100 + '%' }"/></view>
          <text class="bar-count">{{ (stats as any)[`rating${6-i}Count`] || 0 }}</text>
        </view>
      </view>
    </view>

    <!-- Tab -->
    <view class="tabs">
      <view
        v-for="(tab, i) in tabs" :key="i"
        class="tab-item" :class="{ active: activeTab === i }"
        @click="switchTab(i)"
      >{{ tab.label }}</view>
    </view>

    <!-- 评价列表 -->
    <view class="review-list">
      <view class="review-item" v-for="r in getFilteredReviews()" :key="r.id">
        <view class="review-header">
          <view class="user-info">
            <image v-if="!r.isAnonymous && r.userAvatar" :src="r.userAvatar" class="user-avatar"/>
            <view v-else class="user-avatar placeholder">
              <uni-icons type="person" size="24" color="#999" />
            </view>
            <text class="user-name">{{ r.isAnonymous ? '匿名用户' : (r.userNickname || '用户') }}</text>
          </view>
          <view class="stars">
            <uni-icons v-for="i in 5" :key="i" :type="i <= r.rating ? 'star-filled' : 'star'" size="14" :color="i <= r.rating ? '#FF9500' : '#ddd'" />
          </view>
        </view>
        <text class="review-content" v-if="r.content">{{ r.content }}</text>
        <view class="review-images" v-if="parseImages(r.images).length">
          <image v-for="(img, idx) in parseImages(r.images)" :key="idx" :src="img" mode="aspectFill" class="review-img"/>
        </view>
        <view class="review-reply" v-if="r.replyContent">
          <text class="reply-label">商家回复：</text>
          <text class="reply-content">{{ r.replyContent }}</text>
        </view>
        <text class="review-time">{{ r.createTime?.replace('T', ' ').substring(0, 16) }}</text>
      </view>

      <view v-if="!getFilteredReviews().length && !loading" class="empty">暂无评价</view>
      <view v-if="loading" class="loading-tip">加载中...</view>
      <view v-if="!hasMore && getFilteredReviews().length" class="no-more">没有更多了</view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.rv { min-height: 100vh; background: #f5f5f5; }

.stats-card {
  display: flex; background: #fff; padding: 24rpx; margin-bottom: 16rpx; gap: 30rpx;
}
.stats-left { text-align: center; padding-top: 8rpx; min-width: 140rpx; }
.avg-rating { font-size: 56rpx; font-weight: 700; color: #FF5000; display: block; }
.good-rate { font-size: 22rpx; color: #999; }
.stats-right { flex: 1; }
.bar-row { display: flex; align-items: center; gap: 8rpx; margin-bottom: 6rpx; }
.bar-label { font-size: 22rpx; color: #999; width: 50rpx; text-align: right; }
.bar-track { flex: 1; height: 12rpx; background: #f0f0f0; border-radius: 6rpx; overflow: hidden; }
.bar-fill { height: 100%; background: #FF5000; border-radius: 6rpx; transition: width .3s; }
.bar-count { font-size: 22rpx; color: #999; width: 50rpx; }

.tabs { display: flex; background: #fff; padding: 0 16rpx; margin-bottom: 16rpx; }
.tab-item {
  flex: 1; text-align: center; padding: 20rpx 0; font-size: 26rpx; color: #666;
  position: relative;
}
.tab-item.active { color: #FF5000; font-weight: 600; }
.tab-item.active::after {
  content: ''; position: absolute; bottom: 0; left: 30%; right: 30%;
  height: 4rpx; background: #FF5000; border-radius: 2rpx;
}

.review-list { padding: 0 16rpx; }
.review-item { background: #fff; padding: 24rpx; margin-bottom: 12rpx; border-radius: 12rpx; }
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.user-info { display: flex; align-items: center; gap: 12rpx; }
.user-avatar { width: 48rpx; height: 48rpx; border-radius: 50%; }
.user-avatar.placeholder { display: flex; align-items: center; justify-content: center; background: #f0f0f0; }
.user-name { font-size: 26rpx; color: #333; }
.stars { display: flex; align-items: center; gap: 4rpx; }

.review-content { font-size: 28rpx; color: #333; line-height: 1.6; display: block; margin-bottom: 12rpx; }
.review-images { display: flex; flex-wrap: wrap; gap: 8rpx; margin-bottom: 12rpx; }
.review-img { width: 160rpx; height: 160rpx; border-radius: 8rpx; }

.review-reply {
  background: #f8f8f8; padding: 16rpx; border-radius: 8rpx; margin-bottom: 8rpx;
}
.reply-label { font-size: 24rpx; color: #FF5000; font-weight: 600; }
.reply-content { font-size: 24rpx; color: #666; }

.review-time { font-size: 22rpx; color: #ccc; }

.empty { text-align: center; padding: 100rpx 0; color: #999; }
.loading-tip { text-align: center; padding: 24rpx 0; color: #999; font-size: 24rpx; }
.no-more { text-align: center; padding: 24rpx 0; color: #ccc; font-size: 22rpx; }
</style>
