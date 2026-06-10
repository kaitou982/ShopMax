<script setup lang="ts">
defineOptions({ name: 'FaqManagement' })

import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { PageResult } from '@/types/api'
import { get, post, put, del } from '@/api/request'

interface CsFaq {
  id: number
  category: string
  question: string
  answer: string
  sortOrder: number
  status: number
}

const loading = ref(false)
const list = ref<CsFaq[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const filterCategory = ref('')

const categories = ['支付', '退换货', '配送', '发票', '会员', '售后', '其他']

// Dialog
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<Partial<CsFaq>>({ category: '', question: '', answer: '', sortOrder: 0 })
const formId = ref<number | null>(null)

// Import dialog
const importVisible = ref(false)
const importJson = ref('')

const loadList = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterCategory.value) params.category = filterCategory.value
    const data = await get<PageResult<CsFaq>>('/api/v1/cs/faqs', params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  isEdit.value = false
  formId.value = null
  form.value = { category: '', question: '', answer: '', sortOrder: 0 }
  dialogVisible.value = true
}

const openEdit = (row: CsFaq) => {
  isEdit.value = true
  formId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.question || !form.value.answer) {
    ElMessage.warning('问题和答案不能为空')
    return
  }
  try {
    if (isEdit.value && formId.value) {
      await put(`/api/v1/cs/faqs/${formId.value}`, form.value as Record<string, unknown>)
      ElMessage.success('更新成功')
    } else {
      await post('/api/v1/cs/faqs', form.value as Record<string, unknown>)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } catch { /* error handled by interceptor */ }
}

const handleDelete = async (row: CsFaq) => {
  try {
    await ElMessageBox.confirm(`确认删除 FAQ「${row.question}」？`, '删除确认', { type: 'warning' })
    await del(`/api/v1/cs/faqs/${row.id}`)
    ElMessage.success('删除成功')
    loadList()
  } catch { /* cancelled */ }
}

const handleImport = async () => {
  try {
    const items = JSON.parse(importJson.value)
    if (!Array.isArray(items) || items.length === 0) {
      ElMessage.warning('请粘贴有效的 JSON 数组')
      return
    }
    await post('/api/v1/cs/faqs/batch-import', { items })
    ElMessage.success(`导入 ${items.length} 条成功`)
    importVisible.value = false
    importJson.value = ''
    loadList()
  } catch {
    ElMessage.error('导入失败，请检查 JSON 格式')
  }
}

const handleExport = async () => {
  try {
    const data = await get<CsFaq[]>('/api/v1/cs/faqs/export')
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'faq-export.json'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

const handlePageChange = (p: number) => {
  pageNum.value = p
  loadList()
}

const handlePageSizeChange = (s: number) => {
  pageSize.value = s
  pageNum.value = 1
  loadList()
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="faq-page">
    <div class="page-header">
      <h2>FAQ 知识库管理</h2>
      <div class="header-actions">
        <el-button @click="importVisible = true">批量导入</el-button>
        <el-button @click="handleExport">导出 JSON</el-button>
        <el-button type="primary" @click="openCreate">新增 FAQ</el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterCategory" placeholder="分类筛选" clearable @change="loadList" style="width: 160px">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
    </div>

    <el-table :data="list" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="answer" label="答案" min-width="300" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > pageSize"
      style="margin-top: 16px; justify-content: flex-end"
      layout="total, sizes, prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      :current-page="pageNum"
      @update:current-page="handlePageChange"
      @update:page-size="handlePageSizeChange"
    />

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑 FAQ' : '新增 FAQ'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题">
          <el-input v-model="form.question" maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" type="textarea" :rows="5" maxlength="5000" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- Import Dialog -->
    <el-dialog v-model="importVisible" title="批量导入 FAQ" width="600px">
      <p style="margin-bottom: 12px; color: #666">请粘贴 JSON 数组，每项包含 category、question、answer、sortOrder 字段。</p>
      <el-input v-model="importJson" type="textarea" :rows="12" placeholder='[{"category":"支付","question":"...","answer":"...","sortOrder":1}]' />
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.faq-page { padding: 20px; }
.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;
  h2 { margin: 0; font-size: 18px; }
}
.header-actions { display: flex; gap: 8px; }
.filter-bar { margin-bottom: 16px; }
</style>
