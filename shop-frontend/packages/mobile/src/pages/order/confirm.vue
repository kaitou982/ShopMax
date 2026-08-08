<script setup lang="ts">defineOptions({ name: 'OrderConfirm' })
import { ref, computed, onMounted } from 'vue'
import { orderApi, addressApi, couponApi, getMemberDiscount, type AddressInfo, type CouponReceive } from '@shop/shared'
import { useCartStore, useUserStore } from '@/stores'

const cartStore = useCartStore()
const userStore = useUserStore()
const goAddress = () => uni.navigateTo({ url: '/pages/user/address' })
const address = ref<AddressInfo | null>(null)
const submitting = ref(false)

const availableCoupons = ref<CouponReceive[]>([])
const selectedCoupon = ref<CouponReceive | null>(null)
const showCouponPicker = ref(false)

const total = computed(() => cartStore.totalPrice)
const freight = computed(() => total.value >= 99 ? 0 : 10)

const couponDiscount = computed(() => {
  if (!selectedCoupon.value) return 0
  const c = selectedCoupon.value
  if (c.couponType === 2) return total.value * (1 - (c.discountRate || 0))
  if (c.couponType === 3) return Math.min(Number(c.discountAmount || 0), freight.value)
  return Number(c.discountAmount || 0)
})

// 会员等级折扣
const memberLevel = computed(() => userStore.userInfo?.memberLevel || 1)
const memberDiscountRate = computed(() => getMemberDiscount(memberLevel.value))
const memberDiscount = computed(() => {
  if (memberDiscountRate.value >= 1) return 0
  return Number((total.value * (1 - memberDiscountRate.value)).toFixed(2))
})

// 积分抵扣
const useIntegral = ref(false)
const integralBalance = computed(() => userStore.userInfo?.integral || 0)
const maxIntegralDeduct = computed(() => {
  if (integralBalance.value <= 0) return 0
  const afterCoupon = total.value * memberDiscountRate.value + freight.value - couponDiscount.value
  return Math.min(integralBalance.value / 100, afterCoupon * 0.5)
})
const integralDiscount = computed(() => useIntegral.value ? Number(maxIntegralDeduct.value.toFixed(2)) : 0)

const finalPay = computed(() => Math.max(0, total.value * memberDiscountRate.value + freight.value - couponDiscount.value - integralDiscount.value))

onMounted(async () => {
  try { address.value = await addressApi.getDefault() } catch {
    try { const list = await addressApi.getList(); if (list?.length) address.value = list[0] } catch {}
  }
  try {
    const res = await couponApi.getMyCoupons(0)
    const items = cartStore.selectedItems
    availableCoupons.value = (res.records || []).filter(c => {
      const min = Number(c.minAmount || 0)
      const applicableTotal = c.couponType === 3 ? freight.value : total.value
      if (min > applicableTotal) return false
      if (c.applicableType === 3 && c.applicableIds) {
        try {
          const ids = JSON.parse(c.applicableIds) as number[]
          if (!items.some(i => ids.includes(i.productId))) return false
        } catch { /* JSON parse error, skip coupon */ return false }
      }
      return true
    })
  } catch { }
})

const selectCoupon = (c: CouponReceive) => { selectedCoupon.value = c; showCouponPicker.value = false }
const removeCoupon = () => { selectedCoupon.value = null }

