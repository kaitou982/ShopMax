-- V1: 支付模块初始化
CREATE TABLE IF NOT EXISTS `pay_payment` (
  `id` bigint NOT NULL,
  `payment_no` varchar(64) NOT NULL COMMENT '支付单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `channel` varchar(32) NOT NULL COMMENT '支付渠道 ALIPAY/WX/PAY',
  `status` tinyint DEFAULT 0 COMMENT '状态 0-待支付 1-已支付 2-已退款',
  `trade_no` varchar(128) DEFAULT NULL COMMENT '第三方交易号',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单表';

CREATE TABLE IF NOT EXISTS `pay_refund_record` (
  `id` bigint NOT NULL,
  `refund_no` varchar(64) NOT NULL COMMENT '退款单号',
  `payment_id` bigint NOT NULL COMMENT '支付单ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
  `status` tinyint DEFAULT 0 COMMENT '状态 0-待退款 1-已退款 2-退款失败',
  `trade_no` varchar(128) DEFAULT NULL COMMENT '第三方退款号',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_payment_id` (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';
