<script setup lang="ts">
defineOptions({ name: 'MemberCenterPage' })
import { ref, onMounted, computed } from 'vue'
import { walletApi, type MemberInfo } from '@shop/shared'

const sbh = uni.getSystemInfoSync().statusBarHeight || 0
const memberInfo = ref<MemberInfo | null>(null)
const loading = ref(true)

const levelColors: Record<number, string> = {
  1: 'linear-gradient(135deg, #909399, #b0b3b8)',
  2: 'linear-gradient(135deg, #409EFF, #66b1ff)',
  3: 'linear-gradient(135deg, #E6A23C, #f0c78a)',
  4: 'linear-gradient(135deg, #9B59B6, #c39bd3)'
}

const levelIcons: Record<number, string> = { 1: 'medal', 2: 'medal', 3: 'medal', 4: 'medal' }

const progressPercent = computed(() => {
  if (!memberInfo.value) return 0
  const { growthValue, nextLevelGrowth, memberLevel } = memberInfo.value
  if (memberLevel >= 4) return 100
  const prevThreshold = memberInfo.value.levelBenefits[memberLevel - 1]?.threshold || 0
  const range = nextLevelGrowth - prevThreshold
  return range > 0 ? Math.min(100, ((growthValue - prevThreshold) / range) * 100) : 0
})

const fetchMemberInfo = async () => {
  try {
    memberInfo.value = await walletApi.getMemberInfo()
  } catch { /* ignore */ } finally { loading.value = false }
}

const goBack = () => uni.navigateBack()
const goIntegralLog = () => uni.navigateTo({ url: '/pages/member/integral-log' })
const goBalanceLog = () => uni.navigateTo({ url: '/pages/member/balance-log' })
const goRecharge = () => uni.navigateTo({ url: '/pages/member/recharge' })
const goCouponCenter = () => uni.navigateTo({ url: '/pages/coupon/center' })

onMounted(() => fetchMemberInfo())
</script>

