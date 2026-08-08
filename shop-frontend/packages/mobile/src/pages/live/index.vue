<script setup lang="ts">
defineOptions({ name: 'LivePage' })
import { ref, onMounted } from 'vue'
import { liveRoomApi, type LiveRoom } from '@shop/shared'
const rooms = ref<LiveRoom[]>([])
const loading = ref(true)
onMounted(async () => {
  try { rooms.value = await liveRoomApi.getRoomList() } catch { /* optional data */ } finally { loading.value = false }
})
const goRoom = (id: string | number) => uni.navigateTo({ url: `/pages/live/room?id=${id}` })
</script>
<template>
  <view class="lv-page">
    <view v-if="loading" class="lv-loading">加载中...</view>
    <view v-else-if="!rooms.length" class="lv-empty">暂无直播</view>
    <view v-else>
      <view class="lv-card" v-for="r in rooms" :key="r.id" @click="goRoom(r.id)">
        <image :src="r.coverImage" mode="aspectFill" class="lv-cover" />
        <view class="lv-info">
          <view class="lv-status" v-if="r.status === 1"><view class="lv-dot" /><text>直播中</text></view>
          <view class="lv-status offline" v-else><text>未开播</text></view>
          <text class="lv-title">{{ r.title }}</text>
          <view class="lv-meta">
            <text class="lv-anchor" v-if="r.anchorNickname">{{ r.anchorNickname }}</text>
            <text class="lv-viewers" v-if="r.onlineCount">{{ r.onlineCount }}人观看</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>
<style scoped lang="scss">
.lv-page { padding: 20rpx; min-height: 100vh; background: #f5f5f5; }
.lv-loading, .lv-empty { text-align: center; padding: 200rpx 0; color: #999; font-size: 28rpx; }
.lv-card { background: #fff; border-radius: 16rpx; overflow: hidden; margin-bottom: 16rpx; }
.lv-cover { width: 100%; height: 400rpx; background: #eee; }
.lv-info { padding: 16rpx; }
.lv-status {
  display: inline-flex; align-items: center; gap: 6rpx;
  font-size: 22rpx; color: #FF3B3B; margin-bottom: 8rpx;
  &.offline { color: #999; }
}
.lv-dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #FF3B3B; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.5} }
.lv-title { font-size: 30rpx; font-weight: 600; color: #333; display: block; margin-bottom: 8rpx; }
.lv-meta { display: flex; align-items: center; gap: 16rpx; }
.lv-anchor { font-size: 24rpx; color: #666; }
.lv-viewers { font-size: 22rpx; color: #999; }
</style>
