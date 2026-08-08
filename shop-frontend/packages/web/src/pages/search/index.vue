<script setup lang="ts">
defineOptions({ name: 'SearchPage' })
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { FlameOutline } from '@vicons/ionicons5'
import { productApi, categoryApi, brandApi, searchApi, type ProductDetail, type Category, type Brand, type HotKeyword } from '@shop/shared'

const route = useRoute()
const router = useRouter()

const keyword = ref((route.query.q as string) || '')
const results = ref<ProductDetail[]>([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

// 排序
const sortBy = ref('')
const sortOptions = [
  { label: '综合', value: '' },
  { label: '价格↑', value: 'price_asc' },
  { label: '价格↓', value: 'price_desc' },
  { label: '销量', value: 'sales' },
  { label: '新品', value: 'newest' },
]

// 筛选
const categories = ref<Category[]>([])
const brands = ref<Brand[]>([])
const selectedCategoryId = ref<number | null>(null)
const selectedBrandId = ref<number | null>(null)

// 热门搜索
const hotKeywords = ref<HotKeyword[]>([])

// 搜索历史
const HISTORY_KEY = 'search_history'
const history = ref<string[]>(JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]'))

// 初始化
onMounted(async () => {
  try { categories.value = await categoryApi.getTree() } catch { /* noop */ }
  try {
    const allBrands = await brandApi.getAll()
    const seen = new Set<string>()
    brands.value = allBrands.filter(b => { if (seen.has(b.name)) return false; seen.add(b.name); return true })
  } catch { /* noop */ }
  try { hotKeywords.value = await searchApi.getHot(10) } catch { /* noop */ }
  if (keyword.value) doSearch()
})

// 监听路由变化
watch(() => route.query.q, (v) => {
  keyword.value = (v as string) || ''
  doSearch()
})

// 执行搜索
const doSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) { results.value = []; return }
  pageNum.value = 1
  saveHistory(kw)
  searchApi.record(kw).catch(() => {})
  await fetchResults()
}

// 获取结果
const fetchResults = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      keyword: keyword.value.trim(),
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (sortBy.value) params.sortBy = sortBy.value
    if (selectedCategoryId.value) params.categoryId = selectedCategoryId.value
    if (selectedBrandId.value) params.brandId = selectedBrandId.value
    const data = await productApi.getPage(params) as any
    results.value = data.records || []
    total.value = data.total || 0
  } catch { results.value = [] }
  finally { loading.value = false }
}

// 排序切换
const changeSort = (val: string) => {
  sortBy.value = val
  pageNum.value = 1
  fetchResults()
}

// 分类筛选
const selectCategory = (id: number | null) => {
  selectedCategoryId.value = id
  pageNum.value = 1
  fetchResults()
}

// 品牌筛选
const selectBrand = (id: number | null) => {
  selectedBrandId.value = id
  pageNum.value = 1
  fetchResults()
}

