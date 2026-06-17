# 秒杀功能验证指南

## 一、环境准备

### 1.1 数据库初始化

执行秒杀订单表和消息表的创建脚本：

```bash
mysql -u root -p shopmax < docs/sql/05-seckill-order.sql
```

### 1.2 初始化秒杀测试数据

```sql
-- 插入秒杀场次（如果不存在）
INSERT INTO mms_seckill_session (name, start_time, end_time, status) VALUES
('10:00 场', '2026-06-15 10:00:00', '2026-06-15 12:00:00', 1),
('14:00 场', '2026-06-15 14:00:00', '2026-06-15 16:00:00', 0),
('20:00 场', '2026-06-15 20:00:00', '2026-06-15 22:00:00', 0);

-- 插入秒杀商品（假设商品ID 1-5 存在于 pms_product 表）
INSERT INTO mms_seckill_product (session_id, product_id, sku_id, seckill_price, seckill_stock, limit_per_user, sort_order, status) VALUES
(1, 1, 1, 99.00, 100, 1, 1, 1),
(1, 2, 2, 199.00, 50, 1, 2, 1),
(1, 3, 3, 299.00, 30, 1, 3, 1);
```

---

## 二、后端服务启动

### 2.1 启动顺序

```bash
# 1. 启动 Redis
redis-server

# 2. 启动网关服务
cd shop-backend/shop-gateway
mvn spring-boot:run

# 3. 启动营销服务
cd shop-backend/shop-modules/shop-marketing-service
mvn spring-boot:run

# 4. 启动商品服务（如果需要）
cd shop-backend/shop-modules/shop-product-service
mvn spring-boot:run

# 5. 启动订单服务（如果需要）
cd shop-backend/shop-modules/shop-order-service
mvn spring-boot:run
```

### 2.2 验证后端接口

使用 curl 或 Postman 测试接口：

```bash
# 1. 获取进行中的秒杀场次
curl http://localhost:8080/api/v1/marketing/seckill/sessions/active

# 2. 获取场次商品（假设 sessionId=1）
curl http://localhost:8080/api/v1/marketing/seckill/products?sessionId=1

# 3. 加载库存到 Redis（需要 ADMIN 权限）
curl -X POST http://localhost:8080/api/v1/marketing/seckill/sessions/1/load-stock \
  -H "Authorization: Bearer <token>"

# 4. 执行秒杀
curl -X POST http://localhost:8080/api/v1/marketing/seckill/execute \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"sessionId": 1, "productId": 1}'
```

---

## 三、前端服务启动

### 3.1 启动 PC Web 端

```bash
cd shop-frontend/packages/web
npm install
npm run dev
```

访问 http://localhost:3100

### 3.2 启动 Mobile 移动端

```bash
cd shop-frontend/packages/mobile
npm install
npm run dev:h5
```

访问 http://localhost:5173

---

## 四、功能验证流程

### 4.1 PC 端秒杀页面验证

1. **访问秒杀页面**
   - 打开 http://localhost:3100/seckill
   - 或点击首页金刚区"限时秒杀"图标

2. **检查场次显示**
   - 应显示进行中的秒杀场次
   - 场次名称和时间应正确显示

3. **检查倒计时**
   - 应显示距离结束的倒计时
   - 倒计时应每秒更新

4. **检查商品展示**
   - 应显示秒杀商品列表
   - 商品图片、名称、价格应正确显示
   - 进度条应显示已抢百分比

5. **测试秒杀按钮**
   - 点击"立即秒杀"按钮
   - 应显示加载状态
   - 成功后应跳转到订单确认页

### 4.2 秒杀流程验证

1. **正常秒杀流程**
   ```
   1. 登录账号
   2. 进入秒杀页面
   3. 选择进行中的场次
   4. 点击"立即秒杀"
   5. 验证是否跳转到订单确认页
   6. 检查数据库是否创建秒杀订单
   ```

2. **重复秒杀验证**
   ```
   1. 对同一商品再次点击秒杀
   2. 应提示"您已参与过该秒杀活动"
   ```

3. **库存不足验证**
   ```
   1. 将秒杀库存设为 1
   2. 第一次秒杀应成功
   3. 第二次秒杀应提示"秒杀商品已售罄"
   ```

4. **超时订单验证**
   ```
   1. 创建秒杀订单但不支付
   2. 等待 15 分钟（或修改数据库 expire_time）
   3. 检查订单状态是否变为"已超时"
   4. 检查库存是否回滚
   ```

### 4.3 限流验证

