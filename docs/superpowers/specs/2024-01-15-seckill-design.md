# 秒杀功能完善设计方案

## 1. 概述

### 1.1 背景

当前秒杀功能已部分实现，包括：
- 后端：实体类、Mapper、Service、Controller
- 前端：Mobile 秒杀页面、PC 首页秒杀条
- 数据库：秒杀场次表、秒杀商品表

但存在以下关键缺失：
- 前后端类型不一致
- 秒杀订单生成缺失
- PC Web 端秒杀专题页缺失
- 限流和防刷机制缺失
- 分布式事务和幂等性控制缺失

### 1.2 目标

完善秒杀功能，实现：
1. 修复前后端类型一致性问题
2. 实现完整的秒杀下单流程
3. 创建 PC Web 端秒杀专题页
4. 添加限流和防刷机制
5. 实现分布式事务和幂等性控制

---

## 2. 实现阶段

### 阶段一：前后端类型一致性修复

**问题：** 前端 `SeckillProduct` 类型包含 `productName`、`productImage`、`originalPrice`、`soldCount` 等字段，但后端 `SeckillProductResponse` 未返回这些字段。

**解决方案：**

1. **后端修改：** 在 `SeckillProductResponse` 中添加商品信息字段
   - `productName` - 商品名称
   - `productImage` - 商品图片
   - `originalPrice` - 原价
   - `soldCount` - 已售数量

2. **Service 层修改：** `getSessionProducts` 方法查询秒杀商品时，同时查询 PMS 商品表获取商品信息

3. **前端修改：** 无需修改，类型已经定义好

**数据流：**
```
SeckillController.getSessionProducts()
  → SeckillService.getSessionProducts()
    → 查询 mms_seckill_product
    → 查询 pms_product 获取商品信息
    → 组装 SeckillProductResponse 返回
```

---

### 阶段二：秒杀订单生成

**当前状态：** 秒杀成功后只扣减库存，不创建订单。

**设计目标：** 秒杀成功后创建正式订单，支持支付超时取消和库存回滚。

#### 2.1 新增秒杀订单表

```sql
CREATE TABLE oms_seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id BIGINT NOT NULL COMMENT '秒杀场次',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待支付/1-已支付/2-已取消/3-已超时',
    expire_time DATETIME NOT NULL COMMENT '支付过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_user_session_product (user_id, session_id, product_id),
    INDEX idx_status_expire (status, expire_time),
    INDEX idx_user_id (user_id)
) COMMENT '秒杀订单表';
```

#### 2.2 修改 executeSeckill 流程

**⚠️ 关键设计：Redis 与事务顺序**

Redis 不是事务性资源，放在 `@Transactional` 内无法回滚。正确顺序：
1. **先执行 Redis 扣减**（可回滚）
2. **成功后再执行数据库事务**（自动回滚）
3. **数据库失败时回滚 Redis**

