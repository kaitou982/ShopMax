<script setup lang="ts">defineOptions({ name: 'ProfilePage' })
import { ref } from 'vue'
import { useUserStore } from '@/stores'
const userStore = useUserStore()
const form = ref({ nickname: userStore.userInfo?.nickname || '', avatar: userStore.userInfo?.avatar || '', gender: userStore.userInfo?.gender || 0 })
const saving = ref(false)
const save = async () => { saving.value = true; try { await userStore.updateUserInfo(form.value); uni.showToast({ title:'保存成功',icon:'success' }) } catch {} finally { saving.value = false } }
</script>
<template>
  <view class="pf-page"><view class="pf-item"><text>昵称</text><input v-model="form.nickname" /></view>
    <view class="pf-item"><text>头像 URL</text><input v-model="form.avatar" /></view>
    <button class="pf-btn" :loading="saving" @click="save">保存</button></view>
</template>
<style scoped lang="scss">.pf-page { padding: 20rpx; } .pf-item { display: flex; justify-content: space-between; align-items: center; background: #fff; padding: 24rpx; margin-bottom: 2rpx; font-size: 28rpx; input { text-align: right; } } .pf-btn { width: 90%; height: 80rpx; background: linear-gradient(90deg,#FF5000,#FF9000); color: #fff; border: none; border-radius: 40rpx; margin: 40rpx auto; font-size: 30rpx; }</style>
