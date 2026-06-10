# ShopMax 前端重设计规范

## 1. 概述

将 ShopMax C 端从单一 UniApp 移动应用重做为 **Web 优先 + 多端适配** 的前端体系，解决 UI 混乱、布局不合理、导航越界等问题。

### 1.1 平台目标

| 平台 | 框架 | UI 库 | 优先级 |
|------|------|-------|--------|
| Web (H5) | Vue 3 + Vite + Vue Router | Naive UI | 主力开发 |
| 微信小程序 | UniApp 3 + Vue 3 | Uni UI | 同步交付 |
| App (iOS/Android) | UniApp 3 + Vue 3 | Uni UI | 同步交付 |

### 1.2 核心原则

- **Web 优先设计**：先做桌面版网站，再通过响应式适配移动端
- **共享数据层**：API 调用和 Pinia Store 通过 `@shop/shared` 包跨端复用
- **独立 UI 层**：Web 用 Naive UI，Mobile 用 Uni UI，各自按平台最佳实践设计
- **共享后端 API**：继续使用现有 Spring Boot 网关和服务，无需后端改动

---

## 2. 项目结构

```
shop-frontend/                        ← 新建仓库
├── pnpm-workspace.yaml
├── package.json                      ← scripts: dev:web, dev:mobile, build:web, build:mobile
├── tsconfig.base.json
│
├── packages/
│   ├── shared/                       ← @shop/shared
│   │   ├── src/
│   │   │   ├── api/                  ← 11 个 API 模块（从 shop-mobile/src/api 迁移）
│   │   │   ├── stores/               ← Pinia stores（user, cart，从 shop-mobile/src/stores 迁移）
│   │   │   ├── types/                ← TypeScript 类型定义
│   │   │   └── utils/                ← 工具函数（格式化、校验）
│   │   └── index.ts                  ← 统一导出
│   │
│   ├── web/                          ← @shop/web（Web 端）
│   │   ├── src/
│   │   │   ├── router/               ← Vue Router 路由
│   │   │   ├── pages/                ← 页面组件
│   │   │   ├── components/           ← 业务组件
│   │   │   ├── layouts/              ← DefaultLayout / BlankLayout
│   │   │   ├── composables/          ← Vue composables
│   │   │   └── assets/               ← 静态资源
│   │   └── index.html
│   │
│   └── mobile/                       ← @shop/mobile（UniApp）
│       ├── src/
│       │   ├── pages/                ← UniApp 页面
│       │   ├── components/           ← Uni UI 组件
│       │   └── pages.json            ← 路由 + TabBar 配置
│       └── manifest.json
```

---

## 3. 依赖关系

```
@shop/shared  ← API + Stores + Types，被 Web 和 Mobile 共同依赖
    ↑                    ↑
@shop/web          @shop/mobile
(Vue 3 + Naive)    (UniApp + Uni UI)
```

**Shared 包含**：API 函数、Pinia Store 定义、TypeScript 类型、工具函数  
**Shared 不包含**：Vue 组件、路由配置、样式、HTTP Client 实例

---

## 4. Web 端设计

### 4.1 技术选型

| 层 | 技术 |
|----|------|
| 框架 | Vue 3.4 + TypeScript 5.4 |
| 构建 | Vite 5 |
| UI 库 | Naive UI（按需 Tree Shaking） |
| 路由 | Vue Router 4 |
| 状态 | Pinia + pinia-plugin-persistedstate |
| HTTP | Axios |
| CSS | SCSS + CSS Variables |
| 图标 | Naive UI Icons + 自定义 SVG |

### 4.2 整体布局

```
┌─────────────────────────────────────────────────────┐
│  顶部导航栏                                          │
│  [Logo] [  搜索框（带联想下拉）  ] [消息] [购物车] [用户] │
├──────────┬──────────────────────────────────────────┤
│          │  轮播 Banner              │ 快报 / 公告   │
│  左侧    ├──────────────────────────────────────────┤
│  分类树  │  ⚡秒杀 👑会员 🔥百补 💬社区 📺直播 🎫领券  │
│          ├──────────────────────────────────────────┤
│  一级    │                                          │
│  分类    │  4 列商品瀑布流                            │
│  hover   │  (图 / 标题 / 价格 / 已售 / 店铺)          │
│  展开    │                                          │
│  二级    │  —— 滚动加载更多 ——                       │
│  面板    │                                          │
├──────────┴──────────────────────────────────────────┤
│  底部页脚（关于 / 帮助 / 备案号）                      │
└─────────────────────────────────────────────────────┘
```

### 4.3 关键交互

- **顶部导航**：固定定位，滚动不消失，背景半透明毛玻璃
- **分类 hover**：鼠标悬停一级分类 → 右侧弹出多列二级/三级面板（含品牌、推荐商品）
- **搜索联想**：输入时下拉展示联想词 + 历史搜索 + 热门推荐
- **购物车面板**：hover 购物车图标 → 右侧滑出 320px 面板（商品列表 / 快速删除 / 去结算）
- **商品卡片**：hover 放大 1.02x + 阴影加深 + 显示「快速加购」按钮
- **加购动画**：点击加购 → 商品图缩小飞入购物车图标（抛物线动画）
- **回到顶部**：滚动超过 800px 显示右下角按钮
- **客服悬浮**：右下角固定客服图标

