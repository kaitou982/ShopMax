-- ShopMax电商平台订单系统数据库表结构
-- 创建日期: 2026-04-22
-- 数据库: MySQL 8.0+

-- 订单主表
CREATE TABLE IF NOT EXISTS `oms_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` varchar(64) NOT NULL COMMENT '订单编号',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总金额',
    `pay_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '应付金额',
    `freight_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '运费',
    `coupon_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '优惠券抵扣金额',
    `integral_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '积分抵扣金额',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态: 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-退款中 6-已退款',
    `pay_type` tinyint DEFAULT NULL COMMENT '支付方式: 1-支付宝 2-微信 3-余额',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
    `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
    `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
    `receiver_name` varchar(64) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` varchar(20) NOT NULL COMMENT '收货人手机号',
    `receiver_address` varchar(500) NOT NULL COMMENT '收货地址',
    `remark` varchar(500) DEFAULT NULL COMMENT '买家留言',
    `source_type` tinyint NOT NULL DEFAULT '1' COMMENT '订单来源: 1-PC 2-H5 3-小程序 4-APP',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- 订单商品明细表
CREATE TABLE IF NOT EXISTS `oms_order_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
    `product_name` varchar(200) NOT NULL COMMENT '商品名称',
    `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
    `sku_specs` varchar(500) DEFAULT NULL COMMENT 'SKU规格',
    `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
    `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
    `subtotal` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '小计金额',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `oms_cart_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
    `product_name` varchar(200) NOT NULL COMMENT '商品名称',
    `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
    `sku_specs` varchar(500) DEFAULT NULL COMMENT 'SKU规格',
    `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
    `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
    `selected` tinyint NOT NULL DEFAULT '1' COMMENT '是否选中: 0-否 1-是',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 插入测试数据 - 订单
INSERT INTO `oms_order` (`order_no`, `user_id`, `total_amount`, `pay_amount`, `freight_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `source_type`) VALUES
('SN20240422000001', 2, 7999.00, 7999.00, 0.00, 3, '张三', '13800138001', '北京市北京市朝阳区建国路88号SOHO现代城A座1001室', 1),
('SN20240422000002', 2, 5299.00, 5299.00, 0.00, 1, '张三', '13800138001', '北京市北京市朝阳区建国路88号SOHO现代城A座1001室', 1),
('SN20240422000003', 3, 6999.00, 6999.00, 0.00, 2, '李四', '13800138002', '上海市上海市浦东新区陆家嘴环路1000号恒生银行大厦20楼', 1),
('SN20240422000004', 3, 11598.00, 11598.00, 0.00, 0, '李四', '13800138002', '上海市上海市浦东新区陆家嘴环路1000号恒生银行大厦20楼', 1),
('SN20240422000005', 2, 4599.00, 4599.00, 10.00, 4, '张三', '13800138001', '北京市北京市朝阳区建国路88号SOHO现代城A座1001室', 1);

-- 插入测试数据 - 订单明细
INSERT INTO `oms_order_item` (`order_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `subtotal`) VALUES
(1, 1, 'iPhone 15 Pro', '/api/v1/files/default/product', 7999.00, 1, 7999.00),
(2, 2, 'iPhone 15', '/api/v1/files/default/product', 5299.00, 1, 5299.00),
(3, 3, '华为 Mate 60 Pro', '/api/v1/files/default/product', 6999.00, 1, 6999.00),
(4, 1, 'iPhone 15 Pro', '/api/v1/files/default/product', 7999.00, 1, 7999.00),
(4, 2, 'iPhone 15', '/api/v1/files/default/product', 3599.00, 1, 3599.00),
(5, 4, '小米14 Pro', '/api/v1/files/default/product', 4599.00, 1, 4599.00);
