# ShopMax 微服务组件验证方案与答辩指南

> 分布式框架大作业答辩参考文档

---

## 一、架构总览

### 1.1 技术栈

| 组件 | 版本 | 作用 |
|------|------|------|
| Java | 21 | 运行时 |
| Spring Boot | 3.2.5 | 微服务基础框架 |
| Spring Cloud | 2023.0.1 | 微服务治理 |
| Spring Cloud Alibaba | 2023.0.1.0 | Nacos/Sentinel 集成 |
| Nacos | v2.4.3 | 服务注册发现 + 配置中心 |
| Spring Cloud Gateway | 2023.0.1 | API 网关 |
| OpenFeign | 2023.0.1 | 声明式 HTTP 调用 |
| Sentinel | 1.8.8 | 流量控制 + 熔断降级 |

### 1.2 微服务拓扑

```
                        ┌─────────────────┐
                        │   Client 浏览器   │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │  Nginx (可选)    │
                        └────────┬────────┘
                                 │
                   ┌─────────────▼─────────────┐
                   │   Gateway (port 8080)      │
                   │   - JWT 认证过滤器          │
                   │   - Redis 限流过滤器        │
                   │   - 13 条路由规则           │
                   └─────────────┬─────────────┘
                                 │ lb:// (Nacos 服务发现)
         ┌───────┬───────┬───────┼───────┬───────┬───────┐
         ▼       ▼       ▼       ▼       ▼       ▼       ▼
      ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
      │auth │ │user │ │product││order│ │payment││live │ │community│
      │8083 │ │8081 │ │8082  ││8088 │ │8084  ││8086 │ │8087    │
      └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘
         │       │       │       │       │       │       │
         │    Feign 调用  │       │       │       │       │
         └───────┴───────┴───────┴───────┴───────┴───────┘
                                 │
                   ┌─────────────▼─────────────┐
                   │   Nacos Server (8848)      │
                   │   - 服务注册中心            │
                   │   - 配置管理中心            │
                   └───────────────────────────┘
```

### 1.3 服务清单

| 服务名 | 端口 | 职责 |
|--------|------|------|
| shop-gateway | 8080 | API 网关，统一入口 |
| shop-auth | 8083 | 认证中心（登录/注册/Token） |
| shop-user-service | 8081 | 用户管理 |
| shop-product-service | 8082 | 商品管理 |
| shop-order-service | 8088 | 订单管理 |
| shop-payment-service | 8084 | 支付管理 |
| shop-marketing-service | 8092 | 营销活动 |
| shop-live-service | 8086 | 直播服务 |
| shop-community-service | 8087 | 社区内容 |
| shop-admin-service | 8089 | 管理后台 |
| shop-file-service | 8090 | 文件存储 |
| shop-customer-service | 8091 | 智能客服 |

---

## 二、Nacos 详解

### 2.1 Nacos 在项目中的业务作用

**两个核心功能：**

1. **服务注册与发现**：所有 12 个微服务启动时自动注册到 Nacos，Gateway 通过服务名（如 `shop-user-service`）发现服务实例，实现 `lb://` 负载均衡路由。

2. **外部化配置中心**：各服务通过 `spring.config.import: optional:nacos:${spring.application.name}.yaml` 从 Nacos 拉取配置，实现配置与代码分离、热更新。

### 2.2 项目中的 Nacos 配置

**服务端配置**（每个服务的 `application.yml`）：

```yaml
spring:
  cloud:
    nacos:
      username: nacos
      password: nacos
      discovery:
        enabled: true
        server-addr: localhost:8848    # Nacos 服务发现地址
      config:
        enabled: true
        server-addr: localhost:8848    # Nacos 配置中心地址
        file-extension: yaml
  config:
    import: optional:nacos:${spring.application.name}.yaml  # 从 Nacos 拉取配置
```

**Docker 部署**（`docker-compose.yml`）：

```yaml
nacos:
  image: nacos/nacos-server:v2.4.3
  ports:
    - "8848:8848"
    - "9848:9848"
    - "9849:9849"
  environment:
    MODE: standalone
    NACOS_AUTH_ENABLE: "true"
```

**主类注解**：

```java
@SpringBootApplication
@EnableDiscoveryClient   // 启用服务发现
public class UserApplication { ... }
```

### 2.3 Nacos 验证方案

#### 验证 1：服务注册

