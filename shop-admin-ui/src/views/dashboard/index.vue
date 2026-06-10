<script setup lang="ts">
defineOptions({ name: 'Dashboard' })

import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import { getDashboardStats, getSalesTrend, getRecentOrders, type DashboardStats, type SalesTrendItem, type RecentOrder } from '@/api/modules/dashboard'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

const stats = ref<DashboardStats | null>(null)
const recentOrders = ref<RecentOrder[]>([])
const loading = ref(true)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待付款', type: 'warning' },
  1: { label: '待发货', type: 'primary' },
  2: { label: '待收货', type: 'info' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'danger' },
  5: { label: '退款中', type: 'warning' },
  6: { label: '已退款', type: 'info' },
}

let chartInstance: echarts.ECharts | null = null

const initChart = async (trendData: SalesTrendItem[]) => {
  await nextTick()
  const el = document.getElementById('sales-chart')
  if (!el) return

  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(el)

  const dates = trendData.map((d) => d.date.slice(5))
  const amounts = trendData.map((d) => d.amount)

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E5E5EA',
      borderWidth: 1,
      textStyle: { color: '#1C1C1E', fontSize: 13 },
      boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
      formatter: (params: { value: number; axisValue: string }[]) =>
        `${params[0].axisValue}<br/><strong>¥${params[0].value.toFixed(2)}</strong>`,
    },
    grid: { left: 48, right: 24, top: 24, bottom: 28 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#E5E5EA' } },
      axisTick: { show: false },
      axisLabel: { color: '#8E8E93', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F0F0F5' } },
      axisLabel: { color: '#8E8E93', fontSize: 12, formatter: (v: number) => `¥${v}` },
    },
    series: [{
      data: amounts,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#FF5000', width: 3 },
      itemStyle: { color: '#FF5000', borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255,80,0,0.2)' },
          { offset: 1, color: 'rgba(255,80,0,0.02)' },
        ]),
      },
    }],
  })
}

onMounted(async () => {
  try {
    const [s, trend, orders] = await Promise.all([
      getDashboardStats(),
      getSalesTrend(),
      getRecentOrders(),
    ])
    stats.value = s
    recentOrders.value = orders
    await initChart(trend)
  } catch { /* noop */ } finally { loading.value = false }
})

onUnmounted(() => { chartInstance?.dispose() })

const statCards = computed(() => {
  if (!stats.value) return []
  const s = stats.value
  return [
    { title: '今日订单', value: s.todayOrders, change: s.orderChange, icon: 'order', gradient: 'linear-gradient(135deg, #FF5000, #FF8C3A)', bg: '#FFF2EC' },
    { title: '今日销售额', value: `¥${(s.todaySales || 0).toFixed(2)}`, change: s.salesChange, icon: 'sales', gradient: 'linear-gradient(135deg, #00B578, #00D68F)', bg: '#E8F9F1' },
    { title: '新增用户', value: s.todayNewUsers, change: s.userChange, icon: 'user', gradient: 'linear-gradient(135deg, #4A90D9, #6DB3F2)', bg: '#EEF4FD' },
    { title: '待处理订单', value: s.pendingOrders, change: s.pendingChange, icon: 'pending', gradient: 'linear-gradient(135deg, #FF3B3B, #FF6B6B)', bg: '#FFEDED' },
  ]
})
</script>

