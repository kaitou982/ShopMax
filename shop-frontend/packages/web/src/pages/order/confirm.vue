<script setup lang="ts">defineOptions({ name: 'OrderConfirm' })
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi, addressApi, couponApi, type AddressInfo, type CouponReceive } from '@shop/shared'
import { useCartStore } from '@/stores'

const router = useRouter()
const cartStore = useCartStore()
const address = ref<AddressInfo | null>(null)
const remark = ref('')
const submitting = ref(false)
const msg = ref('')

const items = computed(() => cartStore.selectedItems)
const total = computed(() => cartStore.totalPrice)
const freight = computed(() => total.value >= 99 ? 0 : 10)

// Coupon selection
const availableCoupons = ref<CouponReceive[]>([])
const selectedCoupon = ref<CouponReceive | null>(null)
const selectedCoupon2 = ref<CouponReceive | null>(null)
const showCouponPicker = ref(false)
const showCouponPicker2 = ref(false)

const couponDiscount = computed(() => {
  let d = 0
  const c1 = selectedCoupon.value; const c2 = selectedCoupon2.value
  if (c1 && c1.couponType !== 3) d += c1.couponType === 2 ? Number(total.value * (1 - (c1.discountRate || 0))) : Number(c1.discountAmount || 0)
  if (c2 && c2.couponType !== 3) d += c2.couponType === 2 ? Number(total.value * (1 - (c2.discountRate || 0))) : Number(c2.discountAmount || 0)
  return d
})

const freightDiscount = computed(() => {
  let d = 0
  if (selectedCoupon.value?.couponType === 3) d += Number(selectedCoupon.value.discountAmount || 0)
  if (selectedCoupon2.value?.couponType === 3) d += Number(selectedCoupon2.value.discountAmount || 0)
  return Math.min(d, freight.value)
})

const finalPay = computed(() => Math.max(0, total.value + freight.value - couponDiscount.value - freightDiscount.value))

onMounted(async () => {
  try { address.value = await addressApi.getDefault() } catch {
    try { const list = await addressApi.getList(); if (list?.length) address.value = list[0] } catch { /* noop */ }
  }
  try {
    const res = await couponApi.getMyCoupons(0)
    availableCoupons.value = (res.records || []).filter((c: CouponReceive) => {
      const minAmount = Number(c.minAmount || 0)
      if (minAmount > total.value) return false
      // Scope filtering: 1=all, 2=categories, 3=specific products
      if (c.applicableType === 3 && c.applicableIds) {
        const ids = JSON.parse(c.applicableIds) as number[]
        const orderProductIds = items.value.map(i => i.productId)
        if (!orderProductIds.some(pid => ids.includes(pid))) return false
      }
      return true
    })
  } catch { /* noop */ }
})

const selectCoupon = (c: CouponReceive) => {
  selectedCoupon.value = c
  showCouponPicker.value = false
}

const removeCoupon = () => { selectedCoupon.value = null; selectedCoupon2.value = null }
const selectCoupon2 = (c: CouponReceive) => { selectedCoupon2.value = c; showCouponPicker2.value = false }
const removeCoupon2 = () => { selectedCoupon2.value = null }

const submit = async () => {
  if (!address.value) { msg.value = '请先添加收货地址'; return }
  if (!items.value.length) { msg.value = '请选择商品'; return }
  submitting.value = true
  try {
    await orderApi.create({
      totalAmount: total.value, payAmount: finalPay.value, freightAmount: freight.value,
      couponAmount: couponDiscount.value + freightDiscount.value,
      userCouponId: selectedCoupon.value?.id,
      userCouponId2: selectedCoupon2.value?.id,
      receiverName: address.value.receiverName, receiverPhone: address.value.receiverPhone,
      receiverAddress: address.value.fullAddress, sourceType: 1,
      items: items.value.map(i => ({ productId: i.productId, productName: i.name, productImage: i.image, price: i.price, quantity: i.quantity })),
    })
    cartStore.clearCart()
    router.push('/order/list')
  } catch (e: any) {
    msg.value = e?.message || '订单提交失败，请重试'
  } finally { submitting.value = false }
}

