# 会员中心页面设计方案

- 日期：2026-06-17
- 状态：已确认
- 范围：后端接口 + 移动端（UniApp）+ Web 端（Vue3 + Naive UI）

---

## 1. 背景与目标

ShopMax 已有完整的会员等级、积分、余额、成长值后端系统，但前端只有用户中心页面的数字展示，没有独立的会员中心页面。

目标：实现信息聚合型会员中心页面，整合展示等级权益、积分/余额/成长值流水、充值功能，并启用已定义但未使用的等级折扣权益。

---

## 2. 现有能力梳理

| 能力 | 现状 | 是否需要改动 |
|------|------|-------------|
| 会员等级（4级） | `ums_user.member_level` + 硬编码阈值 | 否 |
| 等级折扣 | `LEVEL_DISCOUNTS` 定义但未启用 | **启用** |
| 积分系统 | `ums_user.integral` + `ums_integral_log` | 否 |
| 余额系统 | `ums_user.balance` + `ums_balance_log` | 否 |
| 成长值 | `ums_user.growth_value`，无独立日志表 | 否 |
| WalletController | 3 个接口（充值/积分流水/余额流水） | 否 |
| 会员信息聚合接口 | **缺失** | **新增** |

### 等级配置（硬编码在 WalletServiceImpl）

| 等级 | memberLevel | 成长值阈值 | 折扣 |
|------|-------------|-----------|------|
| 普通会员 | 1 | 0 | 无 |
| 银卡会员 | 2 | 500 | 98折 (0.98) |
| 金卡会员 | 3 | 2000 | 95折 (0.95) |
| 钻石会员 | 4 | 10000 | 9折 (0.90) |

---

## 3. 后端变更

### 3.1 新增会员信息聚合接口

**路径**：`GET /api/v1/users/me/member-info`

**返回**：
```json
{
  "memberLevel": 2,
  "memberLevelName": "银卡会员",
  "integral": 1500,
  "balance": 200.00,
  "growthValue": 800,
  "nextLevelGrowth": 2000,
  "nextLevelName": "金卡会员",
  "levelBenefits": [
    { "level": 1, "name": "普通会员", "discount": "无折扣", "threshold": 0 },
    { "level": 2, "name": "银卡会员", "discount": "98折", "threshold": 500 },
    { "level": 3, "name": "金卡会员", "discount": "95折", "threshold": 2000 },
    { "level": 4, "name": "钻石会员", "discount": "9折", "threshold": 10000 }
  ]
}
```

**实现**：在 `WalletController` 中新增方法，调用 `UserService.getById()` 获取用户数据，拼装等级配置返回。

### 3.2 启用等级折扣

**改动文件**：`OrderServiceImpl.createOrder()`

在计算 `realPay` 时，根据用户 `memberLevel` 查询对应折扣系数，乘以商品总价：

```java
// 获取用户等级折扣
double discount = getLevelDiscount(user.getMemberLevel());
BigDecimal discountedAmount = realPay.multiply(BigDecimal.valueOf(discount));
```

**新增私有方法**：
```java
private double getLevelDiscount(int memberLevel) {
    double[] discounts = {1.0, 1.0, 0.98, 0.95, 0.90};
    if (memberLevel < 1 || memberLevel > 4) return 1.0;
    return discounts[memberLevel];
}
```

**注意**：折扣应用于商品总价（不含运费），积分抵扣在折扣之后计算。

### 3.3 已有接口（无需修改）

| 接口 | 路径 | 说明 |
|------|------|------|
| 充值 | POST `/api/v1/users/me/recharge` | 已有 |
| 积分流水 | GET `/api/v1/users/me/integral-logs` | 已有，分页 |
| 余额流水 | GET `/api/v1/users/me/balance-logs` | 已有，分页 |

---

## 4. 移动端 UI 设计

### 4.1 入口

首页金刚区「会员」图标 → 跳转 `/pages/member/index`

### 4.2 会员中心主页结构（从上到下）

