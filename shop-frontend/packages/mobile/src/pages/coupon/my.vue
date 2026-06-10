<script setup lang="ts">
defineOptions({ name: 'MyCouponsPage' })

import { ref, computed, onMounted } from 'vue'
import { couponApi, type CouponReceive } from '@shop/shared'

const list = ref<CouponReceive[]>([])
const loading = ref(true)
const activeTab = ref(0)
const tabs = [{ label: '未使用', status: 0 }, { label: '已使用', status: 1 }, { label: '已过期', status: 2 }]
const filtered = computed(() => list.value.filter(c => c.status === activeTab.value))
const typeLabels: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '运费券', 4: '新人券' }

onMounted(async () => {
  try { const res = await couponApi.getMyCoupons(); list.value = res.records || [] } catch { /* noop */ }
  finally { loading.value = false }
})

const formatDiscount = (c: CouponReceive) => c.couponType === 2 ? (c.discountRate || 0) * 10 + '折' : '¥' + (c.discountAmount || 0)
</script>

<template>
  <view class="mp">
    <view class="tabs">
      <text v-for="t in tabs" :key="t.status" :class="{ active: activeTab === t.status }" @click="activeTab = t.status">{{ t.label }}</text>
    </view>
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!filtered.length" class="state">暂无优惠券</view>
    <view class="list" v-else>
      <view class="card" v-for="c in filtered" :key="c.id" :class="{ used: c.status !== 0 }">
        <view class="c-left">
          <text class="c-type">{{ typeLabels[c.couponType] || '券' }}</text>
          <text class="c-val">{{ formatDiscount(c) }}</text>
          <text class="c-cond" v-if="c.minAmount && c.minAmount > 0">满¥{{ c.minAmount }}</text>
        </view>
        <view class="c-right">
          <text class="c-name">{{ c.couponName }}</text>
          <text class="c-valid" v-if="c.useEndTime">有效期至 {{ c.useEndTime?.slice(0, 10) }}</text>
          <text class="c-info" v-if="c.status === 1 && c.orderNo">订单: {{ c.orderNo }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.mp { min-height: 100vh; background: #f5f5f5; }
.tabs { display: flex; background: #fff; border-bottom: 1rpx solid #e5e5ea; }
.tabs text { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #666; &.active { color: #FF5000; font-weight: 600; border-bottom: 4rpx solid #FF5000; } }
.state { text-align: center; padding: 200rpx 0; color: #999; font-size: 28rpx; }
.list { padding: 20rpx; display: flex; flex-direction: column; gap: 20rpx; }
.card { display: flex; background: #fff; border-radius: 16rpx; overflow: hidden; box-shadow: 0 2rpx 12rpx rgba(0,0,0,.04); &.used { opacity: .5; } }
.c-left { width: 160rpx; flex-shrink: 0; background: linear-gradient(180deg,#FF5000,#FF9000); display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 30rpx 16rpx; color: #fff; }
.c-type { font-size: 22rpx; opacity: .85; }
.c-val { font-size: 44rpx; font-weight: 800; margin: 8rpx 0; }
.c-cond { font-size: 20rpx; opacity: .75; }
.c-right { flex: 1; padding: 24rpx; display: flex; flex-direction: column; justify-content: center; gap: 8rpx; }
.c-name { font-size: 30rpx; font-weight: 600; color: #1c1c1e; }
.c-valid { font-size: 22rpx; color: #999; }
.c-info { font-size: 24rpx; color: #666; }
</style>
