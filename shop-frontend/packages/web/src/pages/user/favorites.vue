<script setup lang="ts">
defineOptions({ name: 'MyFavoritesPage' })

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { communityApi, type NoteResponse } from '@shop/shared'

const router = useRouter()
const favorites = ref<NoteResponse[]>([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await communityApi.getMyFavorites({ pageNum: 1, pageSize: 50 })
    favorites.value = res?.records || []
  } catch { /* noop */ } finally { loading.value = false }
}

onMounted(load)

const goDetail = (id: number) => router.push(`/community/${id}`)

const unfavorite = async (note: NoteResponse) => {
  if (!confirm('确定取消收藏该笔记？')) return
  try {
    await communityApi.toggleFavorite(note.id)
    favorites.value = favorites.value.filter(f => f.id !== note.id)
  } catch { /* noop */ }
}

const formatTime = (t: string) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<template>
  <div class="fav-page">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <h2>我的收藏</h2>

    <div v-if="loading" class="state">加载中...</div>
    <div v-else-if="!favorites.length" class="state">还没有收藏的笔记</div>

    <div class="fav-grid" v-else>
      <div class="fav-card" v-for="n in favorites" :key="n.id" @click="goDetail(n.id)">
        <img v-if="n.coverUrl || n.images?.length" :src="n.coverUrl || n.images[0]" class="fav-img" />
        <div class="fav-body">
          <div class="fav-title">{{ n.title || n.content?.slice(0, 60) || '无标题' }}</div>
          <div class="fav-footer">
            <span class="fav-user">{{ n.userNickname || '用户' }}</span>
            <span class="fav-time">{{ formatTime(n.createTime) }}</span>
          </div>
          <button class="fav-unfav" @click.stop="unfavorite(n)">取消收藏</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.fav-page { max-width: 900px; }

.back-btn {
  display: inline-block; padding: 6px 14px; border: 1px solid #ddd;
  border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer;
  margin-bottom: 12px; color: #666;
  &:hover { border-color: $brand-orange; color: $brand-orange; }
}

h2 { margin-bottom: 20px; }
.state { text-align: center; padding: 80px 0; color: #999; }

.fav-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
  @media (max-width: 1024px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 640px) { grid-template-columns: 1fr; }
}

.fav-card {
  background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer;
  transition: transform .15s;
  &:hover { transform: translateY(-3px); box-shadow: 0 4px 16px rgba(0,0,0,.08); }
}

.fav-img { width: 100%; aspect-ratio: 1; object-fit: cover; background: #f5f5f5; }

.fav-body { padding: 12px 16px; }

.fav-title {
  font-size: 14px; font-weight: 600; line-height: 1.4;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; margin-bottom: 8px; color: #333;
}

.fav-footer { display: flex; justify-content: space-between; font-size: 12px; color: #999; margin-bottom: 8px; }

.fav-time { color: #bbb; }

.fav-unfav {
  width: 100%; padding: 6px 0; border: 1px solid #FF5000; background: #fff;
  color: #FF5000; border-radius: 16px; font-size: 12px; cursor: pointer;
  &:hover { background: #FF5000; color: #fff; }
}
</style>
