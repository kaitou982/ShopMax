# CLAUDE.md - ShopMax 电商平台开发规范

## 项目概述

**项目名称**: ShopMax 电商平台
**技术栈**: SpringBoot 3 + Vue 3 + UniApp
**平台类型**: B2C商城 + 直播带货 + 社交电商 + 内容社区

---

## 项目架构

### 后端微服务架构

后端采用 Spring Cloud 微服务架构，源码在 `shop-backend/` 目录下。

**业务服务层**（`shop-backend/shop-modules/`）：

| 模块 | 职责 | 核心域 |
|------|------|--------|
| shop-user-service | 用户服务 | 用户信息、钱包、会员等级 |
| shop-product-service | 商品服务（C端） | 商品、分类、品牌、新品Banner 只读查询 |
| shop-order-service | 订单服务 | 订单创建、支付、发货、完成全流程 |
| shop-payment-service | 支付服务 | 支付渠道对接、回调处理 |
| shop-marketing-service | 营销服务 | 优惠券、秒杀、团购、促销活动 |
| shop-live-service | 直播服务 | 直播间管理、直播商品 |
| shop-community-service | 社区服务 | 笔记、评论、内容审核 |
| shop-admin-service | 管理服务 | 后台管理接口、通知中心、仪表盘 |
| shop-customer-service | 客服服务 | FAQ、智能问答 |
| shop-file-service | 文件服务 | 文件上传与下载 |

**基础设施层**（`shop-backend/`）：

| 模块 | 职责 |
|------|------|
| shop-gateway | API 网关（路由转发、JWT 认证过滤） |
| shop-auth | 认证服务（登录注册、Token 签发，通过 Feign 调用 user-service） |
| shop-common | 公共模块（见下表） |

**shop-common 子模块**（`shop-backend/shop-common/`）：

| 子模块 | 职责 |
|--------|------|
| shop-common-core | 基础工具类、异常定义 |
| shop-common-feign | Feign 远程调用客户端与 DTO |
| shop-common-redis | Redis 缓存工具 |
| shop-common-security | Spring Security 配置 |
| shop-common-storage | 文件存储抽象 |
| shop-common-web | Web 通用组件（Result、PageResult） |

### 前端 Monorepo 架构

前端 `shop-frontend/` 采用 pnpm workspace monorepo 结构。

```
shop-frontend/
├── packages/
│   ├── mobile/    — UniApp 移动端（@shop/mobile）
│   ├── web/       — Web PC 端，Naive UI（@shop/web）
│   └── shared/    — 共享代码（@shop/shared）
├── pnpm-workspace.yaml
└── package.json
```

**共享层** `@shop/shared` 包含：
- `src/api/` — 统一 API 客户端（各服务接口封装）
- `src/types/` — TypeScript 类型定义
- `src/stores/` — Pinia 共享 Store
- `src/utils/` — 通用工具函数（HTTP 封装等）

**包引用方式**：`"@shop/shared": "workspace:*"`

**独立项目**：`shop-admin-ui/` 是管理后台前端，不在 monorepo 内，独立构建部署。

---

## 绝对强制规则

### 后端开发强制规则

1. **JDK 21 和 SpringBoot 3.x 强制使用**
   - 项目 pom.xml 指定 `<java.version>21</java.version>`，所有代码必须与 Java 21 兼容
   - maven-compiler-plugin 必须配置 `<release>21</release>`（非仅靠 properties，IDE 需要此标签识别模块语言级别）
   - 禁止任何 JDK 8/11/17 或 SpringBoot 2.x 的写法
   - 使用新的日期时间 API (LocalDateTime, LocalDate, LocalTime)
   - 使用 `jakarta.*` 命名空间（禁止 `javax.*`）

