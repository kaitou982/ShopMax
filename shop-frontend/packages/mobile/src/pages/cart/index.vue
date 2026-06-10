<script setup lang="ts">defineOptions({ name: 'CartPage' })
import { useCartStore } from '@/stores'
const cs = useCartStore()
const goCheckout = () => uni.navigateTo({ url: '/pages/order/confirm' })
const goProduct = (id: number) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
</script>
<template>
  <view class="cp">
    <view v-if="!cs.cartList.length" class="empty">🛒<text>购物车空空如也</text></view>
    <view v-else class="list">
      <view class="item" v-for="i in cs.cartList" :key="i.id">
        <text class="cb" :class="{on:i.selected}" @click="cs.toggleSelected(i.id)">{{i.selected?'☑':'☐'}}</text>
        <image :src="i.image||'/api/v1/files/default/product'" mode="aspectFill" class="img" @click="goProduct(i.productId)"/>
        <view class="info" @click="goProduct(i.productId)"><text class="name">{{i.name}}</text><text class="price">¥{{i.price}}</text></view>
        <view class="qty"><text @click="cs.updateQuantity(i.id,i.quantity-1)">−</text><text>{{i.quantity}}</text><text @click="cs.updateQuantity(i.id,i.quantity+1)">+</text></view>
        <text class="del" @click="cs.removeFromCart(i.id)">✕</text>
      </view>
      <view class="bar" style="margin-bottom: 48px"><text class="total">合计: ¥{{cs.totalPrice.toFixed(2)}}</text><button class="btn" @click="goCheckout">去结算({{cs.selectedCount}})</button></view>
    </view>
  </view>
</template>
<style scoped lang="scss">
.cp{min-height:100vh;background:#f5f5f5;padding-bottom:120rpx}
.empty{text-align:center;padding:200rpx 0;font-size:60rpx;color:#ccc;text{display:block;font-size:28rpx;margin-top:20rpx}}
.list{padding:12rpx 0}
.item{display:flex;align-items:center;padding:20rpx;background:#fff;margin-bottom:2rpx;gap:12rpx}
.cb{font-size:40rpx;color:#ccc;flex-shrink:0;&.on{color:#FF5000}}
.img{width:100rpx;height:100rpx;border-radius:8rpx;background:#f0f0f0;flex-shrink:0}
.info{flex:1;min-width:0}.name{font-size:26rpx;color:#333;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;display:block}.price{font-size:28rpx;font-weight:700;color:#FF5000;margin-top:8rpx;display:block}
.qty{display:flex;align-items:center;gap:0;border:1rpx solid #E5E5EA;border-radius:8rpx;overflow:hidden;text{width:48rpx;height:48rpx;display:flex;align-items:center;justify-content:center;font-size:28rpx;background:#f5f5f5;&:nth-child(2){background:#fff;width:56rpx;font-weight:600}}}
.del{font-size:32rpx;color:#ccc;padding:8rpx}
.bar{position:fixed;bottom:0;left:0;right:0;height:100rpx;background:#fff;display:flex;align-items:center;justify-content:space-between;padding:0 24rpx;box-shadow:0 -2rpx 10rpx rgba(0,0,0,.05);.total{font-size:30rpx;font-weight:700}.btn{background:linear-gradient(90deg,#FF5000,#FF9000);color:#fff;border:none;border-radius:40rpx;padding:12rpx 32rpx;font-size:26rpx}}
</style>
