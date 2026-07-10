-- V1: 管理模块初始化
CREATE TABLE IF NOT EXISTS `sys_banner` (
  `id` bigint NOT NULL,
  `title` varchar(128) NOT NULL COMMENT '标题',
  `image` varchar(500) NOT NULL COMMENT '图片URL',
  `url` varchar(500) DEFAULT NULL COMMENT '跳转链接',
  `position` tinyint DEFAULT 1 COMMENT '位置 1-首页 2-分类页',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner表';

CREATE TABLE IF NOT EXISTS `sys_notification` (
  `id` bigint NOT NULL,
  `title` varchar(128) NOT NULL COMMENT '标题',
  `content` text DEFAULT NULL COMMENT '内容',
  `type` tinyint DEFAULT 0 COMMENT '类型 0-系统通知 1-订单通知 2-活动通知',
  `target_user_id` bigint DEFAULT NULL COMMENT '目标用户ID（NULL表示全体）',
  `status` tinyint DEFAULT 0 COMMENT '状态 0-未读 1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_target_user_id` (`target_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
