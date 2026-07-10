-- 通知消息表
CREATE TABLE IF NOT EXISTS `sys_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` TINYINT NOT NULL COMMENT '通知类型: 1退款申请 2入驻审核 3内容审核 4库存预警',
  `title` VARCHAR(128) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `ref_type` VARCHAR(32) DEFAULT NULL COMMENT '关联业务类型: refund/store_apply/note_audit/stock',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';
