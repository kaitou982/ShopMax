package com.shop.common.feign.client;

import com.shop.common.feign.fallback.InternalUserClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "shop-user-service", path = "/internal/users",
             fallbackFactory = InternalUserClientFallbackFactory.class)
public interface InternalUserClient {

    @GetMapping("/{id}/member-level")
    Result<Integer> getMemberLevel(@PathVariable("id") Long id);

    @PostMapping("/{id}/deduct-integral")
    Result<Void> deductIntegral(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/{id}/add-integral")
    Result<Void> addIntegral(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/{id}/deduct-balance")
    Result<Void> deductBalance(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/{id}/add-balance")
    Result<Void> addBalance(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/{id}/add-growth")
    Result<Void> addGrowthValue(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);
}
