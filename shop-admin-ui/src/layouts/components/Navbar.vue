<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Fold, Expand } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import Breadcrumb from './Breadcrumb.vue'
import { useAppStore } from '@/stores/modules/app'
import { useUserStore } from '@/stores/modules/user'

defineOptions({ name: 'Navbar' })

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const roleLabel = computed(() => {
  switch (userStore.userRole) {
    case 'ADMIN': return '管理员'
    case 'STORE': return '店家'
    default: return ''
  }
})

const goProfile = () => {
  router.push('/profile')
}

const handleLogout = () => {
  userStore.logout()
  window.location.reload()
}

// 控制图标切换
const isFullScreen = ref(false)

// 切换全屏
const toggleFullScreen = async () => {
  try {
    if (!document.fullscreenElement) {
      await document.documentElement.requestFullscreen()
      isFullScreen.value = true
    } else {
      await document.exitFullscreen()
      isFullScreen.value = false
    }
  } catch (err) {
    console.log('全屏失败：', err)
  }
}
// 监听用户按 ESC 退出，自动同步图标
const updateFullScreenStatus = () => {
  isFullScreen.value = !!document.fullscreenElement
}

onMounted(() => {
  document.addEventListener('fullscreenchange', updateFullScreenStatus)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', updateFullScreenStatus)
})

</script>

<template>
  <header class="navbar">
    <div class="navbar-left">
      <button class="toggle-btn" @click="appStore.toggleSidebar()" :title="appStore.sidebarCollapsed ? '展开菜单' : '折叠菜单'">
        <el-icon :size="18">
          <Fold v-if="!appStore.sidebarCollapsed" />
          <Expand v-else />
        </el-icon>
      </button>
      <Breadcrumb />
    </div>

    <div class="navbar-right">
      <!-- Quick actions -->
      <div class="quick-actions">
        <button class="action-btn" title="消息通知">
          <el-badge :value="3" :max="99" class="badge">
            <svg viewBox="0 0 20 20" fill="none" width="18" height="18">
              <path d="M15 6.5a4.5 4.5 0 1 1-9 0 4.5 4.5 0 0 1 9 0Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M3 13.5A3.5 3.5 0 0 1 6.5 10h8a3.5 3.5 0 0 1 3.5 3.5V16a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-2.5Z" stroke="currentColor" stroke-width="1.5"/>
            </svg>
          </el-badge>
        </button>
        <button class="action-btn" title="全屏/退出全屏" @click="toggleFullScreen">
          <!-- 进入全屏图标 -->
          <svg v-if="!isFullScreen" viewBox="0 0 20 20" fill="none" width="18" height="18">
            <path d="M3 7V3h4M13 3h4v4M17 13v4h-4M7 17H3v-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>

          <!-- 退出全屏图标 -->
          <svg v-else width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M7 0V7H-6.70552e-08" stroke="#818181"/>
            <path d="M13 20V13H20" stroke="#818181"/>
            <path d="M0 13H7V20" stroke="#818181"/>
            <path d="M20 7H13V-6.70552e-08" stroke="#818181"/>
          </svg>

        </button>
      </div>

      <el-dropdown trigger="click" popper-class="user-dropdown">
        <div class="user-info">
          <el-avatar :size="34" :src="userStore.avatar" class="user-avatar">
            {{ userStore.userName?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="username">{{ userStore.userName || '管理员' }}</span>
          <span class="role-tag" :class="userStore.userRole.toLowerCase()">{{ roleLabel }}</span>
          <span v-if="userStore.isStore && userStore.storeStatus === 0" class="audit-tag">审核中</span>
          <svg viewBox="0 0 16 16" fill="none" width="14" height="14" class="chevron">
            <path d="M4 6L8 10L12 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goProfile">
              <svg viewBox="0 0 16 16" fill="none" width="14" height="14" style="margin-right: 8px;">
                <circle cx="8" cy="6" r="3" stroke="currentColor" stroke-width="1.3"/>
                <path d="M3 13.5A4.5 4.5 0 0 1 7.5 9h1A4.5 4.5 0 0 1 13 13.5V14H3v-.5Z" stroke="currentColor" stroke-width="1.3"/>
              </svg>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item @click="goProfile">
              <svg viewBox="0 0 16 16" fill="none" width="14" height="14" style="margin-right: 8px;">
                <rect x="1.5" y="5.5" width="13" height="9" rx="1.5" stroke="currentColor" stroke-width="1.3"/>
                <circle cx="8" cy="10" r="1.5" stroke="currentColor" stroke-width="1.3"/>
                <path d="M8 2.5V5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
              </svg>
              修改密码
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <svg viewBox="0 0 16 16" fill="none" width="14" height="14" style="margin-right: 8px;">
                <path d="M6 3H3.5A1.5 1.5 0 0 0 2 4.5v8A1.5 1.5 0 0 0 3.5 14H6M11 11l3-3-3-3M14 8H6" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.navbar {
  height: $navbar-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: $navbar-bg;
  box-shadow: $navbar-shadow;
  position: sticky;
  top: 0;
  z-index: $z-navbar;
  backdrop-filter: blur(12px);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: $radius-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $primary-light;
    color: $brand-orange;
  }
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

// ── Quick Actions ────────────────────────────
.quick-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-right: 12px;
  padding-right: 12px;
  border-right: 1px solid $border-color-light;
}

.action-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: $radius-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  

  &:hover {
    background: #F5F5F7;
    color: $text-primary;
  }

  .badge {
    :deep(.el-badge__content) {
      font-size: 10px;
      height: 16px;
      line-height: 16px;
      padding: 0 5px;
    }
  }
}

// ── User Info ────────────────────────────────
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 4px 4px;
  border-radius: $radius-full;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: #F5F5F7;
  }

  .username {
    font-size: $font-size-base;
    font-weight: 500;
    color: $text-primary;
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .chevron {
    color: $text-placeholder;
    transition: transform $transition-fast;
  }

  &:hover .chevron {
    transform: translateY(1px);
  }
}

.user-avatar {
  border: 2px solid transparent;
  background: $brand-gradient;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

.role-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  line-height: 18px;
  &.admin {
    background: rgba(255, 80, 0, 0.1);
    color: #FF5000;
  }
  &.store {
    background: rgba(64, 158, 255, 0.1);
    color: #409EFF;
  }
}

.audit-tag {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  line-height: 16px;
  background: rgba(230, 162, 60, 0.12);
  color: #E6A23C;
}
</style>
