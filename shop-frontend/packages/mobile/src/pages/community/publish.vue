<script setup lang="ts">
defineOptions({ name: 'CommunityPublishPage' })
import { ref } from 'vue'
import { communityApi } from '@shop/shared'
import { BASE_URL } from '@/http'
const images = ref<string[]>([])
const title = ref('')
const content = ref('')
const submitting = ref(false)

const chooseImage = () => {
  const remaining = 9 - images.value.length
  if (remaining <= 0) { uni.showToast({ title: '最多9张', icon: 'none' }); return }
  uni.chooseImage({ count: remaining, sizeType: ['compressed'], sourceType: ['album', 'camera'], success: (res) => { images.value = [...images.value, ...res.tempFilePaths] } })
}
const removeImage = (i: number) => { images.value.splice(i, 1) }

const uploadImage = (filePath: string): Promise<string> => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    uni.uploadFile({
      url: `${BASE_URL}/api/v1/files/upload?type=community`,
      filePath,
      name: 'file',
      header: { Authorization: token ? `Bearer ${token}` : '' },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data.data.url)
          } else {
            reject(new Error(data.message || '上传失败'))
          }
        } catch { reject(new Error('解析响应失败')) }
      },
      fail: (err) => reject(err)
    })
  })
}

const goBack = () => uni.navigateBack()
const submit = async () => {
  if (!content.value.trim()) { await uni.showToast({title: '请输入内容', icon: 'none'}); return }
  submitting.value = true
  try {
    let imageUrls: string[] = []
    if (images.value.length > 0) {
      await uni.showLoading({title: '上传图片中...'})
      imageUrls = await Promise.all(images.value.map(f => uploadImage(f)))
      uni.hideLoading()
    }
    await communityApi.createNote({
      title: title.value || undefined,
      content: content.value,
      images: imageUrls.map((url, i) => ({ imageUrl: url, sortOrder: i })),
      status: 3
    })
    await uni.showToast({title: '发布成功', icon: 'success'})
    setTimeout(() => uni.navigateBack(), 1000)
  } catch { /* handled */ } finally { submitting.value = false }
}
</script>
<template>
  <view class="cp">
    <view class="hd"><text class="cancel" @click="goBack">取消</text><text class="t">发布笔记</text><text class="pub" :class="{on:content.trim()}" @click="submit">{{submitting?'发布中...':'发布'}}</text></view>
    <view class="img-row">
      <view class="img-item" v-for="(url,i) in images" :key="i">
        <image :src="url" mode="aspectFill"/>
        <view class="del" @click="removeImage(i)">
          <uni-icons type="close" size="14" color="#fff" />
        </view>
      </view>
      <view class="add-btn" v-if="images.length<9" @click="chooseImage">
        <uni-icons type="plusempty" size="28" color="#999" />
        <text>{{images.length}}/9</text>
      </view>
    </view>
    <input v-model="title" placeholder="标题（选填）" class="in" />
    <textarea v-model="content" placeholder="分享你的想法..." class="ta" />
  </view>
</template>
<style scoped lang="scss">
.cp{min-height:100vh;background:#fff;padding-top:88rpx}
.hd{position:fixed;top:0;left:0;right:0;height:88rpx;display:flex;align-items:center;justify-content:space-between;padding:0 24rpx;background:#fff;z-index:10;.cancel{font-size:28rpx;color:#666}.t{font-size:32rpx;font-weight:700}.pub{font-size:28rpx;color:#ccc;font-weight:600;&.on{color:#FF5000}}}
.img-row{display:flex;flex-wrap:wrap;gap:12rpx;padding:20rpx}.img-item{width:200rpx;height:200rpx;position:relative;image{width:100%;height:100%;border-radius:8rpx}.del{position:absolute;top:-8rpx;right:-8rpx;width:36rpx;height:36rpx;background:#000;color:#fff;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:22rpx}}
.add-btn{width:200rpx;height:200rpx;border:2rpx dashed #ddd;border-radius:8rpx;display:flex;flex-direction:column;align-items:center;justify-content:center;color:#999;font-size:24rpx;text{&:first-child{font-size:48rpx}}}
.in{width:100%;height:80rpx;padding:0 24rpx;font-size:28rpx;border-bottom:1rpx solid #f0f0f0}
.ta{width:100%;min-height:400rpx;padding:20rpx 24rpx;font-size:28rpx;line-height:1.6}
</style>
