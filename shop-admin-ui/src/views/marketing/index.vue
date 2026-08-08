<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import { nextTick } from 'vue'
import echarts from '@/utils/echarts'
import {
  getCouponList, createCoupon, updateCoupon, deleteCoupon,
  getPromotionList, createPromotion, updatePromotion, deletePromotion, enablePromotion, disablePromotion,
  getSeckillSessions, createSeckillSession, addSeckillProduct, getSeckillProducts, loadSeckillStock,
  getGroupBuyActivities, createGroupBuyActivity,
  getCouponRedemptionRecords, getCouponStats, grantCouponToUsers, getCouponTrend,
  type Coupon, type Promotion, type SeckillSession, type SeckillProduct, type GroupBuyActivity
} from '@/api/modules/marketing'

defineOptions({ name: 'MarketingManagement' })

const userStore = useUserStore()

const activeTab = ref<'coupon' | 'promotion' | 'seckill' | 'groupBuy'>('coupon')

// ============ 优惠券 ============
const couponLoading = ref(false)
const couponList = ref<Coupon[]>([])
const couponTotal = ref(0)
const couponQuery = reactive({ pageNum: 1, pageSize: 10 })

const couponDialogVisible = ref(false)
const couponDialogType = ref<'add' | 'edit'>('add')
const couponFormRef = ref<FormInstance>()
const couponFormData = reactive<Partial<Coupon>>({
  name: '', type: 1, minAmount: 0, discountAmount: 0, discountRate: undefined,
  totalCount: 100, perLimit: 1, validDays: 7, applicableType: 1, description: '', status: 1
})

const couponTypeMap: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '运费券', 4: '新人券' }
const couponStatusMap: Record<number, string> = { 0: '禁用', 1: '启用' }

const fetchCoupons = async () => {
  try {
    couponLoading.value = true
    const res = await getCouponList({ pageNum: couponQuery.pageNum, pageSize: couponQuery.pageSize })
    couponList.value = res.records
    couponTotal.value = res.total
  } finally {
    couponLoading.value = false
  }
}

const handleCouponAdd = () => {
  couponDialogType.value = 'add'
  Object.assign(couponFormData, { name: '', type: 1, minAmount: 0, discountAmount: 0, discountRate: undefined, totalCount: 100, perLimit: 1, validDays: 7, applicableType: 1, applicableIds: '', integralCost: 0, description: '', status: 1 })
  couponFormRef.value?.resetFields()
  couponDialogVisible.value = true
}

const handleCouponEdit = (row: Coupon) => {
  couponDialogType.value = 'edit'
  Object.assign(couponFormData, { ...row })
  if (row.applicableIds) {
    try { couponFormData.applicableIds = JSON.parse(row.applicableIds).join(',') } catch { couponFormData.applicableIds = row.applicableIds }
  }
  couponDialogVisible.value = true
}

const toJsonIds = (val: string | undefined) => {
  if (!val || !val.trim()) return '[]'
  return JSON.stringify(val.split(',').map(v => Number(v.trim())).filter(v => !isNaN(v)))
}

const handleCouponSubmit = async () => {
  await couponFormRef.value?.validate()
  const submitData = { ...couponFormData, applicableIds: toJsonIds(couponFormData.applicableIds) }
  try {
    if (couponDialogType.value === 'add') {
      await createCoupon(submitData)
      ElMessage.success('创建成功')
    } else {
      await updateCoupon(submitData.id!, submitData)
      ElMessage.success('更新成功')
    }
    couponDialogVisible.value = false
    fetchCoupons()
  } catch { /* form validation or API error handled by interceptor */ }
}

const handleCouponDelete = async (row: Coupon) => {
  await ElMessageBox.confirm('确认删除该优惠券？', '提示', { type: 'warning' })
  await deleteCoupon(row.id)
  ElMessage.success('删除成功')
  fetchCoupons()
}

// Redemption records dialog
const recordDialogVisible = ref(false)
const recordLoading = ref(false)
const recordList = ref<Record<string, unknown>[]>([])
const recordTotal = ref(0)
const recordQuery = reactive({ pageNum: 1, pageSize: 10 })
const recordCouponId = ref(0)
const recordStats = ref<Record<string, unknown>>({})

let trendChart: echarts.ECharts | null = null