```java
// ⚠️ 注意：此方法不能加 @Transactional，Redis 操作在事务外
public SeckillResult executeSeckill(Long userId, Long sessionId, Long productId) {
    // ========== 第一步：Redis 扣减（在事务外）==========
    String lockKey = "seckill:lock:" + sessionId + ":" + userId + ":" + productId;
    
    // 1.1 获取分布式锁，防止重复请求
    Boolean locked = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);
    if (!locked) {
        return SeckillResult.fail("请求处理中，请勿重复提交");
    }
    
    try {
        // 1.2 Redis Lua 脚本原子扣减库存 + 记录用户
        Long result = redisTemplate.execute(seckillLuaScript, 
            List.of(stockKey, userKey),  // KEYS
            List.of("900")               // ARGV[1]: userKey 过期时间 900秒(15分钟)
        );
        
        if (result == 0) {
            return SeckillResult.fail("已抢光");
        }
        if (result == -1) {
            return SeckillResult.fail("您已参与过该秒杀活动");
        }
        
        // ========== 第二步：数据库事务 ==========
        try {
            return executeSeckillInTransaction(userId, sessionId, productId);
        } catch (Exception e) {
            // ========== 第三步：事务失败，回滚 Redis ==========
            rollbackRedisStock(stockKey, userKey);
            throw e;
        }
    } finally {
        redisTemplate.delete(lockKey);
    }
}

/**
 * 数据库事务内操作：扣减库存 + 创建订单 + 保存消息
 * 
 * @Transactional 保证以下操作的原子性：
 * 1. DB 扣减库存
 * 2. 创建秒杀订单
 * 3. 保存本地消息
 */
@Transactional(rollbackFor = Exception.class)
protected SeckillResult executeSeckillInTransaction(Long userId, Long sessionId, Long productId) {
    // 2.1 DB 扣减库存（乐观锁）
    int rows = seckillProductMapper.decrementStock(productId);
    if (rows == 0) {
        throw new BusinessException("库存不足");
    }
    
    // 2.2 创建秒杀订单
    SeckillOrder order = createSeckillOrder(userId, sessionId, productId);
    
    // 2.3 保存本地消息（用于异步创建正式订单）
    saveLocalMessage("SECKILL_ORDER", order.getOrderNo(), order);
    
    return SeckillResult.success(order.getOrderNo());
}

/**
 * 回滚 Redis 库存和用户记录
 * 
 * 场景：数据库事务失败时调用
 * 注意：使用 Lua 脚本保证原子性
 */
private void rollbackRedisStock(String stockKey, String userKey) {
    String rollbackScript = """
        local stockKey = KEYS[1]
        local userKey = KEYS[2]
        
        -- 删除用户记录
        redis.call('del', userKey)
        
        -- 回滚库存
        redis.call('incr', stockKey)
        
        return 1
        """;
    redisTemplate.execute(rollbackScript, List.of(stockKey, userKey), List.of());
    log.warn("Redis 库存已回滚: stockKey={}, userKey={}", stockKey, userKey);
}
```

**Lua 脚本（seckillLuaScript）：**

```lua
local stockKey = KEYS[1]
local userKey = KEYS[2]
local expireSeconds = tonumber(ARGV[1])

-- 检查用户是否已购买（幂等性检查）
if redis.call('exists', userKey) == 1 then
    return -1  -- 已购买
end

-- 检查库存
local stock = redis.call('get', stockKey)
if not stock or tonumber(stock) <= 0 then
    return 0  -- 库存不足
end

-- 扣减库存
redis.call('decr', stockKey)

-- 记录用户购买（过期时间 = 订单超时时间）
redis.call('set', userKey, '1', 'EX', expireSeconds)

return 1  -- 成功
```

#### 2.3 本地消息表

```sql
CREATE TABLE mms_seckill_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_type VARCHAR(32) NOT NULL COMMENT '消息类型：SECKILL_ORDER',
    business_id VARCHAR(64) NOT NULL COMMENT '业务ID：秒杀订单号',
    content TEXT NOT NULL COMMENT '消息内容(JSON)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待处理/1-已处理/2-处理失败/3-已死信',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_status_next_retry (status, next_retry_time),
    INDEX idx_business_id (business_id)
) COMMENT '秒杀本地消息表';
```

#### 2.4 定时任务

**消息处理任务：** 每5秒执行，处理待处理消息，调用订单服务创建正式订单。

**超时订单处理任务：** 每分钟执行，扫描超时未支付订单，取消订单并回滚库存。

---

### 阶段三：PC Web 端秒杀专题页

**当前状态：** 仅首页有秒杀跑马灯，无独立页面。

**设计目标：** 创建独立的秒杀专题页，展示场次、商品、倒计时。

#### 3.1 页面结构

