# 后端开发约束规范

## 一、技术栈约束

### 1.1 必须使用的技术
- **JDK**: OpenJDK 21 (强制，pom.xml 指定 <java.version>21</java.version>)
- **SpringBoot**: 3.2.x (强制)
- **SpringCloud**: 2023.0.x (微服务架构)
- **MySQL**: 8.0+ (强制)
- **MyBatis-Plus**: 3.5.x (持久层框架，强制)
- **Redis**: 7.x (缓存，强制)
- **Maven**: 3.9+ (构建工具，强制)

### 1.2 禁止使用的技术
- 禁止使用 SpringBoot 2.x
- 禁止使用 JDK 8/11/17
- 禁止使用 JPA/Hibernate (统一用 MyBatis-Plus)
- 禁止使用 Apache HttpClient (统一用 OkHttp/WebClient)
- 禁止使用 Fastjson (统一用 Jackson)
- 禁止使用 Apache Commons Lang2 (用 Lang3)

---

## 二、项目结构约束

### 2.1 模块命名规范
```
shop-{module-name}
├── shop-gateway          # 网关服务
├── shop-auth             # 认证服务
├── shop-common           # 公共模块(不直接部署)
│   ├── shop-common-core
│   ├── shop-common-web
│   ├── shop-common-redis
│   └── shop-common-security
└── shop-modules          # 业务模块
    ├── shop-user-service         # 用户服务
    ├── shop-product-service      # 商品服务
    ├── shop-order-service        # 订单服务
    ├── shop-payment-service      # 支付服务
    ├── shop-marketing-service    # 营销服务
    ├── shop-live-service         # 直播服务
    ├── shop-community-service    # 社区服务
    └── shop-admin-service        # 管理服务
```

### 2.2 包结构规范
```
com.shop.{module}
├── config          # 配置类
├── controller      # 控制器层
│   ├── request     # 请求DTO
│   └── response    # 响应DTO
├── service         # 服务层
│   ├── impl        # 实现类
│   └── dto         # 服务层DTO
├── mapper          # Mapper接口
├── entity          # 实体类
├── enums           # 枚举类
├── constants       # 常量类
├── utils           # 工具类(模块内)
└── listener        # 监听器
```

---

## 三、编码规范约束

### 3.1 命名规范
| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | UserService, OrderController |
| 方法名 | 小驼峰 | getUserById, createOrder |
| 变量名 | 小驼峰 | userId, orderStatus |
| 常量名 | 全大写下划线 | MAX_RETRY_COUNT |
| 包名 | 全小写 | com.shop.user.service |
| 数据库表名 | 小写下划线 | user_info, order_detail |
| 数据库字段 | 小写下划线 | user_id, create_time |

### 3.2 代码格式约束
```java
// 1. 类注释必须包含作者和日期
/**
 * 用户服务实现类
 *
 * @author {name}
 * @since 2024-01-01
 */
@Service
public class UserServiceImpl implements UserService {

    // 2. 常量在前，变量在后
    private static final int MAX_RETRY = 3;

    // 3. 使用构造器注入，禁止 @Autowired 字段注入
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserServiceImpl(UserMapper userMapper, 
                          RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    // 4. 方法不超过50行，圈复杂度不超过10
    // 5. 禁止使用魔法数字
    // 6. 必须处理异常，禁止捕获后空处理
}
```

### 3.3 严格禁止的代码
```java
// ❌ 禁止使用
@Autowired  // 字段注入
private UserMapper userMapper;

// ❌ 禁止使用
public void method() {
    try {
        // do something
    } catch (Exception e) {
        // 空捕获，必须处理
    }
}

// ❌ 禁止使用
if (status == 1) {  // 魔法数字
    // do something
}

// ❌ 禁止使用
System.out.println("debug");  // 使用日志框架

// ❌ 禁止使用
Date date = new Date();  // 使用 LocalDateTime

// ❌ 禁止使用旧式 instanceof + 显式强转
if (obj instanceof String) {
    return ((String) obj).length();
}

// ✅ 必须使用 Java 16+ pattern matching for instanceof
if (obj instanceof String str) {
    return str.length();
}

// ❌ 禁止使用任何 --enable-preview 特性
// ❌ 禁止使用 @Deprecated(forRemoval=true) 的 API
```

---

## 四、数据库约束

### 4.1 表设计规范
- 所有表必须包含以下字段:
```sql
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0-未删除,1-已删除)',
```
- 主键统一使用 bigint 类型
- 时间字段使用 datetime 类型
- 状态字段使用 tinyint 类型
- 金额字段使用 decimal(10,2) 类型
- 字符串长度: varchar(32), varchar(64), varchar(128), varchar(255), varchar(500)
- 禁止使用 text 类型，超过500字符使用单独表存储

### 4.2 索引规范
- 主键必须是有序的（如自增ID）
- 单表索引不超过5个
- 联合索引字段不超过5个
- 区分度低的字段不建索引
- 必须根据查询SQL创建索引

