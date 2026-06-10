-- ShopMax电商平台用户系统数据库表结构
-- 创建日期: 2026-04-15
-- 数据库: MySQL 8.0+

-- 用户基础表
CREATE TABLE IF NOT EXISTS `ums_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(64) NOT NULL COMMENT '用户名',
    `password` varchar(128) NOT NULL COMMENT '加密密码',
    `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `phone` varchar(20) NOT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `gender` tinyint DEFAULT '0' COMMENT '性别: 0-未知 1-男 2-女',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
    `member_level` tinyint NOT NULL DEFAULT '1' COMMENT '会员等级: 1-普通 2-银卡 3-金卡 4-钻石',
    `integral` int NOT NULL DEFAULT '0' COMMENT '积分',
    `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
    `growth_value` int NOT NULL DEFAULT '0' COMMENT '成长值',
    `openid_mp` varchar(100) DEFAULT NULL COMMENT '微信小程序openid',
    `openid_app` varchar(100) DEFAULT NULL COMMENT '微信APP openid',
    `unionid` varchar(100) DEFAULT NULL COMMENT '微信unionid',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_openid_mp` (`openid_mp`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础表';

-- 用户收货地址表
CREATE TABLE IF NOT EXISTS `ums_user_address` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `receiver_name` varchar(64) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` varchar(20) NOT NULL COMMENT '收货人手机号',
    `province` varchar(50) NOT NULL COMMENT '省份',
    `province_code` varchar(20) DEFAULT NULL COMMENT '省份编码',
    `city` varchar(50) NOT NULL COMMENT '城市',
    `city_code` varchar(20) DEFAULT NULL COMMENT '城市编码',
    `district` varchar(50) NOT NULL COMMENT '区/县',
    `district_code` varchar(20) DEFAULT NULL COMMENT '区/县编码',
    `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
    `full_address` varchar(500) GENERATED ALWAYS AS (concat(`province`, `city`, `district`, `detail_address`)) STORED COMMENT '完整地址',
    `postal_code` varchar(10) DEFAULT NULL COMMENT '邮编',
    `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认: 0-否 1-是',
    `label` varchar(20) DEFAULT NULL COMMENT '标签: 家/公司/学校等',
    `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
    `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

-- 插入测试数据
INSERT INTO `ums_user` (`username`, `password`, `nickname`, `phone`, `email`, `gender`, `status`, `member_level`, `integral`, `balance`, `growth_value`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '管理员', '13800138000', 'admin@shopmax.com', 1, 1, 4, 10000, 9999.99, 5000),
('user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '张三', '13800138001', 'user001@test.com', 1, 1, 1, 100, 0.00, 50),
('user002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '李四', '13800138002', 'user002@test.com', 2, 1, 2, 500, 50.00, 200),
('user003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '王五', '13800138003', 'user003@test.com', 1, 1, 1, 0, 0.00, 0);

-- 插入测试地址数据
INSERT INTO `ums_user_address` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `province_code`, `city`, `city_code`, `district`, `district_code`, `detail_address`, `postal_code`, `is_default`, `label`) VALUES
(2, '张三', '13800138001', '北京市', '110000', '北京市', '110100', '朝阳区', '110105', '建国路88号SOHO现代城A座1001室', '100022', 1, '公司'),
(2, '张三', '13800138001', '北京市', '110000', '北京市', '110100', '海淀区', '110108', '中关村大街1号海龙大厦10层', '100080', 0, '家'),
(3, '李四', '13800138002', '上海市', '310000', '上海市', '310100', '浦东新区', '310115', '陆家嘴环路1000号恒生银行大厦20楼', '200120', 1, '公司'),
(3, '李四', '13800138002', '上海市', '310000', '上海市', '310100', '黄浦区', '310101', '南京东路100号', '200002', 0, '家');
