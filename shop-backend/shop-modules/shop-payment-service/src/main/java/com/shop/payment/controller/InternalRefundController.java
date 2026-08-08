package com.shop.payment.controller;

import com.shop.common.feign.dto.payment.RefundRecordDTO;
import com.shop.common.web.Result;
import com.shop.payment.entity.RefundRecord;
import com.shop.payment.service.IntraRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 退款内部接口（供 admin-service 通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/payments/refunds")
@RequiredArgsConstructor
public class InternalRefundController {

    private final IntraRefundService intraRefundService;

    @GetMapping("/page")
    public Result<Map<String, Object>> page(@RequestParam Integer pageNum,
                                            @RequestParam Integer pageSize,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(intraRefundService.pageAsMap(pageNum, pageSize, status));
    }

    @GetMapping("/order/{orderNo}")
    public Result<RefundRecordDTO> getByOrderNo(@PathVariable String orderNo) {
        RefundRecord record = intraRefundService.getByOrderNo(orderNo);
        if (record == null) {
            return Result.error(404, "退款记录不存在");
        }
        return Result.success(toDTO(record));
    }

    @PostMapping("/{refundNo}/approve")
    public Result<Map<String, Object>> approve(@PathVariable String refundNo) {
        return Result.success(intraRefundService.approve(refundNo));
    }

    @PostMapping("/{refundNo}/reject")
    public Result<Map<String, Object>> reject(@PathVariable String refundNo,
                                              @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "管理员拒绝退款");
        return Result.success(intraRefundService.reject(refundNo, reason));
    }

    @PostMapping("/{refundNo}/manual-approve")
    public Result<Map<String, Object>> manualApprove(@PathVariable String refundNo,
                                                     @RequestBody Map<String, String> body) {
        String remark = body.getOrDefault("remark", "管理员手动标记退款成功");
        return Result.success(intraRefundService.manualApprove(refundNo, remark));
    }

    private RefundRecordDTO toDTO(RefundRecord r) {
        RefundRecordDTO dto = new RefundRecordDTO();
        BeanUtils.copyProperties(r, dto);
        return dto;
    }
}
