<script setup lang="ts">
defineOptions({ name: 'OrderDetailPage' })

import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NIcon, useMessage, useDialog } from 'naive-ui'
import { ArrowBackOutline, WalletOutline, LogoAlipay, LogoWechat, ChevronForwardOutline } from '@vicons/ionicons5'
import { orderApi, type OrderDetail } from '@shop/shared'
import LogisticsDetail from '@/components/LogisticsDetail.vue'

const message = useMessage()
const dialog = useDialog()

const route = useRoute()
const router = useRouter()
const order = ref<OrderDetail | null>(null)
const loading = ref(true)
const actionLoading = ref(false)
const showPayPicker = ref(false)

const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '待付款', color: '#FF5000' },
  1: { label: '待发货', color: '#FF8F1F' },
  2: { label: '待收货', color: '#00B578' },
  3: { label: '已完成', color: '#999' },
  4: { label: '已取消', color: '#999' },
  5: { label: '退款中', color: '#FF5000' },
  6: { label: '已退款', color: '#999' },
}

const payMethods = [
  { value: 3, label: '余额支付', icon: WalletOutline, desc: '使用账户余额支付', color: '#FF9000' },
  { value: 1, label: '支付宝', icon: LogoAlipay, desc: '使用支付宝支付', color: '#1677FF' },
  { value: 2, label: '微信支付', icon: LogoWechat, desc: '使用微信支付', color: '#07C160' },
]

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    order.value = await orderApi.getDetail(id)
  } catch { /* noop */ } finally { loading.value = false }
})

const openPayPicker = () => { showPayPicker.value = true }

const handlePayMethod = (payType: number) => {
  showPayPicker.value = false
  if (payType === 3) {
    handlePay(3, 'web')
  } else if (payType === 1) {
    handlePay(1, 'page')
  } else {
    handlePay(2, 'web')
  }
}

const handlePay = async (payType: number, scene: string) => {
  if (!order.value) return
  actionLoading.value = true
  try {
    if (payType === 3) {
      await orderApi.pay(order.value.id, 3)
    } else {
      const resp = await fetch('/api/v1/payments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` },
        body: JSON.stringify({ orderId: order.value.id, payMethod: payType, scene })
      }).then(r => r.json())
      if (resp.code === 200 && resp.data) {
        if (resp.data.formUrl) {
          // 页面跳转支付
          const formResp = await fetch(resp.data.formUrl, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
          })
          if (!formResp.ok) {
            const errText = await formResp.text()
            throw new Error(errText || '获取支付表单失败')
          }
          const formHtml = await formResp.text()
          const div = document.createElement('div')
          div.style.display = 'none'; div.innerHTML = formHtml
          document.body.appendChild(div)
          const payForm = div.querySelector('form') as HTMLFormElement | null
          if (!payForm) {
            document.body.removeChild(div)
            throw new Error('支付表单解析失败，请检查支付宝配置或使用余额支付')
          }
          payForm.submit()
        } else if (resp.data.codeUrl) {
          message.info('微信支付单号: ' + resp.data.paymentNo + '，请在开发环境手动确认支付')
        }
      } else {
        throw new Error(resp.message || '创建支付单失败')
      }
    }
    order.value = await orderApi.getDetail(order.value.id)
  } catch (e: any) {
    message.error('支付失败: ' + (e?.message || '未知错误'))
  } finally { actionLoading.value = false }
}

const handleCancel = async () => {
  if (!order.value) return
  actionLoading.value = true
  try { await orderApi.cancel(order.value.id, '用户取消'); order.value = await orderApi.getDetail(order.value.id) }
  catch (e: any) {
    message.error('取消失败: ' + (e?.message || '未知错误'))
  } finally { actionLoading.value = false }
}

const handleReceive = async () => {
  if (!order.value) return
  dialog.warning({
    title: '确认收货',
    content: '确认已收到商品？确认后将完成订单。',
    positiveText: '确认收货',
    negativeText: '取消',
    onPositiveClick: async () => {
      actionLoading.value = true
      try { await orderApi.confirmReceive(order.value!.id); order.value = await orderApi.getDetail(order.value!.id) }
      catch (e: any) { message.error('确认收货失败: ' + (e?.message || '未知错误')) } finally { actionLoading.value = false }
    }
  })
}

const handleRefund = async () => {
  if (!order.value || actionLoading.value) return
  if (order.value.status === 5) {
    message.warning('退款申请已提交，请等待审核')
    return
  }
  dialog.warning({
    title: '申请退款',
    content: '确认申请退款？退款申请提交后需等待管理员审核。',
    positiveText: '确认退款',
    negativeText: '取消',
    onPositiveClick: async () => {
      actionLoading.value = true
      try {
        await orderApi.refund(order.value!.id, '用户申请退款')
        message.success('退款申请已提交，等待审核')
        order.value = await orderApi.getDetail(order.value!.id)
      } catch (e: any) { message.error(e?.message || '退款申请失败') } finally { actionLoading.value = false }
    }
  })
}

