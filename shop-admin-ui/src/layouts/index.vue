<script setup lang="ts">
defineOptions({ name: 'Layout' })

import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import TabsView from './components/TabsView.vue'
import { useAppStore } from '@/stores/modules/app'
import { useUserStore } from '@/stores/modules/user'
import { useNotificationStore } from '@/stores/modules/notification'

const appStore = useAppStore()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

onMounted(() => {
  userStore.initUser()
  if (userStore.isLoggedIn) {
    notificationStore.startPolling()
  }
})
</script>

<template>
  <div class="app-layout">
    <Sidebar />
    <div class="main-area" :class="{ collapsed: appStore.sidebarCollapsed }">
      <Navbar />
      <TabsView />
      <main class="content-area">
        <RouterView v-slot="{ Component, route }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </RouterView>
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.app-layout {
  display: flex;
  min-height: 100vh;
  background: $bg-color-page;
}

.main-area {
  flex: 1;
  margin-left: $sidebar-width;
  min-width: 0;
  transition: margin-left $transition-slow;

  &.collapsed {
    margin-left: $sidebar-collapsed-width;
  }
}

.content-area {
  padding: $content-padding;
  min-height: calc(100vh - #{$navbar-height} - 40px);
}

// ── Page Transitions ─────────────────────────
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
