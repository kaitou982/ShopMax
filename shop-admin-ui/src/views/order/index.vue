
<script setup lang="ts">
/**
 * 订单管理页面
 * @description 订单列表管理，支持查看详情、发货、取消等操作
 * @author shop
 * @since 2026-04-22
 */
defineOptions({ name: 'OrderManagement' })

import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import {
  getOrderList,
  getOrderDetail,
  shipOrder,
  cancelOrder,
  deleteOrder
} from '@/api/modules/order'
import type { Order } from '@/api/modules/order'
import { getRefundByOrderNo, approveRefund, rejectRefund, manualApproveRefund } from '@/api/modules/refund'
import type { RefundRecord } from '@/api/modules/refund'
import { createLogistics, logisticsCompanies } from '@/api/modules/logistics'

const userStore = useUserStore()

// 列表相关
const loading = ref(false)
const orderList = ref<Order[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  orderNo: ''
})

// 状态选项
const statusOptions = [
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '退款中', value: 5 },
  { label: '已退款', value: 6 }
]

// 弹窗相关
const detailDialogVisible = ref(false)
const currentOrder = ref<Order | null>(null)

// 退款审核相关
const refundDialogVisible = ref(false)
const refundLoading = ref(false)
const currentRefund = ref<RefundRecord | null>(null)
const rejectReason = ref('')

// 物流相关
const logisticsDialogVisible = ref(false)
const logisticsForm = reactive({
  orderId: 0,
  logisticsNo: '',
  company: 'shunfeng',
  senderName: '',
  senderPhone: '',
  senderAddress: '',
  senderLng: '',
  senderLat: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  receiverLng: '',
  receiverLat: ''
})

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOrderList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: query.status,
      orderNo: query.orderNo || undefined
    })
    orderList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  query.pageNum = 1
  fetchData()
}

// 重置搜索
const handleReset = () => {
  query.status = undefined
  query.orderNo = ''
  query.pageNum = 1
  fetchData()
}

// 查看详情
const handleDetail = async (row: Order) => {
  try {
    const res = await getOrderDetail(row.id)
    currentOrder.value = res
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

// 发货 - 打开物流信息录入对话框
const handleShip = (row: Order) => {
  logisticsForm.orderId = row.id
  logisticsForm.logisticsNo = ''
  logisticsForm.company = 'shunfeng'
  logisticsForm.senderName = 'ShopMax仓库'
  logisticsForm.senderPhone = '13800138000'
  logisticsForm.senderAddress = '广东省深圳市南山区科技园'
  logisticsForm.senderLng = ''
  logisticsForm.senderLat = ''
  logisticsForm.receiverName = row.receiverName || ''
  logisticsForm.receiverPhone = row.receiverPhone || ''
  logisticsForm.receiverAddress = row.receiverAddress || ''
  logisticsForm.receiverLng = ''
  logisticsForm.receiverLat = ''
  logisticsDialogVisible.value = true
}

// 确认发货（含物流信息）
const handleShipConfirm = async () => {
  if (!logisticsForm.logisticsNo) {
    ElMessage.warning('请输入物流单号')
    return
  }

  try {
    // 先创建物流
    await createLogistics({
      orderId: logisticsForm.orderId,
      logisticsNo: logisticsForm.logisticsNo,
      company: logisticsForm.company,
      senderName: logisticsForm.senderName,
      senderPhone: logisticsForm.senderPhone,
      senderAddress: logisticsForm.senderAddress,
      senderLongitude: logisticsForm.senderLng ? Number(logisticsForm.senderLng) : undefined,
      senderLatitude: logisticsForm.senderLat ? Number(logisticsForm.senderLat) : undefined,
      receiverName: logisticsForm.receiverName,
      receiverPhone: logisticsForm.receiverPhone,
      receiverAddress: logisticsForm.receiverAddress,
      receiverLongitude: logisticsForm.receiverLng ? Number(logisticsForm.receiverLng) : undefined,
      receiverLatitude: logisticsForm.receiverLat ? Number(logisticsForm.receiverLat) : undefined
    })

    // 再更新订单状态为已发货
    await shipOrder(logisticsForm.orderId)

    ElMessage.success('发货成功')
    logisticsDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('发货失败:', error)
  }
}

// 取消订单
const handleCancel = async (row: Order) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      type: 'warning',
      inputValidator: (val) => !!val || '请输入取消原因'
    })
    await cancelOrder(row.id, value)
    ElMessage.success('取消成功')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
    }
  }
}