<template>
  <div class="dashboard">
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="welcome-title">{{ greeting }} 👋</h1>
        <p class="welcome-sub">欢迎回到 ShopMax 商家中心，以下是今日的经营概况</p>
      </div>
      <div class="welcome-date">
        <span class="date-text">{{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}</span>
      </div>
    </div>

    <!-- Admin Dashboard -->
    <template v-if="userStore.isAdmin">
      <div class="stat-grid" v-if="!loading">
        <div v-for="item in statCards" :key="item.title" class="stat-card">
          <div class="stat-body">
            <div class="stat-info">
              <span class="stat-title">{{ item.title }}</span>
              <span class="stat-value">{{ item.value }}</span>
            </div>
            <div class="stat-icon-wrap" :style="{ background: item.bg }">
              <div class="stat-icon-inner" :style="{ background: item.gradient }">
                <svg v-if="item.icon === 'order'" viewBox="0 0 24 24" fill="none" width="22" height="22">
                  <path d="M8 2v4h8V2M3 6v15a1 1 0 0 0 1 1h16a1 1 0 0 0 1-1V6a1 1 0 0 0-1-1H4a1 1 0 0 0-1 1Z" stroke="#fff" stroke-width="1.8"/>
                  <path d="M8 11h8M8 15h5" stroke="#fff" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
                <svg v-else-if="item.icon === 'sales'" viewBox="0 0 24 24" fill="none" width="22" height="22">
                  <rect x="2" y="4" width="20" height="16" rx="3" stroke="#fff" stroke-width="1.8"/>
                  <circle cx="12" cy="12" r="3" stroke="#fff" stroke-width="1.8"/>
                  <path d="M12 7v1M12 16v1" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <svg v-else-if="item.icon === 'user'" viewBox="0 0 24 24" fill="none" width="22" height="22">
                  <circle cx="12" cy="8" r="4" stroke="#fff" stroke-width="1.8"/>
                  <path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1" stroke="#fff" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
                <svg v-else-if="item.icon === 'pending'" viewBox="0 0 24 24" fill="none" width="22" height="22">
                  <path d="M12 2L2 20h20L12 2Z" stroke="#fff" stroke-width="1.8" stroke-linejoin="round"/>
                  <path d="M12 10v4M12 17v.5" stroke="#fff" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="stat-grid">
        <div v-for="i in 4" :key="i" class="stat-card skeleton"><div class="skel-line" /><div class="skel-line short" /></div>
      </div>

      <div class="content-grid">
        <div class="chart-card">
          <div class="card-header">
            <h3>近7天销售趋势</h3>
          </div>
          <div class="chart-wrap" id="sales-chart" />
        </div>

        <div class="orders-card">
          <div class="card-header">
            <h3>最新订单</h3>
            <a class="view-all" @click="router.push('/order')">查看全部 →</a>
          </div>
          <div class="orders-list" v-if="recentOrders.length">
            <div v-for="order in recentOrders" :key="order.id" class="order-item">
              <div class="order-info">
                <span class="order-no">{{ order.orderNo }}</span>
              </div>
              <div class="order-meta">
                <el-tag :type="statusMap[order.status]?.type || 'info'" size="small" effect="plain">
                  {{ statusMap[order.status]?.label }}
                </el-tag>
                <span class="order-amount">¥{{ Number(order.amount).toFixed(2) }}</span>
              </div>
              <span class="order-time">{{ order.time?.slice(11, 19) }}</span>
            </div>
          </div>
          <div class="orders-empty" v-else>暂无订单</div>
        </div>
      </div>
    </template>

    <!-- Store Dashboard -->
    <template v-if="userStore.isStore">
      <div v-if="userStore.storeStatus === 0" class="audit-notice">
        <el-result icon="info" title="入驻审核中" sub-title="您的店铺正在审核中，审核通过后即可管理店铺">
          <template #extra>
            <el-button type="primary" @click="router.push('/profile')">查看申请进度</el-button>
          </template>
        </el-result>
      </div>
      <div v-else class="store-welcome">
        <div class="welcome-card">
          <h2>欢迎 {{ userStore.userInfo?.storeName || userStore.userName }} 🏪</h2>
          <p>管理您的店铺商品和订单</p>
          <div class="quick-links">
            <el-button type="primary" @click="router.push('/product')">商品管理</el-button>
            <el-button @click="router.push('/order')">订单管理</el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- User Dashboard -->
    <template v-if="!userStore.isAdmin && !userStore.isStore">
      <div class="user-welcome">
        <div class="welcome-card">
          <h2>欢迎回来 👋</h2>
          <p>探索 ShopMax 商城，发现好物</p>
          <div class="quick-links">
            <el-button type="primary" @click="router.push('/community')">内容社区</el-button>
            <el-button @click="router.push('/profile')">个人中心</el-button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 28px 32px;
  box-shadow: $shadow-sm;
  border: 1px solid $border-color-light;
  .welcome-title { font-size: $font-size-2xl; font-weight: 700; color: $text-primary; margin-bottom: 6px; }
  .welcome-sub { font-size: $font-size-base; color: $text-secondary; }
  .date-text { font-size: $font-size-sm; color: $text-secondary; background: #F5F5F7; padding: 8px 16px; border-radius: $radius-full; }
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 24px;
  box-shadow: $shadow-sm;
  border: 1px solid $border-color-light;
  transition: all $transition-base;
  &:hover { box-shadow: $shadow-md; transform: translateY(-2px); }
  &.skeleton { height: 120px; display: flex; flex-direction: column; gap: 12px; justify-content: center; padding: 24px; }
}

.skel-line { height: 20px; background: #f0f0f0; border-radius: 4px; &.short { width: 50%; } }

.stat-body { display: flex; justify-content: space-between; align-items: flex-start; }
.stat-info { display: flex; flex-direction: column; gap: 6px; }
.stat-title { font-size: $font-size-sm; color: $text-secondary; font-weight: 500; }
.stat-value { font-size: 28px; font-weight: 700; color: $text-primary; }

.stat-icon-wrap {
  width: 52px; height: 52px; border-radius: $radius-md;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-icon-inner {
  width: 40px; height: 40px; border-radius: $radius-sm;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}

.content-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 16px;
}

.chart-card, .orders-card {
  background: $bg-white;
  border-radius: $radius-lg;
  box-shadow: $shadow-sm;
  border: 1px solid $border-color-light;
  overflow: hidden;
}

.card-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 1px solid $border-color-light;
  h3 { font-size: $font-size-md; font-weight: 600; color: $text-primary; margin: 0; }
}

.view-all { font-size: $font-size-sm; color: $brand-orange; cursor: pointer; font-weight: 500; &:hover { opacity: 0.8; } }

.chart-wrap { height: 300px; width: 100%; }

.orders-list { padding: 8px 24px; }
.orders-empty { text-align: center; padding: 60px 0; color: $text-placeholder; font-size: $font-size-sm; }

.order-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 0; border-bottom: 1px solid $border-color-lighter;
  &:last-child { border-bottom: none; }
  &:hover { background: #FFFAF8; margin: 0 -12px; padding: 14px 12px; border-radius: $radius-sm; border-bottom-color: transparent; }
}

.order-info {
  min-width: 140px;
  .order-no { font-size: $font-size-sm; color: $text-primary; font-weight: 600; }
}

.order-meta { display: flex; align-items: center; gap: 12px; }
.order-amount { font-size: $font-size-base; font-weight: 600; color: $text-primary; }
.order-time { font-size: $font-size-xs; color: $text-placeholder; min-width: 60px; text-align: right; }

.welcome-card {
  background: $bg-white; border-radius: $radius-lg; padding: 48px; text-align: center; box-shadow: $shadow-sm;
  h2 { font-size: 24px; color: $text-primary; margin-bottom: 8px; }
  p { color: $text-secondary; margin-bottom: 24px; }
  .quick-links { display: flex; gap: 12px; justify-content: center; }
}

.audit-notice { background: $bg-white; border-radius: $radius-lg; padding: 48px; box-shadow: $shadow-sm; }

@media (max-width: 1400px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .content-grid { grid-template-columns: 1fr; }
}
</style>
