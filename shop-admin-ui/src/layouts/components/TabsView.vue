<script setup lang="ts">
defineOptions({ name: 'TabsView' })

import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTabsStore, type TabItem } from '@/stores/modules/tabs'

const router = useRouter()
const route = useRoute()
const tabsStore = useTabsStore()

const scrollContainer = ref<HTMLElement>()
const showLeftArrow = ref(false)
const showRightArrow = ref(false)

// Context menu state
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextTabPath = ref('')

const contextMenuItems = computed(() => {
  const tab = tabsStore.tabs.find((t) => t.path === contextTabPath.value)
  const isAffix = tab?.affix ?? false
  const tabCount = tabsStore.tabs.length
  const idx = tabsStore.tabs.findIndex((t) => t.path === contextTabPath.value)

  return [
    { label: '关闭当前', action: 'close', disabled: isAffix },
    { label: '关闭其他', action: 'closeOther', disabled: tabCount <= 1 || (tabCount === 2 && !isAffix && tabsStore.tabs.some((t) => t.affix)) },
    { label: '关闭左侧', action: 'closeLeft', disabled: idx <= tabsStore.tabs.filter((t) => t.affix).length },
    { label: '关闭右侧', action: 'closeRight', disabled: idx >= tabCount - 1 },
    { label: '关闭全部', action: 'closeAll', disabled: tabCount <= tabsStore.tabs.filter((t) => t.affix).length }
  ]
})

const updateArrowVisibility = () => {
  const el = scrollContainer.value
  if (!el) return
  showLeftArrow.value = el.scrollLeft > 0
  showRightArrow.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 1
}

const scrollLeft = () => {
  scrollContainer.value?.scrollBy({ left: -200, behavior: 'smooth' })
}

const scrollRight = () => {
  scrollContainer.value?.scrollBy({ left: 200, behavior: 'smooth' })
}

const handleTabClick = (tab: TabItem) => {
  tabsStore.setActiveTab(tab.path)
  router.push(tab.path)
}

const handleTabClose = (e: Event, path: string) => {
  e.stopPropagation()
  const isActive = tabsStore.activeTab === path
  tabsStore.removeTab(path)
  if (!isActive) return
  router.push(tabsStore.activeTab)
}

const handleContextMenu = (e: MouseEvent, path: string) => {
  e.preventDefault()
  contextTabPath.value = path
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuVisible.value = true
}

const handleContextAction = (action: string) => {
  const path = contextTabPath.value
  switch (action) {
    case 'close':
      tabsStore.removeTab(path)
      if (path === route.path) router.push(tabsStore.activeTab)
      break
    case 'closeOther':
      tabsStore.closeOtherTabs(path)
      router.push(tabsStore.activeTab)
      break
    case 'closeLeft':
      tabsStore.closeLeftTabs(path)
      router.push(tabsStore.activeTab)
      break
    case 'closeRight':
      tabsStore.closeRightTabs(path)
      router.push(tabsStore.activeTab)
      break
    case 'closeAll':
      tabsStore.closeAllTabs()
      router.push(tabsStore.activeTab)
      break
  }
  contextMenuVisible.value = false
}

const handleWheel = (e: WheelEvent) => {
  const el = e.currentTarget as HTMLElement
  el.scrollLeft += e.deltaY
}

const hideContextMenu = () => {
  contextMenuVisible.value = false
}

watch(() => tabsStore.activeTab, async () => {
  await nextTick()
  const el = scrollContainer.value
  if (!el) return
  const activeEl = el.querySelector('.tab-item.active') as HTMLElement
  if (activeEl) {
    activeEl.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'smooth' })
  }
})

watch(() => tabsStore.tabs.length, async () => {
  await nextTick()
  updateArrowVisibility()
})

onMounted(() => {
  updateArrowVisibility()
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
})
</script>

