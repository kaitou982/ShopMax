<script setup lang="ts">defineOptions({ name: 'ProductDetail' })
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { productApi, reviewApi, type ProductDetail, type ReviewStats, type ProductReview } from '@shop/shared'
import { useCartStore } from '@/stores'
const cs = useCartStore()
const product = ref<ProductDetail|null>(null); const loading = ref(true); const showSku = ref(false); const qty = ref(1); const action = ref<'cart'|'buy'>('cart')
const reviewStats = ref<ReviewStats|null>(null)
const latestReviews = ref<ProductReview[]>([])
const images = computed(() => { if(!product.value) return []; const s=product.value.subImages?product.value.subImages.split(',').filter(Boolean):[]; return [product.value.mainImage,...s] })
onLoad((opts?:any) => {
  const id = Number(opts?.id)
  productApi.getDetail(id).then(p=>{product.value=p}).catch(()=>{}).finally(()=>loading.value=false)
  reviewApi.getReviewStats(id).then(s=>{reviewStats.value=s}).catch(()=>{})
  reviewApi.getProductReviews(id,1,3).then(r=>{latestReviews.value=r?.records||[]}).catch(()=>{})
})
const openSku = (a:'cart'|'buy') => { action.value=a; showSku.value=true; qty.value=1 }
const confirmSku = () => { if(!product.value) return; showSku.value=false
  cs.addToCart({productId:product.value.id,name:product.value.name,image:product.value.mainImage,price:product.value.salePrice,quantity:qty.value})
  if(action.value==='cart') uni.showToast({title:'已加入购物车',icon:'success'})
  else uni.switchTab({url:'/pages/cart/index'}) }
const goReviews = () => { if(product.value) uni.navigateTo({url:'/pages/product/reviews?id='+product.value.id}) }
const goCs = () => uni.navigateTo({url:'/pages/customer-service/chat'})
const renderStars = (n:number) => n
</script>
<template>
  <view class="pd"><view v-if="loading" class="st">加载中...</view>
  <template v-else-if="product">
    <swiper class="banner" circular><swiper-item v-for="(img,i) in images" :key="i"><image :src="img" mode="aspectFill"/></swiper-item></swiper>
    <view class="info"><view class="price-row"><text class="price">¥{{product.salePrice}}</text><text class="orig" v-if="product.originalPrice">¥{{product.originalPrice}}</text></view><text class="name">{{product.name}}</text><text class="meta">已售{{product.sales||0}} · 库存{{product.stock||0}}件</text></view>
    <!-- 评价摘要 -->
    <view class="review-summary" v-if="reviewStats" @click="goReviews">
      <view class="rs-header">
        <text class="rs-title">商品评价</text>
        <text class="rs-count">{{ reviewStats.totalCount }}条评价</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <view class="rs-rate">
        <view class="rs-stars">
          <uni-icons v-for="i in 5" :key="i" :type="i <= Math.round(reviewStats.avgRating) ? 'star-filled' : 'star'" size="16" :color="i <= Math.round(reviewStats.avgRating) ? '#FF9500' : '#ddd'" />
        </view>
        <text class="rs-avg">{{ reviewStats.avgRating?.toFixed(1) }}</text>
        <text class="rs-good">好评率 {{ reviewStats.goodRate?.toFixed(0)||0 }}%</text>
      </view>
      <view class="rs-preview" v-for="r in latestReviews" :key="r.id">
        <view class="rs-user">
          <text class="rs-name">{{ r.isAnonymous?'匿名':(r.userNickname||'用户') }}</text>
          <view class="rs-ustars">
            <uni-icons v-for="i in 5" :key="i" :type="i <= r.rating ? 'star-filled' : 'star'" size="14" :color="i <= r.rating ? '#FF9500' : '#ddd'" />
          </view>
        </view>
        <text class="rs-content" v-if="r.content">{{ r.content }}</text>
      </view>
    </view>
    <view class="desc-section"><text class="ds-title">商品详情</text><view class="ds-body" v-html="product.detail||product.description||'暂无详情'"/></view>
    <view class="bar"><button class="bc" @click="goCs">客服</button><button class="bc" @click="openSku('cart')">加入购物车</button><button class="bb" @click="openSku('buy')">立即购买</button></view>
  </template></view>
  <view class="sku-ol" v-if="showSku" @click="showSku=false"/><view class="sku" v-if="showSku">
    <view class="sku-hd"><image :src="product?.mainImage" class="sku-img"/><view><text class="sku-p">¥{{product?.salePrice}}</text><text class="sku-s">库存{{product?.stock}}件</text></view></view>
    <view class="sku-row"><text>数量</text><view class="sku-qty"><text @click="qty>1&&qty--">−</text><text>{{qty}}</text><text @click="qty<(product?.stock||1)&&qty++">+</text></view></view>
    <button class="sku-btn" @click="confirmSku">{{action==='cart'?'加入购物车':'立即购买'}}</button>
  </view>
