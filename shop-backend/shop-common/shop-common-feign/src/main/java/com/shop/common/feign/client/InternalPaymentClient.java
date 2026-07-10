package com.shop.common.feign.client;

import com.shop.common.feign.fallback.InternalPaymentClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "shop-payment-service", path = "/internal/payments",
             fallbackFactory = InternalPaymentClientFallbackFactory.class)
public interface InternalPaymentClient {

    @GetMapping("/by-order/{orderId}")
    Result<String> getPaymentNoByOrderId(@PathVariable("orderId") Long orderId);
}
