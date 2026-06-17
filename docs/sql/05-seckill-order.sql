-- 秒杀订单表
CREATE TABLE IF NOT EXISTS `oms_seckill_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `session_id` BIGINT NOT NULL COMMENT '秒杀场次ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待支付 1-已支付 2-已取消 3-已超时',
    `expire_time` DATETIME NOT NULL COMMENT '支付过期时间',
    `pay_time` DATETIME COMMENT '支付时间',
    `order_id` BIGINT COMMENT '正式订单ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_user_session_product` (`user_id`, `session_id`, `product_id`),
    KEY `idx_status_expire` (`status`, `expire_time`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 秒杀本地消息表
CREATE TABLE IF NOT EXISTS `mms_seckill_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `message_type` VARCHAR(32) NOT NULL COMMENT '消息类型：SECKILL_ORDER',
    `business_id` VARCHAR(64) NOT NULL COMMENT '业务ID：秒杀订单号',
    `content` TEXT NOT NULL COMMENT '消息内容(JSON)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-已处理 2-处理失败 3-已死信',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `next_retry_time` DATETIME COMMENT '下次重试时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_next_retry` (`status`, `next_retry_time`),
    KEY `idx_business_id` (`business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀本地消息表';
