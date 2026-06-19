# 新品首发页面设计方案

- 日期：2026-06-17
- 状态：已确认
- 范围：后端 API + 管理后台（Vue3 + Element Plus）+ 移动端（UniApp）

---

## 1. 背景与目标

ShopMax 电商平台的「新品首发」功能目前只有占位入口：

- 移动端首页金刚区「新品首发」图标点击后提示「功能尚在开发中」
- Web 端首页「新品首发」图标点击后提示「功能筹备中，敬请期待」
- 后端已有 `pms_product.is_new` 字段和 `/api/v1/products/new` 接口，但无专用管理页面
- Admin 后台商品管理中不暴露 `isNew` 字段的编辑和展示

目标：实现完整的新品首发功能，包含 C 端展示页面和 Admin 运营管理后台。

---

## 2. 数据模型设计

### 2.1 pms_product 表新增字段

在现有 `is_new` 基础上新增 3 个字段，控制新品生命周期：

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `new_product_sort` | int | 0 | 新品排序权重（数值越大越靠前） |
| `new_product_start_time` | datetime | NULL | 新品上架时间（为空则永久展示） |
| `new_product_end_time` | datetime | NULL | 新品下架时间（为空则不自动过期） |

展示逻辑：`is_new=1` 且当前时间在 `start_time ~ end_time` 范围内才展示为新品。两个时间字段都为空则永久展示。

迁移 SQL：

```sql
ALTER TABLE pms_product
    ADD COLUMN `new_product_sort` int NOT NULL DEFAULT 0 COMMENT '新品排序权重' AFTER `is_new`,
    ADD COLUMN `new_product_start_time` datetime DEFAULT NULL COMMENT '新品上架时间' AFTER `new_product_sort`,
    ADD COLUMN `new_product_end_time` datetime DEFAULT NULL COMMENT '新品下架时间' AFTER `new_product_start_time`;

ALTER TABLE pms_product ADD INDEX idx_product_new_sort (`deleted`, `status`, `is_new`, `new_product_sort`);
```

### 2.2 新增 pms_new_product_banner 表

```sql
CREATE TABLE IF NOT EXISTS `pms_new_product_banner` (
    `id`             bigint NOT NULL AUTO_INCREMENT,
    `title`          varchar(128) NOT NULL COMMENT 'Banner标题',
    `image_url`      varchar(500) NOT NULL COMMENT 'Banner图片',
    `product_id`     bigint DEFAULT NULL COMMENT '关联商品ID（点击跳转商品详情）',
    `link_url`       varchar(500) DEFAULT NULL COMMENT '外部链接（与product_id二选一）',
    `sort`           int NOT NULL DEFAULT 0 COMMENT '排序',
    `status`         tinyint NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    `start_time`     datetime DEFAULT NULL COMMENT '展示开始时间',
    `end_time`       datetime DEFAULT NULL COMMENT '展示结束时间',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新品首发Banner推荐位';
```

---

## 3. 后端 API 设计

### 3.1 新品管理接口（Admin，需 ADMIN 角色）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 新品分页列表 | GET | `/api/v1/admin/products/new` | 支持分类筛选、状态筛选、关键词搜索、分页 |
| 批量标记新品 | PUT | `/api/v1/admin/products/new/batch-mark` | body: `{ ids: [1,2,3] }` |
| 批量取消新品 | PUT | `/api/v1/admin/products/new/batch-unmark` | body: `{ ids: [1,2,3] }` |
| 更新新品设置 | PUT | `/api/v1/admin/products/{id}/new-settings` | body: `{ sort, startTime, endTime }` |
| 新品统计 | GET | `/api/v1/admin/products/new/stats` | 返回总数、即将过期、今日新增 |

### 3.2 Banner 管理接口（Admin，需 ADMIN 角色）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| Banner 列表 | GET | `/api/v1/admin/new-product-banners` | 分页查询 |
| 新增 Banner | POST | `/api/v1/admin/new-product-banners` | 创建 Banner |
| 编辑 Banner | PUT | `/api/v1/admin/new-product-banners/{id}` | 修改 Banner |
| 删除 Banner | DELETE | `/api/v1/admin/new-product-banners/{id}` | 逻辑删除 |

### 3.3 C 端展示接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 新品 Banner 列表 | GET | `/api/v1/new-product-banners` | 返回 `status=1` 且当前时间在 `start_time~end_time` 范围内的 Banner（时间为空视为永久有效），按 `sort DESC` 排序 |
| 新品分页列表 | GET | `/api/v1/products/new-page` | 分页 + 分类筛选 + 排序，仅返回 `is_new=1` 且当前时间在 `start_time~end_time` 范围内的商品（时间为空视为永久有效） |

请求参数（`/api/v1/products/new-page`）：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页数量，默认 20 |
| categoryId | Long | 否 | 分类筛选 |
| sortBy | String | 否 | 排序：`newest`(默认), `sort`, `price_asc`, `price_desc` |

