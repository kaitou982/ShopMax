<script setup lang="ts">
defineOptions({ name: 'CsSessionsManagement' })

import { ref, onMounted } from 'vue'
import { get } from '@/api/request'
import type { PageResult } from '@/types/api'

interface CsSession {
  id: number
  sessionNo: string
  userId: number
  status: number
  lastMessageTime: string
  createTime: string
}

interface CsMessage {
  id: number
  sessionId: number
  role: string
  content: string
  tokenCount: number
  createTime: string
}

const loading = ref(false)
const list = ref<CsSession[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const filterUserId = ref('')

// Detail dialog
const detailVisible = ref(false)
const detailMessages = ref<CsMessage[]>([])
const detailLoading = ref(false)
const currentSession = ref<CsSession | null>(null)

const loadList = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterUserId.value) params.userId = Number(filterUserId.value)
    const data = await get<PageResult<CsSession>>('/api/v1/cs/admin/sessions', params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row: CsSession) => {
  currentSession.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    const data = await get<PageResult<CsMessage>>(`/api/v1/cs/sessions/${row.sessionNo}/messages`, { pageNum: 1, pageSize: 100 })
    detailMessages.value = data.records || []
  } finally {
    detailLoading.value = false
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
  <div class="sessions-page">
    <div class="page-header">
      <h2>客服会话记录</h2>
    </div>

    <div class="filter-bar">
      <el-input v-model="filterUserId" placeholder="按用户ID筛选" clearable @clear="loadList" @keyup.enter="loadList" style="width: 200px" />
      <el-button type="primary" @click="loadList">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="sessionNo" label="会话编号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '进行中' : '已结束' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastMessageTime" label="最后消息时间" width="180" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看对话</el-button>
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

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" :title="`对话详情 - ${currentSession?.sessionNo}`" width="700px">
      <div v-loading="detailLoading" class="message-list">
        <div v-if="detailMessages.length === 0 && !detailLoading" style="text-align: center; color: #999">暂无消息</div>
        <div
          v-for="msg in detailMessages"
          :key="msg.id"
          class="msg-item"
          :class="{ 'msg-user': msg.role === 'user', 'msg-ai': msg.role === 'assistant' }"
        >
          <div class="msg-role">{{ msg.role === 'user' ? '用户' : 'AI助手' }}</div>
          <div class="msg-content">{{ msg.content }}</div>
          <div class="msg-meta">{{ msg.createTime }} | Token: {{ msg.tokenCount }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.sessions-page { padding: 20px; }
.page-header { margin-bottom: 16px; h2 { margin: 0; font-size: 18px; } }
.filter-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.message-list { max-height: 500px; overflow-y: auto; }
.msg-item {
  margin-bottom: 16px; padding: 12px; border-radius: 8px;
  &.msg-user { background: #eff6ff; border-left: 3px solid #3b82f6; }
  &.msg-ai { background: #f9fafb; border-left: 3px solid #10b981; }
}
.msg-role { font-weight: 600; font-size: 13px; margin-bottom: 6px; color: #374151; }
.msg-content { font-size: 14px; line-height: 1.6; color: #1f2937; white-space: pre-wrap; }
.msg-meta { font-size: 12px; color: #9ca3af; margin-top: 8px; }
</style>
