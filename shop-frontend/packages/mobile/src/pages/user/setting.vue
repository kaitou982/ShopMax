<script setup lang="ts">
defineOptions({ name: 'SettingPage' })
import { useUserStore } from '@/stores'
const userStore = useUserStore()
const logout = () => { userStore.logout(); uni.switchTab({ url: '/pages/index/index' }) }
const goPassword = () => uni.navigateTo({ url: '/pages/user/password' })
const showComing = () => uni.showToast({ title: '即将开放，敬请期待', icon: 'none' })
</script>
<template>
  <view class="sp">
    <view class="menu">
      <view class="mi" @click="goPassword">
        <text>修改密码</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <view class="mi" @click="showComing">
        <text>账号安全</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <view class="mi" @click="showComing">
        <text>关于我们</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
    </view>
    <view class="menu" style="margin-top:16rpx"><view class="mi" @click="logout"><text style="color:#FF3B3B">退出登录</text></view></view>
  </view>
</template>
<style scoped lang="scss">
.sp { padding:20rpx;min-height:100vh;background:#f5f5f5 }
.menu { background:#fff;border-radius:12rpx;overflow:hidden }
.mi { display:flex;justify-content:space-between;padding:28rpx 24rpx;border-bottom:1rpx solid #f5f5f5;font-size:28rpx }
.ar { color:#ccc;font-size:32rpx }
</style>
