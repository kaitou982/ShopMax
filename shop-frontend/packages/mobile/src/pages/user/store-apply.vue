<script setup lang="ts">
defineOptions({ name: 'StoreApplyPage' })
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores'
import { userApi } from '@shop/shared'
const userStore = useUserStore()
const form = ref({ storeName: '', storeLogo: '', storeDescription: '' })
const submitting = ref(false)
const showForm = ref(false)

const status = computed(() => userStore.storeStatus)

const startReapply = () => {
  form.value = { storeName: '', storeLogo: '', storeDescription: '' }
  showForm.value = true
}

const submit = async () => {
  if (!form.value.storeName.trim()) { uni.showToast({ title: '请输入店铺名称', icon: 'none' }); return }
  submitting.value = true
  try {
    await userApi.applyStore(form.value)
    await userStore.fetchUserInfo()
    showForm.value = false
    uni.showToast({ title: '申请已提交', icon: 'success' })
  } catch { /* handled */ } finally { submitting.value = false }
}

onMounted(() => userStore.fetchUserInfo())
</script>
<template>
  <view class="sa-page">
    <!-- 已通过 -->
    <view v-if="status === 1" class="sa-status">
      <text class="sa-emoji">✅</text>
      <text class="sa-status-title">店铺已通过审核</text>
    </view>

    <!-- 审核中 -->
    <view v-else-if="status === 0" class="sa-status">
      <text class="sa-emoji">⏳</text>
      <text class="sa-status-title">审核中，请耐心等待</text>
    </view>

    <!-- 被拒绝 + 重新提交表单 -->
    <view v-else-if="status === 2 && showForm">
      <view class="sa-status" style="padding:40rpx 0">
        <text style="font-size:28rpx;color:#FF3B3B">上次申请被拒绝，请修改后重新提交</text>
      </view>
      <view class="sa-item"><text>店铺名称</text><input v-model="form.storeName" placeholder="请输入" /></view>
      <view class="sa-item"><text>Logo URL</text><input v-model="form.storeLogo" placeholder="可选" /></view>
      <view class="sa-item"><text>简介</text><input v-model="form.storeDescription" placeholder="可选" /></view>
      <button class="sa-btn" :loading="submitting" @click="submit">重新提交</button>
    </view>

    <!-- 被拒绝 + 提示页 -->
    <view v-else-if="status === 2" class="sa-status">
      <text class="sa-emoji">❌</text>
      <text class="sa-status-title">申请已被拒绝</text>
      <text class="sa-status-hint">可修改信息后重新提交</text>
      <button class="sa-btn" @click="startReapply">重新申请</button>
    </view>

    <!-- 首次申请 -->
    <template v-else>
      <view class="sa-item"><text>店铺名称</text><input v-model="form.storeName" placeholder="请输入" /></view>
      <view class="sa-item"><text>Logo URL</text><input v-model="form.storeLogo" placeholder="可选" /></view>
      <view class="sa-item"><text>简介</text><input v-model="form.storeDescription" placeholder="可选" /></view>
      <button class="sa-btn" :loading="submitting" @click="submit">提交申请</button>
    </template>
  </view>
</template>
<style scoped lang="scss">
.sa-page { padding: 20rpx; min-height: 100vh; background: #f5f5f5; }
.sa-status { text-align: center; padding: 120rpx 0; }
.sa-emoji { font-size: 80rpx; display: block; }
.sa-status-title { font-size: 32rpx; font-weight: 700; display: block; margin-top: 24rpx; }
.sa-status-hint { font-size: 26rpx; color: #999; display: block; margin-top: 12rpx; }
.sa-item { background: #fff; padding: 24rpx; margin-bottom: 2rpx; display: flex; justify-content: space-between; font-size: 28rpx; input { text-align: right; } }
.sa-btn { width: 90%; height: 80rpx; background: linear-gradient(90deg,#FF5000,#FF9000); color: #fff; border: none; border-radius: 40rpx; margin: 40rpx auto; font-size: 30rpx; }
</style>
