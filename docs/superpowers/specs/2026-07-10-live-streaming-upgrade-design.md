# 直播功能升级设计方案

> 日期: 2026-07-10 | 状态: 设计完成

---

## 一、背景与目标

当前直播系统已实现基础功能（推流/拉流/弹幕/礼物/商品讲解），但存在以下不足：
- 弹幕消息类型不完整（缺进入/离开/关注/购买/系统广播）
- 缺少礼物动画特效和排行榜
- 无推荐算法，首页商品和直播列表为简单排序
- 商品详情页无直播引流入口
- 直播间内下单需跳转订单页，体验不流畅
- 无用户行为数据采集

**目标**: 打造抖音级别的直播电商体验，覆盖推荐、实时互动、购买闭环。

---

## 二、技术决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 推荐算法 | 规则引擎 + 热度排序 | 无需 ML 基础设施，Redis Sorted Set 即可满足 |
| 直播下单 | 半屏浮层下单 | 不打断直播观看，抖音/快手验证过的体验 |
| 数据埋点 | HTTP 异步上报 | 实现最简单，前端 POST 后不阻塞 |
| 平台范围 | Web PC 优先 | 先在 Web 端验证，Mobile 端后续跟进 |
| 礼物增强 | 充值 + 动画 + 排行榜 + 广播 | 完整变现闭环 |

---

## 三、五路推荐引擎

### 路1: 首页商品推荐

- **热度分公式**: `score = 销量分(0-50) + 评分分(0-20) + 新品分(0-15) + 类目偏好分(0-15)`
- **存储**: `Redis ZSET rec:product:hot`（全局）+ `rec:product:category:{catId}`（类目）
- **API**: `GET /api/v1/products/recommend?categoryId=&userId=&limit=20`
- **实现**: 定时任务(每分钟)计算热度分 → ZADD 写入 → API 查询时 ZREVRANGE + 用户标签过滤

### 路2: 直播信息流推荐

- **热度分公式**: `score = 在线分(0-40) + 互动分(礼物x2+点赞)(0-30) + 新开播分(0-15) + 类目匹配(0-15)`
- **存储**: `Redis ZSET rec:live:hot` + `rec:live:category:{catId}`
- **API**: `GET /api/v1/live/rooms/feed?categoryId=&userId=&limit=10&cursor=`
- **前端**: 抖音式竖屏全屏滑动，每卡片一个直播间 + 推荐商品预览

### 路3: 直播间内商品推荐

- **策略**: 当前直播商品(讲解中优先) + 同主播历史爆款 TOP5 + 同类目热销 TOP5
- **API**: `GET /api/v1/live/products/recommend?roomId=&userId=&limit=10`
- **展示**: 在直播间商品面板中以"为你推荐"分区展示

### 路4: 商品详情页引流直播间

- **关联索引**: `Redis Set live:product:{productId}:rooms → {roomId}` (SADD上架/SREM下架/讲解时提权)
- **API**: `GET /api/v1/live/rooms/by-product?productId=`
- **展示**: 商品详情页嵌入直播卡片（头像+在线人数+讲解标签），点击进入直播间

### 路5: 用户个性化

- **用户标签**: `Redis Hash user:{userId}:prefs → {catId: weight}` — 浏览+1, 购买+5, 停留30s+2
- **匹配策略**: 关注主播在播→置顶, 偏好类目商品→1.5x权重, 偏好类目直播→1.3x权重
- **融合**: 在推荐结果上叠加用户偏好权重

---

## 四、WebSocket 消息协议

### Client → Server

```json
{"type":"danmaku","content":"...","color":"#fff"}
{"type":"like"}
{"type":"gift","giftId":1,"count":1}
{"type":"heartbeat"}
{"type":"ping"}
```

### Server → Client (广播)

| type | 说明 | 关键字段 |
|------|------|---------|
| `danmaku` | 弹幕消息 | userId, nickname, content, color |
| `like` | 点赞(批量) | userId, totalLikes |
| `gift` | 礼物 | userId, nickname, giftName, giftIcon, count, combo, animationUrl |
| `enter` | 用户进入 | userId, nickname, onlineCount |
| `follow` | 关注主播 | userId, nickname |
| `purchase` | 购买通知 | userId, nickname, productName |
| `system` | 系统广播 | content, level(normal/broadcast) |
| `product` | 商品讲解/推荐 | action(explain/recommend), product{...} |
| `online` | 在线人数 | count |

---

## 五、直播间半屏购买流程

```
用户点击商品/抢购按钮
  → 底部滑出商品选择浮层（半屏）
    → 选择规格(SKU) / 数量
    → 可加入购物车 或 立即购买
      → 支付确认浮层（优惠券+积分+支付方式）
        → 确认支付
          → Feign调用订单服务创建订单
          → Feign调用支付服务
          → 支付成功 → 广播购买消息 → 关闭浮层
```

**关鍵点**:
- 直播价(`livePrice`)优先于商品原价
- 下单时校验库存(Feign调用 product-service)
- 优惠券叠加(Feign调用 marketing-service)
- 支付方式: 余额/微信/支付宝（复用现有 payment-service）

---

## 六、新增数据模型

### 数据库表

**lms_coin_recharge** — 虚拟币充值记录
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| coin_amount | int | 虚拟币数量 |
| pay_amount | decimal(10,2) | 支付金额(元) |
| pay_method | varchar(20) | balance/wechat/alipay |
| trade_no | varchar(64) | 支付流水号 |
| status | tinyint | 0处理中/1成功/2失败 |
| create_time | datetime | 创建时间 |

