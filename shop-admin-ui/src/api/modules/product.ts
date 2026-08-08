import { get, post, put, del } from '../request'
import type { PageResult, PageParams } from '@/types/api'

export interface Category {
  id: number
  parentId: number
  name: string
  level: number
  icon: string
  sort: number
  status: number
  createTime: string
}

export interface Brand {
  id: number
  name: string
  logo: string
  description: string
  sort: number
  status: number
  createTime: string
}

export interface Product {
  id: number
  name: string
  subtitle: string
  description: string
  mainImage: string
  subImages: string
  detail: string
  categoryId: number
  brandId: number
  originalPrice: number
  salePrice: number
  stock: number
  sales: number
  status: number
  isRecommend: number
  isNew: number
  sort: number
  createTime: string
}

// 分类管理
export const getCategoryList = (params?: PageParams & { parentId?: number }) => {
  return get<PageResult<Category>>('/api/v1/categories', params)
}

export const getCategoryTree = () => {
  return get<Category[]>('/api/v1/categories/tree')
}

export const createCategory = (data: Partial<Category>) => {
  return post<Category>('/api/v1/categories', data)
}

export const updateCategory = (id: number, data: Partial<Category>) => {
  return put<Category>(`/api/v1/categories/${id}`, data)
}

export const deleteCategory = (id: number) => {
  return del(`/api/v1/categories/${id}`)
}

// 品牌管理
export const getBrandList = (params?: PageParams) => {
  return get<PageResult<Brand>>('/api/v1/brands', params)
}

export const getAllBrands = () => {
  return get<Brand[]>('/api/v1/brands/all')
}

export const createBrand = (data: Partial<Brand>) => {
  return post<Brand>('/api/v1/brands', data)
}

export const updateBrand = (id: number, data: Partial<Brand>) => {
  return put<Brand>(`/api/v1/brands/${id}`, data)
}

export const deleteBrand = (id: number) => {
  return del(`/api/v1/brands/${id}`)
}

// 商品管理
export const getProductList = (params?: PageParams & { categoryId?: number; keyword?: string; status?: number }) => {
  return get<PageResult<Product>>('/api/v1/products', params)
}

export const getProductDetail = (id: number) => {
  return get<Product>(`/api/v1/products/${id}`)
}

export const createProduct = (data: Partial<Product>) => {
  return post<Product>('/api/v1/products', data)
}

export const updateProduct = (id: number, data: Partial<Product>) => {
  return put<Product>(`/api/v1/products/${id}`, data)
}

export const deleteProduct = (id: number) => {
  return del(`/api/v1/products/${id}`)
}

export const onShelfProduct = (id: number) => {
  return put(`/api/v1/products/${id}/on-shelf`)
}

export const offShelfProduct = (id: number) => {
  return put(`/api/v1/products/${id}/off-shelf`)
}

// ==================== 新品管理 ====================

export interface NewProductBanner {
  id: number
  title: string
  imageUrl: string
  productId: number | null
  linkUrl: string | null
  sort: number
  status: number
  startTime: string | null
  endTime: string | null
  createTime: string
}

// 新品商品管理
export const getNewProductList = (params: { pageNum?: number; pageSize?: number; categoryId?: number }) => {
  return get<{ records: Product[]; total: number }>('/api/v1/admin/products/new', params)
}

export const batchMarkNew = (ids: number[]) => {
  return put('/api/v1/admin/products/new/batch-mark', { ids })
}

export const batchUnmarkNew = (ids: number[]) => {
  return put('/api/v1/admin/products/new/batch-unmark', { ids })
}

export const updateNewProductSettings = (id: number, data: { sort?: number; startTime?: string; endTime?: string }) => {
  return put(`/api/v1/admin/products/${id}/new-settings`, data)
}

export const getNewProductStats = () => {
  return get<{ total: number; active: number; expiring: number; todayNew: number }>('/api/v1/admin/products/new/stats')
}

// 新品Banner管理
export const getNewProductBanners = (params?: { pageNum?: number; pageSize?: number }) => {
  return get<PageResult<NewProductBanner>>('/api/v1/admin/new-product-banners', params)
}

export const createNewProductBanner = (data: Partial<NewProductBanner>) => {
  return post<NewProductBanner>('/api/v1/admin/new-product-banners', data)
}

export const updateNewProductBanner = (id: number, data: Partial<NewProductBanner>) => {
  return put<NewProductBanner>(`/api/v1/admin/new-product-banners/${id}`, data)
}

export const deleteNewProductBanner = (id: number) => {
  return del(`/api/v1/admin/new-product-banners/${id}`)
}
