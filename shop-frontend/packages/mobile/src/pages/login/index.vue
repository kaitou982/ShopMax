<script setup lang="ts">defineOptions({ name: 'LoginPage' })
import { ref } from 'vue'
import { userApi } from '@shop/shared'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const mode = ref<'pwd' | 'sms' | 'email'>('pwd')
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)

// Password login
const pwdForm = ref({ username: '', password: '' })
const handlePwdLogin = async () => {
  if (!pwdForm.value.username || !pwdForm.value.password) { uni.showToast({ title: '请输入用户名和密码', icon: 'none' }); return }
  loading.value = true
  try { await userStore.login(pwdForm.value.username, pwdForm.value.password); uni.switchTab({ url: '/pages/user/index' }) }
  catch { /* handled */ } finally { loading.value = false }
}

// SMS login
const smsForm = ref({ phone: '', code: '' })
const sendSmsCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(smsForm.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return }
  sending.value = true
  try {
    await userApi.sendSmsCode({ phone: smsForm.value.phone, type: 'login' })
    countdown.value = 60
    const t = setInterval(() => { if (--countdown.value <= 0) clearInterval(t) }, 1000)
  } catch { /* handled */ } finally { sending.value = false }
}
const handleSmsLogin = async () => {
  if (!smsForm.value.phone || !smsForm.value.code) { uni.showToast({ title: '请输入手机号和验证码', icon: 'none' }); return }
  loading.value = true
  try { await userStore.loginByPhone(smsForm.value.phone, smsForm.value.code); uni.switchTab({ url: '/pages/user/index' }) }
  catch { /* handled */ } finally { loading.value = false }
}

// Email login
const emailForm = ref({ email: '', code: '' })
const sendEmailCode = async () => {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.value.email)) { uni.showToast({ title: '请输入正确邮箱', icon: 'none' }); return }
  sending.value = true
  try {
    await userApi.sendEmailCode({ email: emailForm.value.email, type: 'login' })
    countdown.value = 60
    const t = setInterval(() => { if (--countdown.value <= 0) clearInterval(t) }, 1000)
  } catch { /* handled */ } finally { sending.value = false }
}
const handleEmailLogin = async () => {
  if (!emailForm.value.email || !emailForm.value.code) { uni.showToast({ title: '请输入邮箱和验证码', icon: 'none' }); return }
  loading.value = true
  try { await userStore.loginByEmail(emailForm.value.email, emailForm.value.code); uni.switchTab({ url: '/pages/user/index' }) }
  catch { /* handled */ } finally { loading.value = false }
}

const goRegister = () => uni.navigateTo({ url: '/pages/login/register' })
const goForgotPassword = () => uni.navigateTo({ url: '/pages/login/forgot-password' })
</script>
<template>
  <view class="lp">
    <view class="card">
      <text class="title">登录 ShopMax</text>
      <view class="tabs">
        <text :class="{ on: mode === 'pwd' }" @click="mode = 'pwd'">密码登录</text>
        <text :class="{ on: mode === 'sms' }" @click="mode = 'sms'; countdown = 0">手机登录</text>
        <text :class="{ on: mode === 'email' }" @click="mode = 'email'; countdown = 0">邮箱登录</text>
      </view>

      <template v-if="mode === 'pwd'">
        <input v-model="pwdForm.username" placeholder="用户名" class="in" style="width: 300px"/>
        <input v-model="pwdForm.password" placeholder="密码" type="password" class="in" />
        <text class="forgot" @click="goForgotPassword">忘记密码？</text>
        <button class="btn" :loading="loading" @click="handlePwdLogin">登录</button>
      </template>

      <template v-else-if="mode === 'sms'">
        <input v-model="smsForm.phone" placeholder="手机号" class="in" maxlength="11" />
        <view class="code-row"><input v-model="smsForm.code" placeholder="验证码" class="in" /><button class="send-btn" :disabled="countdown > 0 || sending" @click="sendSmsCode">{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</button></view>
        <button class="btn" :loading="loading" @click="handleSmsLogin">登录</button>
      </template>

      <template v-else>
        <input v-model="emailForm.email" placeholder="邮箱地址" class="in" />
        <view class="code-row"><input v-model="emailForm.code" placeholder="验证码" class="in" /><button class="send-btn" :disabled="countdown > 0 || sending" @click="sendEmailCode">{{ countdown > 0 ? countdown + 's' : '获取验证码' }}</button></view>
        <button class="btn" :loading="loading" @click="handleEmailLogin">登录</button>
      </template>

      <text class="link" @click="goRegister">还没有账号？立即注册</text>
    </view>
  </view>
</template>
<style scoped lang="scss">
.lp { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #FFF3EC, #F2F2F7); padding: 40rpx }
.card { background: #fff; border-radius: 24rpx; padding: 50rpx 36rpx; width: 100%; max-width: 600rpx }
.title { font-size: 40rpx; font-weight: 700; text-align: center; display: block; margin-bottom: 32rpx }
.tabs { display: flex; gap: 32rpx; justify-content: center; margin-bottom: 32rpx; text { font-size: 28rpx; color: #999; padding-bottom: 8rpx; &.on { color: #FF5000; font-weight: 700; border-bottom: 3rpx solid #FF5000 } } }
.in { width: 300px; height: 88rpx; border: 1px solid #E5E5EA; border-radius: 16rpx; padding: 0 24rpx; font-size: 28rpx; margin-bottom: 20rpx }
.code-row { display: flex; gap: 16rpx; .in { flex: 1 } }
.send-btn { width: 200rpx; height: 88rpx; background: #FFF3EC; color: #FF5000; border: none; border-radius: 16rpx; font-size: 24rpx; flex-shrink: 0 }
.btn { width: 100%; height: 88rpx; background: linear-gradient(90deg, #FF5000, #FF9000); color: #fff; border: none; border-radius: 44rpx; font-size: 32rpx; margin-top: 12rpx }
.forgot { display: block; text-align: right; margin: -8rpx 0 16rpx; font-size: 26rpx; color: #FF5000 }
.link { display: block; text-align: center; margin-top: 24rpx; font-size: 26rpx; color: #999 }
</style>
