<script setup lang="ts">
defineOptions({ name: 'MyCouponsPage' })

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { couponApi, type CouponReceive } from '@shop/shared'

const router = useRouter()
const list = ref<CouponReceive[]>([])
const loading = ref(true)
const activeTab = ref(0)

const tabs = [
  { label: '未使用', status: 0 },
  { label: '已使用', status: 1 },
  { label: '已过期', status: 2 },
]

const filtered = computed(() => list.value.filter(c => c.status === activeTab.value))

onMounted(async () => {
  try {
    const res = await couponApi.getMyCoupons()
    list.value = res.records || []
  } catch { /* noop */ } finally { loading.value = false }
})

const typeLabels: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '运费券', 4: '新人券' }

const formatDiscount = (c: CouponReceive) => {
  if (c.couponType === 2) return (c.discountRate || 0) * 10 + '折'
  return '¥' + (c.discountAmount || 0)
}
</script>

<template>
  <div class="mc-page">
    <button class="mc-back" @click="router.back()">← 返回个人中心</button>
    <h2>我的优惠券</h2>

    <div class="tabs">
      <span v-for="t in tabs" :key="t.status" :class="{ active: activeTab === t.status }" @click="activeTab = t.status">
        {{ t.label }}
      </span>
    </div>

    <div v-if="loading" class="mc-empty">加载中...</div>
    <div v-else-if="!filtered.length" class="mc-empty">暂无优惠券</div>

    <div class="mc-list" v-else>
      <div class="mc-item" v-for="c in filtered" :key="c.id" :class="{ used: c.status !== 0 }">
        <div class="mc-left">
          <span class="mc-type">{{ typeLabels[c.couponType] || '券' }}</span>
          <span class="mc-value">{{ formatDiscount(c) }}</span>
          <span class="mc-condition" v-if="c.minAmount && c.minAmount > 0">满¥{{ c.minAmount }}</span>
          <span class="mc-condition" v-else>无门槛</span>
        </div>
        <div class="mc-right">
          <div class="mc-name">{{ c.couponName }}</div>
          <div class="mc-valid" v-if="c.useEndTime">有效期至 {{ c.useEndTime?.slice(0, 10) }}</div>
          <div class="mc-info" v-if="c.status === 0">可使用</div>
          <div class="mc-info" v-else-if="c.status === 1">
            已使用
            <span v-if="c.orderNo">{{ c.orderNo }}</span>
            <span v-if="c.useTime">{{ c.useTime?.slice(0, 16).replace('T', ' ') }}</span>
          </div>
          <div class="mc-info" v-else>已过期</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.mc-page { max-width: 900px; }

.mc-back {
  padding: 6px 16px; border: 1px solid $border-color; background: #fff;
  border-radius: 18px; font-size: $font-size-sm; cursor: pointer;
  color: $text-secondary; margin-bottom: $spacing-lg; display: inline-block;
  &:hover { border-color: $brand-orange; color: $brand-orange; }
}

h2 { margin-bottom: $spacing-xl; }

.tabs { display: flex; border-bottom: 2px solid $border-color; margin-bottom: $spacing-lg;
  span { padding: 10px 20px; font-size: $font-size-base; color: $text-secondary; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px;
    &.active { color: $brand-orange; border-bottom-color: $brand-orange; font-weight: 600; }
  }
}

.mc-empty { text-align: center; padding: 80px 0; color: $text-hint; }

.mc-list { display: flex; flex-direction: column; gap: $spacing-md; }

.mc-item {
  display: flex; background: $bg-card; border-radius: $radius-md; overflow: hidden;
  box-shadow: $shadow-sm; transition: all $transition-fast;
  &:hover { box-shadow: $shadow-md; }
  &.used { opacity: 0.6; .mc-left { background: linear-gradient(180deg, #B0B0B0, #D0D0D0); } }
}

.mc-left {
  width: 120px; flex-shrink: 0;
  background: linear-gradient(180deg, #FF5000, #FF9000);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 24px 12px; color: #fff; text-align: center;
  .mc-type { font-size: $font-size-xs; opacity: 0.85; }
  .mc-value { font-size: 26px; font-weight: 800; margin: 6px 0; }
  .mc-condition { font-size: 10px; opacity: 0.75; }
}

.mc-right {
  flex: 1; padding: 20px; display: flex; flex-direction: column; justify-content: center; gap: 6px;
}

.mc-name { font-size: $font-size-md; font-weight: 600; color: $text-primary; }
.mc-valid { font-size: $font-size-xs; color: $text-hint; }
.mc-info { font-size: $font-size-sm; color: $text-secondary; }
</style>
