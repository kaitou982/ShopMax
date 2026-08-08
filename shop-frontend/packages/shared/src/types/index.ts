// ── API 通用类型 ──────────────────────────────
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageParams {
  pageNum?: number
  pageSize?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  pages: number
  current: number
  size: number
}

// ── 用户相关 ────────────────────────────────────
export interface UserInfo {
  userId: number
  username: string
  nickname: string
  avatar: string
  phone: string
  email: string
  gender: number
  birthday: string
  status: number
  memberLevel: number
  memberLevelName: string
  integral: number
  balance: number
  growthValue: number
  lastLoginTime: string
  role: string
  storeStatus?: number
  storeName?: string
}

export interface LoginResponse extends UserInfo {
  token: string
}

export interface LoginForm { username: string; password: string }
export interface PhoneLoginForm { phone: string; verifyCode: string }
export interface EmailLoginForm { email: string; verifyCode: string }
export interface WxLoginForm { openid: string; unionid?: string; nickname?: string; avatar?: string }

// ── 会员/钱包相关 ──────────────────────────────
export interface LevelBenefit {
  level: number; name: string; discount: string; threshold: number
}

export interface MemberInfo {
  memberLevel: number; memberLevelName: string
  integral: number; balance: number; growthValue: number
  nextLevelGrowth: number; nextLevelName: string
  levelBenefits: LevelBenefit[]
}

export interface IntegralLog {
  id: number; userId: number; changeAmount: number; afterAmount: number
  type: number; bizId: string; remark: string; createTime: string
}

export interface BalanceLog {
  id: number; userId: number; changeAmount: number; afterAmount: number
  type: number; bizId: string; payChannel: string; remark: string; createTime: string
}
export interface RegisterForm { phone?: string; email?: string; password: string; verifyCode: string; username?: string; nickname?: string }
export interface SendEmailCodeForm { email: string; type: string }
export interface UpdateUserForm { nickname?: string; avatar?: string; gender?: number; birthday?: string; email?: string }
export interface ChangePasswordForm { oldPassword: string; newPassword: string; confirmPassword: string }

// ── 商品相关 ────────────────────────────────────
export interface ProductDetail {
  id: number; name: string; subtitle: string; description: string
  mainImage: string; subImages: string; detail: string
  categoryId: number; brandId: number
  originalPrice: number; salePrice: number; stock: number; sales: number
  status: number; isRecommend: number; isNew: number; sort: number
}

export interface ProductPageParams extends PageParams {
  categoryId?: number; keyword?: string; status?: number
  sortBy?: 'price_asc' | 'price_desc' | 'sales' | 'newest'
}

export interface Category {
  id: number; name: string; parentId: number; level: number
  icon: string; image: string; sort: number; children?: Category[]
}

export interface Brand {
  id: number; name: string; logo: string; description: string
  sort: number; status: number
}

// ── 搜索相关 ────────────────────────────────────
export interface HotKeyword {
  keyword: string; count: number
}

export interface SuggestResponse {
  products: string[]; hotWords: string[]
}

// ── 订单相关 ────────────────────────────────────
export interface OrderItem {
  productId: number; productName: string; productImage: string
  salePrice: number; quantity: number
}

export interface CreateOrderParams {
  totalAmount: number; payAmount: number; freightAmount: number
  couponAmount?: number; integralAmount?: number; useIntegral?: number
  userCouponId?: number; userCouponId2?: number
  receiverName: string; receiverPhone: string; receiverAddress: string
  remark?: string; sourceType: number
  items: OrderItem[]
}

export interface OrderDetail {
  id: number; orderNo: string; userId: number
  totalAmount: number; payAmount: number; freightAmount: number
  couponAmount: number; integralAmount: number
  status: number; payType: number
  receiverName: string; receiverPhone: string; receiverAddress: string
  remark: string; sourceType: number
  createTime: string; payTime?: string; deliveryTime?: string; receiveTime?: string
  items?: OrderItem[]
}

// ── 地址相关 ────────────────────────────────────
export interface AddressInfo {
  addressId: number
  receiverName: string; receiverPhone: string
  province: string; provinceCode: string
  city: string; cityCode: string
  district: string; districtCode: string
  detailAddress: string; fullAddress: string
  postalCode: string; isDefault: number; label: string
  longitude: number; latitude: number; createTime: string
}

