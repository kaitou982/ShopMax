# 旧订单退款问题修复说明

## 问题描述

旧订单申请退款功能出现两个问题：
1. **未找到退款记录** - 管理员审核退款时，系统提示未找到退款记录
2. **支付宝退款失败: 交易不存在** - 调用支付宝退款接口时，支付宝返回交易不存在的错误

## 根本原因分析

### 问题1: 未找到退款记录

**根本原因**：前端代码查询退款记录的逻辑存在问题

**代码位置**：`shop-admin-ui\src\views\order\index.vue` 第209-223行

```javascript
// 原代码 - 只查询处理中的第一页记录
const res = await getRefundList({ pageNum: 1, pageSize: 1, status: 0 })
const match = res.records.find((r: RefundRecord) => r.orderNo === row.orderNo)
```

**问题**：
1. 只查询了 `status: 0`（处理中）的退款记录
2. 只查询了第一页的1条记录
3. 如果退款记录不在第一页，或者退款记录的状态不是0，就找不到

### 问题2: 支付宝退款失败: 交易不存在

**根本原因**：旧订单在支付宝沙箱环境中的交易记录已不存在

**代码位置**：`shop-backend\shop-modules\shop-admin-service\src\main\java\com\shop\admin\service\RefundAdminService.java` 第86-99行

**问题**：
1. 旧订单的支付可能是在另一个沙箱环境中完成的
2. 沙箱环境可能已经重置，导致交易记录丢失
3. 支付宝返回"交易不存在"的错误

## 修复方案

### 修复1: 添加后端接口 - 根据订单号查询退款记录

**文件**：`shop-backend\shop-modules\shop-admin-service\src\main\java\com\shop\admin\controller\RefundAdminController.java`

```java
@Operation(summary = "根据订单号查询退款记录")
@GetMapping("/order/{orderNo}")
public Result<RefundRecord> getByOrderNo(@PathVariable String orderNo) {
    return Result.success(refundAdminService.getByOrderNo(orderNo));
}
```

**文件**：`shop-backend\shop-modules\shop-admin-service\src\main\java\com\shop\admin\service\RefundAdminService.java`

```java
/** 根据订单号查询退款记录（取最新一条） */
public RefundRecord getByOrderNo(String orderNo) {
    LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
    w.eq(RefundRecord::getOrderNo, orderNo);
    w.orderByDesc(RefundRecord::getCreateTime);
    w.last("LIMIT 1");
    return refundRecordMapper.selectOne(w);
}
```

### 修复2: 修改前端查询逻辑

**文件**：`shop-admin-ui\src\views\order\index.vue`

```javascript
// 修改后 - 根据订单号精确查询
const handleRefundReview = async (row: Order) => {
  refundLoading.value = true
  refundDialogVisible.value = true
  currentRefund.value = null
  rejectReason.value = ''
  try {
    const refund = await getRefundByOrderNo(row.orderNo)
    currentRefund.value = refund || null
  } catch {
    ElMessage.error('获取退款记录失败')
  } finally {
    refundLoading.value = false
  }
}
```

### 修复3: 添加手动标记退款功能

**适用场景**：旧订单在支付宝/微信中的交易记录已不存在，无法通过网关退款

**后端接口**：

```java
@Operation(summary = "手动标记退款成功（用于旧订单支付网关不可用的情况）")
@PostMapping("/{refundNo}/manual-approve")
@PreAuthorize("hasRole('ADMIN')")
public Result<Map<String, Object>> manualApprove(@PathVariable String refundNo,
                                                  @RequestBody Map<String, String> body) {
    String remark = body.getOrDefault("remark", "管理员手动标记退款成功");
    return Result.success(refundAdminService.manualApprove(refundNo, remark));
}
```

**前端按钮**：
- 在退款审核弹窗中添加"手动标记退款"按钮
- 仅在退款状态为"处理中"或"失败"时显示
- 添加提示信息说明使用场景

## 修改的文件列表

1. **后端控制器**
   - `shop-backend\shop-modules\shop-admin-service\src\main\java\com\shop\admin\controller\RefundAdminController.java`
     - 添加 `getByOrderNo` 接口
     - 添加 `manualApprove` 接口

2. **后端服务**
   - `shop-backend\shop-modules\shop-admin-service\src\main\java\com\shop\admin\service\RefundAdminService.java`
     - 添加 `getByOrderNo` 方法
     - 添加 `manualApprove` 方法

3. **前端API**
   - `shop-admin-ui\src\api\modules\refund.ts`
     - 添加 `getRefundByOrderNo` 接口
     - 添加 `manualApproveRefund` 接口

4. **前端页面**
   - `shop-admin-ui\src\views\order\index.vue`
     - 修改 `handleRefundReview` 方法
     - 添加 `handleManualApproveRefund` 方法
     - 在退款审核弹窗中添加手动标记退款按钮

## 测试建议

1. **测试退款记录查询**
   - 创建新订单并申请退款
   - 在管理员后台审核退款，确认能正确显示退款记录

2. **测试手动标记退款**
   - 模拟旧订单场景（支付宝交易不存在）
   - 使用手动标记退款功能
   - 确认订单状态更新为"已退款"
   - 确认库存已恢复

3. **测试退款拒绝**
   - 拒绝退款申请
   - 确认订单状态恢复为"待发货"

## 注意事项

1. 手动标记退款功能仅用于处理旧订单的特殊情况，不应在正常流程中使用
2. 使用手动标记退款时，系统会记录操作备注，便于审计追溯
3. 对于余额支付的订单，手动标记退款会自动执行余额退还