// 翻页
const onPageChange = (p: number) => {
  pageNum.value = p
  fetchResults()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 搜索历史
const saveHistory = (kw: string) => {
  history.value = [kw, ...history.value.filter(h => h !== kw)].slice(0, 8)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}
const onHistory = (h: string) => { keyword.value = h; doSearch() }
const clearHistory = () => { history.value = []; localStorage.removeItem(HISTORY_KEY) }

// 总页数
const totalPages = ref(0)
watch(total, () => { totalPages.value = Math.ceil(total.value / pageSize.value) })
</script>

<template>
  <div class="search-page">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <!-- 左侧筛选面板 -->
    <aside class="filter-panel">
      <div class="filter-section">
        <h4 class="filter-title">分类</h4>
        <div class="filter-list">
          <div class="filter-item" :class="{ active: !selectedCategoryId }" @click="selectCategory(null)">全部</div>
          <div v-for="cat in categories" :key="cat.id">
            <div class="filter-item" :class="{ active: selectedCategoryId === cat.id }" @click="selectCategory(cat.id)">
              {{ cat.name }}
            </div>
            <div v-if="cat.children?.length" class="filter-children">
              <div v-for="child in cat.children" :key="child.id"
                class="filter-item child" :class="{ active: selectedCategoryId === child.id }"
                @click="selectCategory(child.id)">
                {{ child.name }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="filter-section">
        <h4 class="filter-title">品牌</h4>
        <div class="filter-list">
          <div class="filter-item" :class="{ active: !selectedBrandId }" @click="selectBrand(null)">全部</div>
          <div v-for="b in brands" :key="b.id"
            class="filter-item" :class="{ active: selectedBrandId === b.id }"
            @click="selectBrand(b.id)">
            {{ b.name }}
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧内容 -->
    <main class="search-content">
      <!-- 热门搜索（未搜索时显示） -->
      <div v-if="!keyword && hotKeywords.length" class="hot-section">
        <div class="h-header">
          <n-icon :size="16" color="#FF5000"><FlameOutline /></n-icon>
          <span>热门搜索</span>
        </div>
        <div class="h-tags">
          <span v-for="(item, i) in hotKeywords" :key="item.keyword" class="h-tag hot" :class="{ top: i < 3 }" @click="onHistory(item.keyword)">
            {{ item.keyword }}
          </span>
        </div>
      </div>

      <!-- 搜索历史（未搜索时显示） -->
      <div v-if="!keyword && history.length" class="history-section">
        <div class="h-header"><span>搜索历史</span><button @click="clearHistory">清空</button></div>
        <div class="h-tags">
          <span v-for="h in history" :key="h" class="h-tag" @click="onHistory(h)">{{ h }}</span>
        </div>
      </div>

      <!-- 排序栏 -->
      <div v-if="keyword" class="sort-bar">
        <span v-for="opt in sortOptions" :key="opt.value"
          class="sort-item" :class="{ active: sortBy === opt.value }"
          @click="changeSort(opt.value)">
          {{ opt.label }}
        </span>
        <span class="result-count" v-if="total > 0">共 {{ total }} 件商品</span>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="state">搜索中...</div>

      <!-- 搜索结果 -->
      <div v-else-if="results.length" class="product-grid">
        <router-link v-for="p in results" :key="p.id" :to="`/product/${p.id}`" class="card">
          <img :src="p.mainImage || '/api/v1/files/default/product'" />
          <div class="body">
            <span class="name">{{ p.name }}</span>
            <span class="price">¥{{ p.salePrice }}</span>
            <span class="sales">已售{{ (p.sales||0) > 10000 ? ((p.sales/1e4).toFixed(1)+'万') : (p.sales||0) }}</span>
          </div>
        </router-link>
      </div>

      <!-- 空状态 -->
      <div v-else-if="keyword && !loading" class="state">未找到相关商品</div>

      <!-- 分页器 -->
      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="pageNum <= 1" @click="onPageChange(pageNum - 1)">上一页</button>
        <template v-for="p in totalPages" :key="p">
          <button v-if="p <= 5 || p === totalPages || Math.abs(p - pageNum) <= 1"
            :class="{ active: p === pageNum }" @click="onPageChange(p)">{{ p }}</button>
          <span v-else-if="p === 6 && pageNum > 4" class="dots">...</span>
        </template>
        <button :disabled="pageNum >= totalPages" @click="onPageChange(pageNum + 1)">下一页</button>
      </div>
    </main>
  </div>
</template>

<style scoped lang="scss">
.back-btn { display: inline-block; padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; margin-bottom: 12px; color: #666; &:hover { border-color: $brand-orange; color: $brand-orange; } }
.search-page {
  display: flex; gap: 24px; min-height: 600px;
}

// 左侧筛选
.filter-panel {
  width: 200px; flex-shrink: 0;
  .filter-section { margin-bottom: 24px; }
  .filter-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 12px; }
  .filter-list { display: flex; flex-direction: column; gap: 4px; }
  .filter-item {
    padding: 6px 12px; font-size: 13px; color: #666; border-radius: 6px; cursor: pointer;
    &:hover { color: #FF5000; background: #FFF3EC; }
    &.active { color: #FF5000; font-weight: 600; background: #FFF3EC; }
    &.child { padding-left: 24px; font-size: 12px; }
  }
}

// 右侧内容
.search-content { flex: 1; min-width: 0; }

// 热门搜索
.hot-section {
  margin-bottom: 20px;
  .h-header { margin-bottom: 12px; font-size: 14px; }
  .h-tags { display: flex; flex-wrap: wrap; gap: 8px; }
  .h-tag {
    background: #f5f5f5; padding: 6px 16px; border-radius: 16px; font-size: 13px; color: #666; cursor: pointer;
    &:hover { background: #FFF3EC; color: #FF5000; }
    &.top { background: #FFF3EC; color: #FF5000; font-weight: 500; }
  }
}

// 搜索历史
.history-section {
  margin-bottom: 24px;
  .h-header { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px;
    button { background: none; border: none; color: #999; cursor: pointer; font-size: 12px; }
  }
  .h-tags { display: flex; flex-wrap: wrap; gap: 8px; }
  .h-tag {
    background: #f5f5f5; padding: 6px 16px; border-radius: 16px; font-size: 13px; color: #666; cursor: pointer;
    &:hover { background: #FFF3EC; color: #FF5000; }
  }
}

// 排序栏
.sort-bar {
  display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
  .sort-item {
    padding: 6px 16px; border-radius: 16px; font-size: 13px; color: #666; cursor: pointer;
    background: #f5f5f5;
    &:hover { color: #FF5000; }
    &.active { background: #FF5000; color: #fff; }
  }
  .result-count { margin-left: auto; font-size: 12px; color: #999; }
}

// 商品网格
.product-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px;
  .card {
    background: #fff; border-radius: 12px; overflow: hidden; text-decoration: none; color: inherit;
    transition: box-shadow 0.2s;
    &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
    img { width: 100%; aspect-ratio: 1; object-fit: cover; background: #f5f5f5; }
    .body { padding: 10px;
      .name { font-size: 13px; display: block; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; color: #333; }
      .price { font-size: 16px; font-weight: 700; color: #FF5000; display: block; margin-top: 6px; }
      .sales { font-size: 11px; color: #999; display: block; margin-top: 4px; }
    }
  }
}

// 状态
.state { text-align: center; padding: 80px 0; color: #999; font-size: 14px; }

// 分页器
.pagination {
  display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 24px; padding: 16px 0;
  button {
    min-width: 36px; height: 36px; padding: 0 12px;
    border: 1px solid #e0e0e0; border-radius: 6px; background: #fff;
    font-size: 13px; color: #333; cursor: pointer;
    &:hover { border-color: #FF5000; color: #FF5000; }
    &.active { background: #FF5000; color: #fff; border-color: #FF5000; }
    &:disabled { opacity: 0.4; cursor: not-allowed; }
  }
  .dots { font-size: 13px; color: #999; }
}
</style>
