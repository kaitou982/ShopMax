<script setup lang="ts">
defineOptions({ name: 'RechargePage' })
import { ref } from 'vue'
import { walletApi } from '@shop/shared'

const amount = ref('')
const payChannel = ref<'alipay' | 'wxpay'>('wxpay')
const loading = ref(false)
const presetAmounts = [50, 100, 200, 500]

const selectPreset = (val: number) => { amount.value = String(val) }

const handleRecharge = async () => {
  const val = parseFloat(amount.value)
  if (!val || val < 0.01) {
    uni.showToast({ title: '请输入正确金额', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await walletApi.recharge({ amount: val, payChannel: payChannel.value })
    uni.showToast({ title: '充值成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch { /* handled by interceptor */ } finally { loading.value = false }
}
</script>

<template>
  <view class="page">
    <view class="section">
      <text class="section-title">充值金额</text>
      <view class="amount-input">
        <text class="currency">¥</text>
        <input v-model="amount" type="digit" placeholder="请输入充值金额" class="input" />
      </view>
      <view class="presets">
        <view
          v-for="p in presetAmounts"
          :key="p"
          class="preset"
          :class="{ active: amount === String(p) }"
          @click="selectPreset(p)"
        >¥{{ p }}</view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">支付方式</text>
      <view class="pay-options">
        <view class="pay-option" :class="{ active: payChannel === 'wxpay' }" @click="payChannel = 'wxpay'">
          <uni-icons type="weixin" size="24" color="#07C160" />
          <text>微信支付</text>
          <uni-icons v-if="payChannel === 'wxpay'" type="checkmarkempty" size="18" color="#FF5000" />
        </view>
        <view class="pay-option" :class="{ active: payChannel === 'alipay' }" @click="payChannel = 'alipay'">
          <uni-icons type="wallet" size="24" color="#1677FF" />
          <text>支付宝</text>
          <uni-icons v-if="payChannel === 'alipay'" type="checkmarkempty" size="18" color="#FF5000" />
        </view>
      </view>
    </view>

    <button class="submit-btn" :loading="loading" :disabled="!amount" @click="handleRecharge">立即充值</button>
  </view>
</template>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding: 16px; }

.section { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 16px; }
.section-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 16px; display: block; }

.amount-input { display: flex; align-items: center; border-bottom: 2px solid #f0f0f0; padding-bottom: 12px; }
.currency { font-size: 28px; font-weight: 700; color: #333; margin-right: 8px; }
.input { flex: 1; font-size: 28px; font-weight: 700; color: #333; }

.presets { display: flex; gap: 12px; margin-top: 16px; }
.preset { flex: 1; text-align: center; padding: 10px; border-radius: 8px; background: #f5f5f5; font-size: 16px; font-weight: 600; color: #333; }
.preset.active { background: #FFF0E8; color: #FF5000; border: 1px solid #FF5000; }

.pay-options { display: flex; flex-direction: column; gap: 12px; }
.pay-option { display: flex; align-items: center; gap: 12px; padding: 14px; border-radius: 8px; border: 1px solid #f0f0f0; }
.pay-option.active { border-color: #FF5000; background: #FFF8F5; }
.pay-option text { flex: 1; font-size: 15px; color: #333; }

.submit-btn { margin-top: 24px; height: 48px; background: linear-gradient(90deg, #FF5000, #FF8C3A); color: #fff; font-size: 16px; font-weight: 600; border-radius: 24px; border: none; }
.submit-btn[disabled] { opacity: 0.5; }
</style>
