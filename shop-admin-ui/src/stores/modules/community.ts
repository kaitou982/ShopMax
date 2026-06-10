import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getAuditNoteList,
  getAuditNoteDetail,
  auditNote,
  getStatsOverview,
  type NoteResponse,
  type NoteDetailResponse,
  type StatsOverview,
  type AuditNoteParams,
  type NoteQueryParams
} from '@/api/modules/community'

export const useCommunityStore = defineStore('community', () => {
  const auditList = ref<NoteResponse[]>([])
  const auditTotal = ref(0)
  const currentNote = ref<NoteDetailResponse | null>(null)
  const stats = ref<StatsOverview | null>(null)
  const loading = ref(false)
  const previewLoading = ref(false)

  const fetchAuditList = async (params: NoteQueryParams = {}) => {
    loading.value = true
    try {
      const data = await getAuditNoteList({ pageNum: 1, pageSize: 10, ...params })
      auditList.value = data.records || []
      auditTotal.value = data.total || 0
    } finally {
      loading.value = false
    }
  }

  const fetchAuditDetail = async (id: number) => {
    previewLoading.value = true
    try {
      currentNote.value = await getAuditNoteDetail(id)
    } finally {
      previewLoading.value = false
    }
  }

  const doAudit = async (id: number, data: AuditNoteParams) => {
    await auditNote(id, data)
    auditList.value = auditList.value.filter(n => n.id !== id)
    auditTotal.value--
    if (currentNote.value?.id === id) {
      currentNote.value = null
    }
  }

  const fetchStats = async () => {
    stats.value = await getStatsOverview()
  }

  const selectNote = (note: NoteResponse | null) => {
    if (!note) {
      currentNote.value = null
      return
    }
    fetchAuditDetail(note.id)
  }

  return {
    auditList,
    auditTotal,
    currentNote,
    stats,
    loading,
    previewLoading,
    fetchAuditList,
    fetchAuditDetail,
    doAudit,
    fetchStats,
    selectNote
  }
})
