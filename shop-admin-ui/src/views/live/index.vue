<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { useUserStore } from '@/stores/modules/user'
import {
  getAnchorList, auditAnchor,
  getLiveRoomList, createLiveRoom, updateLiveRoom, deleteLiveRoom, startLive, endLive,
  getRoomProducts, addLiveProduct, removeLiveProduct, setExplaining, unexplain,
  getGiftList, createGift, updateGift, deleteGift,
  type Anchor, type LiveRoom, type LiveProduct, type Gift
} from '@/api/modules/live'

defineOptions({ name: 'LiveManagement' })

const userStore = useUserStore()

const activeTab = ref<'anchor' | 'room' | 'products' | 'gifts'>('anchor')

// ============ 主播管理 ============
const anchorLoading = ref(false)
const anchorList = ref<Anchor[]>([])
const anchorTotal = ref(0)
const anchorQuery = reactive<{ pageNum: number; pageSize: number; status?: number }>({ pageNum: 1, pageSize: 10 })

const anchorStatusMap: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已禁用' }
const anchorLevelMap: Record<number, string> = { 1: '普通', 2: '铜牌', 3: '银牌', 4: '金牌', 5: '钻石' }
const anchorStatusColors: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }

const fetchAnchors = async () => {
  try {
    anchorLoading.value = true
    const res = await getAnchorList({ ...anchorQuery })
    anchorList.value = res.records
    anchorTotal.value = res.total
  } finally {
    anchorLoading.value = false
  }
}

const handleAudit = async (row: Anchor, status: number) => {
  let rejectReason = ''
  if (status === 2) {
    try {
      const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', { type: 'warning' })
      if (!value) return
      rejectReason = value
    } catch { return }
  }
  await auditAnchor(row.id, { status, rejectReason })
  ElMessage.success(status === 1 ? '已通过审核' : '已拒绝')
  fetchAnchors()
}

// ============ 直播间管理 ============
const roomLoading = ref(false)
const roomList = ref<LiveRoom[]>([])
const roomTotal = ref(0)
const roomQuery = reactive<{ pageNum: number; pageSize: number; type?: number; status?: number }>({ pageNum: 1, pageSize: 10 })

const roomDialogVisible = ref(false)
const roomDialogType = ref<'add' | 'edit'>('add')
const roomFormRef = ref<FormInstance>()
const roomFormData = reactive<Partial<LiveRoom>>({
  anchorId: undefined, title: '', type: 1, startTime: '', cover: '', notice: '', pushUrl: '', pullUrl: ''
})

const roomTypeMap: Record<number, string> = { 1: '推荐', 2: '穿搭', 3: '美妆', 4: '美食', 5: '家居', 6: '数码', 7: '母婴' }
const roomStatusMap: Record<number, string> = { 0: '预告', 1: '直播中', 2: '已结束', 3: '已关闭' }
const roomStatusColors: Record<number, string> = { 0: 'info', 1: 'success', 2: '', 3: 'danger' }

const fetchRooms = async () => {
  try {
    roomLoading.value = true
    const res = await getLiveRoomList({ ...roomQuery })
    roomList.value = res.records
    roomTotal.value = res.total
  } finally {
    roomLoading.value = false
  }
}

const handleRoomAdd = () => {
  roomDialogType.value = 'add'
  Object.assign(roomFormData, { anchorId: undefined, title: '', type: 1, startTime: '', cover: '', notice: '', pushUrl: '', pullUrl: '' })
  roomFormRef.value?.resetFields()
  roomDialogVisible.value = true
}

const handleRoomEdit = (row: LiveRoom) => {
  roomDialogType.value = 'edit'
  Object.assign(roomFormData, { ...row })
  roomDialogVisible.value = true
}

const handleRoomSubmit = async () => {
  await roomFormRef.value?.validate()
  try {
    if (roomDialogType.value === 'add') {
      await createLiveRoom(roomFormData)
      ElMessage.success('创建成功')
    } else {
      await updateLiveRoom(roomFormData.id!, roomFormData)
      ElMessage.success('更新成功')
    }
    roomDialogVisible.value = false
    fetchRooms()
  } catch { /* handled */ }
}

