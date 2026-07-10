-- V1: 营销模块初始化
CREATE TABLE IF NOT EXISTS `mms_coupon` (
  `id` bigint NOT NULL,
  `name` varchar(128) NOT NULL COMMENT '优惠券名称',
  `type` tinyint NOT NULL COMMENT '类型 1-满减 2-折扣 3-无门槛',
  `value` decimal(10,2) NOT NULL COMMENT '面值/折扣',
  `min_amount` decimal(10,2) DEFAULT 0 COMMENT '最低消费',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `total` int NOT NULL COMMENT '发行总量',
  `used` int DEFAULT 0 COMMENT '已使用数',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

CREATE TABLE IF NOT EXISTS `mms_coupon_receive` (
  `id` bigint NOT NULL,
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` tinyint DEFAULT 0 COMMENT '状态 0-未使用 1-已使用 2-已过期',
  `order_id` bigint DEFAULT NULL COMMENT '使用订单ID',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券领取记录表';

CREATE TABLE IF NOT EXISTS `mms_seckill_product` (
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `session_id` bigint NOT NULL COMMENT '场次ID',
  `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价',
  `seckill_stock` int NOT NULL COMMENT '秒杀库存',
  `seckill_limit` int DEFAULT 1 COMMENT '限购数量',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

CREATE TABLE IF NOT EXISTS `mms_seckill_session` (
  `id` bigint NOT NULL,
  `name` varchar(128) NOT NULL COMMENT '场次名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀场次表';

CREATE TABLE IF NOT EXISTS `mms_seckill_order` (
  `id` bigint NOT NULL,
  `seckill_product_id` bigint NOT NULL COMMENT '秒杀商品ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `status` tinyint DEFAULT 0 COMMENT '状态 0-待支付 1-已支付 2-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

CREATE TABLE IF NOT EXISTS `mms_seckill_message` (
  `id` bigint NOT NULL,
  `seckill_product_id` bigint NOT NULL COMMENT '秒杀商品ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` tinyint DEFAULT 0 COMMENT '状态 0-待处理 1-已处理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀消息表';

CREATE TABLE IF NOT EXISTS `mms_group_buy_activity` (
  `id` bigint NOT NULL,
  `name` varchar(128) NOT NULL COMMENT '活动名称',
  `product_id` bigint NOT NULL COMMENT '商品ID`,
  `group_price` decimal(10,2) NOT NULL COMMENT '团购价',
  `group_size` int NOT NULL COMMENT '成团人数',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购活动表';

CREATE TABLE IF NOT EXISTS `mms_group_buy_group` (
  `id` bigint NOT NULL,
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `leader_id` bigint NOT NULL COMMENT '团长ID',
  `status` tinyint DEFAULT 0 COMMENT '状态 0-进行中 1-已成团 2-已失败',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购组表';

CREATE TABLE IF NOT EXISTS `mms_group_buy_member` (
  `id` bigint NOT NULL,
  `group_id` bigint NOT NULL COMMENT '团购组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购成员表';

CREATE TABLE IF NOT EXISTS `mms_promotion` (
  `id` bigint NOT NULL,
  `name` varchar(128) NOT NULL COMMENT '促销名称',
  `type` tinyint NOT NULL COMMENT '类型 1-满减 2-折扣 3-赠品',
  `rules` text DEFAULT NULL COMMENT '规则JSON',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

CREATE TABLE IF NOT EXISTS `mms_promotion_scope` (
  `id` bigint NOT NULL,
  `promotion_id` bigint NOT NULL COMMENT '促销ID',
  `type` tinyint NOT NULL COMMENT '类型 1-商品 2-分类 3-品牌',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_promotion_id` (`promotion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销范围表';
