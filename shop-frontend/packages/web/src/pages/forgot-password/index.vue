<script setup lang="ts">
defineOptions({ name: 'ForgotPasswordPage' })

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@shop/shared'

const router = useRouter()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const step = ref<'email' | 'reset'>('email')
const emailExists = ref<boolean | null>(null)

const form = ref({ email: '', verifyCode: '', newPassword: '', confirmPassword: '' })

const checkEmail = async () => {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) return
  try {
    const exists = await userApi.checkEmail(form.value.email)
    emailExists.value = exists
    if (!exists) return
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
  if (!form.value.verifyCode || !form.value.newPassword) return
  if (form.value.newPassword.length < 6) return
  if (form.value.newPassword !== form.value.confirmPassword) return

  loading.value = true
  try {
    await userApi.resetPassword({
      email: form.value.email,
      verifyCode: form.value.verifyCode,
      newPassword: form.value.newPassword,
    })
    router.push('/login')
  } catch { /* handled */ } finally { loading.value = false }
}
</script>

<template>
  <div class="fp-page">
    <div class="fp-card">
      <h2>重置密码</h2>
      <p class="sub">通过邮箱验证码重置您的密码</p>

      <template v-if="step === 'email'">
        <input v-model="form.email" placeholder="请输入注册邮箱" class="input" @keyup.enter="checkEmail" />
        <p v-if="emailExists === false" class="hint-error">该邮箱未注册</p>
        <button class="fp-btn" @click="checkEmail">下一步</button>
      </template>

      <template v-else>
        <p class="email-info">已发送验证码至 {{ form.email }}</p>
        <div class="code-row">
          <input v-model="form.verifyCode" placeholder="验证码" class="input code-input" />
          <button class="send-btn" :disabled="countdown > 0 || sending" @click="sendCode">
            {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
          </button>
        </div>
        <input v-model="form.newPassword" type="password" placeholder="新密码（至少6位）" class="input" />
        <input v-model="form.confirmPassword" type="password" placeholder="确认新密码" class="input" @keyup.enter="handleReset" />
        <button class="fp-btn" :disabled="loading" @click="handleReset">
          {{ loading ? '重置中...' : '重置密码' }}
        </button>
      </template>

      <p class="login-link"><router-link to="/login">返回登录</router-link></p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.fp-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #FFF3EC, #F2F2F7);
}
.fp-card {
  background: #fff; border-radius: $radius-lg; padding: $spacing-3xl;
  width: 400px; box-shadow: $shadow-md; text-align: center;
  h2 { font-size: $font-size-2xl; margin-bottom: $spacing-sm; }
  .sub { color: $text-hint; font-size: $font-size-sm; margin-bottom: $spacing-xl; }
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
.fp-btn {
  width: 100%; height: 44px; background: $brand-gradient; color: #fff;
  border: none; border-radius: 22px; font-size: $font-size-base; font-weight: 600; cursor: pointer;
  margin-top: $spacing-base;
  &:disabled { opacity: 0.6; }
}
.hint-error { color: #FF3B30; font-size: $font-size-sm; margin: -$spacing-xs 0 $spacing-sm; text-align: left; }
.email-info { color: $text-hint; font-size: $font-size-sm; margin-bottom: $spacing-base; text-align: left; }
.login-link {
  margin-top: $spacing-lg; font-size: $font-size-sm; color: $text-hint;
  a { color: $brand-orange; text-decoration: none; }
}
</style>
