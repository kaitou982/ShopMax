<script setup lang="ts">defineOptions({ name: 'AddressPage' })
import { ref, onMounted } from 'vue'
import { addressApi, type AddressInfo } from '@shop/shared'

const list = ref<AddressInfo[]>([])
const loading = ref(false)
const form = ref({ receiverName:'',receiverPhone:'',province:'',city:'',district:'',detailAddress:'' })
const editing = ref<AddressInfo|null>(null)
const showForm = ref(false)
const saving = ref(false)

onMounted(load)
async function load() { loading.value = true; try { list.value = await addressApi.getList() } catch {} finally { loading.value = false } }

function openAdd() { editing.value=null;form.value={receiverName:'',receiverPhone:'',province:'',city:'',district:'',detailAddress:''};showForm.value=true }
function openEdit(a:AddressInfo) { editing.value=a;form.value={receiverName:a.receiverName,receiverPhone:a.receiverPhone,province:a.province,city:a.city,district:a.district,detailAddress:a.detailAddress};showForm.value=true }

async function save() {
  saving.value=true
  try {
    if(editing.value) await addressApi.update(editing.value.addressId,{...form.value,isDefault:false})
    else await addressApi.create({...form.value,isDefault:false})
    showForm.value=false;load()
  } catch {} finally { saving.value=false }
}
async function del(id:number) {
  const r=await new Promise<boolean>(resolve=>uni.showModal({title:'确认删除',content:'确定删除该地址？',success:res=>resolve(res.confirm)}))
  if(r){await addressApi.delete(id);load()}
}
async function setDefault(id:number) { await addressApi.setDefault(id);load() }
</script>
<template>
  <view class="ap">
    <view class="hd"><text class="hdt">收货地址</text><button class="add" @click="openAdd">+ 新增</button></view>
    <view v-if="loading" class="empty">加载中...</view>
    <view v-else-if="!list.length&&!showForm" class="empty">暂无收货地址</view>
    <view class="card" v-for="a in list" :key="a.addressId"><text class="an">{{a.receiverName}} {{a.receiverPhone}}</text><text class="aa">{{a.fullAddress}}</text>
      <view class="acts"><view class="act-btn" @click="openEdit(a)">编辑</view><view class="act-btn del" @click="del(a.addressId)">删除</view><view class="act-btn" v-if="!a.isDefault" @click="setDefault(a.addressId)">默认</view><text class="dt" v-else>默认</text></view>
    </view>

    <view class="ol" v-if="showForm" @click="showForm=false"/><view class="fm" v-if="showForm">
      <text class="fmt">{{editing?'编辑':'新增'}}地址</text>
      <input v-model="form.receiverName" placeholder="收货人" /><input v-model="form.receiverPhone" placeholder="手机号" />
      <view class="r3"><input v-model="form.province" placeholder="省" /><input v-model="form.city" placeholder="市" /><input v-model="form.district" placeholder="区" /></view>
      <input v-model="form.detailAddress" placeholder="详细地址" />
      <button class="sv" :loading="saving" @click="save">保存</button>
    </view>
  </view>
</template>
<style scoped lang="scss">
.ap{padding:20rpx;min-height:100vh;background:#f5f5f5}.hd{display:flex;justify-content:space-between;align-items:center;margin-bottom:20rpx}.hdt{font-size:32rpx;font-weight:700}.add{padding:8rpx 24rpx;background:#FF5000;color:#fff;border:none;border-radius:20rpx;font-size:24rpx}
.empty{text-align:center;padding:200rpx 0;color:#999}
.card{background:#fff;border-radius:12rpx;padding:20rpx;margin-bottom:12rpx}.an{font-size:28rpx;font-weight:600;display:block}.aa{font-size:24rpx;color:#666;display:block;margin-top:6rpx}.acts{display:flex;gap:16rpx;margin-top:12rpx;align-items:center}.act-btn{border:1rpx solid #ddd;border-radius:12rpx;padding:4rpx 20rpx;font-size:22rpx;color:#666;flex-shrink:0;white-space:nowrap;text-align:center;line-height:1.6}.act-btn.del{color:#FF3B3B;border-color:#FFCCCC}.dt{font-size:22rpx;color:#FF5000;background:#FFF3EC;padding:2rpx 12rpx;border-radius:6rpx;flex-shrink:0}
.ol{position:fixed;inset:0;background:rgba(0,0,0,.4);z-index:400}.fm{position:fixed;bottom:0;left:0;right:0;background:#fff;border-radius:24rpx 24rpx 0 0;padding:28rpx;z-index:401}.fmt{font-size:30rpx;font-weight:700;display:block;margin-bottom:20rpx}.fm input{width:100%;height:80rpx;border:1px solid #E5E5EA;border-radius:12rpx;padding:0 16rpx;margin-bottom:12rpx;font-size:26rpx}.r3{display:flex;gap:8rpx;input{flex:1}}.sv{width:100%;height:80rpx;background:linear-gradient(90deg,#FF5000,#FF9000);color:#fff;border:none;border-radius:40rpx;font-size:28rpx;margin-top:8rpx}
</style>
