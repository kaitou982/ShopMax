# ShopMax 直播带货系统设计文档

**日期**: 2026-06-05
**状态**: 设计完成，待实现
**版本**: v1.0

---

## 1. 背景与目标

ShopMax 电商平台已完成后端直播 CRUD 骨架（主播/直播间/直播商品/消息记录）和管理后台 UI，但缺少实际的流媒体推流/拉流能力、前端视频播放器和完整的互动系统。

**目标**：实现完整的直播带货功能，包括：
- 主播通过 OBS 推流，观众在 Web/Mobile 端观看
- 实时弹幕、点赞、虚拟礼物互动
- 直播间内商品展示与购买闭环
- 直播录制与回放

## 2. 技术选型

| 组件 | 选型 | 说明 |
|------|------|------|
| 流媒体服务器 | SRS 6.0 (Docker) | 开源、免费、支持 RTMP/HLS/HTTP-FLV |
| 推流方式 | OBS Studio | 行业标准，主播端零开发 |
| Web 播放器 | mpegts.js | HTTP-FLV 低延迟播放 |
| 移动端播放器 | HLS (原生 `<video>`) | 兼容性最好 |
| 实时互动 | Spring WebSocket | 复用已有基础设施 |
| 对象存储 | MinIO | 录制回放存储 |
| 缓存 | Redis | 在线人数、限频、状态缓存 |

## 3. 系统架构

```
┌─────────────┐    RTMP     ┌─────────────┐   HTTP-FLV/HLS   ┌──────────────┐
│  OBS Studio │ ──────────→ │  SRS 6.0    │ ────────────────→ │  Web/Mobile  │
│  (主播推流)  │             │  (Docker)    │                   │  (观众播放)   │
└─────────────┘             └──────┬──────┘                   └──────────────┘
                                   │
                            on_publish / on_unpublish / on_play 回调
                                   │
                                   ▼
                            ┌──────────────┐    WebSocket    ┌──────────────┐
                            │ shop-live-   │ ←──────────────→ │  Web/Mobile  │
                            │ service      │                  │  (互动消息)   │
                            └──────┬──────┘                  └──────────────┘
                                   │
                        ┌──────────┼──────────┐
                        ▼          ▼          ▼
                    ┌───────┐ ┌───────┐ ┌──────────┐
                    │ MySQL │ │ Redis │ │  MinIO   │
                    │       │ │       │ │ (录制)    │
                    └───────┘ └───────┘ └──────────┘
```

### 3.1 Docker 网络

所有容器加入共享网络 `shopmax-net`，通过容器名通信：

```yaml
# docker-compose.yml 新增
services:
  srs:
    image: ossrs/srs:6
    ports:
      - "1935:1935"   # RTMP 推流
      - "8085:8080"   # HTTP-FLV/HLS
      - "1985:1985"   # SRS HTTP API
    volumes:
      - ./docker/srs/conf:/usr/local/srs/conf
      - ./docker/srs/record:/usr/local/srs/record
    networks:
      - shopmax-net
    restart: unless-stopped

networks:
  shopmax-net:
    driver: bridge
```

## 4. SRS 配置

### 4.1 核心配置 (docker/srs/conf/srs.conf)

```
listen              1935;
http_api {
    enabled         on;
    listen          1985;
}
http_server {
    enabled         on;
    listen          8080;
    dir             ./objs/nginx/html;
}

vhost __defaultVhost__ {
    http_hooks {
        enabled         on;
        on_publish      http://shop-live:8083/api/v1/live/srs/callback;
        on_unpublish    http://shop-live:8083/api/v1/live/srs/callback;
        on_play         http://shop-live:8083/api/v1/live/srs/on-play;
        on_stop         http://shop-live:8083/api/v1/live/srs/on-stop;
    }
    hls {
        enabled         on;
        hls_fragment    2;
        hls_window      10;
        hls_path        ./objs/nginx/html;
        hls_m3u8_file   [app]/[stream].m3u8;
        hls_ts_file     [app]/[stream]-[seq].ts;
    }
    dvr {
        enabled         on;
        dvr_path        ./record/[app]/[stream].[timestamp].flv;
        dvr_plan        session;
    }
}
```

### 4.2 推流地址格式

```
rtmp://localhost:1935/live/{roomId}?token={jwt}
```

- `roomId` = 直播间 ID
- `token` = 推流鉴权 JWT（有效期 2 小时）
- 后端生成地址时写入 Redis 白名单
- SRS `on_publish` 回调时验证白名单