**lms_gift_rank** — 礼物排行榜
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| room_id | bigint | 直播间ID |
| user_id | bigint | 用户ID |
| gift_count | int | 礼物数量 |
| total_amount | int | 总虚拟币金额 |
| rank_type | tinyint | 1本场/2日榜/3周榜/4月榜 |
| rank_date | date | 排行日期 |
| rank_position | int | 排名 |
| create_time | datetime | 创建时间 |

**sys_user_behavior_log** — 用户行为日志
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| event_type | varchar(20) | browse/click/stay/purchase/like/share |
| target_type | varchar(20) | product/live/note |
| target_id | bigint | 目标ID |
| duration | int | 停留时长(秒), stay事件用 |
| create_time | datetime | 事件时间 |

### Redis 数据结构

| Key | 类型 | 用途 |
|-----|------|------|
| `rec:product:hot` | ZSET | 商品热度分 |
| `rec:product:category:{catId}` | ZSET | 类目商品热度 |
| `rec:live:hot` | ZSET | 直播热度分 |
| `rec:live:category:{catId}` | ZSET | 类目直播热度 |
| `live:product:{productId}:rooms` | SET | 商品关联直播房间 |
| `user:{userId}:prefs` | HASH | 用户类目偏好 |
| `live:gift:rank:{roomId}:{type}` | ZSET | 礼物排行榜 |

---

## 七、新增后端服务模块

| 组件 | 位置 | 职责 |
|------|------|------|
| RecommendService | shop-live-service | 五路推荐查询，热度分计算 |
| BehaviorController | shop-live-service | 用户行为事件采集(POST /api/v1/live/behavior) |
| BehaviorService | shop-live-service | 行为事件处理(写Redis偏好+写DB日志) |
| GiftRankService | shop-live-service | 排行榜计算与查询 |
| CoinRechargeService | shop-live-service | 虚拟币充值(对接payment-service Feign) |
| LiveMessageHandler(增强) | shop-live-service | 补充enter/follow/purchase/system/product消息处理 |
| LiveProductController(增强) | shop-live-service | 上架/下架时维护商品-直播Redis关联索引 |

**不新增独立推荐服务** — 推荐逻辑放在 shop-live-service 内，避免过度拆分。

---

## 八、前端改动范围 (Web PC 优先)

| 页面/组件 | 改动 |
|-----------|------|
| `LiveDanmaku.vue` | 支持 enter/system/gift 消息类型渲染 |
| `LiveGiftPanel.vue` | 增加充值入口按钮 |
| `LiveGiftAnimation.vue` | **新建** — Lottie 全屏礼物特效 + 连击数字 |
| `LiveProductFloat.vue` | 购买按钮触发半屏浮层 |
| `LivePurchasePanel.vue` | **新建** — 半屏购买浮层(SKU选择+数量+下单) |
| `LivePaymentPanel.vue` | **新建** — 支付确认浮层(支付方式+优惠券) |
| `LiveRankPanel.vue` | **新建** — 礼物排行榜侧边栏 |
| `live/room.vue` | 集成新组件，支持新消息类型 |
| `live/index.vue` | **重构** — 抖音式竖屏信息流 |
| `product/detail.vue` | 增加直播引流卡片（正在讲解该商品） |
| `home/index.vue` | 首页商品推荐接入推荐API |
| `useLiveSocket.ts` | 增加 connect/disconnect 后的 enter/leave 发送 |

---

## 九、参考：10 个额外功能（不在本次范围，供后续参考）

1. **直播预约提醒** — 预约未开播直播间，开播时推送通知
2. **直播回放+精彩片段** — 自动生成回放，倍速+商品时间轴锚点
3. **主播PK/连麦** — 双主播同时直播，观众投票/礼物决定胜负
4. **红包/福袋** — 主播发红包抽奖，提升停留时长
5. **直播截图** — FFmpeg 定时截图作为回放封面/分享图
6. **商品讲解回放锚点** — 记录商品讲解时间戳，回放可跳转
7. **分享裂变** — 邀请码+奖励机制
8. **热度任务** — 观看/发言/点赞达标触发奖励
9. **AI 智能讲解** — 语音识别生成字幕+商品摘要
10. **主播数据看板** — 实时观看趋势、商品点击率、收入画像

---

## 十、实施阶段

### 第一阶段: 核心体验 (本次)
- WebSocket 消息类型完善 (enter/follow/purchase/system/product)
- 半屏浮层下单 + 支付 (LivePurchasePanel, LivePaymentPanel)
- 礼物动画特效 (LiveGiftAnimation, Lottie)
- 直播间内商品推荐 (路3)
- 商品详情页引流直播入口 (路4)
- 行为数据采集 (BehaviorController)

### 第二阶段: 推荐 + 变现 (后续)
- 虚拟币充值
- 礼物排行榜 + 全站广播
- 首页商品推荐 (路1)
- 直播信息流推荐 (路2)
- 用户个性化 (路5)

### 第三阶段: 生态扩展 (远期)
- 直播预约提醒、回放+商品锚点、红包/福袋、分享裂变等

---

## 十一、验证方式

1. **弹幕**: 进入直播间 — 弹幕滚动 + 进入消息广播 + 在线人数更新
2. **礼物**: 发送礼物 — 全屏 Lottie 动画 + 连击数字 + 消息广播
3. **半屏下单**: 点击商品 — 浮层滑出 → 选规格 → 下单 → 支付 → 购买广播
4. **商品引流**: 商品详情页出现"正在直播讲解"卡片 → 点击进入直播间
5. **行为埋点**: 浏览/点击商品 — POST /api/v1/live/behavior — Redis 偏好更新
6. **推荐**: 首页商品/直播列表按热度分排序，非简单时间倒序
