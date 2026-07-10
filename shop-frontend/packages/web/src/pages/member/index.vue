<script setup lang="ts">
defineOptions({ name: 'MemberCenterPage' })
import { ref, onMounted, computed } from 'vue'
import { NTabs, NTabPane, NButton, NEmpty, NTag } from 'naive-ui'
import { walletApi, type MemberInfo, type IntegralLog, type BalanceLog } from '@shop/shared'

const memberInfo = ref<MemberInfo | null>(null)
const loading = ref(true)
const activeTab = ref('integral')

// 积分流水
const integralLogs = ref<IntegralLog[]>([])
const integralLoading = ref(false)
const integralHasMore = ref(true)
let integralPage = 1

// 余额流水
const balanceLogs = ref<BalanceLog[]>([])
const balanceLoading = ref(false)
const balanceHasMore = ref(true)
let balancePage = 1

// 充值
const rechargeAmount = ref('')
const payChannel = ref('wxpay')
const rechargeLoading = ref(false)
const presetAmounts = [50, 100, 200, 500]

const levelColors: Record<number, string> = {
  1: 'linear-gradient(135deg, #909399, #b0b3b8)',
  2: 'linear-gradient(135deg, #409EFF, #66b1ff)',
  3: 'linear-gradient(135deg, #E6A23C, #f0c78a)',
  4: 'linear-gradient(135deg, #9B59B6, #c39bd3)'
}

const progressPercent = computed(() => {
  if (!memberInfo.value) return 0
  const { growthValue, nextLevelGrowth, memberLevel } = memberInfo.value
  if (memberLevel >= 4) return 100
  const prevThreshold = memberInfo.value.levelBenefits[memberLevel - 1]?.threshold || 0
  const range = nextLevelGrowth - prevThreshold
  return range > 0 ? Math.min(100, ((growthValue - prevThreshold) / range) * 100) : 0
})

const integralTypeLabels: Record<number, string> = {
  1: '注册赠送', 2: '邀请奖励', 3: '订单完成', 4: '积分兑换', 5: '积分支付', 6: '退款退回', 7: '管理员调整'
}
const balanceTypeLabels: Record<number, string> = { 1: '充值', 2: '支付', 3: '退款', 4: '提现', 5: '管理员调整' }
const balanceTypeColors: Record<number, string> = { 1: 'success', 2: 'error', 3: 'info', 4: 'warning', 5: 'default' }

const fetchMemberInfo = async () => {
  try { memberInfo.value = await walletApi.getMemberInfo() } catch { /* ignore */ } finally { loading.value = false }
}

const fetchIntegralLogs = async (refresh = false) => {
  if (refresh) { integralPage = 1; integralHasMore.value = true }
  if (!integralHasMore.value || integralLoading.value) return
  integralLoading.value = true
  try {
    const res = await walletApi.getIntegralLogs({ pageNum: integralPage, pageSize: 20 })
    const data = res as any
    if (refresh) integralLogs.value = data.records || []
    else integralLogs.value.push(...(data.records || []))
    integralHasMore.value = data.current < data.pages
    integralPage++
  } catch { /* ignore */ } finally { integralLoading.value = false }
}

const fetchBalanceLogs = async (refresh = false) => {
  if (refresh) { balancePage = 1; balanceHasMore.value = true }
  if (!balanceHasMore.value || balanceLoading.value) return
  balanceLoading.value = true
  try {
    const res = await walletApi.getBalanceLogs({ pageNum: balancePage, pageSize: 20 })
    const data = res as any
    if (refresh) balanceLogs.value = data.records || []
    else balanceLogs.value.push(...(data.records || []))
    balanceHasMore.value = data.current < data.pages
    balancePage++
  } catch { /* ignore */ } finally { balanceLoading.value = false }
}

const handleRecharge = async () => {
  const val = parseFloat(rechargeAmount.value)
  if (!val || val < 0.01) return
  rechargeLoading.value = true
  try {
    await walletApi.recharge({ amount: val, payChannel: payChannel.value })
    rechargeAmount.value = ''
    fetchMemberInfo()
  } catch { /* ignore */ } finally { rechargeLoading.value = false }
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
  if (tab === 'integral' && integralLogs.value.length === 0) fetchIntegralLogs(true)
  if (tab === 'balance' && balanceLogs.value.length === 0) fetchBalanceLogs(true)
}

onMounted(() => {
  fetchMemberInfo()
  fetchIntegralLogs(true)
})
</script>