2. **Java 21 特性强制使用与限制**
   - ✅ 必须使用：pattern matching for instanceof（禁止旧式 instanceof + 显式强转）
   - ✅ 必须使用：`java.net.http.HttpClient`（禁止旧式 HttpURLConnection）
   - ✅ 允许使用：Switch Expressions（`->` 箭头语法）、Text Blocks（`"""..."""`）、Records
   - ✅ 允许使用：Virtual Threads（`Thread.ofVirtual()`）处理高并发场景
   - ❌ 禁止使用：任何 `--enable-preview` 特性（如 String Templates），因为它们是预览特性，随时可能变更
   - ❌ 禁止使用：已标记 `@Deprecated(forRemoval=true)` 的 API

3. **Pattern Matching for instanceof 强制**
```java
// ✅ 正确 — Java 16+ 模式匹配
if (obj instanceof String str) {
    return str.length();
}
if (obj instanceof Long val) {
    return val;
}

// ❌ 禁止 — 旧式 instanceof + 显式强转
if (obj instanceof String) {
    return ((String) obj).length();
}
```

4. **必须使用 MyBatis-Plus，禁止 JPA**
   - 所有数据库操作通过 MyBatis-Plus 完成
   - 复杂查询使用自定义 Mapper XML

5. **构造器注入强制，禁止字段注入**
```java
// ✅ 正确
@Service
public class UserService {
    private final UserMapper userMapper;
    
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
}

// ❌ 禁止
@Autowired
private UserMapper userMapper;
```

6. **必须处理异常，禁止空 catch**
```java
// ✅ 正确
try {
    // do something
} catch (Exception e) {
    log.error("操作失败: {}", e.getMessage(), e);
    throw new BusinessException("操作失败");
}
```

7. **禁止使用魔法数字，必须使用常量**
```java
// ✅ 正确
private static final int MAX_RETRY_COUNT = 3;

if (status == OrderStatus.PAID.getCode()) {
    // do something
}
```

8. **数据库表必须包含标准字段**
```sql
`id` bigint NOT NULL AUTO_INCREMENT,
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
`deleted` tinyint NOT NULL DEFAULT '0',
```

### 前端开发强制规则

1. **Vue 3 + TypeScript + 组合式API 强制**
   - 禁止使用 Options API
   - 禁止使用 JavaScript

2. **必须使用 Pinia，禁止 Vuex**
```typescript
// ✅ 正确
import { defineStore } from 'pinia'
export const useUserStore = defineStore('user', () => {
    // ...
})
```

3. **组件必须使用 name 属性**
```vue
<script setup>
defineOptions({ name: 'UserList' })
</script>
```

4. **禁止使用 any 类型**
```typescript
// ✅ 正确
const data = ref<UserInfo | null>(null)

// ❌ 禁止
const data: any = response.data
```

5. **Props 和 Emits 必须定义类型**
```typescript
interface Props {
    userId: number
    showDelete?: boolean
}

const props = withDefaults(defineProps<Props>(), {
    showDelete: false
})

interface Emits {
    (e: 'update', id: number): void
}

const emit = defineEmits<Emits>()
```

### 移动端开发强制规则

1. **UniApp + Vue 3 + TypeScript 强制**

2. **页面必须使用 rpx 单位**
```scss
.container {
    padding: 24rpx;
    font-size: 32rpx;
}
```

3. **必须适配安全区域**
```scss
.bottom-bar {
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);
}
```

4. **平台特定代码必须使用条件编译**
```typescript
// #ifdef MP-WEIXIN
// 微信小程序代码
// #endif
```

---

## 架构规范

### Feign 远程调用规范

1. **Feign 客户端定义位置**：所有 Feign 接口统一定义在 `shop-common-feign` 模块
   - 客户端接口：`com.shop.common.feign.client` 包
   - DTO 对象：`com.shop.common.feign.dto.{domain}` 包
   - 降级工厂：`com.shop.common.feign.fallback` 包

2. **接口路径规范**：内部调用必须使用 `/internal/` 路径前缀
   - `/internal/` 路径不经过 Gateway 认证（服务间直连）
   - 对外 API 使用 `/api/v1/` 前缀，经过 Gateway 认证

