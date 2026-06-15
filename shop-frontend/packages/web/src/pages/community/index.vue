<script setup lang="ts">defineOptions({ name: 'CommunityPage' })
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { HeartOutline, Heart, ChatbubbleOutline } from '@vicons/ionicons5'
import { communityApi, type NoteResponse } from '@shop/shared'

const router = useRouter()
const notes = ref<NoteResponse[]>([])
const tab = ref<'recommend' | 'following'>('recommend')

const loadNotes = async () => {
  const res = await communityApi.getNoteList({ pageSize: 20, tab: tab.value })
  notes.value = ((res as any)?.records || []) as NoteResponse[]
}

onMounted(loadNotes)

const goDetail = (id: number) => router.push(`/community/${id}`)
const toggleLike = async (n: NoteResponse) => {
  try { await communityApi.toggleLike(n.id); n.isLiked = !n.isLiked; n.likeCount += n.isLiked ? 1 : -1 } catch { /* need login */ }
}
</script>
<template>
  <div class="cm-page">
    <div class="cm-header">
      <h2>社区</h2>
      <div class="cm-tabs"><span :class="{ active: tab === 'recommend' }" @click="tab='recommend';loadNotes()">推荐</span><span :class="{ active: tab === 'following' }" @click="tab='following';loadNotes()">关注</span></div>
    </div>
    <div class="cm-grid">
      <div class="cm-card" v-for="n in notes" :key="n.id" @click="goDetail(n.id)">
        <div class="cm-card-header"><img :src="n.userAvatar || '/api/v1/files/default/product'" class="cm-avatar" /><span>{{ n.userNickname }}</span></div>
        <img :src="n.coverUrl || n.images?.[0]" class="cm-cover" v-if="n.coverUrl || n.images?.length" />
        <div class="cm-body"><p>{{ n.title || n.content?.slice(0, 100) }}</p></div>
        <div class="cm-actions">
          <span class="cm-action-item" :class="{ liked: n.isLiked }" @click="toggleLike(n)">
            <n-icon :size="16" :color="n.isLiked ? '#FF3B3B' : '#999'"><component :is="n.isLiked ? Heart : HeartOutline" /></n-icon>
            {{ n.likeCount }}
          </span>
          <span class="cm-action-item">
            <n-icon :size="16" color="#999"><ChatbubbleOutline /></n-icon>
            {{ n.commentCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped lang="scss">
.cm-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h2 { margin: 0; } }
.cm-tabs { display: flex; gap: 20px; span { font-size: 14px; color: #666; cursor: pointer; &.active { color: $brand-orange; font-weight: 600; } } }
.cm-grid { columns: 3; column-gap: 16px; @media (max-width: 1024px) { columns: 2; } @media (max-width: 640px) { columns: 1; } }
.cm-card { break-inside: avoid; background: #fff; border-radius: 16px; overflow: hidden; margin-bottom: 16px; }
.cm-card-header { display: flex; align-items: center; gap: 10px; padding: 14px 16px 10px; font-size: 13px; font-weight: 600; }
.cm-avatar { width: 32px; height: 32px; border-radius: 50%; background: #eee; }
.cm-cover { width: 100%; display: block; background: #f5f5f5; }
.cm-body { padding: 10px 16px; p { font-size: 13px; line-height: 1.6; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; } }
.cm-actions { display: flex; gap: 20px; padding: 10px 16px 14px; font-size: 13px; }
.cm-action-item { display: inline-flex; align-items: center; gap: 6px; cursor: pointer; &.liked { animation: pop .3s ease; } }
@keyframes pop { 0% { transform: scale(1); } 50% { transform: scale(1.2); } 100% { transform: scale(1); } }
</style>