const goAddress = () => router.push('/user')
</script>

<template>
  <div class="oc-page">
    <h2>确认订单</h2>

    <div class="oc-msg" v-if="msg">{{ msg }}</div>

    <!-- 地址 -->
    <div class="section" @click="goAddress">
      <div class="section-title">收货地址 <span class="arrow">›</span></div>
      <div v-if="address" class="addr-card">
        <strong>{{ address.receiverName }}</strong> {{ address.receiverPhone }}
        <p>{{ address.fullAddress }}</p>
      </div>
      <div v-else class="addr-empty">点击选择收货地址</div>
    </div>

    <!-- 商品 -->
    <div class="section">
      <div class="section-title">商品信息</div>
      <div class="item" v-for="i in items" :key="i.id">
        <img :src="i.image || '/api/v1/files/default/product'" />
        <span class="name">{{ i.name }}</span>
        <span class="price">¥{{ i.price }} × {{ i.quantity }}</span>
        <span class="sub">¥{{ (i.price * i.quantity).toFixed(2) }}</span>
      </div>
    </div>

    <!-- 备注 -->
    <div class="section">
      <div class="section-title">订单备注</div>
      <input v-model="remark" placeholder="选填：给卖家留言" class="remark" />
    </div>

    <!-- 优惠券 -->
    <div class="section coupon-section">
      <div class="section-title">优惠券</div>
      <div v-if="selectedCoupon" class="coupon-selected">
        <span class="cs-name">{{ selectedCoupon.couponName }}</span>
        <span class="cs-discount">-¥{{ couponDiscount.toFixed(2) }}</span>
        <span class="cs-remove" @click="removeCoupon">✕</span>
      </div>
      <div v-else class="coupon-trigger" @click="showCouponPicker = !showCouponPicker">
        {{ availableCoupons.length ? availableCoupons.length + ' 张可用' : '暂无可用' }}
        <span class="arrow">›</span>
      </div>
      <div class="coupon-list" v-if="showCouponPicker">
        <div v-if="!availableCoupons.length" class="coupon-empty">暂无可用优惠券</div>
        <div class="coupon-item" v-for="c in availableCoupons" :key="c.id" @click="selectCoupon(c)">
          <div class="ci-left">
            <span class="ci-type">{{ {1:'满减',2:'折扣',3:'运费',4:'新人'}[c.couponType]||'券' }}</span>
            <span class="ci-val">{{ c.couponType === 2 ? (c.discountRate||0)*10+'折' : '¥'+(c.discountAmount||0) }}</span>
          </div>
          <div class="ci-right">
            <strong>{{ c.couponName }}</strong>
            <span v-if="c.minAmount && c.minAmount > 0">满¥{{ c.minAmount }}可用</span>
          </div>
        </div>
      </div>

      <!-- Second coupon (stacking) -->
      <div v-if="selectedCoupon && selectedCoupon.couponType !== 3" style="margin-top:12px">
        <div v-if="selectedCoupon2" class="coupon-selected">
          <span class="cs-name">{{ selectedCoupon2.couponName }}</span>
          <span class="cs-remove" @click="removeCoupon2">✕</span>
        </div>
        <div v-else class="coupon-trigger" @click="showCouponPicker2 = !showCouponPicker2">
          + 叠加运费券或其他优惠
          <span class="arrow">›</span>
        </div>
        <div class="coupon-list" v-if="showCouponPicker2">
          <div class="coupon-item" v-for="c in availableCoupons.filter(x => x.id !== selectedCoupon?.id)" :key="c.id" @click="selectCoupon2(c)">
            <div class="ci-left">
              <span class="ci-type">{{ {1:'满减',2:'折扣',3:'运费',4:'新人'}[c.couponType]||'券' }}</span>
              <span class="ci-val">{{ c.couponType === 2 ? (c.discountRate||0)*10+'折' : '¥'+(c.discountAmount||0) }}</span>
            </div>
            <div class="ci-right">
              <strong>{{ c.couponName }}</strong>
              <span v-if="c.minAmount && c.minAmount > 0">满¥{{ c.minAmount }}可用</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 汇总 -->
    <div class="summary">
      <div class="row"><span>商品总额</span><span>¥{{ total.toFixed(2) }}</span></div>
      <div class="row"><span>运费</span><span>免运费</span></div>
      <div class="row" v-if="couponDiscount > 0"><span>优惠券</span><span class="discount">-¥{{ couponDiscount.toFixed(2) }}</span></div>
      <div class="row" v-if="freightDiscount > 0"><span>运费券</span><span class="discount">-¥{{ freightDiscount.toFixed(2) }}</span></div>
      <div class="row total"><span>应付总额</span><span class="amount">¥{{ finalPay.toFixed(2) }}</span></div>
    </div>

    <button class="submit-btn" :disabled="submitting || !address" @click="submit">
      {{ submitting ? '提交中...' : '提交订单' }}
    </button>
  </div>
