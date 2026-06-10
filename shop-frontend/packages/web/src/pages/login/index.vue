<script setup lang="ts">
defineOptions({ name: 'LoginPage' })

import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import { userApi } from '@shop/shared'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const mode = ref<'pwd' | 'email'>('pwd')
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)

// 密码登录
const pwdForm = ref({ username: '', password: '' })
const handlePwdLogin = async () => {
  if (!pwdForm.value.username || !pwdForm.value.password) return
  loading.value = true
  try {
    await userStore.login(pwdForm.value.username, pwdForm.value.password)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}

// 邮箱验证码登录
const emailForm = ref({ email: '', code: '' })
const sendCode = async () => {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.value.email)) return
  sending.value = true
  try {
    await userApi.sendEmailCode({ email: emailForm.value.email, type: 'login' })
    countdown.value = 60
    const timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch { /* handled */ } finally { sending.value = false }
}
const handleEmailLogin = async () => {
  if (!emailForm.value.email || !emailForm.value.code) return
  loading.value = true
  try {
    await userStore.loginByEmail(emailForm.value.email, emailForm.value.code)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch { /* handled */ } finally { loading.value = false }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h2>登录 ShopMax</h2>
      <p class="sub">品质生活电商平台</p>

      <div class="tabs">
        <span :class="{ on: mode === 'pwd' }" @click="mode = 'pwd'">密码登录</span>
        <span :class="{ on: mode === 'email' }" @click="mode = 'email'">邮箱登录</span>
      </div>

      <template v-if="mode === 'pwd'">
        <input v-model="pwdForm.username" placeholder="用户名/手机号/邮箱" class="input" @keyup.enter="handlePwdLogin" />
        <input v-model="pwdForm.password" type="password" placeholder="密码" class="input" @keyup.enter="handlePwdLogin" />
        <p class="forgot-link"><router-link to="/forgot-password">忘记密码？</router-link></p>
        <button class="login-btn" :disabled="loading" @click="handlePwdLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </template>

      <template v-else>
        <input v-model="emailForm.email" placeholder="邮箱地址" class="input" />
        <div class="code-row">
          <input v-model="emailForm.code" placeholder="验证码" class="input code-input" @keyup.enter="handleEmailLogin" />
          <button class="send-btn" :disabled="countdown > 0 || sending" @click="sendCode">
            {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
          </button>
        </div>
        <button class="login-btn" :disabled="loading" @click="handleEmailLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </template>

      <p class="register-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #FFF3EC, #F2F2F7);
}
.login-card {
  background: #fff; border-radius: $radius-lg; padding: $spacing-3xl;
  width: 400px; box-shadow: $shadow-md; text-align: center;
  h2 { font-size: $font-size-2xl; margin-bottom: $spacing-sm; }
  .sub { color: $text-hint; font-size: $font-size-sm; margin-bottom: $spacing-xl; }
}
.tabs {
  display: flex; gap: $spacing-xl; justify-content: center; margin-bottom: $spacing-lg;
  span {
    font-size: $font-size-sm; color: $text-hint; cursor: pointer; padding-bottom: $spacing-xs;
    border-bottom: 2px solid transparent;
    &.on { color: $brand-orange; font-weight: 600; border-bottom-color: $brand-orange; }
  }
}
.input {
  width: 100%; height: 44px; padding: 0 $spacing-md; margin-bottom: $spacing-base;
  border: 1px solid $border-color; border-radius: $radius-base; outline: none;
  font-size: $font-size-base;
  &:focus { border-color: $brand-orange; }
}
.code-row { display: flex; gap: $spacing-sm; margin-bottom: 0; }
.code-input { flex: 1; }
.send-btn {
  width: 120px; height: 44px; background: #FFF3EC; color: $brand-orange;
  border: none; border-radius: $radius-base; font-size: $font-size-sm; cursor: pointer; flex-shrink: 0;
  &:disabled { opacity: 0.6; }
}
.login-btn {
  width: 100%; height: 44px; background: $brand-gradient; color: #fff;
  border: none; border-radius: 22px; font-size: $font-size-base; font-weight: 600; cursor: pointer;
  margin-top: $spacing-base;
  &:disabled { opacity: 0.6; }
}
.register-link {
  margin-top: $spacing-lg; font-size: $font-size-sm; color: $text-hint;
  a { color: $brand-orange; text-decoration: none; }
}
.forgot-link {
  text-align: right; margin: -$spacing-xs 0 $spacing-sm;
  a { font-size: $font-size-sm; color: $brand-orange; text-decoration: none; }
}
</style>