export interface CreateAddressForm {
  receiverName: string; receiverPhone: string
  province: string; provinceCode?: string
  city: string; cityCode?: string
  district: string; districtCode?: string
  detailAddress: string; postalCode?: string
  isDefault?: boolean; label?: string
}

export interface UpdateAddressForm extends Partial<CreateAddressForm> {}

// ── 社区相关 ────────────────────────────────────
export interface NoteResponse {
  id: number; userId: number; userNickname: string; userAvatar: string
  title: string; content?: string; coverUrl: string; status: number
  likeCount: number; commentCount: number; favoriteCount: number; viewCount: number
  images: string[]; products: ProductItem[]
  isLiked?: boolean; isFavorited?: boolean; createTime: string
}

export interface NoteImageItem { id: number; imageUrl: string; sortOrder: number }

export interface NoteDetailResponse extends NoteResponse {
  shareCount: number; locationName: string; updateTime: string
  images: NoteImageItem[]; isFollowing?: boolean
}

export interface ProductItem { id: number; name: string; mainImage: string; salePrice: number }

export interface CommentResponse {
  id: number; noteId: number; userId: number
  userNickname: string; userAvatar: string
  parentId: number | null; replyToUserId: number | null
  replyToUserNickname: string; content: string; likeCount: number
  children: CommentResponse[]; createTime: string
}

// ── 直播相关 ────────────────────────────────────
export interface LiveRoom {
  id: number; anchorId: number; title: string; cover: string; notice: string
  type: number; startTime: string; actualStartTime: string; endTime: string
  pushUrl: string; pullUrl: string; status: number
  onlineCount: number; totalViewCount: number; peakOnlineCount: number
  likeCount: number; giftCount: number; duration: number
  replayDuration: number; replayUrl: string
  anchorNickname: string; anchorAvatar: string
  createTime: string; updateTime: string
}

export interface LiveProduct {
  id: number; roomId: number; productId: number; skuId: number
  livePrice: number; sortOrder: number; status: number
  productName: string; productImage: string; originalPrice: number
}

export interface Gift {
  id: number; name: string; icon: string; animationUrl: string
  price: number; sortOrder: number
}

export interface CoinLog {
  id: number; userId: number; amount: number; type: number
  bizId: string; remark: string; createTime: string
}

export interface LiveMessage {
  id: number; roomId: number; userId: number; type: number
  content: string; giftId: number; giftCount: number
  nickname: string; avatar: string; createTime: string
}

// ── 轮播图相关 ────────────────────────────────────
export interface Banner {
  id: number; title: string; imageUrl: string; linkUrl: string
  sort: number; status: number
}

// ── 新品首发相关 ────────────────────────────────────
export interface NewProductBanner {
  id: number; title: string; imageUrl: string
  productId: number | null; linkUrl: string | null
  sort: number; status: number
  startTime: string | null; endTime: string | null
  createTime: string
}

export interface NewProductPageParams extends PageParams {
  categoryId?: number
  sortBy?: 'newest' | 'sort' | 'price_asc' | 'price_desc'
}

// ── 营销相关 ────────────────────────────────────
export interface Coupon {
  id: number; name: string; type: number
  minAmount: number; discountAmount: number; discountRate: number
  totalCount: number; receivedCount: number; usedCount: number
  perLimit: number; validDays: number; stackable: number
  useStartTime: string; useEndTime: string
  applicableType: number; applicableIds: string; integralCost: number
  description: string; status: number; createTime: string; updateTime: string
}

export interface CouponReceive {
  id: number; couponId: number; userId: number
  receiveTime: string; useTime: string
  orderId: number; orderNo: string; status: number
  couponName: string; couponType: number
  minAmount: number; discountAmount: number; discountRate: number
  useEndTime: string; createTime: string
  applicableType: number; applicableIds: string
}

export interface SeckillSession {
  id: number; name: string; startTime: string; endTime: string; status: number
}

export interface SeckillProduct {
  id: number; productId: number; productName: string; productImage: string
  originalPrice: number; seckillPrice: number; stock: number; soldCount: number
  sessionId: number; status: number
}

// ── 店铺入驻相关 ────────────────────────────────
export interface StoreApplyForm {
  storeName: string; storeLogo?: string; storeDescription?: string
}

export type * from './customer-service'
