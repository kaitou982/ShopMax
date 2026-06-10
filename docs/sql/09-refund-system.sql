-- ============================================================
-- 退款系统：退款记录表（审计追溯）
-- ============================================================

CREATE TABLE IF NOT EXISTS `oms_refund_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `refund_no` varchar(64) NOT NULL COMMENT '退款单号（幂等键，对应支付宝 out_request_no）',
    `payment_no` varchar(32) NOT NULL COMMENT '关联支付单号',
    `order_no` varchar(64) NOT NULL COMMENT '关联订单号',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `refund_amount` decimal(10,2) NOT NULL COMMENT '本次退款金额',
    `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '退款状态: 0-处理中 1-退款成功 2-退款失败',
    `pay_method` tinyint NOT NULL COMMENT '支付方式: 1-支付宝 2-微信 3-余额',
    `gateway_refund_no` varchar(64) DEFAULT NULL COMMENT '第三方退款流水号',
    `fail_reason` varchar(500) DEFAULT NULL COMMENT '退款失败原因',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

-- 给 oms_payment 表新增退款原因字段
ALTER TABLE `oms_payment` ADD COLUMN `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因' AFTER `refund_amount`;
