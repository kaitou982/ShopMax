<script setup lang="ts">
defineOptions({ name: 'HomePage' })
import { ref, onMounted } from 'vue'
import { productApi, couponApi, bannerApi, type ProductDetail, type Banner } from '@shop/shared'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const showAdmin = ref(false)

const sbh = uni.getSystemInfoSync().statusBarHeight || 0

const iconList = [
  { n: '秒杀', i: 'fire' },
  { n: '会员', i: 'vip' },
  { n: '百补', i: 'gift' },
  { n: '社区', i: 'chat' },
  { n: '直播', i: 'videocam' },
  { n: '领券', i: 'wallet' },
  { n: '新品', i: 'shop' },
  { n: '收藏', i: 'star' }
]
const banners = ref<Banner[]>([])
const products = ref<ProductDetail[]>([])
const loading = ref(true)
const refreshing = ref(false)
let page = 1
const hasMore = ref(true)

const fetchProducts = async (refresh = false) => {
  if (refresh) { page = 1; hasMore.value = true }
  if (!hasMore.value && !refresh) return
  loading.value = true
  try {
    const data = await productApi.getPage({ pageNum: page, pageSize: 10 })
    const res = data as any
    if (refresh) {
      products.value = res.records || []
    } else {
      products.value.push(...(res.records || []))
    }
    hasMore.value = res.current < res.pages
    page++
  } catch (e) {
    if (!refresh) await uni.showToast({title: '加载失败，请重试', icon: 'none'})
  } finally { loading.value = false; refreshing.value = false }
}

onMounted(() => {
  fetchProducts(true)
  bannerApi.getActive().then(data => { banners.value = data }).catch(() => {})
})
const onRefresh = () => { refreshing.value = true; fetchProducts(true) }
const onLoadMore = () => { if (!loading.value && hasMore.value) { fetchProducts() } }
const goProduct = (id: number) => uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
const goSearch = () => uni.navigateTo({ url: '/pages/search/index' })
const goCart = () => uni.switchTab({ url: '/pages/cart/index' })
const goPage = (url: string) => uni.navigateTo({ url })
const iconNav: Record<string, () => void> = {
  '秒杀': () => uni.navigateTo({ url:'/pages/seckill/index' }),
  '会员': () => uni.showToast({ title:'会员中心筹备中', icon:'none' }),
  '百补': () => uni.showToast({ title:'百亿补贴筹备中', icon:'none' }),
  '社区': () => uni.navigateTo({ url:'/pages/community/index' }),
  '直播': () => uni.navigateTo({ url:'/pages/live/index' }),
  '领券': () => uni.navigateTo({ url:'/pages/coupon/center' }),
  '新品': () => uni.navigateTo({ url:'/pages/search/index' }),
  '收藏': () => uni.navigateTo({ url:'/pages/user/favorites' }),
}
</script>
<template>
  <view class="hp">
    <view class="nav" :style="{ paddingTop: sbh + 'px' }">
      <view class="nav-inner">
        <text class="logo">ShopMax</text>
        <view class="search-box" @click="goSearch">
          <uni-icons type="search" size="16" color="#999" />
          <text class="search-text">搜索商品</text>
        </view>
        <view class="cart-icon" v-if="userStore.isAdmin||userStore.isStore" @click="showAdmin=true">
          <uni-icons type="gear" size="24" color="#fff" />
        </view>
        <view class="cart-icon" @click="goCart">
          <uni-icons type="cart" size="24" color="#fff" />
        </view>
      </view>
    </view>
    <scroll-view scroll-y class="main" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh" @scrolltolower="onLoadMore">
      <swiper class="banner" circular autoplay :interval="3000" v-if="banners.length">
        <swiper-item v-for="b in banners" :key="b.id" @click="b.linkUrl && goPage(b.linkUrl)">
          <image :src="b.imageUrl" mode="aspectFill" class="b-img" />
        </swiper-item>
      </swiper>
      <view class="icons">
        <view class="ic" v-for="item in iconList" :key="item.n" @click="iconNav[item.n]?.()">
          <view class="ic-circle">
            <uni-icons :type="item.i" size="24" color="#FF5000" />
          </view>
          <text class="ic-label">{{ item.n }}</text>
        </view>
      </view>
      <view class="skel-grid" v-if="loading && !products.length">
        <view class="skel-card" v-for="i in 4" :key="i"><view class="skel-img" /><view class="skel-body"><view class="skel-line" /><view class="skel-line short" /></view></view>
      </view>
      <view class="section"><text class="s-title">为你推荐</text></view>
      <view class="grid" v-if="products.length">
        <view class="card" v-for="p in products" :key="p.id" @click="goProduct(p.id)">
          <image :src="p.mainImage || '/api/v1/files/default/product'" mode="aspectFill" class="card-img" lazy-load />
          <view class="card-body"><text class="card-name">{{ p.name }}</text>
            <view class="card-foot"><text class="card-price">¥{{ p.salePrice }}</text><text class="card-sales">已售{{ (p.sales||0)>10000?((p.sales/1e4).toFixed(1)+'万'):p.sales }}</text></view>
          </view>
        </view>
      </view>
      <view class="more" v-if="loading && products.length"><text>加载中...</text></view>
      <view class="more" v-else-if="!hasMore && products.length"><text>— 没有更多了 —</text></view>
    </scroll-view>

    <!-- Admin popup -->
    <view class="admin-ol" v-if="showAdmin" @click="showAdmin=false"/>
    <view class="admin-pop" v-if="showAdmin">
      <view class="admin-hd">
        <image :src="userStore.userInfo?.avatar||'/api/v1/files/default/avatar'" class="admin-av"/>
        <view><text class="admin-nm">{{ userStore.userName }}</text><text class="admin-rl">{{ userStore.isAdmin?'管理员':'店家' }}</text></view>
        <view class="admin-close" @click="showAdmin=false">
          <uni-icons type="close" size="20" color="#ccc" />
        </view>
      </view>
      <view class="admin-menu">
        <view class="admin-mi" v-if="userStore.isAdmin" @click="showAdmin=false;goPage('/pages/user/index')">
          <view class="admin-mi-left">
            <uni-icons type="person" size="20" color="#666" />
            <text>用户管理</text>
          </view>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
        <view class="admin-mi" @click="showAdmin=false;goPage('/pages/order/list')">
          <view class="admin-mi-left">
            <uni-icons type="list" size="20" color="#666" />
            <text>订单管理</text>
          </view>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
        <view class="admin-mi" v-if="userStore.isAdmin" @click="showAdmin=false;goPage('/pages/user/store-apply')">
          <view class="admin-mi-left">
            <uni-icons type="shop" size="20" color="#666" />
            <text>入驻审核</text>
          </view>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
        <view class="admin-mi" @click="showAdmin=false;goPage('/pages/user/profile')">
          <view class="admin-mi-left">
            <uni-icons type="compose" size="20" color="#666" />
            <text>个人资料</text>
          </view>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
        <view class="admin-mi" @click="showAdmin=false;goPage('/pages/user/setting')">
          <view class="admin-mi-left">
            <uni-icons type="gear" size="20" color="#666" />
            <text>设置</text>
          </view>
          <uni-icons type="right" size="16" color="#ccc" />
        </view>
      </view>
    </view>
  </view>
