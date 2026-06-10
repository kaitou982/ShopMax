<script setup lang="ts">
/**
 * 登录页面
 * @description 用户登录页面，带鼠标跟随动画效果
 * @author shop
 * @since 2026-04-15
 */
defineOptions({ name: 'LoginPage' })

import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import { login } from '@/api/modules/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const formRef = ref()

// 鼠标跟随动画相关
const mouseX = ref(0)
const mouseY = ref(0)
const boxTransform = ref('')
const bgTransform = ref('')
const particles = ref<Array<{ x: number; y: number; vx: number; vy: number; size: number; opacity: number }>>([])

// 初始化粒子
const initParticles = () => {
  particles.value = Array.from({ length: 30 }, () => ({
    x: Math.random() * window.innerWidth,
    y: Math.random() * window.innerHeight,
    vx: (Math.random() - 0.5) * 0.5,
    vy: (Math.random() - 0.5) * 0.5,
    size: Math.random() * 4 + 2,
    opacity: Math.random() * 0.5 + 0.2
  }))
}

// 动画帧
let animationFrame: number
const animateParticles = () => {
  particles.value = particles.value.map(p => {
    let newX = p.x + p.vx
    let newY = p.y + p.vy

    if (newX < 0 || newX > window.innerWidth) p.vx *= -1
    if (newY < 0 || newY > window.innerHeight) p.vy *= -1

    return { ...p, x: newX, y: newY }
  })
  animationFrame = requestAnimationFrame(animateParticles)
}

// 鼠标移动处理
const handleMouseMove = (e: MouseEvent) => {
  const { clientX, clientY } = e
  const centerX = window.innerWidth / 2
  const centerY = window.innerHeight / 2

  mouseX.value = clientX
  mouseY.value = clientY

  // 计算偏移量（反向移动产生视差效果）
  const offsetX = (clientX - centerX) / centerX
  const offsetY = (clientY - centerY) / centerY

  // 登录框跟随鼠标轻微移动（反向）
  boxTransform.value = `translate(${-offsetX * 15}px, ${-offsetY * 15}px) perspective(1000px) rotateY(${offsetX * 3}deg) rotateX(${-offsetY * 3}deg)`

  // 背景反向移动
  bgTransform.value = `translate(${offsetX * 30}px, ${offsetY * 30}px) scale(1.1)`
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  initParticles()
  animateParticles()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  cancelAnimationFrame(animationFrame)
})

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login({
        username: loginForm.username,
        password: loginForm.password
      })
      userStore.setToken(res.token)
      userStore.setUserInfo({
        userId: res.userId,
        username: res.username,
        nickname: res.nickname,
        avatar: res.avatar,
        email: '',
        phone: res.phone,
        gender: res.gender,
        status: 1,
        memberLevel: res.memberLevel,
        memberLevelName: res.memberLevelName,
        integral: res.integral,
        balance: res.balance,
        growthValue: res.growthValue,
        role: res.role || '',
        storeStatus: res.storeStatus,
        storeName: res.storeName
      })

      if (res.role !== 'ADMIN' && res.role !== 'STORE') {
        userStore.logout()
        ElMessage.error('该账号无权限登录管理后台')
        return
      }

      ElMessage.success('登录成功')
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error) {
      // 错误已在拦截器处理
    } finally {
      loading.value = false
    }
  })
}

</script>

<template>
  <div class="login-page" @mousemove="handleMouseMove">
    <!-- 动态背景 -->
    <div class="animated-bg" :style="{ transform: bgTransform }">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 浮动粒子 -->
    <div class="particles">
      <div
        v-for="(p, i) in particles"
        :key="i"
        class="particle"
        :style="{
          left: `${p.x}px`,
          top: `${p.y}px`,
          width: `${p.size}px`,
          height: `${p.size}px`,
          opacity: p.opacity
        }"
      ></div>
    </div>

    <!-- 登录框 -->
    <div class="login-box" :style="{ transform: boxTransform }">
      <div class="logo-section">
        <div class="logo">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 3h18v18H3V3zm2 2v14h14V5H5zm2 2h4v4H7V7zm6 0h4v4h-4V7zm-6 6h4v4H7v-4zm6 0h4v4h-4v-4z" fill="currentColor"/>
          </svg>
        </div>
        <h2 class="title">欢迎回来</h2>
        <p class="subtitle">登录 ShopMax 电商管理系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            立即登录
          </el-button>
        </el-form-item>
      </el-form>

    </div>

    <!-- 鼠标光效 -->
    <div
      class="cursor-glow"
      :style="{
        left: `${mouseX}px`,
        top: `${mouseY}px`
      }"
    ></div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

// 动态背景
.animated-bg {
  position: fixed;
  inset: -50px;
  z-index: 0;
  transition: transform 0.3s ease-out;

  .gradient-orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.6;
    animation: float 20s infinite ease-in-out;

    &.orb-1 {
      width: 600px;
      height: 600px;
      background: radial-gradient(circle, #667eea 0%, transparent 70%);
      top: -200px;
      left: -100px;
      animation-delay: 0s;
    }

    &.orb-2 {
      width: 500px;
      height: 500px;
      background: radial-gradient(circle, #764ba2 0%, transparent 70%);
      bottom: -150px;
      right: -100px;
      animation-delay: -7s;
    }

    &.orb-3 {
      width: 400px;
      height: 400px;
      background: radial-gradient(circle, #f093fb 0%, transparent 70%);
      top: 50%;
      left: 50%;
      animation-delay: -14s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(50px, -50px) scale(1.1);
  }
  50% {
    transform: translate(0, 50px) scale(0.95);
  }
  75% {
    transform: translate(-50px, -25px) scale(1.05);
  }
}

// 浮动粒子
.particles {
  position: fixed;
  inset: 0;
  z-index: 1;
  pointer-events: none;

  .particle {
    position: absolute;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
  }
}

// 登录框
.login-box {
  position: relative;
  z-index: 10;
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow:
    0 25px 50px -12px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.05) inset;
  transition: transform 0.15s ease-out;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 24px;
    padding: 1px;
    background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 50%, rgba(255,255,255,0.1) 100%);
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    pointer-events: none;
  }
}

// Logo区域
.logo-section {
  text-align: center;
  margin-bottom: 32px;

  .logo {
    width: 80px;
    height: 80px;
    margin: 0 auto 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
    animation: pulse 2s infinite;
    color: #fff;

    svg {
      width: 40px;
      height: 40px;
    }
  }

  .title {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 8px;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  }

  .subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.6);
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 10px 40px rgba(102, 126, 234, 0.6);
  }
}

// 登录表单
.login-form {
  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    box-shadow: none;
    transition: all 0.3s ease;

    &:hover, &:focus-within {
      background: rgba(255, 255, 255, 0.12);
      border-color: rgba(102, 126, 234, 0.5);
      box-shadow: 0 0 20px rgba(102, 126, 234, 0.2);
    }

    .el-input__inner {
      color: #fff;

      &::placeholder {
        color: rgba(255, 255, 255, 0.4);
      }
    }

    .el-input__icon {
      color: rgba(255, 255, 255, 0.5);
    }
  }

  :deep(.el-form-item__error) {
    color: #ff6b6b;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 30px rgba(102, 126, 234, 0.5);
  }

  &:active {
    transform: translateY(0);
  }
}

// 注册链接
// 鼠标光效
.cursor-glow {
  position: fixed;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 5;
  transform: translate(-50%, -50%);
  transition: left 0.1s ease-out, top 0.1s ease-out;
}
</style>
