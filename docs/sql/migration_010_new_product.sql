-- Migration 010: 新品首发功能
-- 1. pms_product 新增新品生命周期字段
-- 2. 新增 pms_new_product_banner 表

-- 1. pms_product 新增字段
ALTER TABLE pms_product
    ADD COLUMN `new_product_sort` int NOT NULL DEFAULT 0 COMMENT '新品排序权重（数值越大越靠前）' AFTER `is_new`,
    ADD COLUMN `new_product_start_time` datetime DEFAULT NULL COMMENT '新品上架时间（为空则永久展示）' AFTER `new_product_sort`,
    ADD COLUMN `new_product_end_time` datetime DEFAULT NULL COMMENT '新品下架时间（为空则不自动过期）' AFTER `new_product_start_time`;

-- 新品排序复合索引
ALTER TABLE pms_product ADD INDEX idx_product_new_sort (`deleted`, `status`, `is_new`, `new_product_sort`);

-- 2. 新品首发Banner推荐位表
CREATE TABLE IF NOT EXISTS `pms_new_product_banner` (
    `id`             bigint NOT NULL AUTO_INCREMENT,
    `title`          varchar(128) NOT NULL COMMENT 'Banner标题',
    `image_url`      varchar(500) NOT NULL COMMENT 'Banner图片',
    `product_id`     bigint DEFAULT NULL COMMENT '关联商品ID（点击跳转商品详情）',
    `link_url`       varchar(500) DEFAULT NULL COMMENT '外部链接（与product_id二选一）',
    `sort`           int NOT NULL DEFAULT 0 COMMENT '排序',
    `status`         tinyint NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    `start_time`     datetime DEFAULT NULL COMMENT '展示开始时间',
    `end_time`       datetime DEFAULT NULL COMMENT '展示结束时间',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新品首发Banner推荐位';

-- 测试数据：新品Banner
INSERT INTO `pms_new_product_banner` (`title`, `image_url`, `product_id`, `sort`, `status`) VALUES
('iPhone 15 Pro 新品首发', '/api/v1/files/default/product', 1, 100, 1),
('华为 Mate 60 Pro 限量发售', '/api/v1/files/default/product', 3, 90, 1),
('小米14 Pro 震撼上市', '/api/v1/files/default/product', 4, 80, 1);
