# ShopMax 电商平台

B2C 商城 + 直播带货 + 社交电商 + 内容社区，全栈微服务电商平台。

## 在线体验

| 端 | 体验地址 |
|---|---------|
| 🖥️ Web PC 端 | [https://shopmax.vercel.app](https://shopmax.vercel.app) |
| 📱 移动端 H5 | [https://shopmax-m.vercel.app](https://shopmax-m.vercel.app) |
| ⚙️ 管理后台 | [https://shopmax-admin.vercel.app](https://shopmax-admin.vercel.app) |

> 后端 API 通过 VPS 部署，域名待配置。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端框架 | Spring Boot 3 + Spring Cloud (JDK 21) |
| 注册中心 | Nacos |
| 熔断降级 | Sentinel |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 文件存储 | MinIO |
| 消息队列 | RocketMQ |
| 搜索引擎 | Elasticsearch |
| Web 前端 | Vue 3 + Naive UI + Vite |
| 移动端 | UniApp + Vue 3 |
| 管理后台 | Vue 3 + Element Plus |
| 容器化 | Docker + Docker Compose |

## 微服务架构

```
                  ┌─────────────┐
                  │   Nginx 80   │
                  └──────┬──────┘
                         │
                  ┌──────▼──────┐
                  │  Gateway    │ ← API 网关 + JWT 认证
                  └──────┬──────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
   ┌────▼────┐    ┌──────▼──────┐   ┌────▼────┐
   │  Auth   │    │    User     │   │ Product │
   └─────────┘    └─────────────┘   └─────────┘
        │                │                │
   ┌────▼────┐    ┌──────▼──────┐   ┌────▼────┐
   │  Order  │    │  Payment    │   │Marketing│
   └─────────┘    └─────────────┘   └─────────┘
        │                │                │
   ┌────▼────┐    ┌──────▼──────┐   ┌────▼────┐
   │Community│    │    Live     │   │  Admin  │
   └─────────┘    └─────────────┘   └─────────┘
        │                │
   ┌────▼────┐    ┌──────▼──────┐
   │   File  │    │  Customer   │
   └─────────┘    └─────────────┘
```

## 本地开发

```bash
# 1. 启动基础设施
docker compose up -d

# 2. 启动后端（需要 JDK 21 + Maven）
cd shop-backend
mvn spring-boot:run -pl shop-gateway
mvn spring-boot:run -pl shop-modules/shop-product-service
# ... 按需启动其他服务

# 3. 启动 Web 前端
cd shop-frontend
pnpm install
pnpm dev:web

# 4. 启动移动端
pnpm dev:mobile

# 5. 启动管理后台
cd shop-admin-ui
npm install
npm run dev
```

## 项目结构

```
ShopMax/
├── shop-backend/          # 后端微服务 (Spring Cloud)
│   ├── shop-gateway/      # API 网关
│   ├── shop-auth/         # 认证服务
│   ├── shop-common/       # 公共模块 (Feign, Redis, Security)
│   └── shop-modules/      # 业务服务
├── shop-frontend/         # 前端 Monorepo (pnpm)
│   └── packages/
│       ├── web/           # Web PC 端
│       ├── mobile/        # 移动端 (UniApp)
│       └── shared/        # 共享代码
├── shop-admin-ui/         # 管理后台
├── docker/                # Docker 配置
├── docs/                  # 文档 + SQL
├── docker-compose.yml     # 本地开发环境
└── docker-compose.prod.yml # 生产部署配置
```

## 生产部署

### 前端 (Vercel)

1. 注册 [Vercel](https://vercel.com)，用 GitHub 登录
2. 在 Vercel 创建 3 个项目，分别指向:
   - `shop-frontend` → Web 前端
   - `shop-admin-ui` → 管理后台
   - `shop-frontend` → 移动端 H5 (Build Command: `pnpm --filter @shop/mobile build:h5`)
3. 每次 `git push` 自动部署

### 后端 (VPS)

1. 购买一台轻量云服务器（推荐阿里云 2核4G，99元/年）
2. 安装 Docker + Git
3. SSH 登录后执行:

```bash
git clone https://github.com/你的用户名/ShopMax.git /opt/shopmax
cd /opt/shopmax
cp .env.example .env
vim .env  # 填入数据库密码、JWT密钥等
docker compose -f docker-compose.prod.yml up -d
```

## License

MIT