```bash
# 1. 启动 Nacos
docker compose up -d nacos

# 2. 启动任意微服务（如 shop-user-service）

# 3. 访问 Nacos 控制台
#    URL: http://localhost:8848/nacos
#    账号: nacos / nacos

# 4. 左侧菜单 "服务管理" → "服务列表"
#    预期：能看到 shop-user-service，状态为 "健康"
```

#### 验证 2：服务发现（通过 Gateway 路由）

```bash
# 1. 确保 shop-user-service 已注册到 Nacos

# 2. 通过 Gateway 访问（注意：lb:// 依赖 Nacos 服务发现）
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <token>"

# 预期：Gateway 通过 Nacos 发现 user-service 实例，转发请求并返回结果
```

#### 验证 3：配置中心热更新

```bash
# 1. 在 Nacos 控制台创建配置
#    Data ID: shop-user-service.yaml
#    Group: DEFAULT_GROUP
#    格式: YAML
#    内容:
#      custom:
#        message: "hello from nacos"

# 2. 在 user-service 代码中读取配置
#    @Value("${custom.message}")
#    private String message;

# 3. 修改 Nacos 中的配置值，观察服务日志
#    预期：服务自动刷新配置，无需重启
```

》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》书签标记（6.17）
#### 验证 4：多实例负载均衡

```bash
# 1. 启动两个 user-service 实例（不同端口）
#    实例1: server.port=8081
#    实例2: server.port=8181

# 2. 在 Nacos 控制台确认两个实例都注册成功

# 3. 多次调用同一接口
for i in {1..10}; do
  curl http://localhost:8080/api/v1/users/1 -H "Authorization: Bearer <token>"
done

# 4. 观察两个实例的日志
# 预期：请求交替落在两个实例上（轮询策略）
```

### 2.4 Nacos 集群部署

```
┌─────────────────────────────────────────────────┐
│                 Nacos 集群 (3 节点)              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │ Nacos-1  │  │ Nacos-2  │  │ Nacos-3  │      │
│  │ :8848    │  │ :8849    │  │ :8850    │      │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘      │
│       │              │              │            │
│       └──────────────┼──────────────┘            │
│                      │                           │
│              ┌───────▼───────┐                   │
│              │  MySQL 主从    │                   │
│              │  共享配置数据   │                   │
│              └───────────────┘                   │
│                                                  │
│  前端: Nginx 负载均衡 (upstream nacos-cluster)   │
└─────────────────────────────────────────────────┘
```

**部署步骤：**

```bash
# 1. 准备 MySQL 数据库
#    执行 Nacos 的 MySQL 初始化脚本 (nacos-mysql.sql)

# 2. 修改 cluster.conf（3 个节点各自的 IP）
#    192.168.1.10:8848
#    192.168.1.11:8848
#    192.168.1.12:8848

# 3. 每个节点的 application.properties 配置
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://mysql-host:3306/nacos?characterEncoding=utf8
db.user=root
db.password=password

# 4. 启动每个节点
sh startup.sh

# 5. Nginx 配置
upstream nacos-cluster {
    server 192.168.1.10:8848;
    server 192.168.1.11:8848;
    server 192.168.1.12:8848;
}
server {
    listen 8848;
    location / {
        proxy_pass http://nacos-cluster;
    }
}

# 6. 微服务配置改为 Nginx 地址
spring.cloud.nacos.discovery.server-addr=nginx-host:8848
```

---

## 三、Gateway 详解

### 3.1 Gateway 在项目中的业务作用

**四大职责：**

1. **统一入口**：所有客户端请求统一经过 Gateway（端口 8080），再路由到后端微服务。

2. **路由转发**：根据 URL 路径匹配规则，将请求转发到对应的微服务。使用 `lb://` 协议实现基于 Nacos 的负载均衡。

3. **JWT 认证**：`AuthGlobalFilter` 从请求头提取 Token，验证 JWT 有效性，将 `X-User-Id` 和 `X-User-Role` 注入下游请求头。

4. **流量控制**：`RateLimitFilter` 使用 Redis 滑动窗口对秒杀接口进行 IP/用户级限流。

