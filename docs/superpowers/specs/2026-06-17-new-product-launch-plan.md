# 新品首发 - 实施计划

基于设计文档：`2026-06-17-new-product-launch-design.md`

---

## 前置说明

- 移动端路径：`shop-frontend/packages/mobile/`（非 `shop-mobile/`）
- Admin 控制器放在 `shop-admin-service`（gateway 已路由 `/api/v1/admin/**`）
- C 端控制器放在 `shop-product-service`（gateway 已路由 `/api/v1/products/**`）
- 所有服务共享同一数据库，各服务可直接查询所需表
- C 端 Banner 接口路径调整为 `/api/v1/products/new-product-banners`（复用 gateway 已有的 `/api/v1/products/**` 路由）

---

## Phase 1：数据库迁移

### Task 1.1：创建迁移脚本

文件：`docs/sql/migration_007_new_product.sql`

内容：
- `ALTER TABLE pms_product` 新增 3 个字段
- 新增索引 `idx_product_new_sort`
- `CREATE TABLE pms_new_product_banner`
- 插入测试 Banner 数据

---

## Phase 2：后端 - Product 实体扩展

### Task 2.1：Product 实体新增字段

文件：`shop-backend/shop-modules/shop-product-service/.../entity/Product.java`

新增字段：
- `private Integer newProductSort;`
- `private LocalDateTime newProductStartTime;`
- `private LocalDateTime newProductEndTime;`

### Task 2.2：ProductService 新增新品分页查询

文件：`shop-backend/shop-modules/shop-product-service/.../service/ProductService.java`

新增方法：
- `PageResult<Product> pageNew(Integer pageNum, Integer pageSize, Long categoryId, String sortBy)`

文件：`shop-backend/shop-modules/shop-product-service/.../service/impl/ProductServiceImpl.java`

实现逻辑：
- `is_new = 1 AND status = 1 AND deleted = 0`
- 时间过滤：`(start_time IS NULL OR start_time <= NOW()) AND (end_time IS NULL OR end_time >= NOW())`
- 排序：`newest` → `create_time DESC`，`sort` → `new_product_sort DESC, create_time DESC`，`price_asc/price_desc`
- 使用 MyBatis-Plus `Page` + `LambdaQueryWrapper`
- 清除 `product:new:` 缓存前缀

### Task 2.3：ProductController 新增 C 端接口

文件：`shop-backend/shop-modules/shop-product-service/.../controller/ProductController.java`

新增端点：
- `GET /api/v1/products/new-page` → 调用 `pageNew()`

---

## Phase 3：后端 - 新品 Banner 模块（C 端）

### Task 3.1：NewProductBanner 实体

文件（新建）：`shop-backend/shop-modules/shop-product-service/.../entity/NewProductBanner.java`

对应表 `pms_new_product_banner`，标准实体模式。

### Task 3.2：NewProductBannerMapper

文件（新建）：`shop-backend/shop-modules/shop-product-service/.../mapper/NewProductBannerMapper.java`

继承 `BaseMapper<NewProductBanner>`，无自定义 SQL。

### Task 3.3：NewProductBannerController（C 端）

文件（新建）：`shop-backend/shop-modules/shop-product-service/.../controller/NewProductBannerController.java`

- `GET /api/v1/products/new-product-banners`
- 查询条件：`status=1, deleted=0`，时间范围过滤，按 `sort DESC` 排序
- 返回 `Result<List<NewProductBanner>>`

---

## Phase 4：后端 - Admin 管理接口

### Task 4.1：Admin Product Mapper（新品管理查询）

文件（新建）：`shop-backend/shop-modules/shop-admin-service/.../mapper/NewProductMapper.java`

自定义 `@Select` 查询：
- 新品分页列表（联表查分类名）
- 新品统计（总数、进行中、即将过期、今日新增）

### Task 4.2：NewProductAdminController

文件（新建）：`shop-backend/shop-modules/shop-admin-service/.../controller/NewProductAdminController.java`

