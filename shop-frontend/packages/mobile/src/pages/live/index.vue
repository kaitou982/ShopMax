<script setup lang="ts">defineOptions({ name: 'LivePage' })
import { ref, onMounted } from 'vue'
import { liveRoomApi, type LiveRoom } from '@shop/shared'
const rooms = ref<LiveRoom[]>([])
onMounted(async () => { rooms.value = await liveRoomApi.getRoomList() })
</script>
<template>
  <view class="lv-page"><view class="lv-card" v-for="r in rooms" :key="r.id"><image :src="r.coverImage" mode="aspectFill" class="lv-cover" /><text class="lv-title">{{ r.title }}</text></view></view>
</template>
<style scoped lang="scss">.lv-page { padding: 20rpx; } .lv-card { background: #fff; border-radius: 16rpx; overflow: hidden; margin-bottom: 16rpx; } .lv-cover { width: 100%; height: 400rpx; background: #eee; } .lv-title { font-size: 28rpx; padding: 16rpx; display: block; }</style>
