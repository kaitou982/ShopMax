-- ============================================================
-- ShopMax 角色与店家入驻迁移脚本
-- 功能: 三角色体系 (ADMIN/STORE/USER) + 店家入驻审核
-- 版本: v1.0.0
-- 日期: 2026-05-21
-- ============================================================

ALTER TABLE ums_user
  ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/STORE/USER',
  ADD COLUMN store_status TINYINT DEFAULT NULL COMMENT '店家审核状态: 0-待审核 1-已通过 2-已拒绝',
  ADD COLUMN store_name VARCHAR(128) DEFAULT NULL COMMENT '店铺名称',
  ADD COLUMN store_logo VARCHAR(500) DEFAULT NULL COMMENT '店铺Logo',
  ADD COLUMN store_description VARCHAR(1000) DEFAULT NULL COMMENT '店铺简介',
  ADD COLUMN store_apply_time DATETIME DEFAULT NULL COMMENT '申请入驻时间',
  ADD COLUMN store_audit_time DATETIME DEFAULT NULL COMMENT '入驻审核时间',
  ADD COLUMN store_reject_reason VARCHAR(500) DEFAULT NULL COMMENT '入驻拒绝原因',
  ADD INDEX idx_role (role),
  ADD INDEX idx_store_status (store_status);

-- 种子数据: username='admin' 设为管理员
UPDATE ums_user SET role = 'ADMIN' WHERE username = 'admin';

-- ============================================================
-- STORE 数据隔离：商品表添加创建者字段
-- ============================================================
ALTER TABLE pms_product
  ADD COLUMN create_user_id BIGINT DEFAULT NULL COMMENT '创建者用户ID（店家关联）',
  ADD INDEX idx_create_user_id (create_user_id);
