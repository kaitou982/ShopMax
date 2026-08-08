package com.shop.payment.controller;

import com.shop.common.web.Result;
import com.shop.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付服务内部接口（供其他微服务通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/by-order/{orderId}")
    public Result<String> getPaymentNoByOrderId(@PathVariable Long orderId) {
        String paymentNo = paymentService.getPaymentNoByOrderId(orderId);
        return Result.success(paymentNo);
    }

    @PostMapping("/by-order-no/update-status")
    public Result<Void> updatePaymentStatusByOrderNo(@RequestBody Map<String, Object> request) {
        String orderNo = (String) request.get("orderNo");
        Integer status = (Integer) request.get("status");
        paymentService.updatePaymentStatusByOrderNo(orderNo, status);
        return Result.success();
    }
}
