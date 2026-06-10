import { getHttpClient } from '../utils/http'
import type { Banner } from '../types'

export const bannerApi = {
  getActive: () => getHttpClient().get<Banner[]>('/api/v1/banners/active'),
}
