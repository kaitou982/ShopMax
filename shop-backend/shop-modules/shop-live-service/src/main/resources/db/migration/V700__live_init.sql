-- V1: 直播模块初始化
CREATE TABLE IF NOT EXISTS `lms_live_room` (
  `id` bigint NOT NULL,
  `anchor_id` bigint NOT NULL COMMENT '主播ID',
  `title` varchar(128) NOT NULL COMMENT '直播间标题',
  `cover` varchar(500) DEFAULT NULL COMMENT '封面图',
  `stream_key` varchar(128) DEFAULT NULL COMMENT '推流密钥',
  `status` tinyint DEFAULT 0 COMMENT '状态 0-未开播 1-直播中 2-已结束',
  `viewer_count` int DEFAULT 0 COMMENT '观看人数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_anchor_id` (`anchor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间表';

CREATE TABLE IF NOT EXISTS `lms_anchor` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(64) NOT NULL COMMENT '主播名称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `description` varchar(500) DEFAULT NULL COMMENT '简介',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主播表';

CREATE TABLE IF NOT EXISTS `lms_live_product` (
  `id` bigint NOT NULL,
  `live_room_id` bigint NOT NULL COMMENT '直播间ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_live_room_id` (`live_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播商品表';

CREATE TABLE IF NOT EXISTS `lms_live_message` (
  `id` bigint NOT NULL,
  `live_room_id` bigint NOT NULL COMMENT '直播间ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` varchar(500) NOT NULL COMMENT '消息内容',
  `type` tinyint DEFAULT 0 COMMENT '类型 0-文字 1-礼物 2-进入',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_live_room_id` (`live_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播消息表';

CREATE TABLE IF NOT EXISTS `lms_gift` (
  `id` bigint NOT NULL,
  `name` varchar(64) NOT NULL COMMENT '礼物名称',
  `icon` varchar(500) DEFAULT NULL COMMENT '图标',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `animation` varchar(500) DEFAULT NULL COMMENT '动画URL',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='礼物表';

CREATE TABLE IF NOT EXISTS `lms_coin_log` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` int NOT NULL COMMENT '金币变动值',
  `type` tinyint NOT NULL COMMENT '类型 1-收入 2-支出',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金币记录表';
