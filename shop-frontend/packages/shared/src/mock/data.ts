// ============================================================
// ShopMax 演示数据 — HR 无需后端即可体验完整商城功能
// 启用方式: 设置环境变量 VITE_MOCK=true
// ============================================================

const now = new Date().toISOString()
const d = (days: number) => new Date(Date.now() - days * 86400000).toISOString()

// ── 用户 ──
export const demoUser = {
  userId: 1, username: 'demo', nickname: 'ShopMax体验官', avatar: '',
  phone: '138****8888', email: 'demo@shopmax.cn', gender: 1, birthday: '2000-01-01',
  status: 1, memberLevel: 3, memberLevelName: '黄金会员', integral: 2580, balance: 168.50,
  growthValue: 3580, lastLoginTime: now, role: 'USER',
  token: 'demo-token-mock'
}

// ── 分类 ──
export const categories = [
  { id: 1, name: '手机数码', icon: '', image: '', sort: 1, children: [
    { id: 11, name: '智能手机', icon: '', image: '', sort: 1, parentId: 1 },
    { id: 12, name: '耳机音箱', icon: '', image: '', sort: 2, parentId: 1 },
    { id: 13, name: '智能穿戴', icon: '', image: '', sort: 3, parentId: 1 },
  ]},
  { id: 2, name: '电脑办公', icon: '', image: '', sort: 2, children: [
    { id: 21, name: '笔记本', icon: '', image: '', sort: 1, parentId: 2 },
    { id: 22, name: '显示器', icon: '', image: '', sort: 2, parentId: 2 },
  ]},
  { id: 3, name: '服饰潮流', icon: '', image: '', sort: 3 },
  { id: 4, name: '美妆护肤', icon: '', image: '', sort: 4 },
  { id: 5, name: '家居生活', icon: '', image: '', sort: 5 },
  { id: 6, name: '食品生鲜', icon: '', image: '', sort: 6 },
]

// ── 商品 ──
const makeProduct = (id: number, name: string, price: number, catId: number, sales = 0) => ({
  id, name, salePrice: price, marketPrice: Math.round(price * 1.3),
  mainImage: `https://picsum.photos/seed/p${id}/400/400`,
  subImages: JSON.stringify([`https://picsum.photos/seed/p${id}a/400/400`, `https://picsum.photos/seed/p${id}b/400/400`]),
  categoryId: catId, brandId: 1, brandName: 'ShopMax精选',
  sales, stock: 999, status: 1, isNew: id <= 6, isRecommend: true,
  description: `<p>这是 ${name} 的详细描述。品质保证，售后无忧。</p>`,
  createTime: d(30), updateTime: d(1),
})

export const products = [
  makeProduct(1, 'iPhone 16 Pro Max 256GB 沙漠钛金属', 8999, 11, 15680),
  makeProduct(2, '华为 Mate 70 Pro+ 512GB 昆仑玻璃', 7999, 11, 23450),
  makeProduct(3, 'Samsung Galaxy S25 Ultra 512GB', 8999, 11, 8920),
  makeProduct(4, 'MacBook Pro 16" M4 Pro 芯片 24GB', 17999, 21, 4560),
  makeProduct(5, 'ThinkPad X1 Carbon Gen 13 2.8K', 12999, 21, 3210),
  makeProduct(6, 'AirPods Pro 3 主动降噪 USB-C', 1899, 12, 45680),
  makeProduct(7, 'Sony WH-1000XM6 无线降噪耳机', 2499, 12, 12340),
  makeProduct(8, 'Apple Watch Ultra 3 钛金属表壳', 6499, 13, 8900),
  makeProduct(9, 'Dell U4025QW 40" 5K 超宽曲面显示器', 9999, 22, 2100),
  makeProduct(10, '罗技 MX Master 4 无线鼠标', 799, 22, 18900),
  makeProduct(11, '春季新款男士休闲夹克', 399, 3, 8900),
  makeProduct(12, '兰蔻小黑瓶精华肌底液 50ml', 1080, 4, 23400),
  makeProduct(13, '戴森 V16 无线吸尘器', 4990, 5, 6700),
  makeProduct(14, '三只松鼠坚果大礼包 2kg', 159, 6, 56800),
  makeProduct(15, '索尼 PS6 游戏主机 国行', 4299, 1, 3400),
  makeProduct(16, '小米 16S Ultra 1TB 徕卡影像', 6999, 11, 12300),
]

// ── Banner ──
export const banners = [
  { id: 1, title: '新品首发', imageUrl: 'https://picsum.photos/seed/b1/1200/400', linkUrl: '/new-product', sort: 1, status: 1 },
  { id: 2, title: '限时秒杀', imageUrl: 'https://picsum.photos/seed/b2/1200/400', linkUrl: '/seckill', sort: 2, status: 1 },
  { id: 3, title: '直播专场', imageUrl: 'https://picsum.photos/seed/b3/1200/400', linkUrl: '/live', sort: 3, status: 1 },
]

