package com.shop.admin.controller;

import com.shop.admin.service.RefundAdminService;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.payment.entity.RefundRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 退款审核控制器
 *
 * @author shop
 * @since 2026-06-01
 */
@Tag(name = "退款审核")
@RestController
@RequestMapping("/api/v1/admin/refunds")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STORE')")
public class RefundAdminController {

    private final RefundAdminService refundAdminService;

    @Operation(summary = "分页查询退款记录")
    @GetMapping
    public Result<PageResult<RefundRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(refundAdminService.page(pageNum, pageSize, status));
    }

    @Operation(summary = "根据订单号查询退款记录")
    @GetMapping("/order/{orderNo}")
    public Result<RefundRecord> getByOrderNo(@PathVariable String orderNo) {
        return Result.success(refundAdminService.getByOrderNo(orderNo));
    }

    @Operation(summary = "批准退款（触发支付网关退款）")
    @PostMapping("/{refundNo}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> approve(@PathVariable String refundNo) {
        return Result.success(refundAdminService.approve(refundNo));
    }

    @Operation(summary = "拒绝退款")
    @PostMapping("/{refundNo}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> reject(@PathVariable String refundNo,
                                               @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "管理员拒绝退款");
        return Result.success(refundAdminService.reject(refundNo, reason));
    }

    @Operation(summary = "手动标记退款成功（用于旧订单支付网关不可用的情况）")
    @PostMapping("/{refundNo}/manual-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> manualApprove(@PathVariable String refundNo,
                                                      @RequestBody Map<String, String> body) {
        String remark = body.getOrDefault("remark", "管理员手动标记退款成功");
        return Result.success(refundAdminService.manualApprove(refundNo, remark));
    }
}