// 删除订单
const handleDelete = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
      type: 'warning'
    })
    await deleteOrder(row.id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 打开退款审核弹窗
const handleRefundReview = async (row: Order) => {
  refundLoading.value = true
  refundDialogVisible.value = true
  currentRefund.value = null
  rejectReason.value = ''
  try {
    const refund = await getRefundByOrderNo(row.orderNo)
    currentRefund.value = refund || null
  } catch {
    ElMessage.error('获取退款记录失败')
  } finally {
    refundLoading.value = false
  }
}

// 批准退款
const handleApproveRefund = async () => {
  if (!currentRefund.value) return
  try {
    await ElMessageBox.confirm('确认批准退款？将调用支付网关执行退款。', '批准退款', { type: 'warning' })
    await approveRefund(currentRefund.value.refundNo)
    ElMessage.success('退款审核通过，已执行退款')
    refundDialogVisible.value = false
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批准退款失败')
    }
  }
}

// 拒绝退款
const handleRejectRefund = async () => {
  if (!currentRefund.value) return
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    await ElMessageBox.confirm('确认拒绝退款？订单将恢复为待发货状态。', '拒绝退款', { type: 'warning' })
    await rejectRefund(currentRefund.value.refundNo, rejectReason.value)
    ElMessage.success('已拒绝退款')
    refundDialogVisible.value = false
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('拒绝退款失败')
    }
  }
}

// 手动标记退款成功（用于旧订单支付网关不可用的情况）
const handleManualApproveRefund = async () => {
  if (!currentRefund.value) return
  try {
    await ElMessageBox.confirm(
      '确认手动标记退款成功？此操作用于旧订单支付网关交易记录不存在的情况。',
      '手动标记退款',
      { type: 'warning' }
    )
    await manualApproveRefund(currentRefund.value.refundNo, '管理员手动标记退款成功（旧订单支付网关不可用）')
    ElMessage.success('已手动标记退款成功')
    refundDialogVisible.value = false
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('手动标记退款失败')
    }
  }
}

// 获取退款状态文本
const getRefundStatusText = (status: number) => {
  const texts: Record<number, string> = { 0: '处理中', 1: '退款成功', 2: '退款失败' }
  return texts[status] || '未知'
}

// 获取支付方式文本
const getPayMethodText = (method: number) => {
  const texts: Record<number, string> = { 1: '支付宝', 2: '微信', 3: '余额' }
  return texts[method] || '未知'
}

