import request from '@/api/request'

/**
 * 上传单个图片文件
 */
export const uploadFile = (file: File, type: string): Promise<{ url: string; objectName: string; size: number; contentType: string }> => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/api/v1/files/upload?type=${type}`, formData)
}

/**
 * 批量上传图片文件
 */
export const uploadFiles = (files: File[], type: string): Promise<{ url: string; objectName: string; size: number; contentType: string }[]> => {
  const formData = new FormData()
  files.forEach(f => formData.append('files', f))
  return request.post(`/api/v1/files/upload/batch?type=${type}`, formData)
}
