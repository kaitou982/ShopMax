<script setup lang="ts">
defineOptions({ name: 'LivePage' })
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { EyeOutline, PersonOutline } from '@vicons/ionicons5'
import { liveRoomApi, type LiveRoom } from '@shop/shared'

const router = useRouter()

const rooms = ref<LiveRoom[]>([])
const loading = ref(true)
const statusLabels: Record<number, string> = { 0: '预告', 1: '直播中', 2: '已结束', 3: '已关闭', 4: '待推流' }
const statusColors: Record<number, string> = { 0: '#FF8F1F', 1: '#FF3B3B', 2: '#999', 3: '#999', 4: '#409EFF' }

onMounted(async () => {
  try {
    rooms.value = await liveRoomApi.getRoomList()
  } catch {
    /* noop */
  }
  loading.value = false
})
</script>
<template>
  <div class="lv-page">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <h2>直播</h2>
    <div class="lv-grid" v-if="!loading">
      <div class="lv-card" v-for="r in rooms" :key="r.id" @click="router.push(`/live/${r.id}`)">
        <div class="lv-cover-wrap">
          <img :src="r.cover || '/api/v1/files/default/product'" class="lv-cover" />
          <span class="lv-status" :style="{ background: statusColors[r.status] }">{{ statusLabels[r.status] }}</span>
          <span class="lv-viewers" v-if="r.status === 1">
            <n-icon :size="12" color="#fff"><EyeOutline /></n-icon>
            {{ r.onlineCount }}
          </span>
        </div>
        <div class="lv-body">
          <div class="lv-title">{{ r.title }}</div>
          <div class="lv-info">
            <span class="lv-av">{{ r.anchorNickname?.[0] || '?' }}</span>
            <span>{{ r.anchorNickname || '主播' }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="state" v-if="loading">加载中...</div>
    <div class="state" v-else-if="!rooms.length">暂无直播间</div>
  </div>
</template>
<style scoped lang="scss">
.back-btn { display: inline-block; padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; margin-bottom: 12px; color: #666; &:hover { border-color: $brand-orange; color: $brand-orange; } }
h2 { margin-bottom: 20px; }
.lv-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; @media (max-width: 1024px) { grid-template-columns: repeat(3, 1fr); } @media (max-width: 640px) { grid-template-columns: repeat(2, 1fr); } }
.lv-card { background: #fff; border-radius: 16px; overflow: hidden; transition: all .15s; cursor: pointer; &:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,.08); } }
.lv-cover-wrap { position: relative; }
.lv-cover { width: 100%; aspect-ratio: 16/10; object-fit: cover; background: #f5f5f5; }
.lv-status { position: absolute; top: 8px; left: 8px; padding: 2px 10px; border-radius: 4px; font-size: 11px; color: #fff; font-weight: 600; }
.lv-viewers { position: absolute; bottom: 8px; right: 8px; padding: 2px 8px; border-radius: 8px; font-size: 11px; color: #fff; background: rgba(0,0,0,.5); display: flex; align-items: center; gap: 4px; }
.lv-body { padding: 12px; }
.lv-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lv-info { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #999; }
.lv-av { width: 20px; height: 20px; border-radius: 50%; background: linear-gradient(135deg,#e74c3c,#f39c12); color: #fff; font-size: 11px; display: flex; align-items: center; justify-content: center; }
.state { text-align: center; padding: 80px 0; color: #999; }
</style>