// ── 社区笔记 ──
export const notes = [
  { id: 1, userId: 2, userNickname: '数码达人小王', userAvatar: '', title: 'iPhone 16 Pro Max 上手体验',
    content: '刚拿到手就被这个沙漠钛金属的质感惊艳到了！相比上一代，手感更轻，边框圆润了不少。相机按钮确实需要适应一下，但拍照效果提升明显。续航比 15 Pro Max 强了至少 2 小时，A18 Pro 芯片流畅度完全没话说。', coverUrl: 'https://picsum.photos/seed/n1/600/600',
    status: 1, likeCount: 234, commentCount: 18, favoriteCount: 56, viewCount: 3456, shareCount: 12,
    products: [{ id: 1, name: 'iPhone 16 Pro Max', mainImage: 'https://picsum.photos/seed/p1/200/200', salePrice: 8999 }],
    images: [{ id: 1, imageUrl: 'https://picsum.photos/seed/n1a/600/600', sortOrder: 0 }, { id: 2, imageUrl: 'https://picsum.photos/seed/n1b/600/600', sortOrder: 1 }],
    createTime: d(2), isLiked: false, isFavorited: false },
  { id: 2, userId: 3, userNickname: '美妆博主Lucy', userAvatar: '', title: '春季护肤routine分享',
    content: '最近换季皮肤状态不太稳定，调整了一下护肤步骤。重点推荐兰蔻小黑瓶，维稳效果真的很棒！用了两周后皮肤明显变细腻了。', coverUrl: 'https://picsum.photos/seed/n2/600/600',
    status: 1, likeCount: 567, commentCount: 42, favoriteCount: 189, viewCount: 8900, shareCount: 34,
    products: [{ id: 12, name: '兰蔻小黑瓶精华肌底液', mainImage: 'https://picsum.photos/seed/p12/200/200', salePrice: 1080 }],
    images: [{ id: 3, imageUrl: 'https://picsum.photos/seed/n2a/600/600', sortOrder: 0 }],
    createTime: d(5), isLiked: true, isFavorited: false },
  { id: 3, userId: 4, userNickname: '美食探店日记', userAvatar: '', title: '办公室零食推荐，打工人必备',
    content: '三只松鼠这个大礼包性价比太高了！里面坚果、果干搭配很均衡，2kg能吃一个月。独立小包装很方便带到公司。推荐给所有需要下午茶补充能量的打工人们！', coverUrl: 'https://picsum.photos/seed/n3/600/600',
    status: 1, likeCount: 189, commentCount: 23, favoriteCount: 67, viewCount: 2345, shareCount: 8,
    products: [{ id: 14, name: '三只松鼠坚果大礼包', mainImage: 'https://picsum.photos/seed/p14/200/200', salePrice: 159 }],
    images: [], createTime: d(8), isLiked: false, isFavorited: true },
]

// ── 评论 ──
export const makeComments = (noteId: number) => {
  if (noteId === 1) return [
    { id: 101, noteId: 1, userId: 5, userNickname: '科技爱好者', userAvatar: '', parentId: null, replyToUserId: null, replyToUserNickname: '', content: '续航真的这么强吗？我还在犹豫要不要换', likeCount: 12, createTime: d(1), children: [
      { id: 103, noteId: 1, userId: 2, userNickname: '数码达人小王', userAvatar: '', parentId: 101, replyToUserId: 5, replyToUserNickname: '科技爱好者', content: '真的！我从早上用到晚上还有30%', likeCount: 5, createTime: d(1), children: [] },
    ]},
    { id: 102, noteId: 1, userId: 6, userNickname: '小明聊数码', userAvatar: '', parentId: null, replyToUserId: null, replyToUserNickname: '', content: '沙漠钛金属比原色钛好看太多了', likeCount: 8, createTime: d(2), children: [] },
  ]
  if (noteId === 2) return [
    { id: 201, noteId: 2, userId: 7, userNickname: '护肤小白', userAvatar: '', parentId: null, replyToUserId: null, replyToUserNickname: '', content: '小黑瓶对油皮友好吗？', likeCount: 3, createTime: d(4), children: [
      { id: 202, noteId: 2, userId: 3, userNickname: '美妆博主Lucy', userAvatar: '', parentId: 201, replyToUserId: 7, replyToUserNickname: '护肤小白', content: '我是混油皮用着很清爽，吸收很快不油腻', likeCount: 7, createTime: d(4), children: [] },
    ]},
  ]
  return []
}

