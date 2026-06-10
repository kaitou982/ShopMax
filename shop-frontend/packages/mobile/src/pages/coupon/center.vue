<script setup lang="ts">
defineOptions({ name: 'CouponCenterPage' })

import { ref, onMounted } from 'vue'
import { couponApi, type Coupon } from '@shop/shared'

const coupons = ref<Coupon[]>([])
const loading = ref(true)
const claiming = ref<number | null>(null)
const claimed = ref<Set<number>>(new Set())

const typeLabels: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '运费券', 4: '新人券' }

onMounted(async () => {
  try {
    const res = await couponApi.getAvailableCoupons()
    coupons.value = res.records || []
  } catch { /* noop */ } finally { loading.value = false }
})

const claim = async (c: Coupon) => {
  claiming.value = c.id
  try {
    if (c.integralCost && c.integralCost > 0) {
      await couponApi.exchangeCoupon(c.id)
    } else {
      await couponApi.receiveCoupon(c.id)
    }
    claimed.value.add(c.id)
    c.receivedCount++
    uni.showToast({ title: '领取成功', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e?.message || '领取失败', icon: 'none' })
  } finally { claiming.value = null }
}

const formatDiscount = (c: Coupon) => c.type === 2 ? (c.discountRate || 0) * 10 + '折' : '¥' + (c.discountAmount || 0)
const isFinished = (c: Coupon) => c.receivedCount >= c.totalCount
</script>

<template>
  <view class="cp">
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!coupons.length" class="state">暂无可用优惠券</view>
    <view class="list" v-else>
      <view class="card" v-for="c in coupons" :key="c.id" :class="{ done: isFinished(c) || claimed.has(c.id) }">
        <view class="c-left">
          <text class="c-type">{{ typeLabels[c.type] || '券' }}</text>
          <text class="c-val">{{ formatDiscount(c) }}</text>
          <text class="c-cond" v-if="c.minAmount && c.minAmount > 0">满¥{{ c.minAmount }}</text>
          <text class="c-cond" v-else>无门槛</text>
        </view>
        <view class="c-right">
          <text class="c-name">{{ c.name }}</text>
          <text class="c-desc" v-if="c.description">{{ c.description }}</text>
          <text class="c-valid" v-if="c.useStartTime">{{ c.useStartTime?.slice(0,10) }} ~ {{ c.useEndTime?.slice(0,10) }}</text>
          <text class="c-valid" v-else-if="c.validDays">领取后{{ c.validDays }}天有效</text>
          <view class="c-bar-wrap"><view class="c-bar"><view class="c-bar-fill" :style="{ width: (c.receivedCount/c.totalCount*100)+'%' }" /></view><text class="c-count">{{ c.receivedCount }}/{{ c.totalCount }}</text></view>
          <button class="c-btn" :class="{ done: isFinished(c) || claimed.has(c.id) }" :disabled="isFinished(c) || claimed.has(c.id) || claiming === c.id" @click="claim(c)">
            {{ claiming === c.id ? '处理中...' : isFinished(c) ? '已抢光' : claimed.has(c.id) ? '已领取' : c.integralCost ? c.integralCost+'积分兑换' : '立即领取' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.cp { min-height: 100vh; background: #f5f5f5; }
.state { text-align: center; padding: 200rpx 0; color: #999; font-size: 28rpx; }
.list { padding: 20rpx; display: flex; flex-direction: column; gap: 20rpx; }
.card { display: flex; background: #fff; border-radius: 16rpx; overflow: hidden; box-shadow: 0 2rpx 12rpx rgba(0,0,0,.04); &.done { opacity: .5; } }
.c-left { width: 160rpx; flex-shrink: 0; background: linear-gradient(180deg,#FF5000,#FF9000); display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 30rpx 16rpx; color: #fff; }
.c-type { font-size: 22rpx; opacity: .85; }
.c-val { font-size: 44rpx; font-weight: 800; margin: 8rpx 0; }
.c-cond { font-size: 20rpx; opacity: .75; }
.c-right { flex: 1; padding: 20rpx; display: flex; flex-direction: column; gap: 8rpx; }
.c-name { font-size: 28rpx; font-weight: 600; color: #1c1c1e; }
.c-desc { font-size: 22rpx; color: #999; }
.c-bar-wrap { display: flex; align-items: center; gap: 12rpx; margin-top: auto; }
.c-bar { flex: 1; height: 6rpx; background: #f0f0f0; border-radius: 3rpx; overflow: hidden; }
.c-bar-fill { height: 100%; background: linear-gradient(90deg,#FF5000,#FF9000); border-radius: 3rpx; }
.c-count { font-size: 20rpx; color: #999; }
.c-btn { width: 100%; padding: 12rpx 0; border: none; border-radius: 24rpx; background: linear-gradient(90deg,#FF5000,#FF9000); color: #fff; font-size: 26rpx; font-weight: 600; margin-top: 8rpx; &.done { background: #00B578; } }
</style>
