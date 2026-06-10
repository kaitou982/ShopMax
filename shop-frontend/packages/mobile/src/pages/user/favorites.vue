<script setup lang="ts">
defineOptions({ name: 'MyFavorites' })
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { communityApi, type NoteResponse } from '@shop/shared'

const favorites = ref<NoteResponse[]>([])
const loading = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)

onLoad(() => { loadFavorites(true) })

async function loadFavorites(reset = false) {
  if (loading.value) return
  if (reset) { pageNum.value = 1; hasMore.value = true; favorites.value = [] }
  if (!hasMore.value) return
  try {
    loading.value = true
    const res = await communityApi.getMyFavorites({ pageNum: pageNum.value, pageSize: 10 })
    if (res?.records) {
      favorites.value = reset ? res.records : [...favorites.value, ...res.records]
      hasMore.value = favorites.value.length < res.total
      pageNum.value++
    }
  } catch {} finally { loading.value = false }
}

async function unfavorite(note: NoteResponse) {
  const confirmed = await new Promise<boolean>(resolve =>
    uni.showModal({ title: '取消收藏', content: '确定取消收藏该笔记？', success: res => resolve(res.confirm) })
  )
  if (!confirmed) return
  try {
    await communityApi.toggleFavorite(note.id)
    favorites.value = favorites.value.filter(f => f.id !== note.id)
    uni.showToast({ title: '已取消收藏', icon: 'success' })
  } catch {}
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
}

function formatTime(t: string) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onReachBottom(() => loadFavorites())
</script>

<template>
  <view class="fav">
    <view v-if="!favorites.length && !loading" class="empty">
      <text class="empty-icon">⭐</text>
      <text class="empty-text">还没有收藏的笔记</text>
    </view>

    <view class="fav-list">
      <view class="fav-card" v-for="note in favorites" :key="note.id" @click="goDetail(note.id)">
        <image v-if="note.coverUrl || note.images?.length" :src="note.coverUrl || (note.images as string[])[0]" class="fav-img" mode="aspectFill"/>
        <view class="fav-body">
          <text class="fav-title">{{ note.title || '无标题' }}</text>
          <text class="fav-content" v-if="note.content">{{ note.content }}</text>
          <view class="fav-footer">
            <view class="fav-user">
              <image v-if="note.userAvatar" :src="note.userAvatar" class="fav-avatar"/>
              <text class="fav-name">{{ note.userNickname || '用户' }}</text>
            </view>
            <view class="fav-actions">
              <text class="fav-time">{{ formatTime(note.createTime) }}</text>
              <text class="fav-unfav" @click.stop="unfavorite(note)">取消收藏</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="loading" class="loading-tip">加载中...</view>
    <view v-if="!hasMore && favorites.length" class="no-more">没有更多了</view>
  </view>
</template>

<style scoped lang="scss">
.fav { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.empty { text-align: center; padding: 200rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #999; }

.fav-list { }
.fav-card {
  background: #fff; border-radius: 12rpx; overflow: hidden;
  margin-bottom: 16rpx;
}
.fav-img { width: 100%; height: 300rpx; }
.fav-body { padding: 20rpx; }
.fav-title {
  font-size: 30rpx; font-weight: 600; color: #333; display: block;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 8rpx;
}
.fav-content {
  font-size: 26rpx; color: #666; line-height: 1.5; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 12rpx;
}
.fav-footer { display: flex; justify-content: space-between; align-items: center; }
.fav-user { display: flex; align-items: center; gap: 8rpx; }
.fav-avatar { width: 40rpx; height: 40rpx; border-radius: 50%; }
.fav-name { font-size: 24rpx; color: #999; }
.fav-actions { display: flex; align-items: center; gap: 16rpx; }
.fav-time { font-size: 22rpx; color: #ccc; }
.fav-unfav { font-size: 24rpx; color: #FF5000; }

.loading-tip { text-align: center; padding: 24rpx 0; color: #999; font-size: 24rpx; }
.no-more { text-align: center; padding: 24rpx 0; color: #ccc; font-size: 22rpx; }
</style>