const handleRoomDelete = async (row: LiveRoom) => {
  await ElMessageBox.confirm('确认删除该直播间？', '提示', { type: 'warning' })
  await deleteLiveRoom(row.id)
  ElMessage.success('删除成功')
  fetchRooms()
}

const handleStartLive = async (row: LiveRoom) => {
  await ElMessageBox.confirm('确认开始直播？直播将正式开始。', '提示', { type: 'info' })
  await startLive(row.id)
  ElMessage.success('直播已开始')
  fetchRooms()
}

// ============ 生成推流地址 ============
const streamUrlDialogVisible = ref(false)
const streamUrls = ref({ pushUrl: '', pullUrlFlv: '', pullUrlHls: '' })

const handleGenerateUrl = async (row: LiveRoom) => {
  await ElMessageBox.confirm('确认生成推流地址？生成后房间状态将变为"待推流"。', '提示', { type: 'info' })
  try {
    const res = await startLive(row.id)
    // 从推流地址中提取 token
    const token = res.pushUrl?.split('token=')[1] || ''
    const tokenParam = token ? `?token=${token}` : ''
    streamUrls.value = {
      pushUrl: res.pushUrl || '',
      pullUrlFlv: res.pullUrl || `http://localhost:8085/live/${row.id}.flv${tokenParam}`,
      pullUrlHls: `http://localhost:8085/live/${row.id}.m3u8${tokenParam}`
    }
    streamUrlDialogVisible.value = true
    fetchRooms()
  } catch { /* handled */ }
}

const handleViewUrl = (row: LiveRoom) => {
  const token = row.pushUrl?.split('token=')[1] || ''
  const tokenParam = token ? `?token=${token}` : ''
  streamUrls.value = {
    pushUrl: row.pushUrl || '',
    pullUrlFlv: row.pullUrl || `http://localhost:8085/live/${row.id}.flv${tokenParam}`,
    pullUrlHls: `http://localhost:8085/live/${row.id}.m3u8${tokenParam}`
  }
  streamUrlDialogVisible.value = true
}

const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

const handleEndLive = async (row: LiveRoom) => {
  await ElMessageBox.confirm('确认结束直播？结束后将生成回放。', '提示', { type: 'warning' })
  await endLive(row.id)
  ElMessage.success('直播已结束')
  fetchRooms()
}

// ============ 直播商品管理 ============
const productRoomId = ref<number>(0)
const productList = ref<LiveProduct[]>([])
const productLoading = ref(false)
const productDialogVisible = ref(false)
const productFormData = reactive<Partial<LiveProduct>>({ roomId: 0, productId: 0, skuId: 0, livePrice: 0, sortOrder: 0 })

const fetchProducts = async () => {
  if (!productRoomId.value) { productList.value = []; return }
  try { productLoading.value = true; productList.value = await getRoomProducts(productRoomId.value) } finally { productLoading.value = false }
}
const handleProductAdd = () => { Object.assign(productFormData, { roomId: productRoomId.value || 0, productId: 0, skuId: 0, livePrice: 0, sortOrder: 0 }); productDialogVisible.value = true }
const handleProductSubmit = async () => {
  try { await addLiveProduct(productFormData); ElMessage.success('上架成功'); productDialogVisible.value = false; fetchProducts() } catch { /* handled */ }
}
const handleProductRemove = async (id: number) => {
  await ElMessageBox.confirm('确认下架该商品？', '提示', { type: 'warning' })
  await removeLiveProduct(id); ElMessage.success('已下架'); fetchProducts()
}
const handleProductExplain = async (id: number) => { await setExplaining(id); ElMessage.success('已标记为讲解中'); fetchProducts() }
const handleProductUnexplain = async (id: number) => { await unexplain(id); ElMessage.success('已取消讲解'); fetchProducts() }

// ============ 礼物管理 ============
const giftLoading = ref(false)
const giftList = ref<Gift[]>([])
const giftDialogVisible = ref(false)
const giftDialogType = ref<'add' | 'edit'>('add')
const giftFormRef = ref<FormInstance>()
const giftFormData = reactive<Partial<Gift>>({
  id: undefined, name: '', icon: '', animationUrl: '', price: 0, sortOrder: 0
})

const fetchGifts = async () => {
  try {
    giftLoading.value = true
    giftList.value = await getGiftList()
  } finally {
    giftLoading.value = false
  }
}

