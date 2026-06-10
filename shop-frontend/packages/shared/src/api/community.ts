import { getHttpClient } from '../utils/http'
import type { NoteResponse, NoteDetailResponse, CommentResponse, PageResult } from '../types'

export const communityApi = {
  getNoteList: (params?: Record<string, unknown>) =>
    getHttpClient().get<PageResult<NoteResponse>>('/api/v1/community/notes', params),

  getNoteDetail: (id: number) =>
    getHttpClient().get<NoteDetailResponse>(`/api/v1/community/notes/${id}`),

  createNote: (data: Record<string, unknown>) =>
    getHttpClient().post<NoteDetailResponse>('/api/v1/community/notes', data),

  updateNote: (id: number, data: Record<string, unknown>) =>
    getHttpClient().put<NoteDetailResponse>(`/api/v1/community/notes/${id}`, data),

  deleteNote: (id: number) =>
    getHttpClient().delete<void>(`/api/v1/community/notes/${id}`),

  toggleLike: (id: number) =>
    getHttpClient().post<boolean>(`/api/v1/community/notes/${id}/like`),

  toggleFavorite: (id: number) =>
    getHttpClient().post<boolean>(`/api/v1/community/notes/${id}/favorite`),

  getComments: (noteId: number, params?: Record<string, unknown>) =>
    getHttpClient().get<PageResult<CommentResponse>>(`/api/v1/community/notes/${noteId}/comments`, params),

  createComment: (noteId: number, data: { content: string; parentId?: number; replyToUserId?: number }) =>
    getHttpClient().post<CommentResponse>(`/api/v1/community/notes/${noteId}/comments`, data as unknown as Record<string, unknown>),

  deleteComment: (id: number) =>
    getHttpClient().delete<void>(`/api/v1/community/comments/${id}`),

  getUserNotes: (userId: number, params?: Record<string, unknown>) =>
    getHttpClient().get<PageResult<NoteResponse>>(`/api/v1/community/users/${userId}/notes`, params),

  getMyFavorites: (params?: Record<string, unknown>) =>
    getHttpClient().get<PageResult<NoteResponse>>('/api/v1/community/users/me/favorites', params),
}
