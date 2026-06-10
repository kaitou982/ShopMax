<script setup lang="ts">
defineOptions({ name: 'CommunityManagement' })

import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCommunityStore } from '@/stores/modules/community'
import { useUserStore } from '@/stores/modules/user'
import type { NoteResponse, NoteDetailResponse } from '@/api/modules/community'
import {
  getAuditNoteList,
  getAuditNoteDetail,
  auditNote,
  getStatsOverview
} from '@/api/modules/community'

const store = useCommunityStore()
const userStore = useUserStore()
const activeTab = ref<'audit' | 'stats'>('audit')
const filterStatus = ref<number | undefined>(undefined)
const keyword = ref('')
const selectedNoteId = ref<number | null>(null)
const previewLoading = ref(false)
const currentPreview = ref<NoteDetailResponse | null>(null)

const statusTabs = [
  { label: '全部', value: undefined },
  { label: '待审核', value: 3 },
  { label: '已通过', value: 2 },
  { label: '已驳回', value: 4 }
]

onMounted(() => {
  loadList()
  loadStats()
})

const loadList = async () => {
  try {
    const data = await getAuditNoteList({
      pageNum: 1,
      pageSize: 10,
      status: filterStatus.value,
      keyword: keyword.value || undefined
    })
    store.auditList = data.records || []
    store.auditTotal = data.total || 0
  } catch {
    ElMessage.error('加载列表失败')
  }
}

const loadStats = async () => {
  try {
    store.stats = await getStatsOverview()
  } catch {
    // ignore
  }
}

const onFilterChange = (status: number | undefined) => {
  filterStatus.value = status
  selectedNoteId.value = null
  currentPreview.value = null
  loadList()
}

const onSearch = () => {
  loadList()
}

const onSelectNote = async (note: NoteResponse) => {
  selectedNoteId.value = note.id
  previewLoading.value = true
  try {
    currentPreview.value = await getAuditNoteDetail(note.id)
  } catch {
    ElMessage.error('加载详情失败')
  } finally {
    previewLoading.value = false
  }
}

const onApprove = async (noteId: number) => {
  try {
    await auditNote(noteId, { status: 2 })
    ElMessage.success('已通过')
    store.auditList = store.auditList.filter(n => n.id !== noteId)
    if (selectedNoteId.value === noteId) {
      selectedNoteId.value = null
      currentPreview.value = null
    }
    loadStats()
  } catch {
    ElMessage.error('操作失败')
  }
}

const onReject = async (noteId: number) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回审核', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: (val: string) => {
        if (!val || !val.trim()) return '驳回原因不能为空'
        return true
      }
    })
    await auditNote(noteId, { status: 4, rejectReason: value.trim() })
    ElMessage.success('已驳回')
    store.auditList = store.auditList.filter(n => n.id !== noteId)
    if (selectedNoteId.value === noteId) {
      selectedNoteId.value = null
      currentPreview.value = null
    }
    loadStats()
  } catch {
    // 用户取消操作，不提示错误
  }
}
</script>

