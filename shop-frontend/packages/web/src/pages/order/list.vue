<script setup lang="ts">defineOptions({ name: 'OrderList' })
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi, type OrderDetail } from '@shop/shared'

const router = useRouter()
const orders = ref<OrderDetail[]>([])
const activeTab = ref(-1)
const refundingId = ref<number | null>(null)

const tabs = [
  { name: '全部', status: -1 },
  { name: '待付款', status: 0 },
  { name: '待发货', status: 1 },
  { name: '待收货', status: 2 },
  { name: '已完成', status: 3 },
]
const statusMap: Record<number, string> = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款' }

const filtered = computed(() => activeTab.value === -1 ? orders.value : orders.value.filter(o => o.status === activeTab.value))

onMounted(async () => { try { orders.value = await orderApi.getMyOrders() } catch { /* noop */ } })

const goDetail = (id: number) => router.push(`/order/${id}`)
const handleReceive = async (o: OrderDetail) => {
  const r = confirm('确认已收到商品？确认后将完成订单。')
  if (r) {
    await orderApi.confirmReceive(o.id)
    orders.value = await orderApi.getMyOrders()
  }
}
const handleCancel = async (o: OrderDetail) => {
  await orderApi.cancel(o.id, '取消')
  orders.value = await orderApi.getMyOrders()
}
const handleRefund = async (o: OrderDetail) => {
  if (refundingId.value === o.id) return
  if (o.status === 5) {
    alert('退款申请已提交，请等待审核')
    return
  }
  const r = confirm('确认申请退款？退款申请提交后需等待管理员审核。')
  if (!r) return
  refundingId.value = o.id
  try {
    await orderApi.refund(o.id, '用户申请退款')
    alert('退款申请已提交，等待审核')
    orders.value = await orderApi.getMyOrders()
  } catch (e: any) {
    alert(e?.message || '退款申请失败')
  } finally {
    refundingId.value = null
  }
}
</script>

<template>
  <div class="ol-page">
    <h2>我的订单</h2>

    <div class="tabs">
      <span v-for="t in tabs" :key="t.status" :class="{ active: activeTab === t.status }" @click="activeTab = t.status">{{ t.name }}</span>
    </div>

    <div v-if="!filtered.length" class="empty">暂无订单</div>

    <div class="order-card" v-for="o in filtered" :key="o.id">
      <div class="o-header">
        <span class="o-no">{{ o.orderNo }}</span>
        <span class="o-status" :style="{ color: o.status === 0 ? '#FF5000' : o.status === 2 ? '#00B578' : '#666' }">{{ statusMap[o.status] }}</span>
      </div>
      <div class="o-body">
        <span>{{ o.receiverName }} {{ o.receiverPhone }}</span>
        <span class="o-amount">¥{{ o.payAmount }}</span>
      </div>
      <div class="o-time">{{ o.createTime }}</div>
      <div class="o-actions">
        <button v-if="o.status === 0" class="btn primary" @click="goDetail(o.id)">去付款</button>
        <button v-if="o.status === 0" class="btn" @click="handleCancel(o)">取消</button>
        <button v-if="o.status === 1 || o.status === 2" class="btn warn" @click="handleRefund(o)" :disabled="refundingId === o.id">申请退款</button>
        <span v-if="o.status === 5" class="refund-tag">退款审核中</span>
        <button v-if="o.status === 2" class="btn primary" @click="handleReceive(o)">确认收货</button>
        <button v-if="o.status === 2 || o.status === 3" class="btn" @click="goDetail(o.id)">查看物流</button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
h2 { margin-bottom: 20px; }
.tabs { display: flex; gap: 0; border-bottom: 2px solid #e5e5ea; margin-bottom: 20px;
  span { padding: 10px 20px; font-size: 14px; color: #666; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px;
    &.active { color: #FF5000; border-bottom-color: #FF5000; font-weight: 600; }
  }
}
.empty { text-align: center; padding: 80px 0; color: #999; }
.order-card { background: #fff; border-radius: 12px; padding: 16px 20px; margin-bottom: 12px; }
.o-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.o-no { font-size: 13px; color: #666; }
.o-status { font-size: 13px; font-weight: 600; }
.o-body { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; }
.o-amount { font-weight: 700; color: #FF5000; font-size: 16px; }
.o-time { font-size: 12px; color: #999; margin-bottom: 12px; }
.o-actions { display: flex; justify-content: flex-end; gap: 8px; }
.btn { padding: 6px 16px; border-radius: 14px; border: 1px solid #ddd; background: #fff; font-size: 12px; cursor: pointer; &.primary { background: #FF5000; color: #fff; border-color: #FF5000; } &.warn { border-color: #FF3B3B; color: #FF3B3B; } &:disabled { opacity: 0.5; cursor: default; } }
.refund-tag { padding: 6px 16px; border-radius: 14px; border: 1px solid #FF5000; background: #FFF8F4; font-size: 12px; color: #FF5000; }
</style>
