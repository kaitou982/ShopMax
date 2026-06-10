-- Migration 006: 搜索关键词记录表 + 排序优化索引
-- 日期: 2026-05-31

-- 1. 搜索关键词记录表
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

-- 2. 商品价格排序索引
ALTER TABLE pms_product ADD INDEX idx_product_price (deleted, status, sale_price);

-- 3. 商品销量排序索引
ALTER TABLE pms_product ADD INDEX idx_product_sales (deleted, status, sales);