### 4.3 SQL编写规范
```sql
-- 禁止使用 SELECT *
SELECT id, username, phone FROM user_info WHERE id = 1;

-- 禁止使用隐式类型转换
WHERE user_id = '123'  -- ❌ user_id是bigint
WHERE user_id = 123    -- ✅

-- 批量操作限制在500条以内
```

---

## 五、API接口约束

### 5.1 RESTful 规范
```
GET    /api/v1/users          # 查询列表
GET    /api/v1/users/{id}     # 查询详情
POST   /api/v1/users          # 创建
PUT    /api/v1/users/{id}     # 全量更新
PATCH  /api/v1/users/{id}     # 部分更新
DELETE /api/v1/users/{id}     # 删除
```

### 5.2 统一响应格式
```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
```

### 5.3 错误码规范
```java
public interface ErrorCode {
    // 系统错误 1xxxx
    int SYSTEM_ERROR = 10000;
    int PARAM_ERROR = 10001;
    int UNAUTHORIZED = 10002;
    int FORBIDDEN = 10003;
    int NOT_FOUND = 10004;

    // 用户模块 2xxxx
    int USER_NOT_FOUND = 20001;
    int USER_PASSWORD_ERROR = 20002;
    int USER_ALREADY_EXISTS = 20003;

    // 商品模块 3xxxx
    int PRODUCT_NOT_FOUND = 30001;
    int PRODUCT_STOCK_NOT_ENOUGH = 30002;

    // 订单模块 4xxxx
    int ORDER_NOT_FOUND = 40001;
    int ORDER_STATUS_ERROR = 40002;
}
```

---

## 六、缓存约束

### 6.1 Key命名规范
```
shop:{module}:{business}:{identifier}

# 示例
shop:user:info:{userId}           # 用户信息
shop:product:detail:{productId}   # 商品详情
shop:cart:{userId}                # 购物车
```

### 6.2 缓存使用规范
```java
// 必须使用注解式缓存
@Cacheable(value = "user", key = "'info:' + #userId")
public UserInfo getUserById(Long userId) {
    return userMapper.selectById(userId);
}

@CacheEvict(value = "user", key = "'info:' + #userId")
public void updateUser(Long userId, UserUpdateRequest request) {
    // update logic
}

// 缓存有效期
// - 热点数据: 10-30分钟
// - 配置数据: 1-24小时
// - 会话数据: 30分钟-7天
```

---

## 七、事务约束

### 7.1 事务使用规范
```java
// 事务注解必须指定回滚异常
@Transactional(rollbackFor = Exception.class)
public void createOrder(OrderCreateRequest request) {
    // 1. 校验库存
    // 2. 创建订单
    // 3. 扣减库存
    // 4. 清空购物车
}

// 禁止在事务中进行远程调用
// 禁止在事务中进行大量数据查询
// 事务方法必须是public
```

---

## 八、日志约束

### 8.1 日志规范
```java
// 使用SLF4J
private static final Logger log = LoggerFactory.getLogger(UserService.class);

// 日志级别使用规范
log.debug("用户登录: {}", userId);           // 调试信息
log.info("订单创建成功: orderId={}", orderId); // 业务信息
log.warn("库存不足: productId={}", productId); // 警告信息
log.error("支付失败: {}", e.getMessage(), e);  // 错误信息

// 禁止使用字符串拼接
log.info("userId: " + userId);  // ❌
log.info("userId: {}", userId);  // ✅
```

---

## 九、安全检查清单

### 9.1 每个功能必须检查
- [ ] SQL注入防护 (使用参数化查询)
- [ ] XSS防护 (输入校验、输出转义)
- [ ] CSRF防护 (Token验证)
- [ ] 接口鉴权 (JWT验证)
- [ ] 敏感数据加密 (密码、手机号)
- [ ] 接口防重放 (时间戳+随机数)
- [ ] 并发控制 (分布式锁)

### 9.2 敏感数据处理
```java
// 密码必须加密存储
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String encodedPassword = encoder.encode(rawPassword);

// 手机号脱敏
public static String maskPhone(String phone) {
    if (phone == null || phone.length() != 11) {
        return phone;
    }
    return phone.substring(0, 3) + "****" + phone.substring(7);
}
```

---

## 十、代码审查清单

### 10.1 提交前自查
- [ ] 代码符合阿里巴巴Java开发规范
- [ ] 单元测试覆盖率 > 60%
- [ ] 所有TODO已处理或已记录Issue
- [ ] 无用代码已删除
- [ ] 敏感信息未提交
- [ ] 配置文件正确

### 10.2 Code Review检查点
- [ ] 业务逻辑正确性
- [ ] 性能隐患 (N+1查询、大事务)
- [ ] 安全隐患 (SQL注入、越权)
- [ ] 异常处理完整性
- [ ] 日志打印合理性