### 4.3 拉流地址格式

| 协议 | 地址 | 平台 |
|------|------|------|
| HTTP-FLV | `http://localhost:8085/live/{roomId}.flv?token={jwt}` | Web 端 |
| HLS | `http://localhost:8085/live/{roomId}.m3u8?token={jwt}&expire={ts}` | 移动端/H5/小程序 |

## 5. 推流鉴权与状态管理

### 5.1 主播开播流程

1. 主播在后台点击"开播" → `POST /api/v1/live/rooms/{id}/start`
2. 后端校验房间状态（必须为"预告"或"待推流"）
3. 生成推流 Token（JWT，有效期 2h）
4. 写入 Redis 白名单：`live:push:whitelist:{roomId} = token`（TTL 2h）
5. 更新房间状态为"待推流"（status=4）
6. 返回推流地址给主播
7. 主播将地址填入 OBS → 开始推流
8. SRS 回调 `on_publish` → 后端验证 Redis 白名单 → 更新状态为"直播中"（status=1）
9. 记录开播时间，清除 Redis 白名单

### 5.2 直播状态机

```
预告(0) → [主播点击开播] → 待推流(4) → [SRS on_publish] → 直播中(1) → [SRS on_unpublish] → 已结束(2)
                                                                                    ↓
                                                                              [异常断流检测]
                                                                                    ↓
                                                                              已结束(2)
```

状态值：
- `0` = 预告
- `1` = 直播中
- `2` = 已结束
- `3` = 已关闭
- `4` = 待推流（新增）

### 5.3 拉流鉴权

- **HTTP-FLV**：SRS `on_play` 回调验证 token 参数
- **HLS**：签名 URL + Nginx 校验，token 有效期 30 分钟，前端自动续签

### 5.4 Redis Key 设计

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `live:push:whitelist:{roomId}` | String | 2h | 推流白名单 |
| `live:room:{roomId}:online` | String(int) | - | 在线人数 |
| `live:room:{roomId}:likes` | String(int) | - | 点赞计数 |
| `live:room:{roomId}:status` | String(int) | - | 直播状态 |
| `live:danmaku:{roomId}:{uid}` | String | 2s | 弹幕限频 |
| `live:coin:{uid}` | String(int) | - | 虚拟币余额缓存 |

## 6. 前端播放器

### 6.1 Web 端播放器 (packages/web)

**新增依赖**：`mpegts.js` + `hls.js`

组件 `components/LivePlayer.vue`：
- 使用 mpegts.js 播放 HTTP-FLV（低延迟 1~3 秒）
- 不支持时自动降级到 hls.js
- 直播状态感知：加载中 / 直播中(LIVE 标记) / 已结束 / 断流重连
- 自动重连（最多 3 次，间隔 3 秒）
- 静音/取消静音、全屏切换

### 6.2 Mobile 端播放器 (packages/mobile)

组件 `components/LivePlayer.vue`：
- UniApp `<video>` 组件播放 HLS
- 微信小程序原生支持 HLS
- H5 端使用 hls.js 降级
- 条件编译处理平台差异

### 6.3 回放播放器

组件 `components/ReplayPlayer.vue`：
- Web 端：mpegts.js 播放 FLV 回放，支持进度条拖拽、暂停
- Mobile 端：FFmpeg 转码为 MP4 后播放
- 与 LivePlayer 复用核心逻辑，差异：非直播模式、显示时长

## 7. WebSocket 互动系统

### 7.1 消息协议

统一 JSON 格式：

```json
{
  "type": "danmaku | like | gift | enter | leave | product | system",
  "data": { ... },
  "timestamp": 1717584000000
}
```

消息类型：

| 类型 | 方向 | 说明 |
|------|------|------|
| `danmaku` | C→S→广播 | 弹幕消息 |
| `like` | C→S | 点赞（不广播，仅计数） |
| `gift` | C→S→广播 | 送礼消息 |
| `enter` | S→广播 | 用户进入 |
| `leave` | S→广播 | 用户离开 |
| `product` | S→广播 | 商品讲解推送 |
| `system` | S→广播 | 系统消息 |
| `online` | S→广播 | 在线人数更新 |

### 7.2 弹幕系统

**后端处理**：
1. 接收弹幕消息
2. Redis 限频检查（`live:danmaku:{roomId}:{uid}`，TTL 2s）
3. 敏感词过滤（内置词库 + Redis 缓存）
4. 内容截断（最长 200 字）
5. 持久化到 `lms_live_message`
6. 广播给房间所有连接

