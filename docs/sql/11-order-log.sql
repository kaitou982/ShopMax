-- =====================================================
-- 订单操作日志表
-- =====================================================
CREATE TABLE IF NOT EXISTS `oms_order_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `order_no` varchar(64) NOT NULL COMMENT '订单编号',
    `old_status` varchar(32) DEFAULT NULL COMMENT '操作前状态',
    `action` varchar(32) NOT NULL COMMENT '操作动作: CREATE/PAY/SHIP/CONFIRM/CANCEL/REFUND/REFUND_APPLY/DELETE',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志表';
