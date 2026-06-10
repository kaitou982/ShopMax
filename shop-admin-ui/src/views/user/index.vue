<script setup lang="ts">
/**
 * 用户管理页面
 * @description 用户列表管理，支持增删改查
 * @author shop
 * @since 2026-04-22
 */
defineOptions({ name: 'UserManagement' })

import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import { getUserList, getUserDetail, updateUser, deleteUser, createUser } from '@/api/modules/user'
import type { UserInfo } from '@/types/api'

const userStore = useUserStore()

// 列表相关
const loading = ref(false)
const userList = ref<UserInfo[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  phone: ''
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const formData = reactive({
  userId: 0,
  username: '',
  nickname: '',
  phone: '',
  email: '',
  status: 1,
  memberLevel: 1
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList(query)
    userList.value = res.records

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
  query.username = ''
  query.phone = ''
  query.pageNum = 1
  fetchData()
}

// 打开新增弹窗
const handleAdd = () => {
  dialogType.value = 'add'
  formData.userId = 0
  formData.username = ''
  formData.nickname = ''
  formData.phone = ''
  formData.email = ''
  formData.status = 1
  formData.memberLevel = 1
  dialogVisible.value = true
}

// 打开编辑弹窗
const handleEdit = async (row: UserInfo) => {
  dialogType.value = 'edit'
  try {
    const res = await getUserDetail(row.userId)
    Object.assign(formData, res)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (dialogType.value === 'add') {
        // 创建用户需要密码，这里使用默认密码
        await createUser({...formData, password: '123456' })
        ElMessage.success('新增成功')
      } else {
        await updateUser(formData.userId, formData)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      await fetchData()
    } catch (error) {
      console.error('操作失败:', error)
    }
  })
}

// 删除用户
const handleDelete = async (row: UserInfo) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      type: 'warning'
    })
    await deleteUser(row.userId)
    ElMessage.success('删除成功')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
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
})
</script>

<template>
  <div v-if="userStore.isAdmin" class="user-page page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="query.username"
        placeholder="用户名"
        clearable
        @clear="handleReset"
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-input
        v-model="query.phone"
        placeholder="手机号"
        clearable
        @clear="handleReset"
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>搜索
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="userList" border>
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="memberLevel" label="会员等级" width="100">
        <template #default="{ row }">
          <el-tag type="info">{{ row.memberLevelName || '普通会员' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="integral" label="积分" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
// All shared styles (page-header, search-bar, pagination)
// are now provided globally in styles/index.scss
</style>
