<script setup lang="ts">defineOptions({ name: 'StoreApplyPage' })
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores'
import { userApi } from '@shop/shared'
const userStore = useUserStore()
const form = ref({ storeName: '', storeLogo: '', storeDescription: '' })
const submitting = ref(false)
const submit = async () => {
  if (!form.value.storeName) { uni.showToast({ title: '请输入店铺名称', icon: 'none' }); return }
  submitting.value = true
  try { await userApi.applyStore(form.value); await userStore.fetchUserInfo(); uni.showToast({ title: '申请已提交', icon: 'success' }) }
  catch {} finally { submitting.value = false }
}
onMounted(() => userStore.fetchUserInfo())
</script>
<template>
  <view class="sa-page">
    <view v-if="userStore.storeStatus === 1" style="text-align:center;padding:120rpx 0"><text style="font-size:80rpx">✅</text><text style="display:block;margin-top:24rpx;font-size:32rpx;font-weight:700">店铺已通过审核</text></view>
    <view v-else-if="userStore.storeStatus === 0" style="text-align:center;padding:120rpx 0"><text style="font-size:80rpx">⏳</text><text style="display:block;margin-top:24rpx;font-size:32rpx">审核中，请耐心等待</text></view>
    <view v-else-if="userStore.storeStatus === 2" style="text-align:center;padding:120rpx 0"><text style="font-size:80rpx">❌</text><text style="display:block;margin-top:24rpx;font-size:32rpx;font-weight:700">申请已被拒绝</text><text style="display:block;margin-top:12rpx;font-size:26rpx;color:#999">可修改信息后重新提交</text><button class="sa-btn" @click="submit" style="margin-top:32rpx">重新提交</button></view>
    <template v-else>
      <view class="sa-item"><text>店铺名称</text><input v-model="form.storeName" placeholder="请输入" /></view>
      <view class="sa-item"><text>Logo URL</text><input v-model="form.storeLogo" placeholder="可选" /></view>
      <view class="sa-item"><text>简介</text><input v-model="form.storeDescription" placeholder="可选" /></view>
      <button class="sa-btn" :loading="submitting" @click="submit">提交申请</button>
    </template>
  </view>
</template>
<style scoped lang="scss">.sa-page { padding: 20rpx; } .sa-item { background: #fff; padding: 24rpx; margin-bottom: 2rpx; display: flex; justify-content: space-between; font-size: 28rpx; input { text-align: right; } } .sa-btn { width: 90%; height: 80rpx; background: linear-gradient(90deg,#FF5000,#FF9000); color: #fff; border: none; border-radius: 40rpx; margin: 40rpx auto; font-size: 30rpx; }</style>
