-- 优惠券领取记录唯一索引：防止同一用户重复领同一张券
ALTER TABLE mms_coupon_receive ADD UNIQUE KEY uk_coupon_user (coupon_id, user_id);

-- 订单表：关联使用的优惠券记录
ALTER TABLE oms_order ADD COLUMN user_coupon_id BIGINT DEFAULT NULL AFTER integral_amount;