</template>

<style scoped lang="scss">
.oc-page { max-width: 800px; }
h2 { margin-bottom: 24px; }
.oc-msg { padding: 10px 16px; border-radius: 8px; background: #FFF3EC; color: $brand-orange; font-size: $font-size-sm; margin-bottom: $spacing-base; }
.section { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 16px; cursor: pointer; }
.section-title { font-size: 15px; font-weight: 600; margin-bottom: 12px; display: flex; justify-content: space-between; }
.arrow { color: #ccc; font-size: 20px; }
.addr-card { font-size: 14px; p { color: #666; margin-top: 4px; } }
.addr-empty { color: #999; font-size: 14px; }
.item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f5f5f5; font-size: 13px;
  img { width: 56px; height: 56px; border-radius: 6px; object-fit: cover; background: #f5f5f5; }
  .name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .price { color: #666; }
  .sub { font-weight: 600; }
}
.remark { width: 100%; border: 1px solid #e5e5ea; border-radius: 8px; padding: 10px 12px; font-size: 13px; outline: none; box-sizing: border-box; }
.summary { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 24px; }
.row { display: flex; justify-content: space-between; font-size: 14px; padding: 6px 0; color: #666; &.total { border-top: 1px solid #eee; margin-top: 8px; padding-top: 12px; font-size: 16px; font-weight: 700; color: #1c1c1e; } }
.amount { color: #FF5000; font-size: 20px; }
.discount { color: $color-success; font-weight: 600; }

// Coupon
.coupon-section { cursor: default; }
.coupon-selected { display: flex; align-items: center; gap: 12px; padding: 10px 14px; background: #E8F9F1; border-radius: 8px; }
.cs-name { font-size: 14px; font-weight: 600; color: $text-primary; flex: 1; }
.cs-discount { font-size: 16px; font-weight: 700; color: $color-success; }
.cs-remove { cursor: pointer; color: #999; font-size: 16px; &:hover { color: $color-danger; } }
.coupon-trigger { font-size: 14px; color: $text-secondary; display: flex; justify-content: space-between; align-items: center; cursor: pointer; padding: 4px 0; }
.coupon-list { margin-top: 12px; max-height: 240px; overflow-y: auto; }
.coupon-empty { text-align: center; padding: 20px; color: $text-hint; font-size: 13px; }
.coupon-item { display: flex; gap: 12px; padding: 12px; border: 1px solid #f0f0f5; border-radius: 8px; margin-bottom: 8px; cursor: pointer; transition: all 0.15s; &:hover { border-color: $brand-orange; background: #FFF8F5; } }
.ci-left { width: 72px; flex-shrink: 0; background: $brand-gradient; border-radius: 6px; padding: 10px 8px; text-align: center; color: #fff; .ci-type { font-size: 10px; opacity: 0.85; display: block; } .ci-val { font-size: 16px; font-weight: 700; display: block; margin-top: 2px; } }
.ci-right { flex: 1; display: flex; flex-direction: column; justify-content: center; gap: 4px; strong { font-size: 14px; color: $text-primary; } span { font-size: 12px; color: $text-secondary; } }
.submit-btn { width: 100%; height: 48px; background: linear-gradient(90deg,#FF5000,#FF9000); color: #fff; border: none; border-radius: 24px; font-size: 16px; font-weight: 600; cursor: pointer; &:disabled { opacity: 0.5; } }
</style>
