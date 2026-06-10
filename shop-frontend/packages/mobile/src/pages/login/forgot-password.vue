<script setup lang="ts">
defineOptions({ name: 'ForgotPasswordPage' })
import { ref } from 'vue'
import { userApi } from '@shop/shared'

const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const step = ref<'email' | 'reset'>('email')
const emailExists = ref<boolean | null>(null)

const form = ref({ email: '', verifyCode: '', newPassword: '', confirmPassword: '' })

const checkEmail = async () => {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) { uni.showToast({ title: '请输入正确的邮箱', icon: 'none' }); return }
  try {
    const exists = await userApi.checkEmail(form.value.email)
    emailExists.value = exists
    if (!exists) { uni.showToast({ title: '该邮箱未注册', icon: 'none' }); return }
    step.value = 'reset'
  } catch { /* handled */ }
}

const sendCode = async () => {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) return
  sending.value = true
  try {
    await userApi.sendEmailCode({ email: form.value.email, type: 'reset' })
    countdown.value = 60
    const timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch { /* handled */ } finally { sending.value = false }
}

const handleReset = async () => {
  if (!form.value.verifyCode || !form.value.newPassword) { uni.showToast({ title: '请填写完整', icon: 'none' }); return }
  if (form.value.newPassword.length < 6) { uni.showToast({ title: '密码至少6位', icon: 'none' }); return }
  if (form.value.newPassword !== form.value.confirmPassword) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return }

  loading.value = true
  try {
    await userApi.resetPassword({
      email: form.value.email,
      verifyCode: form.value.verifyCode,
      newPassword: form.value.newPassword,
    })
    uni.showToast({ title: '密码已重置', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch { /* handled */ } finally { loading.value = false }
}
</script>
<template>
  <view class="fp">
    <view class="card">
      <text class="title">重置密码</text>
      <template v-if="step === 'email'">
        <input v-model="form.email" placeholder="请输入注册邮箱" class="in" />
        <button class="btn" @click="checkEmail">下一步</button>
      </template>
      <template v-else>
        <text class="info">验证码已发送至 {{ form.email }}</text>
        <view class="code-row"><input v-model="form.verifyCode" placeholder="验证码" class="in" /><button class="send-btn" :disabled="countdown > 0 || sending" @click="sendCode">{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</button></view>
        <input v-model="form.newPassword" placeholder="新密码（至少6位）" type="password" class="in" />
        <input v-model="form.confirmPassword" placeholder="确认新密码" type="password" class="in" />
        <button class="btn" :loading="loading" @click="handleReset">重置密码</button>
      </template>
      <text class="link" @click="uni.navigateBack()">返回登录</text>
    </view>
  </view>
</template>
<style scoped lang="scss">
.fp { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #FFF3EC, #F2F2F7); padding: 40rpx }
.card { background: #fff; border-radius: 24rpx; padding: 50rpx 36rpx; width: 100%; max-width: 600rpx }
.title { font-size: 40rpx; font-weight: 700; text-align: center; display: block; margin-bottom: 40rpx }
.info { font-size: 26rpx; color: #999; display: block; margin-bottom: 20rpx }
.in { width: 300px; height: 88rpx; border: 1px solid #E5E5EA; border-radius: 16rpx; padding: 0 24rpx; font-size: 28rpx; margin-bottom: 20rpx }
.code-row { display: flex; gap: 16rpx; .in { flex: 1 } }
.send-btn { width: 200rpx; height: 88rpx; background: #FFF3EC; color: #FF5000; border: none; border-radius: 16rpx; font-size: 24rpx; flex-shrink: 0 }
.btn { width: 100%; height: 88rpx; background: linear-gradient(90deg, #FF5000, #FF9000); color: #fff; border: none; border-radius: 44rpx; font-size: 32rpx; margin-top: 16rpx }
.link { display: block; text-align: center; margin-top: 24rpx; font-size: 26rpx; color: #999 }
</style>