const handleGiftAdd = () => {
  giftDialogType.value = 'add'
  Object.assign(giftFormData, { id: undefined, name: '', icon: '', animationUrl: '', price: 0, sortOrder: 0 })
  giftFormRef.value?.resetFields()
  giftDialogVisible.value = true
}

const handleGiftEdit = (row: Gift) => {
  giftDialogType.value = 'edit'
  Object.assign(giftFormData, { ...row })
  giftDialogVisible.value = true
}

const handleGiftSubmit = async () => {
  await giftFormRef.value?.validate()
  try {
    if (giftDialogType.value === 'add') {
      await createGift(giftFormData)
      ElMessage.success('创建成功')
    } else {
      await updateGift(giftFormData.id!, giftFormData)
      ElMessage.success('更新成功')
    }
    giftDialogVisible.value = false
    fetchGifts()
  } catch { /* handled */ }
}

const handleGiftDelete = async (row: Gift) => {
  await ElMessageBox.confirm('确认删除该礼物？', '提示', { type: 'warning' })
  await deleteGift(row.id)
  ElMessage.success('删除成功')
  fetchGifts()
}

const handleTabChange = () => {
  if (activeTab.value === 'anchor') fetchAnchors()
  else if (activeTab.value === 'room') fetchRooms()
  else if (activeTab.value === 'products') fetchProducts()
  else fetchGifts()
}

onMounted(() => fetchAnchors())
</script>