3. **@FeignClient 注解规范**
```java
// ✅ 正确 — 指定服务名、路径、降级工厂
@FeignClient(name = "shop-user-service", path = "/internal/users",
             fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {
    @PostMapping("/login")
    Result<UserLoginResponse> login(@RequestBody UserLoginRequest request);
}

// ✅ 正确 — 路径写在方法级别
@FeignClient(name = "shop-order-service",
             fallbackFactory = OrderServiceClientFallbackFactory.class)
public interface OrderServiceClient {
    @GetMapping("/internal/orders/by-order-no")
    Result<OrderSimpleResponse> getByOrderNo(@RequestParam("orderNo") String orderNo,
                                              @RequestParam("userId") Long userId);
}
```

4. **降级机制强制**：每个 FeignClient 必须实现 `FallbackFactory`，不得使用简单 fallback 类
```java
@Slf4j
@Component
public class OrderServiceClientFallbackFactory implements FallbackFactory<OrderServiceClient> {
    @Override
    public OrderServiceClient create(Throwable cause) {
        log.error("订单服务调用失败: {}", cause.getMessage(), cause);
        return new OrderServiceClient() {
            @Override
            public Result<OrderSimpleResponse> getByOrderNo(String orderNo, Long userId) {
                return Result.error(503, "订单服务暂时不可用，请稍后再试");
            }
        };
    }
}
```

### Admin / C端分离模式

**核心原则**：同一张数据库表在 `shop-admin-service` 和对应 C 端服务（如 `shop-product-service`）中各维护一套独立的 Entity + Controller。

1. **职责分离**
   - `shop-admin-service`：管理端完整 CRUD，使用 `@PreAuthorize` 权限控制
   - C 端服务（如 `shop-product-service`）：只读查询，面向普通用户

2. **Entity 独立维护**
   - 两个服务的 Entity 映射同一张数据库表，字段结构保持一致
   - 各自独立维护，不做跨模块 Entity 引用
   - 示例：`pms_new_product_banner` 表在 admin-service 和 product-service 中各有一个 `NewProductBanner` Entity

3. **权限控制规范**
   - Admin 端 Controller 必须使用 `@PreAuthorize` 注解
   - 仅管理员：`@PreAuthorize("hasRole('ADMIN')")`
   - 管理员或商家：`@PreAuthorize("hasAnyRole('ADMIN','STORE')")`
   - 类级别注解对整个 Controller 生效，方法级别可进一步细化

4. **命名约定**
   - Admin 端 Controller：`{Entity}AdminController` 或 `{Entity}Controller`（在 admin-service 包内）
   - C 端 Controller：`{Entity}Controller`（在对应 C 端服务包内）

### 安全规范

1. **敏感信息管理**：所有敏感值必须使用环境变量，禁止硬编码
```yaml
# ✅ 正确 — 使用环境变量 + 默认值
spring:
  datasource:
    password: ${DB_PASSWORD:aptx4869}
jwt:
  secret: ${JWT_SECRET:shopmax-secret-key-2026-for-jwt-signing-and-verification}

# ❌ 禁止 — 硬编码敏感值
spring:
  datasource:
    password: aptx4869
```

2. **Token 黑名单**：用户登出后 Token 必须加入 Redis 黑名单
   - Key 格式：`token:blacklist:{token}`
   - Gateway AuthGlobalFilter 和 JwtAuthenticationFilter 都必须检查黑名单
   - 接口：`BlacklistChecker`（shop-common-core）→ `RedisBlacklistChecker`（shop-common-redis）

3. **Redis 反序列化安全**：禁止使用 `LaissezFaireSubTypeValidator`，必须使用 `BasicPolymorphicTypeValidator` 白名单

4. **登录安全**：必须实现登录失败次数限制
   - 连续 5 次失败后锁定 30 分钟
   - Key 格式：`login:fail:{username}` 或 `login:fail:{ip}`

5. **双 Token 机制**：Access Token（1天） + Refresh Token（7天）
   - Access Token 过期后使用 Refresh Token 刷新
   - Refresh Token 使用后必须轮换（旧 Token 加入黑名单）

### 可观测性规范

