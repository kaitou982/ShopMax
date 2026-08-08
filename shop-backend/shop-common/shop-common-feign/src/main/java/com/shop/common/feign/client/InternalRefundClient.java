package com.shop.common.feign.client;

import com.shop.common.feign.dto.payment.RefundRecordDTO;
import com.shop.common.feign.fallback.InternalRefundClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 退款内部调用 Feign 客户端
 */
@FeignClient(name = "shop-payment-service", contextId = "internalRefundClient",
             path = "/internal/payments/refunds", fallbackFactory = InternalRefundClientFallbackFactory.class)
public interface InternalRefundClient {

    @GetMapping("/page")
    Result<Map<String, Object>> page(@RequestParam("pageNum") Integer pageNum,
                                     @RequestParam("pageSize") Integer pageSize,
                                     @RequestParam(value = "status", required = false) Integer status);

    @GetMapping("/order/{orderNo}")
    Result<RefundRecordDTO> getByOrderNo(@PathVariable("orderNo") String orderNo);

    @PostMapping("/{refundNo}/approve")
    Result<Map<String, Object>> approve(@PathVariable("refundNo") String refundNo);

    @PostMapping("/{refundNo}/reject")
    Result<Map<String, Object>> reject(@PathVariable("refundNo") String refundNo,
                                       @RequestBody Map<String, String> body);

    @PostMapping("/{refundNo}/manual-approve")
    Result<Map<String, Object>> manualApprove(@PathVariable("refundNo") String refundNo,
                                              @RequestBody Map<String, String> body);
}