### 4.4 页面清单

| 路由 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | Banner + 分类 + 金刚区 + 瀑布流 + 推荐 |
| `/category/:id?` | 分类页 | 左侧分类树 + 右侧商品列表 |
| `/search?q=` | 搜索结果 | 筛选 + 排序 + 商品网格 |
| `/product/:id` | 商品详情 | 图片 + SKU + 参数 + 评价 + 推荐 |
| `/cart` | 购物车 | 商品列表 + 总计 + 结算 |
| `/order/confirm` | 确认订单 | 地址 + 商品 + 总计 + 提交 |
| `/order/list` | 订单列表 | 状态 Tab + 订单卡片 |
| `/order/:id` | 订单详情 | 进度 + 商品 + 物流 |
| `/user` | 个人中心 | 资料 + 订单入口 + 设置 |
| `/user/profile` | 编辑资料 | 头像 + 昵称 + 手机 |
| `/user/address` | 收货地址 | 列表 + 新增/编辑 |
| `/community` | 社区 | 笔记列表（左侧入口或金刚区） |
| `/live` | 直播 | 直播间列表 |
| `/login` | 登录 | 账号密码 / 手机验证码 |
| `/register` | 注册 | 手机 + 密码 + 验证码 |

### 4.5 响应式策略

| 断点 | 宽度 | 行为 |
|------|------|------|
| 手机 | < 768px | 全宽，顶部简化导航，商品 2 列 |
| 平板 | 768-1199px | 居中 max-width:720px，商品 3 列，顶部下拉分类 |
| 桌面 | ≥ 1200px | 完整布局：max-width:1440px 居中，左侧分类 + 4 列商品 |

---

## 5. 移动端设计

### 5.1 技术选型

| 层 | 技术 |
|----|------|
| 框架 | UniApp 3 + Vue 3 |
| UI 库 | Uni UI（替换 uview-plus） |
| 状态 | Pinia（复用 @shop/shared 的 Store 定义） |
| HTTP | uni.request 封装（与 shared 接口一致） |

### 5.2 整体布局

```
┌──────────────────────┐
│  顶部导航（固定）      │  ← Logo / 城市 + 居中搜索框（支持语音/扫码）
├──────────────────────┤
│  功能入口 横向滑动     │  ← ⚡秒杀 👑会员 💬社区 📺直播 🎫领券
├──────────────────────┤
│  Banner 轮播          │
├──────────────────────┤
│  商品瀑布流（2 列）    │
│  ┌─────┐ ┌─────┐     │
│  │ 卡片 │ │ 卡片 │     │
│  └─────┘ └─────┘     │
│  ── 滚动加载更多 ──    │
├──────────────────────┤
│  TabBar（固定）        │  ← 🏠首页 │ 📂分类 │ 🛒购物车 │ 👤我的
└──────────────────────┘
```

### 5.3 TabBar 结构

4 个 tab（从当前 6 个精简）：

| Tab | 路径 | 图标 |
|-----|------|------|
| 首页 | pages/index/index | 🏠 |
| 分类 | pages/category/index | 📂 |
| 购物车 | pages/cart/index | 🛒 |
| 我的 | pages/user/index | 👤 |

社区和直播通过首页金刚区图标入口进入独立子页面，不再占据 TabBar 位置。

### 5.4 页面清单

与当前 mobile 端页面基本一致，UI 全部重写：

| 页面 | 变化 |
|------|------|
| 首页 | 新版：顶部搜索 + 金刚区 + Banner + 瀑布流 |
| 分类页 | 新版：左侧抽屉或顶部 Tab 切换 |
| 商品详情 | 新版：图 + 规格 + 参数 + 评价 |
| 购物车 | 新版：卡片式 + 底部结算栏 |
| 订单列表/确认 | 新版：状态 Tab + 卡片 |
| 个人中心 | 新版：头部卡片 + 功能菜单 |
| 社区/直播 | 独立子页面，金刚区入口 |
| 搜索 | 新版：搜索历史 + 联想 + 结果页 |
| 地址管理 | 保持现有逻辑，UI 刷新 |
| 登录/注册 | 简化，手机验证码优先 |
| 店铺入驻 | 保留现有状态机流程，UI 刷新 |

---

## 6. Shared 包设计

### 6.1 API 模块

| 模块 | 文件 | 端点数量 |
|------|------|----------|
| user | api/user.ts | 9 |
| product | api/product.ts | 6 |
| order | api/order.ts | 6 |
| address | api/address.ts | 7 |
| community | api/community.ts | 12 |
| coupon | api/coupon.ts | 3 |
| live | api/live.ts | 3 |
| seckill | api/seckill.ts | 3 |
| category | api/product.ts (categoryApi) | 2 |