const handleViewRecords = async (row: Coupon) => {
  recordCouponId.value = row.id
  recordQuery.pageNum = 1
  recordDialogVisible.value = true
  try { recordStats.value = await getCouponStats(row.id) || {} } catch { /* noop */ }
  fetchRecords()
  // Render trend chart
  try {
    const trend = await getCouponTrend(row.id)
    await nextTick()
    const el = document.getElementById('coupon-trend-chart')
    if (!el) return
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(el)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 16, top: 16, bottom: 24 },
      xAxis: { type: 'category', data: trend.map(t => t.date.slice(5)), axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f0f0f5' } } },
      series: [{ data: trend.map(t => t.count), type: 'line', smooth: true, lineStyle: { color: '#FF5000', width: 2 }, itemStyle: { color: '#FF5000' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(255,80,0,0.15)' }, { offset: 1, color: 'rgba(255,80,0,0)' }]) } }]
    })
  } catch { /* noop */ }
}

const fetchRecords = async () => {
  recordLoading.value = true
  try {
    const res = await getCouponRedemptionRecords(recordCouponId.value, { pageNum: recordQuery.pageNum, pageSize: recordQuery.pageSize })
    recordList.value = res.records
    recordTotal.value = res.total
  } finally { recordLoading.value = false }
}

// Grant dialog
const grantDialogVisible = ref(false)
const grantCouponId = ref(0)
const grantCouponName = ref('')
const grantUserIds = ref('')
const grantLoading = ref(false)
const grantResult = ref<{ success: number; failed: number; total: number } | null>(null)

const handleGrantOpen = (row: Coupon) => {
  grantCouponId.value = row.id
  grantCouponName.value = row.name
  grantUserIds.value = ''
  grantResult.value = null
  grantDialogVisible.value = true
}

const handleGrantSubmit = async () => {
  const ids = grantUserIds.value.split(/[,\s]+/).map(v => Number(v.trim())).filter(v => !isNaN(v))
  if (!ids.length) { ElMessage.warning('请输入用户ID'); return }
  grantLoading.value = true
  try {
    grantResult.value = await grantCouponToUsers(grantCouponId.value, ids)
    ElMessage.success(`发放完成: 成功 ${grantResult.value.success}, 失败 ${grantResult.value.failed}`)
  } catch { /* handled */ } finally { grantLoading.value = false }
}

// ============ 促销活动 ============
const promoLoading = ref(false)
const promoList = ref<Promotion[]>([])
const promoTotal = ref(0)
const promoQuery = reactive<{ pageNum: number; pageSize: number; status?: number }>({ pageNum: 1, pageSize: 10 })

const promoDialogVisible = ref(false)
const promoDialogType = ref<'add' | 'edit'>('add')
const promoFormRef = ref<FormInstance>()
const promoFormData = reactive<Partial<Promotion>>({
  name: '', type: 1, minAmount: 0, discountAmount: 0, discountRate: undefined,
  startTime: '', endTime: '', applicableType: 1, description: ''
})

const promoTypeMap: Record<number, string> = { 1: '满减', 2: '满折' }
const promoStatusMap: Record<number, string> = { 0: '未开始', 1: '进行中', 2: '已结束', 3: '已禁用' }

const fetchPromotions = async () => {
  try {
    promoLoading.value = true
    const res = await getPromotionList({ ...promoQuery })
    promoList.value = res.records
    promoTotal.value = res.total
  } finally {
    promoLoading.value = false
  }
}

const handlePromoAdd = () => {
  promoDialogType.value = 'add'
  Object.assign(promoFormData, { name: '', type: 1, minAmount: 0, discountAmount: 0, discountRate: undefined, startTime: '', endTime: '', applicableType: 1, description: '' })
  promoFormRef.value?.resetFields()
  promoDialogVisible.value = true
}

const handlePromoEdit = (row: Promotion) => {
  promoDialogType.value = 'edit'
  Object.assign(promoFormData, { ...row })
  promoDialogVisible.value = true
}

const handlePromoSubmit = async () => {
  await promoFormRef.value?.validate()
  try {
    if (promoDialogType.value === 'add') {
      await createPromotion(promoFormData)
      ElMessage.success('创建成功')
    } else {
      await updatePromotion(promoFormData.id!, promoFormData)
      ElMessage.success('更新成功')
    }
    promoDialogVisible.value = false
    fetchPromotions()
  } catch { /* handled */ }
}

const handlePromoDelete = async (row: Promotion) => {
  await ElMessageBox.confirm('确认删除该促销活动？', '提示', { type: 'warning' })
  await deletePromotion(row.id)
  ElMessage.success('删除成功')
  fetchPromotions()
}

const handlePromoEnable = async (row: Promotion) => {
  await enablePromotion(row.id)
  ElMessage.success('已启用')
  fetchPromotions()
}

const handlePromoDisable = async (row: Promotion) => {
  await disablePromotion(row.id)
  ElMessage.success('已停用')
  fetchPromotions()
}

// ============ 秒杀 ============
const seckillLoading = ref(false)
const seckillSessions = ref<SeckillSession[]>([])
const seckillTotal = ref(0)
const seckillQuery = reactive({ pageNum: 1, pageSize: 10 })
const seckillDialogVisible = ref(false)
const seckillFormData = reactive<Partial<SeckillSession>>({ name: '', startTime: '', endTime: '' })

const seckillProductDialogVisible = ref(false)
const seckillProductFormData = reactive<Partial<SeckillProduct>>({ productId: 0, skuId: 0, seckillPrice: 0, seckillStock: 100, limitPerUser: 1, sortOrder: 0, status: 1 })
let seckillProductSessionId = ref(0)
const seckillProducts = ref<SeckillProduct[]>([])
const seckillProductsVisible = ref(false)

const fetchSeckillSessions = async () => {
  try { seckillLoading.value = true; const res = await getSeckillSessions({ pageNum: seckillQuery.pageNum, pageSize: seckillQuery.pageSize }); seckillSessions.value = res.records; seckillTotal.value = res.total } finally { seckillLoading.value = false }
}
const handleSeckillAdd = () => { Object.assign(seckillFormData, { name: '', startTime: '', endTime: '' }); seckillDialogVisible.value = true }
const handleSeckillSubmit = async () => {
  try { await createSeckillSession(seckillFormData); ElMessage.success('创建成功'); seckillDialogVisible.value = false; fetchSeckillSessions() } catch { /* handled */ }
}
const handleAddProduct = (sessionId: number) => { seckillProductSessionId.value = sessionId; Object.assign(seckillProductFormData, { productId: 0, skuId: 0, seckillPrice: 0, seckillStock: 100, limitPerUser: 1, sortOrder: 0, status: 1 }); seckillProductDialogVisible.value = true }
const handleSeckillProductSubmit = async () => {
  try { await addSeckillProduct(seckillProductSessionId.value, seckillProductFormData); ElMessage.success('添加成功'); seckillProductDialogVisible.value = false } catch { /* handled */ }
}
const handleViewProducts = async (sessionId: number) => { seckillProductSessionId.value = sessionId; seckillProducts.value = await getSeckillProducts(sessionId); seckillProductsVisible.value = true }
const handleLoadStock = async (sessionId: number) => { await loadSeckillStock(sessionId); ElMessage.success('库存已加载到Redis') }

// ============ 拼团 ============
const groupBuyLoading = ref(false)
const groupBuyActivities = ref<GroupBuyActivity[]>([])
const groupBuyTotal = ref(0)
const groupBuyQuery = reactive({ pageNum: 1, pageSize: 10 })
const groupBuyDialogVisible = ref(false)
const groupBuyFormData = reactive<Partial<GroupBuyActivity>>({ name: '', productId: 0, skuId: 0, groupPrice: 0, requiredCount: 2, expireHours: 24, stock: 100, startTime: '', endTime: '', status: 1 })

const fetchGroupBuyActivities = async () => {
  try { groupBuyLoading.value = true; const res = await getGroupBuyActivities({ pageNum: groupBuyQuery.pageNum, pageSize: groupBuyQuery.pageSize }); groupBuyActivities.value = res.records; groupBuyTotal.value = res.total } finally { groupBuyLoading.value = false }
}
const handleGroupBuyAdd = () => { Object.assign(groupBuyFormData, { name: '', productId: 0, skuId: 0, groupPrice: 0, requiredCount: 2, expireHours: 24, stock: 100, startTime: '', endTime: '', status: 1 }); groupBuyDialogVisible.value = true }
const handleGroupBuySubmit = async () => {
  try { await createGroupBuyActivity(groupBuyFormData); ElMessage.success('创建成功'); groupBuyDialogVisible.value = false; fetchGroupBuyActivities() } catch { /* handled */ }
}

const handleTabChange = () => {
  if (activeTab.value === 'coupon') fetchCoupons()
  else if (activeTab.value === 'promotion') fetchPromotions()
  else if (activeTab.value === 'seckill') fetchSeckillSessions()
  else fetchGroupBuyActivities()
}

onMounted(() => fetchCoupons())
</script>

<template>
  <div v-if="userStore.isAdmin" class="marketing-page page-container">
    <div class="page-header">
      <h2>营销活动</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 优惠券管理 -->
      <el-tab-pane label="优惠券管理" name="coupon">
        <div class="tab-header">
          <el-button type="primary" :icon="Plus" @click="handleCouponAdd">新增优惠券</el-button>
        </div>
        <el-table v-loading="couponLoading" :data="couponList" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="名称" min-width="150" />
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag size="small">{{ couponTypeMap[row.type] || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="门槛" width="100">
            <template #default="{ row }">满{{ row.minAmount }}元</template>
          </el-table-column>
          <el-table-column label="优惠" width="100">
            <template #default="{ row }">
              <span v-if="row.type === 2">{{ (Number(row.discountRate) * 10).toFixed(1) }}折</span>
              <span v-else>减{{ row.discountAmount }}元</span>
            </template>
          </el-table-column>
          <el-table-column label="发放" width="130">
            <template #default="{ row }">{{ row.receivedCount }} / {{ row.totalCount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ couponStatusMap[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="290" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleCouponEdit(row)">编辑</el-button>
              <el-button type="warning" link @click="handleGrantOpen(row)">发券</el-button>
              <el-button type="success" link @click="handleViewRecords(row)">记录</el-button>
              <el-button type="danger" link @click="handleCouponDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="couponQuery.pageNum" v-model:page-size="couponQuery.pageSize"
            :total="couponTotal" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
            @current-change="fetchCoupons" @size-change="fetchCoupons" />
        </div>

        <el-dialog v-model="couponDialogVisible" :title="couponDialogType === 'add' ? '新增优惠券' : '编辑优惠券'" width="600px">
          <el-form ref="couponFormRef" :model="couponFormData" label-width="100px">
            <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称' }]">
              <el-input v-model="couponFormData.name" placeholder="如：新人满100减20" />
            </el-form-item>
            <el-form-item label="类型" prop="type" :rules="[{ required: true }]">
              <el-select v-model="couponFormData.type">
                <el-option v-for="(label, val) in couponTypeMap" :key="val" :label="label" :value="Number(val)" />
              </el-select>
            </el-form-item>
            <el-form-item label="门槛金额" prop="minAmount">
              <el-input-number v-model="couponFormData.minAmount" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item v-if="couponFormData.type !== 2" label="减免金额" prop="discountAmount">
              <el-input-number v-model="couponFormData.discountAmount" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item v-if="couponFormData.type === 2" label="折扣率" prop="discountRate">
              <el-input-number v-model="couponFormData.discountRate" :min="0.01" :max="1" :step="0.05" :precision="2" />
              <span style="margin-left:8px;color:#999">如 0.85 = 85折</span>
            </el-form-item>
            <el-form-item label="发放总量" prop="totalCount">
              <el-input-number v-model="couponFormData.totalCount" :min="1" />
            </el-form-item>
            <el-form-item label="每人限领" prop="perLimit">
              <el-input-number v-model="couponFormData.perLimit" :min="1" />
            </el-form-item>
            <el-form-item label="有效天数" prop="validDays">
              <el-input-number v-model="couponFormData.validDays" :min="1" />
            </el-form-item>
            <el-form-item label="适用类型" prop="applicableType">
              <el-select v-model="couponFormData.applicableType">
                <el-option :value="1" label="全部商品" />
                <el-option :value="2" label="指定分类" />
                <el-option :value="3" label="指定商品" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="couponFormData.applicableType !== 1" label="适用ID" prop="applicableIds">
              <el-input v-model="couponFormData.applicableIds" :placeholder="couponFormData.applicableType === 2 ? '分类ID，逗号分隔，如 1,2,3' : '商品ID，逗号分隔，如 101,102,103'" />
            </el-form-item>
            <el-form-item label="积分兑换">
              <el-input-number v-model="couponFormData.integralCost" :min="0" placeholder="0表示不支持积分兑换" />
            </el-form-item>
            <el-form-item label="可叠加">
              <el-switch v-model="couponFormData.stackable" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="使用说明" prop="description">
              <el-input v-model="couponFormData.description" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-switch v-model="couponFormData.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="couponDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleCouponSubmit">确定</el-button>
          </template>
        </el-dialog>

        <!-- 领取记录弹窗 -->
        <el-dialog v-model="recordDialogVisible" title="领取记录" width="800px">
          <div v-if="recordStats.name" class="record-stats">
            <span><strong>{{ recordStats.name }}</strong></span>
            <span>领取率: {{ recordStats.claimRate }}%</span>
            <span>使用率: {{ recordStats.useRate }}%</span>
            <span>已发 {{ recordStats.received_count }}/{{ recordStats.total_count }}</span>
            <span>已用 {{ recordStats.used_count }}</span>
          </div>
          <div id="coupon-trend-chart" style="width:100%;height:240px;margin-bottom:12px" />
          <el-table v-loading="recordLoading" :data="recordList" border>
            <el-table-column label="用户" min-width="120">
              <template #default="{ row }">{{ row.userNickname || '用户' + row.user_id }}</template>
            </el-table-column>
            <el-table-column label="手机号" width="130">
              <template #default="{ row }">{{ row.userPhone || '-' }}</template>
            </el-table-column>
            <el-table-column label="领取时间" width="170">
              <template #default="{ row }">{{ row.receive_time?.toString()?.slice(0, 19)?.replace('T', ' ') || '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'" size="small">
                  {{ ({0:'未使用',1:'已使用',2:'已过期'})[Number(row.status)] || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="使用时间" width="170">
              <template #default="{ row }">{{ row.use_time?.toString()?.slice(0, 19)?.replace('T', ' ') || '-' }}</template>
            </el-table-column>
            <el-table-column label="订单号" min-width="160">
              <template #default="{ row }">{{ row.order_no || '-' }}</template>
            </el-table-column>
          </el-table>
          <div class="pagination" style="margin-top:16px">
            <el-pagination v-model:current-page="recordQuery.pageNum" v-model:page-size="recordQuery.pageSize"
              :total="recordTotal" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" small
              @change="fetchRecords" />
          </div>
        </el-dialog>

        <!-- 定向发券弹窗 -->
        <el-dialog v-model="grantDialogVisible" title="定向发券" width="500px">
          <p style="margin-bottom:16px">为 <strong>{{ grantCouponName }}</strong> 发放给指定用户</p>
          <el-input v-model="grantUserIds" type="textarea" :rows="4" placeholder="输入用户ID，用逗号或空格分隔，如：1,2,3,4,5" />
          <div v-if="grantResult" style="margin-top:12px;padding:10px;background:#f5f5f7;border-radius:8px;font-size:13px">
            发放完成：总共 {{ grantResult.total }} 人，成功 <strong style="color:#00B578">{{ grantResult.success }}</strong>，失败 <strong style="color:#FF3B3B">{{ grantResult.failed }}</strong>
          </div>
          <template #footer>
            <el-button @click="grantDialogVisible = false">关闭</el-button>
            <el-button type="primary" :loading="grantLoading" @click="handleGrantSubmit">确认发券</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- 促销活动管理 -->
      <el-tab-pane label="促销活动管理" name="promotion">
        <div class="tab-header">
          <el-button type="primary" :icon="Plus" @click="handlePromoAdd">新增促销活动</el-button>
        </div>
        <el-table v-loading="promoLoading" :data="promoList" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="活动名称" min-width="160" />
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag size="small">{{ promoTypeMap[row.type] || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="门槛" width="100">
            <template #default="{ row }">满{{ row.minAmount }}元</template>
          </el-table-column>
          <el-table-column label="优惠" width="100">
            <template #default="{ row }">
              <span v-if="row.type === 2">{{ (Number(row.discountRate) * 10).toFixed(1) }}折</span>
              <span v-else>减{{ row.discountAmount }}元</span>
            </template>
          </el-table-column>
          <el-table-column label="开始时间" width="160">
            <template #default="{ row }">{{ row.startTime }}</template>
          </el-table-column>
          <el-table-column label="结束时间" width="160">
            <template #default="{ row }">{{ row.endTime }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : ''" size="small">{{ promoStatusMap[row.status] || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handlePromoEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 3" type="success" link @click="handlePromoEnable(row)">启用</el-button>
              <el-button v-if="row.status === 1" type="warning" link @click="handlePromoDisable(row)">停用</el-button>
              <el-button type="danger" link @click="handlePromoDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="promoQuery.pageNum" v-model:page-size="promoQuery.pageSize"
            :total="promoTotal" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
            @current-change="fetchPromotions" @size-change="fetchPromotions" />
        </div>

        <el-dialog v-model="promoDialogVisible" :title="promoDialogType === 'add' ? '新增促销活动' : '编辑促销活动'" width="600px">
          <el-form ref="promoFormRef" :model="promoFormData" label-width="100px">
            <el-form-item label="活动名称" prop="name" :rules="[{ required: true, message: '请输入活动名称' }]">
              <el-input v-model="promoFormData.name" placeholder="如：五一满减活动" />
            </el-form-item>
            <el-form-item label="类型" prop="type" :rules="[{ required: true }]">
              <el-select v-model="promoFormData.type">
                <el-option :value="1" label="满减" />
                <el-option :value="2" label="满折" />
              </el-select>
            </el-form-item>
            <el-form-item label="门槛金额" prop="minAmount" :rules="[{ required: true }]">
              <el-input-number v-model="promoFormData.minAmount" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item v-if="promoFormData.type === 1" label="减免金额" prop="discountAmount">
              <el-input-number v-model="promoFormData.discountAmount" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item v-if="promoFormData.type === 2" label="折扣率" prop="discountRate">
              <el-input-number v-model="promoFormData.discountRate" :min="0.01" :max="1" :step="0.05" :precision="2" />
            </el-form-item>
            <el-form-item label="开始时间" prop="startTime" :rules="[{ required: true, message: '请选择开始时间' }]">
              <el-date-picker v-model="promoFormData.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
            <el-form-item label="结束时间" prop="endTime" :rules="[{ required: true, message: '请选择结束时间' }]">
              <el-date-picker v-model="promoFormData.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
            <el-form-item label="适用类型" prop="applicableType">
              <el-select v-model="promoFormData.applicableType">
                <el-option :value="1" label="全部商品" />
                <el-option :value="2" label="指定分类" />
                <el-option :value="3" label="指定商品" />
              </el-select>
            </el-form-item>
            <el-form-item label="活动描述" prop="description">
              <el-input v-model="promoFormData.description" type="textarea" :rows="2" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="promoDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handlePromoSubmit">确定</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- 秒杀管理 -->
      <el-tab-pane label="秒杀管理" name="seckill">
        <div class="tab-header">
          <el-button type="primary" :icon="Plus" @click="handleSeckillAdd">新增秒杀场次</el-button>
        </div>
        <el-table v-loading="seckillLoading" :data="seckillSessions" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="场次名称" width="140" />
          <el-table-column label="开始时间" width="160"><template #default="{ row }">{{ row.startTime }}</template></el-table-column>
          <el-table-column label="结束时间" width="160"><template #default="{ row }">{{ row.endTime }}</template></el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : ''" size="small">{{ {0:'未开始',1:'进行中',2:'已结束'}[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleAddProduct(row.id)">添加商品</el-button>
              <el-button type="success" link @click="handleViewProducts(row.id)">查看商品</el-button>
              <el-button v-if="row.status === 1" type="warning" link @click="handleLoadStock(row.id)">加载库存</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="seckillQuery.pageNum" v-model:page-size="seckillQuery.pageSize" :total="seckillTotal" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @current-change="fetchSeckillSessions" @size-change="fetchSeckillSessions" />
        </div>

        <el-dialog v-model="seckillDialogVisible" title="新增秒杀场次" width="500px">
          <el-form :model="seckillFormData" label-width="100px">
            <el-form-item label="场次名称" prop="name" :rules="[{ required: true }]"><el-input v-model="seckillFormData.name" placeholder="如：10点场" /></el-form-item>
            <el-form-item label="开始时间"><el-date-picker v-model="seckillFormData.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
            <el-form-item label="结束时间"><el-date-picker v-model="seckillFormData.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
          </el-form>
          <template #footer><el-button @click="seckillDialogVisible = false">取消</el-button><el-button type="primary" @click="handleSeckillSubmit">确定</el-button></template>
        </el-dialog>

        <el-dialog v-model="seckillProductDialogVisible" title="添加秒杀商品" width="500px">
          <el-form :model="seckillProductFormData" label-width="100px">
            <el-form-item label="商品ID"><el-input-number v-model="seckillProductFormData.productId" :min="1" /></el-form-item>
            <el-form-item label="SKU ID"><el-input-number v-model="seckillProductFormData.skuId" :min="1" /></el-form-item>
            <el-form-item label="秒杀价"><el-input-number v-model="seckillProductFormData.seckillPrice" :min="0" :precision="2" /></el-form-item>
            <el-form-item label="秒杀库存"><el-input-number v-model="seckillProductFormData.seckillStock" :min="1" /></el-form-item>
            <el-form-item label="每人限购"><el-input-number v-model="seckillProductFormData.limitPerUser" :min="1" /></el-form-item>
          </el-form>
          <template #footer><el-button @click="seckillProductDialogVisible = false">取消</el-button><el-button type="primary" @click="handleSeckillProductSubmit">确定</el-button></template>
        </el-dialog>

        <el-dialog v-model="seckillProductsVisible" title="秒杀商品列表" width="700px">
          <el-table :data="seckillProducts" border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="productId" label="商品ID" width="80" />
            <el-table-column label="秒杀价" width="100"><template #default="{ row }">¥{{ row.seckillPrice }}</template></el-table-column>
            <el-table-column label="库存" width="80"><template #default="{ row }">{{ row.seckillStock }}</template></el-table-column>
            <el-table-column label="限购" width="60" prop="limitPerUser" />
            <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag size="small" :type="row.status===1?'success':'info'">{{ row.status===1?'启用':'禁用' }}</el-tag></template></el-table-column>
          </el-table>
          <template #footer><el-button @click="seckillProductsVisible = false">关闭</el-button></template>
        </el-dialog>
      </el-tab-pane>

      <!-- 拼团管理 -->
      <el-tab-pane label="拼团管理" name="groupBuy">
        <div class="tab-header">
          <el-button type="primary" :icon="Plus" @click="handleGroupBuyAdd">新增拼团活动</el-button>
        </div>
        <el-table v-loading="groupBuyLoading" :data="groupBuyActivities" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="活动名称" min-width="160" />
          <el-table-column label="拼团价" width="100"><template #default="{ row }">¥{{ row.groupPrice }}</template></el-table-column>
          <el-table-column label="成团人数" width="90" prop="requiredCount" />
          <el-table-column label="有效时长" width="90"><template #default="{ row }">{{ row.expireHours }}小时</template></el-table-column>
          <el-table-column label="库存" width="70" prop="stock" />
          <el-table-column label="时间" width="300">
            <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="groupBuyQuery.pageNum" v-model:page-size="groupBuyQuery.pageSize" :total="groupBuyTotal" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @current-change="fetchGroupBuyActivities" @size-change="fetchGroupBuyActivities" />
        </div>

        <el-dialog v-model="groupBuyDialogVisible" title="新增拼团活动" width="600px">
          <el-form :model="groupBuyFormData" label-width="100px">
            <el-form-item label="活动名称" prop="name" :rules="[{ required: true }]"><el-input v-model="groupBuyFormData.name" /></el-form-item>
            <el-form-item label="商品ID"><el-input-number v-model="groupBuyFormData.productId" :min="1" /></el-form-item>
            <el-form-item label="SKU ID"><el-input-number v-model="groupBuyFormData.skuId" :min="1" /></el-form-item>
            <el-form-item label="拼团价格"><el-input-number v-model="groupBuyFormData.groupPrice" :min="0" :precision="2" /></el-form-item>
            <el-form-item label="成团人数"><el-input-number v-model="groupBuyFormData.requiredCount" :min="2" /></el-form-item>
            <el-form-item label="有效时长(小时)"><el-input-number v-model="groupBuyFormData.expireHours" :min="1" /></el-form-item>
            <el-form-item label="库存"><el-input-number v-model="groupBuyFormData.stock" :min="1" /></el-form-item>
            <el-form-item label="开始时间"><el-date-picker v-model="groupBuyFormData.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
            <el-form-item label="结束时间"><el-date-picker v-model="groupBuyFormData.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
          </el-form>
          <template #footer><el-button @click="groupBuyDialogVisible = false">取消</el-button><el-button type="primary" @click="handleGroupBuySubmit">确定</el-button></template>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped lang="scss">
.marketing-page {
  .tab-header {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
  }
  .record-stats {
    display: flex; gap: 24px; flex-wrap: wrap;
    padding: 12px 16px; background: #f5f5f7; border-radius: 8px;
    margin-bottom: 16px; font-size: 13px; color: #666;
  }
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