<template>
  <div class="member-page" v-loading="loading">
    <!-- 等级卡片 -->
    <div class="level-card" :style="{ background: levelColors[memberInfo?.memberLevel || 1] }">
      <div class="level-left">
        <div class="level-badge">{{ memberInfo?.memberLevelName?.charAt(0) || '普' }}</div>
        <div class="level-info">
          <div class="level-name">{{ memberInfo?.memberLevelName || '普通会员' }}</div>
          <div class="level-desc" v-if="memberInfo && memberInfo.memberLevel < 4">
            再获 {{ memberInfo.nextLevelGrowth - memberInfo.growthValue }} 成长值升级为{{ memberInfo.nextLevelName }}
          </div>
          <div class="level-desc" v-else>已达最高等级</div>
        </div>
      </div>
      <div class="level-right">
        <div class="progress-wrap">
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <div class="progress-labels">
            <span>{{ memberInfo?.growthValue || 0 }}</span>
            <span>{{ memberInfo?.memberLevel >= 4 ? 'MAX' : memberInfo?.nextLevelGrowth || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据卡片 + 权益表 -->
    <div class="content-row">
      <div class="left-col">
        <div class="data-cards">
          <div class="data-card">
            <div class="data-value">{{ memberInfo?.integral || 0 }}</div>
            <div class="data-label">积分</div>
          </div>
          <div class="data-card">
            <div class="data-value">¥{{ memberInfo?.balance?.toFixed(2) || '0.00' }}</div>
            <div class="data-label">余额</div>
          </div>
          <div class="data-card">
            <div class="data-value">{{ memberInfo?.growthValue || 0 }}</div>
            <div class="data-label">成长值</div>
          </div>
        </div>
      </div>
      <div class="right-col">
        <div class="benefits-card">
          <h3>等级权益</h3>
          <div class="benefit-row" v-for="b in memberInfo?.levelBenefits" :key="b.level"
               :class="{ active: b.level === memberInfo?.memberLevel }">
            <span class="b-name">{{ b.name }}</span>
            <span class="b-discount">{{ b.discount }}</span>
            <span class="b-threshold">{{ b.threshold }}成长值</span>
            <n-tag v-if="b.level === memberInfo?.memberLevel" type="success" size="small">当前</n-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 区域 -->
    <div class="tab-section">
      <n-tabs v-model:value="activeTab" type="line" @update:value="handleTabChange">
        <n-tab-pane name="integral" tab="积分明细">
          <div class="log-list">
            <div class="log-item" v-for="log in integralLogs" :key="log.id">
              <div class="log-left">
                <div class="log-title">{{ log.remark || integralTypeLabels[log.type] || '积分变动' }}</div>
                <div class="log-time">{{ log.createTime }}</div>
              </div>
              <div class="log-right">
                <div class="log-amount" :class="{ positive: log.changeAmount > 0 }">
                  {{ log.changeAmount > 0 ? '+' : '' }}{{ log.changeAmount }}
                </div>
                <div class="log-after">余额 {{ log.afterAmount }}</div>
              </div>
            </div>
            <n-empty v-if="!integralLoading && !integralLogs.length" description="暂无积分记录" />
            <div class="load-more" v-if="integralLoading">加载中...</div>
          </div>
        </n-tab-pane>

        <n-tab-pane name="balance" tab="余额明细">
          <div class="log-list">
            <div class="log-item" v-for="log in balanceLogs" :key="log.id">
              <div class="log-left">
                <n-tag :type="balanceTypeColors[log.type] as any" size="small">{{ balanceTypeLabels[log.type] }}</n-tag>
                <div class="log-title">{{ log.remark || '' }}</div>
                <div class="log-time">{{ log.createTime }}</div>
              </div>
              <div class="log-right">
                <div class="log-amount" :class="{ positive: log.changeAmount > 0 }">
                  {{ log.changeAmount > 0 ? '+' : '' }}{{ (log.changeAmount ?? 0).toFixed(2) }}
                </div>
                <div class="log-after">余额 {{ (log.afterAmount ?? 0).toFixed(2) }}</div>
              </div>
            </div>
            <n-empty v-if="!balanceLoading && !balanceLogs.length" description="暂无余额记录" />
            <div class="load-more" v-if="balanceLoading">加载中...</div>
          </div>
        </n-tab-pane>

        <n-tab-pane name="recharge" tab="余额充值">
          <div class="recharge-section">
            <div class="amount-input">
              <span class="currency">¥</span>
              <input v-model="rechargeAmount" type="number" placeholder="请输入充值金额" />
            </div>
            <div class="presets">
              <div v-for="p in presetAmounts" :key="p" class="preset" :class="{ active: rechargeAmount === String(p) }" @click="rechargeAmount = String(p)">¥{{ p }}</div>
            </div>
            <div class="pay-channels">
              <label :class="{ active: payChannel === 'wxpay' }" @click="payChannel = 'wxpay'">微信支付</label>
              <label :class="{ active: payChannel === 'alipay' }" @click="payChannel = 'alipay'">支付宝</label>
            </div>
            <n-button type="error" block size="large" :loading="rechargeLoading" :disabled="!rechargeAmount" @click="handleRecharge">立即充值</n-button>
          </div>
        </n-tab-pane>
      </n-tabs>
    </div>
  </div>
</template>

<style scoped lang="scss">
.member-page { max-width: 1000px; margin: 0 auto; }

.level-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 32px; border-radius: 16px; color: #fff; margin-bottom: 24px;
}
.level-left { display: flex; align-items: center; gap: 20px; }
.level-badge { width: 64px; height: 64px; border-radius: 50%; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 700; }
.level-name { font-size: 24px; font-weight: 700; }
.level-desc { font-size: 14px; opacity: 0.85; margin-top: 4px; }
.level-right { width: 300px; }
.progress-track { height: 8px; background: rgba(255,255,255,0.3); border-radius: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: #fff; border-radius: 4px; transition: width 0.3s; }
.progress-labels { display: flex; justify-content: space-between; font-size: 12px; margin-top: 6px; opacity: 0.8; }

.content-row { display: flex; gap: 24px; margin-bottom: 24px; }
.left-col { flex: 1; }
.right-col { width: 320px; }

.data-cards { display: flex; gap: 16px; }
.data-card { flex: 1; background: #fff; border-radius: 12px; padding: 20px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.data-value { font-size: 28px; font-weight: 700; color: #333; }
.data-label { font-size: 14px; color: #999; margin-top: 4px; }

.benefits-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); height: 100%; }
.benefits-card h3 { font-size: 16px; margin: 0 0 16px; }
.benefit-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.benefit-row:last-child { border-bottom: none; }
.benefit-row.active { background: #FFF5F0; margin: 0 -20px; padding: 10px 20px; border-radius: 8px; }
.b-name { width: 80px; font-size: 14px; }
.b-discount { width: 60px; font-size: 14px; color: #FF5000; font-weight: 600; }
.b-threshold { flex: 1; font-size: 13px; color: #999; }

.tab-section { background: #fff; border-radius: 12px; padding: 20px 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }

.log-list { min-height: 300px; }
.log-item { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-bottom: 1px solid #f5f5f5; }
.log-left { flex: 1; }
.log-title { font-size: 15px; color: #333; margin-top: 4px; }
.log-time { font-size: 12px; color: #999; margin-top: 4px; }
.log-right { text-align: right; }
.log-amount { font-size: 18px; font-weight: 600; color: #FF5000; }
.log-amount.positive { color: #00B578; }
.log-after { font-size: 12px; color: #999; margin-top: 2px; }
.load-more { text-align: center; padding: 20px; color: #999; font-size: 14px; }

.recharge-section { max-width: 400px; margin: 20px auto; }
.amount-input { display: flex; align-items: center; border-bottom: 2px solid #f0f0f0; padding-bottom: 12px; margin-bottom: 16px; }
.currency { font-size: 28px; font-weight: 700; color: #333; margin-right: 8px; }
.amount-input input { flex: 1; font-size: 28px; font-weight: 700; border: none; outline: none; }
.presets { display: flex; gap: 12px; margin-bottom: 20px; }
.preset { flex: 1; text-align: center; padding: 10px; border-radius: 8px; background: #f5f5f5; font-size: 16px; font-weight: 600; cursor: pointer; }
.preset.active { background: #FFF0E8; color: #FF5000; border: 1px solid #FF5000; }
.pay-channels { display: flex; gap: 12px; margin-bottom: 20px; }
.pay-channels label { flex: 1; padding: 12px; border-radius: 8px; border: 1px solid #f0f0f0; text-align: center; cursor: pointer; }
.pay-channels label.active { border-color: #FF5000; background: #FFF8F5; color: #FF5000; }

@media (max-width: 768px) {
  .level-card { flex-direction: column; gap: 20px; padding: 24px 20px; }
  .level-right { width: 100%; }
  .content-row { flex-direction: column; }
  .right-col { width: 100%; }
  .data-cards { flex-wrap: wrap; }
  .data-card { min-width: calc(50% - 8px); }
  .benefit-row { flex-wrap: wrap; gap: 8px; }
}
</style>
