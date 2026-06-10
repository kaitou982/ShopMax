-- ========================================
-- ShopMax 智能客服系统 建表脚本
-- 版本: 1.0.0
-- 日期: 2026-06-02
-- 表前缀: csms_ (Customer Service Message System)
-- ========================================

-- 3.1 客服会话表
CREATE TABLE IF NOT EXISTS csms_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_no VARCHAR(32) NOT NULL COMMENT '会话编号（格式: CS-{yyyyMMdd}-{6位随机}）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0进行中 1已结束',
    last_message_time DATETIME COMMENT '最后消息时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_session_no (session_no),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话表';

-- 3.2 客服消息表
CREATE TABLE IF NOT EXISTS csms_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(16) NOT NULL COMMENT '角色: user/assistant/system/tool',
    content TEXT COMMENT '消息内容',
    tool_calls JSON COMMENT '工具调用信息（JSON格式）',
    tool_call_id VARCHAR(128) COMMENT '工具调用结果关联ID',
    token_count INT DEFAULT 0 COMMENT 'Token消耗',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_session_id (session_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';

-- 3.3 FAQ 知识库表
CREATE TABLE IF NOT EXISTS csms_faq (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(64) COMMENT '分类: 支付/退换货/配送/发票/会员/售后/其他',
    question VARCHAR(512) NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FAQ知识库表';

-- ========================================
-- 3.6 FAQ 种子数据（30条）
-- ========================================

-- 支付类
INSERT INTO csms_faq (category, question, answer, sort_order) VALUES
('支付', '支持哪些支付方式？', '我们支持微信支付、支付宝、银行卡支付三种方式，您可以在结算页面选择最方便的支付方式。', 1),
('支付', '支付失败怎么办？', '请先检查银行卡余额是否充足，或尝试更换支付方式。如果多次尝试仍失败，请在聊天窗口输入"人工客服"寻求帮助。', 2),
('支付', '支付后多久确认到账？', '微信支付和支付宝通常是即时到账。银行卡支付根据银行不同可能需要1-3分钟。如果超过10分钟仍未确认，请联系客服。', 3),
('支付', '可以使用优惠券吗？', '可以的！在结算页面点击"使用优惠券"，选择您要使用的优惠券即可自动抵扣。注意每笔订单只能使用一张优惠券。', 4);

-- 退换货类
INSERT INTO csms_faq (category, question, answer, sort_order) VALUES
('退换货', '退货流程是什么？', '在"我的订单"中找到对应订单，点击"申请退货"，填写退货原因并上传凭证照片，提交后等待审核。审核通过后，您会收到退货地址，按指引寄回商品并填写运单号即可。', 5),
('退换货', '多久可以退货？', '签收后7天内可申请无理由退货，商品需保持原包装完好、不影响二次销售。如果是质量问题，请在签收后24小时内拍照联系客服处理。', 6),
('退换货', '退款多久到账？', '审核通过后，微信/支付宝退款1-3个工作日到账，银行卡退款3-7个工作日到账。您可以在"我的订单"中查看退款进度。', 7),
('退换货', '退货的运费谁承担？', '因商品质量问题导致的退货，运费由我们承担（请先垫付，收到退货后返还）。非质量问题的无理由退货，运费由您承担。', 8),
('退换货', '换货怎么操作？', '目前暂不支持直接换货。您可以先申请退货退款，再重新下单购买需要的商品。给您带来不便敬请谅解。', 9);

-- 配送类
INSERT INTO csms_faq (category, question, answer, sort_order) VALUES
('配送', '多久能发货？', '正常订单下单后24小时内发货（节假日顺延）。预售商品按商品页面标注的时间发货，请您留意商品详情页的预售说明。', 10),
('配送', '如何查询物流？', '在"我的订单"中找到对应订单，点击进入订单详情页即可查看实时物流跟踪信息。', 11),
('配送', '配送范围和费用？', '全国大部分地区包邮。部分偏远地区（新疆、西藏、青海等）可能产生额外运费，系统会在下单时自动计算并提示。', 12),
('配送', '可以修改收货地址吗？', '下单后如果还未发货，可以在"我的订单"中修改收货地址。如果已经发货，请联系客服协助处理。', 13),
('配送', '收到包裹破损怎么办？', '请先拍照保留证据（外包装和商品），然后拒收或在签收后24小时内联系客服，我们将为您处理补发或退款。', 14);

-- 发票类
INSERT INTO csms_faq (category, question, answer, sort_order) VALUES
('发票', '如何开发票？', '下单时在结算页面的"发票信息"栏填写开票信息即可。我们支持电子发票和纸质发票两种形式，电子发票会发送到您的邮箱。', 15),
('发票', '发票可以补开吗？', '下单后30天内可以在"我的订单"中找到对应订单申请补开发票。超过30天的订单请联系客服协助处理。', 16),
('发票', '发票抬头写错了怎么办？', '如果发票还未开具，可以在订单详情中修改发票信息。如果已经开具，请联系客服作废原发票后重新开具。', 17);

-- 会员类
INSERT INTO csms_faq (category, question, answer, sort_order) VALUES
('会员', '会员有什么权益？', '会员享受专属折扣价、生日礼包、双倍积分、优先客服、专属活动等权益。等级越高，权益越多！详情可在"我的-会员中心"查看。', 18),
('会员', '如何成为会员？', '注册即为基础会员。累计消费满1000元自动升级为银卡会员，满5000元升级为金卡会员，满10000元升级为钻石会员。', 19),
('会员', '积分有什么用？', '积分可以在下单时抵扣现金（100积分=1元），也可以在积分商城兑换商品或优惠券。积分有效期为获得之日起1年。', 20),
('会员', '积分怎么获取？', '消费1元得1积分，每日签到可得5-20积分，参与平台活动可以获得额外积分奖励。', 21);

-- 售后类
INSERT INTO csms_faq (category, question, answer, sort_order) VALUES
('售后', '商品有质量问题怎么办？', '请在签收后24小时内拍照或录视频联系客服，我们将核实后为您办理换货或退款，并承担来回运费。', 22),
('售后', '收到的商品和描述不符？', '请拍照保留证据并联系客服，我们核实后将为您办理退货退款，运费由我们承担。给您带来的不便深表歉意。', 23),
('售后', '商品漏发了怎么办？', '请先核对包裹内的发货清单，确认后联系客服，我们会尽快为您补发漏发的商品。', 24),
('售后', '如何联系人工客服？', '工作时间（9:00-21:00）在本聊天窗口输入"人工客服"即可转接人工服务。非工作时间可以留言，我们会尽快回复。', 25);

-- 其他类
INSERT INTO csms_faq (category, question, answer, sort_order) VALUES
('其他', '如何修改密码？', '在"我的-设置-账户安全"中点击"修改密码"，输入原密码和新密码即可完成修改。', 26),
('其他', '账号被盗怎么办？', '请立即联系客服冻结账号，然后通过"忘记密码"功能重置密码。建议开启手机验证登录提高安全性。', 27),
('其他', '可以注销账号吗？', '在"我的-设置-账户安全"中申请注销账号。注销后所有数据将被永久删除且不可恢复，请谨慎操作。有未完成订单时无法注销。', 28),
('其他', 'App支持哪些手机系统？', '我们支持iOS 13.0及以上版本、Android 8.0及以上版本。您也可以在手机浏览器中访问我们的H5网页版。', 29),
('其他', '营业时间是什么时候？', '我们的在线客服工作时间为每天9:00-21:00（含节假日）。您也可以随时使用智能客服助手查询常见问题。', 30);
