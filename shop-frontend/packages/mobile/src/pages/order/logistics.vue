<script setup lang="ts">
/**
 * 物流详情页面
 * @description 显示物流地图和轨迹
 */
import { ref, shallowRef, onMounted } from 'vue'

defineOptions({
  name: 'LogisticsDetail'
})

interface TraceItem {
  id: number
  traceTime: string
  location: string
  content: string
  latitude?: number
  longitude?: number
}

interface LogisticsInfo {
  id: number
  logisticsNo: string
  company: string
  status: number
  senderName?: string
  senderPhone?: string
  senderAddress?: string
  senderLatitude?: number
  senderLongitude?: number
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  receiverLatitude?: number
  receiverLongitude?: number
  traces: TraceItem[]
}

const loading = ref<boolean>(false)
const logistics = shallowRef<LogisticsInfo | null>(null)

/** 将 API 响应转为全新普通对象，避免 Vue 对冻结对象添加内部标记时报错 */
const normalizeLogistics = (raw: Record<string, unknown>): LogisticsInfo => ({
  id: (raw.id as number) ?? 0,
  logisticsNo: (raw.logisticsNo as string) ?? '',
  company: (raw.company as string) ?? '',
  status: (raw.status as number) ?? 0,
  senderName: (raw.senderName as string) ?? '',
  senderPhone: (raw.senderPhone as string) ?? '',
  senderAddress: (raw.senderAddress as string) ?? '',
  senderLatitude: raw.senderLatitude as number | undefined,
  senderLongitude: raw.senderLongitude as number | undefined,
  receiverName: (raw.receiverName as string) ?? '',
  receiverPhone: (raw.receiverPhone as string) ?? '',
  receiverAddress: (raw.receiverAddress as string) ?? '',
  receiverLatitude: raw.receiverLatitude as number | undefined,
  receiverLongitude: raw.receiverLongitude as number | undefined,
  traces: (raw.traces as TraceItem[]) ?? [],
})

// 地图相关
const latitude = ref<number>(22.54)
const longitude = ref<number>(113.93)
const markers = shallowRef<any[]>([])
const polyline = shallowRef<any[]>([])

// 状态文本映射
const statusTextMap: Record<number, string> = {
  0: '已发货',
  1: '运输中',
  2: '派送中',
  3: '已签收'
}

// 状态颜色映射
const statusColorMap: Record<number, string> = {
  0: '#909399',
  1: '#2979ff',
  2: '#ff9900',
  3: '#19be6b'
}

// 获取页面参数
const orderId = ref<number>(0)