1. **日志配置**：使用 `logback-spring.xml`，按环境区分输出
   - dev 环境：控制台输出
   - prod 环境：文件轮转（100MB/文件，7天，1GB 总量）+ JSON 格式
   - 日志格式必须包含 `[%X{traceId:-}]`

2. **链路追踪**：使用 Micrometer Tracing + Brave
   - Gateway TraceFilter 注入 `X-Trace-Id` 请求头
   - 下游服务 MdcTraceFilter 读取并注入 MDC
   - FeignRequestInterceptor 自动传播 `X-Trace-Id`

3. **健康检查**：所有服务必须暴露 Actuator 端点
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

4. **监控告警**：使用 Prometheus + Grafana
   - Prometheus 抓取 `/actuator/prometheus` 端点
   - Grafana 可视化展示

### 分布式模式规范

1. **分布式锁**：使用 `RedisDistributedLock`，禁止使用 `setIfAbsent` 模拟
```java
// ✅ 正确
RedisDistributedLock.LockHandle lock = distributedLock.tryLock(key, 5, 10, TimeUnit.SECONDS);
if (lock == null) {
    throw new BusinessException("获取锁失败");
}
try {
    // 业务逻辑
} finally {
    lock.unlock();
}

// ❌ 禁止
if (redisUtil.setIfAbsent(key, value, 10, TimeUnit.SECONDS)) {
    try { /* 业务逻辑 */ } finally { redisUtil.delete(key); }
}
```

2. **熔断降级**：使用 Sentinel，默认规则
   - 慢调用比例：500ms 阈值，50% 比例，30 秒熔断
   - 异常比例：50% 阈值，30 秒熔断

3. **Feign 超时**：所有服务必须配置超时
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
```

4. **请求头传播**：FeignRequestInterceptor 自动传播
   - `X-User-Id`：用户 ID
   - `X-User-Role`：用户角色
   - `Authorization`：认证令牌
   - `X-Trace-Id`：链路追踪 ID

---

## 代码生成模板

### 后端 Controller 模板

```java
package com.shop.{module}.controller;

import com.shop.common.web.Result;
import com.shop.common.web.PageResult;
import com.shop.{module}.service.{Entity}Service;
import com.shop.{module}.entity.{Entity};
import com.shop.{module}.controller.request.{Entity}CreateRequest;
import com.shop.{module}.controller.request.{Entity}UpdateRequest;
import com.shop.{module}.controller.response.{Entity}Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {Entity}控制器
 *
 * @author {author}
 * @since {date}
 */
@Tag(name = "{Entity}管理")
@RestController
@RequestMapping("/api/v1/{module}s")
@RequiredArgsConstructor
public class {Entity}Controller {

    private final {Entity}Service {entity}Service;

    @Operation(summary = "创建{Entity}")
    @PostMapping
    public Result<{Entity}Response> create(@Valid @RequestBody {Entity}CreateRequest request) {
        return Result.success({entity}Service.create(request));
    }

    @Operation(summary = "更新{Entity}")
    @PutMapping("/{id}")
    public Result<{Entity}Response> update(@PathVariable Long id,
                                           @Valid @RequestBody {Entity}UpdateRequest request) {
        return Result.success({entity}Service.update(id, request));
    }

    @Operation(summary = "删除{Entity}")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        {entity}Service.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取{Entity}详情")
    @GetMapping("/{id}")
    public Result<{Entity}Response> getById(@PathVariable Long id) {
        return Result.success({entity}Service.getById(id));
    }

