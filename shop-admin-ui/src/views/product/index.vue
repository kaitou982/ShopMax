
<script setup lang="ts">
/**
 * 商品管理页面
 * @description 商品列表管理，支持增删改查、上架下架
 * @author shop
 * @since 2026-04-22
 */
defineOptions({ name: 'ProductManagement' })

import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getProductList,
  getProductDetail,
  createProduct,
  updateProduct,
  deleteProduct,
  onShelfProduct,
  offShelfProduct,
  getAllBrands,
  getCategoryTree
} from '@/api/modules/product'

const userStore = useUserStore()
import type { Product, Category, Brand } from '@/api/modules/product'

// 列表相关
const loading = ref(false)
const productList = ref<Product[]>([])
const total = ref(0)
const categoryList = ref<Category[]>([])
const brandList = ref<Brand[]>([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  categoryId: undefined as number | undefined,
  keyword: '',
  status: undefined as number | undefined
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const formData = reactive<Partial<Product>>({
  id: undefined,
  name: '',
  subtitle: '',
  description: '',
  mainImage: '',
  categoryId: undefined,
  brandId: undefined,
  originalPrice: 0,
  salePrice: 0,
  stock: 0,
  status: 0,
  isRecommend: 0,
  isNew: 0
})

const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  salePrice: [{ required: true, message: '请输入销售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      categoryId: query.categoryId,
      keyword: query.keyword || undefined,
      status: query.status
    })
    productList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 获取分类和品牌数据
const fetchCategoryAndBrand = async () => {
  try {
    const [categoryRes, brandRes] = await Promise.all([
      getCategoryTree(),
      getAllBrands()
    ])
    categoryList.value = categoryRes
    brandList.value = brandRes
  } catch (error) {
    console.error('获取分类品牌失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  query.pageNum = 1
  fetchData()
}

// 重置搜索
const handleReset = () => {
  query.categoryId = undefined
  query.keyword = ''
  query.status = undefined
  query.pageNum = 1
  fetchData()
}

// 打开新增弹窗
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(formData, {
    id: undefined,
    name: '',
    subtitle: '',
    description: '',
    mainImage: '',
    categoryId: undefined,
    brandId: undefined,
    originalPrice: 0,
    salePrice: 0,
    stock: 0,
    status: 0,
    isRecommend: 0,
    isNew: 0
  })
  dialogVisible.value = true
}

// 打开编辑弹窗
const handleEdit = async (row: Product) => {
  dialogType.value = 'edit'
  try {
    const res = await getProductDetail(row.id)
    Object.assign(formData, res)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取商品详情失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (dialogType.value === 'add') {
        await createProduct(formData)
        ElMessage.success('新增成功')
      } else {
        await updateProduct(formData.id!, formData)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      await fetchData()
    } catch (error) {
      console.error('操作失败:', error)
    }
  })
}

// 删除商品
const handleDelete = async (row: Product) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 上架
const handleOnShelf = async (row: Product) => {
  try {
    await onShelfProduct(row.id)
    ElMessage.success('上架成功')
    await fetchData()
  } catch (error) {
    console.error('上架失败:', error)
  }
}

// 下架
const handleOffShelf = async (row: Product) => {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '提示', {
      type: 'warning'
    })
    await offShelfProduct(row.id)
    ElMessage.success('下架成功')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
    }
  }
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

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchData()
  fetchCategoryAndBrand()
})
</script>

<template>
  <div v-if="userStore.isAdmin || userStore.isStore" class="product-page page-container">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增商品
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select v-model="query.categoryId" placeholder="选择分类" clearable @clear="handleReset" style="width: 150px">
        <el-option
          v-for="item in categoryList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="商品名称"
        clearable
        @clear="handleReset"
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.status" placeholder="商品状态" clearable @clear="handleReset" style="width: 120px">
        <el-option label="下架" :value="0" />
        <el-option label="上架" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>搜索
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="productList" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="主图" width="80">
        <template #default="{ row }">
          <el-image
            :src="row.mainImage"
            :preview-src-list="[row.mainImage]"
            fit="cover"
            style="width: 50px; height: 50px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="salePrice" label="销售价" width="100">
        <template #default="{ row }">
          ¥{{ row.salePrice.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="originalPrice" label="原价" width="100">
        <template #default="{ row }">
          ¥{{ row.originalPrice.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isRecommend" label="推荐" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isRecommend === 1" type="warning">推荐</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 0"
            type="success"
            link
            @click="handleOnShelf(row)"
          >上架</el-button>
          <el-button
            v-else
            type="warning"
            link
            @click="handleOffShelf(row)"
          >下架</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增商品' : '编辑商品'"
      width="900px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="副标题">
              <el-input v-model="formData.subtitle" placeholder="请输入副标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="formData.categoryId" placeholder="选择分类" style="width: 100%">
                <el-option
                  v-for="item in categoryList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-select v-model="formData.brandId" placeholder="选择品牌" style="width: 100%">
                <el-option
                  v-for="item in brandList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="原价">
              <el-input-number v-model="formData.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="销售价" prop="salePrice">
              <el-input-number v-model="formData.salePrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="formData.stock" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品主图">
              <ImageUpload v-model="formData.mainImage" upload-type="product" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="推荐">
              <el-radio-group v-model="formData.isRecommend">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
// All shared styles (page-header, search-form, pagination-wrap)
// are now provided globally in styles/index.scss
</style>
