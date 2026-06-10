-- ShopMax电商平台内容社区数据库表结构
-- 创建日期: 2026-05-21
-- 数据库: MySQL 8.0+
-- 模块: 内容社区 (Community System - cms_)

-- ============================================================
-- 种草笔记主表
-- ============================================================
CREATE TABLE IF NOT EXISTS `cms_note` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    `user_id` bigint NOT NULL COMMENT '作者用户ID',
    `title` varchar(128) DEFAULT NULL COMMENT '笔记标题',
    `content` text COMMENT '笔记正文',
    `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图URL',
    `content_type` tinyint NOT NULL DEFAULT '1' COMMENT '内容类型: 1-图文 2-视频(P2)',
    `video_url` varchar(500) DEFAULT NULL COMMENT '视频URL(P2预留)',
    `video_duration` int DEFAULT NULL COMMENT '视频时长秒数(P2预留)',
    `status` tinyint NOT NULL DEFAULT '3' COMMENT '状态: 1-草稿 2-已发布 3-审核中 4-已驳回 5-已删除',
    `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
    `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
    `audit_user_id` bigint DEFAULT NULL COMMENT '审核人ID',
    `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览数',
    `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数(冗余)',
    `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数(冗余)',
    `favorite_count` int NOT NULL DEFAULT '0' COMMENT '收藏数(冗余)',
    `share_count` int NOT NULL DEFAULT '0' COMMENT '分享数(冗余)',
    `is_recommended` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐: 0-否 1-是(P2)',
    `is_top` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶: 0-否 1-是(P2)',
    `is_essence` tinyint NOT NULL DEFAULT '0' COMMENT '是否加精: 0-否 1-是(P2)',
    `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度(P2预留)',
    `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度(P2预留)',
    `location_name` varchar(128) DEFAULT NULL COMMENT '位置名称(P2预留)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_content_type` (`content_type`),
    KEY `idx_is_recommended` (`is_recommended`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_user_status_time` (`user_id`, `status`, `create_time`),
    KEY `idx_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='种草笔记主表';

-- ============================================================
-- 笔记图片表
-- ============================================================
CREATE TABLE IF NOT EXISTS `cms_note_image` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `note_id` bigint NOT NULL COMMENT '笔记ID',
    `image_url` varchar(500) NOT NULL COMMENT '图片URL',
    `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序序号',
    `width` int DEFAULT NULL COMMENT '图片宽度(P2预留)',
    `height` int DEFAULT NULL COMMENT '图片高度(P2预留)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_note_id` (`note_id`),
    KEY `idx_note_sort` (`note_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记图片表';

-- ============================================================
-- 笔记关联商品表
-- ============================================================
CREATE TABLE IF NOT EXISTS `cms_note_product` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `note_id` bigint NOT NULL COMMENT '笔记ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_note_id` (`note_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记关联商品表';

-- ============================================================
-- 笔记点赞表
-- ============================================================
CREATE TABLE IF NOT EXISTS `cms_note_like` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `note_id` bigint NOT NULL COMMENT '笔记ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_note_user` (`note_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记点赞表';

-- ============================================================
-- 笔记评论表（支持楼中楼回复）
-- ============================================================
CREATE TABLE IF NOT EXISTS `cms_note_comment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `note_id` bigint NOT NULL COMMENT '笔记ID',
    `user_id` bigint NOT NULL COMMENT '评论用户ID',
    `parent_id` bigint DEFAULT NULL COMMENT '父评论ID(NULL=一级评论, 非NULL=二级回复)',
    `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复用户ID',
    `content` varchar(1000) NOT NULL COMMENT '评论内容',
    `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_note_id` (`note_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_note_parent_time` (`note_id`, `parent_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记评论表';

-- ============================================================
-- 笔记收藏表
-- ============================================================
CREATE TABLE IF NOT EXISTS `cms_note_favorite` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `note_id` bigint NOT NULL COMMENT '笔记ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_note_user` (`note_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记收藏表';

-- ============================================================
-- 测试数据
-- ============================================================

-- 测试笔记数据（作者: user001=张三, user002=李四, user003=王五）
INSERT INTO `cms_note` (`id`, `user_id`, `title`, `content`, `cover_url`, `status`, `view_count`, `like_count`, `comment_count`, `favorite_count`, `is_recommended`) VALUES
(1, 2, '夏日穿搭分享 — 超舒服的纯棉T恤', '这件T恤真的太舒服了！面料柔软透气，夏天穿完全不会闷热，推荐给大家～\n\n我买了白色和黑色两个颜色，都超级百搭。搭配牛仔裤或者短裙都可以，质量也很好，洗了几次都没有变形。', '/api/v1/files/default/community', 2, 1280, 128, 15, 42, 1),
(2, 3, '蓝牙耳机深度测评 | 百元价位千元体验', '用了一周这款蓝牙耳机，音质出乎意料的好。降噪效果在通勤时非常实用，续航也能满足一天的使用。\n\n主要优点：\n1. 音质清晰，低音饱满\n2. 佩戴舒适，长时间不累\n3. 续航约6小时\n\n缺点：充电盒有点大', '/api/v1/files/default/community', 2, 2560, 230, 42, 88, 1),
(3, 2, '租房好物清单 | 小空间收纳神器', '搬进30平小公寓后，收纳成了最大的问题。整理了这一个月来觉得最实用的收纳好物～\n\n1. 床底收纳箱 - 换季衣物终于有地方放了\n2. 门后挂钩架 - 包包帽子全部上墙\n3. 免打孔置物架 - 卫生间神器', '/api/v1/files/default/community', 2, 980, 86, 12, 30, 0),
(4, 3, '平价口红试色合集 💋 10支50元以内', '整理了最近入手的10支平价口红，每一支都不超过50元！学生党友好～\n\n试色从豆沙色到正红色都有，黄皮友好度也标注了。个人最推荐第3支，显白又日常。', '/api/v1/files/default/community', 3, 0, 0, 0, 0, 0),
(5, 1, '周末烘焙 | 零失败的巧克力熔岩蛋糕', '第一次做就成功了！分享这个超简单的配方～\n\n材料：黑巧克力100g、黄油50g、鸡蛋2个、低筋面粉30g、糖40g\n\n步骤：\n1. 巧克力和黄油隔水融化\n2. 鸡蛋加糖打发\n3. 混合后筛入面粉\n4. 200度烤12分钟', '/api/v1/files/default/community', 2, 1890, 196, 28, 65, 0),
(6, 2, 'iPhone 15 Pro Max 一个月使用感受', '从安卓换到iPhone，最大的感受就是系统流畅度确实不一样。\n\n拍照方面：人像模式进步很大，暗光表现也很不错\n续航：一天一充够用\n缺点：充电速度还是偏慢\n\n总体来说值得入手！', '/api/v1/files/default/community', 2, 3450, 312, 56, 120, 1);

-- 测试图片数据
INSERT INTO `cms_note_image` (`note_id`, `image_url`, `sort_order`) VALUES
(1, '/api/v1/files/default/community', 0),
(1, '/api/v1/files/default/community', 1),
(1, '/api/v1/files/default/community', 2),
(2, '/api/v1/files/default/community', 0),
(2, '/api/v1/files/default/community', 1),
(3, '/api/v1/files/default/community', 0),
(4, '/api/v1/files/default/community', 0),
(5, '/api/v1/files/default/community', 0),
(6, '/api/v1/files/default/community', 0),
(6, '/api/v1/files/default/community', 1);

-- 测试商品关联数据（关联已有商品ID）
INSERT INTO `cms_note_product` (`note_id`, `product_id`) VALUES
(1, 1),
(2, 2),
(6, 3);

-- 测试点赞数据
INSERT INTO `cms_note_like` (`note_id`, `user_id`) VALUES
(1, 1), (1, 3), (1, 4),
(2, 1), (2, 2),
(3, 1), (3, 3), (3, 4),
(5, 2), (5, 3), (5, 4),
(6, 2), (6, 3);

-- 测试评论数据
INSERT INTO `cms_note_comment` (`note_id`, `user_id`, `parent_id`, `reply_to_user_id`, `content`) VALUES
(1, 3, NULL, NULL, '颜色好看！我也买了同款，质量真的不错 👍'),
(1, 1, NULL, NULL, '尺码偏大还是偏小？想入手一件'),
(1, 2, 2, 1, '我觉得码数正常，按平时穿的选就行～'),
(2, 1, NULL, NULL, '这款我也在用，降噪确实不错'),
(2, 2, NULL, NULL, '音质和AirPods比怎么样？'),
(2, 3, 5, 2, '个人觉得差距不大，这个价格性价比很高'),
(3, 3, NULL, NULL, '床底收纳箱求链接！'),
(5, 2, NULL, NULL, '周末试了一下，真的零失败！感谢分享'),
(5, 3, 8, 2, '开心！烤好的时候整个厨房都是巧克力香味'),
(6, 3, NULL, NULL, '拍照效果确实好，尤其人像模式'),
(6, 1, NULL, NULL, '续航能撑一天重度使用吗？'),
(6, 2, 11, 1, '重度使用的话下午需要充一次，轻度完全够');

-- 测试收藏数据
INSERT INTO `cms_note_favorite` (`note_id`, `user_id`) VALUES
(1, 1), (1, 3),
(2, 1), (2, 4),
(3, 3),
(5, 1), (5, 2),
(6, 1), (6, 2), (6, 3);