**1. 会员等级卡片**
- 渐变背景色（普通→灰色、银卡→蓝色、金卡→金色、钻石→紫色）
- 左侧：等级徽章 + 等级名称
- 右侧：成长值进度条（当前值/下一级阈值）
- 底部：权益标签行（如「98折」「专属客服」）

**2. 数据卡片行（三列）**
- 积分：数字 + 「查看明细 >」→ 积分流水页
- 余额：数字 + 「充值」按钮 → 充值页
- 成长值：数字

**3. 等级权益对照表**
- 四行卡片：等级图标 + 名称 + 折扣 + 所需成长值
- 当前等级高亮 + 「当前」标签

**4. 功能入口列表**
- 积分明细 → `/pages/member/integral-log`
- 余额明细 → `/pages/member/balance-log`
- 余额充值 → `/pages/member/recharge`
- 积分兑换优惠券 → `/pages/coupon/center`

### 4.3 积分流水页 `/pages/member/integral-log`

- 顶部：当前积分总数（大字）
- 分类 Tab：全部 / 收入 / 支出
- 列表项：图标 + 变动描述 + 时间 + 变动金额（绿色+ / 红色-）+ 变动后余额
- 上拉加载更多

### 4.4 余额流水页 `/pages/member/balance-log`

- 顶部：当前余额（大字）+ 充值按钮
- 分类 Tab：全部 / 充值 / 消费 / 退款
- 列表项：类型标签 + 变动描述 + 时间 + 变动金额 + 变动后余额
- 上拉加载更多

### 4.5 余额充值页 `/pages/member/recharge`

- 金额输入框
- 预设金额快捷按钮：50 / 100 / 200 / 500
- 支付方式：支付宝 / 微信（radio 选择）
- 充值按钮

### 4.6 路由注册

```json
{ "path": "pages/member/index", "style": { "navigationBarTitleText": "会员中心", "navigationStyle": "custom" } },
{ "path": "pages/member/integral-log", "style": { "navigationBarTitleText": "积分明细" } },
{ "path": "pages/member/balance-log", "style": { "navigationBarTitleText": "余额明细" } },
{ "path": "pages/member/recharge", "style": { "navigationBarTitleText": "余额充值" } }
```

### 4.7 首页金刚区接入

```js
'会员': () => uni.navigateTo({ url: '/pages/member/index' })
```

---

## 5. Web 端 UI 设计

### 5.1 路由

```js
{ path: 'member', name: 'Member', component: () => import('@/pages/member/index.vue'), meta: { title: '会员中心' } }
```

### 5.2 页面布局

**顶部**：会员等级卡片（横向通栏，渐变背景，等级徽章 + 进度条 + 权益标签）

**中部**：左右两栏
- 左栏：数据卡片（积分/余额/成长值）+ 功能入口列表
- 右栏：等级权益对照表

**底部**：Tab 切换（积分明细 / 余额明细 / 充值）
- 积分/余额明细：表格展示（时间、类型、变动金额、变动后余额、备注）
- 充值：金额输入 + 支付方式 + 充值按钮

### 5.3 首页入口

金刚区「会员中心」图标 → 跳转 `/member`（已有占位，接入即可）

---

## 6. 实现范围总结

| 模块 | 新增/修改 | 关键文件 |
|------|-----------|----------|
| 后端：会员信息接口 | 新增 | WalletController.java |
| 后端：等级折扣启用 | 修改 | OrderServiceImpl.java |
| 移动端：会员中心主页 | 新增 | pages/member/index.vue |
| 移动端：积分流水页 | 新增 | pages/member/integral-log.vue |
| 移动端：余额流水页 | 新增 | pages/member/balance-log.vue |
| 移动端：充值页 | 新增 | pages/member/recharge.vue |
| 移动端：路由 | 修改 | pages.json |
| 移动端：金刚区入口 | 修改 | pages/index/index.vue |
| 移动端：API | 修改 | shared/src/api/user.ts, wallet.ts |
| Web 端：会员中心页 | 新增 | pages/member/index.vue |
| Web 端：路由 | 修改 | router/index.ts |
| Web 端：首页入口 | 修改 | pages/home/index.vue |
