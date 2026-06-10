<script setup lang="ts">
defineOptions({ name: 'RegisterPage' })

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@shop/shared'

const router = useRouter()
const mode = ref<'email' | 'phone'>('email')
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)

const form = ref({ contact: '', password: '', confirmPwd: '', verifyCode: '' })
const emailExists = ref<boolean | null>(null)

let checkTimer: ReturnType<typeof setTimeout> | null = null
const onEmailBlur = () => {
  if (checkTimer) clearTimeout(checkTimer)
  emailExists.value = null
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.contact)) return
  checkTimer = setTimeout(async () => {
    try {
      emailExists.value = await userApi.checkEmail(form.value.contact)
    } catch { /* handled */ }
  }, 500)
}

const sendCode = async () => {
  if (mode.value === 'email') {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.contact)) return
    sending.value = true
    try {
      await userApi.sendEmailCode({ email: form.value.contact, type: 'register' })
      countdown.value = 60
      const timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
    } catch { /* handled */ } finally { sending.value = false }
  } else {
    if (!/^1[3-9]\d{9}$/.test(form.value.contact)) return
    sending.value = true
    try {
      await userApi.sendSmsCode({ phone: form.value.contact, type: 'register' })
      countdown.value = 60
      const timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
    } catch { /* handled */ } finally { sending.value = false }
  }
}

const handleRegister = async () => {
  if (!form.value.contact || !form.value.verifyCode || !form.value.password) return
  if (form.value.password.length < 6) return
  if (form.value.password !== form.value.confirmPwd) return

  loading.value = true
  try {
    const data: Record<string, string> = {
      password: form.value.password,
      verifyCode: form.value.verifyCode,
    }
    if (mode.value === 'email') {
      data.email = form.value.contact
    } else {
      data.phone = form.value.contact
    }
    await userApi.register(data as any)
    router.push('/login')
  } catch { /* handled */ } finally { loading.value = false }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <h2>注册 ShopMax</h2>
      <p class="sub">品质生活电商平台</p>

      <div class="tabs">
        <span :class="{ on: mode === 'email' }" @click="mode = 'email'; form.contact = ''">邮箱注册</span>
        <span :class="{ on: mode === 'phone' }" @click="mode = 'phone'; form.contact = ''">手机号注册</span>
      </div>

      <div v-if="mode === 'email'" class="input-wrap">
        <input v-model="form.contact" placeholder="邮箱地址" class="input" @blur="onEmailBlur" />
        <p v-if="emailExists === true" class="hint-error">该邮箱已有账号</p>
      </div>
      <input v-else v-model="form.contact" placeholder="手机号" class="input" maxlength="11" />

      <div class="code-row">
        <input v-model="form.verifyCode" placeholder="验证码" class="input code-input" />
        <button class="send-btn" :disabled="countdown > 0 || sending || emailExists === true" @click="sendCode">
          {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
        </button>
      </div>

      <input v-model="form.password" type="password" placeholder="密码（至少6位）" class="input" />
      <input v-model="form.confirmPwd" type="password" placeholder="确认密码" class="input" @keyup.enter="handleRegister" />

      <button class="register-btn" :disabled="loading" @click="handleRegister">
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <p class="login-link">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.register-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #FFF3EC, #F2F2F7);
}
.register-card {
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
.input-wrap { margin-bottom: 0; }
.hint-error { color: #FF3B30; font-size: $font-size-sm; margin: -$spacing-xs 0 $spacing-sm; text-align: left; }
.code-row { display: flex; gap: $spacing-sm; margin-bottom: 0; }
.code-input { flex: 1; }
.send-btn {
  width: 120px; height: 44px; background: #FFF3EC; color: $brand-orange;
  border: none; border-radius: $radius-base; font-size: $font-size-sm; cursor: pointer; flex-shrink: 0;
  &:disabled { opacity: 0.6; }
}
.register-btn {
  width: 100%; height: 44px; background: $brand-gradient; color: #fff;
  border: none; border-radius: 22px; font-size: $font-size-base; font-weight: 600; cursor: pointer;
  margin-top: $spacing-base;
  &:disabled { opacity: 0.6; }
}
.login-link {
  margin-top: $spacing-lg; font-size: $font-size-sm; color: $text-hint;
  a { color: $brand-orange; text-decoration: none; }
}
</style>
