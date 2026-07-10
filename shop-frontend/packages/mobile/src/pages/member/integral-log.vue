<script setup lang="ts">
defineOptions({ name: 'IntegralLogPage' })
import { ref, onMounted } from 'vue'
import { walletApi, type IntegralLog, type MemberInfo } from '@shop/shared'

const memberInfo = ref<MemberInfo | null>(null)
const logs = ref<IntegralLog[]>([])
const loading = ref(false)
const hasMore = ref(true)
let page = 1
const activeTab = ref<'all' | 'in' | 'out'>('all')

const typeLabels: Record<number, string> = {
  1: '注册赠送', 2: '邀请奖励', 3: '订单完成', 4: '积分兑换', 5: '积分支付', 6: '退款退回', 7: '管理员调整'
}

const filteredLogs = ref<IntegralLog[]>([])

const fetchMemberInfo = async () => {
  try { memberInfo.value = await walletApi.getMemberInfo() } catch { /* ignore */ }
}

const fetchLogs = async (refresh = false) => {
  if (refresh) { page = 1; hasMore.value = true }
  if (!hasMore.value || loading.value) return
  loading.value = true
  try {
    const res = await walletApi.getIntegralLogs({ pageNum: page, pageSize: 20 })
    const data = res as any
    if (refresh) logs.value = data.records || []
    else logs.value.push(...(data.records || []))
    hasMore.value = data.current < data.pages
    page++
    applyFilter()
  } catch { /* ignore */ } finally { loading.value = false }
}

const applyFilter = () => {
  if (activeTab.value === 'in') filteredLogs.value = logs.value.filter(l => l.changeAmount > 0)
  else if (activeTab.value === 'out') filteredLogs.value = logs.value.filter(l => l.changeAmount < 0)
  else filteredLogs.value = logs.value
}

const switchTab = (tab: 'all' | 'in' | 'out') => {
  activeTab.value = tab
  applyFilter()
}

onMounted(() => { fetchMemberInfo(); fetchLogs(true) })
</script>

<template>
  <view class="page">
    <!-- 顶部积分总数 -->
    <view class="header">
      <text class="header-label">当前积分</text>
      <text class="header-value">{{ memberInfo?.integral || 0 }}</text>
    </view>

    <!-- Tab -->
    <view class="tabs">
      <view class="tab" :class="{ active: activeTab === 'all' }" @click="switchTab('all')">全部</view>
      <view class="tab" :class="{ active: activeTab === 'in' }" @click="switchTab('in')">收入</view>
      <view class="tab" :class="{ active: activeTab === 'out' }" @click="switchTab('out')">支出</view>
    </view>

    <!-- 列表 -->
    <scroll-view scroll-y class="list" @scrolltolower="fetchLogs()">
      <view class="log-item" v-for="log in filteredLogs" :key="log.id">
        <view class="log-left">
          <text class="log-desc">{{ log.remark || typeLabels[log.type] || '积分变动' }}</text>
          <text class="log-time">{{ log.createTime }}</text>
        </view>
        <view class="log-right">
          <text class="log-amount" :class="{ positive: log.changeAmount > 0 }">
            {{ log.changeAmount > 0 ? '+' : '' }}{{ log.changeAmount }}
          </text>
          <text class="log-after">余额 {{ log.afterAmount }}</text>
        </view>
      </view>
      <view class="loading-more" v-if="loading">加载中...</view>
      <view class="loading-more" v-else-if="!hasMore && filteredLogs.length">— 没有更多了 —</view>
      <view class="empty" v-if="!loading && !filteredLogs.length">暂无积分记录</view>
    </scroll-view>
  </view>
</template>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }

.header { background: linear-gradient(135deg, #FF5000, #FF8C3A); padding: 32px 20px 24px; text-align: center; color: #fff; }
.header-label { font-size: 14px; opacity: 0.85; display: block; }
.header-value { font-size: 40px; font-weight: 700; margin-top: 4px; display: block; }

.tabs { display: flex; background: #fff; padding: 0 16px; border-bottom: 1px solid #f0f0f0; }
.tab { flex: 1; text-align: center; padding: 12px 0; font-size: 14px; color: #666; position: relative; }
.tab.active { color: #FF5000; font-weight: 600; }
.tab.active::after { content: ''; position: absolute; bottom: 0; left: 30%; right: 30%; height: 2px; background: #FF5000; border-radius: 1px; }

.list { height: calc(100vh - 200px); }
.log-item { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; background: #fff; border-bottom: 1px solid #f5f5f5; }
.log-left { flex: 1; }
.log-desc { font-size: 15px; color: #333; display: block; }
.log-time { font-size: 12px; color: #999; margin-top: 4px; display: block; }
.log-right { text-align: right; }
.log-amount { font-size: 18px; font-weight: 600; color: #FF5000; display: block; }
.log-amount.positive { color: #00B578; }
.log-after { font-size: 12px; color: #999; margin-top: 2px; display: block; }

.loading-more { text-align: center; padding: 20px; font-size: 13px; color: #999; }
.empty { text-align: center; padding: 60px; font-size: 14px; color: #999; }
</style>
