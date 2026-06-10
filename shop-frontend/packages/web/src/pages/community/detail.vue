<script setup lang="ts">
defineOptions({ name: 'NoteDetailPage' })

import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { communityApi, type NoteDetailResponse, type CommentResponse } from '@shop/shared'

const route = useRoute()
const router = useRouter()
const note = ref<NoteDetailResponse | null>(null)
const loading = ref(true)
const comments = ref<CommentResponse[]>([])
const commentText = ref('')
const replyTo = ref<{ id: number; nickname: string } | null>(null)
const submitting = ref(false)

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    note.value = await communityApi.getNoteDetail(id)
    const cRes = await communityApi.getComments(id, { pageSize: 50 })
    comments.value = ((cRes as any)?.records || []) as CommentResponse[]
  } catch { /* noop */ } finally { loading.value = false }
})

const toggleLike = async () => {
  if (!note.value) return
  try {
    await communityApi.toggleLike(note.value.id)
    note.value.isLiked = !note.value.isLiked
    note.value.likeCount += note.value.isLiked ? 1 : -1
  } catch { /* noop */ }
}

const toggleFavorite = async () => {
  if (!note.value) return
  try {
    await communityApi.toggleFavorite(note.value.id)
    note.value.isFavorited = !note.value.isFavorited
    note.value.favoriteCount += note.value.isFavorited ? 1 : -1
  } catch { /* noop */ }
}

const submitComment = async () => {
  if (!commentText.value.trim() || !note.value) return
  submitting.value = true
  try {
    await communityApi.createComment(note.value.id, {
      content: commentText.value.trim(),
      parentId: replyTo.value?.id,
      replyToUserId: undefined,
    })
    commentText.value = ''
    replyTo.value = null
    const cRes = await communityApi.getComments(note.value.id, { pageSize: 50 })
    comments.value = ((cRes as any)?.records || []) as CommentResponse[]
  } catch { /* noop */ } finally { submitting.value = false }
}

const reply = (c: CommentResponse) => {
  replyTo.value = { id: c.id, nickname: c.userNickname }
  commentText.value = ''
}
</script>

<template>
  <div class="nd-page">
    <button class="nd-back" @click="router.back()">← 返回社区</button>

    <div v-if="loading" class="nd-loading">加载中...</div>

    <template v-else-if="note">
      <!-- 图片 -->
      <img v-if="note.coverUrl || note.images?.length" :src="note.coverUrl || note.images[0]?.imageUrl" class="nd-cover" />

      <!-- 标题与作者 -->
      <div class="nd-header">
        <h2>{{ note.title || '无标题' }}</h2>
        <div class="nd-author">
          <img :src="note.userAvatar || '/default-avatar.svg'" class="nd-avatar" @error="$event.target.src='/default-avatar.svg'" />
          <div>
            <strong>{{ note.userNickname }}</strong>
            <span class="nd-time">{{ note.createTime?.slice(0, 16).replace('T', ' ') }}</span>
          </div>
          <button class="nd-follow" v-if="note.isFollowing === false">+ 关注</button>
        </div>
      </div>

      <!-- 正文 -->
      <div class="nd-content" v-if="note.content">
        <p>{{ note.content }}</p>
      </div>

      <!-- 关联商品 -->
      <div class="nd-products" v-if="note.products?.length">
        <h4>相关商品</h4>
        <div class="nd-product-list">
          <div class="nd-product" v-for="p in note.products" :key="p.id" @click="router.push(`/product/${p.id}`)">
            <img :src="p.mainImage || '/api/v1/files/default/product'" />
            <span class="nd-pname">{{ p.name }}</span>
            <span class="nd-pprice">¥{{ p.salePrice }}</span>
          </div>
        </div>
      </div>

      <!-- 互动栏 -->
      <div class="nd-actions">
        <button class="nd-act" :class="{ active: note.isLiked }" @click="toggleLike">
          {{ note.isLiked ? '❤️' : '🤍' }} {{ note.likeCount }}
        </button>
        <button class="nd-act" :class="{ active: note.isFavorited }" @click="toggleFavorite">
          {{ note.isFavorited ? '⭐' : '☆' }} {{ note.favoriteCount }}
        </button>
        <span class="nd-act">💬 {{ note.commentCount }}</span>
        <span class="nd-act">👁 {{ note.viewCount }}</span>
      </div>

      <!-- 评论区 -->
      <div class="nd-comments">
        <h3>评论 ({{ comments.length }})</h3>

        <!-- 评论输入 -->
        <div class="nd-comment-form">
          <input v-model="commentText" :placeholder="replyTo ? `回复 @${replyTo.nickname}...` : '写下你的评论...'" @keyup.enter="submitComment" />
          <button @click="submitComment" :disabled="submitting || !commentText.trim()">{{ submitting ? '发送中' : '发送' }}</button>
        </div>
        <button class="nd-cancel-reply" v-if="replyTo" @click="replyTo = null; commentText = ''">取消回复</button>

        <!-- 评论列表 -->
        <div class="nd-comment-list" v-if="comments.length">
          <div class="nd-cm" v-for="c in comments" :key="c.id">
            <img :src="c.userAvatar || '/default-avatar.svg'" class="nd-cm-avatar" @error="$event.target.src='/default-avatar.svg'" />
            <div class="nd-cm-body">
              <div class="nd-cm-hd">
                <strong>{{ c.userNickname }}</strong>
                <span class="nd-cm-time">{{ c.createTime?.slice(0, 16).replace('T', ' ') }}</span>
              </div>
              <p class="nd-cm-text">
                <span v-if="c.replyToUserNickname" class="nd-cm-reply">回复 @{{ c.replyToUserNickname }}：</span>
                {{ c.content }}
              </p>
              <button class="nd-cm-reply-btn" @click="reply(c)">回复</button>
            </div>
          </div>
        </div>
        <div class="nd-no-cm" v-else>暂无评论，来说两句吧</div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.nd-page { max-width: 720px; }

