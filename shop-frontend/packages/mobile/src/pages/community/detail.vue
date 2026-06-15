<script setup lang="ts">
defineOptions({ name: 'CommunityDetailPage' })
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { communityApi, type NoteDetailResponse, type CommentResponse, type PageResult } from '@shop/shared'
import { useUserStore } from '@/stores'

const noteId = ref(0)
const note = ref<NoteDetailResponse | null>(null)
const comments = ref<CommentResponse[]>([])
const commentText = ref('')
const replyTo = ref<CommentResponse | null>(null)
const submitting = ref(false)
const loading = ref(true)

onLoad((opts?: any) => { noteId.value = Number(opts?.id); loadDetail(); loadComments() })

const loadDetail = async () => {
  try { note.value = await communityApi.getNoteDetail(noteId.value) } catch { /* noop */ } finally { loading.value = false }
}
const loadComments = async () => {
  const res = await communityApi.getComments(noteId.value, { pageSize: 50 })
  comments.value = ((res as unknown as PageResult<CommentResponse>)?.records || [])
}

const toggleLike = async () => {
  if (!note.value) return
  try { const liked = await communityApi.toggleLike(noteId.value); note.value.isLiked = liked; note.value.likeCount += liked ? 1 : -1 } catch { /* need login */ }
}
const toggleFav = async () => {
  if (!note.value) return
  try { const faved = await communityApi.toggleFavorite(noteId.value); note.value.isFavorited = faved; note.value.favoriteCount += faved ? 1 : -1 } catch { /* need login */ }
}

const onSubmitComment = async () => {
  if (!commentText.value.trim() || submitting.value) return
  submitting.value = true
  try {
    await communityApi.createComment(noteId.value, { content: commentText.value, parentId: replyTo.value?.id })
    commentText.value = ''; replyTo.value = null; uni.showToast({ title:'评论成功', icon:'success' }); loadComments()
  } catch { uni.showToast({ title:'请先登录', icon:'none' }) } finally { submitting.value = false }
}
</script>
<template>
  <view class="cd" v-if="!loading && note">
    <!-- images -->
    <swiper class="sw" circular v-if="note.images?.length"><swiper-item v-for="img in note.images" :key="img.id"><image :src="img.imageUrl" mode="aspectFill"/></swiper-item></swiper>
    <view class="hd"><image :src="note.userAvatar||'/api/v1/files/default/avatar'" class="av"/><view><text class="nn">{{note.userNickname}}</text><text class="time">{{note.createTime}}</text></view></view>
    <view class="body"><text>{{note.content}}</text></view>
    <view class="actions">
      <view class="action-item" :class="{on:note.isLiked}" @click="toggleLike">
        <uni-icons :type="note.isLiked ? 'heart-filled' : 'heart'" size="20" :color="note.isLiked ? '#FF3B3B' : '#999'" />
        <text>{{note.likeCount}}</text>
      </view>
      <view class="action-item" :class="{on:note.isFavorited}" @click="toggleFav">
        <uni-icons :type="note.isFavorited ? 'star-filled' : 'star'" size="20" :color="note.isFavorited ? '#FF9000' : '#999'" />
        <text>{{note.favoriteCount}}</text>
      </view>
      <view class="action-item">
        <uni-icons type="chat" size="20" color="#999" />
        <text>{{note.commentCount}}</text>
      </view>
    </view>

    <view class="comments"><text class="ct">评论 ({{comments.length}})</text>
      <view class="ci" v-for="c in comments" :key="c.id"><image :src="c.userAvatar||'/api/v1/files/default/avatar'" class="cav"/><view class="cbd"><text class="cn">{{c.userNickname}}</text><text class="cc">{{c.content}}</text><text class="cr" @click="replyTo=c;commentText='@'+c.userNickname+' '">回复</text></view></view>
    </view>

    <view class="input-bar"><input v-model="commentText" :placeholder="replyTo?'回复 @'+replyTo.userNickname:'写评论...'" class="ci-in" @confirm="onSubmitComment"/><text v-if="commentText.trim()" @click="onSubmitComment" class="send">{{submitting?'...':'发送'}}</text></view>
  </view>
  <view class="st" v-else-if="loading">加载中...</view>
</template>
<style scoped lang="scss">
.cd{min-height:100vh;background:#fff;padding-bottom:100rpx}.st{text-align:center;padding:200rpx 0;color:#999}
.sw{height:750rpx;image{width:100%;height:100%}}
.hd{display:flex;align-items:center;gap:16rpx;padding:20rpx 24rpx}.av{width:56rpx;height:56rpx;border-radius:50%;background:#eee}.nn{font-size:28rpx;font-weight:600;display:block}.time{font-size:22rpx;color:#999}
.body{padding:0 24rpx 20rpx;text{font-size:30rpx;line-height:1.7}}
.actions{display:flex;gap:40rpx;padding:16rpx 24rpx;border-top:1rpx solid #f0f0f0}
.action-item{display:flex;align-items:center;gap:8rpx;font-size:28rpx;&.on{color:#FF5000}}
.comments{padding:20rpx 24rpx;border-top:1rpx solid #f0f0f0}.ct{font-size:28rpx;font-weight:700;display:block;margin-bottom:16rpx}
.ci{display:flex;gap:12rpx;margin-bottom:20rpx}.cav{width:40rpx;height:40rpx;border-radius:50%;background:#eee;flex-shrink:0}.cbd{flex:1}.cn{font-size:24rpx;color:#999}.cc{font-size:28rpx;margin-top:4rpx}.cr{font-size:22rpx;color:#999;margin-top:4rpx}
.input-bar{position:fixed;bottom:0;left:0;right:0;display:flex;align-items:center;gap:12rpx;padding:12rpx 20rpx;background:#fff;border-top:1rpx solid #f0f0f0}.ci-in{flex:1;height:68rpx;background:#f5f5f5;border-radius:34rpx;padding:0 20rpx;font-size:26rpx}.send{color:#FF5000;font-weight:600;font-size:28rpx;flex-shrink:0}
</style>
