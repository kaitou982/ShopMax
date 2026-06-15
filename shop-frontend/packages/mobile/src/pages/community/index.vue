<script setup lang="ts">defineOptions({ name: 'CommunityPage' })
import { ref, onMounted } from 'vue'
import { communityApi, type NoteResponse } from '@shop/shared'
const notes = ref<NoteResponse[]>([]); const tab = ref<'recommend'|'following'>('recommend')
const load = async () => { const r = await communityApi.getNoteList({pageSize:20,tab:tab.value}); notes.value = ((r as any)?.records||[]) }
onMounted(load)
const toggleLike = async (n:NoteResponse) => { try{await communityApi.toggleLike(n.id);n.isLiked=!n.isLiked;n.likeCount+=n.isLiked?1:-1}catch{} }
const goDetail = (id: number) => uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
const goPublish = () => uni.navigateTo({ url: '/pages/community/publish' })
</script>
<template>
  <view class="cm"><view class="cm-tabs"><text :class="{on:tab==='recommend'}" @click="tab='recommend';load()">推荐</text><text :class="{on:tab==='following'}" @click="tab='following';load()">关注</text></view>
  <view class="cm-list"><view class="cm-card" v-for="n in notes" :key="n.id" @click="goDetail(n.id)">
    <view class="cm-hd"><image :src="n.userAvatar||'/api/v1/files/default/avatar'" class="cm-av"/><text class="cm-nn">{{n.userNickname}}</text></view>
    <image v-if="n.coverUrl||n.images?.length" :src="n.coverUrl||n.images[0]" mode="aspectFill" class="cm-cover"/>
    <view class="cm-body"><text>{{n.title||n.content?.slice(0,120)}}</text></view>
    <view class="cm-ft">
      <view class="cm-ft-item" :class="{liked:n.isLiked}" @click.stop="toggleLike(n)">
        <uni-icons :type="n.isLiked ? 'heart-filled' : 'heart'" size="18" :color="n.isLiked ? '#FF3B3B' : '#999'" />
        <text>{{n.likeCount}}</text>
      </view>
      <view class="cm-ft-item">
        <uni-icons type="chat" size="18" color="#999" />
        <text>{{n.commentCount}}</text>
      </view>
    </view>
  </view></view></view>
  <view class="fab" @click="goPublish">
    <uni-icons type="plusempty" size="28" color="#fff" />
  </view>
</template>
<style scoped lang="scss">
.cm{min-height:100vh;background:#f5f5f5}.cm-tabs{display:flex;gap:40rpx;padding:20rpx 30rpx;background:#fff;position:sticky;top:0;z-index:10;text{font-size:28rpx;color:#666;&.on{color:#FF5000;font-weight:700}}}
.cm-list{padding:12rpx 20rpx}.cm-card{background:#fff;border-radius:16rpx;overflow:hidden;margin-bottom:16rpx}
.cm-hd{display:flex;align-items:center;gap:12rpx;padding:16rpx}.cm-av{width:48rpx;height:48rpx;border-radius:50%;background:#f0f0f0}.cm-nn{font-size:26rpx;font-weight:600}
.cm-cover{width:100%;height:500rpx;background:#f0f0f0}
.cm-body{padding:12rpx 16rpx;text{font-size:28rpx;line-height:1.6;color:#333}}
.cm-ft{display:flex;gap:32rpx;padding:12rpx 16rpx 20rpx;font-size:26rpx}
.cm-ft-item{display:flex;align-items:center;gap:8rpx;&.liked{animation:pop .3s ease}}
@keyframes pop{0%,100%{transform:scale(1)}50%{transform:scale(1.2)}}
.fab{position:fixed;right:40rpx;bottom:140rpx;width:96rpx;height:96rpx;border-radius:50%;background:#000;display:flex;align-items:center;justify-content:center;box-shadow:0 8rpx 24rpx rgba(0,0,0,.2);z-index:100}
</style>
