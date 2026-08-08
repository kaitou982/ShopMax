-- ============================================================
-- ShopMax 分库迁移脚本（增量版）
-- 只迁移还在 shopmax 中的表
-- ============================================================

-- 1. 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS `shop_community` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `shop_live` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `shop_customer` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `shop_admin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 迁移社区模块表 → shop_community
RENAME TABLE shopmax.cms_note TO shop_community.cms_note;
RENAME TABLE shopmax.cms_note_image TO shop_community.cms_note_image;
RENAME TABLE shopmax.cms_note_product TO shop_community.cms_note_product;
RENAME TABLE shopmax.cms_note_like TO shop_community.cms_note_like;
RENAME TABLE shopmax.cms_note_comment TO shop_community.cms_note_comment;
RENAME TABLE shopmax.cms_note_favorite TO shop_community.cms_note_favorite;

-- 3. 迁移直播模块表 → shop_live
RENAME TABLE shopmax.lms_anchor TO shop_live.lms_anchor;
RENAME TABLE shopmax.lms_live_room TO shop_live.lms_live_room;
RENAME TABLE shopmax.lms_live_product TO shop_live.lms_live_product;
RENAME TABLE shopmax.lms_gift TO shop_live.lms_gift;
RENAME TABLE shopmax.lms_coin_log TO shop_live.lms_coin_log;

-- 4. 迁移客服模块表 → shop_customer
RENAME TABLE shopmax.csms_session TO shop_customer.csms_session;
RENAME TABLE shopmax.csms_message TO shop_customer.csms_message;
RENAME TABLE shopmax.csms_faq TO shop_customer.csms_faq;

-- 5. 迁移营销模块剩余表 → shop_marketing
RENAME TABLE shopmax.mms_group_buy_activity TO shop_marketing.mms_group_buy_activity;
RENAME TABLE shopmax.mms_group_buy_group TO shop_marketing.mms_group_buy_group;
RENAME TABLE shopmax.mms_group_buy_member TO shop_marketing.mms_group_buy_member;
RENAME TABLE shopmax.mms_promotion TO shop_marketing.mms_promotion;
RENAME TABLE shopmax.mms_promotion_scope TO shop_marketing.mms_promotion_scope;
RENAME TABLE shopmax.mms_seckill_message TO shop_marketing.mms_seckill_message;
RENAME TABLE shopmax.oms_seckill_order TO shop_marketing.oms_seckill_order;

-- 6. 迁移订单模块剩余表 → shop_order
RENAME TABLE shopmax.oms_logistics_company TO shop_order.oms_logistics_company;

-- 7. 迁移支付模块剩余表 → shop_payment
RENAME TABLE shopmax.pay_refund_record TO shop_payment.pay_refund_record;

-- 8. 迁移管理模块表 → shop_admin
RENAME TABLE shopmax.cms_banner TO shop_admin.cms_banner;
RENAME TABLE shopmax.sys_banner TO shop_admin.sys_banner;
RENAME TABLE shopmax.sys_notification TO shop_admin.sys_notification;

-- 9. 验证迁移结果
SELECT 'shop_community' AS db, COUNT(*) AS tables FROM information_schema.tables WHERE table_schema='shop_community'
UNION ALL SELECT 'shop_live', COUNT(*) FROM information_schema.tables WHERE table_schema='shop_live'
UNION ALL SELECT 'shop_customer', COUNT(*) FROM information_schema.tables WHERE table_schema='shop_customer'
UNION ALL SELECT 'shop_marketing', COUNT(*) FROM information_schema.tables WHERE table_schema='shop_marketing'
UNION ALL SELECT 'shop_order', COUNT(*) FROM information_schema.tables WHERE table_schema='shop_order'
UNION ALL SELECT 'shop_payment', COUNT(*) FROM information_schema.tables WHERE table_schema='shop_payment'
UNION ALL SELECT 'shop_admin', COUNT(*) FROM information_schema.tables WHERE table_schema='shop_admin'
UNION ALL SELECT 'shopmax_remaining', COUNT(*) FROM information_schema.tables WHERE table_schema='shopmax';
