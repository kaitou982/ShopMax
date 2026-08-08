<script setup lang="ts">defineOptions({ name: 'CategoryPage' })
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { categoryApi, productApi, type Category, type ProductDetail } from '@shop/shared'

const route = useRoute()
const router = useRouter()
const cats = ref<Category[]>([])
const activeId = ref<number>(Number(route.params.id) || 0)
const products = ref<ProductDetail[]>([])
const loading = ref(false)
const sort = ref<'default' | 'price-asc' | 'price-desc' | 'sales'>('default')

onMounted(async () => {
  const tree = await categoryApi.getTree()
  cats.value = tree || []
  if (!activeId.value && cats.value[0]) activeId.value = cats.value[0].id
  if (activeId.value) loadProducts()
})

watch(activeId, () => { products.value = []; loadProducts() })
watch(sort, () => { products.value = []; loadProducts() })

const loadProducts = async () => {
  if (!activeId.value) return
  loading.value = true
  try {
    const data = await productApi.getPage({ categoryId: activeId.value, pageSize: 30 })
    let list = ((data as any)?.records || []) as ProductDetail[]
    if (sort.value === 'price-asc') list.sort((a,b) => a.salePrice - b.salePrice)
    else if (sort.value === 'price-desc') list.sort((a,b) => b.salePrice - a.salePrice)
    else if (sort.value === 'sales') list.sort((a,b) => (b.sales||0) - (a.sales||0))
    products.value = list
  } catch { products.value = [] } finally { loading.value = false }
}

const activeCat = (id: number) => { activeId.value = id; router.replace(`/category/${id}`) }
</script>
<template>
  <div class="cat-page">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <h2>{{ cats.find(c => c.id === activeId)?.name || '全部分类' }}</h2>
    <div class="sort-bar">
      <span v-for="s in [{v:'default',n:'综合'},{v:'sales',n:'销量'},{v:'price-asc',n:'价格↑'},{v:'price-desc',n:'价格↓'}]" :key="s.v"
        :class="{ active: sort === s.v }" @click="sort = s.v as any">{{ s.n }}</span>
    </div>
    <div class="product-grid" v-if="!loading">
      <router-link v-for="p in products" :key="p.id" :to="`/product/${p.id}`" class="card">
        <img :src="p.mainImage || '/api/v1/files/default/product'" />
        <div class="body">
          <span class="name">{{ p.name }}</span>
          <div class="bottom"><span class="price">¥{{ p.salePrice }}</span><span class="sales">已售{{ (p.sales||0)>10000?((p.sales/10000).toFixed(1)+'万'):p.sales }}</span></div>
        </div>
      </router-link>
    </div>
    <div class="loading" v-else>加载中...</div>
  </div>
</template>
<style scoped lang="scss">
.back-btn { display: inline-block; padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; margin-bottom: 12px; color: #666; &:hover { border-color: $brand-orange; color: $brand-orange; } }
h2 { margin-bottom: 16px; }
.sort-bar { display: flex; gap: 24px; padding: 10px 0; margin-bottom: 16px; border-bottom: 1px solid $border-light;
  span { font-size: 13px; color: #666; cursor: pointer; &.active { color: $brand-orange; font-weight: 600; } }
}
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; @media (max-width: 1024px) { grid-template-columns: repeat(3, 1fr); } @media (max-width: 640px) { grid-template-columns: repeat(2, 1fr); } }
.card { background: #fff; border-radius: 12px; overflow: hidden; text-decoration: none; color: inherit; transition: all .15s;
  &:hover { transform: translateY(-3px); box-shadow: 0 4px 16px rgba(0,0,0,.08); }
  img { width: 100%; aspect-ratio: 1; object-fit: cover; background: #f5f5f5; }
}
.body { padding: 12px; }
.name { font-size: 13px; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 8px; }
.bottom { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 16px; font-weight: 700; color: $brand-orange; }
.sales { font-size: 11px; color: #999; }
.loading { text-align: center; padding: 80px 0; color: #999; }
</style>
