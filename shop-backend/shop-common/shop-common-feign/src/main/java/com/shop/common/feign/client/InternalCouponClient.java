package com.shop.common.feign.client;

import com.shop.common.feign.fallback.InternalCouponClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "shop-marketing-service", contextId = "internalCouponClient",
             path = "/internal/coupons", fallbackFactory = InternalCouponClientFallbackFactory.class)
public interface InternalCouponClient {

    @PostMapping("/use")
    Result<Void> useCoupon(@RequestBody Map<String, Object> request);

    @GetMapping("/detail")
    Result<Map<String, Object>> getCouponDetail(@RequestParam("id") Long id, @RequestParam("userId") Long userId);

    @PostMapping("/restore")
    Result<Void> restoreCoupon(@RequestBody Map<String, Object> request);
}