```
/seckill
├── 场次选择栏（横向 Tab）
│   ├── 场次1：10:00 场
│   ├── 场次2：14:00 场
│   └── 场次3：20:00 场
├── 倒计时区域
│   └── 距离结束 HH:MM:SS
├── 商品网格
│   ├── 商品卡片 1
│   │   ├── 商品图片
│   │   ├── 商品名称
│   │   ├── 秒杀价 / 原价
│   │   ├── 进度条（已抢 XX%）
│   │   └── 秒杀按钮
│   ├── 商品卡片 2
│   └── ...
└── 空状态（无活动时显示）
```

#### 3.2 技术实现

1. **路由配置：** 添加 `/seckill` 路由
2. **页面组件：** `shop-frontend/packages/web/src/pages/seckill/index.vue`
3. **API 调用：** 复用 `seckillApi` 已有方法
4. **倒计时：** 使用 `setInterval` 每秒更新
5. **秒杀按钮：** 调用 `seckillApi.executeSeckill`

#### 3.3 响应式设计

- PC端：4列商品网格
- 平板：3列商品网格
- 移动端：2列商品网格

---

### 阶段四：限流和防刷机制

**当前状态：** 无限流、无验证码、无 IP 限制。

**设计目标：** 添加多层防护，防止恶意刷单。

#### 4.1 防护层次

1. **网关层限流（基于 Redis）：**
   - IP 频率限制：同一 IP 每秒最多 10 次请求
   - 用户频率限制：同一用户每秒最多 5 次请求
   - 实现：Redis + Lua 脚本滑动窗口限流

2. **接口层限流（基于 Guava RateLimiter）：**
   - 秒杀接口单独限流：每秒最多 100 次请求
   - 实现：自定义注解 `@RateLimit`

3. **验证码机制：**
   - 秒杀前需要输入图形验证码
   - 防止机器人刷单
   - 实现：Redis 存储验证码，5分钟过期

4. **黑名单机制：**
   - 记录异常请求（频繁超限、验证码错误）
   - 自动加入黑名单，24小时后解除
   - 实现：Redis 存储黑名单用户/IP

#### 4.2 数据流

```
请求 → 网关限流检查
  → 通过 → 接口限流检查
    → 通过 → 验证码校验（如有）
      → 通过 → 执行秒杀
      → 失败 → 返回错误
    → 失败 → 返回"请求过于频繁"
  → 失败 → 返回"系统繁忙"
```

---

### 阶段五：分布式事务与幂等性控制

#### 5.1 分布式事务方案

**推荐方案：本地消息表 + 定时补偿**

**理由：**
1. 项目当前未引入 RocketMQ，避免增加中间件依赖
2. 本地消息表实现简单，与现有 Spring 生态兼容
3. 秒杀场景允许短暂延迟，最终一致性可接受

**实现要点：**
- 本地消息与业务操作在同一事务中
- 定时任务处理消息，调用订单服务
- 失败重试机制，最多3次
- 死信消息告警，人工处理

#### 5.2 幂等性控制

**多层幂等控制：**

1. **前端防重复提交：** 按钮防抖，1秒内禁止重复点击

2. **Redis 分布式锁：** 同一用户同一商品同一场次只能有一个请求处理中

3. **Redis 用户限购记录：** Lua 脚本中检查用户是否已购买

   **⚠️ 关键设计：userKey 过期时间 = 订单超时时间 = 15 分钟（900秒）**

   - Redis Key: `seckill:user:{sessionId}:{userId}:{productId}`
   - 过期时间: 900秒（15分钟）
   - 与订单超时时间保持一致，确保：
     - 订单存在期间，用户无法重复秒杀
     - 订单超时后，用户可以重新秒杀

4. **数据库唯一索引：** `uk_user_session_product` 防止重复订单

5. **消息消费幂等：** 检查订单是否已存在，跳过重复消息

#### 5.3 完整流程图