### 3.2 项目中的路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 认证服务
        - id: shop-auth
          uri: lb://shop-auth          # lb:// 表示负载均衡
          predicates:
            - Path=/api/v1/auth/**     # 路径匹配规则

        # 用户服务
        - id: shop-user-service
          uri: lb://shop-user-service
          predicates:
            - Path=/api/v1/users/**

        # 商品服务（多路径）
        - id: shop-product-service
          uri: lb://shop-product-service
          predicates:
            - Path=/api/v1/products/**,/api/v1/categories/**,/api/v1/brands/**

        # WebSocket 路由
        - id: shop-customer-service-ws
          uri: lb://shop-customer-service
          predicates:
            - Path=/ws/cs/**
```

### 3.3 自定义过滤器

#### AuthGlobalFilter（认证过滤器，order=-100）

```
请求进入 → 是 OPTIONS？→ 放行
         → 是白名单路径？→ 放行
         → 是公开路径？→ 放行
         → 提取 Token → Token 为空？→ 返回 401
                        → Token 无效？→ 返回 401
                        → Token 有效 → 注入 X-User-Id/X-User-Role → 转发
```

白名单路径：
- `/api/v1/auth/login`、`/api/v1/auth/register`、`/api/v1/auth/sms/send` 等

公开路径：
- `/api/v1/products`（GET）、`/api/v1/categories`、`/api/v1/live`、`/api/v1/community` 等

#### RateLimitFilter（限流过滤器，order=-200）

```
请求进入 → 是秒杀接口 (/seckill/execute)？→ 否 → 放行
                                           → 是 → IP 限流 (10/s)
                                                → 用户限流 (5/s)
                                                → 秒杀限流 (3/s)
                                                → 超限？→ 返回 429
```

### 3.4 Gateway 验证方案

#### 验证 1：路由转发

```bash
# 测试用户服务路由
curl http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <token>"
# 预期：返回用户信息（说明路由到 user-service 成功）

# 测试商品服务路由（公开接口，无需 Token）
curl http://localhost:8080/api/v1/products?pageNum=1&pageSize=5
# 预期：返回商品列表

# 测试不存在的路由
curl http://localhost:8080/api/v1/not-exist
# 预期：返回 404
```

#### 验证 2：JWT 认证

```bash
# 不带 Token 访问需要认证的接口
curl http://localhost:8080/api/v1/users/me
# 预期：返回 {"code":401,"message":"未提供认证Token"}

# 带无效 Token
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer invalid-token"
# 预期：返回 {"code":401,"message":"Token无效或已过期"}

# 带有效 Token
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <valid-token>"
# 预期：返回用户信息
```

#### 验证 3：限流

```bash
# 安装压测工具 (Apache Bench)
# 模拟 100 个请求，10 并发
ab -n 100 -c 10 \
  -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/marketing/seckill/execute

# 预期：部分请求返回 429 (Too Many Requests)
# 日志中可以看到 "IP 限流触发"、"秒杀接口限流触发"
```

#### 验证 4：负载均衡

```bash
# 1. 启动两个 user-service 实例（端口 8081 和 8181）
# 2. 在 Nacos 确认两个实例都注册

# 3. 在两个实例中添加日志标识
#    实例1: log.info("实例1 处理请求")
#    实例2: log.info("实例2 处理请求")

# 4. 多次调用
for i in {1..10}; do
  curl http://localhost:8080/api/v1/users/me \
    -H "Authorization: Bearer <token>" 2>/dev/null
done

# 5. 观察两个实例的日志输出
# 预期：请求交替落在两个实例上（默认轮询策略）
```

### 3.5 Gateway 集群部署

```
┌─────────────────────────────────────┐
│           Nginx (端口 80)           │
│   upstream gateway-cluster {        │
│       server gateway-1:8080;        │
│       server gateway-2:8080;        │
│   }                                 │
└──────────────┬──────────────────────┘
               │
       ┌───────┴───────┐
       ▼               ▼
┌─────────────┐ ┌─────────────┐
│ Gateway-1   │ │ Gateway-2   │
│ :8080       │ │ :8080       │
└──────┬──────┘ └──────┬──────┘
       │               │
       └───────┬───────┘
               │ lb:// (Nacos 服务发现)
       ┌───────┴───────┐
       ▼               ▼
  ┌─────────┐    ┌─────────┐
  │UserSvc-1│    │UserSvc-2│
  │ :8081   │    │ :8181   │
  └─────────┘    └─────────┘
```

**关键点：**
- Gateway 是无状态的，可以直接水平扩展
- 多个 Gateway 实例通过 Nginx 做负载均衡
- Gateway 本身也注册到 Nacos，但客户端不直接访问 Nacos 中的 Gateway
- Session/Token 信息存储在 Redis 中（共享），所以 Gateway 实例间无需同步状态

---

## 四、Feign 详解

### 4.1 Feign 在项目中的业务作用

**核心价值：将服务间的 HTTP 调用简化为 Java 接口方法调用。**

项目中的 Feign 调用链路：

```
shop-auth ──Feign──→ shop-user-service
  - 用户登录 (login)
  - 用户注册 (register)
  - 发送验证码 (sendSmsCode / sendEmailCode)
  - 检查邮箱 (existsByEmail)
  - 重置密码 (resetPassword)
  - 申请开店 (applyStore)

shop-customer-service ──Feign──→ shop-product-service
  - 搜索商品 (searchProducts)
  - 搜索分类 (searchCategories)

shop-customer-service ──Feign──→ shop-order-service
  - 查询订单 (getByOrderNo)
```

### 4.2 Feign 架构设计

```
shop-common-feign (共享模块)
├── client/
│   ├── UserServiceClient.java        ← @FeignClient 接口
│   ├── ProductServiceClient.java
│   └── OrderServiceClient.java
├── dto/
│   ├── user/                          ← 共享 DTO (请求/响应)
│   ├── product/
│   └── order/
└── fallback/
    ├── UserServiceClientFallbackFactory.java   ← 降级工厂
    ├── ProductServiceClientFallbackFactory.java
    └── OrderServiceClientFallbackFactory.java
```

### 4.3 FeignClient 接口示例

```java
@FeignClient(
    name = "shop-user-service",                    // Nacos 中的服务名
    path = "/internal/users",                      // 内部端点前缀
    fallbackFactory = UserServiceClientFallbackFactory.class  // 降级工厂
)
public interface UserServiceClient {

    @PostMapping("/login")
    Result<UserLoginResponse> login(@RequestBody UserLoginRequest request);

    @PostMapping("/register")
    Result<UserRegisterResponse> register(@RequestBody UserRegisterRequest request);

    @GetMapping("/check-email")
    Result<Boolean> existsByEmail(@RequestParam("email") String email);
}
```

### 4.4 降级处理 (FallbackFactory)

```java
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("用户服务调用失败: {}", cause.getMessage(), cause);
        return new UserServiceClient() {
            @Override
            public Result<UserLoginResponse> login(UserLoginRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }
            // ... 其他方法的降级实现
        };
    }
}
```

### 4.5 Feign 验证方案

#### 验证 1：正常调用

```bash
# 1. 确保 shop-user-service 和 shop-auth 都已启动

# 2. 通过 Gateway 调用登录接口
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 预期：返回登录成功信息（包含 token）
# 说明：auth 服务通过 Feign 调用了 user-service 的 /internal/users/login
```

#### 验证 2：降级处理

```bash
# 1. 停止 shop-user-service

# 2. 再次调用登录接口
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 预期：返回 {"code":503,"message":"用户服务暂时不可用，请稍后再试"}
# 说明：Feign 调用失败，触发 FallbackFactory 降级
```

#### 验证 3：超时配置

```yaml
# application.yml
feign:
  client:
    config:
      default:
        connectTimeout: 5000    # 连接超时 5 秒
        readTimeout: 10000      # 读取超时 10 秒
  sentinel:
    enabled: true               # 启用 Sentinel 熔断
```

```bash
# 模拟慢接口（在 user-service 中添加 Thread.sleep(15000)）
# 调用后预期：超时降级返回 503
```

#### 验证 4：日志级别

```yaml
# application.yml
logging:
  level:
    com.shop.common.feign.client: DEBUG  # Feign 调用日志
```

```bash
# 调用接口后观察日志
# 预期：能看到 Feign 的请求 URL、请求头、响应状态码、响应体
```

### 4.6 Feign 的负载均衡

Feign 内置了 LoadBalancer，天然支持负载均衡：

```
UserServiceClient.login()
    │
    ▼
Feign 代理 → 解析 "shop-user-service" 服务名
    │
    ▼
LoadBalancer → 从 Nacos 获取实例列表
    │           [实例1: 192.168.1.10:8081]
    │           [实例2: 192.168.1.11:8181]
    │
    ▼
选择实例（默认轮询）→ 发送 HTTP 请求
```

---

## 五、Sentinel 详解

### 5.1 Sentinel 在项目中的业务作用

**三大能力：**

1. **流量控制（Flow Control）**：限制接口的 QPS，防止突发流量压垮服务。
   - 项目应用：秒杀接口限流、全局 QPS 限制

2. **熔断降级（Circuit Breaking）**：当下游服务异常率/慢调用比例超过阈值时，自动熔断，返回降级响应。
   - 项目应用：Feign 调用熔断

3. **系统保护（System Protection）**：根据系统负载（CPU、内存、QPS）自动保护。
   - 项目应用：全局系统保护

### 5.2 Sentinel 在项目中的配置

**依赖**（`shop-modules/pom.xml`）：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

**Gateway 专用依赖**：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
</dependency>
```

**配置**（每个服务的 `application.yml`）：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8858    # Sentinel Dashboard 地址
        port: 8719                   # 与 Sentinel 通信端口

feign:
  sentinel:
    enabled: true                    # Feign + Sentinel 整合
```

**Docker 部署**：

```yaml
sentinel:
  image: bladex/sentinel-dashboard:1.8.8
  ports:
    - "8858:8858"
  environment:
    JAVA_OPT: "-Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858"
```

### 5.3 Sentinel 核心概念

| 概念 | 说明 | 类比 |
|------|------|------|
| **资源 (Resource)** | 被保护的代码块/接口 | 水管中的水 |
| **规则 (Rule)** | 对资源的保护策略 | 水管的阀门 |
| **流控规则** | 限制 QPS/并发数 | 水龙头流量 |
| **降级规则** | 慢调用/异常比例熔断 | 漏水保护阀 |
| **授权规则** | 黑白名单 | 门禁系统 |

### 5.4 流控规则详解

| 字段 | 说明 | 示例 |
|------|------|------|
| 资源名 | 被保护的接口路径 | `/api/v1/marketing/seckill/execute` |
| 阈值类型 | QPS 或并发线程数 | QPS |
| 单机阈值 | 每秒最大请求数 | 10 |
| 流控模式 | 直接/关联/链路 | 直接 |
| 流控效果 | 快速失败/Warm Up/排队等待 | 快速失败 |

### 5.5 降级规则详解

| 策略 | 说明 | 阈值 | 时间窗口 |
|------|------|------|----------|
| 慢调用比例 | 响应时间超过阈值的比例 | RT=1000ms, 比例=0.5 | 10s |
| 异常比例 | 异常请求的比例 | 比例=0.5 | 10s |
| 异常数 | 异常请求的数量 | 数量=5 | 10s |

### 5.6 Sentinel 验证方案

#### 验证 1：Dashboard 服务注册

```bash
# 1. 启动 Sentinel Dashboard
docker compose up -d sentinel

# 2. 启动任意微服务（如 shop-user-service）

# 3. 访问 Sentinel Dashboard
#    URL: http://localhost:8858
#    账号: sentinel / sentinel

# 4. 左侧菜单 "实时监控"
#    预期：能看到 shop-user-service 在服务列表中
#    注意：需要先有请求，服务才会出现在列表中

# 5. 先调用一次接口触发注册
curl http://localhost:8080/api/v1/products
```

#### 验证 2：QPS 流控

```bash
# 1. 在 Sentinel Dashboard 中配置流控规则
#    资源名: /api/v1/products (GET)
#    阈值类型: QPS
#    单机阈值: 5

# 2. 压测
ab -n 50 -c 20 http://localhost:8080/api/v1/products

# 3. 观察 Sentinel Dashboard 的 "实时监控"
#    预期：通过 QPS 稳定在 5 左右，拒绝 QPS 随并发增加

# 4. 观察响应
#    预期：超出阈值的请求返回 "Blocked by Sentinel (flow limiting)"
```

#### 验证 3：Feign 熔断降级

```bash
# 1. 在 Sentinel Dashboard 配置降级规则
#    资源名: POST:http://shop-user-service/internal/users/login
#    策略: 慢调用比例
#    最大 RT: 1000ms (响应超过 1 秒算慢调用)
#    慢调用比例阈值: 0.5 (50% 慢调用触发熔断)
#    熔断时长: 10s

# 2. 在 user-service 的 login 方法中模拟慢响应
#    Thread.sleep(2000); // 2 秒 > 1 秒阈值

# 3. 连续调用登录接口
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"123456"}'
done

# 4. 观察
#    前几次：返回慢响应（2 秒后返回）
#    触发熔断后：立即返回降级响应 "用户服务暂时不可用"
#    10 秒窗口后：自动恢复，再次尝试正常调用
```

#### 验证 4：Gateway 网关限流

```bash
# 1. 在 Sentinel Dashboard 配置网关流控规则
#    资源名: shop-product-service (路由 ID)
#    阈值类型: QPS
#    单机阈值: 10

# 2. 压测
ab -n 100 -c 30 http://localhost:8080/api/v1/products

# 3. 观察 Sentinel Dashboard 的 "网关流控"
#    预期：超出阈值的请求返回 429
```

### 5.7 Sentinel 集群部署

```
┌─────────────────────────────────────────┐
│          Sentinel Dashboard             │
│          (单节点, :8858)                 │
│          - 规则管理                      │
│          - 实时监控                      │
│          - 集群拓扑                      │
└──────────────┬──────────────────────────┘
               │ 推送规则
    ┌──────────┼──────────┐
    ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐
│Token   │ │Token   │ │Token   │
│Server-1│ │Server-2│ │Server-3│
│(独立部署│ │(独立部署│ │(独立部署│
│或嵌入) │ │或嵌入) │ │或嵌入) │
└───┬────┘ └───┬────┘ └───┬────┘
    │          │          │
    ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐
│Service-1│ │Service-2│ │Service-3│
│(业务服务)│ │(业务服务)│ │(业务服务)│
└────────┘ └────────┘ └────────┘
```

**集群模式说明：**
- **Dashboard**：单节点部署，负责规则管理和监控展示
- **Token Server**：负责集群级别的 QPS 统计和令牌分发
- **Token Client**：业务服务中的 Sentinel 客户端，向 Token Server 申请令牌
- 独立模式：Token Server 独立部署，高可用
- 嵌入模式：Token Server 嵌入在某个业务服务中，简单但可用性较低

---

## 六、综合验证流程

### 6.1 完整登录流程（涉及全部 4 个组件）

```
1. 客户端 POST /api/v1/auth/login
   │
2. ▼ Gateway (AuthGlobalFilter)
   │ 检查白名单 → /api/v1/auth/login 在白名单中 → 放行
   │ 检查限流 → 非秒杀接口 → 放行
   │
3. ▼ Gateway 路由匹配
   │ Path=/api/v1/auth/** → 匹配 shop-auth 路由
   │ uri=lb://shop-auth → 通过 Nacos 发现 auth 服务实例
   │
4. ▼ Nacos 服务发现
   │ 查询 shop-auth 服务 → 返回实例地址 127.0.0.1:8083
   │
5. ▼ Gateway 转发请求到 auth 服务
   │
6. ▼ AuthController 接收请求
   │ 调用 userServiceClient.login(request) → Feign 调用
   │
7. ▼ Feign + LoadBalancer
   │ 服务名: shop-user-service
   │ 通过 Nacos 发现实例 → 选择实例（轮询）
   │ 发送 HTTP POST /internal/users/login
   │
8. ▼ Sentinel 监控
   │ 记录本次调用：RT、成功/失败
   │ 检查熔断状态 → 正常 → 放行
   │
9. ▼ UserInternalController 接收请求
   │ 调用 userService.login() → 验证密码 → 生成 JWT
   │ 返回 Result<UserLoginResponse>
   │
10. ▼ Feign 接收响应
    │ 返回给 AuthController
    │
11. ▼ AuthController 返回给客户端
    │ {"code":200,"data":{"token":"xxx","username":"testuser"}}
```

### 6.2 端到端测试脚本

```bash
#!/bin/bash
# ShopMax 微服务组件验证脚本

GATEWAY="http://localhost:8080"
NACOS="http://localhost:8848"
SENTINEL="http://localhost:8858"

echo "========== 1. Nacos 验证 =========="
echo "检查 Nacos 状态..."
curl -s "$NACOS/nacos/v1/ns/service/list?pageNo=1&pageSize=10" | python3 -m json.tool

echo ""
echo "========== 2. Gateway 路由验证 =========="
echo "测试公开接口（商品列表）..."
curl -s "$GATEWAY/api/v1/products?pageNum=1&pageSize=3" | python3 -m json.tool

echo ""
echo "测试认证接口（无 Token）..."
curl -s "$GATEWAY/api/v1/users/me" | python3 -m json.tool

echo ""
echo "========== 3. 登录获取 Token =========="
echo "登录..."
LOGIN_RESULT=$(curl -s -X POST "$GATEWAY/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}')
echo "$LOGIN_RESULT" | python3 -m json.tool
TOKEN=$(echo "$LOGIN_RESULT" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

echo ""
echo "========== 4. Feign 验证 =========="
echo "带 Token 访问用户信息（auth → user Feign 调用）..."
curl -s "$GATEWAY/api/v1/users/me" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo ""
echo "========== 5. Sentinel 验证 =========="
echo "检查 Sentinel Dashboard..."
curl -s "$SENTINEL/" | head -5

echo ""
echo "========== 验证完成 =========="
```

---

## 七、答辩常见问题与回答

### Q1: 为什么选择 Nacos 而不是 Eureka？

| 对比项 | Nacos | Eureka |
|--------|-------|--------|
| 服务发现 | 支持 AP/CP 模式 | 仅 AP 模式 |
| 配置中心 | 内置 | 不支持（需配合 Config Server） |
| 健康检查 | TCP/HTTP/MySQL 多种 | 仅客户端心跳 |
| 管理界面 | 功能丰富 | 简单 |
| 社区活跃 | 阿里维护，更新频繁 | Netflix 已停止维护 |
| Spring Cloud 集成 | 完美支持 | 完美支持 |

**回答**：Nacos 同时提供服务发现和配置中心，减少组件数量；支持 AP/CP 可切换，适应不同场景；社区活跃，Spring Cloud Alibaba 官方推荐。

### Q2: Gateway 和 Zuul 的区别？

| 对比项 | Spring Cloud Gateway | Zuul 1.x |
|--------|---------------------|----------|
| 编程模型 | WebFlux（异步非阻塞） | Servlet（同步阻塞） |
| 性能 | 高（Reactor Netty） | 一般（Tomcat 线程池） |
| 长连接 | 支持 WebSocket | 不支持 |
| 限流 | 内置 RequestRateLimiter | 需自己实现 |
| 路由断言 | 丰富（Path/Method/Header 等） | 简单 |
| 过滤器 | GlobalFilter + GatewayFilter | Pre/Post Filter |

**回答**：Gateway 基于 WebFlux 异步非阻塞模型，性能远优于 Zuul 1.x 的同步阻塞模型；原生支持 WebSocket（本项目的客服和直播功能需要）；路由断言和过滤器机制更灵活。

### Q3: Feign 和 RestTemplate 的区别？

```java
// RestTemplate 方式（需要手动拼 URL）
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<Result> response = restTemplate.postForEntity(
    "http://shop-user-service/internal/users/login",
    request,
    Result.class
);

// Feign 方式（声明式，像调用本地方法）
@FeignClient(name = "shop-user-service")
public interface UserServiceClient {
    @PostMapping("/internal/users/login")
    Result<UserLoginResponse> login(@RequestBody UserLoginRequest request);
}

// 使用
Result<UserLoginResponse> result = userServiceClient.login(request);
```

**回答**：
- Feign 是声明式的，接口即文档，代码更简洁
- 自动集成 LoadBalancer，无需手动处理服务发现
- 支持 FallbackFactory 降级处理
- 支持请求/响应拦截器，便于统一处理认证、日志
- RestTemplate 需要手动拼 URL、处理序列化、处理异常

### Q4: Sentinel 的熔断策略有哪些？

三种策略：

1. **慢调用比例 (SLOW_REQUEST_RATIO)**
   - 当请求响应时间超过阈值（如 1000ms）的比例达到设定值时触发熔断
   - 适用：下游服务响应变慢的场景

2. **异常比例 (ERROR_RATIO)**
   - 当异常请求的比例达到设定值时触发熔断
   - 适用：下游服务开始抛异常的场景

3. **异常数 (ERROR_COUNT)**
   - 当异常请求的数量达到设定值时触发熔断
   - 适用：对异常数量有严格限制的场景

**熔断状态机**：
```
CLOSED（正常）→ 异常率超过阈值 → OPEN（熔断）
OPEN → 等待时间窗口结束 → HALF-OPEN（半开）
HALF-OPEN → 探测请求成功 → CLOSED
HALF-OPEN → 探测请求失败 → OPEN
```

### Q5: 如何保证微服务的高可用？

从四个层面回答：

1. **服务层**：多实例部署 + Nacos 健康检查
   - 每个服务至少部署 2 个实例
   - Nacos 自动剔除不健康实例

2. **网关层**：Gateway 集群 + Nginx 负载均衡
   - Gateway 无状态，可水平扩展
   - Nginx 做 Gateway 前端的负载均衡

3. **调用层**：Feign + Sentinel 熔断降级
   - Feign FallbackFactory 提供降级响应
   - Sentinel 熔断防止级联故障

4. **流量层**：Sentinel 流控 + Gateway 限流
   - 防止突发流量压垮服务
   - 秒杀等高并发场景的流量削峰

### Q6: 服务间调用失败怎么办？

**三层防护**：

```
第一层：Feign 重试机制
  - 配置 ribbon.MaxAutoRetries=1
  - 对读请求自动重试

第二层：Feign FallbackFactory
  - 返回降级响应（如 "服务暂时不可用"）
  - 记录日志，便于排查

第三层：Sentinel 熔断
  - 慢调用/异常比例触发熔断
  - 熔断期间直接返回降级响应，不再调用下游
  - 时间窗口后自动恢复
```

### Q7: 如何实现灰度发布？

**基于 Nacos 元数据的灰度方案**：

```yaml
# 服务实例注册时携带版本元数据
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: v2    # 灰度版本
```

```java
// 自定义 LoadBalancer，根据请求头选择版本
@Bean
public ReactorLoadBalancer<ServiceInstance> reactorServiceInstance(
        LoadBalancerClientFactory factory) {
    return new VersionAwareLoadBalancer(
        factory.getLazyProvider(name, ServiceInstanceListSupplier.class),
        name
    );
}
```

```bash
# 客户端通过请求头指定版本
curl -H "X-Version: v2" http://gateway/api/v1/products

# 旧版本请求走 v1 实例，新版本请求走 v2 实例
```

### Q8: Gateway 的过滤器执行顺序？

```
请求进入
  │
  ▼
GlobalFilter (order=-200)  ← RateLimitFilter（限流）
  │
  ▼
GlobalFilter (order=-100)  ← AuthGlobalFilter（认证）
  │
  ▼
GatewayFilter (路由级过滤器)
  │
  ▼
转发到下游服务
  │
  ▼
响应返回（逆序经过过滤器）
```

order 值越小，优先级越高。本项目中：
- RateLimitFilter: order=-200（先限流，拒绝的请求不需要认证）
- AuthGlobalFilter: order=-100（再认证，通过后才转发）

### Q9: Nacos 的 AP 和 CP 模式有什么区别？

| 模式 | 一致性 | 可用性 | 适用场景 |
|------|--------|--------|----------|
| AP (Distro) | 最终一致 | 高可用 | 服务发现（推荐） |
| CP (Raft) | 强一致 | 较低 | 配置管理、选举 |

```yaml
# 服务发现用 AP 模式（默认）
spring.cloud.nacos.discovery.consistency=AP

# 配置管理用 CP 模式
spring.cloud.nacos.config.consistency=CP
```

**回答**：服务发现对可用性要求高（发现不了服务比发现旧数据更严重），用 AP；配置管理对一致性要求高（配置不一致可能导致业务异常），用 CP。

### Q10: Sentinel 的限流效果有哪些？

1. **快速失败（默认）**：直接拒绝，返回 429
2. **Warm Up**：预热，QPS 从阈值/冷加载因子开始，逐步升至阈值
   - 适用：秒杀开始瞬间防止流量暴增
3. **排队等待**：匀速通过，多余的请求排队
   - 适用：消息队列等需要平滑处理的场景

---

## 八、关键配置文件索引

| 文件 | 作用 |
|------|------|
| `shop-backend/pom.xml` | 父 POM，版本管理 |
| `shop-backend/shop-gateway/pom.xml` | Gateway 依赖（WebFlux、Nacos、Sentinel） |
| `shop-backend/shop-common/shop-common-feign/pom.xml` | Feign 共享模块 |
| `shop-backend/shop-modules/pom.xml` | 业务模块公共依赖 |
| `shop-backend/shop-gateway/src/main/resources/application.yml` | Gateway 路由配置 |
| `shop-backend/shop-gateway/src/main/java/.../AuthGlobalFilter.java` | JWT 认证过滤器 |
| `shop-backend/shop-gateway/src/main/java/.../RateLimitFilter.java` | 限流过滤器 |
| `shop-backend/shop-common/shop-common-feign/src/main/java/.../client/*.java` | FeignClient 接口 |
| `shop-backend/shop-common/shop-common-feign/src/main/java/.../fallback/*.java` | 降级工厂 |
| `shop-backend/shop-common/shop-common-security/src/main/java/.../SecurityConfig.java` | Spring Security 配置 |
| `docker-compose.yml` | 基础设施（Nacos、Sentinel、MinIO、SRS） |