<template>
  <div v-if="userStore.isAdmin" class="live-page page-container">
    <div class="page-header">
      <h2>直播管理</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 主播管理 -->
      <el-tab-pane label="主播管理" name="anchor">
        <div class="tab-header">
          <el-select v-model="anchorQuery.status" placeholder="审核状态" clearable style="width:140px" @change="fetchAnchors">
            <el-option v-for="(label, val) in anchorStatusMap" :key="val" :label="label" :value="Number(val)" />
          </el-select>
        </div>
        <el-table v-loading="anchorLoading" :data="anchorList" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="nickname" label="昵称" width="120" />
          <el-table-column prop="realName" label="真实姓名" width="100" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="等级" width="80">
            <template #default="{ row }">
              <el-tag size="small">{{ anchorLevelMap[row.level] || '普通' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="粉丝" width="80" prop="fansCount" />
          <el-table-column label="直播场次" width="90" prop="totalLiveCount" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="anchorStatusColors[row.status]" size="small">{{ anchorStatusMap[row.status] || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="introduction" label="简介" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button type="success" link @click="handleAudit(row, 1)">通过</el-button>
                <el-button type="danger" link @click="handleAudit(row, 2)">拒绝</el-button>
              </template>
              <span v-else style="color:#999">--</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="anchorQuery.pageNum" v-model:page-size="anchorQuery.pageSize"
            :total="anchorTotal" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
            @current-change="fetchAnchors" @size-change="fetchAnchors" />
        </div>
      </el-tab-pane>

      <!-- 直播间管理 -->
      <el-tab-pane label="直播间管理" name="room">
        <div class="tab-header">
          <el-button type="primary" :icon="Plus" @click="handleRoomAdd">新增直播间</el-button>
          <el-select v-model="roomQuery.type" placeholder="分类" clearable style="width:120px" @change="fetchRooms">
            <el-option v-for="(label, val) in roomTypeMap" :key="val" :label="label" :value="Number(val)" />
          </el-select>
          <el-select v-model="roomQuery.status" placeholder="状态" clearable style="width:120px" @change="fetchRooms">
            <el-option v-for="(label, val) in roomStatusMap" :key="val" :label="label" :value="Number(val)" />
          </el-select>
        </div>
        <el-table v-loading="roomLoading" :data="roomList" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column label="主播" width="120">
            <template #default="{ row }">{{ row.anchorNickname || '-' }}</template>
          </el-table-column>
          <el-table-column label="分类" width="80">
            <template #default="{ row }">
              <el-tag size="small">{{ roomTypeMap[row.type] || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="roomStatusColors[row.status]" size="small">{{ roomStatusMap[row.status] || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预告时间" width="160">
            <template #default="{ row }">{{ row.startTime }}</template>
          </el-table-column>
          <el-table-column label="观看" width="80" prop="totalViewCount" />
          <el-table-column label="点赞" width="80" prop="likeCount" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" type="primary" link @click="handleRoomEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 0" type="success" link @click="handleGenerateUrl(row)">生成地址</el-button>
              <el-button v-if="row.status === 4 && row.pushUrl" type="info" link @click="handleViewUrl(row)">查看地址</el-button>
              <el-button v-if="row.status === 1 || row.status === 4" type="warning" link @click="handleEndLive(row)">结束</el-button>
              <el-button v-if="row.status !== 1" type="danger" link @click="handleRoomDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="roomQuery.pageNum" v-model:page-size="roomQuery.pageSize"
            :total="roomTotal" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
            @current-change="fetchRooms" @size-change="fetchRooms" />
        </div>

        <el-dialog v-model="roomDialogVisible" :title="roomDialogType === 'add' ? '新增直播间' : '编辑直播间'" width="600px">
          <el-form ref="roomFormRef" :model="roomFormData" label-width="100px">
            <el-form-item label="主播ID" prop="anchorId" :rules="[{ required: true, message: '请输入主播ID' }]">
              <el-input-number v-model="roomFormData.anchorId" :min="1" />
            </el-form-item>
            <el-form-item label="标题" prop="title" :rules="[{ required: true, message: '请输入标题' }]">
              <el-input v-model="roomFormData.title" placeholder="直播标题" />
            </el-form-item>
            <el-form-item label="分类" prop="type" :rules="[{ required: true }]">
              <el-select v-model="roomFormData.type">
                <el-option v-for="(label, val) in roomTypeMap" :key="val" :label="label" :value="Number(val)" />
              </el-select>
            </el-form-item>
            <el-form-item label="预告时间" prop="startTime" :rules="[{ required: true, message: '请选择预告时间' }]">
              <el-date-picker v-model="roomFormData.startTime" type="datetime" placeholder="选择预告时间" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
            <el-form-item label="封面" prop="cover">
              <ImageUpload v-model="roomFormData.cover" upload-type="cover" />
            </el-form-item>
            <el-form-item label="公告" prop="notice">
              <el-input v-model="roomFormData.notice" type="textarea" :rows="2" placeholder="直播公告" />
            </el-form-item>
            <el-form-item label="推流地址" prop="pushUrl">
              <el-input v-model="roomFormData.pushUrl" placeholder="RTMP推流地址" />
            </el-form-item>
            <el-form-item label="拉流地址" prop="pullUrl">
              <el-input v-model="roomFormData.pullUrl" placeholder="播放地址" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="roomDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleRoomSubmit">确定</el-button>
          </template>
        </el-dialog>

        <!-- 推流地址弹窗 -->
        <el-dialog v-model="streamUrlDialogVisible" title="推流/拉流地址" width="600px">
          <el-alert type="success" :closable="false" style="margin-bottom: 16px;">
            <template #title>
              <span>地址已生成！请将推流地址填入 OBS 开始直播。</span>
            </template>
          </el-alert>

          <el-form label-width="100px">
            <el-form-item label="推流地址">
              <el-input :model-value="streamUrls.pushUrl" readonly>
                <template #append>
                  <el-button @click="copyToClipboard(streamUrls.pushUrl)">复制</el-button>
                </template>
              </el-input>
              <div class="form-tip">OBS → 设置 → 推流 → 服务器</div>
            </el-form-item>

            <el-form-item label="拉流(FLV)">
              <el-input :model-value="streamUrls.pullUrlFlv" readonly>
                <template #append>
                  <el-button @click="copyToClipboard(streamUrls.pullUrlFlv)">复制</el-button>
                </template>
              </el-input>
              <div class="form-tip">Web 端播放地址（低延迟）</div>
            </el-form-item>

            <el-form-item label="拉流(HLS)">
              <el-input :model-value="streamUrls.pullUrlHls" readonly>
                <template #append>
                  <el-button @click="copyToClipboard(streamUrls.pullUrlHls)">复制</el-button>
                </template>
              </el-input>
              <div class="form-tip">移动端/小程序播放地址</div>
            </el-form-item>
          </el-form>

          <el-divider />

          <el-alert type="info" :closable="false">
            <template #title>
              <span>OBS 配置说明</span>
            </template>
            <template #default>
              <p style="margin: 8px 0 0;">1. 打开 OBS → 设置 → 推流</p>
              <p style="margin: 4px 0;">2. 服务选择"自定义"</p>
              <p style="margin: 4px 0;">3. 服务器填入上方推流地址中 <b>?</b> 之前的部分</p>
              <p style="margin: 4px 0;">4. 流密钥填入 <b>token=xxx</b> 部分</p>
            </template>
          </el-alert>

          <template #footer>
            <el-button type="primary" @click="streamUrlDialogVisible = false">确定</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- 直播商品管理 -->
      <el-tab-pane label="直播商品" name="products">
        <div class="tab-header">
          <el-input-number v-model="productRoomId" :min="1" placeholder="直播间ID" style="width:160px" @change="fetchProducts" />
          <el-button type="primary" :icon="Plus" @click="handleProductAdd" :disabled="!productRoomId">上架商品</el-button>
        </div>
        <el-table v-loading="productLoading" :data="productList" border v-if="productRoomId">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="productId" label="商品ID" width="100" />
          <el-table-column prop="skuId" label="SKU ID" width="100" />
          <el-table-column label="直播价" width="100"><template #default="{ row }">¥{{ row.livePrice }}</template></el-table-column>
          <el-table-column label="排序" width="70" prop="sortOrder" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status===2?'warning':row.status===1?'success':'info'">{{ {0:'已下架',1:'已上架',2:'讲解中'}[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status !== 2" type="warning" link @click="handleProductExplain(row.id)">标记讲解</el-button>
              <el-button v-if="row.status === 2" type="info" link @click="handleProductUnexplain(row.id)">取消讲解</el-button>
              <el-button type="danger" link @click="handleProductRemove(row.id)">下架</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="请输入直播间ID查看商品" />

        <el-dialog v-model="productDialogVisible" title="上架商品" width="500px">
          <el-form :model="productFormData" label-width="100px">
            <el-form-item label="商品ID"><el-input-number v-model="productFormData.productId" :min="1" /></el-form-item>
            <el-form-item label="SKU ID"><el-input-number v-model="productFormData.skuId" :min="1" /></el-form-item>
            <el-form-item label="直播价"><el-input-number v-model="productFormData.livePrice" :min="0" :precision="2" /></el-form-item>
            <el-form-item label="排序"><el-input-number v-model="productFormData.sortOrder" :min="0" /></el-form-item>
          </el-form>
          <template #footer><el-button @click="productDialogVisible = false">取消</el-button><el-button type="primary" @click="handleProductSubmit">确定</el-button></template>
        </el-dialog>
      </el-tab-pane>

      <!-- 礼物管理 -->
      <el-tab-pane label="礼物管理" name="gifts">
        <div class="tab-header">
          <el-button type="primary" :icon="Plus" @click="handleGiftAdd">新增礼物</el-button>
        </div>
        <el-table v-loading="giftLoading" :data="giftList" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="图标" width="80">
            <template #default="{ row }">
              <img v-if="row.icon" :src="row.icon" style="width:40px;height:40px;object-fit:contain" />
              <span v-else>🎁</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" width="120" />
          <el-table-column label="价格" width="100">
            <template #default="{ row }">
              <span style="color:#ffa726;font-weight:600">{{ row.price }} 币</span>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column prop="animationUrl" label="动画URL" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleGiftEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleGiftDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-dialog v-model="giftDialogVisible" :title="giftDialogType === 'add' ? '新增礼物' : '编辑礼物'" width="500px">
          <el-form ref="giftFormRef" :model="giftFormData" label-width="100px">
            <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入礼物名称' }]">
              <el-input v-model="giftFormData.name" placeholder="礼物名称" />
            </el-form-item>
            <el-form-item label="图标" prop="icon">
              <el-input v-model="giftFormData.icon" placeholder="图标URL" />
            </el-form-item>
            <el-form-item label="动画URL" prop="animationUrl">
              <el-input v-model="giftFormData.animationUrl" placeholder="Lottie动画URL" />
            </el-form-item>
            <el-form-item label="价格" prop="price" :rules="[{ required: true, message: '请输入价格' }]">
              <el-input-number v-model="giftFormData.price" :min="1" />
            </el-form-item>
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="giftFormData.sortOrder" :min="0" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="giftDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleGiftSubmit">确定</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped lang="scss">
.live-page {
  .tab-header {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
  }
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}
</style>