const submit = async () => {
  if (!address.value) { uni.showToast({ title:'请选择地址',icon:'none' }); return }
  submitting.value = true
  try {
    await orderApi.create({
      totalAmount: total.value, payAmount: finalPay.value, freightAmount: freight.value,
      couponAmount: couponDiscount.value, integralAmount: integralDiscount.value,
      useIntegral: useIntegral.value ? Math.floor(integralDiscount.value * 100) : 0,
      userCouponId: selectedCoupon.value?.id,
      receiverName: address.value.receiverName, receiverPhone: address.value.receiverPhone,
      receiverAddress: address.value.fullAddress, sourceType: 3,
      items: cartStore.selectedItems.map(i=>({ productId:i.productId, productName:i.name, productImage:i.image, price:i.price, quantity:i.quantity })),
    })
    cartStore.clearCart()
    uni.showToast({ title: '提交成功', icon: 'success' })
    setTimeout(() => uni.redirectTo({ url: '/pages/order/list' }), 1000)
  } catch (e: any) { uni.showToast({ title: e?.message || '提交失败', icon: 'none' }) } finally { submitting.value = false }
}
</script>
<template>
  <view class="oc-page">
    <view class="oc-addr" v-if="address">
      <text class="oca-name">{{ address.receiverName }} {{ address.receiverPhone }}</text>
      <text class="oca-detail">{{ address.fullAddress }}</text>
    </view>
    <view class="oc-addr empty" v-else @click="goAddress">
      <view class="oc-addr-empty-content">
        <text>点击选择收货地址</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>
    <view class="oc-items">
      <view class="oci" v-for="item in cartStore.selectedItems" :key="item.productId">
        <image :src="item.image" mode="aspectFill" class="oci-img"/>
        <view class="oci-info"><text class="oci-name">{{ item.name }}</text><text class="oci-price">¥{{ item.price }} x{{ item.quantity }}</text></view>
      </view>
    </view>
    <view class="oc-row" v-if="memberDiscount > 0"><text>会员折扣</text><text class="val" style="color:#FF5000">-¥{{ memberDiscount.toFixed(2) }}</text></view>
    <view class="oc-row"><text>运费</text><text class="val">¥{{ freight }}</text></view>
    <view class="oc-row" @click="showCouponPicker = true">
      <text>优惠券</text>
      <view class="oc-row-right" :class="{selected:selectedCoupon}">
        <text class="val">{{ selectedCoupon ? '已选' : '选择' }}</text>
        <uni-icons type="right" size="16" :color="selectedCoupon ? '#FF5000' : '#ccc'" />
      </view>
    </view>
    <view class="oc-row" v-if="couponDiscount > 0"><text>优惠券抵扣</text><text class="val" style="color:#FF5000">-¥{{ couponDiscount.toFixed(2) }}</text></view>
    <view class="oc-row" v-if="integralBalance > 0" @click="useIntegral = !useIntegral">
      <text>积分抵扣</text>
      <view class="oc-row-right">
        <text class="val" v-if="integralDiscount > 0" style="color:#FF5000">-¥{{ integralDiscount.toFixed(2) }}</text>
        <text class="val" v-else style="color:#999">{{ integralBalance }}积分可用</text>
        <switch :checked="useIntegral" color="#FF5000" @change="useIntegral = !useIntegral" />
      </view>
    </view>
    <view class="oc-row b"><text>实付</text><text class="val" style="color:#FF5000;font-size:36rpx;font-weight:700">¥{{ finalPay.toFixed(2) }}</text></view>
    <view class="oc-bar"><button class="oc-btn" @click="submit" :loading="submitting">提交订单</button></view>

    <view class="cp-mask" v-if="showCouponPicker" @click="showCouponPicker = false"/>
    <view class="cp-sheet" v-if="showCouponPicker">
      <view class="cp-hd">
        <text>选择优惠券</text>
        <view class="cp-close" @click="showCouponPicker = false">
          <uni-icons type="close" size="20" color="#999" />
        </view>
      </view>
      <view class="cp-empty" v-if="!availableCoupons.length"><text>暂无可用优惠券</text></view>
      <view class="cp-list" v-else>
        <view class="cp-card" v-for="c in availableCoupons" :key="c.id" @click="selectCoupon(c)">
          <view class="cp-left">
            <text class="cp-type">{{ {1:'满减',2:'折扣',3:'运费',4:'新人'}[c.couponType]||'券' }}</text>
            <text class="cp-val">{{ c.couponType === 2 ? (c.discountRate||0)*10+'折' : '¥'+(c.discountAmount||0) }}</text>
          </view>
          <view class="cp-right"><text>满¥{{ c.minAmount || 0 }}可用</text></view>
        </view>
      </view>
    </view>
  </view>
</template>
<style scoped lang="scss">
.oc-page { min-height:100vh;background:#f5f5f5;padding-bottom:120rpx }
.oc-addr { padding:24rpx;background:#fff;margin-bottom:16rpx;&.empty{color:#999} }
.oc-addr-empty-content { display:flex;align-items:center;gap:8rpx }
.oc-row-right { display:flex;align-items:center;gap:8rpx;&.selected .val{color:#FF5000} }
.oca-name { font-size:30rpx;font-weight:600;margin-right:20rpx }
.oca-detail { font-size:26rpx;color:#666;display:block;margin-top:8rpx }
.oc-items { background:#fff;margin-bottom:16rpx }
.oci { display:flex;padding:20rpx;border-bottom:1rpx solid #f0f0f0;.oci-img{width:120rpx;height:120rpx;border-radius:8rpx;margin-right:16rpx}.oci-info{flex:1;display:flex;flex-direction:column;justify-content:space-between;.oci-name{font-size:28rpx}.oci-price{font-size:28rpx;color:#FF5000}}}
.oc-row { display:flex;justify-content:space-between;padding:24rpx;background:#fff;margin-bottom:2rpx;font-size:28rpx;&.b{margin-top:16rpx}.val{color:#333;&.selected{color:#FF5000}}}
.oc-bar { position:fixed;bottom:0;left:0;right:0;padding:16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));background:#fff;box-shadow:0 -2rpx 8rpx rgba(0,0,0,.06);display:flex;justify-content:flex-end }
.oc-btn { background:#FF5000;color:#fff;border:none;padding:16rpx 60rpx;border-radius:40rpx;font-size:28rpx;font-weight:600 }
.cp-mask { position:fixed;inset:0;background:rgba(0,0,0,.5);z-index:100 }
.cp-sheet { position:fixed;bottom:0;left:0;right:0;background:#fff;border-radius:24rpx 24rpx 0 0;max-height:60vh;z-index:101;padding-bottom:env(safe-area-inset-bottom) }
.cp-hd { display:flex;justify-content:space-between;padding:24rpx;font-size:32rpx;font-weight:600;.cp-close{font-size:36rpx;color:#999}}
.cp-empty { padding:60rpx;text-align:center;color:#999 }
.cp-list { padding:0 24rpx 24rpx;max-height:50vh;overflow-y:auto }
.cp-card { display:flex;justify-content:space-between;align-items:center;padding:24rpx;background:#FFF3EC;border-radius:12rpx;margin-bottom:16rpx;.cp-left{display:flex;align-items:center;gap:12rpx}}.cp-val{font-size:28rpx;font-weight:700;color:#FF5000}.cp-type{font-size:22rpx;color:#FF5000;background:#FFE8DF;padding:4rpx 12rpx;border-radius:6rpx}.cp-right{font-size:24rpx;color:#999}
</style>
