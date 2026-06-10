<script setup lang="ts">
/**
 * 物流详情组件
 * @description 显示物流地图和轨迹
 */
import { ref, shallowRef, onUnmounted, watch, nextTick } from 'vue'
import { NButton, NIcon, NSpin, NEmpty } from 'naive-ui'
import { LocationOutline } from '@vicons/ionicons5'

defineOptions({ name: 'LogisticsDetail' })

// ── 类型定义（含完整默认值） ──

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
  statusText: string
  senderName: string
  senderPhone: string
  senderAddress: string
  senderLatitude?: number
  senderLongitude?: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  receiverLatitude?: number
  receiverLongitude?: number
  traces: TraceItem[]
}

/** 为后端返回的可能缺失字段补全安全默认值 */
const normalizeLogistics = (raw: Record<string, unknown> | null): LogisticsInfo => ({
  id: (raw?.id as number) ?? 0,
  logisticsNo: (raw?.logisticsNo as string) ?? '',
  company: (raw?.company as string) ?? '',
  status: (raw?.status as number) ?? 0,
  statusText: statusTextMap[(raw?.status as number) ?? 0] || '未知',
  senderName: (raw?.senderName as string) ?? '',
  senderPhone: (raw?.senderPhone as string) ?? '',
  senderAddress: (raw?.senderAddress as string) ?? '',
  senderLatitude: raw?.senderLatitude as number | undefined,
  senderLongitude: raw?.senderLongitude as number | undefined,
  receiverName: (raw?.receiverName as string) ?? '',
  receiverPhone: (raw?.receiverPhone as string) ?? '',
  receiverAddress: (raw?.receiverAddress as string) ?? '',
  receiverLatitude: raw?.receiverLatitude as number | undefined,
  receiverLongitude: raw?.receiverLongitude as number | undefined,
  traces: (raw?.traces as TraceItem[]) ?? [],
})

interface Props {
  orderId: number
}

const props = defineProps<Props>()

// ── 状态 ──

const loading = ref(false)
const fetching = ref(false)
const logistics = ref<LogisticsInfo | null>(null)
const mapLoaded = ref(false)
const mapError = ref('')
const mapInstance = shallowRef<any>(null)
const amapNamespace = shallowRef<any>(null)

// 状态文本映射
const statusTextMap: Record<number, string> = {
  0: '已发货', 1: '运输中', 2: '派送中', 3: '已签收'
}
const statusColorMap: Record<number, string> = {
  0: '#909399', 1: '#409EFF', 2: '#E6A23C', 3: '#67C23A'
}

// 高德 JS API Key
const AMAP_JS_KEY = import.meta.env.VITE_AMAP_JS_KEY || ''

// ── 安全地图操作封装 ──

/** 校验 map 实例可用后执行绘制操作 */
const safeMapOp = (fn: () => void) => {
  const m = mapInstance.value
  if (!m || typeof m.add !== 'function') return
  try {
    fn()
  } catch (e) {
    console.warn('地图操作失败:', e)
  }
}

// ── 地图初始化 ──