共 8 个模块，51 个端点。从当前 shop-mobile/src/api 迁移，统一 TypeScript 类型。

### 6.2 Pinia Stores

| Store | 持久化 | 跨端共享 |
|-------|--------|----------|
| useUserStore | localStorage / uni.storage | 共享定义，适配器不同 |
| useCartStore | persist: true | Web 用 localStorage，Mobile 用 uni.storage |

### 6.3 跨端适配器

Web 和 Mobile 对 storage 的实现不同（localStorage vs uni.getStorageSync）。Shared 包的 Store 使用注入的 storage 适配器：

```typescript
// @shop/shared
export function createUserStore(storage: StorageAdapter) { ... }

// @shop/web
import { createUserStore } from '@shop/shared'
export const useUserStore = createUserStore(webStorageAdapter)

// @shop/mobile  
import { createUserStore } from '@shop/shared'
export const useUserStore = createUserStore(uniStorageAdapter)
```

---

## 7. 视觉规范

### 7.1 色彩

| 用途 | 色值 | 说明 |
|------|------|------|
| 品牌主色 | #FF5000 | 按钮、标签、价格 |
| 主色渐变 | #FF5000 → #FF9000 | 导航、Banner、强调 |
| 成功 | #00B578 | 支付成功、已发货 |
| 警告 | #FF8F1F | 待付款、库存紧张 |
| 危险 | #FF3B3B | 已取消、已退款 |
| 文字主 | #1C1C1E | 标题、正文 |
| 文字辅 | #8E8E93 | 描述、提示 |
| 背景 | #F2F2F7 | 页面底色 |
| 卡片 | #FFFFFF | 卡片、面板 |
| 边框 | #E5E5EA | 分割线、边框 |

### 7.2 圆角

| 用途 | 值 |
|------|-----|
| 小元素（标签、徽章） | 4px |
| 卡片、按钮 | 12px |
| 大面板、弹窗 | 16px |
| 圆形（头像、图标） | 50% |

### 7.3 阴影

| 层级 | 值 |
|------|-----|
| 卡片 | 0 2px 8px rgba(0,0,0,0.06) |
| 悬浮面板 | 0 4px 16px rgba(0,0,0,0.10) |
| 弹窗 | 0 8px 32px rgba(0,0,0,0.14) |

### 7.4 间距

采用 4px 基准的间距体系：4, 8, 12, 16, 20, 24, 32, 48

---

## 8. 动画规范

| 场景 | 动画 | 时长 |
|------|------|------|
| 路由切换 | fade + 轻微位移 | 200ms |
| 分类面板展开 | slide + fade | 200ms ease-out |
| 商品卡片 hover | scale(1.02) + shadow | 150ms |
| 加购飞入 | 贝塞尔抛物线 + scale(0.3→1) | 400ms |
| 购物车角标更新 | scale(1.3→1) bounce | 300ms |
| 弹窗 | scale(0.95→1) + fade | 200ms |
| 骨架屏 | shimmer 动画 | 1.5s 循环 |

---

## 9. 实施计划

### Phase 1: 基础设施

1. 初始化 pnpm monorepo（workspace + tsconfig）
2. 创建 @shop/shared 包（API + Stores + Types 迁移）
3. 创建 @shop/web 脚手架（Vite + Naive UI + Vue Router）
4. 创建 @shop/mobile 脚手架（UniApp + Uni UI，替换 uview-plus）

### Phase 2: Web 端核心页面

1. DefaultLayout（顶部导航 + 左侧分类 + 底部页脚）
2. 首页（Banner + 金刚区 + 商品瀑布流）
3. 商品详情页（图 + SKU + 参数 + 评价）
4. 购物车 + 订单确认 + 订单列表
5. 登录/注册

### Phase 3: Web 端次要页面

1. 分类页 + 搜索页
2. 个人中心 + 地址管理 + 资料编辑
3. 社区 + 直播
4. 店铺入驻

### Phase 4: 移动端

1. 基于 @shop/shared 重写全部页面 UI
2. 移除 uview-plus，替换为 Uni UI
3. 统一导航栏适配
4. TabBar 从 6 个精简到 4 个

### Phase 5: 交互打磨

1. 动画（加购、切换、hover）
2. 骨架屏 + 空状态 + 错误状态
3. 响应式适配验证
4. 性能优化

---

## 10. 验证标准

- [ ] Web 端在 Chrome/Safari/Edge 桌面端正常显示
- [ ] Web 端在 375px-1440px 范围无布局错乱
- [ ] 移动端 H5 在 iOS Safari / Android Chrome 正常
- [ ] 微信小程序编译通过，功能正常
- [ ] 登录状态在 Web 和 Mobile 端互通（同一后端）
- [ ] 购物车数据跨端同步（登录后从后端恢复）
- [ ] 所有 API 错误有适当的 UI 反馈
- [ ] 页面加载时间 < 2s（首屏）