**前端渲染**：
- 3~5 条水平轨道，弹幕从右向左滚动
- 防重叠：新弹幕选择最空闲的轨道
- 队列管理：高频时丢弃旧弹幕
- 样式：白色文字 + 半透明背景 + 圆角
- 入场动画：从右侧滑入，8 秒后淡出

### 7.3 点赞系统

**后端策略**：
- 收到点赞 → Redis 原子递增 `live:room:{roomId}:likes`
- 每 10 次批量持久化到 DB
- 每 100 次广播一次总数更新

**前端动画**：
- 点击爱心按钮 → 触发粒子爆炸动画
- 3~5 个心形粒子随机方向飞出 + 缩放 + 淡出
- 持续时间 0.8~1.2 秒
- 纯前端效果，不发送 WebSocket 消息

### 7.4 礼物系统

**新增数据表**：`lms_gift`（礼物配置）、`lms_coin_log`（虚拟币流水）

**送礼流程**：
1. 用户点击礼物按钮 → 展示礼物面板
2. 选择礼物 + 数量 → 确认发送
3. WebSocket 发送 `{ type: "gift", data: { giftId, count } }`
4. 后端校验余额 → 扣减虚拟币（Redis 原子操作）
5. 记录消息到 `lms_live_message`
6. 广播给房间所有人（含礼物动画资源 URL）
7. 前端播放 Lottie 全屏特效

**虚拟币机制**：
- 新用户注册赠送 100 币
- 每日登录赠送 10 币
- 送礼消费虚拟币
- 余额存储在 `ums_user.coin_balance` + Redis 缓存

## 8. 直播带货

### 8.1 商品浮窗状态

| 状态 | 触发 | UI |
|------|------|----|
| 最小化入口 | 进入直播间 | 右下角购物车图标 + 商品数 |
| 讲解中弹窗 | 主播设置讲解 | 右下角商品卡片（价格/图片/购买按钮） |
| 商品列表面板 | 点击购物车 | 底部上滑面板，显示全部直播商品 |

### 8.2 主播端交互

- 主播在后台管理页点击"讲解中" → 状态变为 `status=2`
- 后端通过 WebSocket 广播 `{ type: "product", data: { productId, action: "highlight" } }`
- 观众端收到推送 → 商品浮窗自动弹出
- 主播点击"取消讲解" → 浮窗自动收起

### 8.3 购买闭环

- 直播间内商品展示优先使用 `live_price`
- 加购时记录 `liveRoomId`
- 下单时自动使用 `live_price`（如果有效）
- 直播结束后 `live_price` 失效，恢复原价

### 8.4 新增/修改接口

| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | `/api/v1/live/products/{id}/explain` | 设置讲解中 |
| PUT | `/api/v1/live/products/{id}/unexplain` | 取消讲解 |
| GET | `/api/v1/live/rooms/{roomId}/products` | 获取直播间商品列表 |

## 9. 录制与回放

### 9.1 录制流程

1. 直播中 → SRS 实时写入 FLV 到 `/record/live/{roomId}.{timestamp}.flv`
2. 推流断开 → `on_unpublish` 回调
3. 后端异步任务：读取本地 FLV → 上传到 MinIO Bucket `live-replay`
4. 上传成功 → 更新 `lms_live_room.replay_url` + 计算时长
5. 删除本地临时文件

### 9.2 MinIO 存储

```
live-replay/
├── room-1001/
│   ├── 20260605_143000.flv
│   └── 20260605_200000.flv
└── room-1002/
    └── 20260605_190000.flv
```

- 访问策略：read-only (public)
- 生命周期：30 天过期自动删除（配置项 `live.replay.retention-days`）

### 9.3 清理策略

- **即时清理**：上传 MinIO 成功后立即删除本地文件
- **兜底清理**：定时任务每天凌晨 3 点扫描 `/record/`，超过 24h 的残留文件强制删除
- **MinIO 过期**：30 天生命周期自动删除

### 9.4 移动端回放

直播结束后异步使用 FFmpeg 转码为 MP4：
```bash
ffmpeg -i input.flv -c:v libx264 -c:a aac -movflags +faststart output.mp4
```

**FFmpeg 部署**：在 Docker Compose 中添加 FFmpeg 服务或在 shop-live-service 容器中安装 FFmpeg。转码任务通过异步线程池执行，避免阻塞主线程。