</template>
<style scoped lang="scss">
.pd{min-height:100vh;background:#f5f5f5;padding-bottom:120rpx}.st{text-align:center;padding:200rpx 0;color:#999}
.banner{height:750rpx;image{width:100%;height:100%}}
.info{background:#fff;padding:28rpx;margin-bottom:16rpx}.price{font-size:48rpx;font-weight:700;color:#FF5000}.orig{font-size:26rpx;color:#999;text-decoration:line-through;margin-left:12rpx}.name{font-size:32rpx;color:#333;display:block;margin-top:12rpx}.meta{font-size:24rpx;color:#999;display:block;margin-top:8rpx}
.desc-section{background:#fff;padding:24rpx}.ds-title{font-size:30rpx;font-weight:700;margin-bottom:16rpx;display:block}.ds-body{font-size:28rpx;line-height:1.8;color:#666}
.review-summary{background:#fff;padding:24rpx;margin-bottom:16rpx}
.rs-header{display:flex;align-items:center;margin-bottom:12rpx}.rs-title{font-size:30rpx;font-weight:700;flex:1}.rs-count{font-size:24rpx;color:#999}.rs-arrow{font-size:32rpx;color:#ccc;margin-left:8rpx}
.rs-rate{display:flex;align-items:center;gap:12rpx;margin-bottom:16rpx}
.rs-stars{display:flex;align-items:center;gap:4rpx}
.rs-avg{font-size:28rpx;font-weight:700;color:#FF5000}.rs-good{font-size:22rpx;color:#999}
.rs-preview{border-top:1rpx solid #f5f5f5;padding-top:12rpx;margin-bottom:8rpx}.rs-user{display:flex;align-items:center;gap:12rpx;margin-bottom:6rpx}.rs-name{font-size:24rpx;color:#666}.rs-ustars{font-size:20rpx;color:#FF9500}.rs-content{font-size:26rpx;color:#333;line-height:1.5;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
.bar{position:fixed;bottom:0;left:0;right:0;display:flex;padding:12rpx 20rpx;background:#fff;gap:12rpx;box-shadow:0 -2rpx 10rpx rgba(0,0,0,.05)}.bc,.bb{flex:1;height:76rpx;border-radius:38rpx;font-size:28rpx;font-weight:600}.bc{background:#FFF3EC;color:#FF5000;border:1px solid #FF5000}.bb{background:linear-gradient(90deg,#FF5000,#FF9000);color:#fff;border:none}
.sku-ol{position:fixed;inset:0;background:rgba(0,0,0,.4);z-index:300}.sku{position:fixed;bottom:0;left:0;right:0;background:#fff;border-radius:24rpx 24rpx 0 0;padding:28rpx;z-index:301}.sku-hd{display:flex;gap:16rpx;margin-bottom:28rpx}.sku-img{width:100rpx;height:100rpx;border-radius:8rpx}.sku-p{font-size:36rpx;font-weight:700;color:#FF5000;display:block}.sku-s{font-size:22rpx;color:#999}.sku-row{display:flex;justify-content:space-between;align-items:center;margin-bottom:28rpx;font-size:28rpx}.sku-qty{display:flex;border:1rpx solid #E5E5EA;border-radius:8rpx;overflow:hidden;text{width:56rpx;height:56rpx;display:flex;align-items:center;justify-content:center;background:#f5f5f5;font-size:32rpx;&:nth-child(2){background:#fff;width:72rpx;font-weight:600}}}.sku-btn{width:100%;height:80rpx;background:#FF5000;color:#fff;border:none;border-radius:40rpx;font-size:30rpx;font-weight:600}
</style>
