package com.shop.common.feign.client;

import com.shop.common.feign.fallback.InternalPaymentClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "shop-payment-service", contextId = "internalPaymentClient",
             path = "/internal/payments", fallbackFactory = InternalPaymentClientFallbackFactory.class)
public interface InternalPaymentClient {

    @GetMapping("/by-order/{orderId}")
    Result<String> getPaymentNoByOrderId(@PathVariable("orderId") Long orderId);

    @PostMapping("/by-order-no/update-status")
    Result<Void> updatePaymentStatusByOrderNo(@RequestBody Map<String, Object> request);
}
