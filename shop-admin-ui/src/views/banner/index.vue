<script setup lang="ts">
/**
 * BannerManagement 轮播图管理
 * @description Banner轮播图的增删改查、排序管理
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getBannerList, createBanner, updateBanner, deleteBanner,
  type Banner
} from '@/api/modules/banner'

defineOptions({ name: 'BannerManagement' })

const loading = ref(false)
const bannerList = ref<Banner[]>([])
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const formData = reactive<Banner>({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 1,
  status: 1
})

const editId = ref<number | null>(null)

const bannerStatusMap: Record<number, string> = { 0: '禁用', 1: '启用' }

const fetchBanners = async () => {
  try {
    loading.value = true
    bannerList.value = await getBannerList()
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  editId.value = null
  Object.assign(formData, { id: undefined, title: '', imageUrl: '', linkUrl: '', sort: 1, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: Banner) => {
  dialogType.value = 'edit'
  editId.value = row.id!
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row: Banner) => {
  try {
    await ElMessageBox.confirm(`确定删除轮播图 "${row.title}" 吗？`, '删除确认', {
      type: 'warning'
    })
    await deleteBanner(row.id!)
    ElMessage.success('删除成功')
    fetchBanners()
  } catch {
    // cancelled
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    if (dialogType.value === 'add') {
      await createBanner({ ...formData })
      ElMessage.success('创建成功')
    } else {
      await updateBanner(editId.value!, { ...formData })
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    fetchBanners()
  } catch {
    // error handled by interceptor
  }
}

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

onMounted(() => {
  fetchBanners()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>轮播图管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增轮播图</el-button>
    </div>

    <el-table :data="bannerList" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column label="图片" width="180" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imageUrl"
            fit="cover"
            style="width: 150px; height: 67px; border-radius: 4px"
            preview-teleported
            :preview-src-list="[row.imageUrl]"
          />
          <span v-else style="color:#999">无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="linkUrl" label="链接地址" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.linkUrl || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" sortable />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ bannerStatusMap[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增轮播图' : '编辑轮播图'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入轮播图标题" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="图片" prop="imageUrl">
          <div style="display:flex;gap:12px;align-items:flex-start">
            <ImageUpload v-model="formData.imageUrl" upload-type="banner" />
            <el-image
              v-if="formData.imageUrl"
              :src="formData.imageUrl"
              fit="cover"
              style="width: 200px; height: 90px; border-radius: 6px; border: 1px solid #e5e7eb"
              preview-teleported
            />
          </div>
          <div style="color:#909399;font-size:12px;margin-top:6px">
            建议尺寸 750×340，支持 JPG/PNG/WebP，大小不超过 2MB
          </div>
        </el-form-item>

        <el-form-item label="链接地址">
          <el-input v-model="formData.linkUrl" placeholder="点击跳转地址（选填）" />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="1" :max="99" />
          <span style="margin-left:8px;color:#909399;font-size:12px">数值越小越靠前</span>
        </el-form-item>

        <el-form-item label="状态">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
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
.page-container {
  padding: 20px;
  background: #fff;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }
  }
}
</style>
