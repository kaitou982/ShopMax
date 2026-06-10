# CLAUDE.md - ShopMax 电商平台开发规范

## 项目概述

**项目名称**: ShopMax 电商平台  
**技术栈**: SpringBoot 3 + Vue 3 + UniApp  
**平台类型**: B2C商城 + 直播带货 + 社交电商 + 内容社区  

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

### 前端代码审查

- [ ] 使用 Vue 3 + TypeScript
- [ ] 使用组合式 API
- [ ] 使用 Pinia
- [ ] 组件有 name 属性
- [ ] 没有 any 类型
- [ ] Props/Emits 有类型定义
- [ ] 使用 scoped 样式
- [ ] 没有 console.log

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
