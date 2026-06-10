import { getHttpClient } from '../utils/http'
import type { ProductDetail, ProductPageParams, PageResult, Category, Brand } from '../types'

export const productApi = {
  getDetail: (id: number) =>
    getHttpClient().get<ProductDetail>(`/api/v1/products/${id}`),

  getPage: (params?: ProductPageParams) =>
    getHttpClient().get<PageResult<ProductDetail>>('/api/v1/products', params as Record<string, unknown>),

  getRecommend: (limit?: number) =>
    getHttpClient().get<ProductDetail[]>('/api/v1/products/recommend', { limit: limit || 10 } as Record<string, unknown>),

  getNew: (limit?: number) =>
    getHttpClient().get<ProductDetail[]>('/api/v1/products/new', { limit: limit || 10 } as Record<string, unknown>),
}

export const categoryApi = {
  getTree: () =>
    getHttpClient().get<Category[]>('/api/v1/categories/tree'),

  getChildren: (parentId: number) =>
    getHttpClient().get<Category[]>(`/api/v1/categories/${parentId}/children`),
}

export const brandApi = {
  getAll: () =>
    getHttpClient().get<Brand[]>('/api/v1/brands/all'),
}