const initMap = async () => {
  if (!logistics.value) return
  mapError.value = ''
  mapInstance.value = null
  amapNamespace.value = null

  // Key 检查
  if (!AMAP_JS_KEY || AMAP_JS_KEY === 'your_amap_js_key_here') {
    mapError.value = '地图 Key 未配置'
    return
  }

  // 提取坐标
  const l = logistics.value
  let senderLnglat: [number, number] | null = null
  let receiverLnglat: [number, number] | null = null

  if (l.senderLongitude && l.senderLatitude) {
    senderLnglat = [l.senderLongitude, l.senderLatitude]
  }
  if (l.receiverLongitude && l.receiverLatitude) {
    receiverLnglat = [l.receiverLongitude, l.receiverLatitude]
  }

  if (!senderLnglat && !receiverLnglat) {
    mapError.value = '暂无位置坐标数据'
    return
  }

  const mapDiv = document.getElementById('logisticsMap')
  if (!mapDiv) return

  // 防止重复初始化
  if (mapDiv.children.length > 0) {
    mapDiv.innerHTML = ''
    mapInstance.value = null
    amapNamespace.value = null
  }

  try {
    // 加载 SDK
    const AMapLoader = await import('@amap/amap-jsapi-loader')
    const AMap = await AMapLoader.load({
      key: AMAP_JS_KEY,
      version: '2.0',
      plugins: ['AMap.Scale', 'AMap.ToolBar']
    })

    if (!AMap?.Map || typeof AMap.Map !== 'function') {
      mapError.value = '地图 SDK 加载失败'
      return
    }

    amapNamespace.value = AMap

    // 创建地图实例
    const map = new AMap.Map(mapDiv, {
      zoom: 10,
      resizeEnable: true,
      viewMode: '2D'
    })

    if (!map || typeof map.add !== 'function') {
      mapError.value = '地图初始化失败，请检查 API Key'
      return
    }

    mapInstance.value = map

    // 添加发件地标记
    if (senderLnglat) {
      safeMapOp(() => {
        const pos = new AMap.LngLat(senderLnglat![0], senderLnglat![1])
        mapInstance.value.add(new AMap.Marker({
          position: pos,
          title: l.senderAddress,
          content: buildMarkerHtml('📦 发件地', '#67C23A', l.senderAddress),
          offset: new AMap.Pixel(-30, -50)
        }))
      })
    }

    // 添加收件地标记
    if (receiverLnglat) {
      safeMapOp(() => {
        const pos = new AMap.LngLat(receiverLnglat![0], receiverLnglat![1])
        mapInstance.value.add(new AMap.Marker({
          position: pos,
          title: l.receiverAddress,
          content: buildMarkerHtml('📍 收件地', '#ff4444', l.receiverAddress),
          offset: new AMap.Pixel(-30, -50)
        }))
      })
    }

    // 绘制路线 & 货车
    if (senderLnglat && receiverLnglat) {
      await drawRoute(senderLnglat, receiverLnglat)
      addTruckMarker(senderLnglat, receiverLnglat)
    }

    // 自适应视野
    safeMapOp(() => {
      mapInstance.value.setFitView()
    })

    mapLoaded.value = true

  } catch (error) {
    console.error('加载地图失败:', error)
    mapError.value = '地图加载失败'
  }
}

// ── Marker HTML 构建 ──

const buildMarkerHtml = (label: string, bgColor: string, address?: string) => {
  const safe = address || ''
  return `<div style="display:flex;flex-direction:column;align-items:center;"><div style="background:${bgColor};color:white;padding:6px 12px;border-radius:8px;font-size:13px;white-space:nowrap;box-shadow:0 2px 8px rgba(0,0,0,0.15);">${label}</div><div style="font-size:11px;color:#666;margin-top:4px;max-width:150px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${safe}</div></div>`
}

// ── 路线绘制（保留失败直线兜底） ──

const drawRoute = async (
  senderLnglat: [number, number],
  receiverLnglat: [number, number]
) => {
  const AMap = amapNamespace.value
  if (!AMap) return

  // 直线兜底
  const drawFallbackLine = () => {
    safeMapOp(() => {
      if (typeof AMap.Polyline !== 'function') return
      mapInstance.value.add(new AMap.Polyline({
        path: [senderLnglat, receiverLnglat],
        strokeColor: '#3366FF',
        strokeWeight: 4,
        strokeOpacity: 0.8,
        strokeStyle: 'dashed'
      }))
    })
  }

  // 尝试从后端获取真实驾车路线
  try {
    const { getHttpClient } = await import('@shop/shared')
    const http = getHttpClient()
    const routeData = await http.get('/api/v1/logistics/route', {
      originLng: senderLnglat[0],
      originLat: senderLnglat[1],
      destLng: receiverLnglat[0],
      destLat: receiverLnglat[1]
    }) as { distance?: string; duration?: string; routePoints?: number[][] } | null

    if (routeData?.routePoints?.length) {
      safeMapOp(() => {
        if (typeof AMap.Polyline !== 'function') return
        const path = routeData.routePoints!.map(p => new AMap.LngLat(p[0], p[1]))
        mapInstance.value.add(new AMap.Polyline({
          path,
          strokeColor: '#3366FF',
          strokeWeight: 4,
          strokeOpacity: 0.8
        }))
      })
    } else {
      drawFallbackLine()
    }
  } catch {
    // 路线 API 失败，使用直线兜底
    drawFallbackLine()
  }
}

// ── 货车标记 ──

