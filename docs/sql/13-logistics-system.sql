-- ============================================================
-- 物流跟踪系统
-- 创建时间: 2026-06-07
-- ============================================================

-- 物流信息表
CREATE TABLE IF NOT EXISTS `oms_logistics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `logistics_no` varchar(64) NOT NULL COMMENT '物流单号',
  `company` varchar(32) NOT NULL COMMENT '物流公司',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-已发货 1-运输中 2-派送中 3-已签收',
  `sender_name` varchar(32) DEFAULT NULL COMMENT '发件人姓名',
  `sender_phone` varchar(20) DEFAULT NULL COMMENT '发件人电话',
  `sender_address` varchar(255) DEFAULT NULL COMMENT '发件人地址',
  `receiver_name` varchar(32) DEFAULT NULL COMMENT '收件人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收件人电话',
  `receiver_address` varchar(255) DEFAULT NULL COMMENT '收件人地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_logistics_no` (`logistics_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流信息表';

-- 物流轨迹表
CREATE TABLE IF NOT EXISTS `oms_logistics_trace` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `logistics_id` bigint NOT NULL COMMENT '物流ID',
  `trace_time` datetime NOT NULL COMMENT '轨迹时间',
  `content` varchar(500) NOT NULL COMMENT '轨迹内容',
  `location` varchar(128) DEFAULT NULL COMMENT '当前位置',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_logistics_id` (`logistics_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';