// 格式化 traceTime 为可读字符串
const formatTraceTime = (time: string) => {
  if (!time) return ''
  try {
    // 处理 LocalDateTime 数组格式 [2026,6,7,10,30,0]
    const parsed = JSON.parse(time)
    if (Array.isArray(parsed) && parsed.length >= 5) {
      const [y, m, d, h, min, s] = parsed
      return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}:${String(s || 0).padStart(2, '0')}`
    }
  } catch {}
  return time
}

// 初始化地图数据
const initMapData = () => {
  if (!logistics.value) return

  const markersList: any[] = []
  const allCoords: Array<{ latitude: number; longitude: number }> = []

  // 添加发件地标记
  if (logistics.value.senderLatitude && logistics.value.senderLongitude) {
    allCoords.push({ latitude: logistics.value.senderLatitude, longitude: logistics.value.senderLongitude })
    markersList.push({
      id: 90001,
      latitude: logistics.value.senderLatitude,
      longitude: logistics.value.senderLongitude,
      title: logistics.value.senderAddress || '发件地',
      iconPath: '/static/icons/marker-start.png',
      width: 30,
      height: 30,
      callout: {
        content: '📦 ' + (logistics.value.senderAddress || '发件地'),
        display: 'ALWAYS',
        borderRadius: 4,
        padding: 4,
        fontSize: 12,
        bgColor: '#67C23A',
        color: '#ffffff'
      }
    })
  }

  // 添加收件地标记
  if (logistics.value.receiverLatitude && logistics.value.receiverLongitude) {
    allCoords.push({ latitude: logistics.value.receiverLatitude, longitude: logistics.value.receiverLongitude })
    markersList.push({
      id: 90002,
      latitude: logistics.value.receiverLatitude,
      longitude: logistics.value.receiverLongitude,
      title: logistics.value.receiverAddress || '收件地',
      iconPath: '/static/icons/marker-end.png',
      width: 30,
      height: 30,
      callout: {
        content: '📍 ' + (logistics.value.receiverAddress || '收件地'),
        display: 'ALWAYS',
        borderRadius: 4,
        padding: 4,
        fontSize: 12,
        bgColor: '#ff4444',
        color: '#ffffff'
      }
    })
  }

  // 添加轨迹中间点
  if (logistics.value.traces?.length) {
    const validTraces = logistics.value.traces.filter(t => t.latitude && t.longitude)
    validTraces.forEach((trace, index) => {
      allCoords.push({ latitude: trace.latitude!, longitude: trace.longitude! })
      const isFirst = index === 0
      const isLast = index === validTraces.length - 1

      // 避免与发件/收件标记重复
      markersList.push({
        id: trace.id,
        latitude: trace.latitude,
        longitude: trace.longitude,
        title: trace.location,
        iconPath: isLast
          ? '/static/icons/marker-end.png'
          : isFirst
          ? '/static/icons/marker-start.png'
          : '/static/icons/marker-middle.png',
        width: 24,
        height: 24,
        callout: {
          content: trace.location || trace.content,
          display: 'BYCLICK',
          borderRadius: 4,
          padding: 4,
          fontSize: 11,
          bgColor: '#409EFF',
          color: '#ffffff'
        }
      })
    })
  }

  // 设置地图中心点
  if (allCoords.length > 0) {
    const mid = allCoords[Math.floor(allCoords.length / 2)]
    latitude.value = mid.latitude
    longitude.value = mid.longitude
  }

  markers.value = JSON.parse(JSON.stringify(markersList))

  // 构建路线
  if (allCoords.length > 1) {
    polyline.value = JSON.parse(JSON.stringify([{
      points: allCoords,
      color: '#3366FF',
      width: 4,
      arrowLine: true
    }]))
  }
}

// 查询物流信息
const fetchLogistics = async () => {
  if (!orderId.value) return

  loading.value = true
  try {
    const { getHttpClient } = await import('@shop/shared')
    const http = getHttpClient()
    const res = await http.get(`/api/v1/logistics/order/${orderId.value}`)
    // 兼容两种响应格式: { data: ... } 或直接返回数据
    const raw = (res && typeof res === 'object' && 'data' in res) ? (res as Record<string, unknown>).data as Record<string, unknown> : res as Record<string, unknown>
    if (raw && raw.id) {
      logistics.value = normalizeLogistics(raw)
      initMapData()
    }
  } catch (error) {
    console.error('查询物流失败:', error)
    uni.showToast({
      title: '查询物流失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 刷新物流
const refreshLogistics = async () => {
  if (!logistics.value?.id) return

  loading.value = true
  try {
    const { getHttpClient } = await import('@shop/shared')
    const http = getHttpClient()
    await http.post(`/api/v1/logistics/${logistics.value.id}/refresh`)
    await fetchLogistics()
    uni.showToast({
      title: '刷新成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('刷新物流失败:', error)
    uni.showToast({
      title: '刷新失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage as any).options || (currentPage as any).$page?.options

  if (options?.orderId) {
    orderId.value = Number(options.orderId)
    fetchLogistics()
  }
})
</script>

<template>
  <view class="logistics-detail">
    <!-- 物流基本信息 -->
    <view class="logistics-header" v-if="logistics">
      <view class="logistics-info">
        <text class="company">{{ logistics.company }}</text>
        <text class="logistics-no">运单号：{{ logistics.logisticsNo }}</text>
      </view>
      <view class="logistics-status" :style="{ color: statusColorMap[logistics.status] }">
        {{ statusTextMap[logistics.status] || '未知' }}
      </view>
      <view class="refresh-btn" @click="refreshLogistics">
        <text>刷新物流</text>
      </view>
    </view>

    <!-- 地址卡片 -->
    <view class="addr-section" v-if="logistics">
      <view class="addr-card sender">
        <text class="addr-label">📦 发件</text>
        <text class="addr-text">{{ logistics.senderAddress || '-' }}</text>
      </view>
      <text class="addr-arrow">→</text>
      <view class="addr-card receiver">
        <text class="addr-label">📍 收件</text>
        <text class="addr-text">{{ logistics.receiverAddress || '-' }}</text>
      </view>
    </view>

    <!-- 地图区域 -->
    <view class="map-container" v-if="markers.length > 0">
      <map
        id="logisticsMap"
        :latitude="latitude"
        :longitude="longitude"
        :markers="markers"
        :polyline="polyline"
        :scale="8"
        style="width: 100%; height: 400rpx;"
        show-location
      />
    </view>

    <!-- 轨迹列表 -->
    <view class="trace-list" v-if="logistics?.traces?.length">
      <view class="trace-title">物流轨迹</view>
      <view
        v-for="(trace, index) in logistics.traces"
        :key="trace.id"
        class="trace-item"
        :class="{ active: index === 0 }"
      >
        <view class="trace-dot-wrapper">
          <view class="trace-dot" :class="{ active: index === 0 }"></view>
          <view v-if="index < logistics.traces.length - 1" class="trace-line"></view>
        </view>
        <view class="trace-content">
          <text class="trace-time">{{ formatTraceTime(trace.traceTime) }}</text>
          <text class="trace-location" v-if="trace.location">
            📍 {{ trace.location }}
          </text>
          <text class="trace-text">{{ trace.content }}</text>
        </view>
      </view>
    </view>

    <!-- 无物流信息 -->
    <view class="empty" v-else-if="!loading">
      <text class="empty-text">暂无物流信息</text>
    </view>

    <!-- 加载中 -->
    <view class="loading" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
.logistics-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 24rpx;
}

.logistics-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;

  .logistics-info {
    flex: 1;

    .company {
      font-weight: bold;
      font-size: 32rpx;
      margin-right: 16rpx;
    }

    .logistics-no {
      color: #909399;
      font-size: 24rpx;
    }
  }

  .logistics-status {
    font-weight: bold;
    font-size: 28rpx;
    margin-right: 16rpx;
  }

  .refresh-btn {
    padding: 8rpx 16rpx;
    background-color: #2979ff;
    border-radius: 8rpx;

    text {
      color: #ffffff;
      font-size: 24rpx;
    }
  }
}

// 地址卡片
.addr-section {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
  padding: 24rpx;
  background-color: #ffffff;
  border-radius: 16rpx;

  .addr-card {
    flex: 1;

    &.sender {
      .addr-label { color: #67C23A; }
    }

    &.receiver {
      .addr-label { color: #ff4444; }
    }

    .addr-label {
      font-size: 24rpx;
      font-weight: 600;
      margin-bottom: 6rpx;
    }

    .addr-text {
      font-size: 22rpx;
      color: #909399;
      word-break: break-all;
    }
  }

  .addr-arrow {
    font-size: 32rpx;
    color: #c0c4cc;
    flex-shrink: 0;
  }
}

.map-container {
  margin-bottom: 24rpx;
  border-radius: 16rpx;
  overflow: hidden;
  border: 1rpx solid #ebeef5;
}

.trace-list {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;

  .trace-title {
    font-weight: bold;
    font-size: 28rpx;
    margin-bottom: 24rpx;
    padding-left: 16rpx;
    border-left: 6rpx solid #2979ff;
  }
}

.trace-item {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 0;

  &.active {
    .trace-content {
      .trace-text {
        color: #303133;
        font-weight: bold;
      }
    }
  }

  .trace-dot-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 24rpx;

    .trace-dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
      background-color: #dcdfe6;
      flex-shrink: 0;

      &.active {
        background-color: #2979ff;
      }
    }

    .trace-line {
      width: 4rpx;
      flex: 1;
      background-color: #dcdfe6;
      margin-top: 8rpx;
    }
  }

  .trace-content {
    flex: 1;

    .trace-time {
      font-size: 24rpx;
      color: #909399;
      margin-bottom: 8rpx;
    }

    .trace-location {
      font-size: 24rpx;
      color: #606266;
      margin-bottom: 8rpx;
    }

    .trace-text {
      font-size: 28rpx;
      color: #606266;
    }
  }
}

.empty {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;

  .empty-text {
    color: #909399;
    font-size: 28rpx;
  }
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;

  .loading-text {
    color: #909399;
    font-size: 28rpx;
  }
}
</style>
