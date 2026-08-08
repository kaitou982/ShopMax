-- ===================================================
-- 性能优化索引 — community feed + order queries
-- 解决 N+1 查询优化后仍需要的数据库层面索引
-- ===================================================

-- 1. 社区笔记信息流：按状态 + 时间排序查询
-- 对应 NoteServiceImpl.page() ORDER BY create_time DESC WHERE status = 1
ALTER TABLE `cms_note` ADD INDEX `idx_status_create_time` (`status`, `create_time`);

-- 2. 社区笔记按用户分页：用户 + 状态 + 时间
-- 对应 NoteServiceImpl.pageByUserId()
ALTER TABLE `cms_note` ADD INDEX `idx_user_status_create` (`user_id`, `status`, `create_time`);

-- 3. 社区评论：笔记 + 父评论 + 时间排序
-- 对应 NoteCommentServiceImpl.pageByNoteId()
ALTER TABLE `cms_note_comment` ADD INDEX `idx_note_parent_create` (`note_id`, `parent_id`, `create_time`);

-- 4. 订单状态 + 时间：超时取消扫描 + 仪表盘统计
-- 对应 OrderServiceImpl.autoCancelTimeoutOrders()
ALTER TABLE `oms_order` ADD INDEX `idx_status_create_time` (`status`, `create_time`);

-- 5. 仪表盘统计：日期范围查询优化
-- 对应 OrderMapper 中的 sumTodaySales / countTodayOrders 等
-- 将 DATE(create_time) = CURDATE() 改为范围查询后可利用此索引
ALTER TABLE `oms_order` ADD INDEX `idx_create_time` (`create_time`);