响应：标准 `PageResult<Product>` 格式，商品包含 `isNew`、`newProductSort` 字段。

### 3.4 Controller 拆分

- `NewProductAdminController`：Admin 端新品商品管理（5 个接口）
- `NewProductBannerAdminController`：Admin 端 Banner 管理（4 个接口）
- `NewProductBannerController`：C 端 Banner 查询（1 个接口）
- `ProductController` 新增 `/new-page` 端点：C 端新品分页列表（1 个接口）

---

## 4. 管理后台 UI 设计（Vue3 + Element Plus）

### 4.1 导航结构

侧边栏「商品管理」下新增子菜单「新品管理」：

```
商品管理
  ├── 商品列表      （已有）
  ├── 分类管理      （已有）
  ├── 品牌管理      （已有）
  └── 新品管理      （新增）→ /product/new-product
```

路由角色：`['ADMIN']`

### 4.2 页面布局

三个 Tab 页签：**新品商品** / **Banner 管理** / **数据概览**

**Tab 1 - 新品商品：**

- 顶部统计卡片行：新品总数 / 进行中 / 即将过期（7天内） / 今日新增
- 搜索栏：关键词输入框、分类下拉、时间状态筛选（全部/进行中/即将过期/已过期）
- 操作栏：「添加商品」按钮（弹出商品选择对话框，从非新品商品中勾选）、「批量取消新品」按钮
- 表格列：商品图片（缩略图）、商品名称、分类、价格、新品排序、上架时间、下架时间、状态标签（进行中/即将过期/已过期/永久）、操作
- 操作列：编辑新品设置（弹窗修改排序和时间）、取消新品（确认对话框）
- 编辑弹窗字段：排序权重（数字输入）、上架时间（日期时间选择器）、下架时间（日期时间选择器）
- 分页组件

**Tab 2 - Banner 管理：**

- 操作栏：「新增 Banner」按钮
- 表格列：Banner 预览图（缩略图）、标题、关联商品名称、排序、状态开关、展示时间范围、操作
- 操作列：编辑、删除（确认对话框）
- 新增/编辑弹窗字段：图片上传（组件）、标题输入、关联商品（远程搜索选择）、跳转链接输入、排序、展示时间范围（日期时间范围选择器）
- 分页组件

**Tab 3 - 数据概览：**

- 新品数量趋势图（近 30 天，ECharts 折线图）
- 各分类新品数量饼图（ECharts 饼图）

---

## 5. 移动端 UI 设计（UniApp）

### 5.1 入口

首页金刚区「新品首发」图标 → 跳转 `/pages/new-product/index`

### 5.2 页面结构（从上到下）

1. **顶部导航栏**：标题「新品首发」，左侧返回箭头
2. **Banner 轮播区**：从 `/api/v1/new-product-banners` 获取，自动轮播，圆角卡片，左右露出边缘
3. **分类 Tab 栏**：横向滚动 Tab（全部 + 有新品的分类），选中态橙色文字 + 底部指示条
4. **商品列表**：双列瀑布流卡片（复用 `ProductWaterfall` 组件），左上角橙色「NEW」角标，上拉加载更多，下拉刷新
5. **空状态**：无新品时显示空状态插图 + 「暂无新品，敬请期待」

### 5.3 路由

```json
{
  "path": "pages/new-product/index",
  "style": {
    "navigationBarTitleText": "新品首发"
  }
}
```

### 5.4 新品状态计算

移动端根据 `newProductStartTime` 和 `newProductEndTime` 显示标签：
- 无时间限制 → 不显示额外标签
- 进行中 → 显示剩余天数
- 即将开始 → 显示「即将开售」

---

## 6. 实现范围总结

| 模块 | 新增/修改 | 关键文件 |
|------|-----------|----------|
| 数据库 | 修改 pms_product，新增 pms_new_product_banner | migration SQL |
| Product 实体 | 新增 3 个字段 | Product.java |
| ProductMapper | 新增新品分页查询 | ProductMapper.java + XML |
| NewProductAdminController | 新增 | 5 个接口 |
| NewProductBannerAdminController | 新增 | 4 个接口 |
| NewProductBanner 实体 + Mapper + Service | 新增 | 完整 CRUD |
| ProductController | 新增 /new-page 端点 | ProductController.java |
| Admin 侧边栏 | 新增菜单项 | routes.ts / Sidebar.vue |
| Admin 新品管理页 | 新增页面 | views/product/new-product/index.vue |
| Admin API 模块 | 新增新品管理 API | api/modules/product.ts |
| 移动端新品页面 | 新增页面 | pages/new-product/index.vue |
| 移动端 API | 新增新品相关 API | api/product.ts + api/newBanner.ts |
| 移动端 pages.json | 新增路由 | pages.json |