const addTruckMarker = (
  senderLnglat: [number, number],
  receiverLnglat: [number, number]
) => {
  const AMap = amapNamespace.value
  if (!AMap || typeof AMap.Marker !== 'function') return

  const status = logistics.value?.status ?? 0
  let lnglat: [number, number]
  let label: string

  if (status === 0) {
    lnglat = senderLnglat
    label = '已发货'
  } else if (status === 3) {
    lnglat = receiverLnglat
    label = '已签收'
  } else {
    lnglat = [
      (senderLnglat[0] + receiverLnglat[0]) / 2,
      (senderLnglat[1] + receiverLnglat[1]) / 2
    ]
    label = '运输中'
  }

  safeMapOp(() => {
    const pos = new AMap.LngLat(lnglat[0], lnglat[1])
    mapInstance.value.add(new AMap.Marker({
      position: pos,
      content: `<div style="display:flex;flex-direction:column;align-items:center;animation:bounce 1s infinite;"><div style="background:#FF9900;color:white;padding:4px 8px;border-radius:6px;font-size:12px;white-space:nowrap;box-shadow:0 2px 6px rgba(0,0,0,0.2);">🚚 ${label}</div></div><style>@keyframes bounce{0%,100%{transform:translateY(0)}50%{transform:translateY(-5px)}}</style>`,
      offset: new AMap.Pixel(-20, -40),
      zIndex: 100
    }))
  })
}

// ── 查询物流 ──

const fetchLogistics = async () => {
  if (!props.orderId) return
  // 防止并发重复调用
  if (fetching.value) return
  fetching.value = true

  loading.value = true
  logistics.value = null
  mapLoaded.value = false
  mapInstance.value = null
  amapNamespace.value = null
  mapError.value = ''

  try {
    const { getHttpClient } = await import('@shop/shared')
    const http = getHttpClient()
    const raw = await http.get(`/api/v1/logistics/order/${props.orderId}`)
    logistics.value = normalizeLogistics(raw as Record<string, unknown> | null)
  } catch (error) {
    console.error('查询物流失败:', error)
  } finally {
    loading.value = false
    // 等待 DOM 完全就绪后再初始化地图
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 300))
    if (logistics.value) {
      await initMap()
    }
    fetching.value = false
  }
}

// ── 刷新物流 ──

const refreshLogistics = async () => {
  if (!logistics.value?.id) return
  loading.value = true
  try {
    const { getHttpClient } = await import('@shop/shared')
    const http = getHttpClient()
    await http.post(`/api/v1/logistics/${logistics.value.id}/refresh`, {})
  } catch (error) {
    console.error('刷新物流失败:', error)
  }
  await fetchLogistics()
}

// ── 生命周期 ──

watch(() => props.orderId, () => { if (props.orderId) fetchLogistics() }, { immediate: true })

onUnmounted(() => {
  // 清理地图实例
  if (mapInstance.value && typeof mapInstance.value.destroy === 'function') {
    mapInstance.value.destroy()
  }
  mapInstance.value = null
  amapNamespace.value = null
})

// ── 时间格式化 ──