    @Operation(summary = "分页查询{Entity}")
    @GetMapping
    public Result<PageResult<{Entity}Response>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success({entity}Service.page(pageNum, pageSize));
    }

    @Operation(summary = "列表查询{Entity}")
    @GetMapping("/list")
    public Result<List<{Entity}Response>> list() {
        return Result.success({entity}Service.list());
    }
}
```

### 后端 Service 模板

```java
package com.shop.{module}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.{module}.entity.{Entity};
import com.shop.{module}.mapper.{Entity}Mapper;
import com.shop.{module}.service.{Entity}Service;
import com.shop.{module}.controller.request.{Entity}CreateRequest;
import com.shop.{module}.controller.request.{Entity}UpdateRequest;
import com.shop.{module}.controller.response.{Entity}Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {Entity}服务实现
 *
 * @author {author}
 * @since {date}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class {Entity}ServiceImpl extends ServiceImpl<{Entity}Mapper, {Entity}> implements {Entity}Service {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public {Entity}Response create({Entity}CreateRequest request) {
        {Entity} entity = new {Entity}();
        BeanUtils.copyProperties(request, entity);
        
        baseMapper.insert(entity);
        
        log.info("创建{Entity}成功: id={}", entity.getId());
        return convertToResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public {Entity}Response update(Long id, {Entity}UpdateRequest request) {
        {Entity} entity = getEntityById(id);
        BeanUtils.copyProperties(request, entity);
        
        baseMapper.updateById(entity);
        
        log.info("更新{Entity}成功: id={}", id);
        return convertToResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        {Entity} entity = getEntityById(id);
        entity.setDeleted(1);
        baseMapper.updateById(entity);
        
        log.info("删除{Entity}成功: id={}", id);
    }

    @Override
    public {Entity}Response getById(Long id) {
        {Entity} entity = getEntityById(id);
        return convertToResponse(entity);
    }

    @Override
    public PageResult<{Entity}Response> page(Integer pageNum, Integer pageSize) {
        Page<{Entity}> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<{Entity}> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq({Entity}::getDeleted, 0);
        wrapper.orderByDesc({Entity}::getCreateTime);
        
        Page<{Entity}> result = baseMapper.selectPage(page, wrapper);
        
        List<{Entity}Response> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    public List<{Entity}Response> list() {
        LambdaQueryWrapper<{Entity}> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq({Entity}::getDeleted, 0);
        
        return baseMapper.selectList(wrapper).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private {Entity} getEntityById(Long id) {
        LambdaQueryWrapper<{Entity}> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq({Entity}::getId, id);
        wrapper.eq({Entity}::getDeleted, 0);
        
        {Entity} entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException("{Entity}不存在");
        }
        return entity;
    }

    private {Entity}Response convertToResponse({Entity} entity) {
        {Entity}Response response = new {Entity}Response();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}
```

### 后端 Entity 模板

```java
package com.shop.{module}.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * {Entity}实体
 *
 * @author {author}
 * @since {date}
 */
@Data
@TableName("{table_name}")
public class {Entity} implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // TODO: 添加业务字段

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
```

### 前端 Vue 组件模板

```vue
<script setup lang="ts">
/**
 * {ComponentName} 组件
 * @description {description}
 * @author {author}
 * @since {date}
 */
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({
  name: '{ComponentName}'
})

// Props 定义
interface Props {
  // TODO: 定义props
}

const props = withDefaults(defineProps<Props>(), {
  // TODO: 定义默认值
})

// Emits 定义
interface Emits {
  (e: 'update', value: any): void
}

const emit = defineEmits<Emits>()

// State
const loading = ref<boolean>(false)

// Computed
// const computedValue = computed(() => {
//   return someValue
// })

// Methods
const init = async () => {
  try {
    loading.value = true
    // TODO: 初始化逻辑
  } finally {
    loading.value = false
  }
}

// Watch
// watch(() => props.value, (newVal) => {
//   // TODO: 监听逻辑
// })

// Lifecycle
onMounted(() => {
  init()
})
</script>

<template>
  <div class="{component-class}">
    <!-- TODO: 组件模板 -->
  </div>
</template>

<style scoped lang="scss">
.{component-class} {
  // TODO: 组件样式
}
</style>
```

---

## 文件创建规范

### 创建后端文件时

1. **必须检查**: 包名是否符合 `com.shop.{module}` 格式
2. **必须检查**: 类名是否使用大驼峰
3. **必须检查**: 是否使用 Lombok 的 `@RequiredArgsConstructor`
4. **必须检查**: 是否添加类注释（包含作者和日期）
5. **必须检查**: Controller 是否添加 Swagger 注解

### 创建前端文件时

1. **必须检查**: 文件名是否符合命名规范
2. **必须检查**: 是否使用 TypeScript
3. **必须检查**: 是否定义 `defineOptions({ name: 'xxx' })`
4. **必须检查**: Props 和 Emits 是否定义类型
5. **必须检查**: 是否使用 scoped 样式

### 创建移动端文件时

1. **必须检查**: 是否使用 rpx 单位
2. **必须检查**: 是否适配安全区域
3. **必须检查**: 平台特定代码是否使用条件编译

---

## API 接口规范

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1712995200000
}
```

### 错误码

| 错误码 | 含义 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 业务错误-用户相关 |
| 1002 | 业务错误-商品相关 |
| 1003 | 业务错误-订单相关 |

---

## 数据库规范

### 表命名
- 小写下划线，如: `user_info`, `order_detail`
- 模块前缀: `sys_` 系统, `ums_` 用户, `pms_` 商品, `oms_` 订单, `cms_` 社区

### 字段命名
- 主键: `id`
- 外键: `{table}_id`，如: `user_id`
- 状态: `status`
- 删除标志: `deleted`
- 创建时间: `create_time`
- 更新时间: `update_time`

### 字段类型
- 主键: `bigint`
- 状态/类型: `tinyint`
- 金额: `decimal(10,2)`
- 时间: `datetime`
- 字符串: `varchar(32/64/128/255/500)`

---

## 代码审查检查清单

### 后端代码审查

- [ ] 使用 JDK 21 和 SpringBoot 3.x
- [ ] 使用 pattern matching for instanceof（非旧式强转）
- [ ] 未使用 --enable-preview 特性
- [ ] 未使用 @Deprecated(forRemoval=true) 的 API
- [ ] 使用构造器注入
- [ ] 处理所有异常
- [ ] 没有魔法数字
- [ ] 数据库表包含标准字段
- [ ] API 响应格式正确
- [ ] 添加 Swagger 注解
- [ ] 日志打印合理
- [ ] 事务注解正确
- [ ] Feign 客户端定义在 shop-common-feign 模块，非业务服务内
- [ ] Feign 客户端实现了 FallbackFactory 降级（非简单 fallback 类）
- [ ] Feign 内部接口使用 /internal/ 路径前缀
- [ ] Admin 端 Controller 使用 @PreAuthorize 权限注解
- [ ] 同表 Entity 在 admin-service 和 C端服务中独立维护，字段一致
- [ ] 敏感信息使用环境变量（${ENV_VAR:defaultValue}），无硬编码
- [ ] Token 登出时加入 Redis 黑名单
- [ ] 登录接口有失败次数限制
- [ ] 使用 RedisDistributedLock 而非 setIfAbsent 模拟锁
- [ ] Feign 客户端配置了超时（connectTimeout/readTimeout）
- [ ] 日志格式包含 traceId
- [ ] Actuator 端点已暴露（health,info,metrics,prometheus）

### 前端代码审查

- [ ] 使用 Vue 3 + TypeScript
- [ ] 使用组合式 API
- [ ] 使用 Pinia
- [ ] 组件有 name 属性
- [ ] 没有 any 类型
- [ ] Props/Emits 有类型定义
- [ ] 使用 scoped 样式
- [ ] 没有 console.log
- [ ] API 调用使用 @shop/shared 的统一客户端，不直接写 axios
- [ ] 可复用逻辑放在 @shop/shared 而非单个包内

### 移动端代码审查

- [ ] 使用 rpx 单位
- [ ] 适配安全区域
- [ ] 平台代码使用条件编译
- [ ] 图片懒加载
- [ ] 加载状态处理
- [ ] 错误提示友好

---

## 参考文档

- [需求分析文档](./docs/planning/01-requirements-analysis.md)
- [开发阶段规划](./docs/planning/02-development-phases.md)
- [后端约束规范](./docs/specs/backend-constraints.md)
- [前端约束规范](./docs/specs/frontend-constraints.md)
- [移动端约束规范](./docs/specs/mobile-constraints.md)
- [微服务验证指南](./docs/microservice-verification-guide.md)
- [环境变量配置](./.env.example)
