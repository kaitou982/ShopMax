-- ============================================================
-- 支付系统：支付流水表
-- ============================================================

CREATE TABLE IF NOT EXISTS `oms_payment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `payment_no` varchar(32) NOT NULL COMMENT '支付单号',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `order_no` varchar(32) NOT NULL COMMENT '订单号',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
    `pay_method` tinyint NOT NULL COMMENT '支付方式: 1-支付宝 2-微信 3-余额',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-待支付 1-支付成功 2-支付失败 3-退款中 4-已退款',
    `transaction_id` varchar(64) DEFAULT NULL COMMENT '第三方交易号',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
    `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
    `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';
