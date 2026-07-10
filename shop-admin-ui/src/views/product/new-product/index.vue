<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import {
  getNewProductList, batchMarkNew, batchUnmarkNew, updateNewProductSettings, getNewProductStats,
  getNewProductBanners, createNewProductBanner, updateNewProductBanner, deleteNewProductBanner,
  getProductList, getCategoryTree,
  type Product, type NewProductBanner, type Category
} from '@/api/modules/product'

defineOptions({ name: 'NewProductManagement' })

const userStore = useUserStore()

const activeTab = ref<'product' | 'banner' | 'overview'>('product')

// ============ 统计 ============
const stats = reactive({ total: 0, active: 0, expiring: 0, todayNew: 0 })

const fetchStats = async () => {
  try {
    const res = await getNewProductStats()
    Object.assign(stats, res)
  } catch { /* ignore */ }
}

// ============ 新品商品 ============
const productLoading = ref(false)
const productList = ref<any[]>([])
const productTotal = ref(0)
const productQuery = reactive({ pageNum: 1, pageSize: 10, categoryId: undefined as number | undefined })
const selectedIds = ref<number[]>([])

const categoryList = ref<Category[]>([])
const fetchCategories = async () => {
  try {
    categoryList.value = await getCategoryTree()
  } catch { /* ignore */ }
}

const fetchProducts = async () => {
  try {
    productLoading.value = true
    const res = await getNewProductList({
      pageNum: productQuery.pageNum,
      pageSize: productQuery.pageSize,
      categoryId: productQuery.categoryId
    })
    productList.value = res.records
    productTotal.value = res.total
  } finally {
    productLoading.value = false
  }
}

const handleProductSelect = (selection: any[]) => {
  selectedIds.value = selection.map((s: any) => s.id)
}

const handleBatchUnmark = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择商品')
    return
  }
  await ElMessageBox.confirm(`确定取消 ${selectedIds.value.length} 个商品的新品标记？`, '提示', { type: 'warning' })
  await batchUnmarkNew(selectedIds.value)
  ElMessage.success('操作成功')
  selectedIds.value = []
  fetchProducts()
  fetchStats()
}

// 编辑新品设置弹窗
const settingsDialogVisible = ref(false)
const settingsFormRef = ref<FormInstance>()
const settingsFormData = reactive({ id: 0, sort: 0, startTime: '', endTime: '' })

const handleEditSettings = (row: any) => {
  settingsFormData.id = row.id
  settingsFormData.sort = row.new_product_sort || 0
  settingsFormData.startTime = row.new_product_start_time || ''
  settingsFormData.endTime = row.new_product_end_time || ''
  settingsDialogVisible.value = true
}

const handleSettingsSubmit = async () => {
  await updateNewProductSettings(settingsFormData.id, {
    sort: settingsFormData.sort,
    startTime: settingsFormData.startTime || undefined,
    endTime: settingsFormData.endTime || undefined
  })
  ElMessage.success('更新成功')
  settingsDialogVisible.value = false
  fetchProducts()
}

// 添加商品弹窗
const addDialogVisible = ref(false)
const addProductLoading = ref(false)
const addProductList = ref<Product[]>([])
const addProductTotal = ref(0)
const addProductQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const addSelectedIds = ref<number[]>([])

const fetchAddProducts = async () => {
  try {
    addProductLoading.value = true
    const res = await getProductList({
      pageNum: addProductQuery.pageNum,
      pageSize: addProductQuery.pageSize,
      keyword: addProductQuery.keyword || undefined,
      status: 1
    })
    // 过滤掉已经是新品的
    const currentIds = new Set(productList.value.map((p: any) => p.id))
    addProductList.value = res.records.filter(p => !currentIds.has(p.id))
    addProductTotal.value = res.total
  } finally {
    addProductLoading.value = false
  }
}

const handleAddDialogOpen = () => {
  addSelectedIds.value = []
  addProductQuery.keyword = ''
  addProductQuery.pageNum = 1
  fetchAddProducts()
  addDialogVisible.value = true
}

const handleAddSelect = (selection: Product[]) => {
  addSelectedIds.value = selection.map(s => s.id)
}

const handleBatchMark = async () => {
  if (addSelectedIds.value.length === 0) {
    ElMessage.warning('请先选择商品')
    return
  }
  await batchMarkNew(addSelectedIds.value)
  ElMessage.success(`已添加 ${addSelectedIds.value.length} 个新品`)
  addDialogVisible.value = false
  fetchProducts()
  fetchStats()
}

// ============ Banner 管理 ============
const bannerLoading = ref(false)
const bannerList = ref<NewProductBanner[]>([])
const bannerTotal = ref(0)
const bannerQuery = reactive({ pageNum: 1, pageSize: 10 })

