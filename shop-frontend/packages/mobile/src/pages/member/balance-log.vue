<script setup lang="ts">
defineOptions({ name: 'BalanceLogPage' })
import { ref, onMounted } from 'vue'
import { walletApi, type BalanceLog, type MemberInfo } from '@shop/shared'

const memberInfo = ref<MemberInfo | null>(null)
const logs = ref<BalanceLog[]>([])
const loading = ref(false)
const hasMore = ref(true)
let page = 1
const activeTab = ref<'all' | 'charge' | 'pay' | 'refund'>('all')

const typeLabels: Record<number, string> = { 1: '充值', 2: '支付', 3: '退款', 4: '提现', 5: '管理员调整' }
const typeTagColors: Record<number, string> = { 1: '#00B578', 2: '#FF5000', 3: '#409EFF', 4: '#E6A23C', 5: '#909399' }

const filteredLogs = ref<BalanceLog[]>([])

const fetchMemberInfo = async () => {
  try { memberInfo.value = await walletApi.getMemberInfo() } catch { /* ignore */ }
}

const fetchLogs = async (refresh = false) => {
  if (refresh) { page = 1; hasMore.value = true }
  if (!hasMore.value || loading.value) return
  loading.value = true
  try {
    const res = await walletApi.getBalanceLogs({ pageNum: page, pageSize: 20 })
    const data = res as any
    if (refresh) logs.value = data.records || []
    else logs.value.push(...(data.records || []))
    hasMore.value = data.current < data.pages
    page++
    applyFilter()
  } catch { /* ignore */ } finally { loading.value = false }
}

const applyFilter = () => {
  if (activeTab.value === 'charge') filteredLogs.value = logs.value.filter(l => l.type === 1)
  else if (activeTab.value === 'pay') filteredLogs.value = logs.value.filter(l => l.type === 2)
  else if (activeTab.value === 'refund') filteredLogs.value = logs.value.filter(l => l.type === 3)
  else filteredLogs.value = logs.value
}

const switchTab = (tab: typeof activeTab.value) => { activeTab.value = tab; applyFilter() }

const goRecharge = () => uni.navigateTo({ url: '/pages/member/recharge' })

onMounted(() => { fetchMemberInfo(); fetchLogs(true) })
</script>

<template>
  <view class="page">
    <!-- 顶部余额 -->
    <view class="header">
      <text class="header-label">账户余额(元)</text>
      <text class="header-value">{{ memberInfo?.balance?.toFixed(2) || '0.00' }}</text>
      <view class="recharge-btn" @click="goRecharge">充值</view>
    </view>

    <!-- Tab -->
    <view class="tabs">
      <view class="tab" :class="{ active: activeTab === 'all' }" @click="switchTab('all')">全部</view>
      <view class="tab" :class="{ active: activeTab === 'charge' }" @click="switchTab('charge')">充值</view>
      <view class="tab" :class="{ active: activeTab === 'pay' }" @click="switchTab('pay')">消费</view>
      <view class="tab" :class="{ active: activeTab === 'refund' }" @click="switchTab('refund')">退款</view>
    </view>

    <!-- 列表 -->
    <scroll-view scroll-y class="list" @scrolltolower="fetchLogs()">
      <view class="log-item" v-for="log in filteredLogs" :key="log.id">
        <view class="log-left">
          <view class="log-tag" :style="{ background: typeTagColors[log.type] || '#999' }">{{ typeLabels[log.type] || '其他' }}</view>
          <text class="log-desc">{{ log.remark || '' }}</text>
          <text class="log-time">{{ log.createTime }}</text>
        </view>
        <view class="log-right">
          <text class="log-amount" :class="{ positive: log.changeAmount > 0 }">
            {{ log.changeAmount > 0 ? '+' : '' }}{{ (log.changeAmount ?? 0).toFixed(2) }}
          </text>
          <text class="log-after">余额 {{ (log.afterAmount ?? 0).toFixed(2) }}</text>
        </view>
      </view>
      <view class="loading-more" v-if="loading">加载中...</view>
      <view class="loading-more" v-else-if="!hasMore && filteredLogs.length">— 没有更多了 —</view>
      <view class="empty" v-if="!loading && !filteredLogs.length">暂无余额记录</view>
    </scroll-view>
  </view>
</template>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }

.header { background: linear-gradient(135deg, #FF5000, #FF8C3A); padding: 32px 20px 24px; text-align: center; color: #fff; position: relative; }
.header-label { font-size: 14px; opacity: 0.85; display: block; }
.header-value { font-size: 40px; font-weight: 700; margin-top: 4px; display: block; }
.recharge-btn { position: absolute; right: 20px; top: 50%; transform: translateY(-50%); background: rgba(255,255,255,0.25); padding: 8px 20px; border-radius: 20px; font-size: 14px; }

.tabs { display: flex; background: #fff; padding: 0 16px; border-bottom: 1px solid #f0f0f0; }
.tab { flex: 1; text-align: center; padding: 12px 0; font-size: 14px; color: #666; position: relative; }
.tab.active { color: #FF5000; font-weight: 600; }
.tab.active::after { content: ''; position: absolute; bottom: 0; left: 30%; right: 30%; height: 2px; background: #FF5000; border-radius: 1px; }

.list { height: calc(100vh - 200px); }
.log-item { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; background: #fff; border-bottom: 1px solid #f5f5f5; }
.log-left { flex: 1; }
.log-tag { display: inline-block; font-size: 11px; color: #fff; padding: 2px 8px; border-radius: 4px; margin-bottom: 4px; }
.log-desc { font-size: 15px; color: #333; display: block; }
.log-time { font-size: 12px; color: #999; margin-top: 4px; display: block; }
.log-right { text-align: right; }
.log-amount { font-size: 18px; font-weight: 600; color: #FF5000; display: block; }
.log-amount.positive { color: #00B578; }
.log-after { font-size: 12px; color: #999; margin-top: 2px; display: block; }

.loading-more { text-align: center; padding: 20px; font-size: 13px; color: #999; }
.empty { text-align: center; padding: 60px; font-size: 14px; color: #999; }
</style>