const getMethodLabel = (t: number) => payMethods.find(m => m.value === t)?.label || '未知'
</script>

<template>
  <div class="od-page">
    <button class="od-back" @click="router.back()">
      <n-icon :size="16"><ArrowBackOutline /></n-icon>
      返回订单列表
    </button>

    <div v-if="loading" class="od-loading">加载中...</div>

    <template v-else-if="order">
      <div class="od-status-bar" :style="{ background: statusMap[order.status]?.color || '#999' }">
        <h2>{{ statusMap[order.status]?.label || '未知' }}</h2>
        <p class="od-no">{{ order.orderNo }}</p>
      </div>

      <div class="od-card">
        <h3>收货信息</h3>
        <div class="od-info-row"><span class="od-label">收货人</span><span>{{ order.receiverName }} {{ order.receiverPhone }}</span></div>
        <div class="od-info-row"><span class="od-label">地址</span><span>{{ order.receiverAddress }}</span></div>
      </div>

      <div class="od-card">
        <h3>金额明细</h3>
        <div class="od-info-row"><span class="od-label">商品总额</span><span>¥{{ order.totalAmount.toFixed(2) }}</span></div>
        <div class="od-info-row" v-if="order.freightAmount"><span class="od-label">运费</span><span>¥{{ order.freightAmount.toFixed(2) }}</span></div>
        <div class="od-info-row" v-if="order.couponAmount"><span class="od-label">优惠券</span><span class="od-discount">-¥{{ order.couponAmount.toFixed(2) }}</span></div>
        <div class="od-info-row" v-if="order.integralAmount"><span class="od-label">积分抵扣</span><span class="od-discount">-¥{{ order.integralAmount.toFixed(2) }}</span></div>
        <div class="od-divider" />
        <div class="od-info-row od-total"><span>实付金额</span><span class="od-pay">¥{{ order.payAmount.toFixed(2) }}</span></div>
        <div class="od-info-row" v-if="order.payType" style="margin-top:4px"><span class="od-label">支付方式</span><span>{{ getMethodLabel(order.payType) }}</span></div>
      </div>

      <!-- 物流信息 -->
      <div class="od-card" v-if="order.status === 2 || order.status === 3">
        <h3>物流信息</h3>
        <LogisticsDetail :order-id="order.id" />
      </div>

      <div class="od-card">
        <h3>订单时间线</h3>
        <div class="od-timeline">
          <div class="od-tl-item done">
            <span class="od-tl-dot" /> <span class="od-tl-text">下单</span>
            <span class="od-tl-time">{{ order.createTime?.slice(0,16).replace('T',' ') }}</span>
          </div>
          <div class="od-tl-item" :class="{ done: order.payTime }">
            <span class="od-tl-dot" /> <span class="od-tl-text">付款</span>
            <span class="od-tl-time">{{ order.payTime?.slice(0,16).replace('T',' ') || '待付款' }}</span>
          </div>
          <div class="od-tl-item" :class="{ done: order.deliveryTime }">
            <span class="od-tl-dot" /> <span class="od-tl-text">发货</span>
            <span class="od-tl-time">{{ order.deliveryTime?.slice(0,16).replace('T',' ') || '待发货' }}</span>
          </div>
          <div class="od-tl-item" :class="{ done: order.receiveTime }">
            <span class="od-tl-dot" /> <span class="od-tl-text">收货</span>
            <span class="od-tl-time">{{ order.receiveTime?.slice(0,16).replace('T',' ') || '待收货' }}</span>
          </div>
        </div>
      </div>

      <div class="od-card" v-if="order.remark">
        <h3>订单备注</h3>
        <p class="od-remark">{{ order.remark }}</p>
      </div>

      <div class="od-actions" v-if="order.status === 0 || order.status === 1 || order.status === 2 || order.status === 5">
        <button class="od-btn primary" v-if="order.status === 0" @click="openPayPicker" :disabled="actionLoading">
          {{ actionLoading ? '处理中...' : '去付款' }}
        </button>
        <button class="od-btn" v-if="order.status === 0" @click="handleCancel" :disabled="actionLoading">取消订单</button>
        <button class="od-btn warn" v-if="order.status === 1 || order.status === 2" @click="handleRefund" :disabled="actionLoading">申请退款</button>
        <span v-if="order.status === 5" class="od-refund-status">退款审核中...</span>
        <button class="od-btn primary" v-if="order.status === 2" @click="handleReceive" :disabled="actionLoading">
          {{ actionLoading ? '处理中...' : '确认收货' }}
        </button>
      </div>
    </template>

    <!-- 支付方式选择弹窗 -->
    <Teleport to="body">
      <div class="pp-overlay" v-if="showPayPicker" @click="showPayPicker = false" />
      <div class="pp-sheet" v-if="showPayPicker">
        <div class="pp-hd">
          <h3>选择支付方式</h3>
          <span class="pp-amount">¥{{ order?.payAmount.toFixed(2) }}</span>
        </div>
        <div class="pp-list">
          <div class="pp-item" v-for="m in payMethods" :key="m.value"
               :class="{ balance: m.value === 3 }" @click="handlePayMethod(m.value)">
            <n-icon :size="28" :color="m.color"><component :is="m.icon" /></n-icon>
            <div class="pp-info"><span class="pp-label">{{ m.label }}</span><span class="pp-desc">{{ m.desc }}</span></div>
            <n-icon :size="20" color="#ccc"><ChevronForwardOutline /></n-icon>
          </div>
        </div>
        <button class="pp-close" @click="showPayPicker = false">取消</button>
      </div>
    </Teleport>

  </div>
