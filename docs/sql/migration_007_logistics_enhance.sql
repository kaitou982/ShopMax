-- ============================================================
-- 物流跟踪系统增强
-- 创建时间: 2026-06-07
-- 说明: 添加缓存字段和坐标字段
-- ============================================================

-- 1. 物流信息表添加缓存字段
ALTER TABLE `oms_logistics`
ADD COLUMN `last_query_time` datetime DEFAULT NULL COMMENT '上次查询API时间' AFTER `receiver_address`;

-- 2. 物流轨迹表添加坐标字段
ALTER TABLE `oms_logistics_trace`
ADD COLUMN `location_code` varchar(32) DEFAULT NULL COMMENT '地点编码' AFTER `location`,
ADD COLUMN `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度' AFTER `location_code`,
ADD COLUMN `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度' AFTER `latitude`;

-- 3. 创建物流公司编码表（可选，用于管理快递公司信息）
CREATE TABLE IF NOT EXISTS `oms_logistics_company` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '公司名称',
  `code` varchar(32) NOT NULL COMMENT '公司编码',
  `website` varchar(128) DEFAULT NULL COMMENT '官网',
  `phone` varchar(20) DEFAULT NULL COMMENT '客服电话',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流公司表';

-- 4. 插入常用物流公司数据
INSERT INTO `oms_logistics_company` (`name`, `code`, `website`, `phone`) VALUES
('顺丰速运', 'shunfeng', 'https://www.sf-express.com', '95338'),
('韵达快递', 'yunda', 'https://www.yundaex.com', '95546'),
('圆通速递', 'yuantong', 'https://www.yto.net.cn', '95554'),
('中通快递', 'zhongtong', 'https://www.zto.com', '95311'),
('申通快递', 'shentong', 'https://www.sto.cn', '95543'),
('京东物流', 'jd', 'https://www.jdl.com', '950616'),
('EMS', 'ems', 'https://www.ems.com.cn', '11183'),
('德邦快递', 'debang', 'https://www.deppon.com', '95353'),
('极兔速递', 'jtexpress', 'https://www.jtexpress.com', '956036'),
('百世快递', 'huitongkuaidi', 'https://www.800bestex.com', '95320');
