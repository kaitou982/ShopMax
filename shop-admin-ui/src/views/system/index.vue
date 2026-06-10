<script setup lang="ts">
defineOptions({ name: 'SystemSettings' })

import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import { getStoreApplications, auditStore } from '@/api/modules/user'
import type { UserInfo } from '@/types/api'

const userStore = useUserStore()

const loading = ref(false)
const applications = ref<UserInfo[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const auditLoading = ref<Record<number, boolean>>({})

const loadApplications = async () => {
  loading.value = true
  try {
    const res = await getStoreApplications(pageNum.value, pageSize.value)
    applications.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleAudit = async (userId: number, status: number) => {
  if (status === 2) {
    try {
      const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝入驻', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea'
      })
      if (!reason) return
      await doAudit(userId, status, reason)
    } catch {
      // cancelled
    }
  } else {
    await ElMessageBox.confirm('确定通过该店家的入驻申请吗？', '审核确认', {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
      type: 'success'
    }).then(() => doAudit(userId, status, undefined))
      .catch(() => {})
  }
}

const doAudit = async (userId: number, status: number, rejectReason?: string) => {
  auditLoading.value[userId] = true
  try {
    await auditStore(userId, { status, rejectReason })
    ElMessage.success(status === 1 ? '审核通过，店家请重新登录以使权限生效' : '已拒绝')
    loadApplications()
  } finally {
    auditLoading.value[userId] = false
  }
}

onMounted(() => {
  if (userStore.isAdmin) {
    loadApplications()
  }
})
</script>

<template>
  <div v-if="userStore.isAdmin" class="page-container">
    <h2>系统设置</h2>

    <el-tabs type="border-card">
      <!-- 入驻审核 -->
      <el-tab-pane label="店家入驻审核">
        <el-table :data="applications" v-loading="loading" stripe>
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="storeName" label="店铺名称" width="180" />
          <el-table-column prop="storeDescription" label="店铺简介" min-width="200" show-overflow-tooltip />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="申请时间" width="170">
            <template #default="{ row }">
              {{ row.lastLoginTime || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                type="success"
                size="small"
                :loading="auditLoading[row.userId]"
                @click="handleAudit(row.userId, 1)"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                :loading="auditLoading[row.userId]"
                @click="handleAudit(row.userId, 2)"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="total > pageSize"
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadApplications"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </el-tab-pane>

      <!-- 系统信息 -->
      <el-tab-pane label="关于系统">
        <el-descriptions title="ShopMax 电商平台" :column="2" border>
          <el-descriptions-item label="版本">v1.0.0</el-descriptions-item>
          <el-descriptions-item label="技术栈">SpringBoot 3 + Vue 3</el-descriptions-item>
          <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
          <el-descriptions-item label="Java版本">JDK 21</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
    </el-tabs>
  </div>
  <el-empty v-else description="您没有权限访问此页面" />
</template>