<template>
  <div class="tabs-view">
    <!-- Left scroll arrow -->
    <transition name="arrow-fade">
      <div v-if="showLeftArrow" class="scroll-arrow scroll-arrow--left" @click="scrollLeft">
        <el-icon><ArrowLeft /></el-icon>
      </div>
    </transition>

    <!-- Tab items -->
    <div
      ref="scrollContainer"
      class="tabs-scroll"
      @scroll="updateArrowVisibility"
      @wheel.prevent="handleWheel"
    >
      <div
        v-for="tab in tabsStore.tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: tabsStore.activeTab === tab.path }"
        @click="handleTabClick(tab)"
        @contextmenu="handleContextMenu($event, tab.path)"
      >
        <el-icon class="tab-icon">
          <component :is="tab.icon" />
        </el-icon>
        <span class="tab-title">{{ tab.title }}</span>
        <span
          v-if="!tab.affix"
          class="tab-close"
          @click="(e) => handleTabClose(e, tab.path)"
        >
          <el-icon><Close /></el-icon>
        </span>
      </div>
    </div>

    <!-- Right scroll arrow -->
    <transition name="arrow-fade">
      <div v-if="showRightArrow" class="scroll-arrow scroll-arrow--right" @click="scrollRight">
        <el-icon><ArrowRight /></el-icon>
      </div>
    </transition>

    <!-- Right-click context menu -->
    <teleport to="body">
      <transition name="context-fade">
        <div
          v-if="contextMenuVisible"
          class="context-menu"
          :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
          @click.stop
        >
          <div
            v-for="item in contextMenuItems"
            :key="item.action"
            class="context-menu-item"
            :class="{ disabled: item.disabled }"
            @click="!item.disabled && handleContextAction(item.action)"
          >
            {{ item.label }}
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.tabs-view {
  display: flex;
  align-items: center;
  height: 40px;
  background: $bg-white;
  border-bottom: 1px solid $border-color-light;
  position: relative;
  flex-shrink: 0;
}

.tabs-scroll {
  flex: 1;
  display: flex;
  align-items: stretch;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  scrollbar-width: none;
  height: 100%;

  &::-webkit-scrollbar {
    display: none;
  }
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  height: 100%;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  border-right: 1px solid $border-color-lighter;
  background: $bg-color-page;
  transition: all $transition-fast;
  position: relative;
  flex-shrink: 0;
  user-select: none;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: $brand-gradient;
    transform: scaleX(0);
    transition: transform $transition-fast;
  }

  &:hover {
    color: $text-primary;
    background: $bg-white;

    .tab-close {
      opacity: 1;
    }
  }

  &.active {
    color: $brand-orange;
    background: $bg-white;
    font-weight: 600;

    &::after {
      transform: scaleX(1);
    }

    .tab-icon {
      color: $brand-orange;
    }
  }
}

.tab-icon {
  font-size: 15px;
  flex-shrink: 0;
}

.tab-title {
  font-size: $font-size-sm;
  line-height: 1;
}

.tab-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  font-size: 10px;
  opacity: 0;
  transition: all $transition-fast;
  margin-left: 2px;
  flex-shrink: 0;

  &:hover {
    background: $danger-light;
    color: $danger-color;
  }
}

// ── Scroll arrows ──────────────────────────────
.scroll-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 100%;
  flex-shrink: 0;
  cursor: pointer;
  color: $text-secondary;
  background: $bg-white;
  transition: all $transition-fast;
  z-index: 2;

  &:hover {
    color: $brand-orange;
    background: $primary-lighter;
  }

  &--left {
    border-right: 1px solid $border-color-light;
  }

  &--right {
    border-left: 1px solid $border-color-light;
  }
}

.arrow-fade-enter-active,
.arrow-fade-leave-active {
  transition: opacity 0.15s ease;
}

.arrow-fade-enter-from,
.arrow-fade-leave-to {
  opacity: 0;
}

// ── Context menu ───────────────────────────────
.context-menu {
  position: fixed;
  z-index: 3000;
  min-width: 120px;
  background: $bg-white;
  border: 1px solid $border-color-light;
  border-radius: $radius-md;
  box-shadow: $shadow-lg;
  padding: 4px;
}

.context-menu-item {
  padding: 8px 16px;
  font-size: $font-size-sm;
  color: $text-primary;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $primary-light;
    color: $brand-orange;
  }

  &.disabled {
    color: $text-placeholder;
    cursor: not-allowed;

    &:hover {
      background: transparent;
      color: $text-placeholder;
    }
  }
}

.context-fade-enter-active,
.context-fade-leave-active {
  transition: opacity 0.12s ease, transform 0.12s ease;
}

.context-fade-enter-from,
.context-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
