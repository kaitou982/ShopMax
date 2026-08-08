<script setup lang="ts">
defineOptions({ name: 'CouponCenterPage' })

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { couponApi, type Coupon } from '@shop/shared'

const router = useRouter()
const message = useMessage()

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

const claim = async (coupon: Coupon) => {
  claiming.value = coupon.id
  try {
    if (coupon.integralCost && coupon.integralCost > 0) {
      await couponApi.exchangeCoupon(coupon.id)
    } else {
      await couponApi.receiveCoupon(coupon.id)
    }
    claimed.value.add(coupon.id)
    coupon.receivedCount++
  } catch (e: any) {
    message.error(e?.message || '领取失败')
  } finally { claiming.value = null }
}

const formatDiscount = (c: Coupon) => {
  if (c.type === 2) return (c.discountRate || 0) * 10 + '折'
  return '¥' + (c.discountAmount || 0)
}

const validText = (c: Coupon) => {
  if (c.useStartTime && c.useEndTime) {
    return c.useStartTime.slice(0, 10) + ' ~ ' + c.useEndTime.slice(0, 10)
  }
  return c.validDays ? '领后' + c.validDays + '天内有效' : '有效期见券面'
}

const isFinished = (c: Coupon) => c.receivedCount >= c.totalCount
</script>

<template>
  <div class="cc-page">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <h2>领券中心</h2>

    <div v-if="loading" class="cc-loading">加载中...</div>
    <div v-else-if="!coupons.length" class="cc-empty">暂无可用优惠券</div>

    <div class="cc-grid" v-else>
      <div class="cc-card" v-for="c in coupons" :key="c.id" :class="{ finished: isFinished(c), claimed: claimed.has(c.id) }">
        <div class="cc-left">
          <span class="cc-type">{{ typeLabels[c.type] || '优惠券' }}</span>
          <span class="cc-value">{{ formatDiscount(c) }}</span>
          <span class="cc-condition" v-if="c.minAmount && c.minAmount > 0">满¥{{ c.minAmount }}可用</span>
          <span class="cc-condition" v-else>无门槛</span>
        </div>
        <div class="cc-right">
          <div class="cc-name">{{ c.name }}</div>
          <div class="cc-desc" v-if="c.description">{{ c.description }}</div>
          <div class="cc-valid">{{ validText(c) }}</div>
          <div class="cc-progress">
            <div class="cc-bar"><div class="cc-bar-fill" :style="{ width: (c.receivedCount / c.totalCount * 100) + '%' }" /></div>
            <span class="cc-count">{{ c.receivedCount }}/{{ c.totalCount }}</span>
          </div>
          <button
            class="cc-btn"
            :class="{ finished: isFinished(c), done: claimed.has(c.id) }"
            :disabled="isFinished(c) || claimed.has(c.id) || claiming === c.id"
            @click.stop="claim(c)"
          >
            {{ claiming === c.id ? '处理中...' : isFinished(c) ? '已抢光' : claimed.has(c.id) ? '已领取' : c.integralCost ? `${c.integralCost}积分兑换` : '立即领取' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.back-btn { display: inline-block; padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; margin-bottom: 12px; color: #666; &:hover { border-color: $brand-orange; color: $brand-orange; } }
.cc-page { max-width: 900px; }

h2 { margin-bottom: $spacing-xl; }

.cc-loading, .cc-empty { text-align: center; padding: 80px 0; color: $text-hint; }

.cc-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }

.cc-card {
  display: flex; background: $bg-card; border-radius: $radius-md; overflow: hidden;
  box-shadow: $shadow-sm; transition: all $transition-fast;
  &:hover { box-shadow: $shadow-md; transform: translateY(-2px); }
  &.finished { opacity: 0.6; }
}

.cc-left {
  width: 100px; flex-shrink: 0;
  background: linear-gradient(180deg, #FF5000, #FF9000);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 20px 12px; color: #fff;
  .cc-type { font-size: $font-size-xs; opacity: 0.85; }
  .cc-value { font-size: 22px; font-weight: 800; margin: 6px 0; }
  .cc-condition { font-size: 10px; opacity: 0.75; }
}

.finished .cc-left {
  background: linear-gradient(180deg, #B0B0B0, #D0D0D0);
}

.cc-right {
  flex: 1; padding: 16px; display: flex; flex-direction: column; gap: 6px; min-width: 0;
}

.cc-name { font-size: $font-size-base; font-weight: 600; color: $text-primary; }
.cc-desc { font-size: $font-size-xs; color: $text-secondary; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cc-valid { font-size: $font-size-xs; color: $text-hint; }

.cc-progress { display: flex; align-items: center; gap: 8px; margin-top: auto; }
.cc-bar { flex: 1; height: 4px; background: #F0F0F5; border-radius: 2px; overflow: hidden; }
.cc-bar-fill { height: 100%; background: $brand-gradient; border-radius: 2px; transition: width 0.3s; }
.cc-count { font-size: 10px; color: $text-hint; white-space: nowrap; }

.cc-btn {
  width: 100%; padding: 8px 0; border: none; border-radius: 16px;
  background: $brand-gradient; color: #fff; font-size: $font-size-sm; font-weight: 600;
  cursor: pointer; transition: all $transition-fast; margin-top: 4px;
  &:hover:not(:disabled) { opacity: 0.9; }
  &:disabled { cursor: default; }
  &.finished { background: #E5E5EA; color: #999; }
  &.done { background: $color-success; }
}
</style>