</template>

<style scoped lang="scss">
.od-page { max-width: 720px; }
.od-back {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 16px; border: 1px solid $border-color; background: #fff;
  border-radius: 18px; font-size: $font-size-sm; cursor: pointer;
  color: $text-secondary; margin-bottom: $spacing-lg;
  &:hover { border-color: $brand-orange; color: $brand-orange; }
}
.od-loading { text-align: center; padding: 80px 0; color: $text-hint; }
.od-status-bar {
  border-radius: $radius-md; padding: $spacing-2xl; margin-bottom: $spacing-lg;
  h2 { color: #fff; margin: 0 0 4px; font-size: $font-size-2xl; }
}
.od-no { color: rgba(255,255,255,0.8); font-size: $font-size-sm; margin: 0; }
.od-card {
  background: $bg-card; border-radius: $radius-md; box-shadow: $shadow-sm;
  padding: $spacing-xl $spacing-2xl; margin-bottom: $spacing-base;
  h3 { margin: 0 0 $spacing-base; font-size: $font-size-md; }
}
.od-info-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; font-size: $font-size-base; }
.od-label { color: $text-secondary; }
.od-discount { color: $color-success; }
.od-divider { height: 1px; background: $border-light; margin: $spacing-sm 0; }
.od-total { font-size: $font-size-md; font-weight: 700; }
.od-pay { color: $brand-orange; font-size: $font-size-xl; font-weight: 700; }
.od-remark { color: $text-secondary; font-size: $font-size-sm; margin: 0; }
.od-timeline { padding: $spacing-sm 0; }
.od-tl-item {
  display: flex; align-items: center; gap: $spacing-md; padding: 8px 0; color: $text-hint; font-size: $font-size-sm;
  &.done { color: $text-primary; .od-tl-dot { background: $brand-orange; border-color: $brand-orange; } }
}
.od-tl-dot { width: 10px; height: 10px; border-radius: 50%; border: 2px solid $border-color; background: #fff; flex-shrink: 0; }
.od-tl-text { width: 40px; flex-shrink: 0; }
.od-tl-time { margin-left: auto; }
.od-actions { display: flex; justify-content: center; gap: $spacing-md; margin-top: $spacing-xl; padding-bottom: $spacing-2xl; }
.od-btn {
  padding: 10px 32px; border: 1px solid $border-color; background: #fff;
  border-radius: 22px; font-size: $font-size-base; cursor: pointer;
  &:hover { border-color: $brand-orange; color: $brand-orange; }
  &:disabled { opacity: 0.5; cursor: default; }
  &.primary { background: $brand-gradient; color: #fff; border: none; &:hover { opacity: 0.9; } }
  &.warn { border-color: #FF3B3B; color: #FF3B3B; &:hover { background: #FFF0F0; } }
}
.od-refund-status {
  padding: 10px 32px; border: 1px solid #FF5000; background: #FFF8F4;
  border-radius: 22px; font-size: $font-size-base; color: #FF5000; font-weight: 500;
}
// Payment picker
.pp-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.45); z-index: 1000; }
.pp-sheet {
  position: fixed; left: 0; right: 0; bottom: 0; background: #fff; border-radius: 16px 16px 0 0;
  z-index: 1001; padding: 24px 20px 32px;
}
.pp-hd { text-align: center; margin-bottom: 20px; h3 { margin: 0; font-size: 18px; } }
.pp-amount { font-size: 28px; font-weight: 700; color: $brand-orange; }
.pp-list { margin-bottom: 16px; }
.pp-item {
  display: flex; align-items: center; gap: 14px; padding: 16px; border: 2px solid $border-light;
  border-radius: 12px; margin-bottom: 10px; cursor: pointer; transition: all .2s;
  &:hover, &.balance { border-color: $brand-orange; background: #FFF8F4; }
}
.pp-icon { font-size: 28px; }
.pp-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.pp-label { font-size: 16px; font-weight: 600; }
.pp-desc { font-size: 13px; color: $text-hint; }
.pp-arrow { font-size: 22px; color: #ccc; }
.pp-close {
  width: 100%; padding: 12px; background: #f5f5f5; border: none; border-radius: 10px;
  font-size: 16px; cursor: pointer; color: $text-secondary;
}
</style>