const bannerDialogVisible = ref(false)
const bannerDialogType = ref<'add' | 'edit'>('add')
const bannerFormRef = ref<FormInstance>()
const bannerFormData = reactive<Partial<NewProductBanner>>({
  title: '', imageUrl: '', productId: null, linkUrl: '', sort: 0, status: 1, startTime: null, endTime: null
})

const fetchBanners = async () => {
  try {
    bannerLoading.value = true
    const res = await getNewProductBanners({ pageNum: bannerQuery.pageNum, pageSize: bannerQuery.pageSize })
    bannerList.value = res.records
    bannerTotal.value = res.total
  } finally {
    bannerLoading.value = false
  }
}

const handleBannerAdd = () => {
  bannerDialogType.value = 'add'
  Object.assign(bannerFormData, { title: '', imageUrl: '', productId: null, linkUrl: '', sort: 0, status: 1, startTime: null, endTime: null })
  bannerFormRef.value?.resetFields()
  bannerDialogVisible.value = true
}

const handleBannerEdit = (row: NewProductBanner) => {
  bannerDialogType.value = 'edit'
  Object.assign(bannerFormData, { ...row })
  bannerDialogVisible.value = true
}

const handleBannerSubmit = async () => {
  await bannerFormRef.value?.validate()
  if (bannerDialogType.value === 'add') {
    await createNewProductBanner(bannerFormData)
    ElMessage.success('创建成功')
  } else {
    await updateNewProductBanner(bannerFormData.id!, bannerFormData)
    ElMessage.success('更新成功')
  }
  bannerDialogVisible.value = false
  fetchBanners()
}

const handleBannerDelete = (row: NewProductBanner) => {
  ElMessageBox.confirm(`确定删除 Banner「${row.title}」？`, '提示', { type: 'warning' }).then(async () => {
    await deleteNewProductBanner(row.id)
    ElMessage.success('删除成功')
    fetchBanners()
  }).catch(() => {})
}

