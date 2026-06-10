<script setup lang="ts">defineOptions({ name: 'UserPage' })
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { couponApi } from '@shop/shared'
import { useUserStore } from '@/stores'
const us = useUserStore()
const showAdmin = ref(false)
const couponCount = ref(0)
onShow(() => { if(us.token) { us.fetchUserInfo(); couponApi.getMyCoupons().then(c => couponCount.value = c?.total||0).catch(()=>{}) } })
const goPage = (path: string) => { if(path) uni.navigateTo({url:path}); else uni.showToast({title:'功能开发中',icon:'none'}) }
const goLogin = () => uni.navigateTo({ url: '/pages/login/index' })
const doLogout = () => { us.logout(); uni.switchTab({ url: '/pages/index/index' }) }
const menu = [{n:'我的订单',p:'/pages/order/list'},{n:'领券中心',p:'/pages/coupon/center'},{n:'我的优惠券',p:'/pages/coupon/my'},{n:'我的收藏',p:'/pages/user/favorites'},{n:'收货地址',p:'/pages/user/address'},{n:'在线客服',p:'/pages/customer-service/chat'},{n:'编辑资料',p:'/pages/user/profile'},{n:'设置',p:'/pages/user/setting'},{n:'店铺入驻',p:'/pages/user/store-apply'}]
</script>
<template>
  <view class="up">
    <view class="hd" v-if="us.isLoggedIn"><image :src="us.userInfo?.avatar||'/api/v1/files/default/avatar'" class="av"/><view class="ifo"><text class="nm">{{us.userName}}</text><text class="rl">{{us.isStore?'店家':'用户'}}</text></view><text v-if="us.isAdmin||us.isStore" class="hd-gear" @click="showAdmin=true">⚙️</text></view>
    <view class="hd" v-else @click="goLogin"><view class="av"><text>👤</text></view><view class="ifo"><text class="nm">点击登录</text></view></view>
    <view class="ast">
      <view class="ai"><text class="avv">{{ (us.userInfo?.balance||0).toFixed(2) }}</text><text>余额</text></view>
      <view class="ai"><text class="avv">{{ us.userInfo?.integral||0 }}</text><text>积分</text></view>
      <view class="ai"><text class="avv">{{ couponCount }}</text><text>优惠券</text></view>
      <view class="ai"><text class="avv">{{ us.userInfo?.growthValue||0 }}</text><text>成长值</text></view>
    </view>
    <view class="mu"><view class="mi" v-for="m in menu" :key="m.n" @click="goPage(m.p)"><text>{{m.n}}</text><text class="ar">›</text></view></view>
    <view class="mu" style="margin-top:16rpx"><view class="mi" @click="doLogout"><text style="color:#FF3B3B">退出登录</text></view></view>

    <view class="admin-ol" v-if="showAdmin" @click="showAdmin=false"/>
    <view class="admin-pop" v-if="showAdmin">
      <view class="admin-hd"><image :src="us.userInfo?.avatar||'/api/v1/files/default/avatar'" class="admin-av"/><view><text class="admin-nm">{{us.userName}}</text><text class="admin-rl">{{us.isAdmin?'管理员':'店家'}}</text></view><text class="admin-close" @click="showAdmin=false">✕</text></view>
      <view class="admin-menu">
        <view class="admin-mi" v-if="us.isAdmin" @click="showAdmin=false"><text>👥 用户管理</text><text class="ar">›</text></view>
        <view class="admin-mi" @click="showAdmin=false;goPage('/pages/order/list')"><text>📋 订单管理</text><text class="ar">›</text></view>
        <view class="admin-mi" v-if="us.isAdmin" @click="showAdmin=false;goPage('/pages/user/store-apply')"><text>🏪 入驻审核</text><text class="ar">›</text></view>
        <view class="admin-mi" @click="showAdmin=false;goPage('/pages/user/profile')"><text>✏️ 个人资料</text><text class="ar">›</text></view>
        <view class="admin-mi" @click="showAdmin=false;goPage('/pages/user/setting')"><text>⚙️ 设置</text><text class="ar">›</text></view>
      </view>
    </view>
  </view>
</template>
<style scoped lang="scss">
.up{min-height:100vh;background:#f5f5f5}
.hd{display:flex;align-items:center;gap:20rpx;padding:60rpx 30rpx 40rpx;background:linear-gradient(135deg,#FF5000,#FF9000)}.av{width:100rpx;height:100rpx;border-radius:50%;background:rgba(255,255,255,.3);display:flex;align-items:center;justify-content:center;font-size:48rpx}.nm{font-size:36rpx;font-weight:700;color:#fff;display:block}.rl{font-size:24rpx;color:rgba(255,255,255,.8)}
.ast{display:flex;background:#fff;padding:20rpx 0;margin-bottom:16rpx}.ai{flex:1;text-align:center;font-size:22rpx;color:#999}.avv{display:block;font-size:32rpx;font-weight:700;color:#333}
.mu{background:#fff}.mi{display:flex;justify-content:space-between;padding:28rpx 30rpx;border-bottom:1rpx solid #f5f5f5;font-size:28rpx}.ar{color:#ccc;font-size:32rpx}
.hd-gear{font-size:36rpx;padding:8rpx;flex-shrink:0}
.admin-ol{position:fixed;inset:0;background:rgba(0,0,0,.4);z-index:500}
.admin-pop{position:fixed;bottom:0;left:0;right:0;background:#fff;border-radius:24rpx 24rpx 0 0;padding:28rpx;z-index:501}
.admin-hd{display:flex;align-items:center;gap:16rpx;margin-bottom:24rpx}.admin-av{width:64rpx;height:64rpx;border-radius:50%;background:#eee}.admin-nm{font-size:30rpx;font-weight:600;display:block}.admin-rl{font-size:22rpx;color:#999}.admin-close{font-size:36rpx;color:#ccc;margin-left:auto}
.admin-menu{border-top:1rpx solid #f0f0f0}.admin-mi{display:flex;justify-content:space-between;padding:24rpx 0;font-size:28rpx;border-bottom:1rpx solid #f5f5f5}
</style>
