-- ShopMax电商平台商品系统数据库表结构
-- 创建日期: 2026-04-22
-- 数据库: MySQL 8.0+

-- 商品分类表
CREATE TABLE IF NOT EXISTS `pms_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID',
    `name` varchar(64) NOT NULL COMMENT '分类名称',
    `level` tinyint NOT NULL DEFAULT '1' COMMENT '分类层级: 1-一级 2-二级 3-三级',
    `icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
    `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 品牌表
CREATE TABLE IF NOT EXISTS `pms_brand` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
    `name` varchar(64) NOT NULL COMMENT '品牌名称',
    `logo` varchar(255) DEFAULT NULL COMMENT '品牌Logo',
    `description` varchar(500) DEFAULT NULL COMMENT '品牌描述',
    `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌表';

-- 商品SPU表
CREATE TABLE IF NOT EXISTS `pms_product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` varchar(200) NOT NULL COMMENT '商品名称',
    `subtitle` varchar(200) DEFAULT NULL COMMENT '商品副标题',
    `description` text COMMENT '商品描述',
    `main_image` varchar(500) DEFAULT NULL COMMENT '商品主图',
    `sub_images` varchar(2000) DEFAULT NULL COMMENT '商品副图，逗号分隔',
    `detail` text COMMENT '商品详情',
    `category_id` bigint DEFAULT NULL COMMENT '分类ID',
    `brand_id` bigint DEFAULT NULL COMMENT '品牌ID',
    `original_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '原价',
    `sale_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '销售价',
    `stock` int NOT NULL DEFAULT '0' COMMENT '库存数量',
    `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-下架 1-上架',
    `is_recommend` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐: 0-否 1-是',
    `is_new` tinyint NOT NULL DEFAULT '0' COMMENT '是否新品: 0-否 1-是',
    `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建者用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_recommend` (`is_recommend`),
    KEY `idx_is_new` (`is_new`),
    KEY `idx_product_list` (`deleted`,`status`,`category_id`,`create_time`),
    KEY `idx_product_recommend` (`deleted`,`status`,`is_recommend`,`sales`),
    KEY `idx_product_new` (`deleted`,`status`,`is_new`,`create_time`),
    KEY `idx_product_store` (`deleted`,`create_user_id`),
    KEY `idx_product_price` (`deleted`,`status`,`sale_price`),
    KEY `idx_product_sales` (`deleted`,`status`,`sales`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SPU表';

-- 商品SKU表
CREATE TABLE IF NOT EXISTS `pms_product_sku` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `title` varchar(200) NOT NULL COMMENT 'SKU标题',
    `image` varchar(500) DEFAULT NULL COMMENT 'SKU图片',
    `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格',
    `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
    `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
    `specs` varchar(500) DEFAULT NULL COMMENT 'SKU规格属性，JSON格式',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- 搜索关键词记录表
CREATE TABLE IF NOT EXISTS `cms_search_keyword` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `keyword` varchar(128) NOT NULL COMMENT '搜索关键词',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `search_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_keyword` (`keyword`),
    KEY `idx_search_time` (`search_time`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索关键词记录表';

-- 插入测试数据 - 分类
INSERT INTO `pms_category` (`name`, `parent_id`, `level`, `icon`, `sort`, `status`) VALUES
('手机数码', 0, 1, 'phone', 1, 1),
('电脑办公', 0, 1, 'computer', 2, 1),
('家用电器', 0, 1, 'appliance', 3, 1),
('服装鞋包', 0, 1, 'clothing', 4, 1),
('手机', 1, 2, '', 1, 1),
('平板', 1, 2, '', 2, 1),
('配件', 1, 2, '', 3, 1),
('笔记本', 2, 2, '', 1, 1),
('台式机', 2, 2, '', 2, 1),
('iPhone', 5, 3, '', 1, 1),
('华为手机', 5, 3, '', 2, 1),
('小米手机', 5, 3, '', 3, 1);

-- 插入测试数据 - 品牌
INSERT INTO `pms_brand` (`name`, `logo`, `description`, `sort`, `status`) VALUES
('Apple', '/api/v1/files/default/brand', '苹果公司', 1, 1),
('华为', '/api/v1/files/default/brand', '华为技术有限公司', 2, 1),
('小米', '/api/v1/files/default/brand', '小米科技有限责任公司', 3, 1),
('联想', '/api/v1/files/default/brand', '联想集团', 4, 1),
('海尔', '/api/v1/files/default/brand', '海尔集团', 5, 1);

-- 插入测试数据 - 商品
INSERT INTO `pms_product` (`name`, `subtitle`, `main_image`, `category_id`, `brand_id`, `original_price`, `sale_price`, `stock`, `sales`, `status`, `is_recommend`, `is_new`) VALUES
('iPhone 15 Pro', 'A17 Pro芯片，钛金属设计', '/api/v1/files/default/product', 10, 1, 8999.00, 7999.00, 100, 500, 1, 1, 1),
('iPhone 15', 'A16芯片，灵动岛设计', '/api/v1/files/default/product', 10, 1, 5999.00, 5299.00, 200, 800, 1, 1, 1),
('华为 Mate 60 Pro', '麒麟9000S，卫星通话', '/api/v1/files/default/product', 11, 2, 6999.00, 6999.00, 50, 1000, 1, 1, 1),
('小米14 Pro', '骁龙8 Gen3，徕卡影像', '/api/v1/files/default/product', 12, 3, 4999.00, 4599.00, 150, 600, 1, 1, 1),
('MacBook Pro 14', 'M3芯片，专业级性能', '/api/v1/files/default/product', 8, 1, 14999.00, 13999.00, 80, 300, 1, 1, 0);