### 9.5 异常断流检测

SRS 在推流断开时会触发 `on_unpublish` 回调。为防止回调丢失，增加兜底检测：
- 定时任务每 5 分钟检查状态为"直播中"的房间
- 通过 SRS HTTP API（`/api/v1/streams`）验证流是否仍然活跃
- 如果流已不存在但房间状态仍为"直播中"，强制更新为"已结束"

## 10. 数据库变更

### 10.1 新增表

**lms_gift（礼物配置表）**：
```sql
CREATE TABLE lms_gift (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(32)  NOT NULL             COMMENT '礼物名称',
    icon          VARCHAR(255) NOT NULL             COMMENT '图标URL',
    animation_url VARCHAR(255) DEFAULT NULL         COMMENT 'Lottie动画URL',
    price         INT          NOT NULL             COMMENT '虚拟币价格',
    sort_order    INT          NOT NULL DEFAULT 0   COMMENT '排序',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='礼物配置表';
```

**lms_coin_log（虚拟币流水表）**：
```sql
CREATE TABLE lms_coin_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL             COMMENT '用户ID',
    amount        INT          NOT NULL             COMMENT '变动数量(正增负减)',
    type          TINYINT      NOT NULL             COMMENT '1注册赠送 2每日签到 3送礼消费 4系统赠送',
    biz_id        VARCHAR(64)  DEFAULT NULL         COMMENT '关联业务ID',
    remark        VARCHAR(128) DEFAULT NULL         COMMENT '备注',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟币流水表';
```

### 10.2 现有表变更

**ums_user**：
```sql
ALTER TABLE ums_user
ADD COLUMN coin_balance INT NOT NULL DEFAULT 0 COMMENT '虚拟币余额';
```

**lms_live_room**：
```sql
ALTER TABLE lms_live_room
ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0 COMMENT '点赞总数',
ADD COLUMN gift_count BIGINT NOT NULL DEFAULT 0 COMMENT '礼物总数',
ADD COLUMN replay_duration INT DEFAULT NULL COMMENT '回放时长(秒)',
MODIFY COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '0预告 1直播中 2已结束 3已关闭 4待推流';
```

**lms_live_message**：
```sql
ALTER TABLE lms_live_message
ADD INDEX idx_room_type_time (room_id, type, create_time);
```

## 11. 后端新增/修改文件清单

### 11.1 shop-live-service 新增

| 文件 | 说明 |
|------|------|
| `controller/SrsCallbackController.java` | SRS 回调处理（on_publish/on_unpublish/on_play） |
| `service/SrsCallbackService.java` | 回调业务逻辑 |
| `service/impl/SrsCallbackServiceImpl.java` | 回调实现 |
| `service/LiveReplayService.java` | 录制回放服务 |
| `service/impl/LiveReplayServiceImpl.java` | 回放实现（MinIO 上传/清理） |
| `service/GiftService.java` | 礼物服务 |
| `service/impl/GiftServiceImpl.java` | 礼物实现（余额扣减/流水记录） |
| `entity/Gift.java` | 礼物实体 |
| `entity/CoinLog.java` | 虚拟币流水实体 |
| `mapper/GiftMapper.java` | 礼物 Mapper |
| `mapper/CoinLogMapper.java` | 流水 Mapper |
| `controller/GiftController.java` | 礼物 API |
| `config/LiveReplayConfig.java` | 回放异步任务配置 |

### 11.2 shop-live-service 修改

| 文件 | 修改内容 |
|------|----------|
| `LiveRoomServiceImpl.java` | 开播生成推流地址 + Redis 白名单 |
| `LiveWebSocketHandler.java` | 完善消息广播、在线人数 |
| `LiveMessageHandler.java` | 完善弹幕/礼物消息处理 |
| `LiveProductServiceImpl.java` | 新增讲解中/取消讲解方法 |
| `LiveRoom.java` | 新增 like_count/gift_count/replay_duration 字段 |
| `Anchor.java` | 无变更 |

### 11.3 shop-gateway 修改

| 文件 | 修改内容 |
|------|----------|
| `AuthGlobalFilter.java` | SRS 回调路径白名单（`/api/v1/live/srs/**`） |
| `JwtAuthenticationFilter.java` | SRS 回调路径白名单 |
| `SecurityConfig.java` | SRS 回调路径白名单 |
| `application.yml` | 新增 WebSocket 路由 `/ws/live/**` → shop-live-service |

