# 搜索功能完整设计方案

**日期**: 2026-05-31
**范围**: Web 端 + 移动端首页搜索功能，含后端关键词记录支持

---

## 1. 背景

ShopMax 需要为 Web 端和移动端开发完整的搜索功能。当前状态：
- **移动端**：有基本搜索页（输入+结果网格+分页），缺少搜索历史、搜索建议、排序筛选
- **Web 端**：有搜索历史，缺少分页加载；导航栏搜索建议下拉未实现
- **后端**：`LIKE '%keyword%'` 匹配商品名称，无关键词记录、无热搜统计、无搜索建议 API

## 2. 功能清单

| 功能 | 说明 |
|------|------|
| 搜索历史 | 前端 localStorage 存储，最近 10 条，支持单条删除和清空 |
| 热门搜索 | 后端记录关键词，按最近 7 天分组计数取 Top 10，Redis 缓存 10 分钟 |
| 搜索建议 | 输入时实时匹配商品名（前5个）+ 热搜词（前3个），300ms 防抖 |
| 排序 | 综合（默认）、价格升降序、销量、新品优先 |
| 多维筛选 | Web 端左侧面板：分类树 + 品牌列表 |
| 分页加载 | 移动端无限滚动，Web 端底部分页器 |
| 关键词记录 | 搜索时异步写入数据库，Redis 去重防刷（同一用户 1 分钟内同词不重复记录） |

## 3. 后端设计

### 3.1 新增搜索关键词表

```sql
CREATE TABLE cms_search_keyword (
    id BIGINT NOT NULL AUTO_INCREMENT,
    keyword VARCHAR(128) NOT NULL COMMENT '搜索关键词',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    search_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT '0',
    PRIMARY KEY (id),
    KEY idx_keyword (keyword),
    KEY idx_search_time (search_time),
    KEY idx_user_id (user_id)
);
```

