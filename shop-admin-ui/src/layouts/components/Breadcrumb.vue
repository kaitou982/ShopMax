<script setup lang="ts">
/**
 * Breadcrumb 面包屑组件
 * @description 根据当前路由自动生成面包屑导航
 * @author ShopMax Team
 * @since 2026-04-27
 */
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import type { RouteLocationMatched } from 'vue-router'

defineOptions({
  name: 'Breadcrumb'
})

const route = useRoute()
const router = useRouter()

interface BreadcrumbItem {
  path: string
  title: string
}

const breadcrumbs = ref<BreadcrumbItem[]>([])

const getBreadcrumbs = (matched: RouteLocationMatched[]): BreadcrumbItem[] => {
  const result: BreadcrumbItem[] = []

  for (const item of matched) {
    if (item.meta?.title) {
      result.push({
        path: item.path,
        title: item.meta.title as string
      })
    }
  }

  return result
}

const updateBreadcrumbs = () => {
  breadcrumbs.value = getBreadcrumbs(route.matched)
}

const handleClick = (item: BreadcrumbItem) => {
  if (item.path === route.path) {
    return
  }
  router.push(item.path)
}

// 监听路由变化
watch(
  () => route.path,
  () => {
    updateBreadcrumbs()
  },
  { immediate: true }
)
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight" class="breadcrumb">
    <el-breadcrumb-item
      v-for="(item, index) in breadcrumbs"
      :key="item.path"
      :to="index === breadcrumbs.length - 1 ? '' : item.path"
      @click="handleClick(item)"
    >
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<style scoped lang="scss">
.breadcrumb {
  :deep(.el-breadcrumb__inner) {
    cursor: pointer;
    color: $text-secondary;
    font-weight: 500;

    &.is-link:hover {
      color: $brand-orange;
    }
  }

  :deep(.el-breadcrumb__item:last-child) {
    .el-breadcrumb__inner {
      color: $text-primary;
      cursor: text;
      font-weight: 600;
    }
  }
}
</style>
