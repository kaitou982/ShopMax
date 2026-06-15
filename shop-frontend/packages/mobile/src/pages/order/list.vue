<script setup lang="ts">defineOptions({ name: 'OrderList' })
import { ref, onMounted } from 'vue'
import { orderApi, type OrderDetail } from '@shop/shared'
const orders = ref<OrderDetail[]>([])
const sm: Record<number,string> = { 0:'待付款',1:'待发货',2:'待收货',3:'已完成',4:'已取消',5:'退款中',6:'已退款' }
const showPaySheet = ref(false)
const payingOrder = ref<OrderDetail | null>(null)
const refundingId = ref<number | null>(null)

const payMethods = [
  { value: 3, label: '余额支付', icon: 'wallet' },
  { value: 1, label: '支付宝', icon: 'auth' },
  { value: 2, label: '微信支付', icon: 'weixin' },
]

onMounted(load)
async function load() { try { orders.value = await orderApi.getMyOrders() } catch {} }

function openPay(o: OrderDetail) { payingOrder.value = o; showPaySheet.value = true }
async function doPay(payType: number) {
  if (!payingOrder.value) return
  showPaySheet.value = false
  await uni.showLoading({title: '支付中...'})
  try {
    if (payType === 3) {
      await orderApi.pay(payingOrder.value.id, 3)
    } else {
      const BASE_URL = 'http://localhost:8080'
      const token = uni.getStorageSync('token') || ''
      const resp = await new Promise<any>((resolve, reject) => {
        uni.request({
          url: `${BASE_URL}/api/v1/payments`, method: 'POST',
          header: { Authorization: token ? `Bearer ${token}` : '', 'Content-Type': 'application/json' },
          data: { orderId: payingOrder.value!.id, payMethod: payType, scene: 'mobile' },
          success: r => { const d = r.data as any; if (d?.code === 200) resolve(d.data); else reject(d) },
          fail: reject
        })
      })
      if (resp.mode === 'simulate' || resp.mode === 'mock') {
        await new Promise<void>((resolve, reject) => {
          uni.request({
            url: `${BASE_URL}/api/v1/payments/${resp.paymentNo}/mock-confirm`, method: 'POST',
            header: { Authorization: token ? `Bearer ${token}` : '', 'Content-Type': 'application/json' },
            success: r => { const d = r.data as any; if (d?.code === 200) resolve(); else reject(d) },
            fail: reject
          })
        })
      }
    }
    await load()
  } finally { uni.hideLoading() }
}

