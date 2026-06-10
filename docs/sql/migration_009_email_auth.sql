-- Migration 009: 邮箱验证码注册支持
-- 将 phone 列改为可空，支持仅邮箱注册的用户

ALTER TABLE ums_user MODIFY COLUMN phone varchar(20) DEFAULT NULL COMMENT '手机号';