const formatTraceTime = (time: string) => {
  if (!time) return ''
  try {
    const parsed = JSON.parse(time)
    if (Array.isArray(parsed) && parsed.length >= 5) {
      const [y, m, d, h, min, s] = parsed
      return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}:${String(s || 0).padStart(2, '0')}`
    }
  } catch { /* ignore parse error, return raw */ }
  return time
}
</script>

<template>
  <div class="logistics-detail">
    <div v-if="loading" class="loading-container">
      <n-spin size="large" />
    </div>

    <template v-else>
      <!-- 物流基本信息 -->
      <div class="logistics-header" v-if="logistics">
        <div class="logistics-info">
          <span class="company">{{ logistics.company }}</span>
          <span class="logistics-no">运单号：{{ logistics.logisticsNo }}</span>
        </div>
        <div class="logistics-status" :style="{ color: statusColorMap[logistics.status] }">
          {{ logistics.statusText }}
        </div>
        <n-button type="primary" text @click="refreshLogistics" :loading="loading">
          刷新物流
        </n-button>
      </div>

      <!-- 地址信息卡片 -->
      <div class="address-cards" v-if="logistics">
        <div class="addr-card sender">
          <div class="addr-label">📦 发件地址</div>
          <div class="addr-name">{{ logistics.senderName }} {{ logistics.senderPhone }}</div>
          <div class="addr-text">{{ logistics.senderAddress }}</div>
        </div>
        <div class="addr-arrow">→</div>
        <div class="addr-card receiver">
          <div class="addr-label">📍 收件地址</div>
          <div class="addr-name">{{ logistics.receiverName }} {{ logistics.receiverPhone }}</div>
          <div class="addr-text">{{ logistics.receiverAddress }}</div>
        </div>
      </div>

      <!-- 地图区域 -->
      <div class="map-container" v-if="logistics">
        <div v-if="mapError" class="map-error">
          <span>{{ mapError }}</span>
        </div>
        <div id="logisticsMap" style="height: 300px; border-radius: 8px;"></div>
      </div>

      <!-- 轨迹列表 -->
      <div class="trace-list" v-if="logistics?.traces?.length">
        <div class="trace-title">物流轨迹</div>
        <div
          v-for="(trace, index) in logistics.traces"
          :key="trace.id"
          class="trace-item"
          :class="{ active: index === 0 }"
        >
          <div class="trace-dot-wrapper">
            <div class="trace-dot" :class="{ active: index === 0 }" />
            <div v-if="index < logistics.traces.length - 1" class="trace-line" />
          </div>
          <div class="trace-content">
            <div class="trace-time">{{ formatTraceTime(trace.traceTime) }}</div>
            <div class="trace-location" v-if="trace.location">
              <n-icon><LocationOutline /></n-icon>
              {{ trace.location }}
            </div>
            <div class="trace-text">{{ trace.content }}</div>
          </div>
        </div>
      </div>

      <n-empty v-else description="暂无物流信息" />
    </template>
  </div>
</template>

<style scoped lang="scss">
.logistics-detail { padding: 16px; }

.loading-container {
  display: flex; justify-content: center; align-items: center; min-height: 200px;
}

.logistics-header {
  display: flex; align-items: center; gap: 16px;
  padding: 16px; background: #f5f7fa; border-radius: 8px; margin-bottom: 16px;

  .logistics-info {
    flex: 1;
    .company { font-weight: bold; font-size: 16px; margin-right: 12px; }
    .logistics-no { color: #909399; font-size: 14px; }
  }
  .logistics-status { font-weight: bold; font-size: 16px; }
}

.address-cards {
  display: flex; align-items: center; gap: 12px; margin-bottom: 16px;

  .addr-card {
    flex: 1; padding: 12px 16px; border-radius: 8px;
    background: #fafafa; border: 1px solid #ebeef5;

    &.sender { border-left: 3px solid #67C23A; }
    &.receiver { border-left: 3px solid #ff4444; }

    .addr-label { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 4px; }
    .addr-name { font-size: 13px; color: #606266; margin-bottom: 2px; }
    .addr-text { font-size: 12px; color: #909399; word-break: break-all; }
  }

  .addr-arrow { font-size: 20px; color: #c0c4cc; flex-shrink: 0; }
}

.map-container {
  margin-bottom: 16px; border: 1px solid #ebeef5;
  border-radius: 8px; overflow: hidden; position: relative;
}

.map-error {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  z-index: 10; background: rgba(255,255,255,0.9);
  padding: 8px 16px; border-radius: 6px; font-size: 13px; color: #909399;
}

.trace-list {
  .trace-title {
    font-weight: bold; font-size: 16px; margin-bottom: 16px;
    padding-left: 12px; border-left: 3px solid #409eff;
  }
}

.trace-item {
  display: flex; gap: 12px; padding: 12px 0;

  &.active .trace-content .trace-text { color: #303133; font-weight: bold; }

  .trace-dot-wrapper {
    display: flex; flex-direction: column; align-items: center; width: 16px;
    .trace-dot {
      width: 12px; height: 12px; border-radius: 50%; background: #dcdfe6; flex-shrink: 0;
      &.active { background: #409eff; box-shadow: 0 0 0 3px rgba(64,158,255,0.2); }
    }
    .trace-line { width: 2px; flex: 1; background: #dcdfe6; margin-top: 4px; }
  }

  .trace-content {
    flex: 1;
    .trace-time { font-size: 13px; color: #909399; margin-bottom: 4px; }
    .trace-location { font-size: 13px; color: #606266; margin-bottom: 4px; display: flex; align-items: center; gap: 4px; }
    .trace-text { font-size: 14px; color: #606266; }
  }
}
</style>
