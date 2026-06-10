-- =============================================
-- ShopMax 营销系统数据库初始化脚本
-- 版本: 1.0
-- 日期: 2026-05-01
-- =============================================

-- 优惠券模板表
CREATE TABLE IF NOT EXISTS `mms_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `name` varchar(64) NOT NULL COMMENT '优惠券名称',
    `type` tinyint NOT NULL DEFAULT '1' COMMENT '优惠券类型: 1-满减券 2-折扣券 3-运费券 4-新人券',
    `min_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '使用门槛金额(0表示无门槛)',
    `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '减免金额(满减券使用)',
    `discount_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '折扣率(折扣券使用, 如0.85表示85折)',
    `total_count` int NOT NULL DEFAULT '0' COMMENT '发放总量',
    `received_count` int NOT NULL DEFAULT '0' COMMENT '已领取数量',
    `used_count` int NOT NULL DEFAULT '0' COMMENT '已使用数量',
    `per_limit` int NOT NULL DEFAULT '1' COMMENT '每人限领数量',
    `valid_days` int NOT NULL DEFAULT '7' COMMENT '领取后有效天数',
    `use_start_time` datetime DEFAULT NULL COMMENT '固定有效期-开始时间',
    `use_end_time` datetime DEFAULT NULL COMMENT '固定有效期-结束时间',
    `applicable_type` tinyint NOT NULL DEFAULT '1' COMMENT '适用类型: 1-全部商品 2-指定分类 3-指定商品',
    `applicable_ids` varchar(2000) DEFAULT NULL COMMENT '适用分类/商品ID列表(JSON数组)',
    `description` varchar(500) DEFAULT NULL COMMENT '使用说明',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券模板表';

