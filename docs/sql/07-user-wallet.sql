-- ============================================================
-- 用户钱包体系：积分流水、余额流水
-- ============================================================

-- 积分流水表
CREATE TABLE IF NOT EXISTS `ums_integral_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `change_amount` int NOT NULL COMMENT '变动数量（正=增加，负=减少）',
    `after_amount` int NOT NULL DEFAULT 0 COMMENT '变动后余额',
    `type` tinyint NOT NULL COMMENT '类型: 1-注册赠送 2-邀请奖励 3-订单完成 4-积分兑换 5-积分支付 6-退款退回 7-管理员调整',
    `biz_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID（订单号/兑换单号等）',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';

-- 余额流水表
CREATE TABLE IF NOT EXISTS `ums_balance_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `change_amount` decimal(10,2) NOT NULL COMMENT '变动金额（正=增加，负=减少）',
    `after_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '变动后余额',
    `type` tinyint NOT NULL COMMENT '类型: 1-充值 2-支付 3-退款 4-提现 5-管理员调整',
    `biz_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID',
    `pay_channel` varchar(32) DEFAULT NULL COMMENT '支付渠道',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额流水表';

-- 商品评价表
CREATE TABLE IF NOT EXISTS `pms_product_review` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `rating` tinyint NOT NULL DEFAULT 5 COMMENT '评分(1-5)',
    `content` varchar(1000) DEFAULT NULL COMMENT '评价内容',
    `images` varchar(2000) DEFAULT NULL COMMENT '评价图片JSON数组',
    `reply_content` varchar(500) DEFAULT NULL COMMENT '商家回复',
    `reply_time` datetime DEFAULT NULL COMMENT '商家回复时间',
    `is_anonymous` tinyint DEFAULT 0 COMMENT '是否匿名: 0-否 1-是',
    `status` tinyint DEFAULT 1 COMMENT '状态: 0-隐藏 1-显示',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- 首页轮播图表
CREATE TABLE IF NOT EXISTS `cms_banner` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` varchar(64) NOT NULL COMMENT '标题',
    `image_url` varchar(500) NOT NULL COMMENT '图片URL',
    `link_url` varchar(500) DEFAULT NULL COMMENT '跳转链接',
    `sort` int DEFAULT 0 COMMENT '排序',
    `status` tinyint DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页轮播图表';

-- 为已有表补充 deleted 字段（已执行过的环境可跳过）
ALTER TABLE `cms_banner` ADD COLUMN `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志: 0-未删除 1-已删除' AFTER `update_time`;

-- 插入默认轮播图
INSERT INTO `cms_banner` (`title`, `image_url`, `link_url`, `sort`) VALUES
('618年中大促', '/api/v1/files/default/cover', '/pages/search/index', 1),
('新品首发', '/api/v1/files/default/cover', '/pages/search/index', 2),
('百亿补贴', '/api/v1/files/default/cover', '/pages/coupon/center', 3);
