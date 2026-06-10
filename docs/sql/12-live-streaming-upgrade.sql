-- ============================================================
-- ShopMax 直播带货系统数据库升级脚本
-- 日期: 2026-06-05
-- 说明: 新增礼物系统、虚拟币系统、直播状态扩展
-- ============================================================

-- 1. 新增礼物配置表
CREATE TABLE IF NOT EXISTS `lms_gift` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(32)  NOT NULL             COMMENT '礼物名称',
    `icon`          VARCHAR(255) NOT NULL             COMMENT '图标URL',
    `animation_url` VARCHAR(255) DEFAULT NULL         COMMENT 'Lottie动画URL',
    `price`         INT          NOT NULL             COMMENT '虚拟币价格',
    `sort_order`    INT          NOT NULL DEFAULT 0   COMMENT '排序',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='礼物配置表';

-- 预置礼物数据
INSERT INTO `lms_gift` (`name`, `icon`, `animation_url`, `price`, `sort_order`) VALUES
('玫瑰',   '/gifts/rose.png',   '/gifts/rose.json',   10,  1),
('棒棒糖', '/gifts/lollipop.png', '/gifts/lollipop.json', 20,  2),
('奶茶',   '/gifts/milktea.png', '/gifts/milktea.json', 50,  3),
('吉他',   '/gifts/guitar.png',  '/gifts/guitar.json',  100, 4),
('火箭',   '/gifts/rocket.png',  '/gifts/rocket.json',  200, 5),
('钻石',   '/gifts/diamond.png', '/gifts/diamond.json', 500, 6);

-- 2. 新增虚拟币流水表
CREATE TABLE IF NOT EXISTS `lms_coin_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL             COMMENT '用户ID',
    `amount`        INT          NOT NULL             COMMENT '变动数量(正增负减)',
    `type`          TINYINT      NOT NULL             COMMENT '1注册赠送 2每日签到 3送礼消费 4系统赠送',
    `biz_id`        VARCHAR(64)  DEFAULT NULL         COMMENT '关联业务ID(消息ID)',
    `remark`        VARCHAR(128) DEFAULT NULL         COMMENT '备注',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟币流水表';

-- 3. 用户表新增虚拟币余额字段
ALTER TABLE `ums_user`
ADD COLUMN `coin_balance` INT NOT NULL DEFAULT 0 COMMENT '虚拟币余额';

-- 4. 直播间表新增字段
ALTER TABLE `lms_live_room`
ADD COLUMN `like_count` BIGINT NOT NULL DEFAULT 0 COMMENT '点赞总数',
ADD COLUMN `gift_count` BIGINT NOT NULL DEFAULT 0 COMMENT '礼物总数',
ADD COLUMN `replay_duration` INT DEFAULT NULL COMMENT '回放时长(秒)',
MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0预告 1直播中 2已结束 3已关闭 4待推流';

-- 5. 直播消息表新增索引（支持回放历史弹幕查询）
ALTER TABLE `lms_live_message`
ADD INDEX `idx_room_type_time` (`room_id`, `type`, `create_time`);

-- ============================================================
-- 执行完成
-- ============================================================