const handleBannerStatusChange = async (row: NewProductBanner) => {
  await updateNewProductBanner(row.id, { status: row.status })
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

// ============ 生命周期 ============
onMounted(() => {
  fetchStats()
  fetchCategories()
  fetchProducts()
  fetchBanners()
})
</script>

<template>
  <div class="new-product-management" v-if="userStore.isAdmin">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">新品总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card active">
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-label">进行中</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ stats.expiring }}</div>
          <div class="stat-label">即将过期</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">{{ stats.todayNew }}</div>
          <div class="stat-label">今日新增</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab 页签 -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- Tab 1: 新品商品 -->
      <el-tab-pane label="新品商品" name="product">
        <div class="toolbar">
          <div class="toolbar-left">
            <el-select v-model="productQuery.categoryId" placeholder="分类筛选" clearable style="width: 160px" @change="fetchProducts">
              <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </div>
          <div class="toolbar-right">
            <el-button type="primary" :icon="Plus" @click="handleAddDialogOpen">添加商品</el-button>
            <el-button type="danger" :icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchUnmark">批量取消新品</el-button>
          </div>
        </div>

        <el-table :data="productList" v-loading="productLoading" @selection-change="handleProductSelect" stripe>
          <el-table-column type="selection" width="50" />
          <el-table-column label="商品图片" width="80">
            <template #default="{ row }">
              <el-image :src="row.main_image" style="width: 50px; height: 50px; border-radius: 4px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category_name" label="分类" width="120" />
          <el-table-column label="价格" width="100">
            <template #default="{ row }">
              <span style="color: #f56c6c; font-weight: 600">¥{{ row.sale_price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="排序权重" width="100" prop="new_product_sort" />
          <el-table-column label="上架时间" width="170">
            <template #default="{ row }">{{ row.new_product_start_time || '永久' }}</template>
          </el-table-column>
          <el-table-column label="下架时间" width="170">
            <template #default="{ row }">{{ row.new_product_end_time || '永久' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.new_product_end_time && new Date(row.new_product_end_time) < new Date()" type="info">已过期</el-tag>
              <el-tag v-else-if="row.new_product_end_time && new Date(row.new_product_end_time) <= new Date(Date.now() + 7 * 86400000)" type="warning">即将过期</el-tag>
              <el-tag v-else type="success">进行中</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEditSettings(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="productQuery.pageNum"
          v-model:page-size="productQuery.pageSize"
          :total="productTotal"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchProducts"
          @size-change="fetchProducts"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </el-tab-pane>

      <!-- Tab 2: Banner 管理 -->
      <el-tab-pane label="Banner 管理" name="banner">
        <div class="toolbar">
          <el-button type="primary" :icon="Plus" @click="handleBannerAdd">新增 Banner</el-button>
        </div>

        <el-table :data="bannerList" v-loading="bannerLoading" stripe>
          <el-table-column label="预览图" width="100">
            <template #default="{ row }">
              <el-image :src="row.imageUrl" style="width: 80px; height: 45px; border-radius: 4px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column label="关联商品" width="120">
            <template #default="{ row }">{{ row.productId ? `商品#${row.productId}` : '-' }}</template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleBannerStatusChange(row)" />
            </template>
          </el-table-column>
          <el-table-column label="展示时间" width="300">
            <template #default="{ row }">
              {{ row.startTime || '永久' }} ~ {{ row.endTime || '永久' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleBannerEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleBannerDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="bannerQuery.pageNum"
          v-model:page-size="bannerQuery.pageSize"
          :total="bannerTotal"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchBanners"
          @size-change="fetchBanners"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </el-tab-pane>

      <!-- Tab 3: 数据概览 -->
      <el-tab-pane label="数据概览" name="overview">
        <el-empty description="数据概览功能开发中" />
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑新品设置弹窗 -->
    <el-dialog v-model="settingsDialogVisible" title="编辑新品设置" width="500px">
      <el-form ref="settingsFormRef" :model="settingsFormData" label-width="100px">
        <el-form-item label="排序权重">
          <el-input-number v-model="settingsFormData.sort" :min="0" :max="9999" />
          <div class="form-tip">数值越大越靠前</div>
        </el-form-item>
        <el-form-item label="上架时间">
          <el-date-picker v-model="settingsFormData.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空则永久" style="width: 100%" />
        </el-form-item>
        <el-form-item label="下架时间">
          <el-date-picker v-model="settingsFormData.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空则不过期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSettingsSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加商品弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加新品商品" width="800px" @open="handleAddDialogOpen">
      <div class="toolbar" style="margin-bottom: 12px">
        <el-input v-model="addProductQuery.keyword" placeholder="搜索商品名称" clearable style="width: 240px" @keyup.enter="fetchAddProducts" />
        <el-button type="primary" @click="fetchAddProducts">搜索</el-button>
      </div>
      <el-table :data="addProductList" v-loading="addProductLoading" @selection-change="handleAddSelect" stripe max-height="400">
        <el-table-column type="selection" width="50" />
        <el-table-column label="图片" width="70">
          <template #default="{ row }">
            <el-image :src="row.mainImage" style="width: 40px; height: 40px; border-radius: 4px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.salePrice }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="addProductQuery.pageNum"
        v-model:page-size="addProductQuery.pageSize"
        :total="addProductTotal"
        layout="total, prev, pager, next"
        @current-change="fetchAddProducts"
        style="margin-top: 12px; justify-content: flex-end"
      />
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="addSelectedIds.length === 0" @click="handleBatchMark">
          确定添加 ({{ addSelectedIds.length }})
        </el-button>
      </template>
    </el-dialog>

    <!-- Banner 编辑弹窗 -->
    <el-dialog v-model="bannerDialogVisible" :title="bannerDialogType === 'add' ? '新增 Banner' : '编辑 Banner'" width="600px">
      <el-form ref="bannerFormRef" :model="bannerFormData" label-width="100px">
        <el-form-item label="标题" prop="title" :rules="[{ required: true, message: '请输入标题' }]">
          <el-input v-model="bannerFormData.title" placeholder="请输入 Banner 标题" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl" :rules="[{ required: true, message: '请输入图片URL' }]">
          <el-input v-model="bannerFormData.imageUrl" placeholder="请输入图片 URL" />
        </el-form-item>
        <el-form-item label="关联商品ID">
          <el-input-number v-model="bannerFormData.productId" :min="0" placeholder="可选" style="width: 100%" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="bannerFormData.linkUrl" placeholder="与商品ID二选一" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="bannerFormData.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="展示时间">
          <el-date-picker v-model="bannerFormData.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="开始时间" style="width: 45%" />
          <span style="margin: 0 8px">~</span>
          <el-date-picker v-model="bannerFormData.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="结束时间" style="width: 45%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBannerSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
  <div v-else class="access-denied">
    <el-empty description="权限不足，仅管理员可访问此页面" />
  </div>
</template>

<style scoped lang="scss">
.new-product-management {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;

  .stat-value {
    font-size: 32px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 8px;
  }

  &.active .stat-value { color: #67c23a; }
  &.warning .stat-value { color: #e6a23c; }
  &.success .stat-value { color: #409eff; }
}

.main-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .toolbar-left, .toolbar-right {
    display: flex;
    gap: 8px;
    align-items: center;
  }
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.access-denied {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
</style>