<template>
  <view class="mc">
    <!-- 导航栏 -->
    <view class="nav" :style="{ paddingTop: sbh + 'px' }">
      <view class="nav-inner">
        <view class="back" @click="goBack"><uni-icons type="back" size="24" color="#fff" /></view>
        <text class="title">会员中心</text>
        <view class="placeholder" />
      </view>
    </view>

    <scroll-view scroll-y class="main" :style="{ paddingTop: (sbh + 50) + 'px' }">
      <!-- 会员等级卡片 -->
      <view class="level-card" :style="{ background: levelColors[memberInfo?.memberLevel || 1] }">
        <view class="level-top">
          <view class="level-badge">
            <uni-icons :type="levelIcons[memberInfo?.memberLevel || 1]" size="36" color="#fff" />
          </view>
          <view class="level-info">
            <text class="level-name">{{ memberInfo?.memberLevelName || '普通会员' }}</text>
            <text class="level-desc" v-if="memberInfo && memberInfo.memberLevel < 4">
              再获 {{ memberInfo.nextLevelGrowth - memberInfo.growthValue }} 成长值升级为{{ memberInfo.nextLevelName }}
            </text>
            <text class="level-desc" v-else>已达最高等级</text>
          </view>
        </view>
        <view class="progress-bar">
          <view class="progress-track">
            <view class="progress-fill" :style="{ width: progressPercent + '%' }" />
          </view>
          <view class="progress-labels">
            <text>{{ memberInfo?.growthValue || 0 }}</text>
            <text>{{ memberInfo?.memberLevel >= 4 ? 'MAX' : memberInfo?.nextLevelGrowth || 0 }}</text>
          </view>
        </view>
      </view>

      <!-- 数据卡片行 -->
      <view class="data-cards">
        <view class="data-card" @click="goIntegralLog">
          <text class="data-value">{{ memberInfo?.integral || 0 }}</text>
          <text class="data-label">积分</text>
          <text class="data-link">查看明细 ></text>
        </view>
        <view class="data-card" @click="goBalanceLog">
          <text class="data-value">{{ memberInfo?.balance?.toFixed(2) || '0.00' }}</text>
          <text class="data-label">余额(元)</text>
          <view class="data-actions">
            <text class="data-link" @click.stop="goRecharge">充值</text>
          </view>
        </view>
        <view class="data-card">
          <text class="data-value">{{ memberInfo?.growthValue || 0 }}</text>
          <text class="data-label">成长值</text>
        </view>
      </view>

      <!-- 等级权益对照表 -->
      <view class="section-card">
        <text class="section-title">等级权益</text>
        <view class="benefit-list">
          <view
            v-for="b in memberInfo?.levelBenefits"
            :key="b.level"
            class="benefit-item"
            :class="{ active: b.level === memberInfo?.memberLevel }"
          >
            <view class="benefit-level">
              <uni-icons type="medal" size="20" :color="b.level === memberInfo?.memberLevel ? '#FF5000' : '#999'" />
              <text class="benefit-name">{{ b.name }}</text>
              <text class="benefit-current" v-if="b.level === memberInfo?.memberLevel">当前</text>
            </view>
            <text class="benefit-discount">{{ b.discount }}</text>
            <text class="benefit-threshold">{{ b.threshold }}成长值</text>
          </view>
        </view>
      </view>

      <!-- 功能入口 -->
      <view class="section-card">
        <view class="menu-item" @click="goIntegralLog">
          <uni-icons type="list" size="20" color="#666" />
          <text class="menu-text">积分明细</text>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
        <view class="menu-item" @click="goBalanceLog">
          <uni-icons type="wallet" size="20" color="#666" />
          <text class="menu-text">余额明细</text>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
        <view class="menu-item" @click="goRecharge">
          <uni-icons type="plus" size="20" color="#666" />
          <text class="menu-text">余额充值</text>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
        <view class="menu-item" @click="goCouponCenter">
          <uni-icons type="gift" size="20" color="#666" />
          <text class="menu-text">积分兑换优惠券</text>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.mc { min-height: 100vh; background: #f5f5f5; }

.nav { position: fixed; top: 0; left: 0; right: 0; z-index: 100; background: transparent; }
.nav-inner { height: 50px; display: flex; align-items: center; padding: 0 16px; }
.back { width: 40px; }
.title { flex: 1; text-align: center; font-size: 17px; font-weight: 600; color: #fff; }
.placeholder { width: 40px; }

/* 等级卡片 */
.level-card { margin: 16px; padding: 24px; border-radius: 16px; color: #fff; }
.level-top { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.level-badge { width: 56px; height: 56px; border-radius: 50%; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; }
.level-info { flex: 1; }
.level-name { font-size: 22px; font-weight: 700; display: block; }
.level-desc { font-size: 13px; opacity: 0.85; margin-top: 4px; }

.progress-track { height: 6px; background: rgba(255,255,255,0.3); border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: #fff; border-radius: 3px; transition: width 0.3s; }
.progress-labels { display: flex; justify-content: space-between; font-size: 12px; margin-top: 6px; opacity: 0.8; }

/* 数据卡片 */
.data-cards { display: flex; gap: 10px; margin: 0 16px 16px; }
.data-card { flex: 1; background: #fff; border-radius: 12px; padding: 16px 12px; text-align: center; }
.data-value { font-size: 22px; font-weight: 700; color: #333; display: block; }
.data-label { font-size: 12px; color: #999; margin-top: 4px; display: block; }
.data-link { font-size: 12px; color: #FF5000; margin-top: 6px; display: block; }
.data-actions { margin-top: 6px; }

/* 等级权益 */
.section-card { background: #fff; margin: 0 16px 16px; border-radius: 12px; padding: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: #333; margin-bottom: 12px; display: block; }

.benefit-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.benefit-item:last-child { border-bottom: none; }
.benefit-item.active { background: #FFF5F0; margin: 0 -16px; padding: 12px 16px; border-radius: 8px; }
.benefit-level { display: flex; align-items: center; gap: 8px; }
.benefit-name { font-size: 14px; color: #333; }
.benefit-current { font-size: 11px; color: #FF5000; background: #FFF0E8; padding: 2px 8px; border-radius: 10px; }
.benefit-discount { font-size: 14px; color: #FF5000; font-weight: 600; }
.benefit-threshold { font-size: 12px; color: #999; width: 80px; text-align: right; }

/* 功能菜单 */
.menu-item { display: flex; align-items: center; gap: 12px; padding: 14px 0; border-bottom: 1px solid #f5f5f5; }
.menu-item:last-child { border-bottom: none; }
.menu-text { flex: 1; font-size: 15px; color: #333; }
</style>
