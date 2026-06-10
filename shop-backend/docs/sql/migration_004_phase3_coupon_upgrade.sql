-- Phase 3: 积分兑换券 + 叠加规则 + 邀请好友得券

-- 1. 优惠券表: 积分兑换 + 叠加
ALTER TABLE mms_coupon ADD COLUMN integral_cost INT DEFAULT 0 AFTER applicable_ids;
ALTER TABLE mms_coupon ADD COLUMN stackable TINYINT DEFAULT 0 AFTER integral_cost;

-- 2. 订单表: 第二张叠加券
ALTER TABLE oms_order ADD COLUMN user_coupon_id2 BIGINT DEFAULT NULL AFTER user_coupon_id;

-- 3. 用户表: 邀请系统
ALTER TABLE ums_user ADD COLUMN referral_code VARCHAR(32) DEFAULT NULL AFTER last_login_ip;
ALTER TABLE ums_user ADD COLUMN inviter_id BIGINT DEFAULT NULL AFTER referral_code;
ALTER TABLE ums_user ADD INDEX idx_referral_code (referral_code);
ALTER TABLE ums_user ADD INDEX idx_inviter_id (inviter_id);
