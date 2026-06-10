-- ============================================================
-- 物流表添加坐标字段
-- 创建时间: 2026-06-07
-- ============================================================

-- 物流表添加发件人坐标
ALTER TABLE `oms_logistics`
ADD COLUMN `sender_latitude` decimal(10,6) DEFAULT NULL COMMENT '发件人纬度' AFTER `sender_address`,
ADD COLUMN `sender_longitude` decimal(10,6) DEFAULT NULL COMMENT '发件人经度' AFTER `sender_latitude`;

-- 物流表添加收件人坐标
ALTER TABLE `oms_logistics`
ADD COLUMN `receiver_latitude` decimal(10,6) DEFAULT NULL COMMENT '收件人纬度' AFTER `receiver_address`,
ADD COLUMN `receiver_longitude` decimal(10,6) DEFAULT NULL COMMENT '收件人经度' AFTER `receiver_latitude`;
