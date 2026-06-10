import { get, post, put, del } from '../request'
import type { PageResult } from '@/types/api'

export interface NoteResponse {
  id: number
  userId: number
  userNickname: string
  userAvatar: string
  title: string
  content?: string
  coverUrl: string
  status: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  viewCount: number
  images: string[]
  products: ProductItem[]
  isLiked?: boolean
  isFavorited?: boolean
  createTime: string
  rejectReason?: string
}

export interface NoteDetailResponse extends NoteResponse {
  shareCount: number
  locationName: string
  updateTime: string
  images: NoteImageItem[]
}

export interface NoteImageItem {
  id: number
  imageUrl: string
  sortOrder: number
}

export interface ProductItem {
  id: number
  name: string
  mainImage: string
  salePrice: number
}

export interface StatsOverview {
  pendingReviewCount: number
  todayApprovedCount: number
  todayRejectedCount: number
  totalNoteCount: number
}

export interface AuditNoteParams {
  status: number
  rejectReason?: string
}

export interface NoteQueryParams {
  pageNum?: number
  pageSize?: number
  status?: number
  keyword?: string
}

// C端
export const getNoteList = (params: { pageNum?: number; pageSize?: number; tab?: string }) =>
  get<PageResult<NoteResponse>>('/api/v1/community/notes', params)

export const getNoteDetail = (id: number) =>
  get<NoteDetailResponse>(`/api/v1/community/notes/${id}`)

export const createNote = (data: Record<string, unknown>) =>
  post<NoteDetailResponse>('/api/v1/community/notes', data)

export const updateNote = (id: number, data: Record<string, unknown>) =>
  put<NoteDetailResponse>(`/api/v1/community/notes/${id}`, data)

export const deleteNote = (id: number) =>
  del<void>(`/api/v1/community/notes/${id}`)

export const toggleLike = (id: number) =>
  post<boolean>(`/api/v1/community/notes/${id}/like`)

export const toggleFavorite = (id: number) =>
  post<boolean>(`/api/v1/community/notes/${id}/favorite`)

export const getComments = (noteId: number, params: { pageNum?: number; pageSize?: number }) =>
  get<PageResult<CommentResponse>>(`/api/v1/community/notes/${noteId}/comments`, params)

export const createComment = (noteId: number, data: { content: string; parentId?: number; replyToUserId?: number }) =>
  post<CommentResponse>(`/api/v1/community/notes/${noteId}/comments`, data)

export const deleteComment = (id: number) =>
  del<void>(`/api/v1/community/comments/${id}`)

export const getUserNotes = (userId: number, params: { pageNum?: number; pageSize?: number }) =>
  get<PageResult<NoteResponse>>(`/api/v1/community/users/${userId}/notes`, params)

export const getMyFavorites = (params: { pageNum?: number; pageSize?: number }) =>
  get<PageResult<NoteResponse>>('/api/v1/community/users/me/favorites', params)

// 后台管理
export const getAuditNoteList = (params: NoteQueryParams) =>
  get<PageResult<NoteResponse>>('/api/v1/admin/community/notes', params)

export const getAuditNoteDetail = (id: number) =>
  get<NoteDetailResponse>(`/api/v1/admin/community/notes/${id}`)

export const auditNote = (id: number, data: AuditNoteParams) =>
  put<NoteDetailResponse>(`/api/v1/admin/community/notes/${id}/audit`, data)

export const getStatsOverview = () =>
  get<StatsOverview>('/api/v1/admin/community/stats/overview')

export interface CommentResponse {
  id: number
  noteId: number
  userId: number
  userNickname: string
  userAvatar: string
  parentId: number | null
  replyToUserId: number | null
  replyToUserNickname: string
  content: string
  likeCount: number
  children: CommentResponse[]
  createTime: string
}
