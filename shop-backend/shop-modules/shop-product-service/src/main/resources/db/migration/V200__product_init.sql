-- V1: 商品模块初始化
CREATE TABLE IF NOT EXISTS `pms_category` (
  `id` bigint NOT NULL,
  `name` varchar(64) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父分类ID',
  `level` tinyint DEFAULT 1 COMMENT '层级',
  `icon` varchar(500) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS `pms_brand` (
  `id` bigint NOT NULL,
  `name` varchar(128) NOT NULL COMMENT '品牌名称',
  `logo` varchar(500) DEFAULT NULL COMMENT '品牌Logo',
  `description` varchar(500) DEFAULT NULL COMMENT '品牌描述',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

CREATE TABLE IF NOT EXISTS `pms_product` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌ID',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `stock` int DEFAULT 0 COMMENT '库存',
  `sales` int DEFAULT 0 COMMENT '销量',
  `main_image` varchar(500) DEFAULT NULL COMMENT '主图',
  `images` text DEFAULT NULL COMMENT '图片列表JSON',
  `detail` text DEFAULT NULL COMMENT '详情HTML',
  `status` tinyint DEFAULT 1 COMMENT '状态 0-下架 1-上架',
  `is_new` tinyint DEFAULT 0 COMMENT '是否新品',
  `is_hot` tinyint DEFAULT 0 COMMENT '是否热销',
  `sort` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `pms_product_sku` (
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_name` varchar(255) DEFAULT NULL COMMENT 'SKU名称',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int DEFAULT 0 COMMENT '库存',
  `specs` varchar(500) DEFAULT NULL COMMENT '规格JSON',
  `image` varchar(500) DEFAULT NULL COMMENT 'SKU图片',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

CREATE TABLE IF NOT EXISTS `pms_product_review` (
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `rating` tinyint NOT NULL COMMENT '评分 1-5',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `images` text DEFAULT NULL COMMENT '评价图片JSON',
  `is_anonymous` tinyint DEFAULT 0 COMMENT '是否匿名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

CREATE TABLE IF NOT EXISTS `pms_search_keyword` (
  `id` bigint NOT NULL,
  `keyword` varchar(128) NOT NULL COMMENT '关键词',
  `count` int DEFAULT 0 COMMENT '搜索次数',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索关键词表';

CREATE TABLE IF NOT EXISTS `pms_new_product_banner` (
  `id` bigint NOT NULL,
  `title` varchar(128) NOT NULL COMMENT '标题',
  `subtitle` varchar(255) DEFAULT NULL COMMENT '副标题',
  `image` varchar(500) NOT NULL COMMENT '图片',
  `product_id` bigint DEFAULT NULL COMMENT '关联商品ID',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新品Banner表';