### 3.2 新增 API 端点

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/v1/search/record` | 记录搜索关键词 | 需登录 |
| GET | `/api/v1/search/hot` | 获取 Top 10 热搜词 | 公开 |
| GET | `/api/v1/search/suggest?keyword=xx&limit=8` | 搜索建议 | 公开 |

### 3.3 SearchService 接口

```java
public interface SearchService {
    void recordKeyword(String keyword, Long userId);
    List<HotKeywordResponse> getHotKeywords(int limit);
    SuggestResponse getSuggestions(String keyword, int limit);
}
```

### 3.4 SearchServiceImpl 实现

**recordKeyword（防刷）：**
- 使用 Redis `setIfAbsent("search:dedup:{userId}:{keyword}", "1", 60, SECONDS)`
- key 存在则跳过，不存在则写入数据库

**getHotKeywords（热搜）：**
- SQL：`SELECT keyword, COUNT(*) as count FROM cms_search_keyword WHERE search_time >= 7天前 AND deleted=0 GROUP BY keyword ORDER BY count DESC LIMIT ?`
- 结果缓存 Redis key `search:hot`，TTL 10 分钟

**getSuggestions（建议）：**
- 商品名匹配：`SELECT DISTINCT name FROM pms_product WHERE name LIKE '%keyword%' AND deleted=0 AND status=1 LIMIT 5`
- 热搜词：取 Top 3 热搜词
- 合并返回，商品名在前，热搜词在后
- 结果缓存 Redis key `search:suggest:{keyword}`，TTL 1 分钟

### 3.5 更新 ProductController.page()

增加 `sortBy` 参数：
- `price_asc`：按售价升序
- `price_desc`：按售价降序
- `sales`：按销量降序
- `newest`：按创建时间降序（默认）

### 3.6 新增排序索引

```sql
ALTER TABLE pms_product ADD INDEX idx_product_price (deleted, status, sale_price);
ALTER TABLE pms_product ADD INDEX idx_product_sales (deleted, status, sales);
```

### 3.7 Gateway/Security 白名单

公开接口（无需登录）：
- `GET /api/v1/search/hot`
- `GET /api/v1/search/suggest`

需登录接口：
- `POST /api/v1/search/record`

## 4. 前端设计

### 4.1 共享层（packages/shared）

**新增 src/api/search.ts：**
```typescript
export const searchApi = {
  record: (keyword: string) => post('/api/v1/search/record', { keyword }),
  getHot: (limit?: number) => get('/api/v1/search/hot', { limit }),
  getSuggest: (keyword: string, limit?: number) =>
    get('/api/v1/search/suggest', { keyword, limit }),
}
```

**扩展 ProductPageParams：**
```typescript
export interface ProductPageParams extends PageParams {
  categoryId?: number
  keyword?: string
  status?: number
  sortBy?: 'price_asc' | 'price_desc' | 'sales' | 'newest'
}
```

### 4.2 移动端搜索页面（重写）

**三态切换：**

1. **初始态**（未输入）：
   - 热门搜索标签（调用 `searchApi.getHot`，标签样式，点击直接搜索）
   - 搜索历史标签（localStorage `search_history`，最近 10 条）
   - 历史支持单条删除（×按钮）和清空全部

2. **输入态**（正在输入）：
   - 实时建议下拉（300ms 防抖调用 `searchApi.getSuggest`）
   - 商品名建议项（显示商品名，高亮匹配文字）
   - 热搜词建议项（带🔥图标）
   - 点击建议项 → 执行搜索 + 记录关键词

3. **结果态**（已搜索）：
   - 排序Tab：综合/价格↑/价格↓/销量/新品
   - 双列商品网格（复用现有卡片样式）
   - 无限滚动分页（复用已实现的 push + onLoadMore 模式）
   - 切换排序时重新请求第一页

**搜索触发逻辑：**
- 点击热搜/历史/建议项 → 关键词填入输入框 → 执行搜索 → 记录关键词 → 切换到结果态
- 点击搜索按钮/键盘确认 → 执行搜索 → 记录关键词 → 保存到历史 → 切换到结果态

### 4.3 Web 端搜索结果页（重写）

**布局：左侧筛选 + 右侧内容**

**左侧筛选面板：**
- 分类树：调用 `categoryApi.getTree()`，显示一级分类，点击展开子分类，选中高亮
- 品牌列表：调用 `brandApi.getAll()`，显示品牌名，支持多选
- 选中筛选项后更新 URL query 参数并重新搜索

**右侧内容区：**
- 排序栏：综合（默认）、价格↑、价格↓、销量、新品
- 4 列商品网格（复用现有卡片样式）
- 底部分页器（Element Plus `el-pagination` 组件）

**URL 参数同步：**
- `?q=关键词&sortBy=price_asc&categoryId=1&brandId=2&page=1`
- 路由变化时自动搜索（watch route.query）

### 4.4 Web 端导航栏搜索建议（DefaultLayout.vue）

- 输入框 focus 时显示建议面板
- 300ms 防抖调用 `searchApi.getSuggest`
- 建议项样式：
  - 商品名：左侧📱图标，匹配文字高亮（橙色）
  - 热搜词：左侧🔥图标，匹配文字高亮
- 点击建议项 → `router.push('/search?q=xxx')`
- 输入框 blur 时延迟 200ms 隐藏（允许点击建议项）
- 建议面板绝对定位在输入框下方，宽度与输入框一致

## 5. 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `docs/sql/02-product-system.sql` | 修改 | 新增表定义和索引 |
| `shop-backend/docs/sql/migration_006_search_keyword.sql` | 新建 | 迁移文件 |
| `shop-backend/shop-modules/shop-product-service/.../entity/SearchKeyword.java` | 新建 | 实体 |
| `shop-backend/shop-modules/shop-product-service/.../mapper/SearchKeywordMapper.java` | 新建 | Mapper |
| `shop-backend/shop-modules/shop-product-service/.../service/SearchService.java` | 新建 | 接口 |
| `shop-backend/shop-modules/shop-product-service/.../service/impl/SearchServiceImpl.java` | 新建 | 实现 |
| `shop-backend/shop-modules/shop-product-service/.../controller/SearchController.java` | 新建 | 控制器 |
| `shop-backend/shop-modules/shop-product-service/.../controller/response/HotKeywordResponse.java` | 新建 | DTO |
| `shop-backend/shop-modules/shop-product-service/.../controller/response/SuggestResponse.java` | 新建 | DTO |
| `shop-backend/shop-modules/shop-product-service/.../controller/ProductController.java` | 修改 | 增加 sortBy |
| `shop-backend/shop-modules/shop-product-service/.../service/impl/ProductServiceImpl.java` | 修改 | 排序逻辑 |
| `shop-backend/shop-gateway/.../filter/AuthGlobalFilter.java` | 修改 | 白名单 |
| `shop-backend/shop-common/shop-common-security/.../SecurityConfig.java` | 修改 | 白名单 |
| `shop-frontend/packages/shared/src/api/search.ts` | 新建 | 搜索 API |
| `shop-frontend/packages/shared/src/api/index.ts` | 修改 | 导出 |
| `shop-frontend/packages/shared/src/types/index.ts` | 修改 | 增加 sortBy |
| `shop-frontend/packages/mobile/src/pages/search/index.vue` | 重写 | 完整搜索页 |
| `shop-frontend/packages/web/src/pages/search/index.vue` | 重写 | 左侧筛选+分页 |
| `shop-frontend/packages/web/src/layouts/DefaultLayout.vue` | 修改 | 搜索建议下拉 |

## 6. 实现顺序

1. SQL 迁移（表 + 索引）
2. 后端搜索服务（SearchService + SearchController）
3. 后端排序支持（ProductController 增加 sortBy）
4. Gateway/Security 白名单
5. 共享 API 层
6. 移动端搜索页面
7. Web 端搜索页面
8. Web 端导航栏建议

## 7. 验证方式

1. 运行迁移 SQL，确认表和索引创建成功
2. `POST /api/v1/search/record` 记录关键词 → 数据库有记录 → 重复请求 1 分钟内不重复记录
3. `GET /api/v1/search/hot` → 返回热搜排行
4. `GET /api/v1/search/suggest?keyword=iPh` → 返回商品名+热搜混合建议
5. `GET /api/v1/products?keyword=手机&sortBy=price_asc` → 按价格排序
6. 移动端：三态切换正常，排序切换生效，历史记录工作
7. Web 端：左侧筛选更新结果，分页器工作，导航栏建议下拉正常
