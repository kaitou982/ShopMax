<script setup lang="ts">
defineOptions({ name: 'OrderDetail' })
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderApi, type OrderDetail } from '@shop/shared'
import { payMethods, doPay as payOrder } from '@/utils/pay'

const order = ref<OrderDetail | null>(null)
const loading = ref(true)
const showPaySheet = ref(false)
const refunding = ref(false)

const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '待付款', color: '#FF5000' },
  1: { label: '待发货', color: '#FF8F1F' },
  2: { label: '待收货', color: '#00B578' },
  3: { label: '已完成', color: '#999' },
  4: { label: '已取消', color: '#999' },
  5: { label: '退款中', color: '#FF5000' },
  6: { label: '已退款', color: '#999' },
}

onLoad((opts?: any) => {
  if (opts?.id) loadDetail(Number(opts.id))
})

async function loadDetail(id: number) {
  try {
    loading.value = true
    order.value = await orderApi.getDetail(id)
  } catch {
    uni.showToast({ title: '加载订单失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goPay() { showPaySheet.value = true }

async function handlePay(payType: number) {
  if (!order.value) return
  showPaySheet.value = false
  await payOrder(order.value.id, payType)
  loadDetail(order.value.id)
}

async function cancelOrder() {
  if (!order.value) return
  const confirmed = await new Promise<boolean>(resolve =>
    uni.showModal({ title: '取消订单', content: '确定取消该订单？', success: res => resolve(res.confirm) })
  )
  if (!confirmed) return
  await uni.showLoading({ title: '取消中...' })
  try {
    await orderApi.cancel(order.value.id, '用户取消')
    uni.showToast({ title: '已取消', icon: 'success' })
    loadDetail(order.value.id)
  } finally { uni.hideLoading() }
}

async function applyRefund() {
  if (!order.value || refunding.value) return
  if (order.value.status === 5) {
    uni.showToast({ title: '退款申请已提交，请等待审核', icon: 'none' })
    return
  }
  const confirmed = await new Promise<boolean>(resolve =>
    uni.showModal({ title: '申请退款', content: '确认申请退款？退款申请提交后需等待管理员审核。', success: res => resolve(res.confirm) })
  )
  if (!confirmed) return
  refunding.value = true
  await uni.showLoading({ title: '申请中...' })
  try {
    await orderApi.refund(order.value.id, '用户申请退款')
    uni.showToast({ title: '退款申请已提交，等待审核', icon: 'none', duration: 2000 })
    loadDetail(order.value.id)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '退款申请失败', icon: 'none' })
  } finally {
    uni.hideLoading()
    refunding.value = false
  }
}

async function confirmReceive() {
  if (!order.value) return
  const confirmed = await new Promise<boolean>(resolve =>
    uni.showModal({ title: '确认收货', content: '确认已收到商品？', success: res => resolve(res.confirm) })
  )
  if (!confirmed) return
  await uni.showLoading({ title: '处理中...' })
  try {
    await orderApi.confirmReceive(order.value.id)
    uni.showToast({ title: '已确认收货', icon: 'success' })
    loadDetail(order.value.id)
  } finally { uni.hideLoading() }
}

function formatTime(t?: string) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}
</script>

<template>
  <view class="od">
    <view v-if="loading" class="loading">加载中...</view>
    <template v-else-if="order">
      <!-- 状态栏 -->
      <view class="status-bar" :style="{ background: statusMap[order.status]?.color || '#999' }">
        <text class="status-label">{{ statusMap[order.status]?.label }}</text>
        <text class="order-no">订单号: {{ order.orderNo }}</text>
      </view>

      <!-- 收货信息 -->
      <view class="card">
        <view class="card-title">
          <uni-icons type="location" size="18" color="#FF5000" />
          <text>收货信息</text>
        </view>
        <view class="addr-info">
          <view class="addr-top">
            <text class="addr-name">{{ order.receiverName }}</text>
            <text class="addr-phone">{{ order.receiverPhone }}</text>
          </view>
          <text class="addr-detail">{{ order.receiverAddress }}</text>
        </view>
      </view>

      <!-- 商品信息 -->
      <view class="card">
        <view class="card-title">
          <uni-icons type="shop" size="18" color="#FF5000" />
          <text>商品信息</text>
        </view>
        <view v-if="order.items?.length">
          <view class="goods-item" v-for="item in order.items" :key="item.productId">
            <image v-if="item.productImage" :src="item.productImage" mode="aspectFill" class="goods-img" />
            <view class="goods-info">
              <text class="goods-name">{{ item.productName }}</text>
              <view class="goods-row">
                <text class="goods-price">¥{{ item.salePrice?.toFixed(2) }}</text>
                <text class="goods-qty">×{{ item.quantity }}</text>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="goods-item">
          <view class="goods-info">
            <text class="goods-name">商品详情暂不可用</text>
            <text class="goods-price">¥{{ order.totalAmount?.toFixed(2) }}</text>
          </view>
        </view>
      </view>

      <!-- 金额明细 -->
      <view class="card">
        <view class="card-title">
          <uni-icons type="wallet" size="18" color="#FF5000" />
          <text>金额明细</text>
        </view>
        <view class="amount-row"><text>商品总额</text><text>¥{{ order.totalAmount?.toFixed(2) }}</text></view>
        <view class="amount-row"><text>运费</text><text>¥{{ order.freightAmount?.toFixed(2) }}</text></view>
        <view class="amount-row" v-if="order.couponAmount > 0"><text>优惠券抵扣</text><text class="discount">-¥{{ order.couponAmount?.toFixed(2) }}</text></view>
        <view class="amount-row" v-if="order.integralAmount > 0"><text>积分抵扣</text><text class="discount">-¥{{ order.integralAmount?.toFixed(2) }}</text></view>
        <view class="amount-row total"><text>实付金额</text><text class="pay-amount">¥{{ order.payAmount?.toFixed(2) }}</text></view>
      </view>

      <!-- 订单时间线 -->
      <view class="card">
        <view class="card-title">
          <uni-icons type="list" size="18" color="#FF5000" />
          <text>订单信息</text>
        </view>
        <view class="timeline">
          <view class="tl-item" :class="{ done: order.createTime }">
            <view class="tl-dot"/><view class="tl-content"><text class="tl-label">下单时间</text><text class="tl-time">{{ formatTime(order.createTime) }}</text></view>
          </view>
          <view class="tl-item" :class="{ done: order.payTime }">
            <view class="tl-dot"/><view class="tl-content"><text class="tl-label">付款时间</text><text class="tl-time">{{ formatTime(order.payTime) || '未付款' }}</text></view>
          </view>
          <view class="tl-item" :class="{ done: order.deliveryTime }">
            <view class="tl-dot"/><view class="tl-content"><text class="tl-label">发货时间</text><text class="tl-time">{{ formatTime(order.deliveryTime) || '未发货' }}</text></view>
          </view>
          <view class="tl-item" :class="{ done: order.receiveTime }">
            <view class="tl-dot"/><view class="tl-content"><text class="tl-label">收货时间</text><text class="tl-time">{{ formatTime(order.receiveTime) || '未收货' }}</text></view>
          </view>
        </view>
      </view>

      <!-- 订单备注 -->
      <view class="card" v-if="order.remark">
        <view class="card-title">
          <uni-icons type="compose" size="18" color="#FF5000" />
          <text>订单备注</text>
        </view>
        <text class="remark-text">{{ order.remark }}</text>
      </view>

      <!-- 底部操作栏 -->
      <view class="action-bar" v-if="order.status === 0 || order.status === 1 || order.status === 2 || order.status === 5">
        <button v-if="order.status === 0" class="btn-primary" @click="goPay">去付款</button>
        <button v-if="order.status === 0" class="btn-outline" @click="cancelOrder">取消订单</button>
        <button v-if="order.status === 1 || order.status === 2" class="btn-outline warn" @click="applyRefund" :disabled="refunding">申请退款</button>
        <text v-if="order.status === 5" class="refund-status">退款审核中...</text>
        <button v-if="order.status === 2" class="btn-primary" @click="confirmReceive">确认收货</button>
      </view>
    </template>

    <!-- 支付方式弹窗 -->
    <view class="ps-mask" v-if="showPaySheet" @click="showPaySheet = false"/>
    <view class="ps-sheet" v-if="showPaySheet">
      <view class="ps-hd"><text>选择支付方式</text><text class="ps-amt">¥{{ order?.payAmount?.toFixed(2) }}</text></view>
      <view class="ps-item" v-for="m in payMethods" :key="m.value" @click="handlePay(m.value)">
        <uni-icons :type="m.icon" size="24" :color="m.value===3?'#FF9000':m.value===1?'#1677FF':'#07C160'" />
        <text class="ps-label">{{ m.label }}</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <button class="ps-close" @click="showPaySheet = false">取消</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.od { min-height: 100vh; background: #f5f5f5; padding-bottom: 140rpx; }
.loading { text-align: center; padding: 200rpx 0; color: #999; }

.status-bar { padding: 40rpx 30rpx; }
.status-label { font-size: 40rpx; font-weight: 700; color: #fff; display: block; }
.order-no { font-size: 24rpx; color: rgba(255,255,255,.8); display: block; margin-top: 8rpx; }

.card { background: #fff; margin: 16rpx 0; padding: 24rpx; }
.card-title { display: flex; align-items: center; gap: 12rpx; font-size: 28rpx; font-weight: 600; color: #333; margin-bottom: 16rpx; }

.addr-info { padding-left: 8rpx; }
.addr-top { display: flex; gap: 20rpx; margin-bottom: 8rpx; }
.addr-name { font-size: 28rpx; font-weight: 600; }
.addr-phone { font-size: 26rpx; color: #666; }
.addr-detail { font-size: 26rpx; color: #666; line-height: 1.5; }

.goods-item { display: flex; gap: 16rpx; padding: 12rpx 0; }
.goods-img { width: 120rpx; height: 120rpx; border-radius: 8rpx; flex-shrink: 0; }
.goods-info { flex: 1; }
.goods-name { font-size: 28rpx; color: #333; display: block; }
.goods-row { display: flex; align-items: center; gap: 12rpx; margin-top: 8rpx; }
.goods-price { font-size: 28rpx; color: #FF5000; font-weight: 600; }
.goods-qty { font-size: 24rpx; color: #999; }

.amount-row { display: flex; justify-content: space-between; padding: 8rpx 0; font-size: 26rpx; color: #666; }
.amount-row.total { border-top: 1rpx solid #f0f0f0; padding-top: 16rpx; margin-top: 8rpx; }
.discount { color: #FF5000; }
.pay-amount { font-size: 32rpx; font-weight: 700; color: #FF5000; }

.timeline { padding-left: 8rpx; }
.tl-item { display: flex; align-items: flex-start; gap: 16rpx; padding: 12rpx 0; position: relative; }
.tl-item:not(:last-child)::before { content: ''; position: absolute; left: 8rpx; top: 36rpx; bottom: -12rpx; width: 2rpx; background: #E5E5EA; }
.tl-dot { width: 16rpx; height: 16rpx; border-radius: 50%; background: #ddd; margin-top: 6rpx; flex-shrink: 0; }
.tl-item.done .tl-dot { background: #FF5000; }
.tl-content { flex: 1; }
.tl-label { font-size: 26rpx; color: #999; display: block; }
.tl-time { font-size: 24rpx; color: #666; }
.tl-item.done .tl-label { color: #333; }

.remark-text { font-size: 26rpx; color: #666; line-height: 1.5; }

.action-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; gap: 16rpx; padding: 16rpx 24rpx;
  background: #fff; box-shadow: 0 -2rpx 10rpx rgba(0,0,0,.05);
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
.btn-primary {
  flex: 1; height: 76rpx; line-height: 76rpx; background: #FF5000; color: #fff;
  border: none; border-radius: 38rpx; font-size: 28rpx; font-weight: 600;
}
.btn-outline {
  flex: 1; height: 76rpx; line-height: 76rpx; background: #fff; color: #666;
  border: 1rpx solid #ddd; border-radius: 38rpx; font-size: 28rpx;
}
.btn-outline.warn { border-color: #FF3B3B; color: #FF3B3B; }
.btn-outline:disabled { opacity: 0.5; }
.refund-status {
  flex: 1; height: 76rpx; line-height: 76rpx; text-align: center;
  border: 1rpx solid #FF5000; background: #FFF8F4; border-radius: 38rpx;
  font-size: 28rpx; color: #FF5000; font-weight: 500;
}

.ps-mask { position: fixed; inset: 0; background: rgba(0,0,0,.45); z-index: 100; }
.ps-sheet {
  position: fixed; left: 0; right: 0; bottom: 0; background: #fff;
  border-radius: 24rpx 24rpx 0 0; z-index: 101;
  padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
}
.ps-hd { text-align: center; margin-bottom: 20rpx; }
.ps-amt { display: block; font-size: 48rpx; font-weight: 700; color: #FF5000; margin-top: 8rpx; }
.ps-item {
  display: flex; align-items: center; gap: 16rpx; padding: 24rpx;
  border: 2rpx solid #f0f0f0; border-radius: 12rpx; margin-bottom: 12rpx;
}
.ps-icon { font-size: 40rpx; }
.ps-label { flex: 1; font-size: 30rpx; font-weight: 600; }
.ps-ar { font-size: 36rpx; color: #ccc; }
.ps-close {
  width: 100%; padding: 24rpx; background: #f5f5f5; border: none;
  border-radius: 12rpx; font-size: 28rpx; color: #666; margin-top: 8rpx;
}
</style>
