// HTTP 客户端抽象接口 — Web 用 Axios，Mobile 用 uni.request

export interface HttpClient {
  get<T>(url: string, params?: Record<string, unknown>): Promise<T>
  post<T>(url: string, data?: Record<string, unknown>): Promise<T>
  put<T>(url: string, data?: Record<string, unknown>): Promise<T>
  delete<T>(url: string): Promise<T>
}

let _client: HttpClient | null = null

export function setHttpClient(client: HttpClient) {
  _client = client
}

export function getHttpClient(): HttpClient {
  if (!_client) throw new Error('HttpClient not initialized. Call setHttpClient() first.')
  return _client
}