.nd-back {
  padding: 6px 16px; border: 1px solid $border-color; background: #fff;
  border-radius: 18px; font-size: $font-size-sm; cursor: pointer;
  color: $text-secondary; margin-bottom: $spacing-lg;
  &:hover { border-color: $brand-orange; color: $brand-orange; }
}

.nd-loading { text-align: center; padding: 80px 0; color: $text-hint; }

// ── 封面 ──
.nd-cover {
  width: 100%; border-radius: $radius-md; margin-bottom: $spacing-lg;
  max-height: 480px; object-fit: cover; background: #f5f5f5;
}

// ── 标题与作者 ──
.nd-header {
  background: $bg-card; border-radius: $radius-md; box-shadow: $shadow-sm;
  padding: $spacing-xl $spacing-2xl; margin-bottom: $spacing-base;
  h2 { margin: 0 0 $spacing-lg; font-size: $font-size-xl; }
}

.nd-author {
  display: flex; align-items: center; gap: $spacing-md;
  strong { font-size: $font-size-base; display: block; }
}

.nd-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }

.nd-time { font-size: $font-size-xs; color: $text-hint; }

.nd-follow {
  margin-left: auto; padding: 5px 16px;
  border: 1px solid $brand-orange; background: #fff;
  color: $brand-orange; border-radius: 16px; font-size: $font-size-xs; cursor: pointer;
  &:hover { background: $brand-orange; color: #fff; }
}

// ── 正文 ──
.nd-content {
  background: $bg-card; border-radius: $radius-md; box-shadow: $shadow-sm;
  padding: $spacing-xl $spacing-2xl; margin-bottom: $spacing-base;
  p { font-size: $font-size-base; line-height: 1.8; color: $text-primary; white-space: pre-wrap; margin: 0; }
}

// ── 关联商品 ──
.nd-products {
  background: $bg-card; border-radius: $radius-md; box-shadow: $shadow-sm;
  padding: $spacing-xl $spacing-2xl; margin-bottom: $spacing-base;
  h4 { margin: 0 0 $spacing-md; font-size: $font-size-base; }
}

.nd-product-list { display: flex; gap: $spacing-md; overflow-x: auto; }

.nd-product {
  flex-shrink: 0; width: 160px; cursor: pointer;
  img { width: 100%; height: 120px; object-fit: cover; border-radius: $radius-base; background: #f5f5f5; }
  .nd-pname { display: block; font-size: $font-size-xs; margin-top: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .nd-pprice { font-size: $font-size-sm; font-weight: 700; color: $brand-orange; }
}

// ── 互动栏 ──
.nd-actions {
  display: flex; gap: $spacing-xl; padding: $spacing-base $spacing-2xl;
  background: $bg-card; border-radius: $radius-md; box-shadow: $shadow-sm;
  margin-bottom: $spacing-base;
}

.nd-act {
  padding: 6px 0; border: none; background: none;
  font-size: $font-size-base; cursor: pointer; color: $text-secondary;
  &.active { color: $brand-orange; }
}

// ── 评论 ──
.nd-comments {
  background: $bg-card; border-radius: $radius-md; box-shadow: $shadow-sm;
  padding: $spacing-xl $spacing-2xl;
  h3 { margin: 0 0 $spacing-lg; }
}

.nd-comment-form {
  display: flex; gap: $spacing-sm; margin-bottom: $spacing-sm;
  input {
    flex: 1; height: 40px; border: 1px solid $border-color; border-radius: 20px;
    padding: 0 16px; font-size: $font-size-sm; outline: none;
    &:focus { border-color: $brand-orange; }
  }
  button {
    padding: 0 20px; border: none; background: $brand-gradient;
    color: #fff; border-radius: 20px; font-size: $font-size-sm; font-weight: 600; cursor: pointer;
    &:disabled { opacity: 0.5; }
  }
}

.nd-cancel-reply {
  border: none; background: none; color: $text-hint; font-size: $font-size-xs;
  cursor: pointer; padding: 0; margin-bottom: $spacing-md;
  &:hover { color: $text-secondary; }
}

.nd-comment-list { margin-top: $spacing-lg; }

.nd-cm {
  display: flex; gap: $spacing-md; padding: $spacing-md 0;
  border-bottom: 1px solid $border-light;
  &:last-child { border-bottom: none; }
}

.nd-cm-avatar {
  width: 36px; height: 36px; border-radius: 50%; object-fit: cover; flex-shrink: 0;
}

.nd-cm-body { flex: 1; min-width: 0; }

.nd-cm-hd {
  display: flex; align-items: center; gap: $spacing-sm; margin-bottom: 4px;
  strong { font-size: $font-size-sm; }
}

.nd-cm-time { font-size: $font-size-xs; color: $text-hint; }

.nd-cm-text {
  font-size: $font-size-base; margin: 0 0 6px; line-height: 1.6;
}

.nd-cm-reply { color: $brand-orange; }

.nd-cm-reply-btn {
  border: none; background: none; color: $text-hint; font-size: $font-size-xs;
  cursor: pointer; padding: 0; &:hover { color: $text-secondary; }
}

.nd-no-cm { text-align: center; padding: 40px 0; color: $text-hint; font-size: $font-size-sm; }
</style>
