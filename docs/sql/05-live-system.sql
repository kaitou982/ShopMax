-- =============================================
-- ShopMax 直播系统数据库初始化脚本
-- 版本: 1.0
-- 日期: 2026-05-01
-- =============================================

-- 主播信息表
CREATE TABLE IF NOT EXISTS `lms_anchor` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主播ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `real_name` varchar(32) DEFAULT NULL COMMENT '真实姓名',
    `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `nickname` varchar(64) NOT NULL COMMENT '主播昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '主播头像',
    `cover` varchar(255) DEFAULT NULL COMMENT '直播间封面',
    `introduction` varchar(500) DEFAULT NULL COMMENT '主播简介',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-已通过 2-已拒绝 3-已禁用',
    `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
    `level` tinyint NOT NULL DEFAULT '1' COMMENT '主播等级: 1-普通 2-铜牌 3-银牌 4-金牌 5-钻石',
    `fans_count` int NOT NULL DEFAULT '0' COMMENT '粉丝数',
    `total_live_count` int NOT NULL DEFAULT '0' COMMENT '累计直播场次',
    `total_duration` bigint NOT NULL DEFAULT '0' COMMENT '累计直播时长(秒)',
    `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_level` (`level`),
    KEY `idx_fans_count` (`fans_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主播信息表';

-- 直播间表
CREATE TABLE IF NOT EXISTS `lms_live_room` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '直播间ID',
    `anchor_id` bigint NOT NULL COMMENT '主播ID',
    `title` varchar(128) NOT NULL COMMENT '直播标题',
    `cover` varchar(255) DEFAULT NULL COMMENT '直播封面',
    `notice` varchar(500) DEFAULT NULL COMMENT '直播公告',
    `type` tinyint NOT NULL DEFAULT '1' COMMENT '直播分类: 1-推荐 2-穿搭 3-美妆 4-美食 5-家居 6-数码 7-母婴',
    `start_time` datetime NOT NULL COMMENT '预告开始时间',
    `actual_start_time` datetime DEFAULT NULL COMMENT '实际开播时间',
    `end_time` datetime DEFAULT NULL COMMENT '直播结束时间',
    `push_url` varchar(500) DEFAULT NULL COMMENT '推流地址',
    `pull_url` varchar(500) DEFAULT NULL COMMENT '拉流地址(播放地址)',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-预告 1-直播中 2-已结束 3-已关闭',
    `online_count` int NOT NULL DEFAULT '0' COMMENT '当前在线人数',
    `total_view_count` int NOT NULL DEFAULT '0' COMMENT '累计观看人次',
    `peak_online_count` int NOT NULL DEFAULT '0' COMMENT '峰值在线人数',
    `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
    `duration` bigint NOT NULL DEFAULT '0' COMMENT '直播时长(秒)',
    `replay_url` varchar(500) DEFAULT NULL COMMENT '回放地址',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_anchor_id` (`anchor_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_online_count` (`online_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间表';

-- 初始测试数据
INSERT INTO `lms_anchor` (`user_id`, `real_name`, `phone`, `nickname`, `avatar`, `introduction`, `status`, `level`) VALUES
(2, '张三', '13800138001', '时尚美妆达人', '/api/v1/files/default/avatar', '专注时尚美妆5年，带你发现最美的自己', 1, 3),
(3, '李四', '13800138002', '数码评测师', '/api/v1/files/default/avatar', '专业数码产品评测，客观公正，帮你选好物', 1, 2);

INSERT INTO `lms_live_room` (`anchor_id`, `title`, `cover`, `notice`, `type`, `start_time`, `status`) VALUES
(1, '夏季美妆新品首发，限时特惠', '/api/v1/files/default/cover', '今晚8点准时开播，超多福利等着你！', 3, '2026-05-01 20:00:00', 0),
(2, '旗舰手机深度对比评测', '/api/v1/files/default/cover', '现场对比各品牌旗舰机型，帮你选对不买贵', 6, '2026-05-02 15:00:00', 0);

-- =============================================
-- 直播商品关联
-- =============================================

CREATE TABLE IF NOT EXISTS `lms_live_product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `room_id` bigint NOT NULL COMMENT '直播间ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `sku_id` bigint NOT NULL COMMENT 'SKU ID',
    `live_price` decimal(10,2) NOT NULL COMMENT '直播价',
    `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-已下架 1-已上架 2-讲解中',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播商品关联表';

-- =============================================
-- 直播消息记录
-- =============================================

CREATE TABLE IF NOT EXISTS `lms_live_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `room_id` bigint NOT NULL COMMENT '直播间ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型: 1-弹幕 2-点赞 3-礼物 4-进入直播间 5-关注',
    `content` varchar(500) DEFAULT NULL COMMENT '消息内容',
    `gift_id` bigint DEFAULT NULL COMMENT '礼物ID',
    `gift_count` int DEFAULT NULL COMMENT '礼物数量',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播消息记录表';
