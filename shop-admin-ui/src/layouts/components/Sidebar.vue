<script setup lang="ts">
defineOptions({ name: 'Sidebar' })

import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/modules/app'
import { useUserStore } from '@/stores/modules/user'
import { routes } from '@/router/routes'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const menuList = computed(() => {
  const layoutRoute = routes.find((r) => r.name === 'Layout')
  if (!layoutRoute?.children) return []
  return layoutRoute.children.filter((item) => {
    const routeRoles = item.meta?.roles as string[] | undefined
    if (!routeRoles) return true
    return routeRoles.includes(userStore.userRole)
  })
})
</script>

<template>
  <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
    <!-- Logo Area -->
    <div class="logo-area">
      <div class="logo-icon">
        <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="36" height="36" rx="10" fill="url(#logo-grad)" />
          <path d="M10 18.5L16 24L26 12" stroke="#fff" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round" />
          <defs>
            <linearGradient id="logo-grad" x1="0" y1="0" x2="36" y2="36">
              <stop stop-color="#FF5000" />
              <stop offset="1" stop-color="#FF8C3A" />
            </linearGradient>
          </defs>
        </svg>
      </div>
      <transition name="fade-slide">
        <div v-if="!appStore.sidebarCollapsed" class="logo-text">
          <span class="logo-brand">Shop</span><span class="logo-highlight">Max</span>
          <span class="logo-subtitle">商家中心</span>
        </div>
      </transition>
    </div>

    <!-- Navigation Menu -->
    <nav class="sidebar-nav">
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        router
        background-color="transparent"
        text-color="#9898A8"
        active-text-color="#FFFFFF"
      >
        <el-menu-item
          v-for="item in menuList"
          :key="item.path"
          :index="item.path"
          class="nav-item"
        >
          <el-icon>
            <component :is="item.meta?.icon" />
          </el-icon>
          <template #title>
            <span class="nav-label">{{ item.meta?.title }}</span>
          </template>
        </el-menu-item>
      </el-menu>
    </nav>

    <!-- Collapse Toggle -->
    <div class="sidebar-footer" @click="appStore.toggleSidebar()">
      <div class="collapse-btn">
        <svg
          viewBox="0 0 20 20"
          fill="none"
          class="collapse-icon"
          :class="{ rotated: appStore.sidebarCollapsed }"
        >
          <path d="M7 5L12 10L7 15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </div>
  </aside>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: $sidebar-width;
  background: linear-gradient(180deg, #1A1A2E 0%, #16213E 100%);
  display: flex;
  flex-direction: column;
  z-index: $z-sidebar;
  transition: width $transition-slow;
  overflow: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.04);

  &.collapsed {
    width: $sidebar-collapsed-width;
  }
}

// ── Logo Area ────────────────────────────────
.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;

  svg {
    width: 100%;
    height: 100%;
  }
}

.logo-text {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0;
  overflow: hidden;
  white-space: nowrap;

  .logo-brand {
    font-size: 18px;
    font-weight: 700;
    color: #FFFFFF;
    letter-spacing: -0.01em;
  }

  .logo-highlight {
    font-size: 18px;
    font-weight: 700;
    color: $brand-orange;
  }

  .logo-subtitle {
    font-size: 10px;
    color: rgba(255, 255, 255, 0.35);
    letter-spacing: 0.08em;
    margin-left: 2px;
    text-transform: uppercase;
  }
}

// ── Navigation ───────────────────────────────
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 8px;

  &::-webkit-scrollbar {
    width: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.08);
    border-radius: 2px;
  }
}

:deep(.el-menu) {
  border-right: none;
  background: transparent;

  .el-menu-item {
    height: 44px;
    line-height: 44px;
    margin-bottom: 4px;
    border-radius: $radius-md;
    font-size: $font-size-base;
    font-weight: 500;
    transition: all $transition-fast;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: $brand-gradient;
      opacity: 0;
      transition: opacity $transition-base;
      border-radius: $radius-md;
      z-index: -1;
    }

    &:hover {
      color: $sidebar-text-active !important;
      background: rgba(255, 80, 0, 0.12);

      .el-icon {
        color: $brand-orange;
      }
    }

    &.is-active {
      color: $sidebar-text-active !important;
      background: transparent;

      &::before {
        opacity: 1;
      }

      .el-icon {
        color: #FFFFFF;
      }

      // Active indicator dot
      &::after {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        background: #FFFFFF;
        border-radius: 0 3px 3px 0;
        box-shadow: 0 0 8px rgba(255, 255, 255, 0.4);
      }
    }

    .el-icon {
      font-size: 18px;
      transition: color $transition-fast;
      color: $sidebar-text;
    }
  }

  // Collapsed state adjustments
  &.el-menu--collapse {
    width: 100%;

    .el-menu-item {
      padding: 0 !important;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: $radius-md;
      margin: 0 auto 4px;
      width: 44px;
      height: 44px;
      min-width: 0;

      .el-icon {
        margin: 0;
      }
    }
  }
}

.nav-label {
  font-size: $font-size-base;
  letter-spacing: 0.01em;
}

// ── Footer / Collapse Button ─────────────────
.sidebar-footer {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: rgba(255, 255, 255, 0.04);

    .collapse-icon {
      color: $brand-orange;
    }
  }
}

.collapse-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-sm;
  transition: background $transition-fast;
}

.collapse-icon {
  width: 18px;
  height: 18px;
  color: rgba(255, 255, 255, 0.4);
  transition: transform $transition-slow, color $transition-fast;

  &.rotated {
    transform: rotate(180deg);
  }
}

// ── Transitions ──────────────────────────────
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all $transition-base;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}
</style>