async function receive(o: OrderDetail) {
  const r = await new Promise<boolean>(resolve => uni.showModal({ title: '确认收货', content: '确认已收到商品？', success: res => resolve(res.confirm) }))
  if (r) { await uni.showLoading({title: '处理中...'}); await orderApi.confirmReceive(o.id); uni.hideLoading(); uni.showToast({ title: '已确认收货', icon: 'success' }); load() }
}
async function cancel(o: OrderDetail) {
  const r = await new Promise<boolean>(resolve => uni.showModal({ title: '取消订单', content: '确定取消该订单？', success: res => resolve(res.confirm) }))
  if (r) { await uni.showLoading({title: '取消中...'}); await orderApi.cancel(o.id, '取消'); uni.hideLoading(); uni.showToast({ title: '已取消', icon: 'success' }); load() }
}
async function refund(o: OrderDetail) {
  if (refundingId.value === o.id) return
  if (o.status === 5) {
    uni.showToast({ title: '退款申请已提交，请等待审核', icon: 'none' })
    return
  }
  const r = await new Promise<boolean>(resolve => uni.showModal({ title: '申请退款', content: '确认申请退款？退款申请提交后需等待管理员审核。', success: res => resolve(res.confirm) }))
  if (!r) return
  refundingId.value = o.id
  await uni.showLoading({title: '申请中...'})
  try {
    await orderApi.refund(o.id, '用户申请退款')
    uni.showToast({ title: '退款申请已提交，等待审核', icon: 'none', duration: 2000 })
    load()
  } catch (e: any) {
    uni.showToast({ title: e?.message || '退款申请失败', icon: 'none' })
  } finally {
    uni.hideLoading()
    refundingId.value = null
  }
}
</script>
<template>
  <view class="ol">
    <view v-if="!orders.length" class="empty">暂无订单</view>
    <view class="card" v-for="o in orders" :key="o.id" @click="uni.navigateTo({url:'/pages/order/detail?id='+o.id})">
      <view class="oh"><text class="ono">{{o.orderNo}}</text><text class="os" :style="{color:o.status===0?'#FF5000':o.status===2?'#00B578':'#666'}">{{sm[o.status]}}</text></view>
      <view class="ob"><text>{{o.receiverName}} {{o.receiverPhone}}</text><text class="oa">¥{{o.payAmount}}</text></view>
      <view class="ot">{{o.createTime}}</view>
      <view class="oacts">
        <button v-if="o.status===0" class="bp" @click="openPay(o)">去付款</button>
        <button v-if="o.status===0" class="bw" @click="cancel(o)">取消</button>
        <button v-if="o.status===1 || o.status===2" class="bw warn" @click="refund(o)" :disabled="refundingId===o.id">申请退款</button>
        <text v-if="o.status===5" class="refund-tag">退款审核中</text>
        <button v-if="o.status===2" class="bp" @click="receive(o)">确认收货</button>
      </view>
    </view>

    <view class="ps-mask" v-if="showPaySheet" @click="showPaySheet=false"/>
    <view class="ps-sheet" v-if="showPaySheet">
      <view class="ps-hd"><text>选择支付方式</text><text class="ps-amt">¥{{ payingOrder?.payAmount.toFixed(2) }}</text></view>
      <view class="ps-item" v-for="m in payMethods" :key="m.value" @click="doPay(m.value)">
        <uni-icons :type="m.icon" size="24" :color="m.value===3?'#FF9000':m.value===1?'#1677FF':'#07C160'" />
        <text class="ps-label">{{m.label}}</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <button class="ps-close" @click="showPaySheet=false">取消</button>
    </view>
  </view>
</template>
<style scoped lang="scss">
.ol{padding:20rpx;min-height:100vh;background:#f5f5f5}.empty{text-align:center;padding:200rpx 0;color:#999}
.card{background:#fff;border-radius:12rpx;padding:20rpx;margin-bottom:12rpx}.oh{display:flex;justify-content:space-between;margin-bottom:8rpx}.ono{font-size:24rpx;color:#666}.os{font-size:26rpx;font-weight:600}.ob{display:flex;justify-content:space-between;font-size:28rpx;margin-bottom:6rpx}.oa{font-weight:700;color:#FF5000}.ot{font-size:22rpx;color:#999;margin-bottom:12rpx}.oacts{display:flex;justify-content:flex-end;gap:12rpx}.bp{padding:8rpx 24rpx;background:#FF5000;color:#fff;border:none;border-radius:20rpx;font-size:24rpx}.bw{padding:8rpx 24rpx;background:#fff;color:#666;border:1rpx solid #ddd;border-radius:20rpx;font-size:24rpx}.bw.warn{border-color:#FF3B3B;color:#FF3B3B}.bw:disabled{opacity:0.5}.refund-tag{padding:8rpx 24rpx;border:1rpx solid #FF5000;background:#FFF8F4;border-radius:20rpx;font-size:24rpx;color:#FF5000}
.ps-mask{position:fixed;inset:0;background:rgba(0,0,0,.45);z-index:100}
.ps-sheet{position:fixed;left:0;right:0;bottom:0;background:#fff;border-radius:24rpx 24rpx 0 0;z-index:101;
  padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
}
.ps-hd{text-align:center;margin-bottom:20rpx}.ps-amt{display:block;font-size:48rpx;font-weight:700;color:#FF5000;margin-top:8rpx}
.ps-item{display:flex;align-items:center;gap:16rpx;padding:24rpx;border:2rpx solid #f0f0f0;border-radius:12rpx;margin-bottom:12rpx}.ps-icon{font-size:40rpx}.ps-label{flex:1;font-size:30rpx;font-weight:600}.ps-ar{font-size:36rpx;color:#ccc}
.ps-close{width:100%;padding:24rpx;background:#f5f5f5;border:none;border-radius:12rpx;font-size:28rpx;color:#666;margin-top:8rpx}
</style>