-- 优惠券领取记录表
CREATE TABLE IF NOT EXISTS `mms_coupon_receive` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '领取记录ID',
    `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time` datetime DEFAULT NULL COMMENT '使用时间',
    `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
    `order_no` varchar(32) DEFAULT NULL COMMENT '关联订单号',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未使用 1-已使用 2-已过期',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_status` (`status`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券领取记录表';

-- 移除唯一索引：管理员发券允许同一用户重复领取，用户限领由应用层 perLimit 控制
ALTER TABLE `mms_coupon_receive` DROP INDEX IF EXISTS `uk_coupon_user`;

-- 促销活动表
CREATE TABLE IF NOT EXISTS `mms_promotion` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '促销活动ID',
    `name` varchar(64) NOT NULL COMMENT '活动名称',
    `description` varchar(500) DEFAULT NULL COMMENT '活动描述',
    `type` tinyint NOT NULL DEFAULT '1' COMMENT '促销类型: 1-满减 2-满折',
    `min_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '满减门槛金额',
    `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '减免金额(满减)',
    `discount_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '折扣率(满折, 如0.9表示9折)',
    `start_time` datetime NOT NULL COMMENT '活动开始时间',
    `end_time` datetime NOT NULL COMMENT '活动结束时间',
    `applicable_type` tinyint NOT NULL DEFAULT '1' COMMENT '适用类型: 1-全部商品 2-指定分类 3-指定商品',
    `applicable_ids` varchar(2000) DEFAULT NULL COMMENT '适用分类/商品ID列表(JSON数组)',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未开始 1-进行中 2-已结束 3-已禁用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销活动表';

-- 促销适用范围表（当需要精确控制适用商品/分类时使用）
CREATE TABLE IF NOT EXISTS `mms_promotion_scope` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '适用范围ID',
    `promotion_id` bigint NOT NULL COMMENT '促销活动ID',
    `target_type` tinyint NOT NULL DEFAULT '1' COMMENT '目标类型: 1-分类 2-商品',
    `target_id` bigint NOT NULL COMMENT '目标ID(分类ID或商品ID)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_promotion_id` (`promotion_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销适用范围表';

-- 初始测试数据
INSERT INTO `mms_coupon` (`name`, `type`, `min_amount`, `discount_amount`, `discount_rate`, `total_count`, `received_count`, `per_limit`, `valid_days`, `applicable_type`, `description`, `status`) VALUES
('新人满100减20', 1, 100.00, 20.00, 1.00, 1000, 0, 1, 30, 1, '新用户专享，满100元减20元，全场通用', 1),
('全场9折券', 2, 0.00, 0.00, 0.90, 500, 0, 1, 15, 1, '全场商品9折优惠，每人限领1张', 1),
('满50减10运费券', 3, 50.00, 10.00, 1.00, 200, 0, 2, 30, 1, '满50元可抵10元运费', 1),
('夏季大促满200减50', 1, 200.00, 50.00, 1.00, 300, 0, 1, 7, 1, '夏季大促专属优惠，满200减50', 1);

INSERT INTO `mms_promotion` (`name`, `description`, `type`, `min_amount`, `discount_amount`, `discount_rate`, `start_time`, `end_time`, `applicable_type`, `status`) VALUES
('五一满减活动', '五一劳动节全场满300减80', 1, 300.00, 80.00, 1.00, '2026-04-28 00:00:00', '2026-05-07 23:59:59', 1, 1),
('开学季折扣', '开学季数码产品满500享8折', 2, 500.00, 0.00, 0.80, '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1, 1);

-- =============================================
-- 秒杀系统
-- =============================================

-- 秒杀场次表
CREATE TABLE IF NOT EXISTS `mms_seckill_session` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '场次ID',
    `name` varchar(64) NOT NULL COMMENT '场次名称(如: 10点场)',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未开始 1-进行中 2-已结束',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀场次表';

-- 秒杀商品表
CREATE TABLE IF NOT EXISTS `mms_seckill_product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
    `session_id` bigint NOT NULL COMMENT '场次ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `sku_id` bigint NOT NULL COMMENT 'SKU ID',
    `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价',
    `seckill_stock` int NOT NULL DEFAULT '0' COMMENT '秒杀库存',
    `limit_per_user` int NOT NULL DEFAULT '1' COMMENT '每人限购数量',
    `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀商品表';

-- 初始数据
INSERT INTO `mms_seckill_session` (`name`, `start_time`, `end_time`, `status`) VALUES
('10点场', '2026-05-01 10:00:00', '2026-05-01 12:00:00', 1),
('14点场', '2026-05-01 14:00:00', '2026-05-01 16:00:00', 1),
('20点场', '2026-05-01 20:00:00', '2026-05-01 22:00:00', 0);

-- =============================================
-- 拼团系统
-- =============================================

-- 拼团活动表
CREATE TABLE IF NOT EXISTS `mms_group_buy_activity` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团活动ID',
    `name` varchar(128) NOT NULL COMMENT '活动名称',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `sku_id` bigint NOT NULL COMMENT 'SKU ID',
    `group_price` decimal(10,2) NOT NULL COMMENT '拼团价格',
    `required_count` int NOT NULL DEFAULT '2' COMMENT '成团人数',
    `expire_hours` int NOT NULL DEFAULT '24' COMMENT '拼团有效小时数',
    `stock` int NOT NULL DEFAULT '0' COMMENT '拼团库存',
    `start_time` datetime NOT NULL COMMENT '活动开始时间',
    `end_time` datetime NOT NULL COMMENT '活动结束时间',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团活动表';

-- 拼团记录表
CREATE TABLE IF NOT EXISTS `mms_group_buy_group` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团ID',
    `activity_id` bigint NOT NULL COMMENT '活动ID',
    `leader_id` bigint NOT NULL COMMENT '团长用户ID',
    `current_count` int NOT NULL DEFAULT '1' COMMENT '当前参团人数',
    `required_count` int NOT NULL COMMENT '成团人数',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-进行中 1-已成团 2-已失败',
    `expire_time` datetime NOT NULL COMMENT '过期时间',
    `complete_time` datetime DEFAULT NULL COMMENT '成团/失败时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_leader_id` (`leader_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团记录表';

-- 拼团成员表
CREATE TABLE IF NOT EXISTS `mms_group_buy_member` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
    `group_id` bigint NOT NULL COMMENT '拼团ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `order_id` bigint DEFAULT NULL COMMENT '订单ID',
    `order_no` varchar(32) DEFAULT NULL COMMENT '订单号',
    `is_leader` tinyint NOT NULL DEFAULT '0' COMMENT '是否团长: 0-否 1-是',
    `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参团时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团成员表';

-- 拼团初始数据
INSERT INTO `mms_group_buy_activity` (`name`, `product_id`, `sku_id`, `group_price`, `required_count`, `expire_hours`, `stock`, `start_time`, `end_time`, `status`) VALUES
('夏日T恤2人拼团', 1, 1, 49.90, 2, 24, 100, '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1),
('品牌耳机3人拼团', 2, 2, 159.00, 3, 12, 50, '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1);
