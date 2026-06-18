<script setup lang="ts">
defineOptions({ name: 'ProfilePage' })
import { ref } from 'vue'
import { useUserStore } from '@/stores'
const userStore = useUserStore()
const form = ref({
  nickname: userStore.userInfo?.nickname || '',
  avatar: userStore.userInfo?.avatar || '',
  gender: userStore.userInfo?.gender || 0,
  birthday: userStore.userInfo?.birthday || '',
})
const saving = ref(false)

const genderOptions = ['保密', '男', '女']
const onGenderChange = (e: any) => { form.value.gender = e.detail.value }

const save = async () => {
  if (!form.value.nickname.trim()) { uni.showToast({ title: '请输入昵称', icon: 'none' }); return }
  saving.value = true
  try {
    await userStore.updateUserInfo(form.value)
    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch { uni.showToast({ title: '保存失败', icon: 'none' }) } finally { saving.value = false }
}
</script>
<template>
  <view class="pf-page">
    <view class="pf-item">
      <text>头像</text>
      <view class="pf-avatar-wrap">
        <image :src="form.avatar || '/static/default-avatar.png'" class="pf-avatar" mode="aspectFill" />
        <input v-model="form.avatar" class="pf-url-input" placeholder="输入头像URL" />
      </view>
    </view>
    <view class="pf-item">
      <text>昵称</text>
      <input v-model="form.nickname" placeholder="请输入昵称" />
    </view>
    <view class="pf-item">
      <text>性别</text>
      <picker :range="genderOptions" :value="form.gender" @change="onGenderChange">
        <text class="pf-picker-val">{{ genderOptions[form.gender] || '保密' }}</text>
      </picker>
    </view>
    <view class="pf-item">
      <text>生日</text>
      <picker mode="date" :value="form.birthday" @change="(e: any) => form.birthday = e.detail.value">
        <text class="pf-picker-val">{{ form.birthday || '请选择' }}</text>
      </picker>
    </view>
    <button class="pf-btn" :loading="saving" @click="save">保存</button>
  </view>
</template>
<style scoped lang="scss">
.pf-page { padding: 20rpx; }
.pf-item {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 24rpx; margin-bottom: 2rpx; font-size: 28rpx;
  input { text-align: right; flex: 1; }
}
.pf-avatar-wrap { display: flex; align-items: center; gap: 16rpx; }
.pf-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; background: #f0f0f0; }
.pf-url-input { font-size: 24rpx; color: #999; width: 300rpx; text-align: right; }
.pf-picker-val { color: #333; }
.pf-btn {
  width: 90%; height: 80rpx; background: linear-gradient(90deg,#FF5000,#FF9000);
  color: #fff; border: none; border-radius: 40rpx; margin: 40rpx auto; font-size: 30rpx;
}
</style>