1. **接口限流验证**
   ```
   1. 使用 JMeter 或脚本快速发送 100+ 请求
   2. 应返回 429 状态码
   3. 应提示"请求过于频繁，请稍后再试"
   ```

2. **分布式锁验证**
   ```
   1. 同时发送多个相同请求
   2. 只有一个请求成功
   3. 其他请求应提示"请求处理中，请勿重复提交"
   ```

---

## 五、数据库验证

### 5.1 检查秒杀订单表

```sql
-- 查看秒杀订单
SELECT * FROM oms_seckill_order ORDER BY create_time DESC LIMIT 10;

-- 检查订单状态分布
SELECT status, COUNT(*) as count FROM oms_seckill_order GROUP BY status;
```

### 5.2 检查本地消息表

```sql
-- 查看消息状态
SELECT status, COUNT(*) as count FROM mms_seckill_message GROUP BY status;

-- 查看待处理消息
SELECT * FROM mms_seckill_message WHERE status = 0;

-- 查看死信消息
SELECT * FROM mms_seckill_message WHERE status = 3;
```

### 5.3 检查库存一致性

```sql
-- 查看秒杀商品库存
SELECT id, product_id, seckill_stock FROM mms_seckill_product WHERE session_id = 1;

-- 检查 Redis 库存
redis-cli GET seckill:stock:1
```

---

## 六、日志验证

### 6.1 查看后端日志

```bash
# 查看营销服务日志
tail -f logs/shop-marketing-service.log | grep "秒杀"
```

### 6.2 关键日志

- `秒杀成功: sessionId=1, productId=1, userId=1, orderNo=SK...`
- `秒杀消息处理成功: orderNo=SK...`
- `超时订单已处理: orderNo=SK...`
- `Redis 库存已回滚: stockKey=seckill:stock:1`
- `秒杀消息已死信: orderNo=SK...`

---

## 七、监控指标验证

### 7.1 访问 Actuator 端点

```bash
# 查看所有指标
curl http://localhost:8092/actuator/metrics

# 查看秒杀相关指标
curl http://localhost:8092/actuator/metrics/seckill.request.total
curl http://localhost:8092/actuator/metrics/seckill.success.total
curl http://localhost:8092/actuator/metrics/seckill.failure.total
curl http://localhost:8092/actuator/metrics/seckill.ratelimit.hit
```

---

## 八、常见问题排查

### 8.1 秒杀失败：库存不足

**原因：** Redis 库存未加载

**解决：**
```bash
# 调用加载库存接口
curl -X POST http://localhost:8080/api/v1/marketing/seckill/sessions/1/load-stock \
  -H "Authorization: Bearer <token>"
```

### 8.2 秒杀失败：当前不在秒杀时间段内

**原因：** 场次时间配置错误

**解决：** 修改数据库中的场次时间
```sql
UPDATE mms_seckill_session 
SET start_time = '2026-06-15 00:00:00', end_time = '2026-06-15 23:59:59' 
WHERE id = 1;
```

### 8.3 前端页面空白

**原因：** API 请求失败

**解决：**
1. 检查后端服务是否启动
2. 检查浏览器控制台错误
3. 检查网络请求是否返回 401/403

### 8.4 订单未创建

**原因：** 消息处理任务未执行

**解决：**
1. 检查 `@Scheduled` 是否启用
2. 查看日志是否有异常
3. 检查消息表状态

---

## 九、性能测试

### 9.1 使用 JMeter 测试

1. 创建线程组：100 并发用户
2. 创建 HTTP 请求：POST /api/v1/marketing/seckill/execute
3. 添加定时器：100ms 间隔
4. 添加断言：检查响应状态码
5. 运行测试，观察：
   - 成功率
   - 响应时间
   - 限流触发次数

### 9.2 预期结果

- 成功率：库存充足时应接近 100%
- 响应时间：平均 < 200ms
- 限流：高并发时应触发限流
- 库存一致性：Redis 和 DB 库存应一致

---

## 十、验证清单

- [ ] 数据库表创建成功
- [ ] 秒杀场次数据插入成功
- [ ] 秒杀商品数据插入成功
- [ ] Redis 服务正常运行
- [ ] 后端服务启动成功
- [ ] 前端页面访问正常
- [ ] 秒杀场次显示正确
- [ ] 倒计时功能正常
- [ ] 商品列表显示正确
- [ ] 秒杀按钮功能正常
- [ ] 秒杀订单创建成功
- [ ] 重复秒杀拦截正常
- [ ] 库存扣减正确
- [ ] 超时订单处理正常
- [ ] 库存回滚正常
- [ ] 限流功能正常
- [ ] 监控指标正常
- [ ] 日志输出正常