// 页码变化
const handlePageChange = (page: number) => {
  query.pageNum = page
  fetchData()
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  query.pageSize = size
  query.pageNum = 1
  fetchData()
}

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'info',
    4: 'danger',
    5: 'warning',
    6: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '退款中',
    6: '已退款'
  }
  return texts[status] || '未知'
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 格式化金额
const formatPrice = (price: number) => {
  return '¥' + (price || 0).toFixed(2)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div v-if="userStore.isAdmin || userStore.isStore" class="order-page page-container">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="query.orderNo"
        placeholder="订单号"
        clearable
        @clear="handleReset"
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.status" placeholder="订单状态" clearable @clear="handleReset" style="width: 150px">
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>搜索
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="orderList" border>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="手机号" width="130" />
      <el-table-column label="订单金额" width="120">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold">{{ formatPrice(row.payAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payType" label="支付方式" width="100">
        <template #default="{ row }">
          <span v-if="row.payType === 1">支付宝</span>
          <span v-else-if="row.payType === 2">微信</span>
          <span v-else-if="row.payType === 3">余额</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.status === 1" type="success" link @click="handleShip(row)">发货</el-button>
          <el-button v-if="row.status === 0" type="warning" link @click="handleCancel(row)">取消</el-button>
          <el-button v-if="row.status === 5" type="warning" link @click="handleRefundReview(row)">审核退款</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentOrder.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="订单金额" :span="2">
          <span style="color: #f56c6c; font-size: 18px; font-weight: bold">
            {{ formatPrice(currentOrder.totalAmount) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="运费">{{ formatPrice(currentOrder.freightAmount) }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">{{ formatPrice(currentOrder.couponAmount) }}</el-descriptions-item>
        <el-descriptions-item label="应付金额" :span="2">
          <span style="color: #f56c6c; font-weight: bold">{{ formatPrice(currentOrder.payAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <span v-if="currentOrder.payType === 1">支付宝</span>
          <span v-else-if="currentOrder.payType === 2">微信</span>
          <span v-else-if="currentOrder.payType === 3">余额</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatDate(currentOrder.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatDate(currentOrder.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ formatDate(currentOrder.deliveryTime) }}</el-descriptions-item>
        <el-descriptions-item label="买家留言" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 退款审核弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="退款审核" width="550px">
      <div v-loading="refundLoading">
        <el-alert v-if="!currentRefund" title="未找到退款记录" type="info" :closable="false" show-icon />
        <el-descriptions v-else :column="2" border>
          <el-descriptions-item label="退款单号">{{ currentRefund.refundNo }}</el-descriptions-item>
          <el-descriptions-item label="退款状态">
            <el-tag :type="currentRefund.status === 0 ? 'warning' : currentRefund.status === 1 ? 'success' : 'danger'">
              {{ getRefundStatusText(currentRefund.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退款金额" :span="2">
            <span style="color: #f56c6c; font-weight: bold">{{ formatPrice(currentRefund.refundAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPayMethodText(currentRefund.payMethod) }}</el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.refundReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ formatDate(currentRefund.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentRefund && currentRefund.status === 0" style="margin-top: 20px">
          <el-divider />
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="2"
            placeholder="拒绝原因（拒绝时必填）"
          />
          <div style="margin-top: 15px; text-align: right">
            <el-button type="danger" @click="handleRejectRefund">拒绝退款</el-button>
            <el-button type="success" @click="handleApproveRefund">批准退款</el-button>
            <el-button type="warning" @click="handleManualApproveRefund">手动标记退款</el-button>
          </div>
          <el-alert
            title="手动标记退款：用于旧订单在支付宝/微信中的交易记录已不存在，无法通过网关退款的情况"
            type="info"
            :closable="false"
            show-icon
            style="margin-top: 10px"
          />
        </div>

        <div v-if="currentRefund && currentRefund.status === 2" style="margin-top: 20px">
          <el-divider />
          <el-alert
            title="退款已失败，可以手动标记退款成功（适用于旧订单支付网关不可用的情况）"
            type="warning"
            :closable="false"
            show-icon
          />
          <div style="margin-top: 15px; text-align: right">
            <el-button type="warning" @click="handleManualApproveRefund">手动标记退款</el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="refundDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 物流信息录入弹窗 -->
    <el-dialog v-model="logisticsDialogVisible" title="发货 - 填写物流信息" width="600px">
      <el-form :model="logisticsForm" label-width="100px">
        <el-form-item label="物流公司" required>
          <el-select v-model="logisticsForm.company" placeholder="请选择物流公司" style="width: 100%">
            <el-option
              v-for="item in logisticsCompanies"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" required>
          <el-input v-model="logisticsForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
        <el-divider content-position="left">发件人信息</el-divider>
        <el-form-item label="发件人">
          <el-input v-model="logisticsForm.senderName" placeholder="发件人姓名" />
        </el-form-item>
        <el-form-item label="发件人电话">
          <el-input v-model="logisticsForm.senderPhone" placeholder="发件人电话" />
        </el-form-item>
        <el-form-item label="发件人地址">
          <el-input v-model="logisticsForm.senderAddress" placeholder="发件人地址" />
        </el-form-item>
        <el-form-item label="发件坐标">
          <el-col :span="11">
            <el-input v-model="logisticsForm.senderLng" placeholder="经度（选填）" />
          </el-col>
          <el-col :span="2" style="text-align:center">,</el-col>
          <el-col :span="11">
            <el-input v-model="logisticsForm.senderLat" placeholder="纬度（选填）" />
          </el-col>
        </el-form-item>
        <el-divider content-position="left">收件人信息</el-divider>
        <el-form-item label="收件人">
          <el-input v-model="logisticsForm.receiverName" placeholder="收件人姓名" />
        </el-form-item>
        <el-form-item label="收件人电话">
          <el-input v-model="logisticsForm.receiverPhone" placeholder="收件人电话" />
        </el-form-item>
        <el-form-item label="收件人地址">
          <el-input v-model="logisticsForm.receiverAddress" placeholder="收件人地址" />
        </el-form-item>
        <el-form-item label="收件坐标">
          <el-col :span="11">
            <el-input v-model="logisticsForm.receiverLng" placeholder="经度（选填）" />
          </el-col>
          <el-col :span="2" style="text-align:center">,</el-col>
          <el-col :span="11">
            <el-input v-model="logisticsForm.receiverLat" placeholder="纬度（选填）" />
          </el-col>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="logisticsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleShipConfirm">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
// All shared styles (page-header, search-bar, pagination)
// are now provided globally in styles/index.scss
</style>