<template>
  <div class="community-admin">
    <!-- Stats Cards -->
    <div class="stats-row">
      <div class="stat-card warn">
        <div class="stat-icon">📋</div>
        <div class="stat-body">
          <div class="stat-value">{{ store.stats?.pendingReviewCount ?? 0 }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-body">
          <div class="stat-value">{{ store.stats?.todayApprovedCount ?? 0 }}</div>
          <div class="stat-label">今日通过</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🚫</div>
        <div class="stat-body">
          <div class="stat-value">{{ store.stats?.todayRejectedCount ?? 0 }}</div>
          <div class="stat-label">今日驳回</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📝</div>
        <div class="stat-body">
          <div class="stat-value">{{ store.stats?.totalNoteCount ?? 0 }}</div>
          <div class="stat-label">笔记总量</div>
        </div>
      </div>
    </div>

    <!-- Tabs & Filter -->
    <div class="toolbar">
      <div class="filter-pills">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          class="filter-pill"
          :class="{ active: filterStatus === tab.value }"
          @click="onFilterChange(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="search-box">
        <input
          v-model="keyword"
          placeholder="搜索标题或内容..."
          class="search-input"
          @keyup.enter="onSearch"
        />
        <button class="search-btn" @click="onSearch">搜索</button>
      </div>
    </div>

    <!-- Main Content: Split Layout -->
    <div class="review-layout">
      <!-- Left: Card List -->
      <div class="review-list">
        <div
          v-for="note in store.auditList"
          :key="note.id"
          class="review-card"
          :class="{ selected: selectedNoteId === note.id }"
          @click="onSelectNote(note)"
        >
          <div class="review-card-thumb">
            <img v-if="note.coverUrl" :src="note.coverUrl" class="thumb-img" />
            <span v-else class="thumb-placeholder">🖼</span>
          </div>
          <div class="review-card-info">
            <div class="review-card-title">{{ note.title || '无标题' }}</div>
            <div class="review-card-meta">
              <span>👤 {{ note.userNickname || '用户' + note.userId }}</span>
              <span>🕐 {{ note.createTime }}</span>
            </div>
          </div>
          <div class="review-card-actions" @click.stop v-if="userStore.isAdmin">
            <button class="btn-approve" @click="onApprove(note.id)">通过</button>
            <button class="btn-reject" @click="onReject(note.id)">驳回</button>
          </div>
        </div>

        <div class="review-empty" v-if="store.auditList.length === 0">
          <p>暂无审核内容</p>
        </div>
      </div>

      <!-- Right: Preview Panel -->
      <div class="review-preview">
        <div v-if="!selectedNoteId" class="preview-placeholder">
          <p>👈 点击左侧笔记查看详情</p>
        </div>

        <div v-else-if="previewLoading" class="preview-loading">
          <p>加载中...</p>
        </div>

        <div v-else-if="currentPreview" class="preview-content">
          <div class="preview-images" v-if="currentPreview.images && currentPreview.images.length > 0">
            <img
              v-for="img in currentPreview.images.slice(0, 4)"
              :key="img.id"
              :src="img.imageUrl"
              class="preview-img"
            />
          </div>

          <div class="preview-title">{{ currentPreview.title || '无标题' }}</div>
          <div class="preview-author">{{ currentPreview.userNickname }} · {{ currentPreview.createTime }}</div>
          <div class="preview-body">{{ currentPreview.content }}</div>

          <div class="preview-products" v-if="currentPreview.products && currentPreview.products.length > 0">
            <div class="preview-product" v-for="p in currentPreview.products" :key="p.id">
              <img :src="p.mainImage || ''" class="preview-product-img" />
              <div class="preview-product-info">
                <div class="preview-product-name">{{ p.name }}</div>
                <div class="preview-product-price">¥{{ p.salePrice }}</div>
              </div>
            </div>
          </div>

          <!-- Audit Actions -->
          <div class="preview-actions" v-if="userStore.isAdmin">
            <div class="action-buttons">
              <button class="action-btn approve" @click="onApprove(currentPreview.id)">✓ 通过审核</button>
              <button class="action-btn reject" @click="onReject(currentPreview.id)">✕ 驳回</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.community-admin {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #f0f0f0;

  &.warn {
    background: linear-gradient(135deg, #fff9f0, #fff);
    border-color: #ffe0c0;

    .stat-value { color: #e67e22; }
  }
}

.stat-icon {
  font-size: 28px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #000;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-pills {
  display: flex;
  gap: 8px;
}

.filter-pill {
  padding: 6px 18px;
  border-radius: 20px;
  border: 1px solid #e0e0e0;
  background: #fff;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    border-color: #bbb;
  }

  &.active {
    background: #000;
    color: #fff;
    border-color: #000;
    font-weight: 500;
  }
}

.search-box {
  display: flex;
  gap: 8px;
}

.search-input {
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13px;
  width: 200px;
  outline: none;

  &:focus {
    border-color: #000;
  }
}

.search-btn {
  padding: 6px 16px;
  background: #000;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}

.review-layout {
  flex: 1;
  display: flex;
  gap: 16px;
  overflow: hidden;
}

.review-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  padding-right: 4px;
}

.review-card {
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    border-color: #bbb;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  }

  &.selected {
    border-color: #000;
    box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  }
}

.review-card-thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  font-size: 20px;
}

.review-card-info {
  flex: 1;
  min-width: 0;
}

.review-card-title {
  font-size: 13px;
  font-weight: 600;
  color: #000;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.review-card-meta {
  font-size: 11px;
  color: #999;
  display: flex;
  gap: 12px;
}

.review-card-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
}

.btn-approve {
  font-size: 11px;
  font-weight: 600;
  padding: 5px 14px;
  border-radius: 6px;
  border: none;
  background: #000;
  color: #fff;
  cursor: pointer;
}

.btn-reject {
  font-size: 11px;
  font-weight: 600;
  padding: 5px 14px;
  border-radius: 6px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  cursor: pointer;
}

.review-empty {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}

.review-preview {
  width: 340px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  overflow-y: auto;
}

.preview-placeholder, .preview-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-size: 14px;
}

.preview-content {
  padding: 16px;
}

.preview-images {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-bottom: 14px;
}

.preview-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  background: #f5f5f5;
}

.preview-title {
  font-size: 15px;
  font-weight: 600;
  color: #000;
  margin-bottom: 4px;
}

.preview-author {
  font-size: 11px;
  color: #999;
  margin-bottom: 12px;
}

.preview-body {
  font-size: 13px;
  line-height: 1.7;
  color: #333;
  margin-bottom: 14px;
}

.preview-products {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.preview-product {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #fafafa;
  border-radius: 8px;
}

.preview-product-img {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: cover;
  background: #eee;
}

.preview-product-info {
  flex: 1;
}

.preview-product-name {
  font-size: 12px;
  color: #333;
}

.preview-product-price {
  font-size: 13px;
  font-weight: 700;
  color: #000;
}

.preview-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 14px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 8px 0;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;

  &.approve {
    background: #000;
    color: #fff;
    border: none;
  }

  &.reject {
    background: #fff;
    color: #666;
    border: 1px solid #ddd;
  }
}
</style>