// ── 订单 ──
export const orders = [
  { id: 1, orderNo: 'SM202608010001', userId: 1, status: 3, payAmount: 8999, freight: 0, receiverName: '张三', receiverPhone: '138****8888',
    createTime: d(7), payTime: d(7), items: [{ id: 1, productId: 1, productName: 'iPhone 16 Pro Max', productImage: 'https://picsum.photos/seed/p1/200/200', price: 8999, quantity: 1 }] },
  { id: 2, orderNo: 'SM202608050002', userId: 1, status: 2, payAmount: 2298, freight: 0, receiverName: '张三', receiverPhone: '138****8888',
    createTime: d(3), payTime: d(3), items: [{ id: 2, productId: 6, productName: 'AirPods Pro 3', productImage: 'https://picsum.photos/seed/p6/200/200', price: 1899, quantity: 1 }, { id: 3, productId: 11, productName: '春季新款男士休闲夹克', productImage: 'https://picsum.photos/seed/p11/200/200', price: 399, quantity: 1 }] },
  { id: 3, orderNo: 'SM202608070003', userId: 1, status: 0, payAmount: 159, freight: 0, receiverName: '张三', receiverPhone: '138****8888',
    createTime: d(1), items: [{ id: 4, productId: 14, productName: '三只松鼠坚果大礼包', productImage: 'https://picsum.photos/seed/p14/200/200', price: 159, quantity: 1 }] },
]

// ── 优惠券 ──
export const coupons = [
  { id: 1, couponName: '新人满199减20', couponType: 1, discountAmount: 20, minAmount: 199, totalCount: 1000, receivedCount: 345, useStartTime: d(0), useEndTime: d(-365).replace(/T.*/, 'T23:59:59'), status: 1, integralCost: 0, applicableType: 1 },
  { id: 2, couponName: '数码全场9折', couponType: 2, discountRate: 0.9, minAmount: 500, totalCount: 500, receivedCount: 128, useStartTime: d(0), useEndTime: d(-365).replace(/T.*/, 'T23:59:59'), status: 1, integralCost: 0, applicableType: 1 },
  { id: 3, couponName: '满99免运费', couponType: 3, discountAmount: 10, minAmount: 99, totalCount: 2000, receivedCount: 567, useStartTime: d(0), useEndTime: d(-365).replace(/T.*/, 'T23:59:59'), status: 1, integralCost: 0, applicableType: 1 },
]

// ── 秒杀 ──
export const seckillSessions = [
  { id: 1, name: '10:00场', startTime: '10:00', endTime: '12:00', status: 1 },
  { id: 2, name: '14:00场', startTime: '14:00', endTime: '16:00', status: 1 },
  { id: 3, name: '20:00场', startTime: '20:00', endTime: '22:00', status: 1 },
]
export const seckillProducts = [
  { id: 1, productId: 6, productName: 'AirPods Pro 3', productImage: 'https://picsum.photos/seed/p6/400/400', seckillPrice: 1499, originalPrice: 1899, stock: 50, sales: 120, sessionId: 1 },
  { id: 2, productId: 10, productName: '罗技 MX Master 4', productImage: 'https://picsum.photos/seed/p10/400/400', seckillPrice: 599, originalPrice: 799, stock: 30, sales: 80, sessionId: 1 },
  { id: 3, productId: 12, productName: '兰蔻小黑瓶', productImage: 'https://picsum.photos/seed/p12/400/400', seckillPrice: 899, originalPrice: 1080, stock: 20, sales: 200, sessionId: 2 },
]

// ── 直播 ──
export const liveRooms = [
  { id: 1, anchorUserId: 10, anchorNickname: '数码大玩家', title: 'iPhone 16 系列深度体验', cover: 'https://picsum.photos/seed/l1/400/300', status: 1, onlineCount: 2345 },
  { id: 2, anchorUserId: 11, anchorNickname: '美妆一姐', title: '春季护肤好物推荐专场', cover: 'https://picsum.photos/seed/l2/400/300', status: 1, onlineCount: 5678 },
  { id: 3, anchorUserId: 12, anchorNickname: '零食试吃官', title: '办公室零食大测评', cover: 'https://picsum.photos/seed/l3/400/300', status: 0, onlineCount: 0 },
]

// ── 会员 ──
export const memberInfo = {
  memberLevel: 3, memberLevelName: '黄金会员', integral: 2580, balance: 168.50, growthValue: 3580,
  nextLevelGrowth: 5000, nextLevelName: '铂金会员',
  levelBenefits: [
    { level: 1, name: '普通会员', discount: '无折扣', threshold: 0 },
    { level: 2, name: '白银会员', discount: '98折', threshold: 1000 },
    { level: 3, name: '黄金会员', discount: '95折', threshold: 3000 },
    { level: 4, name: '铂金会员', discount: '92折', threshold: 5000 },
  ]
}

// ── 新品 Banner ──
export const newProductBanners = [
  { id: 1, title: '春季新品首发', imageUrl: 'https://picsum.photos/seed/nb1/1200/400', linkUrl: '/product/1', sort: 1 },
  { id: 2, title: '数码好物节', imageUrl: 'https://picsum.photos/seed/nb2/1200/400', linkUrl: '/product/4', sort: 2 },
]

// ── 收货地址 ──
export const addresses = [
  { addressId: 1, receiverName: '张三', receiverPhone: '13812348888', province: '广东省', city: '深圳市', district: '南山区', detailAddress: '科技园路88号创新大厦12层', fullAddress: '广东省深圳市南山区科技园路88号创新大厦12层', isDefault: true },
]
