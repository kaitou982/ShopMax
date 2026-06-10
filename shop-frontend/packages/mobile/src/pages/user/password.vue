<script setup lang="ts">
defineOptions({ name: 'ChangePasswordPage' })
import { ref } from 'vue'
import { userApi } from '@shop/shared'

const form = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const loading = ref(false)

const submit = async () => {
  if (!form.value.oldPassword || !form.value.newPassword) { uni.showToast({ title:'请填写完整', icon:'none' }); return }
  if (form.value.newPassword.length < 6) { uni.showToast({ title:'新密码至少6位', icon:'none' }); return }
  if (form.value.newPassword !== form.value.confirmPassword) { uni.showToast({ title:'两次密码不一致', icon:'none' }); return }
  loading.value = true
  try {
    await userApi.changePassword(form.value)
    uni.showToast({ title:'修改成功', icon:'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch { /* handled */ } finally { loading.value = false }
}
</script>
<template>
  <view class="pp">
    <view class="card">
      <text class="title">修改密码</text>
      <input v-model="form.oldPassword" placeholder="旧密码" type="password" class="in" />
      <input v-model="form.newPassword" placeholder="新密码（至少6位）" type="password" class="in" />
      <input v-model="form.confirmPassword" placeholder="确认新密码" type="password" class="in" />
      <button class="btn" :loading="loading" @click="submit">确认修改</button>
    </view>
  </view>
</template>
<style scoped lang="scss">
.pp { min-height:100vh;display:flex;align-items:center;justify-content:center;background:#f5f5f5;padding:40rpx }
.card { background:#fff;border-radius:24rpx;padding:50rpx 36rpx;width:100%;max-width:600rpx }
.title { font-size:40rpx;font-weight:700;text-align:center;display:block;margin-bottom:40rpx }
.in { width:100%;height:88rpx;border:1px solid #E5E5EA;border-radius:16rpx;padding:0 24rpx;font-size:28rpx;margin-bottom:20rpx }
.btn { width:100%;height:88rpx;background:linear-gradient(90deg,#FF5000,#FF9000);color:#fff;border:none;border-radius:44rpx;font-size:32rpx;margin-top:16rpx }
</style>