</template>
<style scoped lang="scss">
.hp { height: 100vh; display: flex; flex-direction: column; background: #f5f5f5; }
.nav { background: linear-gradient(90deg,#FF5000,#FF9000); }
.nav-inner { display: flex; align-items: center; padding: 10rpx 20rpx; gap: 16rpx; height: 80rpx; }
.logo { font-size: 36rpx; font-weight: 800; color: #fff; }
.search-box { flex: 1; height: 60rpx; background: rgba(255,255,255,.9); border-radius: 30rpx; display: flex; align-items: center; padding: 0 20rpx; gap: 8rpx; }
.search-text { font-size: 24rpx; color: #999; }
.cart-icon { display: flex; align-items: center; justify-content: center; }
.main { flex: 1; }
.banner { height: 300rpx; } .b-img { width: 100%; height: 100%; }
.icons { display: flex; flex-wrap: wrap; padding: 20rpx 16rpx; background: #fff; margin-bottom: 12rpx; }
.ic { width: 25%; display: flex; flex-direction: column; align-items: center; padding: 12rpx 0; }
.ic-circle { width: 80rpx; height: 80rpx; border-radius: 50%; background: #FFF3EC; display: flex; align-items: center; justify-content: center; font-size: 36rpx; }
.ic-label { font-size: 22rpx; color: #666; margin-top: 8rpx; }
.section { padding: 16rpx 20rpx; } .s-title { font-size: 32rpx; font-weight: 700; }
.grid { display: flex; flex-wrap: wrap; gap: 10rpx; padding: 0 16rpx 20rpx; }
.card { width: calc(50% - 5rpx); background: #fff; border-radius: 16rpx; overflow: hidden; }
.card-img { width: 100%; height: 340rpx; background: #f0f0f0; }
.card-body { padding: 14rpx; }
.card-name { font-size: 26rpx; color: #1C1C1E; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
.card-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 8rpx; }
.card-price { font-size: 32rpx; font-weight: 700; color: #FF5000; }
.card-sales { font-size: 20rpx; color: #999; }
.more { text-align: center; padding: 30rpx; font-size: 24rpx; color: #999; }
.skel-grid { display: flex; flex-wrap: wrap; gap: 10rpx; padding: 0 16rpx; }
.skel-card { width: calc(50% - 5rpx); background: #fff; border-radius: 16rpx; overflow: hidden; }
.skel-img { width: 100%; height: 340rpx; background: linear-gradient(90deg,#f0f0f0 25%,#e8e8e8 50%,#f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.skel-body { padding: 14rpx; }
.skel-line { height: 24rpx; background: #f0f0f0; border-radius: 4rpx; margin-bottom: 10rpx; &.short { width: 40%; } }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.admin-ol{position:fixed;inset:0;background:rgba(0,0,0,.4);z-index:500}
.admin-pop{position:fixed;bottom:0;left:0;right:0;background:#fff;border-radius:24rpx 24rpx 0 0;padding:28rpx;z-index:501}
.admin-hd{display:flex;align-items:center;gap:16rpx;margin-bottom:24rpx}.admin-av{width:64rpx;height:64rpx;border-radius:50%;background:#eee}.admin-nm{font-size:30rpx;font-weight:600;display:block}.admin-rl{font-size:22rpx;color:#999}.admin-close{font-size:36rpx;color:#ccc;margin-left:auto}
.admin-menu{border-top:1rpx solid #f0f0f0}
.admin-mi{display:flex;justify-content:space-between;align-items:center;padding:24rpx 0;font-size:28rpx;border-bottom:1rpx solid #f5f5f5}
.admin-mi-left{display:flex;align-items:center;gap:16rpx}
</style>