```
用户点击秒杀
    ↓
[前端防抖] 1秒内禁止重复点击
    ↓
发送请求 → [网关限流] IP/用户频率检查
    ↓
[接口限流] Guava RateLimiter
    ↓
[Redis分布式锁] 防止同一用户重复请求
    ↓
[Redis Lua脚本]
    ├── 检查用户是否已购买 → 已购买 → 返回"已秒杀过"
    ├── 检查库存 → 库存不足 → 返回"已抢光"
    └── 扣减库存 + 记录用户
    ↓
[本地事务]
    ├── DB 扣减库存
    ├── 创建秒杀订单
    └── 保存本地消息
    ↓
返回"秒杀成功，请在15分钟内支付"
    ↓
[定时任务] 处理本地消息
    ├── 调用订单服务创建正式订单
    ├── 更新消息状态
    └── 失败重试（最多3次）
    ↓
[定时任务] 处理超时订单
    ├── 扫描超时未支付订单
    ├── 取消订单
    └── 回滚库存（Redis + DB）
```

#### 5.4 异常处理策略

| 异常场景 | 处理方式 | 数据一致性保证 |
|----------|----------|----------------|
| Redis 扣减成功，DB 失败 | 回滚 Redis 库存 | 强一致 |
| 秒杀订单创建失败 | 本地事务回滚，Redis 回滚 | 强一致 |
| 订单服务调用失败 | 本地消息表重试 | 最终一致 |
| 消息重复消费 | 幂等检查，跳过已处理 | 幂等保证 |
| 用户重复请求 | 分布式锁 + 用户限购检查 | 幂等保证 |

---

## 3. 技术选型

| 组件 | 技术方案 | 理由 |
|------|----------|------|
| 限流 | Redis + Lua | 分布式友好，性能高 |
| 验证码 | 自定义实现 | 轻量级，无需额外依赖 |
| 定时任务 | Spring @Scheduled | 已有基础设施，简单可靠 |
| 订单创建 | 调用订单服务 | 复用现有订单逻辑 |
| 分布式锁 | Redis SETNX | 简单高效，与现有 Redis 集成 |
| 消息队列 | 本地消息表 | 避免引入中间件，降低复杂度 |

---

## 4. 风险评估

1. **库存一致性风险：** Redis 和 DB 可能存在短暂不一致
   - 缓解：定时任务每30秒同步

2. **订单创建失败风险：** 秒杀成功但订单创建失败
   - 缓解：本地消息表重试机制

3. **限流误伤风险：** 正常用户可能被限流
   - 缓解：合理设置限流阈值，提供友好提示

4. **死信消息风险：** 超过重试次数的消息
   - 缓解：告警通知运维人员，提供手动补偿接口

---

## 5. 监控指标

1. **秒杀成功率：** 成功数 / 请求数
2. **库存扣减延迟：** Redis 扣减到 DB 扣减的时间差
3. **消息处理延迟：** 消息创建到处理完成的时间
4. **死信消息数：** 超过重试次数的消息
5. **限流触发次数：** 被限流的请求数

---

## 6. 实施计划

| 阶段 | 任务 | 预计工时 | 依赖 |
|------|------|----------|------|
| 阶段一 | 修复前后端类型一致性 | 2小时 | 无 |
| 阶段二 | 实现秒杀订单生成 | 4小时 | 阶段一 |
| 阶段三 | 创建 PC 秒杀专题页 | 3小时 | 阶段一 |
| 阶段四 | 添加限流和防刷机制 | 4小时 | 阶段二 |
| 阶段五 | 实现分布式事务和幂等性 | 6小时 | 阶段二 |

**总计预计工时：19小时**

---

## 7. 验收标准

1. 秒杀成功后能创建正式订单
2. 订单超时能自动取消并回滚库存
3. PC 端有独立的秒杀专题页
4. 秒杀接口有限流保护
5. 同一用户同一商品只能秒杀一次
6. 高并发下数据保持一致
7. 异常场景有告警和补偿机制
