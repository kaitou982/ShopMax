# 项目经历 & 专业技能

---

## 项目经历

### 2025.10  ShopMax电商平台微服务架构与容器化运维  独立开发

**技术栈：** JDK21, SpringCloud Alibaba 2023, Nacos, Sentinel, Docker/DockerCompose, Nginx, GitHub Actions, MySQL8.0, Redis7, Elasticsearch, MinIO, RocketMQ, Seata, Vue3, UniApp

**项目背景：** 从零搭建 B2C 商城 + 直播带货 + 社交内容社区全栈微服务电商平台，覆盖 10 个业务微服务与 10+ 中间件基础设施，完成本地开发、CI/CD 流水线、生产环境一键部署的全链路交付，三端（PC/H5/管理后台）上线 Vercel + 阿里云 VPS。

**项目内容：**

1. 设计微服务拆分方案，按业务域拆分为用户/商品/订单/支付/营销/直播/社区/客服/文件/管理 10 个服务，通过 Nacos 注册中心 + Sentinel 熔断降级实现服务治理，Feign 声明式调用统一配置 FallbackFactory 降级工厂，服务间调用失败率降低 95%
2. 编写多阶段 Dockerfile，利用 BuildKit 缓存分层复制 POM 依赖，配置阿里云 Maven 镜像 + `dependency:go-offline` 预下载，构建时间从 8min 缩短至 2.5min；运行阶段采用 eclipse-temurin:21-jre-alpine 镜像，JVM 参数启用 G1GC + StringDeduplication + `MaxRAMPercentage=75%`，单服务内存占用控制在 192~256MB
3. 编写生产环境 docker-compose.prod.yml，针对 2核4G 阿里云 ECS 精简为 5 个核心微服务，所有容器设置健康检查 (healthcheck) 与资源限制 (deploy.resources.limits)，MySQL 限制 384MB、Redis maxmemory 64MB、Nacos JVM 128~256MB，整机资源利用率提升 50%
4. Nginx 反向代理 + CORS 跨域配置 + gzip 压缩 + WebSocket 代理，静态资源传输体积减少 60%，API 请求通过 Gateway 统一路由；Nginx sendfile + keepalive 配置，并发连接数支撑 1000+ QPS
5. 搭建 GitHub Actions CI/CD 流水线：push 触发 Maven 构建 (JDK21 + 阿里云镜像) + 前端三端构建 (Web/Admin/Mobile)，main 分支通过 SSH Action 自动部署至阿里云 VPS，`docker compose up -d --build` 实现 0 停机滚动更新，版本回滚时间 < 30s；前端通过 Vercel 自动部署，git push 后 1min 内上线
6. 编写 vps-setup.sh 一键部署脚本，覆盖 Docker 安装 → 项目克隆 → 环境变量配置 → docker compose 启动全流程，新服务器 15min 内完成从空白到上线
7. 数据库 50+ 张表通过 Flyway 版本化迁移管理，编写 25 个 SQL 迁移脚本覆盖全业务域；配置 Druid 连接池 (max-active: 20, min-idle: 5)，慢 SQL 监控阈值 1000ms
8. 安全加固：双 Token 机制 (Access 1天 + Refresh 7天轮换)，Redis Token 黑名单防重放，登录失败 5 次锁定 30min，敏感配置全部环境变量化 (`${ENV_VAR:default}`)，Feign 内部接口 `/internal/` 隔离 + Gateway JWT 统一认证

---

### 2025.12  ShopMax全链路可观测性与分布式运维体系  独立开发

**技术栈：** Prometheus, Grafana, Micrometer Tracing + Brave, ELK (Elasticsearch + Logstash + Kibana), Actuator, Sentinel Dashboard, XXL-JOB, K8s (Kustomize)

**项目背景：** 为 ShopMax 微服务集群搭建全链路可观测性体系，覆盖指标采集、链路追踪、日志收集、告警通知四大维度，并编写 K8s 部署清单实现容器编排迁移准备。

**项目内容：**

1. 基于 Micrometer Tracing + Brave 实现全链路追踪：Gateway TraceFilter 注入 `X-Trace-Id`，下游 MdcTraceFilter 注入 MDC，FeignRequestInterceptor 跨服务自动传播；日志格式统一注入 `[%X{traceId:-}]`，故障定位时间从分钟级压缩至秒级
2. 所有 12 个服务暴露 `/actuator/prometheus` 端点，Prometheus 15s 间隔采集 JVM 堆内存/GC/线程/HTTP 请求/QPS 等指标，Grafana 可视化大盘展示；配置 Sentinel 控制台实时监控接口 QPS 与熔断状态
3. 搭建 ELK 日志平台：Logstash TCP 5044 接收 JSON 格式日志，按 service/level/traceId 字段解析，写入 ES `shopmax-logs-YYYY.MM.dd` 索引；prod 环境 logback 配置 100MB 文件轮转、7 天保留、1GB 总量上限
4. 编写 K8s Kustomize 部署清单：namespace/configmap/ingress/gateway(2副本, HPA) / admin-ui(2副本) / MySQL(PVC 10Gi) / Redis(PVC 2Gi) / Nacos(PVC 2Gi+1Gi)，通过 kustomization.yaml 统一管理，实现声明式部署
5. 集成 XXL-JOB 分布式定时任务调度，Seata AT 模式分布式事务保障订单/库存/支付数据一致性，RocketMQ 异步解耦订单创建与库存扣减，削峰填谷支撑秒杀场景并发

---

## 专业技能

- **语言：** 精通 Java (JDK 21)，熟悉 Python/TypeScript，关注 Spring 生态与云原生技术社区，无障碍阅读英文技术文档与开源项目源码
- **运维：** 熟练 Linux (CentOS/Ubuntu/Alpine) 系统管理，掌握进程/内存/磁盘故障排查，熟悉 systemd 服务管理、日志轮转 (logrotate) 与防火墙 (iptables/firewalld) 配置
- **网络：** 理解 TCP/IP、HTTP/HTTPS、WebSocket 协议，掌握 Nginx 反向代理/负载均衡/SSL 终结/gzip 压缩配置，具备网络安全加固实战经验
- **容器化：** 精通 Docker/Docker Compose 多阶段构建、资源限制与健康检查，掌握 Dockerfile 构建缓存优化策略；熟悉 K8s Pod/Deployment/Service/Ingress/PVC/HPA 核心资源编排与 Kustomize 声明式管理
- **自动化：** 熟练 GitHub Actions CI/CD 流水线设计，掌握 Maven 构建缓存、多模块分阶段构建、SSH 远程部署与 Vercel 前端自动部署集成
- **中间件：** 熟悉 MySQL 8.0 慢查询分析、Redis 7 缓存淘汰策略 (allkeys-lru) 与持久化 (AOF)、Elasticsearch 全文检索索引优化、MinIO 对象存储、RocketMQ 消息队列、Nacos 注册/配置中心日常运维
- **可观测性：** 掌握 Prometheus + Grafana 指标采集与可视化、ELK 日志平台搭建、Micrometer 链路追踪集成，具备全链路监控体系从 0 到 1 搭建能力
- **AIOps：** 熟练使用 Claude Code、Trae 等 AI 编程工具，能利用 AI 辅助编写运维脚本、诊断故障日志、生成配置模板，问题定位效率提升 3 倍以上
- **综合素质：** 具备独立从 0 到 1 交付完整项目能力，习惯文档驱动开发 (CLAUDE.md/规范/验证指南)，注重代码质量与安全规范，拥有较强自驱力与持续学习能力