路径前缀：`/api/v1/admin/products`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/new` | 新品分页列表 |
| PUT | `/new/batch-mark` | 批量标记新品 |
| PUT | `/new/batch-unmark` | 批量取消新品 |
| PUT | `/{id}/new-settings` | 更新新品排序/时间 |
| GET | `/new/stats` | 新品统计 |

### Task 4.3：NewProductBanner 实体（Admin 端）

文件（新建）：`shop-backend/shop-modules/shop-admin-service/.../entity/NewProductBanner.java`

与 C 端实体相同定义（各服务独立定义实体，共享数据库表）。

### Task 4.4：NewProductBannerMapper（Admin 端）

文件（新建）：`shop-backend/shop-modules/shop-admin-service/.../mapper/NewProductBannerMapper.java`

### Task 4.5：NewProductBannerAdminController

文件（新建）：`shop-backend/shop-modules/shop-admin-service/.../controller/NewProductBannerAdminController.java`

路径前缀：`/api/v1/admin/new-product-banners`

标准 CRUD 4 个接口，`@PreAuthorize("hasRole('ADMIN')")`。

### Task 4.6：Gateway 路由无需修改

`/api/v1/admin/**` 已路由到 `shop-admin-service`，`/api/v1/products/**` 已路由到 `shop-product-service`。

### Task 4.7：SecurityConfig 无需修改

C 端 Banner 接口 `GET /api/v1/products/new-product-banners` 已被 `GET /api/v1/products/**` 的公开规则覆盖。

---

## Phase 5：管理后台前端

### Task 5.1：新增 API 函数

文件：`shop-admin-ui/src/api/modules/product.ts`

新增接口类型：
- `NewProductBanner`（id, title, imageUrl, productId, linkUrl, sort, status, startTime, endTime）

新增 API 函数：
- `getNewProductList(params)` → GET `/api/v1/admin/products/new`
- `batchMarkNew(ids)` → PUT `/api/v1/admin/products/new/batch-mark`
- `batchUnmarkNew(ids)` → PUT `/api/v1/admin/products/new/batch-unmark`
- `updateNewProductSettings(id, data)` → PUT `/api/v1/admin/products/{id}/new-settings`
- `getNewProductStats()` → GET `/api/v1/admin/products/new/stats`
- `getNewProductBanners(params)` → GET `/api/v1/admin/new-product-banners`
- `createNewProductBanner(data)` → POST `/api/v1/admin/new-product-banners`
- `updateNewProductBanner(id, data)` → PUT `/api/v1/admin/new-product-banners/{id}`
- `deleteNewProductBanner(id)` → DELETE `/api/v1/admin/new-product-banners/{id}`

### Task 5.2：新增路由

文件：`shop-admin-ui/src/router/routes.ts`

在 children 数组中新增：
```
{
  path: '/product/new-product',
  name: 'NewProduct',
  component: () => import('@/views/product/new-product/index.vue'),
  meta: { title: '新品管理', icon: 'GoodsFilled', roles: ['ADMIN'] }
}
```

### Task 5.3：新品管理页面

文件（新建）：`shop-admin-ui/src/views/product/new-product/index.vue`

参考 `marketing/index.vue` 的 Tab 模式，包含三个 Tab：

**Tab 1 - 新品商品：**
- 统计卡片行（4 个 el-card）
- 搜索栏（el-input + el-select + el-select）
- 操作栏（添加商品按钮 + 批量取消按钮）
- el-table + el-pagination
- 编辑新品设置弹窗（el-dialog + el-form）

**Tab 2 - Banner 管理：**
- 操作栏（新增 Banner 按钮）
- el-table + el-pagination
- 新增/编辑 Banner 弹窗（el-dialog + el-form + el-upload + 商品远程搜索）

**Tab 3 - 数据概览：**
- 两个 ECharts 图表（折线图 + 饼图）

### Task 5.4：添加商品选择对话框

在新品商品 Tab 中，点击「添加商品」弹出商品选择对话框：
- el-dialog 内包含 el-table（展示非新品商品列表）
- 支持 checkbox 多选
- 支持关键词搜索
- 确认后调用 `batchMarkNew` 接口

---

## Phase 6：移动端前端

### Task 6.1：新增 API 函数

文件：`shop-frontend/packages/shared/src/api/product.ts`

新增：
- `productApi.getNewPage(params)` → GET `/api/v1/products/new-page`
- `newProductBannerApi.getList()` → GET `/api/v1/products/new-product-banners`

文件：`shop-frontend/packages/shared/src/types/index.ts`

新增：
- `NewProductBanner` 接口类型
- `NewProductPageParams` 类型（扩展 `PageParams` + `categoryId` + `sortBy`）

### Task 6.2：新增新品首发页面

文件（新建）：`shop-frontend/packages/mobile/src/pages/new-product/index.vue`

页面结构：
1. 自定义导航栏（标题「新品首发」+ 返回箭头）
2. Banner swiper 轮播（圆角卡片，autoplay）
3. 分类 Tab（scroll-view 横向滚动，选中态样式）
4. 商品双列网格（复用首页 card 样式，左上角 NEW 角标）
5. 空状态（无新品时显示）
6. 上拉加载更多 + 下拉刷新

### Task 6.3：注册路由

文件：`shop-frontend/packages/mobile/src/pages.json`

在 pages 数组中新增：
```json
{
  "path": "pages/new-product/index",
  "style": {
    "navigationBarTitleText": "新品首发",
    "navigationStyle": "custom"
  }
}
```

### Task 6.4：接入首页金刚区入口

文件：`shop-frontend/packages/mobile/src/pages/index/index.vue`

修改「新品」图标的点击事件，从 `toast('即将开放')` 改为 `uni.navigateTo({ url: '/pages/new-product/index' })`。

---

## Phase 7：联调验证

### Task 7.1：后端验证

- 执行 migration SQL
- 启动服务，验证 Admin 新品管理接口（CRUD、批量操作、统计）
- 验证 Admin Banner 管理接口
- 验证 C 端新品分页接口（时间过滤、分类筛选、排序）
- 验证 C 端 Banner 列表接口

### Task 7.2：管理后台验证

- 新品商品 Tab：列表、搜索、添加商品、编辑设置、批量取消
- Banner 管理 Tab：新增、编辑、删除、状态开关
- 数据概览 Tab：图表展示

### Task 7.3：移动端验证

- 首页金刚区「新品」→ 跳转新品页面
- Banner 轮播展示和点击跳转
- 分类 Tab 切换和商品列表更新
- 上拉加载更多、下拉刷新
- 空状态展示

---

## 实施顺序

```
Phase 1 (数据库) → Phase 2 (Product 扩展) → Phase 3 (C 端 Banner) → Phase 4 (Admin 接口)
                                                                                ↓
Phase 5 (Admin 前端) ←──────────────────────────────────────────────────────────┘
                                                                                ↓
Phase 6 (移动端前端) → Phase 7 (联调)
```

Phase 2/3/4 可并行开发（无依赖）。Phase 5 依赖 Phase 4 完成。Phase 6 依赖 Phase 2/3 完成。
