import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'

export interface TabItem {
  path: string
  name: string
  title: string
  icon?: string
  affix?: boolean
}

const AFFIX_TABS: TabItem[] = [
  { path: '/dashboard', name: 'Dashboard', title: '首页', icon: 'HomeFilled', affix: true }
]

export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref<TabItem[]>([...AFFIX_TABS])
  const activeTab = ref('/dashboard')

  const tabPaths = computed(() => tabs.value.map((t) => t.path))

  const addTab = (route: RouteLocationNormalized) => {
    const path = route.path
    const title = (route.meta?.title as string) || ''
    const icon = (route.meta?.icon as string) || ''
    const name = (route.name as string) || ''

    if (tabPaths.value.includes(path)) {
      activeTab.value = path
      return
    }

    tabs.value.push({ path, name, title, icon })
    activeTab.value = path
  }

  const removeTab = (path: string) => {
    const tab = tabs.value.find((t) => t.path === path)
    if (tab?.affix) return

    const idx = tabs.value.findIndex((t) => t.path === path)
    if (idx === -1) return

    tabs.value.splice(idx, 1)

    if (activeTab.value === path) {
      const next = tabs.value[idx] || tabs.value[idx - 1]
      if (next) {
        activeTab.value = next.path
      }
    }
  }

  const closeOtherTabs = (path: string) => {
    tabs.value = tabs.value.filter((t) => t.affix || t.path === path)
    activeTab.value = path
  }

  const closeLeftTabs = (path: string) => {
    const idx = tabs.value.findIndex((t) => t.path === path)
    if (idx === -1) return
    tabs.value = tabs.value.filter((t, i) => t.affix || i >= idx)
    activeTab.value = path
  }

  const closeRightTabs = (path: string) => {
    const idx = tabs.value.findIndex((t) => t.path === path)
    if (idx === -1) return
    tabs.value = tabs.value.filter((t, i) => t.affix || i <= idx)
    activeTab.value = path
  }

  const closeAllTabs = () => {
    tabs.value = tabs.value.filter((t) => t.affix)
    activeTab.value = tabs.value[0]?.path || '/dashboard'
  }

  const setActiveTab = (path: string) => {
    activeTab.value = path
  }

  return {
    tabs,
    activeTab,
    addTab,
    removeTab,
    closeOtherTabs,
    closeLeftTabs,
    closeRightTabs,
    closeAllTabs,
    setActiveTab
  }
})
