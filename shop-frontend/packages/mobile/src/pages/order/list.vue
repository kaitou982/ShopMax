<script setup lang="ts">
defineOptions({ name: 'OrderList' })
import { ref, onMounted } from 'vue'
import { orderApi, type OrderDetail } from '@shop/shared'
import { payMethods, doPay } from '@/utils/pay'

const orders = ref<OrderDetail[]>([])
const sm: Record<number, string> = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款' }
const showPaySheet = ref(false)
const payingOrder = ref<OrderDetail | null>(null)
const refundingId = ref<number | null>(null)

onMounted(load)
async function load() { try { orders.value = await orderApi.getMyOrders() } catch { /* optional */ } }

function openPay(o: OrderDetail) { payingOrder.value = o; showPaySheet.value = true }
async function handlePay(payType: number) {
  if (!payingOrder.value) return
  showPaySheet.value = false
  await doPay(payingOrder.value.id, payType)
  load()
}

async function receive(o: OrderDetail) {
  const r = await new Promise<boolean>(resolve => uni.showModal({ title: '确认收货', content: '确认已收到商品？', success: res => resolve(res.confirm) }))
  if (r) { await uni.showLoading({ title: '处理中...' }); await orderApi.confirmReceive(o.id); uni.hideLoading(); uni.showToast({ title: '已确认收货', icon: 'success' }); load() }
}
async function cancel(o: OrderDetail) {
  const r = await new Promise<boolean>(resolve => uni.showModal({ title: '取消订单', content: '确定取消该订单？', success: res => resolve(res.confirm) }))
  if (r) { await uni.showLoading({ title: '取消中...' }); await orderApi.cancel(o.id, '取消'); uni.hideLoading(); uni.showToast({ title: '已取消', icon: 'success' }); load() }
}
async function applyRefund(o: OrderDetail) {
  if (refundingId.value) return
  const r = await new Promise<boolean>(resolve => uni.showModal({ title: '申请退款', content: '确认申请退款？', success: res => resolve(res.confirm) }))
  if (!r) return
  refundingId.value = o.id
  try { await orderApi.refund(o.id, '用户申请退款'); uni.showToast({ title: '退款申请已提交', icon: 'none' }); load() } catch { /* handled */ } finally { refundingId.value = null }
}
const goDetail = (id: number) => uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
</script>
<template>
  <view class="ol-page">
    <view v-if="!orders.length" class="ol-empty">暂无订单</view>
    <view class="ol-card" v-for="o in orders" :key="o.id" @click="goDetail(o.id)">
      <view class="ol-hd"><text class="ol-no">订单号: {{ o.orderNo }}</text><text class="ol-st" :style="{color: o.status===0?'#FF5000':o.status===5?'#FF5000':'#999'}">{{ sm[o.status] }}</text></view>
      <view class="ol-row"><text class="ol-label">金额</text><text class="ol-val">¥{{ o.payAmount?.toFixed(2) }}</text></view>
      <view class="ol-actions" @click.stop>
        <button v-if="o.status===0" class="ol-btn primary" @click="openPay(o)">去付款</button>
        <button v-if="o.status===0" class="ol-btn" @click="cancel(o)">取消</button>
        <button v-if="o.status===2" class="ol-btn primary" @click="receive(o)">确认收货</button>
        <button v-if="o.status===1||o.status===2" class="ol-btn warn" @click="applyRefund(o)" :disabled="refundingId===o.id">申请退款</button>
        <text v-if="o.status===5" class="ol-refund">退款审核中</text>
      </view>
    </view>
  </view>
  <!-- 支付方式弹窗 -->
  <view class="ps-mask" v-if="showPaySheet" @click="showPaySheet=false" />
  <view class="ps-sheet" v-if="showPaySheet">
    <view class="ps-hd"><text>选择支付方式</text><text class="ps-amt">¥{{ payingOrder?.payAmount?.toFixed(2) }}</text></view>
    <view class="ps-item" v-for="m in payMethods" :key="m.value" @click="handlePay(m.value)">
      <uni-icons :type="m.icon" size="24" :color="m.value===3?'#FF9000':m.value===1?'#1677FF':'#07C160'" />
      <text class="ps-label">{{ m.label }}</text>
      <uni-icons type="right" size="16" color="#ccc" />
    </view>
    <button class="ps-close" @click="showPaySheet=false">取消</button>
  </view>
</template>
<style scoped lang="scss">
.ol-page { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.ol-empty { text-align: center; padding: 200rpx 0; color: #999; font-size: 28rpx; }
.ol-card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 16rpx; }
.ol-hd { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.ol-no { font-size: 24rpx; color: #999; } .ol-st { font-size: 26rpx; font-weight: 600; }
.ol-row { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.ol-label { font-size: 26rpx; color: #666; } .ol-val { font-size: 28rpx; font-weight: 600; color: #FF5000; }
.ol-actions { display: flex; gap: 12rpx; justify-content: flex-end; }
.ol-btn { font-size: 24rpx; padding: 8rpx 24rpx; border-radius: 24rpx; border: 1rpx solid #ddd; background: #fff; color: #666; &.primary { background: #FF5000; color: #fff; border: none; } &.warn { border-color: #FF3B3B; color: #FF3B3B; } }
.ol-refund { font-size: 24rpx; color: #FF5000; align-self: center; }
.ps-mask { position: fixed; inset: 0; background: rgba(0,0,0,.45); z-index: 100; }
.ps-sheet { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; border-radius: 24rpx 24rpx 0 0; z-index: 101; padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom)); }
.ps-hd { text-align: center; margin-bottom: 20rpx; }
.ps-amt { display: block; font-size: 48rpx; font-weight: 700; color: #FF5000; margin-top: 8rpx; }
.ps-item { display: flex; align-items: center; gap: 16rpx; padding: 24rpx; border: 2rpx solid #f0f0f0; border-radius: 12rpx; margin-bottom: 12rpx; }
.ps-label { flex: 1; font-size: 30rpx; font-weight: 600; }
.ps-close { width: 100%; padding: 24rpx; background: #f5f5f5; border: none; border-radius: 12rpx; font-size: 28rpx; color: #666; margin-top: 8rpx; }
</style>