> **注意**：根据项目记忆 `auth-three-layers`，新增公开接口必须同时更新 AuthGlobalFilter + JwtAuthenticationFilter + SecurityConfig 三处白名单。

## 12. 前端新增/修改文件清单

### 12.1 packages/web

| 文件 | 说明 |
|------|------|
| `components/LivePlayer.vue` | 直播播放器（mpegts.js + hls.js 降级） |
| `components/ReplayPlayer.vue` | 回放播放器 |
| `components/LiveDanmaku.vue` | 弹幕组件（轨道滚动） |
| `components/LiveGiftPanel.vue` | 礼物面板 |
| `components/LiveGiftAnimation.vue` | 礼物动画（Lottie） |
| `components/LiveProductFloat.vue` | 商品浮窗 |
| `components/LiveProductList.vue` | 商品列表面板 |
| `composables/useLiveSocket.ts` | WebSocket 连接管理 composable |
| `pages/live/room.vue` | 重写直播间页面（沉浸式布局） |
| `pages/live/replay.vue` | 回放页面 |

### 12.2 packages/mobile

| 文件 | 说明 |
|------|------|
| `components/LivePlayer.vue` | 直播播放器（UniApp video + HLS） |
| `components/LiveDanmaku.vue` | 弹幕组件 |
| `components/LiveGiftPanel.vue` | 礼物面板 |
| `components/LiveProductFloat.vue` | 商品浮窗 |
| `composables/useLiveSocket.ts` | WebSocket 连接管理 |
| `pages/live/room.vue` | 重写直播间页面 |
| `pages/live/replay.vue` | 回放页面 |

### 12.3 packages/shared

| 文件 | 说明 |
|------|------|
| `api/live.ts` | 扩展：礼物 API、商品讲解 API、回放 API |
| `types/index.ts` | 新增：Gift、CoinLog、LiveMessage 类型 |

### 12.4 shop-admin-ui

| 文件 | 说明 |
|------|------|
| `views/live/index.vue` | 新增：礼物管理 Tab |

## 13. 验证方案

### 13.1 SRS 部署验证

1. `docker-compose up -d srs`
2. 使用 OBS 推流到 `rtmp://localhost:1935/live/test`
3. 访问 `http://localhost:8085/live/test.flv` 确认 HTTP-FLV 输出
4. 访问 `http://localhost:8085/live/test.m3u8` 确认 HLS 输出

### 13.2 推流鉴权验证

1. 主播点击开播 → 获取推流地址
2. OBS 推流 → 检查 SRS 回调日志
3. 确认房间状态更新为"直播中"
4. 使用错误 token 推流 → 确认被拒绝

### 13.3 播放器验证

1. Web 端打开直播间 → 确认 HTTP-FLV 播放
2. 移动端打开直播间 → 确认 HLS 播放
3. 断开推流 → 确认"直播已结束"提示
4. 重新推流 → 确认自动重连

### 13.4 互动验证

1. 发送弹幕 → 确认房间内所有人收到
2. 连续快速发送 → 确认限频生效（2 秒间隔）
3. 点赞 → 确认前端动画 + 后端计数
4. 送礼 → 确认余额扣减 + 动画广播

### 13.5 直播带货验证

1. 主播设置讲解商品 → 确认观众端浮窗弹出
2. 点击浮窗"抢购" → 确认跳转到确认订单页
3. 确认使用 `live_price` 而非原价

### 13.6 录制回放验证

1. 开播推流 → 直播 1 分钟 → 结束
2. 检查 MinIO `live-replay` Bucket 是否有 FLV 文件
3. 检查本地 `/record/` 是否已清理
4. 打开回放页面 → 确认可以播放
5. 等待 24h → 确认定时任务清理残留文件

---

## 附录：SRS 回调 Controller

```java
@Tag(name = "SRS回调")
@RestController
@RequestMapping("/api/v1/live/srs")
@RequiredArgsConstructor
public class SrsCallbackController {

    private final SrsCallbackService srsCallbackService;

    @PostMapping("/callback")
    public String callback(
            @RequestParam String app,
            @RequestParam String stream,
            @RequestParam String action,
            @RequestParam(required = false) String param) {
        return srsCallbackService.handleCallback(app, stream, action, param);
    }

    @PostMapping("/on-play")
    public String onPlay(
            @RequestParam String stream,
            @RequestParam(required = false) String param) {
        return srsCallbackService.verifyPlayAccess(stream, param);
    }
}
```
