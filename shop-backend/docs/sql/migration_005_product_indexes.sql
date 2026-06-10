-- Migration 005: 商品表性能优化索引 + 缺失字段修复
-- 日期: 2026-05-31

-- 1. 修复缺失的 create_user_id 字段（Product 实体已声明但 SQL schema 遗漏）
ALTER TABLE pms_product ADD COLUMN create_user_id BIGINT DEFAULT NULL COMMENT '创建者用户ID' AFTER sort;

-- 2. 商品列表查询复合索引（覆盖 page() 主查询: deleted + status + category_id + create_time）
ALTER TABLE pms_product ADD INDEX idx_product_list (deleted, status, category_id, create_time);

-- 3. 推荐商品查询复合索引（覆盖 listRecommend: deleted + status + is_recommend + sales）
ALTER TABLE pms_product ADD INDEX idx_product_recommend (deleted, status, is_recommend, sales);

-- 4. 新品查询复合索引（覆盖 listNew: deleted + status + is_new + create_time）
ALTER TABLE pms_product ADD INDEX idx_product_new (deleted, status, is_new, create_time);

-- 5. 店家数据隔离索引（覆盖 applyStoreDataFilter: deleted + create_user_id）
ALTER TABLE pms_product ADD INDEX idx_product_store (deleted, create_user_id);
