<script setup lang="ts">
defineOptions({ name: 'RegisterPage' })
import { ref } from 'vue'
import { userApi } from '@shop/shared'

const mode = ref<'email' | 'phone'>('email')
const form = ref({ contact: '', password: '', confirmPwd: '', verifyCode: '' })
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const emailExists = ref<boolean | null>(null)

let checkTimer: ReturnType<typeof setTimeout> | null = null
const onEmailBlur = () => {
  if (checkTimer) clearTimeout(checkTimer)
  emailExists.value = null
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.contact)) return
  checkTimer = setTimeout(async () => {
    try {
      emailExists.value = await userApi.checkEmail(form.value.contact)
      if (emailExists.value) uni.showToast({ title: '该邮箱已有账号', icon: 'none' })
    } catch { /* handled */ }
  }, 500)
}

const sendCode = async () => {
  if (mode.value === 'email') {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.contact)) { uni.showToast({ title: '请输入正确的邮箱', icon: 'none' }); return }
    sending.value = true
    try {
      await userApi.sendEmailCode({ email: form.value.contact, type: 'register' })
      countdown.value = 60
      const timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
    } catch { /* handled */ } finally { sending.value = false }
  } else {
    if (!/^1[3-9]\d{9}$/.test(form.value.contact)) { uni.showToast({ title: '请输入正确的手机号', icon: 'none' }); return }
    sending.value = true
    try {
      await userApi.sendSmsCode({ phone: form.value.contact, type: 'register' })
      countdown.value = 60
      const timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
    } catch { /* handled */ } finally { sending.value = false }
  }
}

const goLogin = () => uni.redirectTo({ url: '/pages/login/index' })
const handleRegister = async () => {
  if (!form.value.contact || !form.value.verifyCode || !form.value.password) { uni.showToast({ title: '请填写完整', icon: 'none' }); return }
  if (form.value.password.length < 6) { uni.showToast({ title: '密码至少6位', icon: 'none' }); return }
  if (form.value.password !== form.value.confirmPwd) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return }
  loading.value = true
  try {
    const data: Record<string, string> = { password: form.value.password, verifyCode: form.value.verifyCode }
    if (mode.value === 'email') data.email = form.value.contact
    else data.phone = form.value.contact
    await userApi.register(data as any)
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch { /* handled */ } finally { loading.value = false }
}
</script>
<template>
  <view class="rp">
    <view class="card">
      <text class="title">注册账号</text>
      <view class="tabs">
        <text :class="{ on: mode === 'email' }" @click="mode = 'email'; form.contact = ''">邮箱注册</text>
        <text :class="{ on: mode === 'phone' }" @click="mode = 'phone'; form.contact = ''">手机号注册</text>
      </view>
      <input v-if="mode === 'email'" v-model="form.contact" placeholder="邮箱地址" class="in" @blur="onEmailBlur" />
      <input v-else v-model="form.contact" placeholder="手机号" class="in" maxlength="11" />
      <view class="code-row"><input v-model="form.verifyCode" placeholder="验证码" class="in" /><button class="send-btn" :disabled="countdown > 0 || sending || emailExists === true" @click="sendCode">{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</button></view>
      <input v-model="form.password" placeholder="密码（至少6位）" type="password" class="in" />
      <input v-model="form.confirmPwd" placeholder="确认密码" type="password" class="in" />
      <button class="btn" :loading="loading" @click="handleRegister">注册</button>
      <text class="link" @click="goLogin">已有账号？去登录</text>
    </view>
  </view>
</template>
<style scoped lang="scss">
.rp { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #FFF3EC, #F2F2F7); padding: 40rpx }
.card { background: #fff; border-radius: 24rpx; padding: 50rpx 36rpx; width: 100%; max-width: 600rpx }
.title { font-size: 40rpx; font-weight: 700; text-align: center; display: block; margin-bottom: 32rpx }
.tabs { display: flex; gap: 40rpx; justify-content: center; margin-bottom: 32rpx; text { font-size: 28rpx; color: #999; padding-bottom: 8rpx; &.on { color: #FF5000; font-weight: 700; border-bottom: 3rpx solid #FF5000 } } }
.in { width: 300px; height: 88rpx; border: 1px solid #E5E5EA; border-radius: 16rpx; padding: 0 24rpx; font-size: 28rpx; margin-bottom: 20rpx }
.code-row { display: flex; gap: 16rpx; .in { flex: 1 } }
.send-btn { width: 200rpx; height: 88rpx; background: #FFF3EC; color: #FF5000; border: none; border-radius: 16rpx; font-size: 24rpx; flex-shrink: 0 }
.btn { width: 100%; height: 88rpx; background: linear-gradient(90deg, #FF5000, #FF9000); color: #fff; border: none; border-radius: 44rpx; font-size: 32rpx; margin-top: 16rpx }
.link { display: block; text-align: center; margin-top: 24rpx; font-size: 26rpx; color: #999 }
</style>
